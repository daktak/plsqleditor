/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;

import plsqleditor.db.DBMetaDataGatherer;
import au.com.zinescom.util.UsefulOperations;

/**
 * This type represents a type in a database.
 * 
 * @author Toby Zines
 */
public class DbType extends DatabaseEntity implements Serializable
{
	/**
	 * This is the serial version uid.
	 */
	private static final long serialVersionUID = 3258410621136680247L;

	/** This field represents the SQL type of this table. */
	private String myType;

	/** This field is the schema that this table belongs to. */
	private String mySchemaName;

	private DBMetaDataGatherer myDbMetaDataGatherer;

	/**
	 * This constructor generates a new Table with an empty set of columns.
	 * 
	 * @param name
	 */
	public DbType(DBMetaDataGatherer dbmdg, String name)
	{
		myDbMetaDataGatherer = dbmdg;
		setName(name);
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
	 * @param type
	 */
	public void setType(String type)
	{
		myType = type;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DbType))
		{
			return false;
		}
		DbType proc = (DbType) obj;
		if (!UsefulOperations.objectsAreEqual(proc.getName(), getName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(proc.getSchemaName(),
				getSchemaName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(proc.getType(), getType()))
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
		return getName().hashCode() + getSchemaName().hashCode()
				+ +getType().hashCode();
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
		return "Type " + getName();
	}

	public String getDisplayName()
	{
		return getName();
	}
}