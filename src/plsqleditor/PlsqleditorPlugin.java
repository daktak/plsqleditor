package plsqleditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;

import org.boomsticks.plsqleditor.dialogs.openconnections.OpenConnectionList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

import plsqleditor.core.util.XMLUtilities;
import plsqleditor.db.ConnectionDetails;
import plsqleditor.db.ConnectionRegistry;
import plsqleditor.db.DbUtility;
import plsqleditor.editors.ColorManager;
import plsqleditor.editors.PlSqlDocumentProvider;
import plsqleditor.editors.PlSqlPartitionScanner;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.SchemaRegistry;
import plsqleditor.stores.TableStore;

/**
 * The main myPluginRef class to be used in the desktop.
 */
public class PlsqleditorPlugin extends AbstractUIPlugin {
    public static String theId = "plsqleditor";
    // The shared instance.
    private static PlsqleditorPlugin plugin;
    // Resource bundle.
    private ResourceBundle resourceBundle;
    public static final String PLSQL_PARTITIONING = "__plsql_partitioning";
    private static final String CONSOLE_NAME = "Toby's PlSqlEditor Console";
    private PlSqlPartitionScanner fPartitionScanner;
    private ColorManager fColorProvider;
    private Map<String, PackageStore> myPackageStores = new HashMap<String, PackageStore>();
    private Map<String, TableStore> myTableStores = new HashMap<String, TableStore>();
    private Map<String, SchemaRegistry> mySchemaRegistries = new HashMap<String, SchemaRegistry>();
    private ConnectionRegistry myConnectionRegistry;
    private IProject myProject;
    private Object myCurrentDoc;
    private List<Segment> myCurrentSegments;
    private IFile myCurrentFile;
    private IDocumentProvider fDocumentProvider;
    private OpenConnectionList myOpenConnectionList = new OpenConnectionList();

    public PlsqleditorPlugin() {
	plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
	super.start(context);
	DbUtility.init(getPreferenceStore());
    }

