/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import au.com.zinescom.util.UsefulOperations;

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
    private static final long          serialVersionUID = 4050478997986883637L;

    private static final String        DOT              = ".";

    /**
     * This field is a map of all the schemas stored against their name. They are accessible via the call to
     * {@link #getSchema(String, String)}.
     */
    private static Map theSchemas       = new HashMap();

    /**
     * This field represents the name of the schema as it is represented in the database.
     */
    private String                     mySchemaName;

    /**
     * This field represents the full name of the schema as the catalog name concatenated with the schema name,
     * separated by a DOT.
     */
    private String                     myFullName;

    /**
     * This field represents the name of the catalog that owns this schema.
     */
    private String                     myCatalogName;

    private Map         myTables;

    /**
     * This constructor ...
     * 
     */
    public Schema(String catalogName, String schemaName, String fullName)
    {
        myCatalogName = catalogName;
        mySchemaName = schemaName;
        myFullName = fullName;
        myTables = new HashMap();
    }

    /**
     * This method returns all the schemas stored in {@link #theSchemas}table.
     * 
     * @return All the stored schemas
     */
    public static Schema[] getSchemas()
    {
        Object[] schemas = theSchemas.values().toArray();
        return (Schema[]) UsefulOperations.arrayToArray(schemas, Schema.class);
    }

    /**
     * This method gets the instance of the schema with the supplied name. If there is no schema with that name, one
     * will be created and stored in {@link #theSchemas}.
     * 
     * @param catalogName
     *            The name of the catalog that the schema belong to.
     * 
     * @param schemaName
     *            The name of the schema being sought.
     * 
     * @return The schema named by <code>schemaName</code>.
     */
    public static Schema getSchema(String catalogName, String schemaName)
    {
        String key = String.valueOf(catalogName).concat(DOT).concat(String.valueOf(schemaName));
        Schema schema = (Schema) theSchemas.get(key);
        if (schema == null)
        {
            schema = new Schema(catalogName, schemaName, key);
            theSchemas.put(key, schema);
        }
        return schema;
    }

    public String getName()
    {
        return myFullName;
    }

    public void setName(String name)
    {
        throw new UnsupportedOperationException("Cannot call setName on a schema");
    }

    /**
     * This method retusn the map of table names to tables for this schema.
     * 
     * @return The map of table's names to tables within this schema.
     */
    public Map getTables()
    {
        return myTables;
    }

    /**
     * This method adds a table to the list of tables in this schema.
     * 
     * @param table
     *            The table to add to this schema.
     */
    public void addTable(Table table)
    {
        myTables.put(table.getName(), table);
        table.setSchemaName(myFullName);
    }

    /**
     * This method returns the table owned by this schema with the supplied name.
     * 
     * @param tableName
     *            The name of the table to return.
     * 
     * @return The table of the supplied name, or null if there is no table of that name.
     */
    public Table getTable(String tableName)
    {
        return (Table) myTables.get(tableName);
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
    public static void clearSchemas()
    {
        theSchemas.clear(); 
    }
}