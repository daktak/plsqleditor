package plsqleditor.stores;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.Position;

import plsqleditor.db.ConnectionPool;
import plsqleditor.parsers.Segment;

/**
 * This class represents a store of all the schema, package and segment (function, procedure field)
 * information about the pl-sql source in the database.
 * 
 * @author Andrew Cohen
 */
public class DBPackageStore
{
    private ConnectionPool                 myDefaultConnectionPool;

    /**
     * This field maps a schema name to a list of packages.
     */
    private HashMap<String, List<String>>  mySchemaToPackageMap;

    /**
     * This field maps a package name to a list of segments.
     */
    private HashMap<String, List<Segment>> myPackageToSegmentMap;

    /**
     * The last time we got the list of schemas.
     */
    private Timestamp                      myDBLastCacheTime;

    /**
     * This field maps each schema to the time we last cached that schema's objects.
     */
    private HashMap<String, Timestamp>     mySchemaToLastCacheTimeMap;

    /**
     * This field maps each package to the time we last cached that package's segments.
     */
    private HashMap<String, Timestamp>     myPackageToLastCacheTimeMap;

    /**
     * A dummy position to be used when creating Segments. DBPackageStore doesn't keep track of
     * Positions so we need a dummy position.
     */
    private static final Position          dummyPosition = null;

	/**
	 * This SQL string is like a view that shows you all viewable packages
	 * for a given schema, including ones that are viewable via a synonym.
	 * Note that to use it in a preparedStatement, you must bind the 
	 * schemaName 3 times.
	 * There are two reasons this is a sql-string and not just a view in 
	 * the database.  First of all, we don't want to depend on any DDL
	 * statements, and second of all it makes this query faster to have
	 * the schema name in the where-clause as it is here, which you couldn't
	 * do with a view.
	 */
	private static final String            myViewablePackagesSQL = 
		"SELECT p.owner  " + // user who can view this package 
		",p.object_name NAME " + // viewable name of package, i.e. name or synonym name
		",p.owner       package_owner " + // user who owns the package
		",p.object_name package_name " + // actual name of package
		",p.last_ddl_time " + 
		"FROM all_objects p " + 
		"WHERE p.object_type = 'PACKAGE' " + 
		"AND p.owner = UPPER(?) " + 
		"UNION ALL " + 
		"SELECT s.owner owner " + 
		",s.synonym_name NAME " + 
		",s.table_owner package_owner " + 
		",s.table_name package_name " + 
		",p.last_ddl_time " + 
		"FROM all_synonyms s, all_objects p " +
		"WHERE p.object_type = 'PACKAGE' " +
		"AND p.owner <> UPPER(?) " +
		"AND s.owner IN (UPPER(?), 'PUBLIC') " +
		"AND s.synonym_name = p.object_name " +
		"AND s.table_name = p.object_name " +
		"AND s.table_owner = p.owner";

    /**
     * Initializes the DBPackageStore with a database connection. Note that this connection must be
     * kept open throughout the life of DBPackageStore. At the moment there is no support for a
     * connection pool.
     * 
     * @param defaultConnectionPool
     */
    public DBPackageStore(ConnectionPool defaultConnectionPool)
    {
        myDefaultConnectionPool = defaultConnectionPool;
        myPackageToLastCacheTimeMap = new HashMap<String, Timestamp>();
        mySchemaToLastCacheTimeMap = new HashMap<String, Timestamp>();
        myDBLastCacheTime = null;
        mySchemaToPackageMap = new HashMap<String, List<String>>();
        myPackageToSegmentMap = new HashMap<String, List<Segment>>();
    }

    /**
     * Gets the SYSDATE from the database
     * 
     * @return The current SYSDATE (system Timestamp).
     * @throws SQLException If there is a problem connecting to the database.
     */
    protected Timestamp getSysTimestamp() throws SQLException
    {
        String sql = "SELECT sysdate FROM dual";
        List lt = getObjectsByVariables(sql, new Object[]{}, new Timestamp[0]);
        return (Timestamp) lt.get(0);
    }

