package plsqleditor.views.schema;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;

/**
 */
public class PackageData extends TreeParent implements IPackageData
{
	private String myStatus;

	public PackageData(String name)
	{
		super(name, "Package");
	}

	public String getStatus()
	{
		return this.myStatus;
	}

	public void setStatus(String status)
	{
		this.myStatus = status;
	}

	/**
	 * This method should be the result of some aspect of
	 * <p>
	 * <code>
     
     public Object[] getElements(Object parent) 
     {
     return getSqlPackageInfo();
     }
     
     </code> It is currently not used.
	 */
	public static IPackageData[] getSqlPackageInfo(String schema,
			IProject project)
	{
		List<IPackageData> list = new ArrayList<IPackageData>();

		String sql = "select object_name, object_type, status from all_objects "
				+ "where (object_type = 'PACKAGE' or object_type = 'PACKAGE BODY') "
				+ "and owner = ? order by object_name, object_type";

		String[][] results = DbUtility.getObjects(project, schema, sql,
				new Object[] { schema });

		for (int i = 0; i < results.length; i++)
		{
			IPackageData myObj = new PackageData(results[i][0]);
			// myObj.setName(results[i][0]);
			myObj.setType(results[i][1]);
			myObj.setStatus(results[i][2]);
			list.add(myObj);
		}
		return list.toArray(new IPackageData[list.size()]);
	}

	/**
	 * @param project
	 * @param schema
	 * @param packageName
	 * @param extensionName
	 * @param fileToWriteTo
	 * @return
	 */
	public static IFile getFileToWriteTo(final IProject project, final String schema,
			String packageName, String extensionName)
	{
		IFile fileToWriteTo = null;
		try
		{
			IFolder dir = project.getFolder(new Path(schema));
			if (!dir.exists())
			{
				dir.create(true, true, null);
			}
			dir = dir.getFolder(".db");
			if (!dir.exists())
			{
				dir.create(true, true, null);
			}

			fileToWriteTo = dir.getFile(new Path(packageName).addFileExtension(extensionName));
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		return fileToWriteTo;
	}

	public static Action getShowGrantsAction(final Viewer viewer,
			final IProject project, final String schema)
	{
		Action showGrantsAction = new Action()
		{
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof IPackageData)
				{
					IPackageData pkgData = (IPackageData) obj;
					showMessage(getGrants(project, schema, pkgData.getName()),
							viewer);
				}
			}
		};
		showGrantsAction.setText("Show grants");
		showGrantsAction
				.setToolTipText("Show grants associated with this package");
		showGrantsAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJS_INFO_TSK));
		return showGrantsAction;
	}

	/**
	 * This method gets an image descriptor that is associated with the file
	 * identified by the fully qualified (from the root of the installed plugin)
	 * <code>relativeLocation</code> of the image.
	 * 
	 * @param relativeLocation
	 *            The fully qualified name of the image, located from the
	 *            install base of the plugin, whose image descriptor is sought.
	 */
	public static ImageDescriptor getImageDescriptor(String relativeLocation)
	{
		final URL imageUrl = PlsqleditorPlugin.getDefault().getBundle()
				.getEntry("/" + relativeLocation);
		return ImageDescriptor.createFromURL(imageUrl);
	}

	/**
	 * This method gets the grants of a particular package and returns them as a
	 * string of line feed separated grant lines, each line being in the format
	 * Grantee: privelige
	 * 
	 * @param project
	 *            The project containing the packages whose grants are
	 *            requested.
	 * 
	 * @param schema
	 *            The schema in which the package exists.
	 * 
	 * @param packageName
	 *            The name of the package being checked.
	 * 
	 * @return The list of grants of a particular package and returns them as a
	 *         string of line feed separated grant lines, each line being in the
	 *         format Grantee: privilege.
	 */
	static String getGrants(IProject project, String schema, String packageName)
	{
		StringBuffer contents = new StringBuffer();
		try
		{
			String sql = "select grantee, privilege from user_tab_privs_made where table_name = ?";
			String[][] results = DbUtility.getObjects(project, schema, sql,
					new Object[] { packageName.toUpperCase() });
			contents.append("Grants:\n\n");
			for (int i = 0; i < results.length; i++)
			{
				contents.append(results[i][0]); // "grantee"
				contents.append(": ");
				contents.append(results[i][1]); // "privilege"
				contents.append("\n");
			}
		}
		catch (Exception e)
		{
			String failureMsg = e.getMessage();
			contents.append(failureMsg);
		}
		return contents.toString();
	}

	static void showMessage(String message, Viewer viewer)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Package View", message);
	}

	/* (non-Javadoc)
	 * @see plsqleditor.views.schema.SchemaBrowserContentProvider.TreeObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class key)
	{
		if (key.equals(IResource.class))
		{
			return getFile();
		}
		else if (key.equals(IPackageData.class))
		{
			return this;
		}
		return super.getAdapter(key);
	}
}