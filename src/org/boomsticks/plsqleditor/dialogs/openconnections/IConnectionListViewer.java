package org.boomsticks.plsqleditor.dialogs.openconnections;


public interface IConnectionListViewer {

	/**
	 * Update the view to reflect the fact that a task was added
	 * to the task list
	 *
	 * @param task
	 */
	public void addConnection(LiveConnection task);

	/**
	 * Update the view to reflect the fact that a task was removed
	 * from the task list
	 *
	 * @param task
	 */
	public void removeConnection(LiveConnection task);

	/**
	 * Update the view to reflect the fact that one of the tasks
	 * was modified
	 *
	 * @param task
	 */
	public void updateConnection(LiveConnection task);
}
