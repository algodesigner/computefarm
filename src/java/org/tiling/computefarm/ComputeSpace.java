package org.tiling.computefarm;

/**
 * A container for {@link Task} objects that constitute a {@link Job} and their computed results.
 * All tasks that are written into the space are executed to produce result objects which may
 * be taken from the space for re-assembly into an overall result.
 */
public interface ComputeSpace {
	/**
	 * Writes the given task into the space.
	 * @throws CannotWriteTaskException if the {@link Task} cannot be written to the space due to a communication problem.
	 * @throws CancelledException if the {@link Job} is cancelled while it is still running.
	 * @throws ComputeSpaceException if an unrecoverable error occurs in the process of writing the task to the space.
	 */
	public void write(Task task) throws CannotWriteTaskException, CancelledException;

	/**
	 * Takes (and removes) a result from the space. 
	 * @throws CannotTakeResultException if a result cannot be taken from the space due to a communication problem.
	 * @throws CancelledException if the {@link Job} is cancelled while it is still running.
	 * @throws ComputeSpaceException if an unrecoverable error occurs in the process of taking a result from the space.
	 */
	public Object take() throws CannotTakeResultException, CancelledException;
}