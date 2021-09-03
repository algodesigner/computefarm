package org.tiling.computefarm.impl;

import java.util.logging.Logger;

import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;

public abstract class AbstractJobRunner implements JobRunner {

	private static final Logger logger = Logger.getLogger(AbstractJobRunner.class.getName());
    
	private final Job job;
	protected AbstractComputeSpace computeSpace;
	protected GenerateThread generateThread;
	protected CollectThread collectThread;
	private volatile boolean stopped;
	
    public AbstractJobRunner(Job job) {
        this.job = job;
    }
    
    public class GenerateThread extends Thread {
		public void run() {
			logger.fine("GenerateThread starting");
			logger.fine("computeSpace" + computeSpace);
			job.generateTasks(computeSpace);
			logger.fine("GenerateThread stopping");
		}
	}

    public class CollectThread extends Thread {
		public void run() {
			logger.fine("CollectThread starting");
			job.collectResults(computeSpace);
			stopped = true;
			logger.fine("CollectThread stopping");
		}
	}
	
	public void run() {
		logger.fine("JobRunner starting");
		startGenerateAndCollectThreads();
		while (!stopped) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		}		
		logger.fine("JobRunner stopping");
	}

	public void cancel() {
		computeSpace.cancel();
		if (generateThread != null) {
			generateThread.interrupt();
		}
		if (collectThread != null) {
			collectThread.interrupt();
		}
	}
	
	public abstract void startGenerateAndCollectThreads();
	

}
