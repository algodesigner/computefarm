package org.tiling.computefarm.impl.simple;

import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;
import org.tiling.computefarm.impl.AbstractJobRunner;

/**
 * A {@link JobRunner} that runs in the local JVM. 
 */
public class SimpleJobRunner extends AbstractJobRunner {

    public SimpleJobRunner(Job job) {
        super(job);
        computeSpace = new SimpleComputeSpace();
    }
    
    public void startGenerateAndCollectThreads() {
		generateThread = new AbstractJobRunner.GenerateThread();
		generateThread.start();
		collectThread = new AbstractJobRunner.CollectThread();
		collectThread.start();
    }	

}
