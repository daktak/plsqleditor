package plsqleditor.stores;


/**
 * This enum represents the type of the persistence mechanism of a schema/package/function etc.
 * 
 * @version $Id$
 * 
 * @author Toby Zines
 */
public class PersistenceType implements Comparable
{
    private int myId;
    
    private static final int FILE = 0;
    private static final int DATABASE = 1;
    
    public static final PersistenceType File = new PersistenceType(FILE);
    public static final PersistenceType Database = new PersistenceType(DATABASE);
    
    private static String [] theNames = new String [] {"File", "Database"};
    private PersistenceType(int id)
    {
        myId = id;
    }
    
    public String toString()
    {
        return theNames[myId];
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        PersistenceType rhs = (PersistenceType) o;
        return this.myId - rhs.myId;
    }
}
