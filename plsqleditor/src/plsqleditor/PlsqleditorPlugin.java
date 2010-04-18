package plsqleditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import plsqleditor.db.ConnectionRegistry;
import plsqleditor.db.DbUtility;
import plsqleditor.editors.ColorManager;
import plsqleditor.editors.PlSqlCodeScanner;
import plsqleditor.editors.PlSqlPartitionScanner;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.SchemaRegistry;
import plsqleditor.stores.TableStore;

/**
 * The main myPluginRef class to be used in the desktop.
 */
public class PlsqleditorPlugin extends AbstractUIPlugin
{
    public static String                    theId                  = "plsqleditor";
    // The shared instance.
    private static PlsqleditorPlugin        plugin;
    // Resource bundle.
    private ResourceBundle                  resourceBundle;
    public static final String              PLSQL_PARTITIONING     = "__plsql_partitioning";
    private PlSqlPartitionScanner           fPartitionScanner;
    private ColorManager                    fColorProvider;
    private PlSqlCodeScanner                fCodeScanner;
    private Map<String, PackageStore>       myPackageStores        = new HashMap<String, PackageStore>();
    private Map<String, TableStore>         myTableStores          = new HashMap<String, TableStore>();
    private Map<String, SchemaRegistry>     mySchemaRegistries     = new HashMap<String, SchemaRegistry>();
    private ConnectionRegistry              myConnectionRegistry;
    private IProject                        myProject;
    private Object                          myCurrentDoc;
    private List                            myCurrentSegments;
    private IFile                           myCurrentFile;

