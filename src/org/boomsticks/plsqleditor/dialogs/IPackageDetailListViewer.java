package org.boomsticks.plsqleditor.dialogs;


public interface IPackageDetailListViewer {
	
	/**
	 * Update the view to reflect the fact that a package detail
	 * was added to the package detail list.
	 * 
	 * @param detail The detail being added.
	 */
	public void addPackageDetail(LoadSchemaDialog.PackageDetail detail);
	
	/**
	 * Update the view to reflect the fact that a package detail
	 * was removed from the package detail list.
	 * 
	 * @param detail The detail being removed.
	 */
	public void removePackageDetail(LoadSchemaDialog.PackageDetail task);
	
	/**
	 * Update the view to reflect the fact that a package detail
	 * was modified.
	 * 
	 * @param detail The detail being updated.
	 */
	public void updatePackageDetail(LoadSchemaDialog.PackageDetail task);
}
