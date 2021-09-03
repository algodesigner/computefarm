package org.tiling.computefarm;

/**
 * A {@link JobRunner} runs a {@link Job} on behalf of a client. 
 * A given instance may only be used to run one {@link Job} - it is not re-usable.
 */
public interface JobRunner extends Runnable {
    /**
     * Cancels the running {@link Job}, causing a {@link CancelledException} to be thrown
     * if methods on the {@link ComputeSpace} are being invoked, or are invoked after this
     * cancel method has been called.
     */
	public void cancel();
}