    /**
     * Gets the default connection for this class
     * 
     * @return a connection object
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException
    {
        return myDefaultConnectionPool.getConnection();
    }

    /**
     * Utility function to get a list of objects from a SQL query string. Example:
     * <code>newStrings = getObjectsByVariables("SELECT object_name FROM user_objects WHERE object_name = UPPER(?)",new String("xxx");</code>
     * 
     * @param sql SQL to execute. Only the first column in the SELECT is relevant.
     * @param objects The objects to be used as bind parameter in the WHERE clause.
     * @param returnClass The return class. Defaults to String.
     * @return The list of objects in the result set.
     * @throws SQLException If the sql is malformed or there is a problem with the database
     *             connection.
     */
    protected <T> List<T> getObjectsByVariables(String sql, Object[] objects, T[] dummy)
            throws SQLException
    {
        List<Object> toReturn = new ArrayList<Object>();
        ResultSet rs = getResultSetByVariables(sql, objects);
        Class returnClass = dummy.getClass().getComponentType();
        while (rs.next())
        {
            if (returnClass.equals(String.class))
            {
                toReturn.add(rs.getString(1));
            }
            else if (returnClass == Time.class)
            {
                toReturn.add(rs.getTime(1));
            }
            else if (returnClass == Timestamp.class)
            {
                toReturn.add(rs.getTimestamp(1));
            }
            else if (returnClass == Date.class)
            {
                toReturn.add(rs.getDate(1));
            }
            else if (returnClass == Integer.class)
            {
                toReturn.add(rs.getInt(1));
            }
        }
        Statement s = rs.getStatement();
        s.close(); // s closes rs

        T[] a = toReturn.toArray(dummy);
        List<T> newList = new ArrayList<T>();
        for (T t : a)
        {
            newList.add(t);
        }
        return newList;
    }

    protected List<String> getObjectsByVariables(String sql, Object[] objects) throws SQLException
    {
        return getObjectsByVariables(sql, objects, new String[0]);
    }

    /**
     * Utility function to get a result set from a SQL query string. Exemple:
     * <code>ResultSet rs = getResultSetByVariables("SELECT object_name FROM user_objects WHERE object_name = UPPER(?)",new String("xxx");</code>
     * 
     * @param sql SQL to execute.
     * @param objects The objects to be used as bind parameter in the where clause.
     * @return A ResultSet that can be scrolled through and examined to get the results.
     * @throws SQLException If the sql is malformed or there is a problem with the database
     *             connection.
     */
    protected ResultSet getResultSetByVariables(String sql, Object[] objects) throws SQLException
    {
        PreparedStatement s = null;
        ResultSet rs = null;
        Connection c = null;
        try
        {
            c = getConnection();
            s = c.prepareStatement(sql);
            for (int i = 0; i < objects.length; i++)
            {
                if (objects[i].getClass() == String.class)
                {
                    s.setString(i + 1, (String) objects[i]);
                }
                else if (objects[i].getClass() == Timestamp.class)
                {
                    s.setTimestamp(i + 1, (Timestamp) objects[i]);
                }
                else if (objects[i].getClass() == Time.class)
                {
                    s.setTime(i + 1, (Time) objects[i]);
                }
                else if (objects[i].getClass() == Date.class)
                {
                    s.setDate(i + 1, (Date) objects[i]);
                }
                else if (objects[i].getClass() == Integer.class)
                {
                    s.setInt(i + 1, (Integer) objects[i]); // can throw an exception if null
                    // integer
                }
                else
                {
                    String msg = "Invalid class for parameter " + i;
                    System.out.println(msg);
                    throw new IllegalStateException(msg);
                }
            }
            rs = s.executeQuery();
        }
        catch (SQLException e)
        {
            System.out.println("Failed to get ResultSet by Parameters: " + e);
            throw e;
        }
        finally
        {
            releaseConnection(c);
        }
        return rs;
    }

    private void releaseConnection(Connection c)
    {
        myDefaultConnectionPool.free(c);
    }

    /**
     * Returns our internal representation of packageName to be used as the key to the HashMaps. In
     * reality just concatenates the two together.
     * 
     * @param schemaName
     * @param packageName
     * @return the system-wide unique packageName
     */
    protected String getPackageName(String schemaName, String packageName)
    {
        return schemaName + "." + packageName;
    }

    /**
     * This method returns the list of currently known schema names. The first time it is called, it
     * will look the schema names up in the database. On subsequenet calls, it will only check for
     * new scemas. Note that dropped schemas will never be removed from the cache for now.
     * 
     * @param forceUpdate Pass true if you want to force a refresh of all schemas. This will cause
     *            all packages for all schemas to be reset. Defaults to false.
     * 
     * @param updateList Pass true if you want to check for newly added schemas. Default true.
     * 
     * @return The list of currently known schema names.
     * @throws SQLException
     */
    public List<String> getSchemas(boolean forceUpdate, boolean updateList) throws SQLException
    {
        List<String> schemaList = new ArrayList<String>();
        List<String> newSchemaList = null; // this gets initialized later
        String allSchemasSql = "SELECT username " + "FROM all_users u "
                + "WHERE EXISTS (SELECT p.procedure_name " + "              FROM all_procedures p "
                + "              WHERE p.owner = u.username)";
        String newSchemasSql = "SELECT username " + "FROM all_users u " + "WHERE u.created >= ? "
                + "AND EXISTS (SELECT p.procedure_name " + "            FROM all_procedures p "
                + "            WHERE p.owner = u.username)";

        schemaList.addAll(mySchemaToPackageMap.keySet());
        if (schemaList.isEmpty() || myDBLastCacheTime == null || forceUpdate)
        {
            schemaList = getObjectsByVariables(allSchemasSql, new Object[]{});
            myDBLastCacheTime = getSysTimestamp();
            newSchemaList = schemaList;
        }
        else if (updateList)
        {
            newSchemaList = getObjectsByVariables(newSchemasSql, new Object[]{myDBLastCacheTime});
            schemaList.addAll(newSchemaList);
            myDBLastCacheTime = getSysTimestamp();
        }
        // Now add all new schemas to our cache
        for (String s : newSchemaList)
        {
            mySchemaToPackageMap.put(s, null);
        }
        return schemaList;
    }

