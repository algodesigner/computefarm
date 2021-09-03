package org.tiling.computefarm.impl.javaspaces;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscovery;
import net.jini.space.JavaSpace;

import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;
import org.tiling.computefarm.impl.AbstractJobRunner;
import org.tiling.computefarm.impl.javaspaces.util.ServiceFinder;
import org.tiling.computefarm.impl.javaspaces.worker.Worker;

/**
 * A {@link JobRunner} that uses JavaSpaces. It takes care of the
 * JavaSpace discovery lifecycle. <code>JobRunner</code> is a thread that initiates the
 * process of discovery of a JavaSpace, then once a JavaSpace has been found the job
 * is run. The <code>JobRunner</code> thread terminates when the job completes. 
 */
public class JavaSpacesJobRunner extends AbstractJobRunner {

	private static final Logger logger = Logger.getLogger(JavaSpacesJobRunner.class.getName());

	private final DiscoveryManagement discoveryManagement;

	class Listener implements DiscoveryListener {

	    private JavaSpace javaSpace;
		private TransactionManager transactionManager;
		
		public synchronized void discovered(DiscoveryEvent event) {
			logger.entering(getClass().getName(), "discovered");
			if (computeSpace != null) {
				return;
			}

			ServiceRegistrar[] regs = event.getRegistrars();
			for (int i = 0, size = regs.length; i < size; i++) {
				if (computeSpace == null) {
					try {
					    foundService(ServiceFinder.findService(regs[i], JavaSpace.class));
					    foundService(ServiceFinder.findService(regs[i], TransactionManager.class));
					} catch (RemoteException e) {
						logger.log(Level.FINE, "Error while finding service. Discarding.", e);						
						discoveryManagement.discard(regs[i]);
					}
				}
			}
			logger.exiting(getClass().getName(), "discovered");
		}
		
	    /**
	     * A method for signalling all {@link Worker}s that they should
	     * reload their classes.
	     * Exceptions are logged but otherwise ignored.
	     * @param javaSpace the JavaSpace that this <code>Job</code> is using
	     */
	    private void sendReloadClassesSignal(JavaSpace javaSpace) {
	        if (Boolean.getBoolean("org.tiling.computefarm.impl.javaspaces.DontReloadClasses")) {
	            return;
	        }
	        try {
	            ReloadClassesEntry entry = new ReloadClassesEntry();
	            javaSpace.write(entry, null, 10 * 1000);
	            javaSpace.take(entry, null, JavaSpace.NO_WAIT);
	        } catch (RemoteException e) {
	            logger.log(Level.WARNING, "Exception while sending reload classes signal.", e);
	        } catch (TransactionException e) {
	            logger.log(Level.WARNING, "Exception while sending reload classes signal.", e);
	        } catch (UnusableEntryException e) {
	            logger.log(Level.WARNING, "Exception while sending reload classes signal.", e);
	        } catch (InterruptedException e) {
	            logger.log(Level.WARNING, "Exception while sending reload classes signal.", e);
	        }
	    }		

		public void discarded(DiscoveryEvent event) {
			logger.entering(getClass().getName(), "discarded");
			computeSpace = null;			
			logger.exiting(getClass().getName(), "discarded");
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
				computeSpace = new JavaSpaceComputeSpace(javaSpace, transactionManager);
				sendReloadClassesSignal(javaSpace);
				new GenerateThread().start();
				new CollectThread().start();
			}		
		}		
		
	}

	public JavaSpacesJobRunner(Job job) throws IOException {
	    super(job);
		this.discoveryManagement = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
	}
	
    public void startGenerateAndCollectThreads() {
		discoveryManagement.addDiscoveryListener(new Listener());		
    }
	
}
