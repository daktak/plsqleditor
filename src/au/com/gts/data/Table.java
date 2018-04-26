/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import plsqleditor.db.DBMetaDataGatherer;
import au.com.zinescom.util.UsefulOperations;

/**
 * This type represents a table in a database.
 * 
 * Created on 18/06/2003
 * 
 * @author Toby Zines
 */
public class Table extends DatabaseEntity implements Serializable
{
	/**
	 * This is the serial version uid.
	 */
	private static final long serialVersionUID = 3258410621136680247L;

	/** This field represents the SQL type of this table. */
	private String myType;

	/**
	 * This field is the set of the columns within this table.
	 */
	private List<Column> myColumns;

	/** This field is the schema that this table belongs to. */
	private String mySchemaName;

	private DBMetaDataGatherer myDbMetaDataGatherer;

	/**
	 * This constructor generates a new Table with an empty set of columns.
	 */
	public Table(DBMetaDataGatherer dbmdg)
	{
		myDbMetaDataGatherer = dbmdg;
	}

	/**
	 * This returns all the available columns as a Collection.
	 * 
	 * @return {@link #myColumns}.
	 */
	public List<Column> getColumns()
	{
		if (myColumns == null)
		{
			try
			{
				myColumns = new ArrayList<Column>();
				myDbMetaDataGatherer.constructColumns(this);
				for (Column column : myColumns)
				{
					column.setTable(this);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return myColumns;
	}

	/**
	 * This returns the column with the supplied name.
	 * 
	 * @param columnName
	 *            The name of the column desired.
	 * 
	 * @return The named column, or null if there is no column with that name.
	 */
	public Column getColumn(String columnName)
	{
		for (Column column : getColumns())
		{
			if (column.getName().equals(columnName))
			{
				return column;
			}
		}
		return null;
	}

	/**
	 * This method sets a new set of columns onto the Table. In doing so, all
	 * the old columns are removed, and they will no longer reference this
	 * table. Better to only use this in the {@link DBMetaDataGatherer}.
	 * 
	 * @param columns
	 *            The columns to set on this table.
	 */
	public void setColumns(List<Column> columns)
	{
		for (Column column : myColumns)
		{
			column.setTable(null);
		}
		myColumns = columns;

		for (Column column : myColumns)
		{
			column.setTable(this);
		}
	}

	/**
	 * This method adds another column at the end of this table.
	 * 
	 * @param toAdd
	 *            The column to add to this table.
	 */
	public void addColumn(Column toAdd)
	{
		myColumns.add(toAdd);
		toAdd.setTable(this);
	}

	public Schema getSchema()
	{
		Schema[] schemas = Schema.getSchemas(myDbMetaDataGatherer);

		for (int i = 0; i < schemas.length; i++)
		{
			if (schemas[i].getName().equals(mySchemaName))
			{
				return schemas[i];
			}
		}
		return null;
	}

	public String getSchemaName()
	{
		return mySchemaName;
	}

	protected void setSchemaName(String schemaName)
	{
		mySchemaName = schemaName;
	}

	/**
	 * This method sets the type of the particular table.
	 * 
	 * @param tableType
	 */
	public void setType(String tableType)
	{
		myType = tableType;
	}

	/**
	 * This method gets the type of the particular table.
	 * 
	 * @return tableType
	 */
	public String getType()
	{
		return myType;
	}

	/**
	 * This method returns the current list of constraints on all of the owned columns.
	 * 
	 * @return {@link #myConstraints}.
	 */
	public List<Constraint> getConstraints()
	{
		List<Constraint> constraints = new ArrayList<Constraint>();
		for (Column column : getColumns())
		{
			constraints.addAll(column.getConstraints());
		}
		return constraints; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Table))
		{
			return false;
		}
		Table table = (Table) obj;
		if (!UsefulOperations.objectsAreEqual(table.getName(), getName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(table.getSchemaName(),
				getSchemaName()))
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return getName().hashCode() + getSchemaName().hashCode();
	}

	/**
	 * @return The schema name minus the catalog name.
	 */
	public String getStrippedSchemaName()
	{
		return mySchemaName.substring(mySchemaName.indexOf(".") + 1);
	}
	
	public String toString()
	{
		// TODO fix this
		StringBuffer sb = new StringBuffer(getStrippedSchemaName() + "." + getName());
		// + columns
		return sb.toString();
	}
	
	public String getDisplayName()
	{
		return getName();
	}
}