    /**
     * This method gets the table store. It will create a new one if one does not
     * currently exist.
     */
    public TableStore getTableStore(IResource resource) {
	IProject project = resource.getProject();
	String projectName = project.getName();
	TableStore store = (TableStore) myTableStores.get(projectName);
	if (store == null) {
	    store = new TableStore(project);
	    myTableStores.put(projectName, store);
	}
	return store;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
	try {
	    DbUtility.close();
	} finally {
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
    public static PlsqleditorPlugin getDefault() {
	return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative
     * path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
	return imageDescriptorFromPlugin(theId, path);
    }

    /**
     * Returns the string from the myPluginRef's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
	ResourceBundle bundle = PlsqleditorPlugin.getDefault().getResourceBundle();
	try {
	    return (bundle != null) ? bundle.getString(key) : key;
	} catch (MissingResourceException e) {
	    return key;
	}
    }

    /**
     * Returns the PLSQL Editor resource bundle,
     */
    public ResourceBundle getResourceBundle() {
	try {
	    if (resourceBundle == null)
		resourceBundle = ResourceBundle.getBundle("plsqleditor.PlsqleditorPluginResources");
	} catch (MissingResourceException x) {
	    resourceBundle = null;
	}
	return resourceBundle;
    }

    public PlSqlPartitionScanner getPlSqlPartitionScanner() {
	if (fPartitionScanner == null) {
	    fPartitionScanner = new PlSqlPartitionScanner();
	}

	return fPartitionScanner;
    }

    public ColorManager getPlSqlColorProvider() {
	if (fColorProvider == null) {
	    fColorProvider = new ColorManager();
	}
	return fColorProvider;
    }

    public SortedSet<String> getSchemas(IResource resource) {
	return getPackageStore(resource).getSchemas();
    }

    public void loadPackageFile(IFile file, IDocument doc, boolean force) {
	getPackageStore(file).loadPackageFile(file, doc, force, true);
    }

    public List<String> getPackages(String schema, boolean isExpectingPublicSchemas) {
	return getPackageStore(getProject()).getPackages(schema, isExpectingPublicSchemas);
    }

    public List<Segment> getSegments(String schema, String packageName) {
	return getPackageStore(getProject()).getSegments(schema, packageName);
    }

    /**
     * N.B. This method can be called before a setFocus has set the correct schema
     * name.
     *
     * @param file
     * @param filename
     * @param document
     * @return
     */
    public List<Segment> getSegments(IFile file, IDocument document, boolean isPriorToSetFocus) {
	List<Segment> segments = getPackageStore(file).getSegments(file, document, isPriorToSetFocus);
	if (document == myCurrentDoc) // || (file != null &&
	// file.getName().equals(myCurrentFileName)))
	{
	    myCurrentSegments = segments;
	}
	return segments;
    }

    /**
     * This method gets the name of the schema of the currently focused file.
     *
     * @return The name of the current schema.
     */
    public String getCurrentSchema() {
	try {
	    return getPackageStore(getProject()).getCurrentSchema();
	} catch (IllegalStateException iae) {
	    return null;
	}
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    protected void initializeImageRegistry(ImageRegistry reg) {
	super.initializeImageRegistry(reg);
	String pluginId = "plsqleditor";

	Image functionImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_red.png").createImage();
	reg.put(SegmentType.Function.toString() + Segment.PUBLIC, functionImage);
	Image functionImagePrivate = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_red_private.png")
		.createImage();
	reg.put(SegmentType.Function.toString() + Segment.PRIVATE, functionImagePrivate);
	Image procedureImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_blue.png").createImage();
	reg.put(SegmentType.Procedure.toString() + Segment.PUBLIC, procedureImage);
	Image procedureImagePrivate = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_blue_private.png")
		.createImage();
	reg.put(SegmentType.Procedure.toString() + Segment.PRIVATE, procedureImagePrivate);
	Image fieldImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_green.png").createImage();
	reg.put(SegmentType.Field.toString() + Segment.PUBLIC, fieldImage);
	Image fieldImagePrivate = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_green_private.png")
		.createImage();
	reg.put(SegmentType.Field.toString() + Segment.PRIVATE, fieldImagePrivate);
	reg.put(SegmentType.Constant.toString() + Segment.PUBLIC, fieldImage);
	reg.put(SegmentType.Constant.toString() + Segment.PRIVATE, fieldImagePrivate);
	Image typeImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_yellow.png").createImage();
	reg.put(SegmentType.Type.toString() + Segment.PUBLIC, typeImage);
	Image typeImagePrivate = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_yellow_private.png")
		.createImage();
	reg.put(SegmentType.Type.toString() + Segment.PRIVATE, typeImagePrivate);
	reg.put(SegmentType.SubType.toString() + Segment.PUBLIC, typeImage);
	reg.put(SegmentType.SubType.toString() + Segment.PRIVATE, typeImagePrivate);
	Image cursorImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_blue.png").createImage();
	reg.put(SegmentType.Cursor.toString() + Segment.PUBLIC, cursorImage);
	// Image cursorImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_square_blue_private.png").createImage();
	reg.put(SegmentType.Cursor.toString() + Segment.PRIVATE, cursorImage);
	Image packageImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_yellow_header.png").createImage();
	reg.put(SegmentType.Package.toString() + Segment.PUBLIC, packageImage);
	// Image packageImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_square_yellow_header_private.png").createImage();
	reg.put(SegmentType.Package.toString() + Segment.PRIVATE, packageImage);
	Image packageBodyImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_yellow_body.png")
		.createImage();
	reg.put(SegmentType.Package_Body.toString() + Segment.PUBLIC, packageBodyImage);
	// Image packageBodyImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_square_yellow_body_private.png").createImage();
	reg.put(SegmentType.Package_Body.toString() + Segment.PRIVATE, packageBodyImage);
	Image schemaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_triangle_green.png").createImage();
	reg.put(SegmentType.Schema.toString() + Segment.PUBLIC, schemaImage);
	// Image schemaImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_triangle_green_private.png").createImage();
	reg.put(SegmentType.Schema.toString() + Segment.PRIVATE, schemaImage);
	Image labelImage = imageDescriptorFromPlugin(pluginId, "icons/worker.png").createImage();
	reg.put(SegmentType.Label.toString() + Segment.PUBLIC, labelImage);
	// Image labelImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/worker_private.png").createImage();
	reg.put(SegmentType.Label.toString() + Segment.PRIVATE, labelImage);
	Image tableImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_blue.png").createImage();
	reg.put(SegmentType.Table.toString() + Segment.PUBLIC, tableImage);
	// Image tableImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_square_blue_private.png").createImage();
	reg.put(SegmentType.Table.toString() + Segment.PRIVATE, tableImage);
	Image columnImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png").createImage();
	reg.put(SegmentType.Column.toString() + Segment.PUBLIC, columnImage);
	// Image columnImagePrivate = imageDescriptorFromPlugin(pluginId,
	// "icons/bullet_square_red_private.png").createImage();
	reg.put(SegmentType.Column.toString() + Segment.PRIVATE, columnImage);
	Image pragmaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png").createImage();
	reg.put(SegmentType.Pragma.toString() + Segment.PUBLIC, pragmaImage);
	reg.put(SegmentType.Pragma.toString() + Segment.PRIVATE, pragmaImage);

	Image schemaFunctionImage = imageDescriptorFromPlugin(pluginId, "icons/func.png").createImage();
	reg.put("Schema" + SegmentType.Function.toString(), schemaFunctionImage);
	Image schemaProcedureImage = imageDescriptorFromPlugin(pluginId, "icons/proc.png").createImage();
	reg.put("Schema" + SegmentType.Procedure.toString(), schemaProcedureImage);
	Image schemaFieldImage = imageDescriptorFromPlugin(pluginId, "icons/const.png").createImage();
	reg.put("Schema" + SegmentType.Field.toString(), schemaFieldImage);
	reg.put("Schema" + SegmentType.Constant.toString(), schemaFieldImage);
	Image schemaTypeImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_ball_glass_yellow.png").createImage();
	reg.put("Schema" + SegmentType.Type.toString(), schemaTypeImage);
	reg.put("Schema" + SegmentType.SubType.toString(), schemaTypeImage);
	Image schemaPackageImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_yellow.png").createImage();
	reg.put("Schema" + SegmentType.Package.toString(), schemaPackageImage);
	Image schemaSchemaImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_triangle_green.png").createImage();
	reg.put("Schema" + SegmentType.Schema.toString(), schemaSchemaImage);
	Image schemaLabelImage = imageDescriptorFromPlugin(pluginId, "icons/worker.png").createImage();
	reg.put("Schema" + SegmentType.Label.toString(), schemaLabelImage);
	Image schemaTableImage = imageDescriptorFromPlugin(pluginId, "icons/table.png").createImage();
	reg.put("Schema" + SegmentType.Table.toString(), schemaTableImage);
	Image schemaColumnImage = imageDescriptorFromPlugin(pluginId, "icons/bullet_square_red.png").createImage();
	reg.put("Schema" + SegmentType.Column.toString(), schemaColumnImage);

	//
	Image acceptImage = imageDescriptorFromPlugin(pluginId, "icons/accept.png").createImage();
	reg.put("tick", acceptImage);
	Image removeImage = imageDescriptorFromPlugin(pluginId, "icons/remove.png").createImage();
	reg.put("cross", removeImage);
	Image acceptDbImage = imageDescriptorFromPlugin(pluginId, "icons/database_accept.png").createImage();
	reg.put("checked", acceptDbImage);
	Image removeDbImage = imageDescriptorFromPlugin(pluginId, "icons/database_remove.png").createImage();
	reg.put("unchecked", removeDbImage);

	//
	reg.put("FAILURE",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/database_remove.png").createImage());
	reg.put("SUCCESS",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/database_accept.png").createImage());

	// constraints
	reg.put("UniqueKeyConstraint",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/U.gif").createImage());
	reg.put("PrimaryKeyConstraint",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Key.gif").createImage());
	reg.put("ForeignKeyConstraint",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Pin.gif").createImage());
	reg.put("DataConstraint",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Gauge.gif").createImage());
	reg.put("Schema", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Cylinder.gif").createImage());
	reg.put("Table", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/PieGraph.gif").createImage());
	reg.put("Column", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Column.gif").createImage());

	// grants
	reg.put("Grant.EXECUTE",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/GreenFlag.gif").createImage());
	reg.put("Grant.FLASHBACK",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/CameraFlash.gif").createImage());
	reg.put("Grant.ALTER", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Hammer.gif").createImage());
	reg.put("Grant.ON COMMIT REFRESH",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Paste.gif").createImage());
	reg.put("Grant.DEBUG", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Bomb.gif").createImage());
	reg.put("Grant.DELETE",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DeleteRow.gif").createImage());
	reg.put("Grant.UPDATE",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DocumentDraw.gif").createImage());
	reg.put("Grant.QUERY REWRITE",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DocumentOut.gif").createImage());
	reg.put("Grant.SELECT",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/Binocular.gif").createImage());
	reg.put("Grant.INDEX",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/BarGraph.gif").createImage());
	reg.put("Grant.INSERT",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DocumentIn.gif").createImage());
	reg.put("Grant.REFERENCES",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DocumentMag.gif").createImage());
	reg.put("Grant.MERGE VIEW",
		PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/DataStore.gif").createImage());

	//
	reg.put("Refresh", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/NewSheet.gif").createImage());
	reg.put("Trigger", PlsqleditorPlugin.imageDescriptorFromPlugin(pluginId, "icons/worker.png").createImage());
    }

    /**
     * @return The plugin's single instance per project of the schema registry.
     */
    public SchemaRegistry getSchemaRegistry(IResource resource) {
	String projectName = resource.getProject().getName();
	SchemaRegistry sr = mySchemaRegistries.get(projectName);
	if (sr == null) {
	    sr = new SchemaRegistry(projectName, getStateLocation());
	    mySchemaRegistries.put(projectName, sr);
	}
	return sr;
    }

    /**
     * @return The plugin's single instance per project of the connection registry.
     */
    public ConnectionRegistry getConnectionRegistry() {
	if (myConnectionRegistry == null) {
	    myConnectionRegistry = new ConnectionRegistry(getStateLocation());
	}
	return myConnectionRegistry;
    }

    /**
     * @return The myPluginRef's single instance of the schema registry.
     */
    public PackageStore getPackageStore(IResource resource) {
	if (resource == null) {
	    throw new IllegalStateException("The resource supplied to retrieve the package store is null");
	}
	IProject project = resource.getProject();
	String projectName = project.getName();
	PackageStore packageStore = myPackageStores.get(projectName);
	if (packageStore == null) {
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
    public IFile[] getFiles(String schema, String packageName) {
	return getPackageStore(getProject()).getFiles(schema, packageName);
    }

    /**
     * @param project
     */
    public void setProject(IProject project) {
	myProject = project;
    }

    public IProject getProject() {
	return myProject;
    }

    /**
     * @return The list of segments that are obtained from the current document.
     */
    public List<Segment> getCurrentSegments(IDocument doc) {
	if (doc == null) {
	    return myCurrentSegments;
	}
	if (myCurrentDoc != doc) {
	    myCurrentDoc = doc;
	    myCurrentSegments = getSegments(myCurrentFile, doc, false);
	}
	return myCurrentSegments;
    }

    /**
     * @return {@link #myCurrentFileName}.
     */
    public String getCurrentFileName() {
	if (myCurrentFile != null) {
	    return myCurrentFile.getName();
	}
	return null;
    }

    public static void log(String msg, Exception e) {
	if (msg == null) {
	    msg = "";
	}
	getDefault().getLog()
		.log(new Status(IStatus.INFO, getDefault().getBundle().getSymbolicName(), IStatus.OK, msg, e));
    }

    public void setCurrentFile(IFile file) {
	myCurrentFile = file;
    }

    public IDocumentProvider getDocumentProvider() {
	if (fDocumentProvider == null)
	    fDocumentProvider = new PlSqlDocumentProvider();
	return fDocumentProvider;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
    }

    public static String getPluginId() {
	return theId;
    }

    public String getPlsqlExecutable() {
	return getPreferenceStore().getString(PreferenceConstants.P_SQLPLUS_EXECUTABLE);
    }

    /**
     * @return an unmodifiable list of File objects representing directories in the
     *         project's include path. These are the entries explicitly configured
     *         in the project's properties.
     * @see {@link #getEffectiveIncPath}
     */
    public List<File> getIncPath(IProject project) {
	XMLUtilities xmlUtil = new XMLUtilities();
	return makeAbsIncPath(project, xmlUtil.getIncludeEntries(project, true));
    }

    /**
     * @return an unmodifiable list of Strings representing directories in the
     *         project's include path. These are the exact entries explicitly
     *         configured in the project's properties.
     * @see {@link #getIncPath}
     */
    public List<String> getRawIncPath(IProject project) {
	XMLUtilities xmlUtil = new XMLUtilities();
	return Arrays.asList(xmlUtil.getIncludeEntries(project, true));
    }

    /**
     * @return the project's directory in the file system
     */
    public File getProjectDir(IProject project) {
	return project.getFile(new Path("x")).getRawLocation().toFile().getParentFile();
    }

    private List<File> makeAbsIncPath(IProject project, String[] relIncPath) {
	List<File> dirs = new ArrayList<File>();
	File projectDir = getProjectDir(project);

	for (int i = 0; i < relIncPath.length; i++) {
	    File f = new File(relIncPath[i]);
	    if (!f.isAbsolute())
		f = new File(projectDir, relIncPath[i]);
	    dirs.add(f);
	}
	return Collections.unmodifiableList(dirs);
    }

    public static String getUniqueIdentifier() {
	PlsqleditorPlugin plugin = getDefault();
	return plugin != null ? plugin.getBundle().getSymbolicName() : "org.boomsticks.plsqleditor";
    }

    /**
     * Returns a color with the requested RGB value.
     */
    public Color getColor(RGB rgb) {
	return fColorProvider.getColor(rgb);
    }

    /**
     * Returns a colour represented by the given preference setting.
     */
    public Color getColor(String preferenceKey) {
	return getColor(PreferenceConverter.getColor(getPreferenceStore(), preferenceKey));
    }

    /**
     * This method just gets the url and user name for a given project. If the
     * details are not specific to the project, it will retrieve the global values.
     *
     * @param project The project whose possibly project specific url and user name
     *                are sought.
     *
     * @return A temporary ConnectionDetails containing the supplied
     *         <code>project</code>'s specific user name and url.
     */
    public ConnectionDetails getDefaultDbConnectionDetails(IProject project) {
	String url = "";
	String user = "";
	IPreferenceStore prefs = getPreferenceStore();

	try {
	    String isUsingLocalSettings = project
		    .getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_LOCAL_DB_SETTINGS));
	    if (Boolean.valueOf(isUsingLocalSettings).booleanValue()) {
		url = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_URL));
		user = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_USER));
	    } else {
		url = prefs.getString(PreferenceConstants.P_URL);
		user = prefs.getString(PreferenceConstants.P_USER);
	    }
	} catch (CoreException e) {
	    e.printStackTrace();
	}
	ConnectionDetails cd = new ConnectionDetails(null, url, user, null);
	return cd;
    }

    /**
     * @return the openConnectionList
     */
    public OpenConnectionList getOpenConnectionList() {
	return myOpenConnectionList;
    }

    private MessageConsole findConsole(String name) {
	ConsolePlugin plugin = ConsolePlugin.getDefault();
	IConsoleManager conMan = plugin.getConsoleManager();
	IConsole[] existing = conMan.getConsoles();
	for (int i = 0; i < existing.length; i++)
	    if (name.equals(existing[i].getName()))
		return (MessageConsole) existing[i];
	// no console found, so create a new one
	MessageConsole myConsole = new MessageConsole(name, null);
	conMan.addConsoles(new IConsole[] { myConsole });
	return myConsole;
    }

    public void sendMessageToConsole(String message) {
	MessageConsole myConsole = findConsole(CONSOLE_NAME);
	MessageConsoleStream out = myConsole.newMessageStream();
	out.println(message);
	try {
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void showConsole() {
	MessageConsole myConsole = findConsole(CONSOLE_NAME);
	IWorkbenchPage page = null;
	try {
	    page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	} catch (NullPointerException npe) {
	    log("Failed to display the console because the active page is null", npe);
	    return;
	}
	String id = IConsoleConstants.ID_CONSOLE_VIEW;
	IConsoleView view;
	try {
	    view = (IConsoleView) page.showView(id);
	    view.display(myConsole);
	} catch (PartInitException e) {
	    log("Failed to display the console : " + e.getMessage(), e);
	    e.printStackTrace();
	}
    }

    public static String getSchema(IFile file, IProject project, String schemaName) {
	String isUsingLocalSettings = "false";
	String user = schemaName;
	String tmpUser = "";
	try {
	    isUsingLocalSettings = project
		    .getPersistentProperty(new QualifiedName("", PreferenceConstants.USE_LOCAL_DB_SETTINGS));
	    if (Boolean.valueOf(isUsingLocalSettings).booleanValue()) {
		tmpUser = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_USER));
	    }
	} catch (CoreException e) {
	    e.printStackTrace();
	}
	String header = "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] +[Oo][Rr] +[Rr][Ee][Pp][Ll][Aa][Cc][Ee] +[Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
	String body = header + "+[Bb][Oo][Dd][Yy] ";
	String withoutSchema = "\\W*(\\w+).*";
	String withSchema = "\\W*(\\w+)\\.\\W*(\\w+).*";
	String declare = "\\W*[Dd][Ee][Cc][Ll][Aa][Rr][Ee].*";
	String begin = "\\W*[Bb][Ee][Gg][Ii][Nn] ";
	IPreferenceStore thePrefs = DbUtility.getPrefs();
	if (thePrefs.getBoolean(PreferenceConstants.P_ALLOW_SCHEMA_LOADING)) {
	    try {
		// Determine if schema is in the bodyStart string
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		String line = null;
		while ((line = br.readLine()) != null) {
		    if ((line.matches(header + withSchema)) || (line.matches(body + withSchema))) {
			if (tmpUser.equals("") || tmpUser == null) {
			    user = thePrefs.getString(PreferenceConstants.P_USER);
			} else {
			    user = tmpUser;
			}
			break;
		    }
		    // if other start tags found, lets break the loop asap
		    else if ((line.matches(header + withoutSchema)) || (line.matches(body + withoutSchema))
			    || line.matches(declare) || line.matches(begin)) {
			break;
		    }
		}
		br.close();
	    } catch (CoreException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return user;
    }
}
