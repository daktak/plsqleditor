/**
 * 
 */
package plsqleditor.actions;


/**
 * This class represents the type of open we will perform on an editor.
 * It will either be a schema open (which does nothing), a package open, which
 * causes the package to open and no navigation to take place, or a Method open
 * which causes the package to open and the method to be navigated to.  
 * 
 * @author Toby Zines
 */
public class OpenLocationType implements Comparable<Object>
{
    private int myId;
    
    private static final int SCHEMA = 0;
    private static final int PACKAGE = 1;
    private static final int METHOD = 2;

    public static final OpenLocationType Schema = new OpenLocationType(SCHEMA);
    public static final OpenLocationType Package = new OpenLocationType(PACKAGE);
    public static final OpenLocationType Method = new OpenLocationType(METHOD);
    
    private static String [] theNames = new String [] {"Schema", "Package", "Method"};
    private OpenLocationType(int id)
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
        OpenLocationType rhs = (OpenLocationType) o;
        return this.myId - rhs.myId;
    }
}
