package plsqleditor.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import plsqleditor.db.ResultSetWrapper;

/**
 * The content provider class is responsible for providing objects to the view.
 * It can wrap existing objects in adapters or simply return objects as-is.
 * These objects may be sensitive to the current input of the view, or ignore it
 * and always show the same content (like Task List, for example).
 * 
 * @author Toby Zines
 */
public class SqlOutputContentProvider implements IStructuredContentProvider
{
    private ResultSetWrapper                myCurrentResultSet;
    private static String[]                 DEFAULT_TITLES   = new String[]{"Message"};
    private static SqlResult                DEFAULT_RESULT;
    private String[]                        myTitles         = DEFAULT_TITLES;
    private List                            myElementList;

    private Object[]                        myElements       = new Object[]{DEFAULT_RESULT};
    private Viewer                          myViewer;
    private static SqlOutputContentProvider theInstance;
    private String                          myQueryString    = "Welcome";
    private int                             myNumRowsPerNext = 200;

    static
    {
        DEFAULT_RESULT = new SqlResult(DEFAULT_TITLES);
        DEFAULT_RESULT.put(DEFAULT_TITLES[0], "Empty");
    }

    /**
     * This is the private constructor of the singleton
     * SqlOutputContentProvider.
     */
    private SqlOutputContentProvider()
    {
        //
    }

    /**
     * This method gets the singleton instance.
     * 
     * @return the singleton instance.
     */
    public static synchronized SqlOutputContentProvider getInstance()
    {
        if (theInstance == null)
        {
            theInstance = new SqlOutputContentProvider();
        }
        return theInstance;
    }

    /**
     * This method updates the content to have the latest particular output.
     * 
     * @param rs The resultset that will be backing this content provider until
     *            the next call to this method.
     * 
     * @param queryString The query that caused the supplied result set
     *            <code>rs</code> to be generated.
     */
    public void updateContent(ResultSetWrapper rs, String queryString)
    {
        setupTitles(rs);
        setQueryString(rs.getElapsedTime() / 1000 + "ms elapsed: " + queryString);
        myElementList = new ArrayList();
        next(false, true);
    }

    /**
     * This method retrieves the next set of data from the current result set
     * and notifies the viewer of an update (if there has been a change).
     * 
     * @param getAll If this is <code>true</code> all the remaining data will
     *            be retrieved. Otherwise, only (at most) the next
     *            {@link #myNumRowsPerNext} will be retrieved. This value can be
     *            set by a call to {@link #setNumRowsPerNext(int)}.
     * 
     * @param forceChange If this is <code>true</code> it means that an update
     *            must occur, even if there is no change to the result set.
     * 
     * @return The number of new rows retrieved.
     */
    private int next(boolean getAll, boolean forceChange)
    {
        int count = 0;
        if (myCurrentResultSet != null)
        {
            boolean isChanged = false;
            try
            {
                while (myCurrentResultSet.next() && (getAll || count++ < myNumRowsPerNext))
                {
                    SqlResult sqlResult = new SqlResult(myTitles);
                    // String[] sqlResult = new String[myTitles.length];
                    for (int i = 0; i < myTitles.length; i++)
                    {
                        sqlResult.put(myTitles[i], myCurrentResultSet.getObject(myTitles[i]));
                        // sqlResult[i] =
                        // String.valueOf(rs.getObject(myTitles[i]));
                    }
                    myElementList.add(sqlResult);
                    isChanged = true;
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            myElements = myElementList.toArray();
            if (myViewer != null && (isChanged || forceChange))
            {
                myViewer.refresh();
            }
        }
        return count;
    }

    /**
     * This method retrieves the next set of data from the current result set
     * and notifies the viewer of an update (if there has been a change).
     * 
     * @param getAll If this is <code>true</code> all the remaining data will
     *            be retrieved. Otherwise, only (at most) the next
     *            {@link #myNumRowsPerNext} will be retrieved. This value can be
     *            set by a call to {@link #setNumRowsPerNext(int)}.
     * 
     * @return The number of new rows retrieved.
     */
    public int next(boolean getAll)
    {
        return next(getAll, false);
    }

    /**
     * This is a helper method that extracts the column names from the supplied
     * result set <code>rs</code>'s meta data.
     * 
     * @param rs The result set wrapper whose column names will be retrieved.
     */
    private void setupTitles(ResultSetWrapper rs)
    {
        myCurrentResultSet = rs;
        try
        {
            if (myCurrentResultSet != null)
            {
                myTitles = myCurrentResultSet.getColumnNames();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput)
    {
        myViewer = v;
        System.out.println("inputChanged: Updated");
    }

    public void close()
    {
        if (myCurrentResultSet != null)
        {
            myCurrentResultSet.close();
        }
    }

    public Object[] getElements(Object parent)
    {
        return myElements;
    }

    /**
     * @return the array of my titles.
     */
    public String[] getTitles()
    {
        return myTitles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
        //
    }

    /**
     * @return The query string that caused the result set to be generated.
     */
    public String getQueryString()
    {
        return myQueryString;
    }

    /**
     * @param queryString The queryString to set.
     */
    private void setQueryString(String queryString)
    {
        myQueryString = queryString;
    }

    /**
     * @param numRowsPerNext The numRowsPerNext to set.
     */
    public void setNumRowsPerNext(int numRowsPerNext)
    {
        myNumRowsPerNext = numRowsPerNext;
    }
}
