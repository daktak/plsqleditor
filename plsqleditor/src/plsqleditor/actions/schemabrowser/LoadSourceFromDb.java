/**
 * 
 */
package plsqleditor.actions.schemabrowser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.PlSqlPackage;
import plsqleditor.stores.PlSqlSchema;
import plsqleditor.stores.Source;
import plsqleditor.views.schema.PackageData;
import plsqleditor.views.schema.TreeObject;

/**
 * @author tzines
 * 
 */
public class LoadSourceFromDb extends AbstractHandler
{

	private static HashMap<String, String> myExtensionMap;

	/**
	 * 
	 */
	public LoadSourceFromDb()
	{
		// nothing to do
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
		if (firstElement instanceof TreeObject)
		{
			TreeObject tree = (TreeObject) firstElement;
			PlsqleditorPlugin p = PlsqleditorPlugin.getDefault();
			IProject project = p.getProject();
			String sourceName = tree.getName();
			String type = tree.getType();
			if (!validateType(type))
			{
				MessageDialog.openInformation(
						HandlerUtil.getActiveShell(event), "Load Source",
						"Nothing to load from this level.");
				return null;
			}
			String schemaName = tree.getParent().getParent().getName();
			final IFile file = generateDbFile(p, project, sourceName, type,
					schemaName, true);

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
			{
				public void run()
				{
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					try
					{
						IDE.openEditor(page, file, true);
					}
					catch (PartInitException e)
					{
						//
					}
				}
			});
		}
		return null;
	}

	/**
	 * @param p
	 * @param project
	 * @param sourceName
	 * @param type
	 * @param schemaName
	 * @return
	 */
	public static IFile generateDbFile(PlsqleditorPlugin p, IProject project,
			String sourceName, String type, String schemaName,
			boolean generateWithBody)
	{
		StringBuffer contents = new StringBuffer(1000);
		String typeUpper = type.toUpperCase();
		try
		{
			PackageStore pstore = p.getPackageStore(project);
			contents.append(pstore.getSource(schemaName, sourceName, type));
			if (generateWithBody
					&& (typeUpper.equals("TYPE") || typeUpper.equals("PACKAGE")))
			{
				String typeBody = typeUpper + " BODY";
				contents.append("\n\n");
				String toAdd = pstore.getSource(schemaName, sourceName, typeBody);
				contents.append(toAdd.replaceFirst(typeBody, "CREATE OR REPLACE " + typeBody));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new IllegalStateException(
					"Failed to retrieve information from db: " + e.toString());
		}
		String strContents = contents.toString();
		strContents = strContents.replaceFirst(typeUpper,
				"CREATE OR REPLACE " + typeUpper);
		InputStream stream = new ByteArrayInputStream(strContents.getBytes());
		IPath schemaLocation = tryToFindSchemaLoc(p, project, schemaName,
				sourceName);
		IFile fileToWriteTo = null;
		String extension = getExtension(type);
		if (schemaLocation == null)
		{
			fileToWriteTo = PackageData.getFileToWriteTo(project, schemaName,
					sourceName, extension);
		}
		else
		{
			fileToWriteTo = PackageData.getFileToWriteTo(project,
					schemaLocation.toString(), sourceName, extension);
		}
		try
		{
			if (fileToWriteTo.exists())
			{
				fileToWriteTo.setContents(stream, true, true, null);
			}
			else
			{
				fileToWriteTo.create(stream, true, null);
			}
			stream.close();
		}
		catch (Exception e)
		{
			String msg = "Failed to write file: " + e.toString();
			PlsqleditorPlugin.log(msg, e);
			throw new IllegalStateException(msg);
		}
		return fileToWriteTo;
	}

	private static String getExtension(String type)
	{
		if (myExtensionMap == null)
		{
			myExtensionMap = new HashMap<String, String>();
			myExtensionMap.put("Package", "pks");
			myExtensionMap.put("Package Body", "pkdb");
			myExtensionMap.put("Procedure", "prc");
			myExtensionMap.put("Function", "prc");
			myExtensionMap.put("Trigger", "trg");
			myExtensionMap.put("Type", "sql");
			myExtensionMap.put("Type", "sql");
		}
		String toReturn = myExtensionMap.get(type);
		if (toReturn == null)
		{
			toReturn = "sql";
		}
		return toReturn;
	}

	/**
	 * @param p
	 * @param project
	 * @param schemaName
	 * @param schemaLocation
	 * @param sourceName
	 * @return
	 */
	private static IPath tryToFindSchemaLoc(PlsqleditorPlugin p,
			IProject project, String schemaName, String sourceName)
	{
		PlSqlSchema schm = p.getPackageStore(project).getSchema(schemaName);
		IPath schemaLocation = null;
		if (schm != null)
		{
			PlSqlPackage pkg = schm.getPackage(sourceName);
			if (pkg != null)
			{
				Source[] sources = schm.getSources();
				if (sources.length > 0)
				{
					Source source = sources[0];
					schemaLocation = source.getSource();
				}
			}
		}
		return schemaLocation;
	}

	public static boolean validateType(String type)
	{
		return type != null
				&& (type.equals("Package") || type.equals("Trigger")
						|| type.equals("Function") || type.equals("Procedure") || type
						.equals("Type"));
	}
}
