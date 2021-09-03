package org.tiling.computefarm.impl.javaspaces;

import net.jini.core.entry.Entry;
import net.jini.id.Uuid;

import org.tiling.computefarm.Job;

/**
 * A {@link ResultEntry} is an entry holder for result objects
 * corresponding to task results. 
 */
public class ResultEntry implements Entry {
	
	/**
	 * The globally unique identifier for the associated {@link Job}. 
	 */
	public Uuid jobId;

	/**
	 * The result payload. 
	 */
	public Object result;
	
	public ResultEntry() {
	}
	
	public ResultEntry(Uuid jobId) {
		this.jobId = jobId;
	}
	
	public ResultEntry(Uuid jobId, Object result) {
		this.jobId = jobId;
		this.result = result;
	}
}
