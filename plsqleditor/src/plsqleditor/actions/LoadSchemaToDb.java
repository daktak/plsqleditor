package plsqleditor.actions;

import java.util.HashMap;
import java.util.Iterator;

import org.boomsticks.plsqleditor.dialogs.LoadSchemaDialog;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.ParseType;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.PlSqlPackage;
import plsqleditor.stores.PlSqlSchema;
import plsqleditor.stores.Source;

public class LoadSchemaToDb extends PersistentPropertyAction
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);

		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IFile)
		{
			IFile f = (IFile) firstElement;
			IProject project = f.getProject();
			// This is a list of the package names mapped to the owned file.
			// This may have maps to null if there are missing package bodies.
			HashMap<String, IFile> packagesMap = new HashMap<String, IFile>();
			PackageStore ps = PlsqleditorPlugin.getDefault().getPackageStore(f);
			String schemaName = ps.findSchemaNameForFile(f);

			// get the packages to load
			if (schemaName != null)
			{
				PlSqlSchema schema = ps.getSchema(schemaName);
				Source[] schemaSources = schema.getSources();
				for (Iterator<PlSqlPackage> packages = schema.getPackages()
						.values().iterator(); packages.hasNext();)
				{
					PlSqlPackage pkg = packages.next();
					String pkgName = pkg.getName();
					Source bodySource = pkg.getSource(ParseType.Package_Body);
					if (bodySource != null)
					{
						String bodySourceFilename = bodySource.getSource()
								.toString();
						for (int i = 0; i < schemaSources.length; i++)
						{
							IPath fullpath = schemaSources[i].getSource()
									.append(bodySourceFilename);
							if (project.exists(fullpath))
							{
								IFile bodyFile = project.getFile(fullpath);
								packagesMap.put(pkgName, bodyFile);
								break;
							}
						}
					}
					else
					{
						packagesMap.put(pkgName, null);
					}
				}
			}

			// display the list in a table and ask for an ok
			Shell shell = HandlerUtil.getActiveShell(event);
			try
			{
				LoadSchemaDialog dialog = new LoadSchemaDialog(shell,
						"Load Schema", "Select the packages you wish to load", packagesMap);
				dialog.open();
			}
			catch (RuntimeException e)
			{
				e.printStackTrace();
				MessageDialog.openInformation(shell, "Load Schema Attempt",
						"Failed to process : " + e.getMessage());
			}
		}
		return null;
	}
}