    public List<String> getSchemas() throws SQLException
    {
        return getSchemas(false, true);
    }

    /**
     * Loads the segments for the given schema and package into our cache, and returns the list.
     * 
     * @param schemaName Schema of package to be loaded.
     * @param packageName Package to be loaded
     * @return The list of segments that were just loaded.
     * @throws SQLException
     */
    protected List<Segment> loadSegments(String schemaName, String packageName) throws SQLException
    {
        String sql = 
			"SELECT p.procedure_name " +
			"      ,p.object_name " +
			"FROM   all_procedures p " +
			"      , (" + myViewablePackagesSQL + ") pk " +
			"WHERE  pk.owner IN (UPPER(?), 'PUBLIC') " +
			"AND    pk.NAME = UPPER(?) " +
			"AND    p.owner = pk.package_owner " +
			"AND    p.object_name = pk.package_name";

        Timestamp cacheTime = getSysTimestamp();
        List<String> segmentNameList = getObjectsByVariables(sql, new Object[]{schemaName,schemaName,schemaName,schemaName,
                packageName});
        List<Segment> segmentList = new ArrayList<Segment>();
        for (String segmentName : segmentNameList)
        {
            segmentList.add(getSegmentFromDB(schemaName, packageName, segmentName));
        }
        myPackageToSegmentMap.put(getPackageName(schemaName, packageName), segmentList);
        myPackageToLastCacheTimeMap.put(getPackageName(schemaName, packageName), cacheTime);
        return segmentList;
    }

    /**
     * Gets the segment representing a function or procedure from the database.
     * 
     * @param schemaName The scehma name.
     * @param packageName The package name.
     * @param segmentName The segment name.
     * @return The segment representing the procedure or function.
     * @throws SQLException
     */
    protected Segment getSegmentFromDB(String schemaName, String packageName, String segmentName)
            throws SQLException
    {
        String sql = 
			"SELECT a.argument_name, a.position, a.in_out " +
			"      ,(CASE WHEN a.type_owner IS NULL THEN a.data_type ELSE a.type_owner || '.'|| a.type_name || '.'|| a.type_subname END) data_type " +
			"      ,(CASE WHEN pk.owner <> pk.package_owner THEN 'YES' ELSE 'NO' END) is_synonym " +
			"FROM   all_arguments a " + 
			"      , (" + myViewablePackagesSQL + ") pk " +
			"WHERE  pk.owner IN (UPPER(?), 'PUBLIC') " +
			"AND    pk.NAME = UPPER(?) " +
			"AND    a.owner = pk.package_owner " + 
			"AND    a.package_name = pk.package_name " +
			"AND    a.OBJECT_NAME = UPPER(?) " +
			"AND    a.DATA_LEVEL = 0 " +
			"ORDER BY a.position";
		System.out.println(sql);
        Segment.SegmentType segmentType = Segment.SegmentType.Procedure;
        String segmentReturnType = null;
        Segment segment = null;

        ResultSet rs = getResultSetByVariables(sql, new Object[]{schemaName, schemaName, schemaName, schemaName, packageName,
                segmentName});

        while (rs.next())
        {
            if (rs.isFirst())
            {
                if (rs.getInt("POSITION") == 0)
                {
                    segmentType = Segment.SegmentType.Function;
                    segmentReturnType = rs.getString("DATA_TYPE");
                }
                segment = new Segment(segmentName, dummyPosition, segmentType);
                segment.setReturnType(segmentReturnType); // hopefully shouldn't freak if it's set to null
				segment.setPublic(rs.getString("IS_SYNONYM").equals("YES"));
            }
            if (rs.getInt("POSITION") > 0)
            {
                segment.addParameter(rs.getString("ARGUMENT_NAME"), rs.getString("IN_OUT"), rs
                        .getString("DATA_TYPE"));
            }
        }

        return segment;
    }

