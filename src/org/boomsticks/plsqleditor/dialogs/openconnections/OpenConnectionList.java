package org.boomsticks.plsqleditor.dialogs.openconnections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plsqleditor.db.ConnectionDetails;
import plsqleditor.db.DbUtility;
import plsqleditor.db.LoadPackageManager;


/**
 * Class that plays the role of the domain model in the TableViewerExample In
 * real life, this class would access a persistent store of some kind.
 *
 */
public class OpenConnectionList
{
    private Set<IConnectionListViewer> changeListeners = new HashSet<IConnectionListViewer>();

    /**
     * Constructor
     */
    public OpenConnectionList()
    {
        super();
    }

    /**
     * Return the collection of tasks
     */
    public List<LiveConnection> getConnections()
    {
        List<LiveConnection> toReturn = new ArrayList<LiveConnection>();
        Map<String, ConnectionDetails> specificConns = LoadPackageManager.instance()
                .getAllSpecificConnections();

        for (String filename : specificConns.keySet())
        {
            ConnectionDetails cd = specificConns.get(filename);
            LiveConnection lc = new LiveConnection(cd.getConnectString());
            lc.setFilename(filename);
            lc.setProject("Not Specified"); // TODO fix this - put project
                                            // specification into specific
                                            // filenames
            lc.setType(LiveConnection.FILE_CONNECTION);
            lc.setUser(cd.getSchemaName());
            toReturn.add(lc);
        }

        Map<String, ConnectionDetails> dbConns = DbUtility.getDbaConnectionPoolDetails();

        for (String project : dbConns.keySet())
        {
            ConnectionDetails cd = dbConns.get(project);
            LiveConnection lc = new LiveConnection(cd.getConnectString());
            lc.setFilename("Not relevant");
            lc.setProject(project);
            lc.setType(LiveConnection.DBA_CONNECTION);
            lc.setUser(cd.getSchemaName());
            toReturn.add(lc);
        }

        Map<String, ConnectionDetails> schemaConns = DbUtility.getSchemaConnectionPoolDetails();

        for (String projectDotSchema : schemaConns.keySet())
        {
            ConnectionDetails cd = schemaConns.get(projectDotSchema);

            int dotIndex = projectDotSchema.indexOf('.');
            String projectName = projectDotSchema.substring(0, dotIndex);

            LiveConnection lc = new LiveConnection(cd.getConnectString());
            lc.setFilename("Not relevant");
            lc.setProject(projectName);
            lc.setType(LiveConnection.SCHEMA_CONNECTION);
            lc.setUser(cd.getSchemaName());
            toReturn.add(lc);
        }
        return toReturn;
    }

    /**
     * @param conn
     */
    public void removeConnection(LiveConnection conn)
    {
        if (conn.getType().equals(LiveConnection.DBA_CONNECTION))
        {
            DbUtility.closeDbaConnection(conn.getProject());
        }
        else if (conn.getType().equals(LiveConnection.FILE_CONNECTION))
        {
            LoadPackageManager.instance().removeFixedConnection(conn.getFilename());
        }
        else if (conn.getType().equals(LiveConnection.SCHEMA_CONNECTION))
        {
            DbUtility.closeSchemaConnection(conn.getProject(), conn.getUser());
        }
        else
        {
            throw new IllegalStateException("The LiveConnection [" + conn
                    + "] is neither DBA, FILE or SCHEMA based");
        }
        for (IConnectionListViewer viewer : changeListeners)
        {
            viewer.removeConnection(conn);
        }
    }

    /**
     * @param conn
     */
    public void connChanged(LiveConnection conn)
    {
        for (IConnectionListViewer viewer : changeListeners)
        {
            viewer.updateConnection(conn);
        }
    }

    /**
     * @param viewer
     */
    public void removeChangeListener(IConnectionListViewer viewer)
    {
        changeListeners.remove(viewer);
    }

    /**
     * @param viewer
     */
    public void addChangeListener(IConnectionListViewer viewer)
    {
        changeListeners.add(viewer);
    }

}
