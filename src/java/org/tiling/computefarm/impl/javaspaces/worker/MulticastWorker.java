package org.tiling.computefarm.impl.javaspaces.worker;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.config.ConfigurationException;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.lease.LeaseRenewalManager;
import net.jini.space.JavaSpace;

import org.tiling.computefarm.impl.javaspaces.ResultEntry;
import org.tiling.computefarm.impl.javaspaces.TaskEntry;
import org.tiling.computefarm.impl.javaspaces.util.ClassServer;
import org.tiling.computefarm.impl.javaspaces.util.ServiceFinder;

/**
 * A {@link MulticastWorker} repeatedly takes a {@link TaskEntry} instance from the space, executes its
 * payload then writes the result back wrapped in a {@link ResultEntry}. 
 */
public class MulticastWorker extends WorkerSupport implements Serializable {

	private static final Logger logger = Logger.getLogger(MulticastWorker.class.getName());
	
	private final LookupLocator[] lookupLocators;
	private DiscoveryManagement discoveryManagement;
	private final Set serviceRegistrars = new HashSet();
	private final LeaseRenewalManager leaseRenewalManager = new LeaseRenewalManager();

	private JavaSpace javaSpace;
	private TransactionManager transactionManager;
	private WorkerThread workerThread;
	
	private class Listener implements DiscoveryListener {
		
		private EventListener eventListener;
		
		public synchronized void discovered(DiscoveryEvent event) {
			logger.entering(getClass().getName(), "discovered");
			if (javaSpace != null && transactionManager != null) {
				return;
			}

			ServiceRegistrar[] regs = event.getRegistrars();
			for (int i = 0, size = regs.length; i < size; i++) {
				
				try {
					registerNotifications(regs[i], JavaSpace.class);
					registerNotifications(regs[i], TransactionManager.class);
					foundService(findService(regs[i], JavaSpace.class));
					foundService(findService(regs[i], TransactionManager.class));
				} catch (ConfigurationException e) {
					logger.log(Level.FINE, "Exporter configuration error. Discarding.", e);						
					discoveryManagement.discard(regs[i]);
				} catch (RemoteException e) {
					logger.log(Level.FINE, "Error while finding service. Discarding.", e);						
					discoveryManagement.discard(regs[i]);
				}
				
			}
			logger.exiting(getClass().getName(), "discovered");
		}
    
		public synchronized void discarded(DiscoveryEvent event) {
			logger.entering(getClass().getName(), "discarded");
			javaSpace = null;
			transactionManager = null;
			workerThread = null;
			ServiceRegistrar[] regs = event.getRegistrars();
			for (int i = 0, size = regs.length; i < size; i++) {
				serviceRegistrars.remove(regs[i]);
			}
			logger.exiting(getClass().getName(), "discarded");
		}

		private void registerNotifications(ServiceRegistrar registrar, Class serviceClass) throws RemoteException, ConfigurationException {
			eventListener = new EventListener();
			EventRegistration registration = registrar.notify(
				new ServiceTemplate(null, new Class[] { serviceClass }, null),
				ServiceRegistrar.TRANSITION_NOMATCH_MATCH,
				eventListener.getRemoteEventListener(),
				null,
				Lease.ANY
			);
			leaseRenewalManager.renewUntil(registration.getLease(), Lease.ANY, null);
		}

		private Object findService(ServiceRegistrar registrar, Class serviceClass) throws RemoteException {
			Object service = ServiceFinder.findService(registrar, serviceClass);
			if (service != null) {
				logger.fine("Found a service! " + service);
				serviceRegistrars.add(registrar);
				return service;
			}
			return null;
		}
	}
	
	private class EventListener implements RemoteEventListener {

    	private final RemoteEventListener remoteEventListener;
		protected EventListener() throws RemoteException, ConfigurationException {
		    Exporter exporter = Config.getExporter("MulticastWorker");
			this.remoteEventListener = (RemoteEventListener) exporter.export(this);
		}
		public RemoteEventListener getRemoteEventListener() {
			return remoteEventListener;
		}

		public void notify(RemoteEvent remoteEvent) {
			logger.entering(getClass().getName(), "notify");
			if (remoteEvent instanceof ServiceEvent) {
				ServiceEvent serviceEvent = (ServiceEvent) remoteEvent;
				foundService(serviceEvent.getServiceItem().service);
			}
			logger.exiting(getClass().getName(), "notify");
		}

	}

	private synchronized void foundService(Object service) {
		if (service == null) {
			return;
		}
		if (service instanceof JavaSpace) {
			logger.fine("Found a JavaSpace!");
			this.javaSpace = (JavaSpace) service;			
		} else if (service instanceof TransactionManager) {
			logger.fine("Found a TransactionManager!");
			this.transactionManager = (TransactionManager) service;
		}
		if (javaSpace != null && transactionManager != null) {
			fireStateChanged(WorkerEventType.CONNECTED);
			workerThread = new WorkerThread(this, javaSpace, transactionManager);
			workerThread.start();		
		}		
	}
	
	public MulticastWorker(LookupLocator[] lookupLocators) {
		this.lookupLocators = lookupLocators;
	}	
	
	public void run() throws IOException {
		fireStateChanged(WorkerEventType.WORKER_STARTED);
		discoveryManagement = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS, lookupLocators, new Listener());
	}
	
	public void discard() {
		for (Iterator i = serviceRegistrars.iterator(); i.hasNext();) {
			ServiceRegistrar serviceRegistrar = (ServiceRegistrar) i.next();
			discoveryManagement.discard(serviceRegistrar);
		}
		fireStateChanged(WorkerEventType.CONNECTED);	
	}
	
	// A static reference to prevent the Worker from being GC'ed.
	// See http://pandonia.canberra.edu.au/java/jini/tutorial/Jeri.xml#Garbage%20Collection
	private static MulticastWorker worker;
	
	private static ClassServer classServer;
	
    public static void main(String[] args) throws Throwable {
    	turnOffUrlConnectionCaching();
		startClassServer();
		LookupLocator[] lookupLocators = new LookupLocator[args.length];
		for (int i = 0; i < args.length; i++) {
			lookupLocators[i] = new LookupLocator(args[i]);
		}
		worker = new MulticastWorker(lookupLocators);
		worker.run();
    }

	private static void turnOffUrlConnectionCaching() throws IOException, MalformedURLException {
	    if (Boolean.getBoolean("org.tiling.computefarm.impl.javaspaces.DontReloadClasses")) {
	    	return;
		}		
		new URL("http://localhost/").openConnection().setDefaultUseCaches(false);
	}

	private static void startClassServer() throws IOException {
	    classServer = new ClassServer(ClassServer.DEFAULT_ROOT, ClassServer.DEFAULT_START_PORT_NUMBER, false);
	    classServer.start();
	}

}