    /**
     * Returns the segments (procedures and functions) for the given schema and package. Will make
     * appropriate use of cache, and/or get updates from the database.
     * 
     * @param schemaName The schema name.
     * @param packageName The package name.
     * @param forceUpdate Pass true if you want to force it to look at the database. Default is
     *            <code>false</code>.
     * @return the list of segments for the given schema and package.
     * @throws SQLException
     */
    public List<Segment> getSegments(String schemaName, String packageName, boolean forceUpdate)
            throws SQLException
    {
        String lastPackageDDLDateSQL = 
			"SELECT last_ddl_time " + 
			"FROM   (" + myViewablePackagesSQL + ") o " + 
			"WHERE  o.owner IN (UPPER(?),'PUBLIC') " +  
			"AND    o.name = UPPER(?) ";

        List<Segment> segmentList = myPackageToSegmentMap.get(getPackageName(schemaName,
                                                                             packageName));
        if (segmentList == null || forceUpdate)
        {
            segmentList = loadSegments(schemaName, packageName);
        }
        else
        {
            Timestamp lastCacheTime = myPackageToLastCacheTimeMap.get(getPackageName(schemaName,
                                                                                     packageName));
            List<Timestamp> lastUpdateTime = getObjectsByVariables(lastPackageDDLDateSQL,
                                                                   new Object[]{schemaName, schemaName, schemaName, schemaName, 
                                                                           packageName},
                                                                   new Timestamp[0]);
            if (lastUpdateTime.size() == 0 || lastUpdateTime.get(0).after(lastCacheTime))
            {
                segmentList = loadSegments(schemaName, packageName);

            }
        }
        return segmentList;
    }

    public List<Segment> getSegments(String schemaName, String packageName) throws SQLException
    {
        return getSegments(schemaName, packageName, false);
    }

    /**
     * Gets all packages in the given schema. Will make appropriate use of cache, and/or get updates
     * from the database.
     * 
     * @param schemaName The schema name.
     * @param forceUpdate Pass true if you want to force it to look at the database. Default is
     *            <code>false</code>.
     * @return The list of packages for the given schema.
     * @throws SQLException
     */
    public List<String> getPackages(String schemaName, boolean forceUpdate) throws SQLException
    {

        String allPackagesSQL = 
			"SELECT p.name " + // for synonyms, this is the synonym
			"      ,p.owner " + // for synonyms, this is the synonym-owner, i.e. PUBLIC
			"FROM (" + myViewablePackagesSQL + ") p ";

        String newPackagesSQL =
			"SELECT p.name " + // for synonyms, this is the synonym
			"      ,p.owner " + // for synonyms, this is the synonym-owner, i.e. PUBLIC
			"FROM (" + myViewablePackagesSQL + ") p " + 
			"WHERE p.last_ddl_time >= ?";

        List<String> packageList = mySchemaToPackageMap.get(schemaName);
        List<String> newPackageList = null;
        Timestamp newCacheTime = getSysTimestamp(); // get cache Timestamp before selects so that
        // reported cache Timestamp is no later than
        // actual cache Timestamp

        if (packageList == null || forceUpdate)
        {
            packageList = getObjectsByVariables(allPackagesSQL, new Object[]{schemaName, schemaName, schemaName});
            mySchemaToPackageMap.put(schemaName, packageList);
            mySchemaToLastCacheTimeMap.put(schemaName, newCacheTime);
        }
        else
        {
            newPackageList = getObjectsByVariables(newPackagesSQL, new Object[]{schemaName, schemaName, schemaName,
                    mySchemaToLastCacheTimeMap.get(schemaName)});
            if (!newPackageList.isEmpty())
            {
                packageList.addAll(newPackageList);
                // Shouldn't have to call because packageList should be a pointer not a copy
                mySchemaToPackageMap.put(schemaName, packageList);
                mySchemaToLastCacheTimeMap.put(schemaName, newCacheTime);
            }
        }

        return packageList;
    }

    public List<String> getPackages(String schemaName) throws SQLException
    {
        return getPackages(schemaName, false);
    }

    public String getSource(String schemaName, String packageName) throws SQLException
    {
        String packageSpec = "";
        String sql = 
			"SELECT text " + 
			"FROM   all_source s " + 
			"WHERE  owner IN (UPPER(?),'PUBLIC') " + 
			"AND    s.TYPE = 'PACKAGE' " + 
			"AND    s.NAME = UPPER(?) " + 
			"ORDER BY s.line ";

        List<String> lines = getObjectsByVariables(sql, new Object[]{schemaName, packageName});

        for (String l : lines)
        {
            packageSpec += l;
        }
        return packageSpec;
    }

}
