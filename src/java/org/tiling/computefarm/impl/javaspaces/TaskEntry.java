package org.tiling.computefarm.impl.javaspaces;

import net.jini.core.entry.Entry;
import net.jini.id.Uuid;

import org.tiling.computefarm.Job;
import org.tiling.computefarm.Task;

/**
 * A {@link TaskEntry} is an entry holder for task objects
 * that are the constituent pieces of a {@link Job}. 
 */
public class TaskEntry implements Entry {
	
	private static final long MAX_RESULT_LIFETIME = Long.parseLong(
	        System.getProperty("org.tiling.computefarm.impl.javaspaces.MaxResultLifetime", "300000") // default to 5 minutes
	);
	
    /**
	 * The globally unique identifier for the associated {@link Job}. 
	 */
	public Uuid jobId;
	
	/**
	 * The task payload. 
	 */
	public Task task;
	
	public TaskEntry() {
	}
	
	public TaskEntry(Uuid jobId, Task task) {
		this.jobId = jobId;
		this.task = task;
	}
	
	/**
	 * @return the lease time for the {@link ResultEntry}.
	 */
	public long getMaxResultLifetime() {
		return MAX_RESULT_LIFETIME;
	}
}
