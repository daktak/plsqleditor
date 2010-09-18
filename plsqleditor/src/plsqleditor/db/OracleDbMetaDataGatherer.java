/**
 * 
 */
package plsqleditor.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import au.com.gts.data.Column;
import au.com.gts.data.Constraint;
import au.com.gts.data.DataConstraint;
import au.com.gts.data.DatabaseEntity;
import au.com.gts.data.DbType;
import au.com.gts.data.Function;
import au.com.gts.data.Grant;
import au.com.gts.data.Procedure;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;
import au.com.gts.data.Trigger;
import au.com.gts.data.UniqueKeyConstraint;

/**
 * @author tzines
 * 
 */
public class OracleDbMetaDataGatherer extends DBMetaDataGatherer
{

	private ConnectionPool myConnectionPool;
	private Connection myConnection;
	private int myConnectionCount = 0;

	public OracleDbMetaDataGatherer(IProject project) throws SQLException
	{
		super(project);
	}

	public List<Trigger> constructTriggers(String schemaName)
			throws SQLException
	{
		String triggerQuery = "select trigger_name, trigger_type, triggering_event, "
				+ "table_owner, base_object_type, table_name, column_name, "
				+ "referencing_names, when_clause, status, "
				+ "action_type from all_triggers " + "where owner = upper(?)";
		List<Trigger> triggers = new ArrayList<Trigger>();
		constructEntities(schemaName, triggerQuery, Trigger.class, triggers);
		return triggers;
	}

