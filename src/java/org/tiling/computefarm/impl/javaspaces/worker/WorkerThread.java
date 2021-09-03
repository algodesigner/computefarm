package org.tiling.computefarm.impl.javaspaces.worker;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.export.Exporter;
import net.jini.space.JavaSpace;

import org.tiling.computefarm.impl.javaspaces.ReloadClassesEntry;
import org.tiling.computefarm.impl.javaspaces.ResultEntry;
import org.tiling.computefarm.impl.javaspaces.TaskEntry;

class WorkerThread extends Thread {
	private static final Logger logger = Logger.getLogger(WorkerThread.class.getName());	
	
	private static final long MAX_TASK_EXECUTION_TIME = Long.parseLong(
	        System.getProperty("org.tiling.computefarm.impl.javaspaces.MaxTaskExecutionTime", "300000") // default to 5 minutes
	);

	private final WorkerSupport workerSupport;
	private final JavaSpace javaSpace;
	private final TransactionManager transactionManager;
	private final boolean reloadClasses;

	private Transaction txn;
	private boolean aborted;
	
    public WorkerThread(WorkerSupport workerSupport, JavaSpace javaSpace, TransactionManager transactionManager) {
    	this(workerSupport, javaSpace, transactionManager, true);
    }
    
    public WorkerThread(WorkerSupport workerSupport, JavaSpace javaSpace, TransactionManager transactionManager, boolean reloadClasses) {
    	this.workerSupport = workerSupport;
    	this.javaSpace = javaSpace;
    	this.transactionManager = transactionManager;
    	this.reloadClasses = reloadClasses;
    }
    
    public class ReloadClassesEventListener implements RemoteEventListener {
    	private final RemoteEventListener remoteEventListener;
		protected ReloadClassesEventListener() throws RemoteException, ConfigurationException {
		    Exporter exporter = Config.getExporter("WorkerThread");
			this.remoteEventListener = (RemoteEventListener) exporter.export(this);
		}
		public RemoteEventListener getRemoteEventListener() {
			return remoteEventListener;
		}
		public void notify(RemoteEvent remoteEvent) {
			logger.fine("Reload classes event received.");
			System.gc();
		}
    }

    public void run() {
        try {
        	
            if (reloadClasses) {
                try {
                    ReloadClassesEventListener reloadClassesEventListener = new ReloadClassesEventListener();
	        		javaSpace.notify(
							new ReloadClassesEntry(), 
							null, 
							reloadClassesEventListener.getRemoteEventListener(), 
							Lease.FOREVER, 
							null
					);
                } catch (ConfigurationException e) {
					logger.log(Level.WARNING, "Exporter configuration error.", e);
				} catch (TransactionException e) {
					logger.log(Level.WARNING, "Exception while creating notification.", e);
				}
            }
        	
			Entry taskTemplate = javaSpace.snapshot(new TaskEntry());

			while (!aborted()) {
				logger.fine("Getting task");

				try {
					txn = TransactionFactory.create(transactionManager, MAX_TASK_EXECUTION_TIME).transaction;
					TaskEntry taskEntry =
						(TaskEntry) javaSpace.take(taskTemplate, txn, MAX_TASK_EXECUTION_TIME / 10);
					if (taskEntry != null) {
						workerSupport.fireStateChanged(WorkerEventType.TASK_RECEIVED);
						workerSupport.fireStateChanged(WorkerEventType.COMPUTATION_STARTED);
						ResultEntry result = new ResultEntry(taskEntry.jobId, taskEntry.task.execute());
						if (result != null) {
							javaSpace.write(result, txn, taskEntry.getMaxResultLifetime());
						}
					}
					txn.commit();
				} catch (LeaseDeniedException e) {
					logger.log(Level.WARNING, "Exception while creating transaction", e);
				} catch (TransactionException e) {
					logger.log(Level.WARNING, "Exception while taking/writing task", e);
				} catch (UnusableEntryException e) {
					logger.log(Level.WARNING, "Exception while taking task", e);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Exception while taking/writing task", e);
					if (txn != null) {
						try {
							txn.abort();
						} catch (Exception ex) {
							logger.log(Level.FINE, "Exception while aborting transaction", ex);
						}
					}
				}
				workerSupport.fireStateChanged(WorkerEventType.COMPUTATION_FINISHED);
			}
		} catch (RemoteException e) {
			logger.log(Level.FINE, "RemoteException while running. Discarding.", e);
			workerSupport.discard();
		}
    }
    
    public synchronized boolean aborted() {
    	return aborted;
    }
    
    /**
     * Cancels the current transaction and terminates the thread.
     * The method will return within the specified timeout period, whether the transaction was successfully aborted or not.
     */
    public void abort(long waitFor) {
		logger.log(Level.FINE, "Aborting.");
    	if (txn != null) {
			try {
				txn.abort(waitFor);
			} catch (Exception ex) {
				logger.log(Level.FINE, "Exception while aborting transaction during thread abort.", ex);
			}
    	}
    	synchronized(this) {
    		aborted = true;
    	}

    }
    
}
