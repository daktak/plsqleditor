/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plsqleditor.db.DBMetaDataGatherer;
import plsqleditor.db.OracleDbMetaDataGatherer;

/**
 * This type represents
 * 
 * Created on 21/06/2003
 * 
 * @author Toby Zines
 */
public class Schema extends DatabaseEntity implements Serializable
{
	/**
	 * This is the serial version uid.
	 */
	private static final long serialVersionUID = 4050478997986883637L;

	private static final String DOT = ".";

	private static Map<DBMetaDataGatherer, Map<String, Schema>> theGathererToSchemaMapMap = new HashMap<DBMetaDataGatherer, Map<String, Schema>>();

	/**
	 * This field represents the name of the schema as it is represented in the
	 * database.
	 */
	private String mySchemaName;

	/**
	 * This field represents the full name of the schema as the catalog name
	 * concatenated with the schema name, separated by a DOT.
	 */
	private String myFullName;

	/**
	 * This field represents the name of the catalog that owns this schema.
	 */
	private String myCatalogName;

	private Map<String, Table> myTables;

	private DBMetaDataGatherer myDBMetadataGatherer;

	private HashMap<String, Trigger> myTriggers;

	private HashMap<String, Function> myFunctions;

	private HashMap<String, Procedure> myProcedures;

	private HashMap<String, DbType> myDatabaseTypes;

	/**
	 * This constructor ...
	 * 
	 */
	public Schema(String catalogName, String schemaName, String fullName,
			DBMetaDataGatherer dbmdg)
	{
		myDBMetadataGatherer = dbmdg;
		myCatalogName = catalogName;
		mySchemaName = schemaName;
		myFullName = fullName;
	}

	/**
	 * This method returns all the schemas stored in {@link #theSchemas}table.
	 * 
	 * @return All the stored schemas
	 */
	public static Schema[] getSchemas(DBMetaDataGatherer dbmdg)
	{
		Map<String, Schema> schemaMap = getSchemaMap(dbmdg);
		return schemaMap.values().toArray(new Schema[schemaMap.size()]);
	}

	private static Map<String, Schema> getSchemaMap(DBMetaDataGatherer dbmdg)
	{
		Map<String, Schema> schemaMap = theGathererToSchemaMapMap.get(dbmdg);
		if (schemaMap == null)
		{
			schemaMap = new HashMap<String, Schema>();
			theGathererToSchemaMapMap.put(dbmdg, schemaMap);
			for (Schema schema : dbmdg.getSchemas())
			{
				schemaMap.put(schema.getSchemaName(), schema);
			} 
		}
		return schemaMap;
	}

	/**
	 * This method gets the instance of the schema with the supplied name. If
	 * there is no schema with that name, one will be created and stored in
	 * {@link #theSchemas}.
	 * 
	 * @param catalogName
	 *            The name of the catalog that the schema belong to.
	 * 
	 * @param schemaName
	 *            The name of the schema being sought.
	 * 
	 * @return The schema named by <code>schemaName</code>.
	 */
	public static Schema getSchema(DBMetaDataGatherer dbmdg,
			String catalogName, String schemaName)
	{
		String key = String.valueOf(catalogName).concat(DOT).concat(
				String.valueOf(schemaName));
		Map<String, Schema> schemaMap = getSchemaMap(dbmdg);
		Schema schema = schemaMap.get(key);
		if (schema == null)
		{
			schema = new Schema(catalogName, schemaName, key, dbmdg);
			schemaMap.put(key, schema);
		}
		return schema;
	}

	public String getName()
	{
		return myFullName;
	}

	public void setName(String name)
	{
		throw new UnsupportedOperationException(
				"Cannot call setName on a schema");
	}

	// TODO getProcedures, getFunctions, getTypes (which have type bodies, like Packages have Package bodies)
	
