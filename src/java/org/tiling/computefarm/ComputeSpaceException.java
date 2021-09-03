package org.tiling.computefarm;

/**
 * Indicates that there is an unrecoverable problem performing an operation on a {@link ComputeSpace}.
 */
public class ComputeSpaceException extends RuntimeException {

	public ComputeSpaceException(Throwable cause) {
		super(cause);
	}

}
