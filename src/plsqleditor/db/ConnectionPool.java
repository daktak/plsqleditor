package plsqleditor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Creates a pool of database connections and returns them as needed. This class
 * handles preallocating, recycling, and managing JDBC connections.
 */

public class ConnectionPool implements Runnable
{
    private String              driver;
    private String              url;
    private String              userName;
    private String              password;
    private int                 numInitialConnections;
    private int                 maxConnections;
    private boolean             waitForConnection;
    private Vector<Connection>  availableConnections, busyConnections;
    private boolean             connectionPending              = false;
    private boolean             myIsAutoCommittingOnClose;
    private final static int    DEFAULT_INITIAL_SIZE           = 10;
    private final static int    DEFAULT_CAPACITY_INCREMENT     = 5;
    private final static long   CONNECTION_WAIT_INTERVAL       = 1000;
    private final static String CONNECTION_LIMIT_ERR_MSG       = "Connection limit reached";
    private final static String DRIVER_CLASS_NOT_FOUND_ERR_MSG = "Database driver class not found";

    public ConnectionPool(String driver,
                          String url,
                          String userName,
                          String password,
                          int numInitialConnections,
                          int maxConnections,
                          boolean waitForConnection) throws SQLException
    {
        this.driver = driver;
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.numInitialConnections = numInitialConnections;
        this.maxConnections = maxConnections;
        this.waitForConnection = waitForConnection;

        // -- make sure we have reasonable defaults
        if (numInitialConnections > maxConnections)
        {
            numInitialConnections = maxConnections;
        }
        busyConnections = new Vector<Connection>(DEFAULT_INITIAL_SIZE, DEFAULT_CAPACITY_INCREMENT);
        availableConnections = new Vector<Connection>(numInitialConnections,
                DEFAULT_CAPACITY_INCREMENT);
        for (int i = 0; i < numInitialConnections; i++)
        {
            availableConnections.addElement(makeNewConnection());
            // -- give the database time to create connection
            try
            {
                Thread.sleep(CONNECTION_WAIT_INTERVAL);
            }
            catch (InterruptedException ignored)
            {
                // do nothing
            }
        }
    }

    public synchronized Connection getConnection() throws SQLException
    {
        if (!availableConnections.isEmpty())
        {
            Connection existingConnection = availableConnections.lastElement();
            int lastIndex = availableConnections.size() - 1;
            availableConnections.removeElementAt(lastIndex);
            // -- check to make sure connection is still alive, if not, create a
            // new one
            if (existingConnection.isClosed())
            {
                notifyAll();
                return getConnection();
            }
            else
            {
                busyConnections.addElement(existingConnection);
                return existingConnection;
            }
        }
        else
        {
            if ((totalConnections() < maxConnections) && !connectionPending)
            {
                makeBackgroundConnection();
            }
            else if (!waitForConnection)
            {
                throw new SQLException(CONNECTION_LIMIT_ERR_MSG);
            }
            try
            {
                wait();
            }
            catch (InterruptedException ignored)
            {
                //
            }
            return getConnection();
        }
    }

    private void makeBackgroundConnection()
    {
        connectionPending = true;
        try
        {
            Thread connectionThread = new Thread(this);
            connectionThread.start();
        }
        catch (OutOfMemoryError oome)
        {
            // -- no code here -- we're giving up on a new connection
        }
    }

    public void run()
    {
        try
        {
            Connection c = makeNewConnection();
            synchronized (this)
            {
                availableConnections.addElement(c);
                connectionPending = false;
                notifyAll();
            }
        }
        catch (SQLException sqle)
        {
            // -- give up creating a new one, just wait for one to become free
        }
        catch (OutOfMemoryError oome)
        {
            // -- give up creating a new one, just wait for one to become free
        }
    }

    private Connection makeNewConnection() throws SQLException
    {
        try
        {
            Class.forName(driver);
            Connection c = DriverManager.getConnection(url, userName, password);
            c.setAutoCommit(false);
            return c;
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new SQLException(DRIVER_CLASS_NOT_FOUND_ERR_MSG);
        }
    }

    public synchronized void free(Connection c)
    {
        if (c != null)
        {
            busyConnections.removeElement(c);
            availableConnections.addElement(c);
            notifyAll();
        }
    }

    public synchronized int totalConnections()
    {
        return (availableConnections.size() + busyConnections.size());
    }

    public synchronized void closeAllConnections()
    {
        closeConnections(availableConnections);
        closeConnections(busyConnections);
        busyConnections = new Vector<Connection>(DEFAULT_INITIAL_SIZE, DEFAULT_CAPACITY_INCREMENT);
        availableConnections = new Vector<Connection>(numInitialConnections, DEFAULT_CAPACITY_INCREMENT);
    }

    public void closeConnections(Vector<Connection> connections)
    {
        try
        {
            for (Connection con : connections)
            {
                if (!con.isClosed())
                {
                    con.close();
                }
            } // while
        }
        catch (SQLException sqle)
        {
            // -- ignore, connection is already closed
        } // catch
    }

    public synchronized String toString()
    {
        String info = "ConnectionPool(" + url + "," + userName + ")" + ", available= "
                + availableConnections.size() + ", busy= " + busyConnections.size() + ", max="
                + maxConnections;
        return info;
    }

    public String getDriver()
    {
        return driver;
    }

    public String getPassword()
    {
        return password;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setAutoCommitting(boolean isAutoCommittingOnClose)
    {
        myIsAutoCommittingOnClose = isAutoCommittingOnClose;
    }

    public boolean isAutoCommittingOnClose()
    {
        return myIsAutoCommittingOnClose;
    }
}
