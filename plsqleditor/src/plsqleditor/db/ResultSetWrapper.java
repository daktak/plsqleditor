/**
 * 
 */
package plsqleditor.db;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author Toby Zines
 *
 */
public class ResultSetWrapper
{
    private ResultSet myResultSet;
    private Connection myConnection;
    private String mySchemaName;
    /**
     * 
     */
    public ResultSetWrapper(ResultSet rs, Connection conn, String schemaName)
    {
        myResultSet = rs;
        myConnection = conn;
        mySchemaName = schemaName;
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }

    public void close()
    {
        DbUtility.close(myResultSet);
        DbUtility.free(mySchemaName, myConnection);
        myConnection = null;
        myResultSet = null;
    }
    
    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBinaryStream(int)
     */
    public InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBinaryStream(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
     */
    public InputStream getBinaryStream(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBinaryStream(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBlob(int)
     */
    public Blob getBlob(int i) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBlob(i);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBlob(java.lang.String)
     */
    public Blob getBlob(String colName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBlob(colName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBoolean(int)
     */
    public boolean getBoolean(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBoolean(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBoolean(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBytes(int)
     */
    public byte[] getBytes(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBytes(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getBytes(java.lang.String)
     */
    public byte[] getBytes(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getBytes(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getCharacterStream(int)
     */
    public Reader getCharacterStream(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getCharacterStream(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
     */
    public Reader getCharacterStream(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getCharacterStream(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getClob(int)
     */
    public Clob getClob(int i) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getClob(i);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getClob(java.lang.String)
     */
    public Clob getClob(String colName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getClob(colName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getDate(int)
     */
    public Date getDate(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getDate(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getDate(java.lang.String)
     */
    public Date getDate(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getDate(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getDouble(int)
     */
    public double getDouble(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getDouble(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getDouble(java.lang.String)
     */
    public double getDouble(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getDouble(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getFloat(int)
     */
    public float getFloat(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getFloat(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getFloat(java.lang.String)
     */
    public float getFloat(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getFloat(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getInt(int)
     */
    public int getInt(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getInt(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getInt(java.lang.String)
     */
    public int getInt(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getInt(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getLong(int)
     */
    public long getLong(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getLong(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getLong(java.lang.String)
     */
    public long getLong(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getLong(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getShort(int)
     */
    public short getShort(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getShort(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getShort(java.lang.String)
     */
    public short getShort(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getShort(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getString(int)
     */
    public String getString(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getString(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getString(java.lang.String)
     */
    public String getString(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getString(columnName);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#next()
     */
    public boolean next() throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.next();
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#previous()
     */
    public boolean previous() throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.previous();
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getMetaData()
     */
    public ResultSetMetaData getMetaData() throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getMetaData();
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getObject(int)
     */
    public Object getObject(int columnIndex) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getObject(columnIndex);
    }

    /* (non-Javadoc)
     * @see java.sql.ResultSet#getObject(java.lang.String)
     */
    public Object getObject(String columnName) throws SQLException
    {
        // TODO Auto-generated method stub
        return myResultSet.getObject(columnName);
    }
    
}
