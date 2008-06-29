/*
 * @version $Id$
 */
package au.com.gts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    /** This field represents the constraints on this table. */
    private List  myConstraints;

    /** This field represents the SQL type of this table. */
    private String            myType;

    /**
     * This field is the set of the columns within this table.
     */
    private List      myColumns;

    /** This field is the schema that this table belongs to. */
    private String            mySchemaName;

    /**
     * This constructor generates a new Table with an empty set of columns.
     */
    public Table()
    {
        myColumns = new ArrayList();
        myConstraints = new ArrayList();
    }

    /**
     * This returns all the available columns as a Collection.
     * 
     * @return {@link #myColumns}.
     */
    public List getColumns()
    {
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
        Iterator iter = myColumns.iterator();
        while (iter.hasNext())
        {
            Column column = (Column) iter.next();
            if (column.getName().equals(columnName))
            {
                return column;
            }
        }
        return null;
    }

    /**
     * This method sets a new set of columns onto the Table. In doing so, all the old columns are removed, and they will
     * no longer reference this table.
     * 
     * @param columns
     *            The columns to set on this table.
     */
    public void setColumns(List columns)
    {
        Iterator iter = myColumns.iterator();

        while (iter.hasNext())
        {
            Column col = (Column) iter.next();
            col.setTable(null);
        }
        myColumns = columns;
        iter = myColumns.iterator();

        while (iter.hasNext())
        {
            Column col = (Column) iter.next();
            col.setTable(this);
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
        Schema[] schemas = Schema.getSchemas();

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
     * This method returns the current list of constratints.
     * 
     * @return {@link #myConstraints}.
     */
    public List getConstraints()
    {
        return myConstraints;
    }

    /**
     * This method sets a new set of constraints on the table. Any earlier constraints are tossed away.
     * 
     * @param constraints
     *            The new set of constraints to replace the old.
     */
    public void setConstraints(List constraints)
    {
        myConstraints = constraints;
    }

    /**
     * This method adds a constraint this table.
     * 
     * @param constraint
     *            The constraint to add.
     */
    public void addConstraint(Constraint constraint)
    {
        myConstraints.add(constraint);
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
        if (!UsefulOperations.objectsAreEqual(table.getSchemaName(), getSchemaName()))
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
}