package plsqleditor.stores;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import plsqleditor.db.DBMetaDataGatherer;
import plsqleditor.db.OracleDbMetaDataGatherer;
import au.com.gts.data.Grant;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;

/**
 * @author Toby Zines
 *
 */
public class TableStore
{
    private DBMetaDataGatherer myDbMetaGatherer;

    /**
     * This constructor creates the store using the supplied connection to retrieve
     * table details.
     */
    public TableStore(IProject project)
    {
        try
		{
			myDbMetaGatherer = new OracleDbMetaDataGatherer(project);
		}
		catch (SQLException e)
		{
			throw new IllegalStateException("Failed to initialise db meta data gatherer", e);
		}
    }

    /**
     * This method returns all the schemas that have been individually queried in the database.
     * 
     * @return all the schemas (queried so far) in the database (that contain tables).
     */
    public Schema [] getSchemas()
    {
        return Schema.getSchemas(myDbMetaGatherer);
    }
    
    /**
     * This method returns the tables for a schema identified by the given schema name.
     * 
     * @param schemaName The name of the schema whose tables are required.
     * 
     * @return the tables for a schema identified by the given schema name. It may be zero
     *         length.
     */
    public Table [] getTables(String schemaName)
    {
    	Schema schema = myDbMetaGatherer.getSchema(schemaName);
        if (schema != null)
        {
            Map<String, Table> tableMap = schema.getTables();
            return tableMap.values().toArray(new Table[tableMap.size()]);
        }
        return new Table[0];
    }

	public Table getTable(String schemaName, String tableName)
	{
		Schema schema = myDbMetaGatherer.getSchema(schemaName);
        if (schema != null)
        {
        	return schema.getTables().get(tableName);
        }
		return null;
	}

	public Schema getSchema(String schema)
	{
		return myDbMetaGatherer.getSchema(schema);
	}
	
	public List<Grant> getGrants(String schemaName, String objectName)
	{
		try
		{
			return myDbMetaGatherer.getGrants(schemaName, objectName);
		}
		catch (SQLException e)
		{
			throw new IllegalStateException("Failed to get grants for [" + schemaName + "." + objectName + "]", e);
		}
	}
}
