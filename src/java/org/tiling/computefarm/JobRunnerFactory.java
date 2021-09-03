package org.tiling.computefarm;

/**
 * A {@link JobRunnerFactory} can create {@link JobRunner} instances.
 */
public abstract class JobRunnerFactory {
    
    /**
     * Create a new {@link JobRunnerFactory} by instantiating a class determined by the <code>org.tiling.computefarm.JobRunnerFactory</code>
     * system property. If this property is not set the JavaSpaces implementation is assumed.
     * @throws ConfigurationException if there is a problem creating a {@link JobRunnerFactory} instance.
     */
    public static JobRunnerFactory newInstance() {
        String jobRunnerClassName = System.getProperty("org.tiling.computefarm.JobRunnerFactory",
        	"org.tiling.computefarm.impl.javaspaces.JavaSpacesJobRunnerFactory");
        try {
            return (JobRunnerFactory) Class.forName(jobRunnerClassName).newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    
    }
    /**
     * Create a new {@link JobRunner} for the given {@link Job}. 
     * @throws ConfigurationException if there is a problem creating a {@link JobRunner} instance.
     */
    public abstract JobRunner newJobRunner(Job job);

}
