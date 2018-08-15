/**
 * 
 */
package plsqleditor.views;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a single row of a result set obtained from a database.
 * Each of these 'rows' understands the columns that it represents, and will contain
 * the data (upon setting) of each of these rows.
 *  
 * @author Toby Zines
 * 
 * @version $Id$
 */
public class SqlResult
{
    /** The columns in this row. */
    private String[] myColumnNames;
    
    /** The map of column names to values for this row. */
    private Map<Object, Object>      myValues;

    /**
     * This constructor creates the result, starting it out as an empty row that knows the columns
     * against which data will be stored.
     * 
     * @param columnNames The names of the columns that are in the row this object represents.
     */
    public SqlResult(String[] columnNames)
    {
        myColumnNames = columnNames;
        myValues = new HashMap<Object, Object>();
    }

    /**
     * This method returns the value associated with the table column represented by the supplied
     * table column <code>index</code>.
     * 
     * @param index
     *            The index into {@link #myColumnNames} indicating the column whose assoicated value
     *            is being requested.
     * 
     * @return The value associated with the given index into the columns.
     */
    public String getText(int index)
    {
        return String.valueOf(get(myColumnNames[index]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        return myValues.get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {
        return myValues.put(key, value);
    }

    /**
     * This method gets the names of the columns of the row represented by this result.
     * 
     * @return The names of the columns that each have a single row of data stored against them in
     *         this object.
     */
    public String[] getColumnNames()
    {
        return myColumnNames;
    }

}
