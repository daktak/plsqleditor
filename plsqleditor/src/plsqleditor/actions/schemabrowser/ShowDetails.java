/**
 * 
 */
package plsqleditor.actions.schemabrowser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import plsqleditor.views.schema.TreeObject;
import plsqleditor.views.schema.TreeParent;

/**
 * @author tzines
 * 
 */
public class ShowDetails extends AbstractHandler
{
	/**
	 * 
	 */
	public ShowDetails()
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);

		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof TreeParent)
		{
			TreeParent tree = (TreeParent) firstElement;
			String sourceName = tree.getName();
			String type = tree.getType();
			if (!validateType(type))
			{
				MessageDialog.openInformation(
						HandlerUtil.getActiveShell(event), "Show Details",
						"Nothing to show from this level");
				return null;
			}
			String schemaName = getSchemaName(tree, type);
			
			StringBuffer sb = new StringBuffer(1000);
			sb.append(  "Schema: ").append(schemaName);
			sb.append("\nName  : ").append(sourceName);
			sb.append("\nObject: ").append(tree.getObject());

			// show the grants in a popup 
			MessageDialog.openInformation(
					HandlerUtil.getActiveShell(event), "Show Details",
					sb.toString());
		}
		return null;
	}

	private String getSchemaName(TreeObject tree, String type)
	{
		String schemaName = null;
		if (type.equals("Schema"))
		{
			schemaName = tree.getName();
		}
		else
		{
			schemaName = tree.getParent().getParent().getName();
		}
		return schemaName;
	}

	public static boolean validateType(String type)
	{
		return type != null
				&& (type.equals("Package") || type.equals("Trigger")
						|| type.equals("Function") || type.equals("Procedure") || type
						.equals("Type")|| type.equals("Schema") || type.equals("Table"))|| type.equals("Segment") ;
	}
}
