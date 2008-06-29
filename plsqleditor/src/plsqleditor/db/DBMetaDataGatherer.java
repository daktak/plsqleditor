/* ---------------------------------------------------------------------
 * EDIT HISTORY:
 * ---------------------------------------------------------------------
 * Date     DDTS#   Author      Changes/Comments
 * ---------------------------------------------------------------------
 * 19/06/2003          Toby Zines  Created
 * ---------------------------------------------------------------------
 */
package plsqleditor.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import au.com.gts.data.Column;
import au.com.gts.data.Constraint;
import au.com.gts.data.ForeignKeyConstraint;
import au.com.gts.data.PrimaryKeyConstraint;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;

/**
 * This type represents a data gatherer which can construct a software representation of a database schema and
 * associated tables, columns and constraints.
 * 
 * Created on 19/06/2003
 * 
 * @author Toby Zines
 */
public class DBMetaDataGatherer
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(DBMetaDataGatherer.class.getName());

    private String              mySchemaName;
    private Map                 mySchemas;

    /**
     * This constructor creates the metadata gatherer with a connection and the name of the schema it should be looking
     * at. Technically its best if the connection is based on the supplied schema.
     * 
     * @param schemaName The name of the schema to query.
     * 
     * @param conn The connection to query concerning the given <code>schemaName</code>.
     * 
     * @throws IllegalStateException when the supplied connection is null.
     */
    public DBMetaDataGatherer(IProject project, String schemaName, Connection conn)
    {
        mySchemaName = schemaName;
        mySchemas = new HashMap();
        initConnectionData(conn);
    }

    /**
     * This constructor creates the metadata gatherer with a connection and the name of the schema it should be looking
     * at. It uses a connection obtained from the DbUtility which attempts to look up the connection from configuration.
     * 
     * @param schemaName The name of the schema to query.
     * @throws SQLException
     * 
     * @throws IllegalStateException when a connection cannot be obtained.
     */
    public DBMetaDataGatherer(IProject project, String schemaName) throws SQLException
    {
        mySchemaName = schemaName;
        mySchemas = new HashMap();
        Connection conn = DbUtility.getSchemaConnection(project, schemaName).connection;

        if (conn != null)
        {
            initConnectionData(conn);
        }
        else
        {
            final String msg = "Cannot obtain connection for schema [" + schemaName + "]";
            logger.warning(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * This method initialises the meta data details using the supplied <code>connection</code>.
     * 
     * @param conn The connection whose meta data will be examined.
     */
    private void initConnectionData(Connection conn) throws IllegalStateException
    {
        if (conn == null)
        {
            throw new IllegalStateException("The supplied connection is null");
        }
        try
        {
            DatabaseMetaData dbmd = conn.getMetaData();
            String catalog = null;
            String schemaPattern = mySchemaName.toUpperCase();
            String tblNamePattern = null;
            constructTables(dbmd, catalog, schemaPattern, tblNamePattern);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method gets the schema that was stored specifically against this meta gatherer on construction that has the
     * supplied <code>name</code>. In general there is only one schema stored against a meta data gatherer - and this
     * should be {@link #mySchemaName}.
     * 
     * @param name The name of the schema sought.
     * 
     * @return The schema object sought.
     */
    public Schema getSchema(String name)
    {
        Schema schema = (Schema) mySchemas.get(name);
        if (schema == null)
        {
            schema = (Schema) mySchemas.get(name.toUpperCase());
        }
        return schema;
    }

    /**
     * This method gets the key schema that was stored specifically against this meta gatherer on construction that has
     * {@link #mySchemaName}.
     * 
     * @return The schema object stored in this gatherer matching the name {@link #mySchemaName}.
     */
    public Schema getSchema()
    {
        return getSchema(mySchemaName);
    }

    /**
     * This method constructs the tables and all of their associated data given the supplied catalog, schema pattern and
     * table pattern.
     * 
     * @param dbmd The meta data from which the constraints are generated.
     * 
     * @param catalog The catalog from which to find the tables and related data.
     * 
     * @param schemaPattern The schemas to include in the search.
     * 
     * @param tblNamePattern The pattern of tables to include in the search.
     * 
     * @throws SQLException
     * 
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    private void constructTables(DatabaseMetaData dbmd, String catalog, String schemaPattern, String tblNamePattern)
            throws SQLException
    {
        String[] tableTypes = new String[]{"TABLE", "VIEW"};
        ResultSet rs = dbmd.getTables(catalog, schemaPattern, tblNamePattern, tableTypes);
        // printMetaData(rs);
        while (rs.next())
        {
            String catalogName = rs.getString("TABLE_CAT");
            String schemaName = rs.getString("TABLE_SCHEM");
            String tableName = rs.getString("TABLE_NAME");
            String tableType = rs.getString("TABLE_TYPE");
            String remarks = rs.getString("REMARKS");

            Schema schema = Schema.getSchema(catalogName, schemaName);
            mySchemas.put(schemaName, schema);
            Table table = new Table();
            if (!Pattern.matches("BIN\\$.*", tableName))
            {
                table.setName(tableName);
                schema.addTable(table);
                table.setType(tableType);
                table.setRemarks(remarks);
                logger.info("TableName is " + tableName + "\n");
                constructColumns(dbmd, catalog, table);
            }
        } // end construct tables


        Schema[] schemas = (Schema[]) mySchemas.values().toArray(new Schema[mySchemas.size()]);

        for (int i = 0; i < schemas.length; i++)
        {
            Map tablesMap = schemas[i].getTables();
            Iterator iter = tablesMap.values().iterator();
            while (iter.hasNext())
            {
                Table table = (Table) iter.next();
                setForeignKeys(dbmd, catalog, table);
                setPrimaryKeys(dbmd, catalog, table);
            }
        }
    }

    /**
     * This method sets the primary keys on the specified table.
     * 
     * @param dbmd The meta data from which the constraints are generated.
     * 
     * @param catalog The catalog from which to find the tables and related data.
     * 
     * @param table The table whose primary keys are going to be discovered.
     * 
     * @see DatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String, java.lang.String)
     */
    private void setPrimaryKeys(DatabaseMetaData dbmd, String catalog, Table table) throws SQLException
    {
        String schemaName = table.getSchemaName();

        // construct constraints
        ResultSet pKeysRs = dbmd.getPrimaryKeys(catalog, schemaName, table.getName());
        // printMetaData(theLogger, fKeysRs);
        while (pKeysRs.next())
        {
            String pkColumnName = pKeysRs.getString("COLUMN_NAME");
            String pkName = pKeysRs.getString("PK_NAME");
            short pkSeqNumber = pKeysRs.getShort("KEY_SEQ");

            Column pkColumn = table.getColumn(pkColumnName);
            if (pkColumn == null)
            {
                throw new RuntimeException("Column [" + pkColumnName + "] missing from table [" + table.getName() + "]");
            }
            Constraint pkCons = new PrimaryKeyConstraint(pkName, pkColumn, pkSeqNumber);
            pkColumn.setIsPrimaryKey(true);
            table.addConstraint(pkCons);
        }
    }

    /**
     * This method constructs the foreign key constraints for a given table and then adds them to that table. It assumes
     * that all referred columns are in tables within the same schema.
     * 
     * @param dbmd The meta data from which the constraints are generated.
     * 
     * @param catalog The catalog from which to find the constraints.
     * 
     * @param table The table whose constraints are being generated.
     * 
     * @throws SQLException When a database error occurs.
     */
    private void setForeignKeys(DatabaseMetaData dbmd, String catalog, Table table) throws SQLException
    {
        Schema schema = table.getSchema();

        // construct constraints
        ResultSet fKeysRs = dbmd.getExportedKeys(catalog, schema.getName(), table.getName());
        // printMetaData(theLogger, fKeysRs);
        while (fKeysRs.next())
        {
            String fkTableName = fKeysRs.getString("FKTABLE_NAME");
            String fkColumnName = fKeysRs.getString("FKCOLUMN_NAME");
            String pkColumnName = fKeysRs.getString("PKCOLUMN_NAME");
            String updateRule = fKeysRs.getString("UPDATE_RULE");
            String deleteRule = fKeysRs.getString("DELETE_RULE");
            String fKeyName = fKeysRs.getString("FK_NAME");
            // String pKeyName = rs.getString("PK_NAME");

            Column pkColumn = table.getColumn(pkColumnName);
            if (pkColumn == null)
            {
                throw new RuntimeException("Column [" + pkColumnName + "] missing from table [" + table.getName() + "]");
            }
            Table fkTable = schema.getTable(fkTableName);
            Column fkColumn = fkTable.getColumn(fkColumnName);
            Constraint fkCons = new ForeignKeyConstraint(fKeyName, pkColumn, fkColumn);
            fkCons.setUpdateRule(updateRule);
            fkCons.setDeleteRule(deleteRule);
            table.addConstraint(fkCons);
            fkTable.addConstraint(fkCons);
        }
    }

    /**
     * This method constructs all the columns for a given table and adds them to the supplied table.
     * 
     * @param dbmd The meta data from which the constraints are generated.
     * 
     * @param catalogName The catalog from which to find the columns.
     * 
     * @param table The table whose columns are being generated.
     * 
     * @throws SQLException
     */
    private void constructColumns(DatabaseMetaData dbmd, String catalogName, Table table) throws SQLException
    {
        ResultSet columnRs = dbmd.getColumns(catalogName, table.getStrippedSchemaName(), table.getName(), null);
        List columnList = new ArrayList();
        while (columnRs.next())
        {
            Column column = new Column();
            String columnName = columnRs.getString("COLUMN_NAME");
            String columnType = columnRs.getString("TYPE_NAME");
            int columnSize = columnRs.getInt("COLUMN_SIZE");
            int decDigits = columnRs.getInt("DECIMAL_DIGITS");
            int precRadix = columnRs.getInt("NUM_PREC_RADIX");
            boolean isNullable = columnRs.getBoolean("NULLABLE");

            column.setName(columnName);
            column.setSQLTypeName(columnType);
            column.setSize(columnSize);
            column.setNumDecimalDigits(decDigits);
            column.setPrecisionRadix(precRadix);
            column.setNullable(isNullable);

            logger.info(column.toString());
            columnList.add(column);
        }
        columnRs.close();
        table.setColumns(columnList);
    }
}
