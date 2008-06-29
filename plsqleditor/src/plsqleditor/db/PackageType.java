/**
 * 
 */
package plsqleditor.db;


/**
 * @author Toby Zines
 *
 */
public class PackageType implements Comparable
{
    private int myId;
    
    private static final int PACKAGE_BODY = 0;
    private static final int PACKAGE = 1;
    private static final int SQL = 2;
    private static final int PACKAGE_HEADER_AND_BODY = 3;
    
    public static final PackageType Package_Body = new PackageType(PACKAGE_BODY);
    public static final PackageType Package = new PackageType(PACKAGE);
    public static final PackageType Sql = new PackageType(SQL);

	public static final PackageType Package_Header_And_Body = new PackageType(PACKAGE_HEADER_AND_BODY);
    
    private static String [] theNames = new String [] {"Package_Body", "Package", "Sql", "PackageHeaderAndBody"};
    
    private PackageType(int id)
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
        PackageType rhs = (PackageType) o;
        return this.myId - rhs.myId;
    }
}
