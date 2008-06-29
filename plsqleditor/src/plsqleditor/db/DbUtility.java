package plsqleditor.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;

/**
 * @author zinest
 * 
 */
public class DbUtility
{
    public static String            DOT                   = ".";
    private static Map              theDbaConnectionPools = new HashMap();
    private static IPreferenceStore thePrefs;

    private static Map              myConnectionPools     = new HashMap();

    /**
     * This is a list of the {@link DbPrefsUpdateListener}s that listen for changes to these settings.
     */
    private static List             myListeners           = new ArrayList();

    /**
     * This field is the list of single connections that are used by the {@link #loadCode(String, String)} calls to load
     * piece of code to the database.
     */
    private static Map              mySchemaConnections   = new HashMap();

    /**
     * This field is the map of dbms output wrappers stored against the name of the schema to which they are attached.
     */
    private static Map              myDbmsOutputs         = new HashMap();

    /**
     * This class maintains information about a Connection, such as whether commits are pending on it or not.
     * Introduced for Feature Request 1415152 - Stop database commit messages 
     * @author Toby Zines
     */
    static class ConnectionContainer
    {
        public Connection connection;
        public boolean isCommitPending = false;
        public ConnectionContainer(Connection conn)
        {
            connection = conn;
        }
    }
    
    public interface DbPrefsUpdateListener
    {
        public void dbPrefsUpdated();
    }

    public static void addListener(DbPrefsUpdateListener l)
    {
        myListeners.add(l);
    }

    /**
     * This method updates the registry listeners.
     */
    private static void updateListeners()
    {
        for (Iterator it = myListeners.iterator(); it.hasNext();)
        {
            DbPrefsUpdateListener l = (DbPrefsUpdateListener) it.next();
            l.dbPrefsUpdated();
        }
    }

    public static void init(IPreferenceStore prefs)
    {
        thePrefs = prefs;
    }