    public PlsqleditorPlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        DbUtility.init(getPreferenceStore());
    }

    /**
     * This method gets the table store. It will create a new one if one does
     * not currently exist.
     */
    public TableStore getTableStore(IResource resource)
    {
        IProject project = resource.getProject();
        String projectName = project.getName();
        TableStore store = (TableStore) myTableStores.get(projectName);
        if (store == null)
        {
            store = new TableStore(project);
            myTableStores.put(projectName, store);
        }
        return store;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        try
        {
            DbUtility.close();
            // PlSqlModelManager.getPlSqlModelManager().shutdown();
        }
        finally
        {
            // ensure we call super.stop as the last thing
            super.stop(context);
        }
        super.stop(context);
        plugin = null;
        resourceBundle = null;
    }

    /**
     * Returns the shared instance.
     */
    public static PlsqleditorPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return imageDescriptorFromPlugin(theId, path);
    }

    /**
     * Returns the string from the myPluginRef's resource bundle, or 'key' if
     * not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = PlsqleditorPlugin.getDefault().getResourceBundle();
        try
        {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }

    /**
     * Returns the myPluginRef's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        try
        {
            if (resourceBundle == null) resourceBundle = ResourceBundle
                    .getBundle("plsqleditor.PlsqleditorPluginResources");
        }
        catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    public PlSqlPartitionScanner getPlSqlPartitionScanner()
    {
        if (fPartitionScanner == null) fPartitionScanner = new PlSqlPartitionScanner();
        return fPartitionScanner;
    }

    public RuleBasedScanner getPlSqlCodeScanner()
    {
        if (fCodeScanner == null)
        {
            fCodeScanner = new PlSqlCodeScanner(getPlSqlColorProvider());
        }
        return fCodeScanner;
    }

    public ColorManager getPlSqlColorProvider()
    {
        if (fColorProvider == null)
        {
            fColorProvider = new ColorManager();
        }
        return fColorProvider;
    }

    public SortedSet getSchemas(IResource resource)
    {
        return getPackageStore(resource).getSchemas();
    }


    public void loadPackageFile(IFile file, IDocument doc, boolean force)
    {
        getPackageStore(file).loadPackageFile(file, doc, force, true);
    }

    public List getPackages(String schema, boolean isExpectingPublicSchemas)
    {
        return getPackageStore(getProject()).getPackages(schema, isExpectingPublicSchemas);
    }

    public List getSegments(String schema, String packageName)
    {
        return getPackageStore(getProject()).getSegments(schema, packageName);
    }

    /**
     * N.B. This method can be called before a setFocus has set the correct
     * schema name.
     * 
     * @param file
     * @param filename
     * @param document
     * @return
     */
    public List getSegments(IFile file, IDocument document, boolean isPriorToSetFocus)
    {
        List segments = getPackageStore(file).getSegments(file, document, isPriorToSetFocus);
        if (document == myCurrentDoc) // || (file != null &&
        // file.getName().equals(myCurrentFileName)))
        {
            myCurrentSegments = segments;
        }
        return segments;
    }

    /**
     * This method gets the name of the schema of the currently focussed file.
     * 
     * @return The name of the current schema.
     */
    public String getCurrentSchema()
    {
        return getPackageStore(getProject()).getCurrentSchema();
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    protected void initializeImageRegistry(ImageRegistry reg)
    {
        super.initializeImageRegistry(reg);
        String pluginId = "plsqleditor";

        Image functionImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_red.png")
                .createImage();
        reg.put(SegmentType.Function.toString() + Segment.PUBLIC, functionImage);
        Image functionImagePrivate = imageDescriptorFromPlugin(pluginId,
                                                               "icons/bullet_ball_glass_red_private.png")
                .createImage();
        reg.put(SegmentType.Function.toString() + Segment.PRIVATE, functionImagePrivate);
        Image procedureImage = imageDescriptorFromPlugin(pluginId,
                                                         "icons/bullet_ball_glass_blue.png")
                .createImage();
        reg.put(SegmentType.Procedure.toString() + Segment.PUBLIC, procedureImage);
        Image procedureImagePrivate = imageDescriptorFromPlugin(pluginId,
                                                                "icons/bullet_ball_glass_blue_private.png")
                .createImage();
        reg.put(SegmentType.Procedure.toString() + Segment.PRIVATE, procedureImagePrivate);
        Image fieldImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_green.png")
                .createImage();
        reg.put(SegmentType.Field.toString() + Segment.PUBLIC, fieldImage);
        Image fieldImagePrivate = imageDescriptorFromPlugin(pluginId,
                                                            "icons/bullet_ball_glass_green_private.png")
                .createImage();
        reg.put(SegmentType.Field.toString() + Segment.PRIVATE, fieldImagePrivate);
        reg.put(SegmentType.Constant.toString() + Segment.PUBLIC, fieldImage);
        reg.put(SegmentType.Constant.toString() + Segment.PRIVATE, fieldImagePrivate);
        Image typeImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_yellow.png")
                .createImage();
        reg.put(SegmentType.Type.toString() + Segment.PUBLIC, typeImage);
        Image typeImagePrivate = imageDescriptorFromPlugin(pluginId,
                                                           "icons/bullet_ball_glass_yellow_private.png")
                .createImage();
        reg.put(SegmentType.Type.toString() + Segment.PRIVATE, typeImagePrivate);
        reg.put(SegmentType.SubType.toString() + Segment.PUBLIC, typeImage);
        reg.put(SegmentType.SubType.toString() + Segment.PRIVATE, typeImagePrivate);
        Image cursorImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_blue.png")
                .createImage();
        reg.put(SegmentType.Cursor.toString() + Segment.PUBLIC, cursorImage);
        // Image cursorImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_square_blue_private.png").createImage();
        reg.put(SegmentType.Cursor.toString() + Segment.PRIVATE, cursorImage);
        Image packageImage = imageDescriptorFromPlugin(pluginId,
                                                       "icons/bullet_square_yellow_header.png")
                .createImage();
        reg.put(SegmentType.Package.toString() + Segment.PUBLIC, packageImage);
        // Image packageImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_square_yellow_header_private.png").createImage();
        reg.put(SegmentType.Package.toString() + Segment.PRIVATE, packageImage);
        Image packageBodyImage = imageDescriptorFromPlugin(pluginId,
                                                           "icons/bullet_square_yellow_body.png")
                .createImage();
        reg.put(SegmentType.Package_Body.toString() + Segment.PUBLIC, packageBodyImage);
        // Image packageBodyImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_square_yellow_body_private.png").createImage();
        reg.put(SegmentType.Package_Body.toString() + Segment.PRIVATE, packageBodyImage);
        Image schemaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_triangle_green.png")
                .createImage();
        reg.put(SegmentType.Schema.toString() + Segment.PUBLIC, schemaImage);
        // Image schemaImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_triangle_green_private.png").createImage();
        reg.put(SegmentType.Schema.toString() + Segment.PRIVATE, schemaImage);
        Image labelImage = imageDescriptorFromPlugin(pluginId, "icons/worker.png").createImage();
        reg.put(SegmentType.Label.toString() + Segment.PUBLIC, labelImage);
        // Image labelImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/worker_private.png").createImage();
        reg.put(SegmentType.Label.toString() + Segment.PRIVATE, labelImage);
        Image tableImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_blue.png")
                .createImage();
        reg.put(SegmentType.Table.toString() + Segment.PUBLIC, tableImage);
        // Image tableImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_square_blue_private.png").createImage();
        reg.put(SegmentType.Table.toString() + Segment.PRIVATE, tableImage);
        Image columnImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png")
                .createImage();
        reg.put(SegmentType.Column.toString() + Segment.PUBLIC, columnImage);
        // Image columnImagePrivate = imageDescriptorFromPlugin(pluginId,
        // "icons/bullet_square_red_private.png").createImage();
        reg.put(SegmentType.Column.toString() + Segment.PRIVATE, columnImage);
        Image pragmaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png")
                .createImage();
        reg.put(SegmentType.Pragma.toString() + Segment.PUBLIC, pragmaImage);
        reg.put(SegmentType.Pragma.toString() + Segment.PRIVATE, pragmaImage);

        Image schemaFunctionImage = imageDescriptorFromPlugin(pluginId, "icons/func.png")
                .createImage();
        reg.put("Schema" + SegmentType.Function.toString(), schemaFunctionImage);
        Image schemaProcedureImage = imageDescriptorFromPlugin(pluginId, "icons/proc.png")
                .createImage();
        reg.put("Schema" + SegmentType.Procedure.toString(), schemaProcedureImage);
        Image schemaFieldImage = imageDescriptorFromPlugin(pluginId, "icons/const.png")
                .createImage();
        reg.put("Schema" + SegmentType.Field.toString(), schemaFieldImage);
        reg.put("Schema" + SegmentType.Constant.toString(), schemaFieldImage);
        Image schemaTypeImage = imageDescriptorFromPlugin(pluginId,
                                                          "icons/bullet_ball_glass_yellow.png")
                .createImage();
        reg.put("Schema" + SegmentType.Type.toString(), schemaTypeImage);
        reg.put("Schema" + SegmentType.SubType.toString(), schemaTypeImage);
        Image schemaPackageImage = imageDescriptorFromPlugin(pluginId,
                                                             "icons/bullet_square_yellow.png")
                .createImage();
        reg.put("Schema" + SegmentType.Package.toString(), schemaPackageImage);
        Image schemaSchemaImage = imageDescriptorFromPlugin(pluginId,
                                                            "icons/bullet_triangle_green.png")
                .createImage();
        reg.put("Schema" + SegmentType.Schema.toString(), schemaSchemaImage);
        Image schemaLabelImage = imageDescriptorFromPlugin(pluginId, "icons/worker.png")
                .createImage();
        reg.put("Schema" + SegmentType.Label.toString(), schemaLabelImage);
        Image schemaTableImage = imageDescriptorFromPlugin(pluginId, "icons/table.png")
                .createImage();
        reg.put("Schema" + SegmentType.Table.toString(), schemaTableImage);
        Image schemaColumnImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png")
                .createImage();
        reg.put("Schema" + SegmentType.Column.toString(), schemaColumnImage);

    }

    /**
     * @return The plugin's single instance per project of the schema registry.
     */
    public SchemaRegistry getSchemaRegistry(IResource resource)
    {
        String projectName = resource.getProject().getName();
        SchemaRegistry sr = mySchemaRegistries.get(projectName);
        if (sr == null)
        {
            sr = new SchemaRegistry(projectName, getStateLocation());
            mySchemaRegistries.put(projectName, sr);
        }
        return sr;
    }

    /**
     * @return The plugin's single instance per project of the connection registry.
     */
    public ConnectionRegistry getConnectionRegistry()
    {
        if (myConnectionRegistry == null)
        {
            myConnectionRegistry = new ConnectionRegistry(getStateLocation());
        }
        return myConnectionRegistry;
    }

    /**
     * @return The myPluginRef's single instance of the schema registry.
     */
    public PackageStore getPackageStore(IResource resource)
    {
        IProject project = resource.getProject();
        String projectName = project.getName();
        PackageStore packageStore = (PackageStore) myPackageStores.get(projectName);
        if (packageStore == null)
        {
            SchemaRegistry sr = getSchemaRegistry(resource);
            packageStore = new PackageStore(project, getPreferenceStore(), sr);
            myPackageStores.put(projectName, packageStore);
        }
        return packageStore;
    }

    /**
     * @param schema
     * @param packageName
     * @return The files for the associated <code>schema</code> and
     *         <code>packageName</code>.
     * 
     * @see PackageStore#getFiles(String, String)
     */
    public IFile[] getFiles(String schema, String packageName)
    {
        return getPackageStore(getProject()).getFiles(schema, packageName);
    }

    /**
     * @param project
     */
    public void setProject(IProject project)
    {
        myProject = project;
    }

    public IProject getProject()
    {
        return myProject;
    }

    /**
     * @return The list of segments that are obtained from the current document.
     */
    public List getCurrentSegments(IDocument doc)
    {
        if (doc == null)
        {
            return myCurrentSegments;
        }
        if (myCurrentDoc != doc)
        {
            myCurrentDoc = doc;
            myCurrentSegments = getSegments(myCurrentFile, doc, false);
        }
        return myCurrentSegments;
    }

    /**
     * @return {@link #myCurrentFileName}.
     */
    public String getCurrentFileName()
    {
        if (myCurrentFile != null)
        {
            return myCurrentFile.getName();
        }
        return null;
    }

    public static void log(String msg, Exception e)
    {
        if (msg == null)
        {
            msg = "";
        }
        getDefault().getLog().log(new Status(IStatus.INFO, getDefault().getBundle()
                .getSymbolicName(), IStatus.OK, msg, e));
    }

    public void setCurrentFile(IFile file)
    {
        myCurrentFile = file;
    }
}
