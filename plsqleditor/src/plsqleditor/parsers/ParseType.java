/**
 * 
 */
package plsqleditor.parsers;


/**
 * @author Toby Zines
 *
 */
public class ParseType implements Comparable
{
    private int myId;
    
    private static final int PACKAGE_HEADER_AND_BODY = 0;
    private static final int PACKAGE = 1;
    private static final int PACKAGE_BODY = 2;
    private static final int SQLSCRIPT = 3;

    public static final ParseType Package_Header_And_Body = new ParseType(PACKAGE_HEADER_AND_BODY);
    public static final ParseType Package = new ParseType(PACKAGE);
    public static final ParseType Package_Body = new ParseType(PACKAGE_BODY);
    public static final ParseType SqlScript = new ParseType(SQLSCRIPT);
    
    public static String [] theNames = new String [] { "Package_Header_And_Body", "Package", "Package_Body", "SqlScript"};
    private static ParseType [] theParseTypes = new ParseType [] { Package_Header_And_Body, Package, Package_Body, SqlScript};
    private ParseType(int id)
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
        ParseType rhs = (ParseType) o;
        return this.myId - rhs.myId;
    }
    
    public static ParseType getParseType(String s)
    {
        for (int i = 0; i < theNames.length; i++)
        {
            if (s.equals(theNames[i]))
            {
                return theParseTypes[i];
            }
        }
        return null;
    }
}
