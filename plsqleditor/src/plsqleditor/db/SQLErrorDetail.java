package plsqleditor.db;

/**
 * This class 
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 2/03/2005 
 *
 */
public class SQLErrorDetail
{
    private int    myRow;
    private int    myColumn;
    private String myText;
    
    public SQLErrorDetail(int row, int column, String text)
    {
        this.myRow = row;
        this.myColumn = column;
        this.myText = text;
    }
    
    public String toString()
    {
        return 
            "SQLErrorDetail:" +
            "\n    row:  " + myRow +
            "\n column:  " + myColumn +
            "\n   text:  " + myText + "\n";
    }

    /**
     * This method returns the column.
     * 
     * @return {@link #myColumn}.
     */
    public int getColumn()
    {
        return myColumn;
    }
    

    /**
     * This method returns the row.
     * 
     * @return {@link #myRow}.
     */
    public int getRow()
    {
        return myRow;
    }
    

    /**
     * This method returns the text.
     * 
     * @return {@link #myText}.
     */
    public String getText()
    {
        return myText;
    }
    

}
