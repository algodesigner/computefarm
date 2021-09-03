package org.tiling.computefarm.impl.javaspaces;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.space.InternalSpaceException;
import net.jini.space.JavaSpace;

import org.tiling.computefarm.CancelledException;
import org.tiling.computefarm.CannotTakeResultException;
import org.tiling.computefarm.CannotWriteTaskException;
import org.tiling.computefarm.ComputeSpace;
import org.tiling.computefarm.ComputeSpaceException;
import org.tiling.computefarm.Task;
import org.tiling.computefarm.impl.AbstractComputeSpace;

/**
 * A {@link ComputeSpace} implemented using a JavaSpace.
 */
class JavaSpaceComputeSpace extends AbstractComputeSpace {
	
	private static final Logger logger = Logger.getLogger(JavaSpaceComputeSpace.class.getName());
	
	private final JavaSpace javaSpace;
	private final TransactionManager transactionManager;
	private final Uuid jobId = UuidFactory.generate();
	
	public JavaSpaceComputeSpace(JavaSpace javaSpace, TransactionManager transactionManager) {
		this.javaSpace = javaSpace;
		this.transactionManager = transactionManager;
	}
	
	public void write(Task task) throws CannotWriteTaskException, CancelledException {
	    checkIfCancelled();
	    Transaction txn = null;
	    try {
		    txn = TransactionFactory.create(transactionManager, 60 * 10 * 1000).transaction;
			javaSpace.write(new TaskEntry(jobId, task), txn, Lease.FOREVER);
		} catch (LeaseDeniedException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new CannotWriteTaskException(e);
	    } catch (TransactionException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new CannotWriteTaskException(e);
	    } catch (RemoteException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			try {
                txn.abort();
            } catch (UnknownTransactionException e1) {
    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
                // drop through to throw wrapped first exception
            } catch (CannotAbortException e1) {
    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
                // drop through to throw wrapped first exception
            } catch (RemoteException e1) {
    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
                // drop through to throw wrapped first exception
            }
			throw new CannotWriteTaskException(e);
	    } catch (InternalSpaceException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new ComputeSpaceException(e);
	    }
		try {
            txn.commit();
        } catch (UnknownTransactionException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new CannotWriteTaskException(e);
        } catch (CannotCommitException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new CannotWriteTaskException(e);
        } catch (RemoteException e) {
			logger.log(Level.WARNING, "Exception while writing task.", e);
			throw new CannotWriteTaskException(e);
        }
	}

	public Object take() throws CannotTakeResultException, CancelledException {
	    checkIfCancelled();
		ResultEntry template = new ResultEntry(jobId);
		while (!Thread.currentThread().isInterrupted()) {
		    Transaction txn = null;
		    ResultEntry resultEntry = null;
		    try {
			    txn = TransactionFactory.create(transactionManager, 10 * 1000).transaction;
			    resultEntry = (ResultEntry) javaSpace.take(template, txn, 1000);
		    } catch (LeaseDeniedException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
            } catch (TransactionException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
            } catch (InterruptedException e) {
        	    checkIfCancelled();
        	    logger.info("Interrupted (but not cancelled).");
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
		    } catch (RemoteException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
				try {
	                txn.abort();
	            } catch (UnknownTransactionException e1) {
	    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
	                // drop through to throw wrapped first exception
	            } catch (CannotAbortException e1) {
	    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
	                // drop through to throw wrapped first exception
	            } catch (RemoteException e1) {
	    			logger.log(Level.WARNING, "Exception while aborting transaction.", e1);
	                // drop through to throw wrapped first exception
	            }		        
		        throw new CannotTakeResultException(e);
            } catch (UnusableEntryException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new ComputeSpaceException(e);
    	    } catch (InternalSpaceException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new ComputeSpaceException(e);
            }
    		try {
                txn.commit();
            } catch (UnknownTransactionException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
            } catch (CannotCommitException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
            } catch (RemoteException e) {
		        logger.log(Level.WARNING, "Exception while taking task.", e);
		        throw new CannotTakeResultException(e);
            }
		    checkIfCancelled();	    
		    if (resultEntry != null) {
				return resultEntry.result;
		    }
		}
	    checkIfCancelled();
	    logger.info("Interrupted (but not cancelled).");
        throw new CannotTakeResultException();

	}
}
