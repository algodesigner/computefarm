package org.tiling.computefarm;

/**
 * A {@link Task} is a small, independently executable piece of a {@link Job}. 
 */
public interface Task {
  /**
   * Excutes the task and returns a result. This method is typically compute intensive.
   * @return the computed result.
   */
  public Object execute(); 
} 
