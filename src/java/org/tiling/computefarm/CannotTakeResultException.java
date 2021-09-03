package org.tiling.computefarm;

/**
 * Indicates that a result could not be taken from the {@link ComputeSpace}.
 */
public class CannotTakeResultException extends Exception {

    public CannotTakeResultException() {
    }

    public CannotTakeResultException(Throwable cause) {
        super(cause);
    }

}
