/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;

import plsqleditor.db.DBMetaDataGatherer;
import au.com.zinescom.util.UsefulOperations;

/**
 * This type represents a trigger in a database.
 *
 * Created on 18/06/2003
 *
 * @author Toby Zines
 */
public class Trigger extends DatabaseEntity implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 3001099020764871166L;

	/** This field represents the SQL type of this table. */
	private String myType;

	/** This field is the schema that this table belongs to. */
	private String mySchemaName;

	private DBMetaDataGatherer myDbMetaDataGatherer;

	private String myTriggeringEvent;

	private String myTableOwner;

	private String myBaseObjectType;

	private String myTableName;

	private String myColumnName;

	private String myReferencingNames;

	private String myWhenClause;

	private String myStatus;

	private String myActionType;

	/**
	 * This constructor generates a new Table with an empty set of columns.
	 * @param actionType2
	 */
	public Trigger(DBMetaDataGatherer dbmdg, String schemaName, String name,
			String triggerType, String triggeringEvent, String tableOwner,
			String baseObjectType, String tableName, String columnName,
			String referencingNames, String whenClause, String status,
			String actionType)
	{
		myDbMetaDataGatherer = dbmdg;
		setName(name);
		setSchemaName(schemaName);
		myType = triggerType;
		myTriggeringEvent = triggeringEvent;
		myTableOwner = tableOwner;
		myBaseObjectType = baseObjectType;
		myTableName = tableName;
		myColumnName = columnName;
		myReferencingNames = referencingNames;
		myWhenClause = whenClause;
		myStatus = status;
		myActionType = actionType;
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
		if (!(obj instanceof Trigger))
		{
			return false;
		}
		Trigger trigger = (Trigger) obj;
		if (!UsefulOperations.objectsAreEqual(trigger.getName(), getName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(trigger.getSchemaName(),
				getSchemaName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(trigger.getType(),
				getType()))
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
		return getName().hashCode() + getSchemaName().hashCode() + getType().hashCode();
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
		return getName();
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return myStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		myStatus = status;
	}


	/**
	 * @return the triggeringEvent
	 */
	public String getTriggeringEvent()
	{
		return myTriggeringEvent;
	}

	/**
	 * @return the tableOwner
	 */
	public String getTableOwner()
	{
		return myTableOwner;
	}

	/**
	 * @return the baseObjectType
	 */
	public String getBaseObjectType()
	{
		return myBaseObjectType;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName()
	{
		return myTableName;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName()
	{
		return myColumnName;
	}

	/**
	 * @return the referencingNames
	 */
	public String getReferencingNames()
	{
		return myReferencingNames;
	}

	/**
	 * @return the whenClause
	 */
	public String getWhenClause()
	{
		return myWhenClause;
	}

	/**
	 * @return the actionType
	 */
	public String getActionType()
	{
		return myActionType;
	}

	public String getDisplayName()
    {
    	return getName() + " on " + getTableOwner() + "." + getTableName();
    }
}