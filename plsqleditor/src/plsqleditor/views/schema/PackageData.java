package plsqleditor.views.schema;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.stores.PlSqlPackage;
import plsqleditor.stores.PlSqlSchema;
import plsqleditor.stores.Source;
import plsqleditor.views.schema.SchemaBrowserContentProvider.TreeParent;

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
     
     </code>
     * It is currently not used.
     */
    public static IPackageData[] getSqlPackageInfo(String schema, IProject project)
    {
        List list = new ArrayList();

        String sql = "select object_name, object_type, status from all_objects "
                + "where (object_type = 'PACKAGE' or object_type = 'PACKAGE BODY') "
                + "and owner = ? order by object_name, object_type";

        String[][] results = DbUtility.getObjects(project, schema, sql, new Object[]{schema});

        for (int i = 0; i < results.length; i++)
        {
            IPackageData myObj = new PackageData(results[i][0]);
            // myObj.setName(results[i][0]);
            myObj.setType(results[i][1]);
            myObj.setStatus(results[i][2]);
            list.add(myObj);
        }
        return (IPackageData[]) list.toArray(new IPackageData[list.size()]);
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        public String getColumnText(Object obj, int index)
        {
            switch (index)
            {
                case 0 :
                    if (obj instanceof IPackageData) return ((IPackageData) obj).getName();
                    return ("");
                case 1 :
                    if (obj instanceof IPackageData) return ((IPackageData) obj).getType();
                    return ("");
                case 2 :
                    if (obj instanceof IPackageData) return ((IPackageData) obj).getStatus();
                    return ("");
                default :
                    return getText(obj);
            }

        }

        public Image getColumnImage(Object obj, int index)
        {
            switch (index)
            {
                case 0 :
                    return getImage(obj);
                default :
                    return null;
            }
        }

        public Image getImage(Object obj)
        {
            Bundle bundle = PlsqleditorPlugin.getDefault().getBundle();
            final URL invalidSqlImgUrl = bundle.getEntry("icons/file_sql_error.gif");
            final URL validSqlImgUrl = bundle.getEntry("icons/file_sql.gif");
            if (obj instanceof IPackageData)
            {
                IPackageData pkgData = (IPackageData) obj;
                if (pkgData.getStatus().equalsIgnoreCase("INVALID"))
                {
                    return ImageDescriptor.createFromURL(invalidSqlImgUrl).createImage();
                }
                else
                {
                    return ImageDescriptor.createFromURL(validSqlImgUrl).createImage();
                }
            }
            else
            {
                return PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_OBJ_ELEMENT);
            }
        }
    }

    class NameSorter extends ViewerSorter
    {
        public int compare(Viewer viewer, Object p1, Object p2)
        {
            return (((IPackageData) p1).getName()
                    .compareToIgnoreCase(((IPackageData) p2).getName()));
        }

    }

    public static Action createOpenDatabasePackageAction(final Viewer viewer,
                                                         final IProject project,
                                                         final String schema)
    {
        Action openDatabasePackageAction = new Action()
        {
            public void run()
            {
                PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                if (obj instanceof IPackageData)
                {
                    IPackageData pkgData = (IPackageData) obj;

                    showMessage("Click detected on " + ((IPackageData) obj).getName(), viewer);

                    String packageName = pkgData.getName();
                    String extensionName = null;

                    if (pkgData.getType().equals("PACKAGE"))
                    {
                        extensionName = ".pks";
                    }
                    else
                    {
                        extensionName = ".pkdb";
                    }
                    IPath schemaLocation = null;
                    IFile fileToWriteTo = null; 
                    PlSqlSchema schm = plugin.getPackageStore(project).getSchema(schema);
                    
                    if (schm != null)
                    {
                        PlSqlPackage pkg = schm.getPackage(packageName);
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
                    if (schemaLocation == null)
                    {
                        //fileToWriteTo = container.getFile(new Path(schema + "/" + packageName + extensionName));
                        fileToWriteTo = project.getFile(new Path(schema).append(packageName + extensionName));
                    }
                    else
                    {
                        fileToWriteTo = project.getFile(schemaLocation.append(packageName + extensionName));
                    }
                    final IFile file = fileToWriteTo;
                    try
                    {
                        InputStream stream = openPackageFromDatabase(project,
                                                                     schema,
                                                                     packageName,
                                                                     pkgData.getType());
                        if (file.exists())
                        {
                            file.setContents(stream, true, true, null);
                        }
                        else
                        {
                            file.create(stream, true, null);
                        }
                        stream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    catch (CoreException e)
                    {
                        e.printStackTrace();
                    }

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
                else
                {
                    showMessage("Click detected on " + obj.toString(), viewer);
                }
            }
        };
        openDatabasePackageAction.setText("Open package");
        openDatabasePackageAction
                .setToolTipText("Open package from database into an active editor");

        openDatabasePackageAction
                .setImageDescriptor(getImageDescriptor("icons/get_package_from_db.gif"));
        return openDatabasePackageAction;
    }

    public static Action getShowGrantsAction(final Viewer viewer,
                                             final IProject project,
                                             final String schema)
    {
        Action showGrantsAction = new Action()
        {
            public void run()
            {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                if (obj instanceof IPackageData)
                {
                    IPackageData pkgData = (IPackageData) obj;
                    showMessage(getGrants(project, schema, pkgData.getName()), viewer);
                }
            }
        };
        showGrantsAction.setText("Show grants");
        showGrantsAction.setToolTipText("Show grants associated with this package");
        showGrantsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
        return showGrantsAction;
    }

    /**
     * This method gets an image descriptor that is associated with the file
     * identified by the fully qualified (from the root of the installed plugin)
     * <code>relativeLocation</code> of the image.
     * 
     * @param relativeLocation The fully qualified name of the image, located
     *            from the install base of the plugin, whose image descriptor is
     *            sought.
     */
    public static ImageDescriptor getImageDescriptor(String relativeLocation)
    {
        final URL imageUrl = PlsqleditorPlugin.getDefault().getBundle().getEntry("/"
                + relativeLocation);
        return ImageDescriptor.createFromURL(imageUrl);
    }

    /**
     * This method opens a package from the database and allows it to be read
     * from the returned InputStream.
     * 
     * @param project The project containing the packages whose grants are
     *            requested.
     * 
     * @param schema The schema in which the package exists.
     * 
     * @param packageName The name of the package being checked.
     * 
     * @param type Whether this is a package or package body etc. Currently only
     *            expected to be a package.
     * 
     * @return an InputStream representing the contents of an owned package.
     */
    static InputStream openPackageFromDatabase(IProject project,
                                               String schema,
                                               String packageName,
                                               String type)
    {
        StringBuffer contents = new StringBuffer();
        try
        {
            String sql = "select TEXT from all_source where name = ? and owner = ? AND TYPE = ?";

            String[][] results = DbUtility.getObjects(project, schema, sql, new Object[]{
                    packageName.toUpperCase(), schema.toUpperCase(), type.toUpperCase()});
            for (int i = 0; i < results.length; i++)
            {
                contents.append(results[i][0]);
            }
            int indexPackage;
            String pkgName = "package";
            if ((indexPackage = contents.indexOf(pkgName)) < 0)
            {
                pkgName = "PACKAGE";
                indexPackage = contents.indexOf(pkgName);
            }
            if (indexPackage > 0)
            {
                contents.replace(indexPackage,
                                 indexPackage + pkgName.length(),
                                 "CREATE OR REPLACE PACKAGE");
            }
        }
        catch (Exception e)
        {
            PlsqleditorPlugin.getDefault().log("Other error: " + e.toString(), e);
        }

        return new ByteArrayInputStream(contents.toString().getBytes());
    }

    /**
     * This method gets the grants of a particular package and returns them as a
     * string of line feed separated grant lines, each line being in the format
     * Grantee: privelige
     * 
     * @param project The project containing the packages whose grants are
     *            requested.
     * 
     * @param schema The schema in which the package exists.
     * 
     * @param packageName The name of the package being checked.
     * 
     * @return The list of grants of a particular package and returns them as a
     *         string of line feed separated grant lines, each line being in the
     *         format Grantee: privelige.
     */
    static String getGrants(IProject project, String schema, String packageName)
    {
        StringBuffer contents = new StringBuffer();
        try
        {
            String sql = "select grantee, privilege from user_tab_privs_made where table_name = ?";
            String[][] results = DbUtility.getObjects(project,
                                                      schema,
                                                      sql,
                                                      new Object[]{packageName.toUpperCase()});
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
        MessageDialog.openInformation(viewer.getControl().getShell(), "Package View", message);
    }
}