package org.tiling.computefarm.impl.simple;

import junit.framework.TestCase;

import org.tiling.computefarm.CancelledException;
import org.tiling.computefarm.CannotTakeResultException;
import org.tiling.computefarm.ComputeSpace;
import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;

public class SimpleTest extends TestCase {
    public void testCancelledRun() {
		class NonterminatingJob implements Job {
            boolean cancelled;
            public void generateTasks(ComputeSpace computeSpace) {
            }
            public void collectResults(ComputeSpace computeSpace) {
    			try {
    			    computeSpace.take();
    			} catch (CannotTakeResultException e) {
    			} catch (CancelledException e) {
    			    cancelled = true;
                }                
            }
            public boolean wasCancelled() {
                return cancelled;
            }
		};
		NonterminatingJob job = new NonterminatingJob();
		JobRunner jobRunner = new SimpleJobRunner(job);
		Thread jobRunnerThread = new Thread(jobRunner); 
		jobRunnerThread.start();
		try { Thread.sleep(100); } catch (InterruptedException e) {}
		jobRunner.cancel();
		assertTrue(job.wasCancelled());
    }
}
