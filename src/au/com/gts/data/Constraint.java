/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;

/**
 * This type represents a constraint on the column of a table.
 * 
 * Created on 18/06/2003
 * 
 * @author Toby Zines
 */
public class Constraint extends DatabaseEntity implements Serializable
{
	/**
	 * This field represents the serial version uid.
	 */
	private static final long serialVersionUID = 4049353106619709241L;

	private String myDeleteRule;

	private String myUpdateRule;

	private String myName;

	public Constraint(String name, Column primaryColumn)
	{
		myName = name;
		myPrimaryColumn = primaryColumn;
	}

	/**
	 * This field represents the main column upon which this constraint is
	 * placed.
	 */
	private Column myPrimaryColumn;

	/**
	 * This method returns the column upon which this constraint is placed.
	 * 
	 * @return {@link #myPrimaryColumn}.
	 */
	public Column getPrimaryColumn()
	{
		return myPrimaryColumn;
	}

	/**
	 * This method sets the column upon which this constraint is based.
	 * 
	 * @param primaryColumn
	 *            The column to be set.
	 */
	public void setPrimaryColumn(Column primaryColumn)
	{
		myPrimaryColumn = primaryColumn;
	}

	/**
	 * This method sets the update rule for this constraint.
	 * 
	 * @param updateRule
	 */
	public void setUpdateRule(String updateRule)
	{
		myUpdateRule = updateRule;
	}

	/**
	 * This method updates the delete rule for this object.
	 * 
	 * @param deleteRule
	 */
	public void setDeleteRule(String deleteRule)
	{
		myDeleteRule = deleteRule;
	}

	/**
	 * @return Returns the deleteRule.
	 */
	public String getDeleteRule()
	{
		return myDeleteRule;
	}

	/**
	 * @return Returns the updateRule.
	 */
	public String getUpdateRule()
	{
		return myUpdateRule;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return myName;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("Constraint - " + getName());
		sb.append("\nPrimary Column: " + getPrimaryColumn().getName());
		sb.append("\nUpdate Rule: " + getUpdateRule());
		sb.append("\nDelete Rule: " + getDeleteRule());
		return sb.toString();
	}
}