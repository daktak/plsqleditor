package plsqleditor.stores;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import plsqleditor.db.DBMetaDataGatherer;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;

/**
 * @author Toby Zines
 *
 */
public class TableStore
{
    private Map myDbMetaGatherers;
    private IProject myProject;

    /**
     * This constructor creates the store using the supplied connection to retrieve
     * table details.
     */
    public TableStore(IProject project)
    {
        myProject = project;
        myDbMetaGatherers = new HashMap();
    }

    /**
     * This method returns all the schemas that have been individually queried in the database.
     * 
     * @return all the schemas (queried so far) in the database (that contain tables).
     */
    public Schema [] getSchemas()
    {
        return Schema.getSchemas();
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
        DBMetaDataGatherer mdg =  (DBMetaDataGatherer) myDbMetaGatherers.get(schemaName);
        Schema schema = null;
        if (mdg == null)
        {
            try
            {
                mdg = new DBMetaDataGatherer(myProject, schemaName);
                myDbMetaGatherers.put(schemaName,mdg);
                schema = mdg.getSchema();
            }
            catch (IllegalStateException e)
            {
                // do nothing
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            schema = mdg.getSchema(schemaName);
        }
        if (schema != null)
        {
            Map tableMap = schema.getTables();
            return (Table[]) tableMap.values().toArray(new Table[tableMap.size()]);
        }
        return new Table[0];
    }
}
