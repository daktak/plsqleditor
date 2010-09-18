/**
 * 
 */
package plsqleditor.actions.schemabrowser;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.views.schema.SchemaBrowserContentProvider;
import plsqleditor.views.schema.TreeObject;
import plsqleditor.views.schema.TreeParent;
import au.com.gts.data.Grant;

/**
 * @author tzines
 * 
 */
public class ShowGrants extends AbstractHandler
{
	/**
	 * 
	 */
	public ShowGrants()
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
			PlsqleditorPlugin p = PlsqleditorPlugin.getDefault();
			IProject project = p.getProject();
			String sourceName = tree.getName();
			String type = tree.getType();
			if (!validateType(type))
			{
				MessageDialog.openInformation(
						HandlerUtil.getActiveShell(event), "Show Grants",
						"Nothing to show from this level.");
				return null;
			}
			String schemaName = getSchemaName(tree, type);
			
			List<Grant> grants = p.getTableStore(project).getGrants(schemaName, sourceName);
			
			TreeObject [] children = tree.getChildren();
			for (int i = 0; i < children.length; i++)
			{
				if (children[i].getType().equals("Grants"))
				{
					tree.removeChild(children[i]);
					break;
				}
			}
			TreeParent grantsTp = new TreeParent("Grants", "Grants");

			tree.addChild(grantsTp);
			StringBuffer sb = new StringBuffer(1000);
			// load up the grants into the tree
			for (Grant grant : grants)
			{
				sb.append(grant.toString()).append("\n");
				TreeObject to = new TreeObject(grant.getName(),"Grant",null);
				to.setObject(grant);
				grantsTp.addChild(to);
			}
			
			SchemaBrowserContentProvider.getInstance().updateNode(tree);

			// show the grants in a popup 
			MessageDialog.openInformation(
					HandlerUtil.getActiveShell(event), "Show Grants",
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
						.equals("Type")|| type.equals("Schema") || type.equals("Table"));
	}
}
