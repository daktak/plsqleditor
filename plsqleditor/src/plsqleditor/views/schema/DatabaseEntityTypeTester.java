/**
 * 
 */
package plsqleditor.views.schema;

import org.eclipse.core.expressions.PropertyTester;

import plsqleditor.actions.schemabrowser.LoadSourceFromDb;
import plsqleditor.actions.schemabrowser.ShowDetails;
import plsqleditor.actions.schemabrowser.ShowGrants;

/**
 * @author tzines
 *
 */
public class DatabaseEntityTypeTester extends PropertyTester
{

	/**
	 * 
	 */
	public DatabaseEntityTypeTester()
	{
		// nothing in here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		TreeObject treeObject = (TreeObject) receiver;
		if ("isGrantLoadable".equals(property)) {
			return ShowGrants.validateType(treeObject.getType());
		}
		if ("isSourceLoadable".equals(property)) {
			return LoadSourceFromDb.validateType(treeObject.getType());
		}
		if ("isDetailsAvailable".equals(property)) {
			return ShowDetails.validateType(treeObject.getType());
		}
		return false;
	}
}
