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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;

import plsqleditor.db.ConnectionPool;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

/**
 * This class represents a store of all the schema, package and segment
 * (function, procedure field) information about the pl-sql source in the
 * database.
 * 
 * @author Andrew Cohen
 */
public class DBPackageStore
{
	private ConnectionPool myDefaultConnectionPool;

	/**
	 * This field maps a schema name to a list of packages.
	 */
	private HashMap mySchemaToPackageMap;

	/**
	 * This field maps a package name to a list of segments.
	 */
	private HashMap myPackageToSegmentMap;

	/**
	 * The last time we got the list of schemas.
	 */
	private Timestamp myDBLastCacheTime;

	/**
	 * This field maps each schema to the time we last cached that schema's
	 * objects.
	 */
	private HashMap mySchemaToLastCacheTimeMap;

	/**
	 * This field maps each schema to the public schema boolean last requested
	 * of it.
	 */
	private HashMap mySchemaToLastPublicSchemaRequestBoolean;

	/**
	 * This field maps each package to the time we last cached that package's
	 * segments.
	 */
	private HashMap<String, Timestamp> myPackageToLastCacheTimeMap;

	/**
	 * A dummy position to be used when creating Segments. DBPackageStore
	 * doesn't keep track of Positions so we need a dummy position.
	 */
	private static final Position dummyPosition = null;

	/**
	 * This SQL string is like a view that shows you all viewable packages for a
	 * given schema, including ones that are viewable via a synonym. Note that
	 * to use it in a preparedStatement, you must bind the schemaName 3 times.
	 * There are two reasons this is a sql-string and not just a view in the
	 * database. First of all, we don't want to depend on any DDL statements,
	 * and second of all it makes this query faster to have the schema name in
	 * the where-clause as it is here, which you couldn't do with a view.
	 */
	private static final String myViewablePackagesSQL = "SELECT p.owner  "
			+ // user who can view this
			// package
			",p.object_name NAME "
			+ // viewable name of package,
			// i.e. name or synonym name
			",p.owner       package_owner "
			+ // user who owns the
			// package
			",p.object_name package_name "
			+ // actual name of
			// package
			",p.last_ddl_time " + "FROM all_objects p "
			+ "WHERE p.object_type = 'PACKAGE' " + "AND p.owner = UPPER(?) "
			+ "UNION ALL " + "SELECT s.owner owner " + ",s.synonym_name NAME "
			+ ",s.table_owner package_owner " + ",s.table_name package_name "
			+ ",p.last_ddl_time " + "FROM all_synonyms s, all_objects p "
			+ "WHERE p.object_type = 'PACKAGE' " + "AND p.owner <> UPPER(?) "
			+ "AND s.owner IN (UPPER(?), 'PUBLIC') "
			+ "AND s.synonym_name = p.object_name "
			+ "AND s.table_name = p.object_name "
			+ "AND s.table_owner = p.owner";

	/**
	 * Initialises the DBPackageStore with a database connection pool.
	 * 
	 * @param defaultConnectionPool
	 */
	public DBPackageStore(ConnectionPool defaultConnectionPool)
	{
		myDefaultConnectionPool = defaultConnectionPool;
		myPackageToLastCacheTimeMap = new HashMap();
		mySchemaToLastCacheTimeMap = new HashMap();
		mySchemaToLastPublicSchemaRequestBoolean = new HashMap();
		myDBLastCacheTime = null;
		mySchemaToPackageMap = new HashMap();
		myPackageToSegmentMap = new HashMap();
	}

