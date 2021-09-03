package org.tiling.computefarm.impl.simple;

import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;
import org.tiling.computefarm.JobRunnerFactory;

/**
 * A {@link JobRunnerFactory} that creates {@link SimpleJobRunner} instances. 
 */
public class SimpleJobRunnerFactory extends JobRunnerFactory {

    public JobRunner newJobRunner(Job job) {
        return new SimpleJobRunner(job);
    }

}
