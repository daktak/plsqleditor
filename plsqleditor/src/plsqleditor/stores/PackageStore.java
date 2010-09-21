package plsqleditor.stores;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import au.com.gts.data.Grant;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.parsers.ContentOutlineParser;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.ParseType;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.parsers.Segment;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.preferences.entities.PackageDetails;
import plsqleditor.preferences.entities.PackageLocation;
import plsqleditor.preferences.entities.SchemaDetails;

/**
 * This class represents a store of all the schema, package and segment
 * (function, procedure field) information about the pl-sql files in the
 * project. Currently it does not query the database for extra information.
 * 
 * @author Toby Zines
 */
public class PackageStore implements SchemaRegistry.RegistryUpdateListener,
		DbUtility.DbPrefsUpdateListener
{
	private String myCurrentSchemaName;
	private ContentOutlineParser myParser;
	private DBPackageStore myDbPackageStore;

	/**
	 * This field maps a schema name to a PlSqlSchema it represents. This schema
	 * will in turn contain all the loaded packages.
	 */
	private SortedMap<String, PlSqlSchema> mySchemaNameToSchemaMap;

	/**
	 * This field maps an IFile <b>name</b> to the PlSql package backing it.
	 */
	private Map<String, PlSqlPackage> myFileNameToPackageMap;

	private SchemaRegistry myRegistry;
	private IProject myProject;

	public PackageStore(IProject project, IPreferenceStore prefs,
			SchemaRegistry registry)
	{
		myProject = project;
		myRegistry = registry;
		myParser = new ContentOutlineParser();
		initMappings();
		initDbPackageStore(project);
		myRegistry.addListener(this);
		DbUtility.addListener(this);
	}

	private void initMappings()
	{
		mySchemaNameToSchemaMap = new TreeMap<String, PlSqlSchema>();
		myFileNameToPackageMap = new HashMap<String, PlSqlPackage>();
		SchemaDetails[] sds = myRegistry.getSchemaMappings();
		for (int i = 0; i < sds.length; i++)
		{
			SchemaDetails sd = sds[i];
			String schema = sd.getName();

			for (Iterator<String> it = sd.getLocations().iterator(); it
					.hasNext();)
			{
				String strLoc = it.next();
				IPath loc = new Path(strLoc);
				// this is in case there are no packages
				loadSchemaLocation(schema, loc);
				PackageDetails[] pds = sd.getPackages();
				for (int j = 0; j < pds.length; j++)
				{
					PackageDetails pd = pds[j];
					PackageLocation[] pkgLocs = pd.getLocations();
					for (int k = 0; k < pkgLocs.length; k++)
					{
						PackageLocation pkgLoc = pkgLocs[k];
						addPackage(schema, loc, pd.getName(), pkgLoc
								.getLocation(), pkgLoc.getParseType(), false,
								true);
					}
				}
			}
		}
	}

	private void initDbPackageStore(IProject project)
	{
		try
		{
			myDbPackageStore = new DBPackageStore(DbUtility
					.getDbaConnectionPool(project));
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
	public SortedSet<String> getSchemas()
	{
		SortedSet<String> set = new TreeSet<String>();

		if (myDbPackageStore != null)
		{
			try
			{
				for (Iterator<String> it = myDbPackageStore.getSchemas()
						.iterator(); it.hasNext();)
				{
					String schema = it.next();
					String lowerSchema = schema.toLowerCase();
					set.add(lowerSchema);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		if (set == null)
		{
			set = new TreeSet<String>();
		}
		set.addAll(mySchemaNameToSchemaMap.keySet());
		return set;
	}

	/**
	 * This method stores the segments of a particular package within the
	 * current schema.
	 * 
	 * @param schemaName
	 *            The name of the schema to use to find the schema within which
	 *            to store the supplied segments.
	 * 
	 * @param file
	 *            The file to use to create a new schema and schema name if the
	 *            schema cannot be found using the supplied
	 *            <code>schemaName</code>, and to determine the filename, which
	 *            is used to determine the type (pkb or pkh).
	 * 
	 * @param packageName
	 *            The name of the package that this is linked to.
	 * 
	 * @param segments
	 *            The list of {@link Segment} objects to store in the current
	 *            schema.
	 */
	private void storeSegments(String schemaName, IFile file,
			String packageName, List<Segment> segments)
	{
		PlSqlSchema schema = getSchema(schemaName);
		String filename = file.getName();
		if (schema == null)
		{
			IPath schemaLocation = file.getProjectRelativePath()
					.removeLastSegments(1);
			schema = new PlSqlSchema(schemaName, new Source(schemaLocation,
					PersistenceType.File));
			mySchemaNameToSchemaMap.put(schema.getName(), schema);
		}
		if (packageName == null)
		{
			packageName = "scratch";
		}
		PlSqlPackage pkg = schema.getPackage(packageName);
		ParseType parseType = PlSqlParserManager.getType(file);

		if (pkg == null)
		{
			pkg = addPackage(schema.getName(), null/*
													 * doesn't matter, already
													 * exists
													 */, packageName, filename,
					parseType, true, parseType != ParseType.Package_Body ? true
							: false);
		}
		pkg.reconcile(segments, parseType, file == null ? IResource.NULL_STAMP
				: file.getModificationStamp());
		myRegistry.saveSchemaMappings(false);
	}

	/**
	 * This method gets the name of the file without the extension (which it
	 * assumes is identified by the final dot in the file name).
	 * 
	 * @param filename
	 *            The full name of the file.
	 * @return The file name minus the extension.
	 */
	private String stripExtension(String filename)
	{
		if (!filename.contains(".")) return filename;
		return filename.substring(0, filename.lastIndexOf("."));
	}

	/**
	 * This method gets the segments from a particular document, storing them
	 * along the way under the current schema. It assumes that the current
	 * schema has been set correctly.
	 * 
	 * @param document
	 * 
	 * @return the list of {@link Segment}s from the document.
	 */
	public List<Segment> getSegments(IFile file, IDocument document,
			boolean isPriorToSetFocus)
	{
		String filename = file.getName();
		try
		{
			String[] packageName = new String[1];
			List<Segment> segments;
			ParseType type = PlSqlParserManager.getType(file);
			PlSqlPackage pkg = myFileNameToPackageMap.get(filename);
            // fix for 3071548 - null schema means file fails to open
            // a bit bodgy though, shouldn't be setting this everywhere
            PlsqleditorPlugin.getDefault().setProject(file.getProject());
            // end fix
			segments = myParser.parseFile(type, document, packageName);
			String schemaFromSegments = getSchemaNameFromSegments(segments);
			if (pkg != null)
			{
				if (schemaFromSegments != null
						&& !schemaFromSegments
								.equals(pkg.getSchema().getName()))
				{
					changeSchemaFor(file, document, schemaFromSegments, pkg
							.getName());
				}
				else
				{
					storeSegments(pkg.getSchema().getName(), file, pkg
							.getName(), segments);
				}
			}
			else
			// i don't have a package stored yet
			{
				String schemaPackageDelimiter = getSchemaPackageDelimiter(file);
				if (schemaFromSegments != null)
				{
					myCurrentSchemaName = schemaFromSegments;
				}
				else if (isPriorToSetFocus)
				{
					myCurrentSchemaName = findSchemaNameForFile(file);
				}
				if (myCurrentSchemaName == null)
				{
					if (stripExtension(filename)
							.indexOf(schemaPackageDelimiter) != -1)
					{
						setCurrentSchema(filename.substring(0, filename
								.indexOf(schemaPackageDelimiter)));
					}
					else
					{
						String schemaName = findSchemaNameForFile(file);
						if (schemaName == null)
						{
							schemaName = findSchemaNameFromFolder(file);
						}
						setCurrentSchema(schemaName != null ? schemaName
								: "unknown");
					}
				}
				// uses myCurrentSchemaName
				storeSegments(getCurrentSchema(), file, packageName[0],
						segments);
			}
			return segments;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String getSchemaPackageDelimiter(IResource resource)
	{
		String schemaPackageDelimiter = null;
		try
		{
			if (resource != null)
			{
				IProject project = resource.getProject();
				String usingProjectSpecific = project.getPersistentProperty(new QualifiedName(
			            "", PreferenceConstants.USE_LOCAL_DB_SETTINGS));
			    if (Boolean.valueOf(usingProjectSpecific).booleanValue())
			    {
			    	schemaPackageDelimiter = project.getPersistentProperty(new QualifiedName("",
			                PreferenceConstants.P_SCHEMA_PACKAGE_DELIMITER));
			    	return schemaPackageDelimiter;
			    }
			}
		}
		catch (CoreException e)
		{
			PlsqleditorPlugin.log("Failed to find project specific package delimiter, using default", e);
		}
		schemaPackageDelimiter = PlsqleditorPlugin.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.P_SCHEMA_PACKAGE_DELIMITER);
		if (schemaPackageDelimiter == null
				|| schemaPackageDelimiter.length() == 0)
		{
			schemaPackageDelimiter = "_";
		}
		return schemaPackageDelimiter;
	}

	/**
	 * Get schema from Schema details schema locations according to the file
	 * location
	 * 
	 * @param file
	 * @return
	 */
	private String findSchemaNameFromFolder(IFile file)
	{
		SchemaDetails[] schemaDetails = myRegistry.getSchemaMappings();
		List schemaLocations = null;
		IPath schemaLocation = file.getProjectRelativePath()
				.removeLastSegments(1);
		for (int i = 0; i < schemaDetails.length; i++)
		{
			schemaLocations = schemaDetails[i].getLocations();
			for (Iterator it = schemaLocations.iterator(); it.hasNext();)
			{
				String location = (String) it.next();
				if (location.equals(schemaLocation.toString()))
				{
					return schemaDetails[i].getName();
				}
			}
		}
		return findSchemaNameForFile(file);
	}

	/**
	 * This method gets the name of the schema associated with the supplied
	 * <code>file</code>.
	 * 
	 * @param file
	 *            The file whose preexisting schema we are looking for.
	 * 
	 * @return The already defined schema of the associated file, or null if it
	 *         is not already defined.
	 */
	public String findSchemaNameForFile(IFile file)
	{
		IPath parent = file.getParent().getFullPath();
		for (Iterator<PlSqlSchema> it = mySchemaNameToSchemaMap.values()
				.iterator(); it.hasNext();)
		{
			PlSqlSchema schema = it.next();
			Source[] sources = schema.getSources();
			for (int i = 0; i < sources.length; i++)
			{
				Source source = sources[i];
				IPath path = source.getSource();
				if (path != null && path.equals(parent))
				{
					return schema.getName();
				}
			}
		}
		return getSchemaForFileByFileToPackageMap(file);
	}

	/**
	 * This method gets the segments for the names schema and package. In
	 * general this will be used on files that aren't currently open (or do not
	 * need to be open).
	 * 
	 * @param schemaName
	 *            The name of the schema whose owned package's segments are
	 *            sought.
	 * 
	 * @param packageName
	 *            The name of the package within the specified
	 *            <code>schemaName</code> whose segments are sought.
	 * 
	 * @return The list of {@link Segment}s for the particular schema and
	 *         package.
	 */
	public List<Segment> getSegments(String schemaName, String packageName)
	{
		schemaName = schemaName == null ? myCurrentSchemaName : schemaName;
		PlSqlSchema schema = getSchema(schemaName);
		if (schema == null)
		{
			schema = new PlSqlSchema(schemaName, new Source(new Path(""),
					PersistenceType.File));
			mySchemaNameToSchemaMap.put(schema.getName(), schema);
		}
		PlSqlPackage pkg = schema.getPackage(packageName);
		List<Segment> segments = null;
		if (pkg == null)
		{
			// this line of code causes the bug where we see another non
			// package object appear as a package....
			// TODO remove this, and if required add appropriate actual desired
			// functionality (that this was trying to do)
			// pkg = new PlSqlPackage(schema, packageName, new Source(new
			// Path(""), SourceType.File));
			// schema.addPackage(pkg);
		}
		else
		{
			segments = pkg.getSegments();
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
							IWorkspaceRoot root = ResourcesPlugin
									.getWorkspace().getRoot();
							IProject project = PlsqleditorPlugin.getDefault()
									.getProject();
							// note, could return list from here, but this
							// ignores files updated
							// by other users and injected into the work space.
							Source primarySource = p.getPrimarySource();
							if (primarySource.getType() == PersistenceType.File)
							{
								IPath location = primarySource.getSource();
								if (location.toString().trim().length() > 0)
								{
									Source[] sources = schema.getSources();
									for (int i = 0; i < sources.length; i++)
									{
										Source source = sources[i];
										IPath schemaLocation = source
												.getSource();
										IPath fullPath = project.getLocation()
												.append(schemaLocation).append(
														location);
										IFile[] files = root
												.findFilesForLocation(fullPath);
										if (files.length > 0
												&& files[0].exists())
										{
											segments = getSegments(files[0],
													getDoc(files[0]), false);
											p.setSegments(segments, files[0]
													.getModificationStamp());
											break;
										}
									}
								}
							}
							if (segments == null || segments.size() == 0)
							{
								getPackages(schemaName, false);
								segments = p.getSegments();
								if (segments == null)
								{
									segments = new ArrayList();
								}
							}
						}
						else
						{
							List newSegments = p.getSegments();
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
		}
		if (segments == null)
		{
			segments = new ArrayList();
		}
		if (segments.size() == 0 && myDbPackageStore != null)
		{
			try
			{
				segments = myDbPackageStore
						.getSegments(schemaName, packageName);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return segments;
	}

	/**
	 * This method gets a {@link PlSqlSchema} schema for the supplied name, or
	 * null if it is not present.
	 * 
	 * @param schemaName
	 *            The name of the schema desired
	 * 
	 * @return a {@link PlSqlSchema} schema for the supplied name, or null if it
	 *         is not present.
	 */
	public PlSqlSchema getSchema(String schemaName)
	{
		return mySchemaNameToSchemaMap.get(schemaName);
	}

	/**
	 * This method adds a package to a schema that may or may not already exist.
	 * 
	 * @param schemaName
	 * @param schemaLocation
	 * @param packageName
	 * @param filename
	 * @param updateRegistry
	 * @param isNonPkbFileThatRequiresAdditionAnyway
	 * @return
	 */
	private PlSqlPackage addPackage(String schemaName, IPath schemaLocation,
			String packageName, String filename, ParseType parseType,
			boolean updateRegistry,
			boolean isNonPkbFileThatRequiresAdditionAnyway)
	{
		PlSqlSchema schema = loadSchemaLocation(schemaName, schemaLocation);
		PlSqlPackage pkg = schema.getPackage(packageName);

		if (pkg != null)
		{
			pkg.addSource(new Source(new Path(filename), PersistenceType.File),
					parseType);
		}
		else if (parseType == ParseType.Package_Body
				|| isNonPkbFileThatRequiresAdditionAnyway)
		{
			pkg = new PlSqlPackage(schema, packageName, new Source(new Path(
					filename), PersistenceType.File), parseType);
			schema.addPackage(pkg);
		}
		addFilenametoPackageMap(filename, pkg);

		if (updateRegistry)
		{
			List<String> locations = new ArrayList<String>();
			Source[] sources = schema.getSources();
			for (int i = 0; i < sources.length; i++)
			{
				Source src = sources[i];
				if (src != null && src.getSource() != null)
				{
					locations.add(src.getSource().toString());
				}
			}
			SchemaDetails sd = new SchemaDetails(schemaName, locations, "");
			SchemaDetails[] all = myRegistry.getSchemaMappings();
			List<SchemaDetails> list = new ArrayList<SchemaDetails>();
			int index = -1;
			for (int j = 0; j < all.length; j++)
			{
				SchemaDetails sdi = all[j];
				list.add(sdi);
				PlSqlSchema oldSchema = getSchema(sdi.getName());
				if (sdi.getName().equals(sd.getName()))
				{
					index = j;
				}
				// else
				// {
				// sources = oldSchema.getSources();
				// for (int i = 0; i < sources.length; i++)
				// {
				// Source src = sources[i];
				// if (src != null && src.getSource() != null)
				// {
				// sdi.addLocation(src.getSource().toString());
				// }
				// }
				// }
				PackageDetails[] pds = sdi.getPackages();

				// remove any packages that have been removed by changeSchemaFor
				for (int i = 0; i < pds.length; i++)
				{
					PackageDetails pd = pds[i];
					String pName = pd.getName();
					if (oldSchema.getPackage(pName) == null)
					{
						sdi.removePackage(pd);
					}
				}
			}
			if (index != -1)
			{
				sd = list.get(index);
				for (Iterator<String> it = locations.iterator(); it.hasNext();)
				{
					String location = it.next();
					sd.addLocation(location);
				}
			}
			else
			{
				list.add(sd);
				myRegistry.setSchemaMappings(list
						.toArray(new SchemaDetails[list.size()]));
			}
			if (sd.addPackage(new PackageDetails(packageName, filename,
					parseType)))
			{
				myRegistry.setUpdated(true);
			}
		}
		return pkg;
	}

	/**
	 * This method adds a filename to package mapping. If the filename is a
	 * package body filename, the corresponding header filename mapping is
	 * provided too.
	 * 
	 * This is a fix for Bug 1418292 - Cannot load pkh file that is not in
	 * default directory
	 * 
	 * @param filename
	 *            The name of the file to add to the mapping from.
	 * 
	 * @param pkg
	 *            The package to add the mapping to.
	 */
	private void addFilenametoPackageMap(String filename, PlSqlPackage pkg)
	{
		PlSqlPackage previous = myFileNameToPackageMap.remove(filename);
		if (previous != null)
		{
			// do nothing
		}
		myFileNameToPackageMap.put(filename, pkg);
		if (filename.endsWith(".pkb"))
		{
			String headerName = filename.replaceFirst("\\.pkb", ".pkh");
			previous = myFileNameToPackageMap.remove(headerName);
			if (previous != null)
			{
				// do nothing atm
			}
			myFileNameToPackageMap.put(headerName, pkg);
		}
	}

	/**
	 * This method finds or creates a particular schema identified by the
	 * <code>schemaName</code> and adds the specified
	 * <code>schemaLocation</code> to its list of sources.
	 * 
	 * @param schemaName
	 *            The name of the schema under which to store any loaded
	 *            packages.
	 * 
	 * @param schemaLocation
	 *            The location to look up new package files.
	 * 
	 * @return The schema that has had extra packages loaded into it.
	 */
	private PlSqlSchema loadSchemaLocation(String schemaName,
			IPath schemaLocation)
	{
		PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
		Source src = null;
		if (schemaLocation != null)
		{
			src = new Source(schemaLocation, PersistenceType.File);
		}
		if (schema == null)
		{
			schema = new PlSqlSchema(schemaName, src);
			mySchemaNameToSchemaMap.put(schema.getName(), schema);
		}
		else if (src != null)
		{
			schema.addSource(src);
		}
		return schema;
	}

	public List<String> getPackages(String schemaName,
			boolean isExpectingPublicSchemas)
	{
		PlSqlSchema schema = mySchemaNameToSchemaMap.get(schemaName);
		List<String> list = new ArrayList<String>();
		if (schema != null)
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = PlsqleditorPlugin.getDefault().getProject();
			Source[] sources = schema.getSources();
			for (int i = 0; i < sources.length; i++)
			{
				Source source = sources[i];
				IPath schemaLocation = source.getSource();
				if (schemaLocation != null)
				{
					if (project.exists(schemaLocation))
					{
						IPath fullPath = project.getLocation().append(
								schemaLocation);
						IContainer[] containers = root
								.findContainersForLocation(fullPath);
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
						System.out.println("Path " + schemaLocation
								+ " does not exist.");
					}
				}
			}
			for (Iterator<PlSqlPackage> it = schema.getPackages().values()
					.iterator(); it.hasNext();)
			{
				PlSqlPackage pkg = it.next();
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
				SortedSet extraPackages = myDbPackageStore.getPackages(
						schemaName, isExpectingPublicSchemas);
				for (Iterator it = extraPackages.iterator(); it.hasNext();)
				{
					String pkg = (String) it.next();
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
		for (int i = 0; i < resources.length; i++)
		{
			IResource file = resources[i];
			if (file instanceof IFile)
			{
				IFile ifile = (IFile) file;
				ParseType type = PlSqlParserManager.getType(ifile);
				if (type != null
						&& (type == ParseType.Package
								|| type == ParseType.Package_Body || type == ParseType.Package_Header_And_Body))
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
	 * @param file
	 *            The file whose appropriate document is sought.
	 * 
	 * @return The document for the given <code>file</code>.
	 */
	public static IDocument getDoc(IFile file)
	{
		IDocument doc = new Document();
		try
		{
			StringBuffer sb = getFileAsString(file);
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

	private static StringBuffer getFileAsString(IFile file)
			throws CoreException, IOException
	{
		String separator = System.getProperty("line.separator");
		BufferedReader br = new BufferedReader(new InputStreamReader(file
				.getContents()));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append(separator);
		}
		return sb;
	}

	/**
	 * This method gets the schema for a supplied file, as long as the file is
	 * stored in the schema mapping.
	 * 
	 * @param file
	 * @return The name of the schema associated to the file, or null if there
	 *         is no mapping.
	 */
	public String getSchemaForFileByFileToPackageMap(IFile file)
	{
		if (file == null)
		{
			return null;
		}
		PlSqlPackage pkg = myFileNameToPackageMap.get(file.getName());
		if (pkg != null)
		{
			return pkg.getSchema().getName();
		}
		return getSchemaNameFromInsideFile(file);
	}

	/**
	 * This method gets the schema name from inside the file.
	 * 
	 * @param file
	 * @return the documented schema name, or null if it is not present.
	 */
	private String getSchemaNameFromInsideFile(IFile file)
	{
		String toReturn = null;
		try
		{
			String fileAsString = getFileAsString(file).toString();
			toReturn = getSchemaFromString(fileAsString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return toReturn;
	}

	private String getSchemaFromString(String fileAsString)
	{
		Pattern p = Pattern.compile("@schema\\W+(\\w+)");
		Matcher m = p.matcher(fileAsString);
		if (m.find())
		{
			return m.group(1);
		}
		return null;
	}

	/**
	 * This method sets the schema name inside the file. N.B. It assumes the @schema
	 * tag is present.
	 * 
	 * @param file
	 * @return the documented schema name, or null if it is not present.
	 */
	private void writeSchemaNameToFile(IFile file, String schemaName)
	{
		try
		{
			Pattern p = Pattern.compile("(@schema\\W+)(\\w+)");
			Matcher m = p.matcher(getFileAsString(file).toString());
			if (m.find())
			{
				String newDoc = m.replaceFirst("$1" + schemaName);
				InputStream is = new ByteArrayInputStream(newDoc.getBytes());
				file.setContents(is, true, true, null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void loadPackageFile(IFile file, IDocument doc, boolean force,
			boolean updateRegistry)
	{
		IPath schemaLocation = file.getProjectRelativePath()
				.removeLastSegments(1);
		String filename = file.getName();
		PlSqlPackage pkg = myFileNameToPackageMap.get(filename);
		List<Segment> segments = pkg == null ? null : pkg.getSegments();

		ParseType parseType = PlSqlParserManager.getType(file);

		// fix for 1441828 - Package constants not shown in completion proposals
		boolean isTypeAlreadyStored = true;
		if (pkg != null)
		{
			isTypeAlreadyStored = pkg.isTypeStored(parseType);
		}
		if (segments == null || segments.size() == 0 || force
				|| !isTypeAlreadyStored)
		{
			if (segments != null && segments.size() > 0 && isTypeAlreadyStored)
			{
				long timeStamp = file.getModificationStamp();
				if (pkg.getLatestChangeForType(parseType) >= timeStamp)
				{
					setCurrentSchema(pkg.getSchema().getName());
					return;
				}
			}
			String schemaName = null;
			String packageName = null;
			if (pkg == null)
			{
				String schemaPackageDelimiter = getSchemaPackageDelimiter(file);
				String extensionlessFilename = stripExtension(filename);
				if (extensionlessFilename.indexOf(schemaPackageDelimiter) != -1)
				{
					int schemaPackageDelimiterIndex = extensionlessFilename
							.indexOf(schemaPackageDelimiter);
					schemaName = extensionlessFilename.substring(0,
							schemaPackageDelimiterIndex);
					packageName = extensionlessFilename
							.substring(schemaPackageDelimiterIndex + 1);
				}
				System.out.println("Doc is " + doc);
				if (doc == null)
				{
					doc = getDoc(file);
				}
				String tmpSch = getSchemaFromString(doc.get());
				if (tmpSch != null)
				{
					schemaName = tmpSch;
				}
			}
			else
			{
				schemaName = pkg.getSchema().getName();
				packageName = pkg.getName();
			}
			if (schemaName != null)
			{
				pkg = addPackage(schemaName, schemaLocation, packageName,
						filename, parseType, true, false);
				String oldSchemaName = myCurrentSchemaName;

				setCurrentSchema(schemaName);
				try
				{
					String[] discoveredPackageName = new String[1];
					if (doc == null)
					{
						doc = getDoc(file);
					}
					segments = myParser.parseFile(parseType, doc,
							discoveredPackageName);
					addFilenametoPackageMap(filename, pkg);
					// TODO should check discoveredPackageName[0] is same as
					// packageName
					storeSegments(getCurrentSchema(), file, packageName,
							segments);
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
				if (updateRegistry)
				{
					myRegistry.saveSchemaMappings(false);
				}
			}
		}
	}

	/**
	 * This method gets the schema name from the list of segments loaded from a
	 * particular (pl)sql file. The schema name will be in the contained
	 * PackageSegment in the list if the header of the file contained an @schema
	 * annotation that indicated the schema.
	 * 
	 * @param segments
	 *            The segments to search through for the schema name.
	 * 
	 * @return The schema name located in the segements, if one is there.
	 */
	private String getSchemaNameFromSegments(List segments)
	{
		for (Iterator iter = segments.iterator(); iter.hasNext();)
		{
			Segment segment = (Segment) iter.next();
			if (segment instanceof PackageSegment)
			{
				PackageSegment ps = (PackageSegment) segment;
				return ps.getSchemaName();
			}
		}
		return null;
	}

	/**
	 * This method changes the schema for a particular file (or sets it if there
	 * is currently no schema).
	 * 
	 * @param filename
	 * @param newSchemaName
	 * @param newPackageName
	 */
	public void changeSchemaFor(IFile file, IDocument doc,
			String newSchemaName, String newPackageName)
	{
		String schemaName = getCurrentSchema();
		PlSqlSchema schema = getSchema(schemaName);

		String filename = file.getName();
		PlSqlPackage pkg = myFileNameToPackageMap.remove(filename);
		schema.removePackage(pkg);

		// the pkh file will have been added too
		PlSqlPackage headerPkg = myFileNameToPackageMap.remove(filename
				.replaceFirst("\\.pkb", ".pkh"));
		if (headerPkg != null)
		{
			schema.removePackage(headerPkg);
		}
		if (schema.size() == 0)
		{
			mySchemaNameToSchemaMap.remove(schemaName);
			SchemaDetails[] details = myRegistry.getSchemaMappings();
			List<SchemaDetails> l = new ArrayList<SchemaDetails>();
			for (int i = 0; i < details.length; i++)
			{
				SchemaDetails detail = details[i];
				if (!detail.getName().equals(schemaName))
				{
					l.add(detail);
				}
			}
			details = l.toArray(new SchemaDetails[l.size()]);
			myRegistry.setSchemaMappings(details);
		}

		IPath schemaLocation = file.getProjectRelativePath()
				.removeLastSegments(1);
		Source newSource = new Source(schemaLocation, PersistenceType.File);

		PlSqlSchema newSchema = getSchema(newSchemaName);
		if (newSchema == null)
		{
			newSchema = new PlSqlSchema(newSchemaName, newSource);
			mySchemaNameToSchemaMap.put(newSchemaName, newSchema);
		}
		else
		{
			newSchema.addSource(newSource);
		}

		ParseType parseType = PlSqlParserManager.getType(file);

		pkg = new PlSqlPackage(newSchema, newPackageName, new Source(new Path(
				filename), PersistenceType.File), parseType);
		newSchema.addPackage(pkg);
		addFilenametoPackageMap(filename, pkg);
		setCurrentSchema(newSchemaName);
		myRegistry.setUpdated(true);
		if (getSchemaNameFromInsideFile(file) != null)
		{
			writeSchemaNameToFile(file, newSchemaName);
		}
		loadPackageFile(file, doc, true, true);
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
	 * @return The name of the schema for the currently focussed file.
	 */
	public String getCurrentSchema()
	{
		return myCurrentSchemaName;
	}

	/**
	 * @param schema
	 * @param packageName
	 * @return the file for the associated <code>schema</code> and
	 *         <code>package</code>, or null if it cannot be found.
	 */
	public IFile getFile(String schema, String packageName)
	{
		PlSqlSchema schm = mySchemaNameToSchemaMap.get(schema);
		if (schm != null)
		{
			PlSqlPackage pkg = schm.getPackage(packageName);
			if (pkg != null)
			{
				String filename = pkg.getPrimarySource().getSource().toString();
				Source[] sources = schm.getSources();
				for (int i = 0; i < sources.length; i++)
				{
					Source source = sources[i];
					IPath schemaLocation = source.getSource();
					IProject project = PlsqleditorPlugin.getDefault()
							.getProject();
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
	 * This method gets all the files associated with a given package.
	 * 
	 * @param schema
	 *            The schema in which the package is located.
	 * 
	 * @param packageName
	 *            The name of the package whose associate files are sought.
	 * 
	 * @return the files for the associated <code>schema</code> and
	 *         <code>package</code>, or null if none can be found.
	 */
	public IFile[] getFiles(String schema, String packageName)
	{
		Map<ParseType, IFile> fileMap = new HashMap<ParseType, IFile>();
		List<IFile> toReturn = new ArrayList<IFile>();
		IProject project = PlsqleditorPlugin.getDefault().getProject();
		PlSqlSchema schm = mySchemaNameToSchemaMap.get(schema);
		if (schm != null)
		{
			PlSqlPackage pkg = schm.getPackage(packageName);
			if (pkg != null)
			{
				Source[] schemaSources = schm.getSources();
				for (int i = 0; i < schemaSources.length; i++)
				{
					Source schemaSource = schemaSources[i];
					IPath schemaLocation = schemaSource.getSource();
					addFileToMap(ParseType.Package_Body, pkg, schemaLocation,
							project, fileMap, toReturn);
					addFileToMap(ParseType.Package, pkg, schemaLocation,
							project, fileMap, toReturn);
				}
			}
		}
		return toReturn.toArray(new IFile[toReturn.size()]);
	}

	private void addFileToMap(ParseType parseType, PlSqlPackage pkg,
			IPath schemaLocation, IProject project,
			Map<ParseType, IFile> toCheck, List<IFile> toAddTo)
	{
		if (!toCheck.containsKey(parseType))
		{
			Source pkgSource = pkg.getSource(parseType);
			if (pkgSource != null)
			{
				String pkgFilename = pkgSource.getSource().toString();
				IPath fullpath = schemaLocation.append(pkgFilename);
				if (project.exists(fullpath))
				{
					IFile file = project.getFile(fullpath);
					toCheck.put(parseType, file);
					toAddTo.add(file);
				}
			}
		}
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
		initDbPackageStore(myProject);
	}

	/**
	 * This method gets the source code associated to the object with the
	 * provided <code>sourceName</code> of the supplied <code>type</code> in the
	 * schema with the provided <code>schemaName</code>.
	 * 
	 * @param schemaName
	 *            The name of the schema that the source is located in or can at
	 *            least be found from (i.e. the source is public).
	 * @param sourceName
	 *            The name of the source.
	 * @param type
	 *            The type of the source. For more details see
	 *            {@link DBPackageStore#getSource(String, String, String)}.
	 *            
	 * @return The source code from the database
	 * 
	 * @throws SQLException when there is a db problem
	 * 
	 * @see plsqleditor.stores.DBPackageStore#getSource(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getSource(String schemaName, String sourceName, String type)
			throws SQLException
	{
		if (myDbPackageStore == null)
		{
			throw new IllegalStateException("The database connection is not connected correctly");
		}
		return myDbPackageStore.getSource(schemaName, sourceName, type);
	}
}
