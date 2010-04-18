package org.boomsticks.plsqleditor.dialogs.openconnections;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorter for the LiveConnectionViewer that displays items of type
 * <code>ExampleTask</code>. The sorter supports three sort criteria:
 * <p>
 * <code>URL</code>: ConnectionString (String)
 * </p>
 * <p>
 * <code>TYPE</code>: Type (schema default or specific connection) (String)
 * </p>
 * <p>
 * <code>TIME_UP</code>: number of seconds alive (long).
 * </p>
 */
public class LiveConnectionSorter extends ViewerSorter
{

    /**
     * Constructor argument values that indicate to sort items by description,
     * owner or percent complete.
     */
    public static final int DEFAULT_SCHEMA_COLUMN = 0;
    public final static int URL                   = 1;
    public final static int TYPE                  = 2;
    public final static int PROJECT               = 3;
    public final static int LAST_FILE             = 4;
    public final static int USER                  = 5;

    // Criteria that the instance uses
    private int             criteria;

    /**
     * Creates a resource sorter that will use the given sort criteria.
     * 
     * @param criteria the sort criterion to use: one of <code>NAME</code> or
     *            <code>TYPE</code>
     */
    public LiveConnectionSorter(int criteria)
    {
        super();
        this.criteria = criteria;
    }

    /*
     * (non-Javadoc) Method declared on ViewerSorter.
     */
    public int compare(Viewer viewer, Object o1, Object o2)
    {

        LiveConnection task1 = (LiveConnection) o1;
        LiveConnection task2 = (LiveConnection) o2;

        switch (criteria)
        {
            case URL :
                return compareUrl(task1, task2);
            case TYPE :
                return compareTypes(task1, task2);
            case PROJECT :
                return compareProject(task1, task2);
            case LAST_FILE :
                return compareFilenames(task1, task2);
            case USER :
                return compareUser(task1, task2);
            default :
                return 0;
        }
    }

    /**
     * Returns a number reflecting the collation order of the given tasks based
     * on the percent completed.
     * 
     * @param task1
     * @param task2
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    private int compareProject(LiveConnection task1, LiveConnection task2)
    {
        int result = task1.getProject().compareTo(task2.getProject());
        return result;
    }
    
    /**
     * 
     * @param task1
     * @param task2
     * @return
     */
    private int compareFilenames(LiveConnection task1, LiveConnection task2)
    {
        int result = task1.getFilename().compareTo(task2.getFilename());
        return result;
    }

    /**
     * Returns a number reflecting the collation order of the given tasks based
     * on the description.
     * 
     * @param task1 the first task element to be ordered
     * @param resource2 the second task element to be ordered
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    protected int compareUrl(LiveConnection task1, LiveConnection task2)
    {
        return task1.getUrl().compareTo(task2.getUrl());
    }

    /**
     * Returns a number reflecting the collation order of the given tasks based
     * on the description.
     * 
     * @param task1 the first task element to be ordered
     * @param resource2 the second task element to be ordered
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    protected int compareUser(LiveConnection task1, LiveConnection task2)
    {
        return task1.getUser().compareTo(task2.getUser());
    }

    /**
     * Returns a number reflecting the collation order of the given tasks based
     * on their owner.
     * 
     * @param resource1 the first resource element to be ordered
     * @param resource2 the second resource element to be ordered
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    protected int compareTypes(LiveConnection task1, LiveConnection task2)
    {
        return task1.getFilename().compareTo(task2.getFilename());
    }

    /**
     * Returns the sort criteria of this this sorter.
     * 
     * @return the sort criterion
     */
    public int getCriteria()
    {
        return criteria;
    }
}
