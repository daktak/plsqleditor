/**
 * 
 */
package plsqleditor.views.schema;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.ConnectionDetails;
import plsqleditor.parsers.AbstractPlSqlParser;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.TableStore;
import au.com.gts.data.Column;
import au.com.gts.data.Constraint;
import au.com.gts.data.DbType;
import au.com.gts.data.Function;
import au.com.gts.data.Procedure;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;
import au.com.gts.data.Trigger;

public class SchemaBrowserContentProvider implements
		IStructuredContentProvider, ITreeContentProvider
{
	private static final String PACKAGE_OUTLINE = "Segments";

	private static final String COLUMNS_HOLDER_TREE_OBJECT_TYPE = "Columns";

	private static final String SCHEMA_TREE_OBJECT_TYPE = "Schema";

	private static final String TABLES_HOLDER_TREE_OBJECT_TYPE = "Tables";

	/**
	 * The tree viewer viewing this content.
	 */
	TreeViewer myViewer;

	private Job runner;
	static Object synchroniser = new Object();
	static Boolean theIsAlreadyRunning = Boolean.FALSE;

	/**
	 * @param view
	 */
	private SchemaBrowserContentProvider()
	{
		//
	}

	TreeParent invisibleRoot;
	IProject myCurrentProject;

	private static SchemaBrowserContentProvider theInstance;

	/**
	 * This method gets the singleton instance.
	 * 
	 * @return the singleton instance.
	 */
	public static synchronized SchemaBrowserContentProvider getInstance()
	{
		if (theInstance == null)
		{
			theInstance = new SchemaBrowserContentProvider();
		}
		return theInstance;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		myViewer = (TreeViewer) v;
		refresh(false);
		System.out.println("inputChanged: Updated");
	}

	public void updateNode(Object node)
	{
		myViewer.refresh(node);
	}

	public void dispose()
	{
		//
	}

	public Object[] getElements(Object parent)
	{
		if (myViewer != null && parent.equals(myViewer.getInput()))
		{
			if (invisibleRoot == null)
			{
				refresh(false);
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	public Object getParent(Object child)
	{
		if (child instanceof TreeObject)
		{
			return ((TreeObject) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent)
	{
		if (parent instanceof TreeParent)
		{
			TreeParent tparent = (TreeParent) parent;
			if (tparent.getChildren().length == 0)
			{
				PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
				if (tparent.getType().equals(TABLES_HOLDER_TREE_OBJECT_TYPE))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addTablesToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals("Triggers"))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addTriggersToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals("Types"))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addTypesToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals("Procedures"))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addProcsToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals("Functions"))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addFuncsToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals("Packages"))
				{
					// this is to produce the packages under a specific schema
					PackageStore pstore = plugin
							.getPackageStore(myCurrentProject);
					addPackagesToTree(pstore, tparent.getParent().getName(),
							tparent);
				}
				else if (tparent.getType().equals(PACKAGE_OUTLINE))
				{
					// this to produce the segments under a specific package
					PackageStore pstore = plugin
							.getPackageStore(myCurrentProject);
					TreeParent packageTp = tparent.getParent();
					addSegmentsToPackage(pstore, packageTp.getParent()
							.getParent().getName(), packageTp.getName(),
							tparent);
				}
				else if (tparent.getType().equals(
						COLUMNS_HOLDER_TREE_OBJECT_TYPE))
				{
					TableStore tstore = plugin.getTableStore(myCurrentProject);
					addColumnsToTree(tstore, tparent.getParent().getName(),
							tparent);
				}
			}
			return tparent.getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent)
	{
		return getChildren(parent).length > 0;
	}

	/*
     * 
     */
	public void refresh(final boolean forceRefresh)
	{
		if (myViewer == null)
		{
			return;
		}
		final PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
		final IProject project = plugin.getProject();
		runner = new Job("Schema browser Update")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				return runRefresh(plugin, project, monitor, forceRefresh);
			}
		};
		runner.schedule();
	}

	private IStatus runRefresh(final PlsqleditorPlugin plugin,
			final IProject project, IProgressMonitor monitor,
			boolean forceRefresh)
	{
		synchronized (synchroniser)
		{
			if (theIsAlreadyRunning.booleanValue())
			{
				monitor.done();
				return Status.OK_STATUS;
			}
			theIsAlreadyRunning = Boolean.TRUE;
		}
		if (project != null)
		{
			if (myCurrentProject == null || !myCurrentProject.equals(project)
					|| forceRefresh)
			{
				if (invisibleRoot != null)
				{
					invisibleRoot = null;
				}
				TableStore tstore = plugin.getTableStore(project);
				PackageStore pstore = plugin.getPackageStore(project);

				TreeParent schemasTreeParent = new TreeParent("Schemas",
						"SchemaList");
				// get pstore schema names
				SortedSet<String> schemaset = pstore.getSchemas();
				// add tstore schema names
				for (Schema schema : tstore.getSchemas())
				{
					schemaset.add(schema.getSchemaName().toLowerCase());
				}

				// then filter some out
				filterSchemas(schemaset);

				for (String schema : schemaset)
				{
					TreeParent schemaTp = new TreeParent(schema,
							SCHEMA_TREE_OBJECT_TYPE);
					schemaTp.setObject(tstore.getSchema(schema));
					schemasTreeParent.addChild(schemaTp);

					TreeParent packagesTp = new TreeParent("Packages",
							"Packages");
					schemaTp.addChild(packagesTp);
					TreeParent triggersTp = new TreeParent("Triggers",
							"Triggers");
					schemaTp.addChild(triggersTp);
					TreeParent typesTp = new TreeParent("Types", "Types");
					schemaTp.addChild(typesTp);
					TreeParent proceduresListTp = new TreeParent("Procedures",
							"Procedures");
					schemaTp.addChild(proceduresListTp);
					TreeParent functionsListTp = new TreeParent("Functions",
							"Functions");
					schemaTp.addChild(functionsListTp);
					TreeParent tablesListTp = new TreeParent("Tables",
							TABLES_HOLDER_TREE_OBJECT_TYPE);
					schemaTp.addChild(tablesListTp);
				}

				ConnectionDetails defaultConnDetails = plugin.getDefaultDbConnectionDetails(project);
				TreeParent root = new TreeParent(project.getName() + ":" + defaultConnDetails.getSchemaName() + "@" + defaultConnDetails.getConnectString(), "Root");
				root.addChild(schemasTreeParent);
				invisibleRoot = new TreeParent("", "Root");
				invisibleRoot.addChild(root);

				myCurrentProject = project;

				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						myViewer.refresh();
					}
				});
			}
		}
		theIsAlreadyRunning = Boolean.FALSE;
		monitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * This method adds the types to the "Types" folder which resides under a
	 * specific Schema folder.
	 * 
	 * @param store
	 *            The store which contains references to the types.
	 * @param schemaName
	 *            The name of the schema whose types will be added.
	 * @param typesTp
	 *            The "Types" node.
	 */
	private void addTypesToTree(TableStore store, String schemaName,
			TreeParent typesTp)
	{
		Schema schema = store.getSchema(schemaName);
		if (schema != null)
		{
			for (DbType type : schema.getDatabaseTypes().values())
			{
				TreeParent typeTp = new TreeParent(type.getName(), "Type");
				typeTp.setObject(type);
				typesTp.addChild(typeTp);
			}
		}
	}

	/**
	 * This method adds the procedures to the "Procedures" folder which resides
	 * under a specific Schema folder.
	 * 
	 * @param store
	 *            The store which contains references to the procedures.
	 * @param schemaName
	 *            The name of the schema whose procedures will be added.
	 * @param procsTp
	 *            The "Procedures" node.
	 */
	private void addProcsToTree(TableStore store, String schemaName,
			TreeParent procsTp)
	{
		Schema schema = store.getSchema(schemaName);
		if (schema != null)
		{
			for (Procedure proc : schema.getProcedures().values())
			{
				TreeParent procTp = new TreeParent(proc.getName(), "Procedure");
				procTp.setObject(proc);
				procsTp.addChild(procTp);
			}
		}
	}

	/**
	 * This method adds the functions to the "Functions" folder which resides
	 * under a specific Schema folder.
	 * 
	 * @param store
	 *            The store which contains references to the functions.
	 * @param schemaName
	 *            The name of the schema whose functions will be added.
	 * @param funcsTp
	 *            The "Functions" node.
	 */
	private void addFuncsToTree(TableStore store, String schemaName,
			TreeParent funcsTp)
	{
		Schema schema = store.getSchema(schemaName);
		if (schema != null)
		{
			for (Function func : schema.getFunctions().values())
			{
				TreeParent funcTp = new TreeParent(func.getName(), "Function");
				funcTp.setObject(func);
				funcsTp.addChild(funcTp);
			}
		}
	}

	/**
	 * This method adds the triggers to the "Triggers" folder which resides
	 * under a specific Schema folder.
	 * 
	 * @param store
	 *            The store which contains references to the triggers.
	 * @param schemaName
	 *            The name of the schema whose triggers will be added.
	 * @param triggersTp
	 *            The "Triggers" node.
	 */
	private void addTriggersToTree(TableStore store, String schemaName,
			TreeParent triggersTp)
	{
		Schema schema = store.getSchema(schemaName);
		if (schema != null)
		{
			for (Trigger trigger : schema.getTriggers().values())
			{
				TreeParent triggerTp = new TreeParent(trigger.getName(),
						"Trigger");
				triggerTp.setObject(trigger);
				triggersTp.addChild(triggerTp);
			}
		}
	}

	/**
	 * This method adds the tables to the "Tables" folder which resides under a
	 * specific Schema folder.
	 * 
	 * @param tstore
	 *            The table store which contains references to the tables.
	 * @param schema
	 *            The name of the schema whose tables will be added.
	 * @param tablesTp
	 *            The "Tables" node.
	 */
	private void addTablesToTree(TableStore tstore, String schema,
			TreeParent tablesTp)
	{
		Table[] tables = tstore.getTables(schema);
		for (int j = 0; j < tables.length; j++)
		{
			Table table = tables[j];
			TreeParent tableTp = new TreeParent(table.getName(), "Table");
			TreeParent columnsTp = new TreeParent("Columns",
					COLUMNS_HOLDER_TREE_OBJECT_TYPE);
			tableTp.addChild(columnsTp);
			tableTp.setObject(table);
			tablesTp.addChild(tableTp);
		}
	}

	/**
	 * This method adds the columns to the "Columns" folder which resides under
	 * a specific Table folder.
	 * 
	 * @param tstore
	 *            The table store which contains references to the tables and
	 *            columns.
	 * @param tableName
	 *            The name of the table whose columns will be added.
	 * @param columnsTp
	 *            The "Columns" node.
	 */
	private void addColumnsToTree(TableStore tstore, String tableName,
			TreeParent columnsTp)
	{
		String schemaName = columnsTp.getParent().getParent().getParent()
				.getName();
		Table table = tstore.getTable(schemaName, tableName);
		List<Column> columns = table.getColumns();
		for (Column col : columns)
		{
			TreeParent columnTo = new TreeParent(col.getName(), "Column");
			columnTo.setObject(col);
			columnsTp.addChild(columnTo);
			Collection<? extends Constraint> constraints = col.getConstraints();
			if (constraints.size() > 0)
			{
				for (Constraint constraint : constraints)
				{
					TreeObject constraintTreeObj = new TreeObject(constraint
							.getName(), "Constraint", null);
					constraintTreeObj.setObject(constraint);
					columnTo.addChild(constraintTreeObj);
				}
			}
		}
	}

	/**
	 * This method adds all the packages from one schema to the Packages tree
	 * parent under that schema.
	 * 
	 * @param pstore
	 *            The package store to retrieve the packages from.
	 * @param schema
	 *            The schema name
	 * @param packagesTp
	 *            The packages tree parent to add the packages to.
	 */
	private void addPackagesToTree(PackageStore pstore, String schema,
			TreeParent packagesTp)
	{
		try
		{
			List<String> packageList = pstore.getPackages(schema, false);
			for (String packageName : packageList)
			{
				TreeParent packageTp = new PackageData(packageName);
				TreeParent segmentsTp = new TreeParent("Package Outline",
						PACKAGE_OUTLINE);
				packageTp.addChild(segmentsTp);
				packagesTp.addChild(packageTp);
			}
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This method adds all the segments of a particular package to the segments
	 * tree parent under that package.
	 * 
	 * @param pstore
	 *            The package store to retrieve the segments from.
	 * @param schema
	 *            The schema name
	 * @param packageName
	 *            The package name.
	 * @param segmentsTp
	 *            The segments tree parent to put them under.
	 */
	private void addSegmentsToPackage(PackageStore pstore, String schema,
			String packageName, TreeParent segmentsTp)
	{
		List<Segment> segments = pstore.getSegments(schema, packageName);
		IFile packageFile = pstore.getFile(schema, packageName);
		PackageSegment pkgSeg = AbstractPlSqlParser.getPackageSegment(segments,
				packageName);

		if (pkgSeg != null)
		{
			segments = pkgSeg.getContainedSegments();
		}

		for (Object object : segments)
		{
			Segment segment = (Segment) object;
			if (segment.getType() != SegmentType.Code)
			{
				TreeObject segmentTo = new TreeObject(segment.getName(),
						"Segment", packageFile);
				segmentTo.setObject(segment);
				segmentsTp.addChild(segmentTo);
			}
		}
	}

	/**
	 * This removes the schemas that have been excluded by regular expression in
	 * the preferences.
	 * 
	 * @param schemaset
	 *            The full list of schemas to put in the table.
	 */
	private void filterSchemas(SortedSet<String> schemaset)
	{
		String filterString = PlsqleditorPlugin.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.P_SCHEMA_BROWSER_FILTER_LIST);
		StringTokenizer st = new StringTokenizer(filterString, ",");
		while (st.hasMoreElements())
		{
			String filter = st.nextToken();
			for (String schema : schemaset)
			{
				if (Pattern.matches(filter, schema.toUpperCase()))
				{
					schemaset.remove(schema);
					break;
				}
			}
		}
	}
}