    /**
     * This method initialises the connection pool being used to talk to the database.
     */
    private static ConnectionPool initDbaConnectionPool(IProject project)
    {
        ConnectionPool dbaConnPool = (ConnectionPool) theDbaConnectionPools.get(project);

        if (dbaConnPool == null)
        {
            if (thePrefs == null)
            {
                throw new IllegalStateException("Prefs have not been set");
            }
            try
            {
                theDbaConnectionPools.put(project, null); // just to store project
                String driver = null;
                String url = null;
                String user = null;
                String passwd = null;
                int initConns = -1;
                int maxConns = -1;
                boolean isAutoCommitOnClose = false;
                try
                {
                    String usingProjectSpecific = project.getPersistentProperty(new QualifiedName("",
                            PreferenceConstants.USE_LOCAL_DB_SETTINGS));
                    if (Boolean.valueOf(usingProjectSpecific).booleanValue())
                    {
                        driver = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_DRIVER));
                        url = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_URL));
                        user = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_USER));
                        passwd = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_PASSWORD));
                        initConns = Integer.parseInt(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_INIT_CONNS)));
                        maxConns = Integer.parseInt(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_MAX_CONNS)));
                        isAutoCommitOnClose = Boolean.valueOf(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE))).booleanValue();
                    }
                    else
                    {
                        isAutoCommitOnClose = thePrefs.getBoolean(PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE);
                    }
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }
                if (driver == null)
                {
                    driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
                }
                if (url == null)
                {
                    url = thePrefs.getString(PreferenceConstants.P_URL);
                }
                if (user == null)
                {
                    user = thePrefs.getString(PreferenceConstants.P_USER);
                }
                if (passwd == null)
                {
                    passwd = thePrefs.getString(PreferenceConstants.P_PASSWORD);
                }
                if (initConns <= 0)
                {
                    initConns = thePrefs.getInt(PreferenceConstants.P_INIT_CONNS);
                }
                if (maxConns <= 0)
                {
                    maxConns = thePrefs.getInt(PreferenceConstants.P_MAX_CONNS);
                }
                if (passwd == null)
                {
                    throw new IllegalStateException("Password for dba connection for " + project.getName()
                            + " is missing");
                }

                if (driver == null || driver.trim().length() == 0)
                {
                    throw new IllegalStateException("The driver class has not been selected");
                }
                if (url == null || url.trim().length() == 0)
                {
                    throw new IllegalStateException("The url has not been selected");
                }
                if (user == null || user.trim().length() == 0)
                {
                    throw new IllegalStateException("The user has not been defined");
                }
                if (passwd == null || passwd.trim().length() == 0)
                {
                    throw new IllegalStateException("The password has not been defined");
                }
                if (initConns == 0)
                {
                    throw new IllegalStateException("initial number of connections is invalid");
                }
                if (maxConns == 0)
                {
                    throw new IllegalStateException("maximum number of connections is invalid");
                }

                dbaConnPool = new ConnectionPool(driver, url, user, passwd, initConns, maxConns, false);
                dbaConnPool.setAutoCommitting(isAutoCommitOnClose);
                theDbaConnectionPools.put(project, dbaConnPool);
            }
            catch (SQLException e)
            {
                String msg = "Failed to initialise connection pool: " + e;
                System.out.println(msg);
                
                // fix for 1437124 - Techie error message when password not supplied
                String connType = "DBA connection for project [" + project + "]";
                String propsLocation = "project properties or dba preferences.";
                checkBadUserOrPwd(msg, connType, propsLocation);
                throw new IllegalStateException(msg);
            }
        }
        return dbaConnPool;
    }

    private static void checkBadUserOrPwd(String msg, String connType, String propsLocation)
    {
        // fix for 1437124 - Techie error message when password not supplied
        if (msg.indexOf("ull user or password") != -1)
        {
            throw new IllegalStateException("Either the username or password is not set for " + connType +
                                            ". Please supply a password for the " + connType +
                                            " in the " + propsLocation);
        }
    }

    /**
     * This method initialises the connection pool being used to talk to the database.
     * 
     * @param project The project whose schema connection pool is being initialised.
     * 
     * @param schema The name of the schema.
     */
    private static void initConnectionPool(IProject project, String schema)
    {
        if (thePrefs == null)
        {
            throw new IllegalStateException("Prefs have not been set");
        }
        String schemaIdentifier = project.getName() + DOT + schema;
        try
        {
            ConnectionPool pool = (ConnectionPool) myConnectionPools.get(schemaIdentifier);
            if (pool == null)
            {
                String driver = null;
                String url = null;
                int initConns = -1;
                int maxConns = -1;
                String user = schema;
                String passwd = PlsqleditorPlugin.getDefault().getSchemaRegistry(project).getPasswordForSchema(schema);
                boolean isAutoCommitOnClose = false;
                try
                {
                    String usingProjectSpecific = project.getPersistentProperty(new QualifiedName("",
                            PreferenceConstants.USE_LOCAL_DB_SETTINGS));
                    if (Boolean.valueOf(usingProjectSpecific).booleanValue())
                    {
                        driver = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_DRIVER));
                        url = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_URL));
                        initConns = Integer.parseInt(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_INIT_CONNS)));
                        maxConns = Integer.parseInt(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_MAX_CONNS)));
                        isAutoCommitOnClose = Boolean.valueOf(project.getPersistentProperty(new QualifiedName("",
                                PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE))).booleanValue();
                    }
                    else
                    {
                        isAutoCommitOnClose = thePrefs.getBoolean(PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE);
                    }
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }
                if (driver == null)
                {
                    driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
                }
                if (url == null)
                {
                    url = thePrefs.getString(PreferenceConstants.P_URL);
                }
                if (initConns <= 0)
                {
                    initConns = thePrefs.getInt(PreferenceConstants.P_INIT_CONNS);
                }
                if (maxConns <= initConns)
                {
                    maxConns = thePrefs.getInt(PreferenceConstants.P_MAX_CONNS);
                }
                if (passwd == null)
                {
                    throw new IllegalStateException("Password for schema " + schema + " is missing");
                }

                ConnectionPool cp = new ConnectionPool(driver, url, user, passwd, initConns, maxConns, false);
                cp.setAutoCommitting(isAutoCommitOnClose);
                myConnectionPools.put(schemaIdentifier, cp);
            }
        }
        catch (SQLException e)
        {
            String msg = "Failed to initialise connection pool for [" + schemaIdentifier + ": " + e;
            String connType = "database connection for schema [" + schema + "]";
            String propsLocation = "project properties.";
            // fix for 1437124 - Techie error message when password not supplied
            checkBadUserOrPwd(msg, connType, propsLocation);
            System.out.println(msg);
            throw new IllegalStateException(msg);
        }
    }


    public static ConnectionPool getDbaConnectionPool(IProject project)
    {
        return initDbaConnectionPool(project);
    }

    /**
     * @param schemaName
     * @param c
     */
    protected static void free(IResource resource, String schemaName, Connection c)
    {
        String cpName = resource.getProject().getName() + DOT + schemaName;
        
        ConnectionPool cp = (ConnectionPool) myConnectionPools.get(cpName);
        if (cp != null)
        {
            cp.free(c);
        }
    }

    protected static Connection getTempConnection(IProject project, String schema) throws SQLException
    {
        initConnectionPool(project, schema);
        String cpName = project.getName() + DOT + schema;
        return ((ConnectionPool) myConnectionPools.get(cpName)).getConnection();
    }

    protected static ConnectionContainer getSchemaConnection(IProject project, String schema) throws SQLException
    {
        String schemaIdentifier = project.getName() + DOT + schema;
        ConnectionContainer container = (ConnectionContainer) mySchemaConnections.get(schemaIdentifier);
        if (container == null)
        {
            initConnectionPool(project, schema);
            Connection conn = ((ConnectionPool) myConnectionPools.get(schemaIdentifier)).getConnection();
            container = new DbUtility.ConnectionContainer(conn);
            mySchemaConnections.put(schemaIdentifier, container);
        }
        return container;
    }

    public static void commit(IProject project, String schema) throws SQLException
    {
        ConnectionContainer c = getSchemaConnection(project, schema);
        if (c != null)
        {
            c.connection.commit();
            c.isCommitPending = false;
        }
    }

    /**
     * This method rolls back a particular schema for a particular resource (which leads back to a particular project).
     * 
     * @param schema The schema to roll back.
     * 
     * @param
     * @throws SQLException
     */
    public static void rollback(IResource resource, String schema) throws SQLException
    {
        IProject project = resource.getProject();
        ConnectionContainer c = getSchemaConnection(project, schema);
        if (c != null)
        {
            c.connection.rollback();
            c.isCommitPending = false;
        }
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset returned based on
     * the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param isAddingEmptyValue This indicates whether an empty string should be added to the return value. If so, it
     *            will be the first entry.
     * 
     * @return The array of strings obtained from the first column of the data returned from the database based on the
     *         <code>sql</code> query.
     */
    public static String[] getObjects(IResource resource, String schema, String sql, Boolean isAddingEmptyValue)
    {
        String[] toReturn;
        PreparedStatement s = null;
        Connection c = null;
        IProject project = resource.getProject();
        try
        {
            c = getTempConnection(project, schema);
            s = c.prepareStatement(sql);
            toReturn = getObjects(s, isAddingEmptyValue);
        }
        catch (SQLException e)
        {
            System.out.println("Failed to retrieve objects: " + e);
            toReturn = new String[0];
        }
        finally
        {
            DbUtility.close(s);
            free(project, schema, c);
        }
        return toReturn;
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset returned based on
     * the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The statement that will yield a resultset.
     * 
     * @param isAddingEmptyValue This indicates whether an empty string should be added to the return value. If so, it
     *            will be the first entry.
     * 
     * @return The array of strings obtained from the first column of the data returned from the database based on the
     *         <code>sql</code> query.
     */
    public static String[] getObjects(PreparedStatement sql, Boolean isAddingEmptyValue)
    {
        String[] toReturn;
        try
        {
            ResultSet rs = sql.executeQuery();
            List v = new Vector();

            if (isAddingEmptyValue.booleanValue())
            {
                v.add("");
            }

            while (rs.next())
            {
                v.add(rs.getString(1));
            }

            toReturn = (String[]) v.toArray(new String[v.size()]);
        }
        catch (SQLException e)
        {
            System.out.println("Failed to retrieve objects: " + e);
            toReturn = new String[0];
        }
        return toReturn;
    }



    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset returned based on
     * the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param extraValues The extra values to set on the sql string.
     * 
     * @return The array of strings arrays obtained from the columns of the data returned from the database based on the
     *         <code>sql</code> query.
     */
    public static String[][] getObjects(IResource resource, String schema, String sql, Object[] extraValues)
    {
        String[][] toReturn;
        PreparedStatement s = null;
        Connection c = null;
        IProject project = resource.getProject();
        try
        {
            c = getTempConnection(project, schema);
            s = c.prepareStatement(sql);
            toReturn = getObjects(s, extraValues);
        }
        catch (SQLException e)
        {
            System.out.println("Failed to retrieve objects: " + e);
            toReturn = new String[0][0];
        }
        finally
        {
            DbUtility.close(s);
            free(project, schema, c);
        }
        return toReturn;
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset returned based on
     * the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param extraValues The extra values to set on the sql string.
     * 
     * @return The array of strings arrays obtained from the columns of the data returned from the database based on the
     *         <code>sql</code> query.
     */
    public static String[][] getObjects(PreparedStatement sql, Object[] extraValues)
    {
        String[][] toReturn;
        try
        {
            for (int i = 0; i < extraValues.length; i++)
            {
                sql.setObject(i + 1, extraValues[i]);
            }

            ResultSet rs = sql.executeQuery();
            List v = new Vector();

            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            while (rs.next())
            {
                List internalVector = new Vector();
                for (int i = 1; i <= numColumns; i++)
                {
                    internalVector.add(String.valueOf(rs.getObject(i)));
                }
                v.add(internalVector);
            }

            toReturn = new String[v.size()][];
            for (int i = 0; i < toReturn.length; i++)
            {
                List l = (List) v.get(i);
                toReturn[i] = (String[]) l.toArray(new String[l.size()]);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Failed to retrieve objects: " + e);
            toReturn = new String[0][0];
        }
        return toReturn;
    }

    /**
     * This method closes a statement if it is not null, ignoring any errors.
     * 
     * @param s The statement to close.
     */
    public static void close(Statement s)
    {
        if (s != null)
        {
            try
            {
                s.close();
            }
            catch (SQLException sqle)
            {
                sqle.printStackTrace();
            }
        }
    }

    /**
     * This method closes a result set if it is not null, ignoring any errors.
     * 
     * @param rs The result set to close.
     */
    public static void close(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException sqle)
            {
                sqle.printStackTrace();
            }
        }
    }

    public static void printErrors(SQLException e)
    {
        System.out.println("SQL Error is : " + e);
        SQLException nextException = e;
        while ((nextException = nextException.getNextException()) != null)
        {
            System.out.println("Next Exception is : " + nextException);
        }
    }

    public static void printWarnings(SQLWarning warning)
    {
        System.out.println("SQL Warning is : " + warning);
        SQLWarning nextWarning = warning;
        while ((nextWarning = nextWarning.getNextWarning()) != null)
        {
            System.out.println("Next Warning is : " + nextWarning);
        }
    }

    /**
     * 
     */
    public static void close()
    {
        List allProjects = new ArrayList();
        allProjects.addAll(theDbaConnectionPools.keySet());
        closeDbaConnections(allProjects);
        clearSchemaConnections(allProjects);
    }

    /**
     * This method reinitialises the connectivity properties when the default db preferences are reset.
     */
    public static void reinit()
    {
        String driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
        String url = thePrefs.getString(PreferenceConstants.P_URL);
        String user = thePrefs.getString(PreferenceConstants.P_USER);
        String passwd = thePrefs.getString(PreferenceConstants.P_PASSWORD);

        List connectionsToReset = new ArrayList();
        List dbaConnectionsToReset = new ArrayList();

        for (Iterator it = theDbaConnectionPools.keySet().iterator(); it.hasNext();)
        {
            IProject proj = (IProject) it.next();
            checkConnection(driver, url, user, passwd, connectionsToReset, dbaConnectionsToReset, proj);
        }
        // non dba stuff
        clearSchemaConnections(connectionsToReset);
        closeDbaConnections(connectionsToReset);
        // dba only stuff
        closeDbaConnections(dbaConnectionsToReset);
        updateListeners();
    }

    /**
     * This method reinitialises the connectivity properties of a SPECIFIC project.
     */
    public static void reinit(IProject project)
    {
        List projectList = Arrays.asList(new IProject[]{project});
        clearSchemaConnections(projectList);
        closeDbaConnections(projectList);
        updateListeners();
    }

    /**
     * This method checks that the connections related to the supplied <code>project</code> are ok, or they require
     * resetting.
     * 
     * @param driver The currently stored driver in the default prefs.
     * @param url The currently stored url in the default prefs.
     * @param user The currently stored user in the default prefs.
     * @param passwd The currently stored password in the default prefs.
     * @param connectionsToReset The list of connections (and dba connections) that require reset. This should be added
     *            to if the url, or driver have been modified.
     * @param dbaConnectionsToReset The list of dba connections that require reset (where the connections do NOT need to
     *            be reset. This should be added to if ONLY the user or password have been modified.
     * @param it
     */
    private static void checkConnection(String driver,
                                        String url,
                                        String user,
                                        String passwd,
                                        List connectionsToReset,
                                        List dbaConnectionsToReset,
                                        IProject project)
    {
        try
        {
            String isUsingLocalSettings = project.getPersistentProperty(new QualifiedName("",
                    PreferenceConstants.USE_LOCAL_DB_SETTINGS));
            if (!Boolean.valueOf(isUsingLocalSettings).booleanValue())
            {
                // remove the connection and reconnect
                String localDriver = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_DRIVER));
                String localUrl = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_URL));
                String localUser = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_USER));
                String localPwd = project.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_PASSWORD));
                if (!driver.equals(localDriver) || !url.equals(localUrl))
                {
                    connectionsToReset.add(project);
                }
                else if (!user.equals(localUser) || !passwd.equals(localPwd))
                {
                    dbaConnectionsToReset.add(project);
                }
            }
        }
        catch (CoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method closes all the dba connections for the list of IProject projects contained by the supplied
     * <code>projectList</code>.
     * 
     * @param projectList The list of IProject objects whose dba connection pools (stored in
     *            {@link #theDbaConnectionPools} indexed by these IProject objects) will be closed.
     */
    private static void closeDbaConnections(List projectList)
    {
        for (Iterator it = projectList.iterator(); it.hasNext();)
        {
            IProject proj = (IProject) it.next();
            ConnectionPool pool = (ConnectionPool) theDbaConnectionPools.remove(proj);
            if (pool != null)
            {
                pool.closeAllConnections();
            }
        }
    }

    /**
     * This method closes all the schemaconnections for the list of IProject projects contained by the supplied
     * <code>projectList</code>.
     * 
     * @param projectList The list of IProject objects whose schema connection pools will be closed. The schema
     *            connections are indexed by IProject.getName() + DOT + schema name.
     */
    private static void clearSchemaConnections(List projectList)
    {
        Shell shell = new Shell();
        List connectionsToRemove = new ArrayList();
        try
        {
            for (Iterator it = projectList.iterator(); it.hasNext();)
            {
                IProject project = (IProject) it.next();
                String projectQualifier = project.getName() + DOT;

                for (Iterator it2 = mySchemaConnections.keySet().iterator(); it2.hasNext();)
                {
                    String schemaIdentifier = (String) it2.next();
                    if (schemaIdentifier.startsWith(projectQualifier))
                    {
                        connectionsToRemove.add(schemaIdentifier);
                        ConnectionContainer container = (ConnectionContainer) mySchemaConnections.get(schemaIdentifier); 
                        Connection c = container.connection;
                        if (container.isCommitPending)
                        {
                            boolean isCommitting = isAutoCommittingOnClose(project) ? true : MessageDialog
                                    .openQuestion(shell,
                                                  "Possible uncommitted data for schema " + schemaIdentifier,
                                                  "There may be uncommitted data for schema " + schemaIdentifier
                                                          + "\nDo you want to commit (yes) or rollback (no)?");
                            try
                            {
                                if (isCommitting)
                                {
                                    c.commit();
                                }
                                else
                                {
                                    c.rollback();
                                }
                            }
                            catch (SQLException e)
                            {
                                if (!isAutoCommittingOnClose(project))
                                {
                                    String action = isCommitting ? "Commit" : "Rollback";
                                    MessageDialog.openInformation(shell, action, "Failed to execute : " + action
                                            + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            shell.dispose();
        }
        for (Iterator it = connectionsToRemove.iterator(); it.hasNext();)
        {
            String ident = (String) it.next();
            mySchemaConnections.remove(ident);
            ConnectionPool cp = (ConnectionPool) myConnectionPools.remove(ident);
            if (cp != null)
            {
                cp.closeAllConnections();
            }
        }
    }

    /**
     * This method returns the password stored in the registry for a given schema.
     * 
     * @param schema The schema whose password is required.
     * 
     * @return The password of the given registry or a blank if there is none stored.
     */
    public static String getPasswordForSchema(String schema)
    {
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        return plugin.getSchemaRegistry(plugin.getProject()).getPasswordForSchema(schema);
    }

    /**
     * This method loads a file into the database, using the single connection configured for the schema with the
     * supplied <code>schemaName</code>, returning any errors it found.
     * 
     * @param schemaName The name of the schema being loaded, for error purposes.
     * 
     * @param toLoad The myOutputText representation of the entire package.
     * 
     * @return The list of errors from the compile, or null if there were none.
     * 
     * @throws SQLException when there is a database error
     */
    public static ResultSetWrapper loadCode(IResource resource, String schemaName, String toLoad) throws SQLException
    {
        Connection c = null;
        PreparedStatement s = null;
        ResultSetWrapper toReturn = null;
        IProject project = resource.getProject();
        try
        {
            ConnectionContainer cc = getSchemaConnection(project, schemaName); 
            c = cc.connection;
            s = c.prepareStatement(toLoad);
            s.execute();

            ResultSet rs = s.getResultSet();
            toReturn = new ResultSetWrapper(rs, s, schemaName);
            cc.isCommitPending = toReturn.getUpdateCount() > 0; 
        }
        catch (SQLException e)
        {
            DbUtility.printErrors(e);
            // this will be closed in the finally
            // don't free, this remains open
            throw e;
        }
        finally
        {
            if (toReturn == null)
            {
                DbUtility.close(s);
                // don't free, this remains open
            }
        }
        return toReturn;
    }

    /**
     * This method returns the Oracle SID of the database to which we are currently talking.
     * 
     * @return The current Oracle SID.
     */
    public static String getSid(IProject project)
    {
        String url = null;

        try
        {
            url = getDbaConnectionPool(project).getUrl();
        }
        catch (Exception e)
        {
            url = null;
        }

        if (url == null)
        {
            return "";
        }
        // e.g. jdbc:oracle:thin:@aalsun108:1521:SID
        return url.replaceFirst("jdbc\\:oracle\\:\\w+\\:\\@[^:]+\\:\\d+\\:(.*)$", "$1");
    }

    /**
     * @return The list of currently running connection pools and their states as a list of strings.
     */
    public static List getCurrentConnectionPoolList()
    {
        List l = new ArrayList();
        for (Iterator it = theDbaConnectionPools.values().iterator(); it.hasNext();)
        {
            ConnectionPool dbaCp = (ConnectionPool) it.next();
            if (dbaCp != null)
            {
                String dbaConn = "Dba Conn: " + dbaCp.toString();
                l.add(dbaConn);
            }
        }
        for (Iterator it = myConnectionPools.values().iterator(); it.hasNext();)
        {
            ConnectionPool cp = (ConnectionPool) it.next();
            String connString = "User Conn: " + cp.toString();
            l.add(connString);
        }
        return l;
    }

    /**
     * @return Returns the isAutoCommittingOnClose.
     */
    public static boolean isAutoCommittingOnClose(IProject project)
    {
        return getDbaConnectionPool(project).isAutoCommittingOnClose();
    }


    /**
     * @param isAutoCommittingOnClose The isAutoCommittingOnClose to set.
     */
    public static void setAutoCommittingOnClose(IProject project, boolean isAutoCommittingOnClose)
    {
        getDbaConnectionPool(project).setAutoCommitting(isAutoCommittingOnClose);
    }

    /**
     * This method retrieves the dbms output object for the given <code>schema</code>.
     * 
     * @param resource The resource whose project dictates which dbms output will be selected.
     * 
     * @param schema The name of the schema whose dbms output wrapper is desired.
     * 
     * @return the dbms output object for the given <code>schema</code>.
     * 
     * @throws SQLException when the connection is dead.
     */
    public static DbmsOutput getDbmsOutput(IResource resource, String schema) throws SQLException
    {
        DbmsOutput output = (DbmsOutput) myDbmsOutputs.get(schema);
        IProject project = resource.getProject();
        if (output == null)
        {
            output = new DbmsOutput(getSchemaConnection(project, schema).connection);
            myDbmsOutputs.put(schema, output);
        }
        return output;
    }
}
