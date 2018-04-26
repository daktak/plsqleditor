/**
 * 
 */
package plsqleditor.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Toby Zines
 * 
 */
public class ResultSetWrapper
{
    private ResultSet myResultSet;
    private String    mySchemaName;
    private String[]  myColumnNames;
    private Statement myStatement;
    private String    myUpdateResponseStatement;
    private boolean   myIsUpdateStatementReturned = false;
    private long      myElapsedTime;
    private int       myUpdateCount;

    /**
     * This constructor creates the result set wrapper, passing in the proxied
     * result set.
     * 
     * @param rs The result set we are proxying
     * 
     * @param s The statement that generated the result set.
     * 
     * @param schemaName The name of the schema the statement was executed in.
     * @throws SQLException
     */
    public ResultSetWrapper(ResultSet rs, Statement s, String schemaName) throws SQLException
    {
        myResultSet = rs;
        mySchemaName = schemaName;
        myStatement = s;
        myUpdateCount = s.getUpdateCount();

        if (myResultSet == null)
        {
            if (myStatement != null)
            {
                myUpdateResponseStatement = "Updated " + myUpdateCount + " rows";
            }
            else
            {
                throw new SQLException("Either the result set [" + rs + "] or the statement [" + s
                        + "] must not be null");
            }
        }
    }

    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }

    public void close()
    {
        DbUtility.close(myResultSet);
        DbUtility.close(myStatement);
        myResultSet = null;
        myStatement = null;
    }

    /**
     * This method gets the column names for this result set.
     * 
     * @return the column names for this result set.
     * @throws SQLException
     */
    public String[] getColumnNames() throws SQLException
    {
        if (myColumnNames == null)
        {
            if (myResultSet == null)
            {
                myColumnNames = new String[]{"UpdateStatement"};
            }
            else
            {
                ResultSetMetaData rsmd = myResultSet.getMetaData();
                myColumnNames = new String[rsmd.getColumnCount()];
                for (int i = 0; i < myColumnNames.length; i++)
                {
                    myColumnNames[i] = rsmd.getColumnName(i + 1);
                }
            }
        }
        return myColumnNames;
    }

    /**
     * This method returns the name of the schema that had this result set
     * generated.
     * 
     * @return the name of the schema that had this result set generated.
     */
    public String getSchemaName()
    {
        return mySchemaName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#next()
     */
    public boolean next() throws SQLException
    {
        if (myResultSet == null)
        {
            if (!myIsUpdateStatementReturned)
            {
                myIsUpdateStatementReturned = true;
                return true;
            }
            else
            {
                return false;
            }
        }
        return myResultSet.next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#previous()
     */
    public boolean previous() throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.previous();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getObject(java.lang.String)
     */
    public Object getObject(String columnName) throws SQLException
    {
        if (myResultSet == null)
        {
            return myUpdateResponseStatement;
        }
        return myResultSet.getObject(columnName);
    }

    public void setElapsedTime(long diffTime)
    {
        myElapsedTime = diffTime;
    }

    /**
     * This method returns the time that this result set was executing before it
     * was returned.
     * 
     * @return
     */
    public long getElapsedTime()
    {
        return myElapsedTime;
    }

    protected int getUpdateCount()
    {
        return myUpdateCount;
    }
}
