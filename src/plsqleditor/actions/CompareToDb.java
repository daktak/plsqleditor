/**
 * 
 */
package plsqleditor.actions;

import java.sql.SQLException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.schemabrowser.LoadSourceFromDb;
import plsqleditor.compare.EditorInput;
import plsqleditor.parsers.ParseType;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.stores.PackageStore;

/**
 * @author tzines
 * 
 */
public class CompareToDb extends AbstractHandler
{
	/**
	 * 
	 */
	public CompareToDb()
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
		try
		{
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil
					.getActiveMenuSelection(event);

			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IFile)
			{
				IFile file = (IFile) firstElement;
				PlsqleditorPlugin p = PlsqleditorPlugin.getDefault();
				boolean success = true;
				if (!success)
				{
					MessageDialog.openInformation(HandlerUtil
							.getActiveShell(event), "Compare to DB",
							"No compare possible for file [" + file.getName()
									+ "]");
					return null;
				}

				PackageStore ps = p.getPackageStore(file);
				String schemaName = ps.findSchemaNameForFile(file);
				String packageName = PlSqlParserManager.getPackageName(file);
				ParseType pt = PlSqlParserManager.getType(file);

				String type = pt.toString().toUpperCase().replace('_', ' ');
				CompareConfiguration compareConfig = new CompareConfiguration();
				compareConfig.setLeftEditable(true);

				if (pt == ParseType.SqlScript)
				{
					throw new IllegalStateException(
							"The parse type of this file is sql script, and cannot be compared with the database.");
				}
				
				// TODO figure out how to use parameters as below
				String generateFile = event.getParameter("generateFile");
				if (generateFile != null
						&& Boolean.valueOf(generateFile).booleanValue())
				{
					IProject project = file.getProject();
					IFile dbFile = LoadSourceFromDb.generateDbFile(p, project,
							packageName, type, schemaName, false);
					CompareUI.openCompareEditor(new EditorInput(compareConfig,
							file, dbFile));
				}
				else
				{
					String source = "";
					try
					{
						source = ps.getSource(schemaName, packageName, type);
						CompareUI.openCompareEditor(new EditorInput(
								compareConfig, file, source));
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						MessageDialog.openError(HandlerUtil
								.getActiveShell(event), "Compare To Db",
								"Unable to load source: " + e.toString());
					}
				}
			}
		}
		catch (RuntimeException e)
		{
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Compare to DB", "Failed to execute the compare: "
							+ e.toString());
		}
		return null;
	}
}