	/**
	 * Gets the SYSDATE from the database
	 * 
	 * @return The current SYSDATE (system Timestamp).
	 * @throws SQLException
	 *             If there is a problem connecting to the database.
	 */
	protected Timestamp getSysTimestamp() throws SQLException
	{
		String sql = "SELECT sysdate FROM dual";
		SortedSet lt = getObjectsByVariables(sql, new Object[] {},
				new Timestamp[0]);
		return (Timestamp) lt.first();
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
	 * Utility function to get a list of objects from a SQL query string.
	 * Example:
	 * <code>newStrings = getObjectsByVariables("SELECT object_name FROM user_objects WHERE object_name = UPPER(?)",new String("xxx");</code>
	 * 
	 * @param sql
	 *            SQL to execute. Only the first column in the SELECT is
	 *            relevant.
	 * @param objects
	 *            The objects to be used as bind parameter in the WHERE clause.
	 * @param dummy
	 *            The object array in the particular format of the return class.
	 * @return The list of objects in the result set.
	 * @throws SQLException
	 *             If the sql is malformed or there is a problem with the
	 *             database connection.
	 */
	protected SortedSet getObjectsByVariables(String sql, Object[] objects,
			Object[] dummy) throws SQLException
	{
		SortedSet toReturn = new TreeSet();
		ResultSet rs = getResultSetByVariables(sql, objects);
		Class returnClass = dummy.getClass().getComponentType();
		while (rs.next())
		{
			if (returnClass.equals(String.class))
			{
				String s = rs.getString(1);
				// fix for [ 1512712 ] "Content Assist" did not complete
				// normally. NPE
				if (s != null)
				{
					// should we add a blank here?
					toReturn.add(s);
				}
			}
			else if (returnClass == Time.class)
			{
				Time t = rs.getTime(1);
				if (t != null)
				{
					// should we add a blank here?
					toReturn.add(t);
				}
			}
			else if (returnClass == Timestamp.class)
			{
				Timestamp t = rs.getTimestamp(1);
				if (t != null)
				{
					// should we add a blank here?
					toReturn.add(t);
				}
			}
			else if (returnClass == Date.class)
			{
				Date d = rs.getDate(1);
				if (d != null)
				{
					// should we add a blank here?
					toReturn.add(d);
				}
			}
			else if (returnClass == Integer.class)
			{
				toReturn.add(new Integer(rs.getInt(1)));
			}
		}
		Statement s = rs.getStatement();
		s.close(); // s closes rs

		return toReturn;
	}

	/**
	 * Utility function to get a list of objects from a SQL query string.
	 * Example:
	 * <code>newStrings = getObjectsByVariables("SELECT object_name FROM user_objects WHERE object_name = UPPER(?)",new String("xxx");</code>
	 * 
	 * @param sql
	 *            SQL to execute. Only the first column in the SELECT is
	 *            relevant.
	 * @param objects
	 *            The objects to be used as bind parameter in the WHERE clause.
	 * @param dummyArrays
	 *            An array of object arrays, each of which is in the particular
	 *            format of the return classes.
	 * 
	 * @return The list of object arrays representing each field of retrieved
	 *         data in the result set.
	 * 
	 * @throws SQLException
	 *             If the sql is malformed or there is a problem with the
	 *             database connection.
	 */
	protected List<Object[]> getObjectSetsByVariables(String sql, Object[] objects,
			Object[] dummyArrays) throws SQLException
	{
		List<Object[]> toReturn = new ArrayList<Object[]>();
		ResultSet rs = getResultSetByVariables(sql, objects);
		while (rs.next())
		{
			Object[] retrievedData = new Object[dummyArrays.length];
			for (int i = 0; i < dummyArrays.length; i++)
			{
				Object[] dummy = (Object[]) dummyArrays[i];
				Class returnClass = dummy.getClass().getComponentType();
				if (returnClass.equals(String.class))
				{
					retrievedData[i] = rs.getString(i + 1);
				}
				else if (returnClass == Time.class)
				{
					retrievedData[i] = rs.getTime(i + 1);
				}
				else if (returnClass == Timestamp.class)
				{
					retrievedData[i] = rs.getTimestamp(i + 1);
				}
				else if (returnClass == Date.class)
				{
					retrievedData[i] = rs.getDate(i + 1);
				}
				else if (returnClass == Integer.class)
				{
					retrievedData[i] = new Integer(rs.getInt(i + 1));
				}
			}
			toReturn.add(retrievedData);
		}
		Statement s = rs.getStatement();
		s.close(); // s closes rs

		return toReturn;
	}

	/**
	 * Defaults the dummy variable to String[], i.e. the return value is set to
	 * be a List of String objects
	 * 
	 * @see #getObjectsByVariables(String , Object[], Object[])
	 */
	protected SortedSet getObjectsByVariables(String sql, Object[] objects)
			throws SQLException
	{
		return getObjectsByVariables(sql, objects, new String[0]);
	}

	/**
	 * Utility function to get a result set from a SQL query string. Exemple:
	 * 
	 * <code>ResultSet rs = getResultSetByVariables("SELECT object_name FROM user_objects WHERE object_name = UPPER(?)",new String("xxx");</code>
	 * 
	 * @param sql
	 *            SQL to execute.
	 * @param objects
	 *            The objects to be used as bind parameter in the where clause.
	 * @return A ResultSet that can be scrolled through and examined to get the
	 *         results.
	 * @throws SQLException
	 *             If the sql is malformed or there is a problem with the
	 *             database connection.
	 */
	protected ResultSet getResultSetByVariables(String sql, Object[] objects)
			throws SQLException
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
					s.setInt(i + 1, ((Integer) objects[i]).intValue()); // can
																		// throw
																		// an
																		// exception
																		// if
					// null
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

	/**
	 * Release the connection. Must be called when done with the Connection
	 * object returned by getConnection
	 * 
	 * @param c
	 *            The Connection object
	 */
	private void releaseConnection(Connection c)
	{
		if (myDefaultConnectionPool != null)
		{
			myDefaultConnectionPool.free(c);
		}
		// else don't release the only connection i have
	}

	/**
	 * Returns our internal representation of packageName to be used as the key
	 * to the HashMaps. In reality just concatenates the two together.
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
	 * This method returns the list of currently known schema names. The first
	 * time it is called, it will look the schema names up in the database. On
	 * subsequent calls, it will only check for new scemas. Note that dropped
	 * schemas will never be removed from the cache for now.
	 * 
	 * @param forceUpdate
	 *            Pass true if you want to force a refresh of all schemas. This
	 *            will cause all packages for all schemas to be reset. Defaults
	 *            to false.
	 * 
	 * @param updateList
	 *            Pass true if you want to check for newly added schemas.
	 *            Default true.
	 * 
	 * @return The list of currently known schema names.
	 * @throws SQLException
	 */
	public SortedSet<String> getSchemas(boolean forceUpdate,
			boolean updateList, int refreshSeconds) throws SQLException
	{
		SortedSet<String> schemaList = new TreeSet<String>();
		SortedSet<String> newSchemaList = new TreeSet<String>();
		String allSchemasSql = "SELECT username " + "FROM all_users u "
				+ "WHERE EXISTS (SELECT p.procedure_name "
				+ "              FROM all_procedures p "
				+ "              WHERE p.owner = u.username)";
		String newSchemasSql = "SELECT username " + "FROM all_users u "
				+ "WHERE u.created >= ? "
				+ "AND EXISTS (SELECT p.procedure_name "
				+ "            FROM all_procedures p "
				+ "            WHERE p.owner = u.username)";

		schemaList.addAll(mySchemaToPackageMap.keySet());
		if (schemaList.isEmpty() || myDBLastCacheTime == null || forceUpdate)
		{
			schemaList = getObjectsByVariables(allSchemasSql, new Object[] {});
			myDBLastCacheTime = getSysTimestamp();
			newSchemaList = schemaList;
		}
		else if (updateList)
		{
			// System.out.println("ZZZ" + getSysTimestamp().getTime() + "|" +
			// myDBLastCacheTime.getTime() + "|" + (refreshSeconds * 1000));
			if (getSysTimestamp().getTime() > myDBLastCacheTime.getTime()
					+ (refreshSeconds * 1000))
			{
				newSchemaList = getObjectsByVariables(newSchemasSql,
						new Object[] { myDBLastCacheTime });
				schemaList.addAll(newSchemaList);
				myDBLastCacheTime = getSysTimestamp();
			}
			// else { System.out.println("Using cached schemas(1)"); }
		}
		// else { System.out.println("Using cached schemas(2)"); }

		// Now add all new schemas to our cache
		for (Iterator it = newSchemaList.iterator(); it.hasNext();)
		{
			String s = (String) it.next();
			mySchemaToPackageMap.put(s, null);
		}
		return schemaList;
	}

	/**
	 * @see #getSchemas(boolean, boolean, int)
	 */
	public SortedSet<String> getSchemas() throws SQLException
	{
		return getSchemas(false, true, 10);
	}

	/**
	 * Loads the segments for the given schema and package into our cache, and
	 * returns the list.
	 * 
	 * @param schemaName
	 *            Schema of package to be loaded.
	 * @param packageName
	 *            Package to be loaded
	 * @return The list of segments that were just loaded.
	 * @throws SQLException
	 */
	protected List<Segment> loadSegments(String schemaName, String packageName)
			throws SQLException
	{
		String sql = "SELECT p.procedure_name " + "      ,p.object_name "
				+ "FROM   all_procedures p " + "      , ("
				+ myViewablePackagesSQL + ") pk "
				+ "WHERE  pk.owner IN (UPPER(?), 'PUBLIC') "
				+ "AND    pk.NAME = UPPER(?) "
				+ "AND    p.owner = pk.package_owner "
				+ "AND    p.object_name = pk.package_name";

		Timestamp cacheTime = getSysTimestamp();
		SortedSet segmentNameList = getObjectsByVariables(sql, new Object[] {
				schemaName, schemaName, schemaName, schemaName, packageName });
		List<Segment> segmentList = new ArrayList<Segment>();
		for (Iterator it = segmentNameList.iterator(); it.hasNext();)
		{
			String segmentName = (String) it.next();
			System.out.println("adding segments for " + segmentName
					+ " in schema " + schemaName + " and package "
					+ packageName);
			// segmentList.add(getSegmentFromDB(schemaName, packageName,
			// segmentName));
			addSegmentsToList(schemaName, packageName, segmentName, segmentList);
		}
		myPackageToSegmentMap.put(getPackageName(schemaName, packageName),
				segmentList);
		myPackageToLastCacheTimeMap.put(
				getPackageName(schemaName, packageName), cacheTime);
		return segmentList;
	}

	/**
	 * Gets the segment representing a function or procedure from the database,
	 * and loads it into the list that is passed in. In most cases, it will only
	 * find one segment to add, but in the case of an overloaded function or
	 * procedure, it will find each overloaded function and add it as a separate
	 * entry.
	 * 
	 * @param schemaName
	 *            The scehma name.
	 * @param packageName
	 *            The package name.
	 * @param segmentName
	 *            The segment name.
	 * @param theList
	 *            The list to add the segments to
	 * @throws SQLException
	 */
	protected void addSegmentsToList(String schemaName, String packageName,
			String segmentName, List<Segment> theList) throws SQLException
	{
		String sql = "SELECT a.argument_name, a.position, a.in_out, a.overload "
				+ "      ,(CASE WHEN a.type_owner IS NULL THEN a.data_type ELSE a.type_owner || '.'|| a.type_name || '.'|| a.type_subname END) data_type "
				+ "      ,(CASE WHEN pk.owner <> pk.package_owner THEN 'YES' ELSE 'NO' END) is_synonym "
				+ "FROM   all_arguments a "
				+ "      , ("
				+ myViewablePackagesSQL
				+ ") pk "
				+ "WHERE  pk.owner IN (UPPER(?), 'PUBLIC') "
				+ "AND    pk.NAME = UPPER(?) "
				+ "AND    a.owner = pk.package_owner "
				+ "AND    a.package_name = pk.package_name "
				+ "AND    a.OBJECT_NAME = UPPER(?) "
				+ "AND    a.DATA_LEVEL = 0 "
				+ "ORDER BY a.overload, a.position";
		// System.out.println(sql);
		SegmentType segmentType = SegmentType.Procedure;
		String segmentReturnType = null;
		Segment segment = null;

		ResultSet rs = getResultSetByVariables(sql, new Object[] { schemaName,
				schemaName, schemaName, schemaName, packageName, segmentName });

		int prevOverload = 1;
		while (rs.next())
		{
			if (rs.isFirst() || rs.getInt("OVERLOAD") != prevOverload)
			{
				if (!rs.isFirst() && rs.getInt("OVERLOAD") != prevOverload)
				{
					theList.add(segment);
				}
				if (rs.getInt("POSITION") == 0)
				{
					segmentType = SegmentType.Function;
					segmentReturnType = rs.getString("DATA_TYPE");
				}
				segment = new Segment(segmentName, dummyPosition, segmentType);
				segment.setReturnType(segmentReturnType);
				segment.setPublicSynonym(rs.getString("IS_SYNONYM").equals(
						"YES"));
			}
			if (rs.getInt("POSITION") > 0)
			{
				segment.addParameter(rs.getString("ARGUMENT_NAME"), rs
						.getString("IN_OUT"), rs.getString("DATA_TYPE"), "", 0);
			}
			prevOverload = rs.getInt("OVERLOAD");
		}
		theList.add(segment);
		Statement s = rs.getStatement();
		s.close(); // s closes rs
	}

	/**
	 * Returns the segments (procedures and functions) for the given schema and
	 * package. Will make appropriate use of cache, and/or get updates from the
	 * database.
	 * 
	 * @param schemaName
	 *            The schema name.
	 * @param packageName
	 *            The package name.
	 * @param forceUpdate
	 *            Pass true if you want to force it to look at the database.
	 *            Default is <code>false</code>.
	 * @return the list of segments for the given schema and package.
	 * @throws SQLException
	 */
	public List getSegments(String schemaName, String packageName,
			boolean forceUpdate, int refreshSeconds) throws SQLException
	{
		String lastPackageDDLDateSQL = "SELECT last_ddl_time "
				+ "FROM   all_objects o "
				+ // was
				// (" +
				// myViewablePackagesSQL
				// + ")
				"WHERE  o.owner = UPPER(?) "
				+ "AND    o.object_name = UPPER(?) ";
		String lastPublicPackageDDLDateSQL = "SELECT last_ddl_time "
				+ "FROM   (" + myViewablePackagesSQL + ") o "
				+ // was
				"WHERE  o.owner IN (UPPER(?),'PUBLIC') "
				+ "AND    o.name = UPPER(?) ";

		List segmentList = (List) myPackageToSegmentMap.get(getPackageName(
				schemaName, packageName));
		if (segmentList == null || forceUpdate)
		{
			segmentList = loadSegments(schemaName, packageName);
		}
		else if (getSysTimestamp().getTime() > ((java.util.Date) myPackageToLastCacheTimeMap
				.get(getPackageName(schemaName, packageName))).getTime()
				+ (refreshSeconds * 1000))
		{
			Timestamp lastCacheTime = (Timestamp) myPackageToLastCacheTimeMap
					.get(getPackageName(schemaName, packageName));
			SortedSet lastUpdateTimeList;
			if (segmentList.size() > 0
					&& ((Segment) segmentList.get(0)).isPublicSynonym())
			{
				lastUpdateTimeList = getObjectsByVariables(
						lastPublicPackageDDLDateSQL,
						new Object[] { schemaName, schemaName, schemaName,
								schemaName, packageName }, new Timestamp[0]);
			}
			else
			{
				lastUpdateTimeList = getObjectsByVariables(
						lastPackageDDLDateSQL, new Object[] { schemaName,
								packageName }, new Timestamp[0]);
			}
			if (lastUpdateTimeList.size() == 0
					|| ((java.util.Date) lastUpdateTimeList.first())
							.after(lastCacheTime))
			{
				segmentList = loadSegments(schemaName, packageName);
			}
		}

		// else { System.out.println("Using cached segments"); }

		return segmentList;
	}

	/**
	 * @see #getSegments(String, String, boolean, int)
	 */
	public List getSegments(String schemaName, String packageName)
			throws SQLException
	{
		return getSegments(schemaName, packageName, false, 10);
	}

	/**
	 * Gets all packages in the given schema. Will make appropriate use of
	 * cache, and/or get updates from the database.
	 * 
	 * @param schemaName
	 *            The <b>owning</b> schema name. i.e if you qualify a call with
	 *            this schema name you can access the returned packages. N.B
	 *            there is one deviation from this, which is that specifying the
	 *            <code>schema</code> as <code>PUBLIC </code> will cause the
	 *            return of all the public packages.
	 * @param forceUpdate
	 *            Pass true if you want to force it to look at the database.
	 *            Default is <code>false</code>.
	 * @return The list of packages for the given schema.
	 * @throws SQLException
	 */
	public SortedSet getPackages(String schemaName, boolean forceUpdate,
			int refreshSeconds, boolean isExpectingPublicSchemas)
			throws SQLException
	{

		String allPackagesSQL = "SELECT p.name " + // for synonyms, this is the
													// synonym
				"      ,p.owner " + // for synonyms, this is the synonym-owner,
									// i.e. PUBLIC
				"FROM (" + myViewablePackagesSQL + ") p ";

		String newPackagesSQL = "SELECT p.name "
				+ // for synonyms, this is the synonym
				"      ,p.owner "
				+ // for synonyms, this is the synonym-owner, i.e. PUBLIC
				"FROM (" + myViewablePackagesSQL + ") p "
				+ "WHERE p.last_ddl_time >= ?";

		SortedSet packageList = (SortedSet) mySchemaToPackageMap
				.get(schemaName);
		SortedSet newPackageList = null;
		Timestamp newCacheTime = getSysTimestamp(); // get cache Timestamp
													// before selects so that
		// reported cache Timestamp is no later than
		// actual cache Timestamp

		// TODO should make this more efficient, by storing a public view and an
		// ALL view
		boolean lastPublicSchemaBooleanForThisSchemaWasDifferent = false;
		Boolean lastPublicSchemaRequestType = (Boolean) mySchemaToLastPublicSchemaRequestBoolean
				.get(schemaName);
		lastPublicSchemaBooleanForThisSchemaWasDifferent = (lastPublicSchemaRequestType == null || lastPublicSchemaRequestType
				.booleanValue() != isExpectingPublicSchemas);

		if (packageList == null || forceUpdate
				|| lastPublicSchemaBooleanForThisSchemaWasDifferent)
		{
			if (isExpectingPublicSchemas)
			{
				packageList = getObjectsByVariables(allPackagesSQL,
						new Object[] { schemaName, schemaName, schemaName });
			}
			else
			{
				List resultingList = getObjectSetsByVariables(allPackagesSQL,
						new Object[] { schemaName, schemaName, schemaName },
						new Object[] { new String[0], new String[0] });
				packageList = new TreeSet();
				for (Iterator it = resultingList.iterator(); it.hasNext();)
				{
					Object[] data = (Object[]) it.next();
					if (!data[1].equals("PUBLIC"))
					{
						packageList.add(data[0]);
					}
				}
			}
			mySchemaToPackageMap.put(schemaName, packageList);
			mySchemaToLastCacheTimeMap.put(schemaName, newCacheTime);
		}
		else if (getSysTimestamp().getTime() > ((java.util.Date) mySchemaToLastCacheTimeMap
				.get(schemaName)).getTime()
				+ (refreshSeconds * 1000))
		{
			if (isExpectingPublicSchemas)
			{
				newPackageList = getObjectsByVariables(newPackagesSQL,
						new Object[] { schemaName, schemaName, schemaName,
								mySchemaToLastCacheTimeMap.get(schemaName) });
			}
			else
			{
				List resultingList = getObjectSetsByVariables(allPackagesSQL,
						new Object[] { schemaName, schemaName, schemaName },
						new Object[] { new String[0], new String[0] });
				newPackageList = new TreeSet();
				for (Iterator it = resultingList.iterator(); it.hasNext();)
				{
					Object[] data = (Object[]) it.next();
					if (!data[1].equals("PUBLIC"))
					{
						newPackageList.add(data[0]);
					}
				}
			}
			if (!newPackageList.isEmpty())
			{
				packageList.addAll(newPackageList);
				// Shouldn't have to call because packageList should be a
				// pointer not a copy
				mySchemaToPackageMap.put(schemaName, packageList);
				mySchemaToLastCacheTimeMap.put(schemaName, newCacheTime);
			}
		}
		mySchemaToLastPublicSchemaRequestBoolean.put(schemaName, new Boolean(
				isExpectingPublicSchemas));

		// else { System.out.println("Using cached packages"); }

		return packageList;
	}

	/**
	 * @see #getPackages(String, boolean, int, boolean)
	 */
	public SortedSet getPackages(String schemaName,
			boolean isExpectingPublicSchemas) throws SQLException
	{
		return getPackages(schemaName, false, 10, isExpectingPublicSchemas);
	}

	/**
	 * Gets the source code, without the initial "CREATE OR REPLACE" at the
	 * beginning
	 * 
	 * @param schemaName
	 *            The name of the schema whose source is sought.
	 * @param sourceName
	 *            The name of the source to retrieve
	 * @param type
	 *            the type of the source - can be
	 *            <ul>
	 *            <li>PACKAGE - this will have the package name as the
	 *            <code>sourceName</code></li>
	 *            <li>PACKAGE BODY - this will have the package name as the
	 *            <code>sourceName</code></li>
	 *            <li>PROCEDURE - this will have the procedure name as the
	 *            <code>sourceName</code></li>
	 *            <li>FUNCTION - this will have the function name as the
	 *            <code>sourceName</code></li>
	 *            <li>TYPE - this will have the type name as the
	 *            <code>sourceName</code></li>
	 *            <li>TYPE BODY - this will have the type name as the
	 *            <code>sourceName</code></li>
	 *            <li>TRIGGER - this will have the trigger name as the
	 *            <code>sourceName</code></li>
	 *            </ul>
	 * @return the source code, without the initial "CREATE OR REPLACE" at the
	 *         beginning
	 * @throws SQLException
	 */
	public String getSource(String schemaName, String sourceName, String type)
			throws SQLException
	{
		StringBuffer sourceCode = new StringBuffer(1000);
		String sql = "SELECT text " + "FROM all_source s "
				+ "WHERE  owner IN (UPPER(?),'PUBLIC') "
				+ "AND    s.TYPE = upper(?) " + "AND    s.NAME = UPPER(?) "
				+ "ORDER BY s.line ";
		ResultSet rs = getResultSetByVariables(sql, new Object[] { schemaName,
				type, sourceName });

		while (rs.next())	
		{
			sourceCode.append(rs.getString(1));
		}
		return sourceCode.toString();
	}
}
