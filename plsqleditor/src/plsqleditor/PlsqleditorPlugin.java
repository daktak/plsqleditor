package plsqleditor;

import java.sql.ResultSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import plsqleditor.db.DbUtility;
import plsqleditor.editors.ColorManager;
import plsqleditor.editors.PlSqlCodeScanner;
import plsqleditor.editors.PlSqlPartitionScanner;
import plsqleditor.parsers.Segment;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.SchemaRegistry;

/**
 * The main plugin class to be used in the desktop.
 */
public class PlsqleditorPlugin extends AbstractUIPlugin
{
    // The shared instance.
    private static PlsqleditorPlugin plugin;
    // Resource bundle.
    private ResourceBundle           resourceBundle;

    public static final String       PLSQL_PARTITIONING = "__plsql_partitioning";

    private PlSqlPartitionScanner    fPartitionScanner;
    private ColorManager             fColorProvider;
    private PlSqlCodeScanner         fCodeScanner;
    private PackageStore             myPackageStore;
    private SchemaRegistry           theSchemaRegistry;
    private IProject                 myProject;

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
        DbUtility.init(getPreferenceStore(), getSchemaRegistry());
        myPackageStore = new PackageStore(getPreferenceStore(), getSchemaRegistry());
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        DbUtility.close();
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
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
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
     * Returns the plugin's resource bundle,
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

    public List<String> getSchemas()
    {
        return myPackageStore.getSchemas();
    }

    public void loadPackageFile(IFile file, IDocument doc, boolean force)
    {
        myPackageStore.loadPackageFile(file, doc, force, true);
    }

    public List<String> getPackages(String schema)
    {
        return myPackageStore.getPackages(schema);
    }

    public List<Segment> getSegments(String schema, String packageName)
    {
        return myPackageStore.getSegments(schema, packageName);
    }

    public List<Segment> getSegments(IFile file, String filename, IDocument document)
    {
        return myPackageStore.getSegments(file, filename, document);
    }

    public List<Segment> getSegments(IDocument document)
    {
        return myPackageStore.getSegments(document);
    }

    /**
     * @return
     */
    public String getCurrentSchema()
    {
        return myPackageStore.getCurrentSchema();
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg)
    {
        super.initializeImageRegistry(reg);
        String pluginId = "plsqleditor";

        ImageDescriptor desc = imageDescriptorFromPlugin(pluginId,
                                                         "icons/bullet_ball_glass_red.png");
        Image functionImage = desc.createImage();
        reg.put(Segment.SegmentType.Function.toString(), functionImage);
        Image procedureImage = imageDescriptorFromPlugin(pluginId,
                                                         "icons/bullet_ball_glass_blue.png")
                .createImage();
        reg.put(Segment.SegmentType.Procedure.toString(), procedureImage);
        Image fieldImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_green.png")
                .createImage();
        reg.put(Segment.SegmentType.Field.toString(), fieldImage);
        Image typeImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_yellow.png")
                .createImage();
        reg.put(Segment.SegmentType.Type.toString(), typeImage);
        Image packageImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_yellow.png")
                .createImage();
        reg.put(Segment.SegmentType.Package.toString(), packageImage);
        Image schemaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_triangle_green.png")
                .createImage();
        reg.put(Segment.SegmentType.Schema.toString(), schemaImage);
        Image labelImage = imageDescriptorFromPlugin(pluginId, "icons/worker.png").createImage();
        reg.put(Segment.SegmentType.Label.toString(), labelImage);
    }

    /**
     * @return
     */
    public SchemaRegistry getSchemaRegistry()
    {
        if (theSchemaRegistry == null)
        {
            theSchemaRegistry = new SchemaRegistry(getStateLocation());
        }
        return theSchemaRegistry;
    }

    /**
     * @param schema
     * @param packageName
     * @return
     */
    public IFile getFile(String schema, String packageName)
    {
        return myPackageStore.getFile(schema, packageName);
    }

    // public RuleBasedScanner getJavaDocScanner()
    // {
    // if(fDocScanner == null)
    // fDocScanner = new JavaDocScanner(fColorProvider);
    // return fDocScanner;
    // }
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
}
