package org.tiling.computefarm;

/**
 * Indicates that a {@link Task} could not be written to the {@link ComputeSpace}.
 */
public class CannotWriteTaskException extends Exception {

    public CannotWriteTaskException(Throwable cause) {
        super(cause);
    }

}