	public Map<String, Function> getFunctions()
	{
		if (myFunctions == null)
		{
			myFunctions = new HashMap<String, Function>();
			List<Function> functions;
			try
			{
				functions = myDBMetadataGatherer.constructFunctions(getSchemaName());
				for (Function function : functions)
				{
					myFunctions.put(function.getName(), function);
					function.setSchemaName(myFullName); 
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myFunctions;
	}

	public Map<String, Procedure> getProcedures()
	{
		if (myProcedures == null)
		{
			myProcedures = new HashMap<String, Procedure>();
			List<Procedure> procedures;
			try
			{
				procedures = myDBMetadataGatherer.constructProcedures(getSchemaName());
				for (Procedure procedure : procedures)
				{
					myProcedures.put(procedure.getName(), procedure);
					procedure.setSchemaName(myFullName); 
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myProcedures;
	}

	public Map<String, DbType> getDatabaseTypes()
	{
		if (myDatabaseTypes == null)
		{
			myDatabaseTypes = new HashMap<String, DbType>();
			List<DbType> procedures;
			try
			{
				procedures = myDBMetadataGatherer.constructTypes(getSchemaName());
				for (DbType dbType : procedures)
				{
					myDatabaseTypes.put(dbType.getName(), dbType);
					dbType.setSchemaName(myFullName);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myDatabaseTypes;
	}
	/**
	 * This method returns the map of table names to tables for this schema.
	 * 
	 * @return The map of table's names to tables within this schema.
	 */
	public Map<String, Trigger> getTriggers()
	{
		if (myTriggers == null)
		{
			myTriggers = new HashMap<String, Trigger>();
			List<Trigger> triggers;
			try
			{
				triggers = myDBMetadataGatherer.constructTriggers(getSchemaName());
				for (Trigger trigger : triggers)
				{
					myTriggers.put(trigger.getName(), trigger);
					trigger.setSchemaName(myFullName); 
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myTriggers;
	}

	/**
	 * This method returns the map of table names to tables for this schema.
	 * 
	 * @return The map of table's names to tables within this schema.
	 */
	public Map<String, Table> getTables()
	{
		if (myTables == null)
		{
			myTables = new HashMap<String, Table>();
			List<Table> tables;
			try
			{
				tables = myDBMetadataGatherer.constructTables(null,
						getSchemaName(), null);
				for (Table table : tables)
				{
					myTables.put(table.getName(), table);
					table.setSchemaName(myFullName);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myTables;
	}

	/**
	 * This method adds a table to the list of tables in this schema.
	 * Don't use this for the {@link OracleDbMetaDataGatherer}.
	 * @param table
	 *            The table to add to this schema.
	 */
	public void addTable(Table table)
	{
		if (myTables == null)
		{
			// for legacy reasons
			myTables = new HashMap<String, Table>();
		}
		myTables.put(table.getName(), table);
		// TODO not sure about full name now
		table.setSchemaName(myFullName);
	}

	/**
	 * This method returns the table owned by this schema with the supplied
	 * name.
	 * 
	 * @param tableName
	 *            The name of the table to return.
	 * 
	 * @return The table of the supplied name, or null if there is no table of
	 *         that name.
	 */
	public Table getTable(String tableName)
	{
		return getTables().get(tableName);
	}

	/**
	 * @return Returns the catalogName.
	 */
	public String getCatalogName()
	{
		return myCatalogName;
	}

	/**
	 * @return Returns the schemaName.
	 */
	public String getSchemaName()
	{
		return mySchemaName;
	}

	/**
	 * This method removes all the Schema Objects stored in {@link #theSchemas}.
	 */
	public static void clearSchemas(DBMetaDataGatherer dbmdg)
	{
		Map<String, Schema> map = getSchemaMap(dbmdg);
		if (map != null)
		{
			map.clear();
		}
	}
	
	public String toString()
	{
		return getName();
	}
	
	public String getDisplayName()
    {
    	return getSchemaName(); 
    }
}