package plsqleditor.db;

import java.sql.*;

/**
 * This class represents a wrapper that knows how to retrieve dbms output from a database
 * connection.
 * 
 * @author Thomas Kyte (tkyte@oracle.com) (modified by tobyz)
 * 
 * @version $Id$
 */
public class DbmsOutput
{
    /**
     * This field is used to enable dbms_output - equivalent to SET SERVEROUTPUT on in SQL*PLUS.
     */
    private CallableStatement myEnableStmt;

    /**
     * This field is used to disable dbms_output - like SET SERVEROUTPUT OFF.</li>
     */
    private CallableStatement myDisableStmt;

    /**
     * This field is used to "dump" or display the results from dbms_output.
     */
    private CallableStatement myShowStmt;

    /**
     * This constructor simply prepares the three statements we plan on executing.
     * <p>
     * The statement we prepare for SHOW is a block of code to return a String of dbms_output
     * output. Normally, you might bind to a PLSQL table type but the jdbc drivers don't support
     * PLSQL table types -- hence we get the output and concatenate it into a string. We will
     * retrieve at least one line of output -- so we may exceed your MAXBYTES parameter below. If
     * you set MAXBYTES to 10 and the first line is 100 bytes long, you will get the 100 bytes.
     * MAXBYTES will stop us from getting yet another line but it will not chunk up a line.
     * 
     * @param conn
     */
    public DbmsOutput(Connection conn) throws SQLException
    {
        myEnableStmt = conn.prepareCall("begin dbms_output.enable(:1); end;");
        myDisableStmt = conn.prepareCall("begin dbms_output.disable; end;");

        myShowStmt = conn.prepareCall("declare " + " l_line varchar2(255); " + " l_done number; "
                + " l_buffer long; " + "begin " + " loop "
                + " exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; "
                + " dbms_output.get_line(l_line, l_done ); "
                + " l_buffer := l_buffer || l_line || chr(10); " + " end loop; "
                + " :done := l_done; " + " :buffer := l_buffer; " + "end;");
    }

    /**
     * This method simply sets your size and executes the dbms_output.enable call.
     * 
     * @param size
     *            The size you wish to enable output to.
     */
    public void enable(int size) throws SQLException
    {
        myEnableStmt.setInt(1, size);
        myEnableStmt.executeUpdate();
    }

    /**
     * This method only has to execute the dbms_output.disable call.
     */
    public void disable() throws SQLException
    {
        myDisableStmt.executeUpdate();
    }

    /**
     * This method does most of the work. It loops over all of the dbms_output data, fetching it in
     * this case 32,000 bytes at a time (give or take 255 bytes). It will store this output in a
     * string buffer and return it when there is no more.
     * 
     * @return The current dbms output.
     */
    public String show() throws SQLException
    {
        myShowStmt.registerOutParameter(2, java.sql.Types.INTEGER);
        myShowStmt.registerOutParameter(3, java.sql.Types.VARCHAR);

        StringBuffer sb = new StringBuffer();

        for (;;)
        {
            myShowStmt.setInt(1, 32000);
            myShowStmt.executeUpdate();
            sb.append(myShowStmt.getString(3));
            if (myShowStmt.getInt(2) == 1)
            {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * This method closes the callable statements associated with the DbmsOutput class.
     * <p>
     * Call this if you allocate a DbmsOutput statement on the stack and it is going to go out of
     * scope -- just as you would with any callable statement, result set and so on.
     */
    public void close() throws SQLException
    {
        myEnableStmt.close();
        myDisableStmt.close();
        myShowStmt.close();
    }
}
