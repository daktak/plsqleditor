package plsqleditor.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 2/03/2005
 */
public class LoadPackageManager
{
    public enum PackageType {
        Package_Body, Package
    }

    private SQLErrorDetail[] mySQLErrors     = new SQLErrorDetail[0];

    /**
     * This constructor creates the SQLErrorManager, passing in the textPane that will be hilited when errors occur, and
     * the {@link #nextError()} and {@link #previousError()} calls are made.
     * 
     * @param textPane
     */
    public LoadPackageManager()
    {
        //
    }

    public SQLErrorDetail [] loadFile(String schemaName, String packageName, String toLoad, PackageType type)
    {
        Connection c = null;
        try
        {
            c = DbUtility.getConnection(schemaName);
            return loadFile(c,packageName,toLoad,type);
        }
        finally
        {
            if (c != null)
            {
                DbUtility.free(schemaName, c);
            }
        }
    }
    
    /**
     * This method loads a file into the database, returning any errors it found.
     * 
     * @param c The connection to use to load the database.
     * 
     * @param packageName The name of the package being loaded, for error purposes.
     * 
     * @param toLoad The text representation of the entire package.
     * 
     * @param type The type of thing being loaded. (package/package body etc).
     * 
     * @return The list of errors from the compile, or null if there were none.
     */
    private SQLErrorDetail [] loadFile(Connection c, String packageName, String toLoad, LoadPackageManager.PackageType type)
    {
        Statement s = null;
        try
        {
            s = c.createStatement();
            s.execute(toLoad);
            
            String warning = getErrorStatus(c, s, packageName, type);
            if (warning != null)
            {
                return getSQLErrors();
            }
            return null;
        }
        catch (SQLException e)
        {
            DbUtility.printErrors(e);
            DbUtility.close(s);
            final String msg ="Failed to load package " + packageName + ": " + e;
            return new SQLErrorDetail[] { new SQLErrorDetail(0,0,msg) };
        }
    }

    /**
     * This method gets the error status of the statement <code>s</code>. If there have been
     * warnings, the error details will be initialised with the new errors, and the first warning
     * message will be returned to indicate something is wrong.
     * 
     * @param s The statement whose warning status is being checked.
     * 
     * @param packageName The name of the package/package body that was just created in the
     *            statement <code>s</code>.
     * 
     * @param packageType The type of package errors we are searching for on failure.
     * 
     * @return The message from the first warning, or null if there were no warnings.
     */
    private String getErrorStatus(Connection c, Statement s, String packageName, PackageType packageType)
    {
        try
        {
            SQLWarning warning = s.getWarnings();

            if (warning != null)
            {
                //DbUtility.printWarnings(warning);
                SQLErrorDetail[] details = getErrorDetails(c, packageName, packageType);

                setErrors(details);
                return warning.getMessage();
            }
            else
            {
                return null;
            }
        }
        catch (SQLException sqle)
        {
            return "Failed to find warnings because of " + sqle;
        }
    }

    public SQLErrorDetail[] getErrorDetails(String schemaName, String packageName, PackageType errorType)
    {
        Connection c = null;
        try
        {
            c = DbUtility.getConnection(schemaName);
            return getErrorDetails(c, packageName, errorType);
        }
        finally
        {
            if (c != null)
            {
                DbUtility.free(schemaName, c);
            }
        }
    }
    
    /**
     * This method gets the error details from the statement that attempted to create/replace the procedure/function of
     * type <code>procType</code> and name <code>procName</code>.
     * 
     * @param s The statement to use to retrieve the errors.
     * 
     * @param packageName The name of the package or package body that was compiled, but caused warnings.
     * 
     * @param errorType the type of the errors to look for.
     * 
     * @return The list of error details concerning the compile problems.
     */
    private SQLErrorDetail[] getErrorDetails(Connection c, String packageName, PackageType errorType)
    {
        String sql = "select line, position, text from all_errors where upper(name) = upper(?) "
                + " and type = ? order by sequence";

        SQLErrorDetail detail = null;
        SQLErrorDetail[] toReturn = null;
        PreparedStatement s = null;
        ResultSet rs = null;
        try
        {
            s = c.prepareStatement(sql);
            s.setString(1, packageName.toUpperCase());
            s.setString(2, errorType.toString().toUpperCase().replace('_', ' '));
            rs = s.executeQuery();
            List<SQLErrorDetail> v = new Vector<SQLErrorDetail>();
            while (rs.next())
            {
                detail = new SQLErrorDetail(rs.getInt(1), rs.getInt(2),rs.getString(3));
                v.add(detail);
            }
            toReturn = v.toArray(new SQLErrorDetail[v.size()]);
            s.close();
        }
        catch (SQLException e)
        {
            final String msg = "Failed to retrieve error details: " + e;
            System.out.println(msg);
            toReturn = new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
        }
        finally
        {
            DbUtility.close(s);
            DbUtility.close(rs);
        }
        return toReturn;
    }

    private void setErrors(SQLErrorDetail[] details)
    {
        mySQLErrors = details;
//        for (int i = 0; i < details.length; i++)
//        {
//            SQLErrorDetail detail = details[i];
//            if (detail.getRow() == 1)
//            {
//                detail.getColumn() -= 24;
//            }
//        }
    }

    /**
     * This method returns the sQLErrors.
     * 
     * @return {@link #mySQLErrors}.
     */
    public SQLErrorDetail[] getSQLErrors()
    {
        return mySQLErrors;
    }
}
