package plsqleditor.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;

import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.SchemaRegistry;

/**
 * @author zinest
 * 
 */
public class DbUtility
{
    private static ConnectionPool              theConnectionPool;
    private static IPreferenceStore            thePrefs;
    private static String                      theUrl;
    private static String                      theDriver;
    private static String                      theDbaUserName;
    private static String                      theDbaPassword;

    private static Map<String, ConnectionPool> myConnectionPools = new HashMap<String, ConnectionPool>();
    private static SchemaRegistry              myRegistry;

    private static List<DbPrefsUpdateListener> myListeners       = new ArrayList<DbPrefsUpdateListener>();

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
        for (DbPrefsUpdateListener l : myListeners)
        {
            l.dbPrefsUpdated();
        }
    }

    public static void init(IPreferenceStore prefs, SchemaRegistry registry)
    {
        thePrefs = prefs;
        myRegistry = registry;
    }

    /**
     * This method initialises the connection pool being used to talk to the database.
     */
    private static void initDbaConnectionPool()
    {
        if (theConnectionPool == null)
        {
            if (thePrefs == null)
            {
                throw new IllegalStateException("Prefs have not been set");
            }
            try
            {
                String driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
                String url = thePrefs.getString(PreferenceConstants.P_URL);
                String user = thePrefs.getString(PreferenceConstants.P_USER);
                String passwd = thePrefs.getString(PreferenceConstants.P_PASSWORD);
                int initConns = thePrefs.getInt(PreferenceConstants.P_INIT_CONNS);
                int maxConns = thePrefs.getInt(PreferenceConstants.P_MAX_CONNS);

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

                theDbaUserName = user;
                theDbaPassword = passwd;
                theUrl = url;
                theDriver = driver;

                theConnectionPool = new ConnectionPool(driver, url, user, passwd, initConns,
                        maxConns, true);
            }
            catch (SQLException e)
            {
                String msg = "Failed to initialise connection pool: " + e;
                System.out.println(msg);
                throw new IllegalStateException(msg, e);
            }
        }
    }

    /**
     * This method initialises the connection pool being used to talk to the database.
     */
    private static void initConnectionPool(String schemaName)
    {
        if (thePrefs == null)
        {
            throw new IllegalStateException("Prefs have not been set");
        }
        try
        {
            ConnectionPool pool = myConnectionPools.get(schemaName);
            if (pool == null)
            {
                String driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
                String url = thePrefs.getString(PreferenceConstants.P_URL);
                String user = schemaName;
                String passwd = myRegistry.getPasswordForSchema(schemaName);
                if (schemaName == null)
                {
                    throw new IllegalStateException("Password for schema " + schemaName
                            + " is missing");
                }
                int initConns = thePrefs.getInt(PreferenceConstants.P_INIT_CONNS);
                int maxConns = thePrefs.getInt(PreferenceConstants.P_MAX_CONNS);

                ConnectionPool cp = new ConnectionPool(driver, url, user, passwd, initConns,
                        maxConns, true);
                myConnectionPools.put(schemaName, cp);
            }
        }
        catch (SQLException e)
        {
            String msg = "Failed to initialise connection pool: " + e;
            System.out.println(msg);
            throw new IllegalStateException(msg, e);
        }
    }


    public static ConnectionPool getDbaConnectionPool()
    {
        initDbaConnectionPool();
        return theConnectionPool;
    }

    protected static Connection getConnection(String schema)
    {
        try
        {
            initConnectionPool(schema);
            return myConnectionPools.get(schema).getConnection();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param schemaName
     * @param c
     */
    protected static void free(String schemaName, Connection c)
    {
        myConnectionPools.get(schemaName).free(c);
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset
     * returned based on the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param isAddingEmptyValue This indicates whether an empty string should be added to the
     *            return value. If so, it will be the first entry.
     * 
     * @return The array of strings obtained from the first column of the data returned from the
     *         database based on the <code>sql</code> query.
     */
    public static String[] getObjects(String schema, String sql, Boolean isAddingEmptyValue)
    {
        String[] toReturn;
        PreparedStatement s = null;
        Connection c = null;
        try
        {
            initConnectionPool(schema);
            c = myConnectionPools.get(schema).getConnection();
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
            myConnectionPools.get(schema).free(c);
        }
        return toReturn;
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset
     * returned based on the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The statement that will yield a resultset.
     * 
     * @param isAddingEmptyValue This indicates whether an empty string should be added to the
     *            return value. If so, it will be the first entry.
     * 
     * @return The array of strings obtained from the first column of the data returned from the
     *         database based on the <code>sql</code> query.
     */
    public static String[] getObjects(PreparedStatement sql, Boolean isAddingEmptyValue)
    {
        String[] toReturn;
        try
        {
            ResultSet rs = sql.executeQuery();
            List<String> v = new Vector<String>();

            if (isAddingEmptyValue.booleanValue())
            {
                v.add("");
            }

            while (rs.next())
            {
                v.add(rs.getString(1));
            }

            toReturn = v.toArray(new String[v.size()]);
        }
        catch (SQLException e)
        {
            System.out.println("Failed to retrieve objects: " + e);
            toReturn = new String[0];
        }
        return toReturn;
    }



    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset
     * returned based on the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param extraValues The extra values to set on the sql string.
     * 
     * @return The array of strings arrays obtained from the columns of the data returned from the
     *         database based on the <code>sql</code> query.
     */
    public static String[][] getObjects(String schema, String sql, Object[] extraValues)
    {
        String[][] toReturn;
        PreparedStatement s = null;
        Connection c = null;
        try
        {
            initConnectionPool(schema);
            c = myConnectionPools.get(schema).getConnection();
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
            theConnectionPool.free(c);
        }
        return toReturn;
    }

    /**
     * This method gets a set of Strings from the database, based on the first column of a resultset
     * returned based on the provided <code>sql</code> query. The first column must be a string.
     * 
     * @param sql The query that will yield a resultset.
     * 
     * @param extraValues The extra values to set on the sql string.
     * 
     * @return The array of strings arrays obtained from the columns of the data returned from the
     *         database based on the <code>sql</code> query.
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
            List<List<String>> v = new Vector<List<String>>();

            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            while (rs.next())
            {
                List<String> internalVector = new Vector<String>();
                for (int i = 1; i <= numColumns; i++)
                {
                    internalVector.add(String.valueOf(rs.getObject(i)));
                }
                v.add(internalVector);
            }

            toReturn = new String[v.size()][];
            for (int i = 0; i < toReturn.length; i++)
            {
                List<String> l = v.get(i);
                toReturn[i] = l.toArray(new String[l.size()]);
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
     * This method closes a statement if it is not null, ignoring any errors.
     * 
     * @param s The statement to close.
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
        closeDbaConnection();
        clearSchemaConnections();
    }

    /**
     * 
     */
    public static void reinit()
    {
        String driver = thePrefs.getString(PreferenceConstants.P_DRIVER);
        String url = thePrefs.getString(PreferenceConstants.P_URL);
        String user = thePrefs.getString(PreferenceConstants.P_USER);
        String passwd = thePrefs.getString(PreferenceConstants.P_PASSWORD);
        if (!driver.equals(theDriver) || !url.equals(theUrl))
        {
            clearSchemaConnections();
            closeDbaConnection();
            updateListeners();
        }
        else if (!user.equals(theDbaUserName) || !passwd.equals(theDbaPassword))
        {
            closeDbaConnection();
            updateListeners();
        }

    }

    private static void closeDbaConnection()
    {
        if (theConnectionPool != null)
        {
            theConnectionPool.closeAllConnections();
            theConnectionPool = null;
        }
    }

    private static void clearSchemaConnections()
    {
        for (ConnectionPool cp : myConnectionPools.values())
        {
            cp.closeAllConnections();
        }
        myConnectionPools.clear();
    }
}
