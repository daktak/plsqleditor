/**
 * 
 */
package plsqleditor.views.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.AbstractPlSqlParser;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.PackageStore;
import plsqleditor.stores.TableStore;
import au.com.gts.data.Column;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;

public class SchemaBrowserContentProvider
        implements
            IStructuredContentProvider,
            ITreeContentProvider
{
    /**
     * 
     */
    Viewer          myViewer;

    private HashMap myOpenDatabasePackageActions = new HashMap();
    private HashMap myShowGrantsAction           = new HashMap();
    private Job     runner;
    static Object   synchroniser                 = new Object();
    static Boolean  theIsAlreadyRunning          = Boolean.FALSE;

    static class TreeObject implements IAdaptable
    {
        private String     name;
        private TreeParent parent;
        private String     type;
        private IFile      file;
        private Object     myObject;

        public TreeObject(String name, String type, IFile associatedFile)
        {
            this.name = name;
            this.type = type;
            this.file = associatedFile;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public void setParent(TreeParent parent)
        {
            this.parent = parent;
        }

        public TreeParent getParent()
        {
            return parent;
        }

        public String toString()
        {
            if (myObject != null)
            {
                return myObject.toString();
            }
            return getName();
        }

        public Object getAdapter(Class key)
        {
            return null;
        }

        protected IFile getFile()
        {
            return file;
        }

        public String getType()
        {
            return type;
        }

        protected Object getObject()
        {
            return myObject;
        }

        protected void setObject(Object object)
        {
            myObject = object;
        }

        protected String getNameForType(String typeName)
        {
            if (type.equals(typeName))
            {
                return name;
            }
            else if (parent != null)
            {
                return ((TreeObject) parent).getNameForType(typeName);
            }
            else
            {
                return null;
            }
        }
    }

    static class TreeParent extends TreeObject
    {
        private ArrayList children;

        public TreeParent(String name, String type)
        {
            super(name, type, null);
            children = new ArrayList();
        }

        public void addChild(TreeObject child)
        {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child)
        {
            children.remove(child);
            child.setParent(null);
        }

        public TreeObject[] getChildren()
        {
            return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
        }

        public boolean hasChildren()
        {
            return children.size() > 0;
        }
    }

    /**
     * @param view
     */
    private SchemaBrowserContentProvider()
    {
        //
    }

    TreeParent                                  invisibleRoot;
    IProject                                    myCurrentProject;
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
        myViewer = v;
        refresh();
        System.out.println("inputChanged: Updated");
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
                refresh();
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
            return ((TreeParent) parent).getChildren();
        }
        return new Object[0];
    }

    public boolean hasChildren(Object parent)
    {
        if (parent instanceof TreeParent)
        {
            return ((TreeParent) parent).hasChildren();
        }
        return false;
    }


    public Action getOpenDatabasePackageAction(final Viewer viewer, final String schema)
    {
        Action openDatabasePackageAction = (Action) myOpenDatabasePackageActions.get(schema);
        if (openDatabasePackageAction == null)
        {
            openDatabasePackageAction = PackageData
                    .createOpenDatabasePackageAction(viewer, myCurrentProject, schema);
            myOpenDatabasePackageActions.put(schema, openDatabasePackageAction);
        }
        return openDatabasePackageAction;
    }

    public Action getShowGrantsAction(final Viewer viewer, final String schema)
    {
        Action showGrantsAction = (Action) myShowGrantsAction.get(schema);
        if (showGrantsAction == null)
        {
            showGrantsAction = PackageData.getShowGrantsAction(viewer, myCurrentProject, schema);
            myShowGrantsAction.put(schema, showGrantsAction);
        }
        return showGrantsAction;
    }

    /**
     * This method resets the content provider's mapped actions.
     */
    void resetViewer()
    {
        myShowGrantsAction.clear();
        myOpenDatabasePackageActions.clear();
    }

    /*
     * 
     */
    public void refresh()
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
                    if (myCurrentProject == null || !myCurrentProject.equals(project))
                    {
                        resetViewer();
                        if (invisibleRoot != null)
                        {
                            invisibleRoot = null;
                        }
                        TableStore tstore = plugin.getTableStore(project);
                        PackageStore pstore = plugin.getPackageStore(project);

                        TreeParent schemas = new TreeParent("Schemas", "SchemaList");
                        SortedSet schemaset = pstore.getSchemas();

                        SortedSet filteredSchemas = new TreeSet();
                        String filterString = PlsqleditorPlugin.getDefault().getPreferenceStore()
                                .getString(PreferenceConstants.P_SCHEMA_BROWSER_FILTER_LIST);
                        StringTokenizer st = new StringTokenizer(filterString, ",");
                        while (st.hasMoreElements())
                        {
                            filteredSchemas.add(st.nextToken());
                        }
                        // filteredSchemas.add("\\w*SYS");
                        // filteredSchemas.add("SYSMAN");
                        // filteredSchemas.add("SYSTEM");
                        // filteredSchemas.add("XDB");
                        // filteredSchemas.add("DBSNMP");
                        // filteredSchemas.add("\\w+PLUGINS");

                        for (Iterator it = schemaset.iterator(); it.hasNext();)
                        {
                            String schema = (String) it.next();
                            TreeParent schemaTp = new TreeParent(schema, "Schema");
                            schemas.addChild(schemaTp);

                            TreeParent tablesTp = new TreeParent("Tables", "TableList");
                            Table[] tables = tstore.getTables(schema);

                            for (int i = 0; i < tables.length; i++)
                            {
                                Table table = tables[i];
                                TreeObject tableTo = new TreeObject(table.getName(), "Table", null);
                                tablesTp.addChild(tableTo);
                            }
                            schemaTp.addChild(tablesTp);

                            try
                            {
                                boolean isFiltered = false;
                                for (Iterator filterIterator = filteredSchemas.iterator(); filterIterator
                                        .hasNext();)
                                {
                                    String filter = (String) filterIterator.next();
                                    if (Pattern.matches(filter, schema.toUpperCase()))
                                    {
                                        isFiltered = true;
                                        break;
                                    }
                                }
                                if (!isFiltered)
                                {
                                    TreeParent packagesTp = new TreeParent("Packages",
                                            "PackageList");
                                    List packageList = pstore.getPackages(schema, false);
                                    for (Iterator it2 = packageList.iterator(); it2.hasNext();)
                                    {
                                        String packageName = (String) it2.next();
                                        IFile packageFile = pstore.getFile(schema, packageName);
                                        TreeParent packageTp = new PackageData(packageName);

                                        List segments = pstore.getSegments(schema, packageName);
                                        PackageSegment pkgSeg = AbstractPlSqlParser
                                                .getPackageSegment(segments, packageName);
                                        
                                        // TODO get this section to be on demand instead of at load time
                                        if (pkgSeg != null)
                                        {
                                            segments = pkgSeg.getContainedSegments();
                                        }
                                        for (Iterator it3 = segments.iterator(); it3.hasNext();)
                                        {
                                            Segment segment = (Segment) it3.next();
                                            if (segment.getType() != SegmentType.Code)
                                            {
                                                TreeObject segmentTo = new TreeObject(segment
                                                        .getName(), "Segment", packageFile);
                                                segmentTo.setObject(segment);
                                                packageTp.addChild(segmentTo);
                                            }
                                        }
                                        packagesTp.addChild(packageTp);
                                    }
                                    schemaTp.addChild(packagesTp);
                                }
                            }
                            catch (RuntimeException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        Schema[] tableSchemas = tstore.getSchemas();
                        for (int i = 0; i < tableSchemas.length; i++)
                        {
                            Schema tableSchema = tableSchemas[i];
                            String tableSchemaName = tableSchema.getName();
                            if (!schemaset.contains(tableSchemaName))
                            {
                                TreeParent schemaTp = new TreeParent(tableSchemaName, "Schema");
                                schemas.addChild(schemaTp);

                                TreeParent tablesTp = new TreeParent("Tables", "TableList");
                                Table[] tables = tstore.getTables(tableSchemaName);

                                for (int j = 0; j < tables.length; j++)
                                {
                                    Table table = tables[j];
                                    TreeParent tableTp = new TreeParent(table.getName(), "Table");
                                    List columns = table.getColumns();
                                    for (Iterator it = columns.iterator(); it.hasNext();)
                                    {
                                        Column col = (Column) it.next();
                                        TreeObject columnTo = new TreeObject(col.getName(),
                                                "Column", null);
                                        tableTp.addChild(columnTo);
                                    }
                                    tablesTp.addChild(tableTp);
                                }
                                schemaTp.addChild(tablesTp);
                            }
                        }

                        TreeParent root = new TreeParent(project.getName(), "Root");
                        root.addChild(schemas);
                        invisibleRoot = new TreeParent("", "Root");
                        invisibleRoot.addChild(root);

                        myCurrentProject = project;

                        
                        Display.getDefault().syncExec (new Runnable () {
                            public void run () {
                                   myViewer.refresh();
                            }
                         });
                    }
                }
                theIsAlreadyRunning = Boolean.FALSE;
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        runner.schedule();

    }
}