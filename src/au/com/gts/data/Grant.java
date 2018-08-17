/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;

import plsqleditor.db.DBMetaDataGatherer;
import au.com.zinescom.util.UsefulOperations;

/**
 * This type represents a stand along procedure in a database.
 *
 * @author Toby Zines
 */
public class Grant extends DatabaseEntity implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = -3459191925608361423L;

	/** This field represents the SQL type of this table. */
	private String myPrivilege;

	/** This field is the schema that this table belongs to. */
	private String mySchemaName;

	private String myGrantor;

	private String myGrantee;

	private String myGrantable;

	private String myTarget;

	private DBMetaDataGatherer myDbMetaDataGatherer;

	/**
	 * This constructor generates a new Table with an empty set of columns.
	 * @param name
	 */
	public Grant(DBMetaDataGatherer dbmdg, String schemaName, String target, String grantor, String grantee, String privilege, String grantable)
	{
		myDbMetaDataGatherer = dbmdg;
		myPrivilege = privilege;
		mySchemaName = schemaName;
		myTarget = target;
		myGrantor = grantor;
		myGrantee = grantee;
		myGrantable = grantable;
		setName("Grant " + myPrivilege + " on " + mySchemaName +
				"." + myTarget + " to " + myGrantee);
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
	 * This method gets the privilege of the particular grant.
	 *
	 * @return myPrivilege
	 */
	public String getPrivilege()
	{
		return myPrivilege;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Grant))
		{
			return false;
		}
		Grant proc = (Grant) obj;
		if (!UsefulOperations.objectsAreEqual(proc.getName(), getName()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(proc.getGrantor(),
				getGrantor()))
		{
			return false;
		}
		if (!UsefulOperations.objectsAreEqual(proc.getGrantable(),
				getGrantable()))
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
		return getName().hashCode() + getGrantor().hashCode() + getGrantable().hashCode();
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
		return "Grantor [" + myGrantor + "] provides " + getName() +
		".\n  Grantable = " + myGrantable;
	}

	/**
	 * @return the grantor
	 */
	public String getGrantor()
	{
		return myGrantor;
	}

	/**
	 * @return the grantee
	 */
	public String getGrantee()
	{
		return myGrantee;
	}

	/**
	 * @return the grantable
	 */
	public String getGrantable()
	{
		return myGrantable;
	}

	/**
	 * @return the target
	 */
	public String getTarget()
	{
		return myTarget;
	}

	public String getDisplayName()
    {
    	return getPrivilege() + " to " + getGrantee();
    }
}