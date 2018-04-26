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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import au.com.gts.data.Column;
import au.com.gts.data.Constraint;
import au.com.gts.data.DbType;
import au.com.gts.data.ForeignKeyConstraint;
import au.com.gts.data.Function;
import au.com.gts.data.Grant;
import au.com.gts.data.PrimaryKeyConstraint;
import au.com.gts.data.Procedure;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;
import au.com.gts.data.Trigger;

/**
 * This type represents a data gatherer which can construct a software
 * representation of a database schema and associated tables, columns and
 * constraints.
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
	private static final Logger logger = Logger
			.getLogger(DBMetaDataGatherer.class.getName());

	protected Map<String, Schema> mySchemas;
	protected IProject myProject;

	private DatabaseMetaData myDBMetadata;

	protected DBMetaDataGatherer(IProject project)
	{
		myProject = project;
		mySchemas = new HashMap<String, Schema>();
	}

	/**
	 * This constructor creates the metadata gatherer with a connection and the
	 * name of the schema it should be looking at. Technically its best if the
	 * connection is based on the supplied schema.
	 * 
	 * @param schemaName
	 *            The name of the schema to query.
	 * 
	 * @param conn
	 *            The connection to query concerning the given
	 *            <code>schemaName</code>.
	 * 
	 * @throws IllegalStateException
	 *             when the supplied connection is null.
	 */
	public DBMetaDataGatherer(IProject project, Connection conn)
	{
		this(project);
		initConnectionData(conn, null);
	}

	/**
	 * This constructor creates the metadata gatherer with a connection and the
	 * name of the schema it should be looking at. It uses a connection obtained
	 * from the DbUtility which attempts to look up the connection from
	 * configuration.
	 * 
	 * @param schemaName
	 *            The name of the schema to query.
	 * @throws SQLException
	 * 
	 * @throws IllegalStateException
	 *             when a connection cannot be obtained.
	 */
	public DBMetaDataGatherer(IProject project, String primerSchema) throws SQLException
	{
		this(project);

		Connection conn = DbUtility.getDbaConnectionPool(project)
				.getConnection();

		try
		{
			if (conn != null)
			{
				initConnectionData(conn, primerSchema);
			}
			else
			{
				final String msg = "Cannot obtain db connection for project ["
						+ project.getName() + "]";
				logger.warning(msg);
				throw new IllegalStateException(msg);
			}
		}
		finally
		{
			if (conn != null)
			{
				DbUtility.getDbaConnectionPool(project).free(conn);
			}
		}
	}

	/**
	 * This method initialises the meta data details using the supplied
	 * <code>connection</code>.
	 * 
	 * @param conn
	 *            The connection whose meta data will be examined.
	 * 
	 * @param primerSchema
	 *            The first schema to load
	 */
	private void initConnectionData(Connection conn, String primerSchema)
			throws IllegalStateException
	{
		if (conn == null)
		{
			throw new IllegalStateException("The supplied connection is null");
		}
		try
		{
			myDBMetadata = conn.getMetaData();
			String catalog = null;
			String tblNamePattern = null;
			String schemaPattern = primerSchema;
			constructTables(catalog, schemaPattern, tblNamePattern);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public List<Schema> getSchemas()
	{
		return Collections.list(Collections.enumeration(mySchemas.values()));
	}

	/**
	 * This method gets the schema that was stored specifically against this
	 * meta gatherer on construction that has the supplied <code>name</code>. In
	 * general there is only one schema stored against a meta data gatherer -
	 * and this should be {@link #mySchemaName}.
	 * 
	 * @param name
	 *            The name of the schema sought.
	 * 
	 * @return The schema object sought.
	 */
	public Schema getSchema(String name)
	{
		getSchemas();
		Schema schema = mySchemas.get(name);
		if (schema == null)
		{
			schema = mySchemas.get(name.toUpperCase());
		}
		return schema;
	}

	/**
	 * This method constructs the tables and all of their associated data given
	 * the supplied catalog, schema pattern and table pattern.
	 * 
	 * @param dbmd
	 *            The meta data from which the constraints are generated.
	 * 
	 * @param catalog
	 *            The catalog from which to find the tables and related data.
	 * 
	 * @param schemaPattern
	 *            The schemas to include in the search.
	 * 
	 * @param tblNamePattern
	 *            The pattern of tables to include in the search.
	 * 
	 * @throws SQLException
	 * 
	 * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String[])
	 */
	public List<Table> constructTables(String catalog, String schemaPattern,
			String tblNamePattern) throws SQLException
	{
		String[] tableTypes = new String[] { "TABLE", "VIEW" };
		ResultSet rs = myDBMetadata.getTables(catalog, schemaPattern,
				tblNamePattern, tableTypes);
		// printMetaData(rs);
		List<Table> tables = new ArrayList<Table>();
		while (rs.next())
		{
			String catalogName = rs.getString("TABLE_CAT");
			String schemaName = rs.getString("TABLE_SCHEM");
			String tableName = rs.getString("TABLE_NAME");
			String tableType = rs.getString("TABLE_TYPE");
			String remarks = rs.getString("REMARKS");

			Schema schema = Schema.getSchema(this, catalogName, schemaName);
			mySchemas.put(schemaName, schema);
			Table table = new Table(this);
			if (!Pattern.matches("BIN\\$.*", tableName))
			{
				table.setName(tableName);
				schema.addTable(table);
				tables.add(table);
				table.setType(tableType);
				table.setRemarks(remarks);
				logger.info("TableName is " + tableName + "\n");
				constructColumns(table);
			}
		} // end construct tables

		Schema[] schemas = mySchemas.values().toArray(
				new Schema[mySchemas.size()]);

		for (int i = 0; i < schemas.length; i++)
		{
			Map<String, Table> tablesMap = schemas[i].getTables();
			for (Table table : tablesMap.values())
			{
				setForeignKeys(catalog, table);
				setPrimaryKeys(catalog, table);
			}
		}
		return tables;
	}

	/**
	 * This method sets the primary keys on the specified table.
	 * 
	 * @param dbmd
	 *            The meta data from which the constraints are generated.
	 * 
	 * @param catalog
	 *            The catalog from which to find the tables and related data.
	 * 
	 * @param table
	 *            The table whose primary keys are going to be discovered.
	 * 
	 * @see DatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	private void setPrimaryKeys(String catalog, Table table)
			throws SQLException
	{
		String schemaName = table.getSchemaName();

		// construct constraints
		ResultSet pKeysRs = myDBMetadata.getPrimaryKeys(catalog, schemaName,
				table.getName());
		// printMetaData(theLogger, fKeysRs);
		while (pKeysRs.next())
		{
			String pkColumnName = pKeysRs.getString("COLUMN_NAME");
			String pkName = pKeysRs.getString("PK_NAME");
			short pkSeqNumber = pKeysRs.getShort("KEY_SEQ");

			Column pkColumn = table.getColumn(pkColumnName);
			if (pkColumn == null)
			{
				throw new RuntimeException("Column [" + pkColumnName
						+ "] missing from table [" + table.getName() + "]");
			}
			createPkConstraint(table, pkColumn, pkName, pkSeqNumber);
		}
	}

	protected void createPkConstraint(Table table, Column pkColumn,
			                          String pkName, short pkSeqNumber)
	{
		// TODO consider putting a single primary key constraint that owns
		// all columns in the pk
		Constraint pkCons = new PrimaryKeyConstraint(pkName, pkColumn,
				pkSeqNumber);
		pkColumn.setIsPrimaryKey(true);
		pkColumn.addConstraint(pkCons);
	}

	/**
	 * This method constructs the foreign key constraints for a given table and
	 * then adds them to that table. It assumes that all referred columns are in
	 * tables within the same schema.
	 * 
	 * @param dbmd
	 *            The meta data from which the constraints are generated.
	 * 
	 * @param catalog
	 *            The catalog from which to find the constraints.
	 * 
	 * @param table
	 *            The table whose constraints are being generated.
	 * 
	 * @throws SQLException
	 *             When a database error occurs.
	 */
	private void setForeignKeys(String catalog, Table table)
			throws SQLException
	{
		Schema schema = table.getSchema();

		// construct constraints
		ResultSet fKeysRs = myDBMetadata.getExportedKeys(catalog, schema
				.getName(), table.getName());
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
			
			Table fkTable = schema.getTable(fkTableName);
			
			Column fkColumn = fkTable.getColumn(fkColumnName);
			if (fkColumn == null)
			{
				throw new RuntimeException("Column [" + fkColumnName
						+ "] missing from table [" + fkTable.getName() + "]");
			}
			createFkConstraint(table, fkTable, fkColumn,
					pkColumnName, updateRule, deleteRule, fKeyName);
		}
	}

	/**
	 * @param table
	 * @param fkTableName
	 * @param fkColumnName
	 * @param pkColumnName
	 * @param updateRule
	 * @param deleteRule
	 * @param fKeyName
	 */
	protected void createFkConstraint(Table table,
			Table fkTable, Column fkColumn, String pkColumnName,
			String updateRule, String deleteRule, String fKeyName)
	{
		Column pkColumn = table.getColumn(pkColumnName);
		if (pkColumn == null)
		{
			throw new RuntimeException("Column [" + pkColumnName
					+ "] missing from table [" + table.getName() + "]");
		}
		Constraint fkCons = new ForeignKeyConstraint(fKeyName, pkColumn,
				fkColumn);
		fkCons.setUpdateRule(updateRule);
		fkCons.setDeleteRule(deleteRule);
		fkColumn.addConstraint(fkCons);
	}

	/**
	 * This method constructs all the columns for a given table and adds them to
	 * the supplied table.
	 * 
	 * @param dbmd
	 *            The meta data from which the constraints are generated.
	 * 
	 * @param catalogName
	 *            The catalog from which to find the columns.
	 * 
	 * @param table
	 *            The table whose columns are being generated.
	 * 
	 * @throws SQLException
	 */
	public void constructColumns(Table table) throws SQLException
	{
		ResultSet columnRs = myDBMetadata.getColumns(null, table
				.getStrippedSchemaName(), table.getName(), null);
		List<Column> columnList = new ArrayList<Column>();
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

	public List<Trigger> constructTriggers(String schemaName) throws SQLException
	{
		return null;
	}

	public List<Function> constructFunctions(String schemaName) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Procedure> constructProcedures(String schemaName) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<DbType> constructTypes(String schemaName) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Grant> getGrants(String schemaName, String objectName) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