	private void constructEntities(String schemaName, String query,
			Class<? extends DatabaseEntity> entityClass,
			List<? extends DatabaseEntity> entities) throws SQLException
	{
		Connection conn = null;
		try
		{
			conn = getConnection();
			PreparedStatement entityStatement = conn.prepareStatement(query);
			entityStatement.setString(1, schemaName);
			ResultSet rs = entityStatement.executeQuery();
			while (rs.next())
			{
				createEntity(rs, schemaName, entityClass, entities);
			} // end construct tables
			rs.close();
			entityStatement.close();
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					freeConnection();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method creates an entity of the right sort (determined by the
	 * supplied <code>entityClass</code>) by extracting the appropriate
	 * information from the supplied result set and adds it to the supplied
	 * <code>entities</code> list.
	 * 
	 * @param rs
	 *            The result set whose data will be extracted to create the
	 *            appropriate object.
	 * @param schemaName
	 *            The name of the schema to add this to.
	 * @param entityClass
	 *            The type of object to create.
	 * @param entities
	 *            The list of objects to add it to.
	 * 
	 * @throws SQLException
	 *             If the result set fails.
	 */
	@SuppressWarnings("unchecked")
	private void createEntity(ResultSet rs, String schemaName,
			Class<? extends DatabaseEntity> entityClass,
			List<? extends DatabaseEntity> entities) throws SQLException
	{
		if (entityClass.equals(Trigger.class))
		{
			List<Trigger> triggerCast = (List<Trigger>) entities;
			String name = rs.getString("TRIGGER_NAME");
			String triggerType = rs.getString("TRIGGER_TYPE");
			String triggeringEvent = rs.getString("TRIGGERING_EVENT");
			String tableOwner = rs.getString("TABLE_OWNER");
			String baseObjectType = rs.getString("BASE_OBJECT_TYPE");
			String tableName = rs.getString("TABLE_NAME");
			String columnName = rs.getString("COLUMN_NAME");
			String referencingNames = rs.getString("REFERENCING_NAMES");
			String whenClause = rs.getString("WHEN_CLAUSE");
			String status = rs.getString("STATUS");
			String actionType = rs.getString("ACTION_TYPE");
			Trigger trigger = new Trigger(this, schemaName, name, triggerType,
					triggeringEvent, tableOwner, baseObjectType, tableName,
					columnName, referencingNames, whenClause, status,
					actionType);
			triggerCast.add(trigger);
		}
		else if (entityClass.equals(Function.class))
		{
			List<Function> functionCast = (List<Function>) entities;
			String name = rs.getString("OBJECT_NAME");
			Function function = new Function(this, name);
			functionCast.add(function);
		}
		if (entityClass.equals(Procedure.class))
		{
			List<Procedure> procCast = (List<Procedure>) entities;
			String name = rs.getString("OBJECT_NAME");
			Procedure proc = new Procedure(this, name);
			procCast.add(proc);
		}
		if (entityClass.equals(DbType.class))
		{
			List<DbType> typeCast = (List<DbType>) entities;
			String name = rs.getString("OBJECT_NAME");
			DbType type = new DbType(this, name);
			typeCast.add(type);
		}
	}

	public List<Grant> getGrants(String schemaName, String objectName) throws SQLException
	{
		String query = "select privilege, grantee, grantor, grantable " +
                     "from user_tab_privs " +
                     "where owner = ? " +
                     "and table_name = ?";
		Connection conn = null;
		List<Grant> grants = new ArrayList<Grant>();
		try
		{
			conn = getConnection();
			PreparedStatement grantStatement = conn.prepareStatement(query);
			grantStatement.setString(1, schemaName.toUpperCase());
			grantStatement.setString(2, objectName.toUpperCase());
			ResultSet rs = grantStatement.executeQuery();
			while (rs.next())	
			{
				String grantor = rs.getString("GRANTOR");
				String grantee = rs.getString("GRANTEE");
				String privilege = rs.getString("PRIVILEGE"); 
				String grantable = rs.getString("GRANTABLE");
				
				Grant g = new Grant(this, schemaName, objectName, grantor, grantee, privilege, grantable);
				grants.add(g);
			}
			rs.close();
			grantStatement.close();
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					freeConnection();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return grants;
	}
	
	public List<Function> constructFunctions(String schemaName)
			throws SQLException
	{
		String funcQuery = "select object_name from all_procedures "
				+ "where owner = upper(?) and object_type = 'FUNCTION'";
		List<Function> funcs = new ArrayList<Function>();
		constructEntities(schemaName, funcQuery, Function.class, funcs);
		return funcs;
	}

	public List<Procedure> constructProcedures(String schemaName)
			throws SQLException
	{
		String procQuery = "select object_name from all_procedures "
				+ "where owner = upper(?) and object_type = 'PROCEDURE'";
		List<Procedure> procs = new ArrayList<Procedure>();
		constructEntities(schemaName, procQuery, Procedure.class, procs);
		return procs;
	}

	public List<DbType> constructTypes(String schemaName) throws SQLException
	{
		String typeQuery = "select object_name from all_procedures "
				+ "where owner = upper(?) and object_type = 'TYPE'";
		List<DbType> types = new ArrayList<DbType>();
		constructEntities(schemaName, typeQuery, DbType.class, types);
		return types;
	}

	public List<Schema> getSchemas()
	{
		if (mySchemas.isEmpty())
		{
			String schemaQuery = "select username from all_users";
			Connection conn = null;
			try
			{
				conn = getConnection();
				PreparedStatement schemaStatement = conn
						.prepareStatement(schemaQuery);
				ResultSet rs = schemaStatement.executeQuery();
				while (rs.next())
				{
					String schemaName = rs.getString("USERNAME");
					Schema schema = Schema.getSchema(this, null, schemaName);
					mySchemas.put(schemaName, schema);
				}
				rs.close();
				schemaStatement.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (conn != null)
					{
						freeConnection();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return Collections.list(Collections.enumeration(mySchemas.values()));
	}
	
	/**
	 * @return
	 * @throws SQLException
	 */
	private synchronized Connection getConnection() throws SQLException
	{
		if (myConnectionPool == null)
		{
			myConnectionPool = DbUtility.getDbaConnectionPool(myProject);
		}
		if (myConnection == null)
		{
			myConnection = myConnectionPool.getConnection();
			myConnectionCount++;
		}
		else
		{
			myConnectionCount++;
		}
		return myConnection;
	}

	/**
	 * @param conn
	 */
	private void freeConnection()
	{
		if (--myConnectionCount <= 0)
		{
			myConnectionCount = 0;
			myConnectionPool.free(myConnection);
			myConnection = null;
		}
	}

	/**
	 * This creates the tables and retusn the list o fthem.
	 * 
	 * @param catalog
	 *            not used
	 * @param schemaPattern
	 *            the schema whose tables should be created. If this is null
	 *            then all schemas will be used.
	 * @param tblNamePattern
	 *            The table to search for. Null means all.
	 */
	public List<Table> constructTables(String catalog, String schemaPattern,
			String tblNamePattern) throws SQLException
	{
		// get the tables
		String tableQuery = "select all_t.owner, all_t.table_name, "
				+ "all_t.status, all_t.pct_free, all_t.pct_used, atc.comments "
				+ "from all_tables all_t, all_tab_comments atc "
				+ "where all_t.owner = atc.owner "
				+ "and all_t.table_name = atc.table_name";
		if (schemaPattern != null)
		{
			tableQuery += " and atc.owner = ?";
		}
		if (tblNamePattern != null)
		{
			tableQuery += " and atc.table_name = ?";
		}

		// get the views?
		// String viewQuery =
		// "select view_name, text_length, text from user_views";
		Connection conn = null;
		List<Table> tables = new ArrayList<Table>();
		try
		{
			conn = getConnection();
			PreparedStatement tableStatement = conn
					.prepareStatement(tableQuery);
			tableStatement.setFetchSize(100);
			int preparedStatmentIndex = 1;
			if (schemaPattern != null)
			{
				tableStatement.setString(preparedStatmentIndex++, schemaPattern
						.toUpperCase());
			}
			if (tblNamePattern != null)
			{
				tableStatement.setString(preparedStatmentIndex++,
						tblNamePattern.toUpperCase());
			}
			ResultSet rs = tableStatement.executeQuery();
			while (rs.next())
			{
				// String schemaName = rs.getString("OWNER");
				String tableName = rs.getString("TABLE_NAME");
				// String tableSpaceName = rs.getString("TABLESPACE_NAME");
				String tableType = "TABLE";
				String remarks = rs.getString("COMMENTS");

				Table table = new Table(this);
				if (!Pattern.matches("BIN\\$.*", tableName))
				{
					table.setName(tableName);
					table.setType(tableType);
					table.setRemarks(remarks);
					tables.add(table);
					System.out.println("TableName is " + tableName + "\n");
				}
			} // end construct tables
			rs.close();
			tableStatement.close();
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					freeConnection();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return tables;
	}

	public void constructColumns(Table table)
	{
		// get the columns
		String columnsQuery = "select owner, table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id "
				+ "from all_tab_columns "
				+ "where owner = ? and table_name = ?";
		Connection conn = null;
		List<Column> columns = new ArrayList<Column>();
		try
		{
			conn = getConnection();
			PreparedStatement columnsStatement = conn
					.prepareStatement(columnsQuery);
			columnsStatement.setString(1, table.getStrippedSchemaName());
			columnsStatement.setString(2, table.getName());
			ResultSet columnRs = columnsStatement.executeQuery();
			while (columnRs.next())
			{
				Column column = new Column();
				String columnName = columnRs.getString("COLUMN_NAME");
				String columnType = columnRs.getString("DATA_TYPE");
				int columnSize = columnRs.getInt("DATA_LENGTH");

				int decDigits = columnRs.getInt("DATA_SCALE");
				int precRadix = columnRs.getInt("DATA_PRECISION");
				String nullable = columnRs.getString("NULLABLE");
				boolean isNullable = (nullable != null && nullable.equals("Y"));

				column.setName(columnName);
				column.setSQLTypeName(columnType);
				column.setSize(columnSize);
				column.setNumDecimalDigits(decDigits);
				column.setPrecisionRadix(precRadix);
				column.setNullable(isNullable);

				System.out.println(column.toString());
				columns.add(column);
			}
			columnRs.close();
			columnsStatement.close();
			table.setColumns(columns);

			// get the constraints
			/**
			 * constraint type can be
			 * <ul>
			 * <li>C - for not null, in a set of strings etc
			 * <li>
			 * <li>R - foreign key constraint</li>
			 * <li>P - primary key</li>
			 * <li>U - unique key</li>
			 * <li>V -</li>
			 * <li>? - Seems to be not nulls on clobs. Maybe</li>
			 * <li>O -</li>
			 * </ul>
			 * Need this to get the type of the constraint
			 */
			// ? list is table name, owner
			// can add search_condition to get the "is not null" etc

			// TODO this uses a group because of bad table joining (between ac,
			// atc and acc). This should be fixed instead of using a group by
			String constraintsQuery = "select count(1), atc.column_name, racc.owner as fk_owner, racc.table_name as fk_table, racc.column_name as fk_column, "
					+ "ac.constraint_name, ac.constraint_type, ac.r_owner, ac.r_constraint_name, acc.position, ac.delete_rule "
					+ "from all_constraints ac, all_tab_columns atc, all_cons_columns acc, all_cons_columns racc "
					+ "where ac.table_name = ? "
					+ "and ac.owner = ? "
					+ "and acc.table_name = ac.table_name "
					+ "and acc.column_name = atc.column_name "
					+ "and ac.constraint_name = acc.constraint_name "
					+ "and atc.table_name = ac.table_name "
					+ "and racc.owner (+) = ac.r_owner "
					+ "and racc.constraint_name (+) = ac.r_constraint_name "
					+ "group by atc.column_name, racc.owner, racc.table_name, racc.column_name, "
					+ "ac.constraint_name, ac.constraint_type, ac.r_owner, ac.r_constraint_name, acc.position, ac.delete_rule";

			PreparedStatement constraintsStatement = conn
					.prepareStatement(constraintsQuery);
			constraintsStatement.setString(1, table.getName().toUpperCase());
			constraintsStatement.setString(2, table.getStrippedSchemaName()
					.toUpperCase());
			ResultSet constraintsRs = constraintsStatement.executeQuery();
			while (constraintsRs.next())
			{
				String columnName = constraintsRs.getString("COLUMN_NAME");
				String constraintName = constraintsRs
						.getString("CONSTRAINT_NAME");
				String constraintType = constraintsRs
						.getString("CONSTRAINT_TYPE");
				short position = constraintsRs.getShort("POSITION");
				String pkTableNameReferredTo = constraintsRs
						.getString("FK_TABLE");
				String pkColumnNameReferredTo = constraintsRs
						.getString("FK_COLUMN");
				String updateRule = "";
				String deleteRule = constraintsRs.getString("DELETE_RULE");
				// String pKeyName =
				// constraintsRs.getString("R_CONSTRAINT_NAME");
				String pkSchema = constraintsRs.getString("R_OWNER");

				Column column = table.getColumn(columnName);
				if (column == null)
				{
					column = new Column();
					column.setName(columnName);
					column.setRemarks("Warning!!! Not loaded from db");
					table.addColumn(column);
				}
				if (constraintType.equals("P"))
				{
					createPkConstraint(table, column, constraintName, position);
				}
				else if (constraintType.equals("R"))
				{
					Table pkTable = Schema.getSchema(this,
							null/* or IGNORE */, pkSchema).getTable(
							pkTableNameReferredTo);
					createFkConstraint(pkTable, table, column,
							pkColumnNameReferredTo, updateRule, deleteRule,
							constraintName);
				}
				else if (constraintType.equals("C"))
				{
					Constraint dataConstraint = new DataConstraint(
							constraintName, column, constraintType,
							"Not Implemented Yet");
					column.addConstraint(dataConstraint);
				}
				else if (constraintType.equals("U"))
				{
					Constraint unqKeyConstraint = new UniqueKeyConstraint(
							constraintName, column);
					column.addConstraint(unqKeyConstraint);
				}
				else
				{
					Constraint dataConstraint = new DataConstraint(
							constraintName, column, constraintType,
							"Not Implemented Yet");
					column.addConstraint(dataConstraint);
				}

			} // end construct columns
			constraintsRs.close();
			constraintsStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new IllegalStateException(e.toString());
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					freeConnection();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
