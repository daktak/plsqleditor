/*
 * @version $Id$
 */
package au.com.gts.data;

/**
 * This type represents
 * 
 * Created on 18/06/2003
 * 
 * @author Toby Zines
 */
public class ForeignKeyConstraint extends Constraint
{
	/**
	 * This is the serial version uid.
	 */
	private static final long serialVersionUID = 4050204145766445105L;

	/**
	 * This constructor generates the foreign key constraint passing in the
	 * primary key, foreign key and constraint name (which is the same as the
	 * foreign key).
	 * 
	 * @param name
	 *            The name of the constraint.
	 * 
	 * @param pkColumn
	 *            The primary key column referenced by this constraint.
	 * 
	 * @param fkColumn
	 *            The foreign key column referenced by this constraint.
	 */
	public ForeignKeyConstraint(String name, Column pkColumn, Column fkColumn)
	{
		super(name, fkColumn);
		myPrimaryKeyColumn = pkColumn;
		myForeignKeyColumn = fkColumn;
	}

	/**
	 * This field represents the column in the owned whose primary key is being
	 * referenced.
	 */
	private Column myPrimaryKeyColumn;

	/**
	 * This field represents the column which is the foreign key representative
	 * of the referenced {@link #myPrimaryKeyColumn}..
	 */
	private Column myForeignKeyColumn;

	public Column getForeignKeyColumn()
	{
		return myForeignKeyColumn;
	}

	public void setForeignKeyColumn(Column column)
	{
		myForeignKeyColumn = column;
	}

	public Column getPrimaryKeyColumn()
	{
		return myPrimaryKeyColumn;
	}

	public void setPrimaryKeyColumn(Column column)
	{
		myPrimaryKeyColumn = column;
	}

	public String getDisplayName()
	{
		return getName() + " ForeignKey references "
				+ getPrimaryKeyColumn().getName();
	}

	public String toString()
	{
		Column fkColumn = getForeignKeyColumn();
		Table fkTable = fkColumn.getTable();
		return super.toString() + 
		"\nForeign Key Column: " + fkTable.getStrippedSchemaName() + "." + fkTable.getName() + "." + fkColumn.getName();
	}
}
