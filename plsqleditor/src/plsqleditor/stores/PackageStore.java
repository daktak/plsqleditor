package plsqleditor.stores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.parsers.ContentOutlineParser;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.ContentOutlineParser.Type;
import plsqleditor.preferences.entities.PackageDetails;
import plsqleditor.preferences.entities.SchemaDetails;

/**
 * This class represents a store of all the schema, package and segment (function, procedure field)
 * information about the pl-sql files in the project. Currently it does not query the database for
 * extra information.
 * 
 * @author Toby Zines
 */
public class PackageStore
        implements
            SchemaRegistry.RegistryUpdateListener,
            DbUtility.DbPrefsUpdateListener
{
    private String                    myCurrentSchemaName;
    private ContentOutlineParser      myParser;
    private DBPackageStore            myDbPackageStore;

    /**
     * This field maps a schema name to a PlSqlSchema it represents. This schema will in turn
     * contain all the loaded packages.
     */
    private Map<String, PlSqlSchema>  mySchemaNameToSchemaMap;

    /**
     * This field maps an IFile name to the PlSql package backing it.
     */
    private Map<String, PlSqlPackage> myFileToPackageMap;

    private SchemaRegistry            myRegistry;

    public PackageStore(IPreferenceStore prefs, SchemaRegistry registry)
    {
        myRegistry = registry;
        myParser = new ContentOutlineParser();
        initMappings();
        initDbPackageStore();
        myRegistry.addListener(this);
        DbUtility.addListener(this);
    }

    private void initMappings()
    {
        mySchemaNameToSchemaMap = new HashMap<String, PlSqlSchema>();
        myFileToPackageMap = new HashMap<String, PlSqlPackage>();
        for (SchemaDetails sd : myRegistry.getSchemaMappings())
        {
            String schema = sd.getName();
            for (String strLoc : sd.getLocations())
            {
                IPath loc = new Path(strLoc);
                // this is in case there are no packages
                loadSchemaLocation(schema, loc);
                for (PackageDetails pd : sd.getPackages())
                {
                    addPackage(schema, loc, pd.getName(), pd.getLocation(), false);
                }
            }
        }
    }

    private void initDbPackageStore()
    {
        try
        {
            myDbPackageStore = new DBPackageStore(DbUtility.getDbaConnectionPool());
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the list of currently known schema names.
     * 
     * @return The list of currently known schema names.
     */
    public List<String> getSchemas()
    {
        List<String> list = new ArrayList<String>();
        list.addAll(mySchemaNameToSchemaMap.keySet());
        if (myDbPackageStore != null)
        {
            try
            {
                for (String schema : myDbPackageStore.getSchemas())
                {
                    String lowerSchema = schema.toLowerCase();
                    if (!list.contains(lowerSchema))
                    {
                        list.add(lowerSchema);
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * This method stores the segments of a particular package within the current schema.
     * 
     * @param packageName The name of the package that this is linked to.
     * 
     * @param segments
     */
    public void storeSegments(String schemaName,
                              IFile file,
                              String filename,
                              String packageName,
                              List<Segment> segments)
    {
        PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
        if (schema == null)
        {
            schema = new PlSqlSchema(schemaName, new Source(null, Source.Type.File));
            mySchemaNameToSchemaMap.put(schema.getName(), schema);
        }
        PlSqlPackage pkg = schema.getPackage(packageName);
        if (!filename.contains(".pkb"))
        {
            if (pkg == null)
            {
                pkg = new PlSqlPackage(schema, packageName, new Source(new Path(filename),
                        Source.Type.File));
            }
            for (Segment s : segments)
            {
                if (!pkg.contains(s))
                {
                    pkg.add(s, file == null ? IResource.NULL_STAMP : file.getModificationStamp());
                }
            }
        }
        else
        {
            if (pkg == null)
            {
                pkg = new PlSqlPackage(schema, packageName, new Source(new Path(filename),
                        Source.Type.File));
            }
            pkg.setSegments(segments, file == null ? IResource.NULL_STAMP : file
                    .getModificationStamp());
        }
        schema.addPackage(pkg);
        myFileToPackageMap.put(filename, pkg);
    }

    /**
     * This method gets the segments from a particular document, storing them along the way under
     * the current schema. It assumes that the current schema has been set correctly.
     * 
     * @param document
     * 
     * @return the list of segments from the document.
     */
    public List<Segment> getSegments(IFile file, String filename, IDocument document)
    {
        try
        {
            String[] packageName = new String[1];
            List<Segment> segments;
            ContentOutlineParser.Type type = getType(filename);
            PlSqlPackage pkg = myFileToPackageMap.get(filename);
            segments = myParser.parseFile(type, document, packageName);
            if (pkg != null)
            {
                storeSegments(pkg.getSchema().getName(), file, filename, pkg.getName(), segments);
            }
            else
            {
                if (myCurrentSchemaName == null)
                {
                    if (filename.contains("_"))
                    {
                        setCurrentSchema(filename.substring(0, filename.indexOf('_')));
                    }
                    else
                    {
                        setCurrentSchema("unknown");
                    }
                }
                // uses myCurrentSchemaName
                storeSegments(getCurrentSchema(), file, filename, packageName[0], segments);
            }
            return segments;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Type getType(String filename)
    {
        return filename.contains(".pkb") ? ContentOutlineParser.Type.Package_Body : filename
                .contains(".pkh")
                ? ContentOutlineParser.Type.Package
                : ContentOutlineParser.Type.SqlScript;
    }

    public List<Segment> getSegments(IDocument document)
    {
        try
        {
            String[] packageName = new String[1];
            List<Segment> segments;
            segments = myParser.parseFile(ContentOutlineParser.Type.Package_Body,
                                          document,
                                          packageName);
            String fileNameDummy = getCurrentSchema() + "_" + packageName[0] + ".pkb";
            storeSegments(getCurrentSchema(), null, fileNameDummy, packageName[0], segments);
            return segments;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public List<Segment> getSegments(String schemaName, String packageName)
    {
        schemaName = schemaName == null ? myCurrentSchemaName : schemaName;
        PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
        if (schema == null)
        {
            schema = new PlSqlSchema(schemaName, new Source(new Path(""), Source.Type.File));
            mySchemaNameToSchemaMap.put(schema.getName(), schema);
        }
        PlSqlPackage pkg = schema.getPackage(packageName);
        if (pkg == null)
        {
            pkg = new PlSqlPackage(schema, packageName, new Source(new Path(""), Source.Type.File));
            schema.addPackage(pkg);
        }
        List<Segment> segments = pkg.getSegments();
        if (segments == null || segments.size() == 0)
        {
            String oldSchema = myCurrentSchemaName;
            myCurrentSchemaName = schema.getName();
            try
            {
                PlSqlPackage p = schema.getPackage(packageName);
                if (p != null)
                {
                    segments = p.getSegments();
                    if (segments == null || segments.size() == 0)
                    {
                        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                        IProject project = PlsqleditorPlugin.getDefault().getProject();
                        // note, could return list from here, but this ignores files updated
                        // by other users and injected into the work space.
                        if (p.getSourceType().getType() == Source.Type.File)
                        {
                            IPath location = p.getSourceType().getSource();
                            if (location.toString().trim().length() > 0)
                            {
                                for (Source source : schema.getSources())
                                {
                                    IPath schemaLocation = source.getSource();
                                    IPath fullPath = project.getLocation().append(schemaLocation)
                                            .append(location);
                                    IFile[] files = root.findFilesForLocation(fullPath);
                                    if (files.length > 0 && files[0].exists())
                                    {
                                        segments = getSegments(files[0],
                                                               location.toString(),
                                                               getDoc(files[0]));
                                        p.setSegments(segments, files[0].getModificationStamp());
                                        break;
                                    }
                                }
                            }
                        }
                        if (segments == null || segments.size() == 0)
                        {
                            getPackages(schemaName);
                            segments = p.getSegments();
                            if (segments == null)
                            {
                                segments = new ArrayList<Segment>();
                            }
                        }
                    }
                    else
                    {
                        List<Segment> newSegments = p.getSegments();
                        if (newSegments != null)
                        {
                            segments.addAll(newSegments);
                        }
                    }
                }
                pkg.setSegments(segments, IResource.NULL_STAMP);
            }
            finally
            {
                myCurrentSchemaName = oldSchema;
            }
            // find file, open it and store segments
        }
        if (segments == null)
        {
            segments = new ArrayList<Segment>();
        }
        if (segments.size() == 0 && myDbPackageStore != null)
        {
            try
            {
                segments = myDbPackageStore.getSegments(schemaName, packageName);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return segments;
    }

    /**
     * This method
     * 
     * @param schemaName
     * @param packageName
     */
    private PlSqlPackage addPackage(String schemaName,
                                    IPath schemaLocation,
                                    String packageName,
                                    String filename,
                                    boolean updateRegistry)
    {
        PlSqlSchema schema = loadSchemaLocation(schemaName, schemaLocation);
        PlSqlPackage pkg;
        if (filename.contains(".pkb"))
        {
            pkg = new PlSqlPackage(schema, packageName, new Source(new Path(filename),
                    Source.Type.File));
            schema.addPackage(pkg);
        }
        else if ((pkg = schema.getPackage(packageName)) == null)
        {
            pkg = new PlSqlPackage(schema, packageName, new Source(new Path(filename),
                    Source.Type.File));
            schema.addPackage(pkg);
        }


        if (updateRegistry)
        {
            List<String> locations = new ArrayList<String>();
            for (Source src : schema.getSources())
            {
                locations.add(src.getSource().toString());
            }
            SchemaDetails sd = new SchemaDetails(schemaName, locations, "");
            SchemaDetails[] all = myRegistry.getSchemaMappings();
            List<SchemaDetails> list = new ArrayList<SchemaDetails>();
            int index = -1;
            int i = 0;
            for (SchemaDetails sdi : all)
            {
                list.add(sdi);
                if (sdi.getName().equals(sd.getName()))
                {
                    index = i;
                }
                i++;
            }
            if (index != -1)
            {
                sd = list.get(index);
            }
            else
            {
                list.add(sd);
                myRegistry.setSchemaMappings(list.toArray(new SchemaDetails[list.size()]));
            }
            sd.addPackage(new PackageDetails(packageName, filename));
        }
        return pkg;
    }

    private PlSqlSchema loadSchemaLocation(String schemaName, IPath schemaLocation)
    {
        PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
        Source src = new Source(schemaLocation, Source.Type.File);
        if (schema == null)
        {
            schema = new PlSqlSchema(schemaName, src);
            mySchemaNameToSchemaMap.put(schema.getName(), schema);
        }
        else
        {
            schema.addSource(src);
        }
        return schema;
    }

    public List<String> getPackages(String schemaName)
    {
        PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
        List<String> list = new ArrayList<String>();
        if (schema != null)
        {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject project = PlsqleditorPlugin.getDefault().getProject();
            for (Source source : schema.getSources())
            {
                IPath schemaLocation = source.getSource();
                if (schemaLocation != null)
                {
                    if (project.exists(schemaLocation))
                    {
                        IPath fullPath = project.getLocation().append(schemaLocation);
                        IContainer[] containers = root.findContainersForLocation(fullPath);
                        if (containers.length > 0)
                        {
                            try
                            {
                                loadPackageFile(containers);
                                break;
                            }
                            catch (CoreException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Path " + schemaLocation + " does not exist.");
                    }
                }
            }
            for (PlSqlPackage pkg : schema.getPackages().values())
            {
                if (pkg.getName() == null)
                {
                    System.out.println("Null pkg name - BAD");
                }
                else
                {
                    list.add(pkg.getName());
                }
            }
        }
        if (myDbPackageStore != null)
        {
            try
            {
                List<String> extraPackages = myDbPackageStore.getPackages(schemaName);
                for (String pkg : extraPackages)
                {
                    String lowerPkg = pkg.toLowerCase();
                    if (!list.contains(lowerPkg))
                    {
                        list.add(lowerPkg);
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return list;
    }

    private void loadPackageFile(IContainer[] containers) throws CoreException
    {
        IResource[] resources = containers[0].members();
        for (IResource file : resources)
        {
            if (file instanceof IFile)
            {
                IFile ifile = (IFile) file;
                if (file.getName().endsWith(".pkb") || file.getName().endsWith(".pkh"))
                {
                    loadPackageFile(ifile, null, false, false);
                }
            }
        }
        // this will only save if it detects modifications have been made
        myRegistry.saveSchemaMappings(false);
    }

    /**
     * This method gets an IDocument from a supplied IFile.
     * 
     * @param file The file whose appropriate document is sought.
     * 
     * @return The document for the given <code>file</code>.
     */
    public static IDocument getDoc(IFile file)
    {
        IDocument doc = new Document();
        String separator = System.getProperty("line.separator");
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(separator);
            }
            doc.set(sb.toString());
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return doc;
    }

    public void loadPackageFile(IFile file, IDocument doc, boolean force, boolean updateRegistry)
    {
        IPath schemaLocation = file.getProjectRelativePath().removeLastSegments(1);
        String filename = file.getName();
        PlSqlPackage pkg = myFileToPackageMap.get(filename);
        List<Segment> segments = pkg == null ? null : pkg.getSegments();
        if (segments == null || segments.size() == 0 || force)
        {
            if (segments != null && segments.size() > 0)
            {
                long timeStamp = file.getModificationStamp();
                if (pkg.getLatestChange() >= timeStamp)
                {
                    return;
                }
            }
            String schemaName = null;
            String packageName = null;
            if (pkg == null)
            {
                if (filename.contains("_") && filename.contains("."))
                {
                    schemaName = filename.substring(0, filename.indexOf('_'));
                    packageName = filename.substring(filename.indexOf('_') + 1, filename
                            .lastIndexOf('.'));
                }
                // TODO need to do something where the schema name is not in the file name
            }
            else
            {
                schemaName = pkg.getSchema().getName();
                packageName = pkg.getName();
            }
            if (schemaName != null)
            {
                pkg = addPackage(schemaName, schemaLocation, packageName, filename, true);
                String oldSchemaName = myCurrentSchemaName;

                setCurrentSchema(schemaName);
                try
                {
                    String[] discoveredPackageName = new String[1];
                    if (doc == null)
                    {
                        doc = getDoc(file);
                    }
                    ContentOutlineParser.Type type = getType(filename);
                    segments = myParser.parseFile(type, doc, discoveredPackageName);
                    myFileToPackageMap.put(filename, pkg);
                    // TODO should check discoveredPackageName[0] is same as packageName
                    storeSegments(getCurrentSchema(), file, filename, packageName, segments);
                    if (updateRegistry)
                    {
                        myRegistry.saveSchemaMappings(false);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (!force)
                    {
                        setCurrentSchema(oldSchemaName);
                    }
                }
            }
        }
    }

    /**
     * This method
     * 
     * @param schemaName
     */
    public void setCurrentSchema(String schemaName)
    {
        myCurrentSchemaName = schemaName;
    }

    /**
     * @return
     */
    public String getCurrentSchema()
    {
        return myCurrentSchemaName;
    }

    /**
     * @param schema
     * @param packageName
     * @return
     */
    public IFile getFile(String schema, String packageName)
    {
        PlSqlSchema schm = mySchemaNameToSchemaMap.get(schema);
        if (schm != null)
        {
            PlSqlPackage pkg = schm.getPackage(packageName);
            if (pkg != null)
            {
                for (Source source : schm.getSources())
                {
                    IPath schemaLocation = source.getSource();
                    String filename = pkg.getSourceType().getSource().toString();
                    IProject project = PlsqleditorPlugin.getDefault().getProject();
                    IPath fullpath = schemaLocation.append(filename);
                    if (project.exists(fullpath))
                    {
                        return project.getFile(fullpath);
                    }
                }
            }
        }
        return null;
    }

    /**
     * @see plsqleditor.stores.SchemaRegistry.RegistryUpdateListener#registryUpdated()
     */
    public void registryUpdated()
    {
        initMappings();
    }

    /**
     * @see plsqleditor.db.DbUtility.DbPrefsUpdateListener#dbPrefsUpdated()
     */
    public void dbPrefsUpdated()
    {
        initDbPackageStore();
    }
}
