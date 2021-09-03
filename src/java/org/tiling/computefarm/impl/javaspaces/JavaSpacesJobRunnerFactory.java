package org.tiling.computefarm.impl.javaspaces;

import java.io.IOException;

import org.tiling.computefarm.ConfigurationException;
import org.tiling.computefarm.Job;
import org.tiling.computefarm.JobRunner;
import org.tiling.computefarm.JobRunnerFactory;

/**
 * A {@link JobRunnerFactory} that creates {@link JavaSpacesJobRunner} instances. 
 */
public class JavaSpacesJobRunnerFactory extends JobRunnerFactory {

    public JobRunner newJobRunner(Job job) {
        try {
			return new JavaSpacesJobRunner(job);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
    }

}
