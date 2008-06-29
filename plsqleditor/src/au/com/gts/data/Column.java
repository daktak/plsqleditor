/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;

import au.com.zinescom.util.UsefulOperations;

/**
 * This type represents
 * 
 * Created on 18/06/2003
 * 
 * @author Toby Zines
 */
public class Column extends DatabaseEntity implements Serializable
{
	/**
     * This field represents the serial version uid.
     */
    private static final long serialVersionUID = 4048794563386816310L;
    private Class myType;
	private String mySQLTypeName;
	private int mySize;
	private int myPrecisionRadix;
	private int myNumDecimalDigits;
	private boolean myIsNullable;

	private Table myTable;

	/**
	 * This field indicates whether this column is a primary key in the table 
	 * to which it is associated.
	 */
	private boolean myIsPrimaryKey;

	/**
	 * @return The type of this column as a java class.
	 */
	public Class getType()
	{
		return myType;
	}

	/**
	 * @param type The type of this column as a java class.
	 */
	public void setType(Class type)
	{
		myType = type;
	}

	/**
	 * This method indicates whether this column is nullable or not.
	 * 
	 * @return <code>true</code> if the field is nullable.
	 */
	public boolean isNullable()
	{
		return myIsNullable;
	}

	/**
	 * @return The number of decimal digits this column has (if decimal).
	 */
	public int getNumDecimalDigits()
	{
		return myNumDecimalDigits;
	}

	/**
	 * @return The precision radix.
	 */
	public int getPrecisionRadix()
	{
		return myPrecisionRadix;
	}

	/**
	 * @return The name of the sql type as a string.
	 */
	public String getSQLTypeName()
	{
		return mySQLTypeName;
	}

	/**
	 * @param b
	 */
	public void setNullable(boolean b)
	{
		myIsNullable = b;
	}

	/**
	 * @param i
	 */
	public void setNumDecimalDigits(int i)
	{
		myNumDecimalDigits = i;
	}

	/**
	 * @param i
	 */
	public void setPrecisionRadix(int i)
	{
		myPrecisionRadix = i;
	}

	/**
	 * @param string
	 */
	public void setSQLTypeName(String string)
	{
		mySQLTypeName = string;
	}

	/**
	 * @return The size of this column.
	 */
	public int getSize()
	{
		return mySize;
	}

	/**
	 * This method sets the size of this column.
	 * 
	 * @param i The size
	 */
	public void setSize(int i)
	{
		mySize = i;
	}

	/**
	 * This method returns the owning table for this column.
	 * 
	 * @return The owning table.
	 */
	public Table getTable()
	{
		return myTable;
	}

	/**
	 * This method sets the owning table for this column.
	 * 
	 * @param table The table that owns this column.
	 */
	protected void setTable(Table table)
	{
		myTable = table;
	}

	/**
	 * This method indicates whether this column is a primary key of the 
	 * table it contains.
	 * 
	 * @return {@link #myIsPrimaryKey}.
	 */
	public boolean isPrimaryKey()
	{
		return myIsPrimaryKey;
	}

	/**
	 * This method sets the indicator as to whether this column is a primary 
	 * key of the table it contains.
	 * 
	 * @param isPrimaryKey <code>true</code> if this is a primary key
	 */
	public void setIsPrimaryKey(boolean isPrimaryKey)
	{
		myIsPrimaryKey = isPrimaryKey;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Column))
        {
            return false;
        }
        Column column = (Column) obj;
        if (!UsefulOperations.objectsAreEqual(column.getName(), getName()))
        {
            return false;
        }
        if (!UsefulOperations.objectsAreEqual(column.getTable().getName(), 
                                              getTable().getName()))
        {
            return false;
        }
        return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return getName().hashCode() + getTable().getName().hashCode();
	}

}