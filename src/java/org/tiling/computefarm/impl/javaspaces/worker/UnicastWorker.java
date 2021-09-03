package org.tiling.computefarm.impl.javaspaces.worker;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.tiling.computefarm.impl.javaspaces.ResultEntry;
import org.tiling.computefarm.impl.javaspaces.TaskEntry;
import org.tiling.computefarm.impl.javaspaces.util.ServiceFinder;

/**
 * A {@link UnicastWorker} repeatedly takes a {@link TaskEntry} instance from the space, executes its
 * payload then writes the result back wrapped in a {@link ResultEntry}. 
 */
public class UnicastWorker extends WorkerSupport implements Serializable, Runnable {

	private static final Logger logger = Logger.getLogger(UnicastWorker.class.getName());
	
	private final LookupLocator lookupLocator;
	private JavaSpace javaSpace;
	private TransactionManager transactionManager;
	private WorkerThread workerThread;
	
	public UnicastWorker(LookupLocator lookupLocator) {
	    this.lookupLocator = lookupLocator;
	}
		
	public void run() {
		fireStateChanged(WorkerEventType.WORKER_STARTED);
	    while (true) {
		    try {
                ServiceRegistrar registrar = lookupLocator.getRegistrar();
                javaSpace = (JavaSpace) ServiceFinder.findService(registrar, JavaSpace.class);
                transactionManager = (TransactionManager) ServiceFinder.findService(registrar, TransactionManager.class);
    			if (javaSpace != null && transactionManager != null) {
    				fireStateChanged(WorkerEventType.CONNECTED);
    				workerThread = new WorkerThread(this, javaSpace, transactionManager, false);
    				workerThread.start();
    				try {
                        workerThread.join();
                    } catch (InterruptedException e) {
                        // continue
                    }
                    if (workerThread.aborted()) {
                    	break;
                    }
    			}
            } catch (RemoteException e) {
				logger.log(Level.FINE, "Error while finding service. Discarding.", e);						
            } catch (IOException e) {
				logger.log(Level.FINE, "Error while finding service. Discarding.", e);						
            } catch (ClassNotFoundException e) {
				logger.log(Level.FINE, "Error while finding service. Discarding.", e);						
            }
			discard();
			try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // continue
            }
	    }
	}
	
	public void discard() {
		javaSpace = null;
		transactionManager = null;
		fireStateChanged(WorkerEventType.DISCONNECTED);
	}
	
    /**
     * Cancels the current transaction and terminates the worker thread.
     * The method will return within the specified timeout period, whether the transaction was successfully aborted or not.
     */
    public void abort(long waitFor) {
		logger.log(Level.FINE, "Aborting.");
    	if (workerThread != null) {
    		workerThread.abort(waitFor);
    	}
    }	
	
	// A static reference to prevent the Worker from being GC'ed.
	// See http://pandonia.canberra.edu.au/java/jini/tutorial/Jeri.xml#Garbage%20Collection
	private static UnicastWorker worker;
	
    public static void main(String[] args) throws Throwable {
    	System.setProperty("java.security.policy", "policy.all");
    	System.setSecurityManager(new RMISecurityManager());
    	
        if (args.length == 0) {
            System.err.println("Usage: UnicastWorker jini-url");
            System.exit(1);
        }
		worker = new UnicastWorker(new LookupLocator(args[0]));
		worker.run();
    }

}
