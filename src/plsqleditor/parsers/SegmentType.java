package plsqleditor.parsers;

/**
 * @author Toby Zines
 *
 */
public class SegmentType implements Comparable<Object>
{
    private int myId;
    
    private static final int SCHEMA = 0;
    private static final int PACKAGE = 1;
    private static final int PACKAGE_BODY = 2;
    private static final int TYPE = 3;
    private static final int SUBTYPE = 4;
    private static final int FIELD = 5;
    private static final int CONSTANT = 6;
    private static final int FUNCTION = 7;
    private static final int PROCEDURE = 8;
    private static final int LABEL = 9;
    private static final int TABLE = 10;
    private static final int COLUMN = 11;
    private static final int CODE = 12;
    private static final int CURSOR= 13;
    private static final int PRAGMA= 14;
    
    public static final SegmentType Schema = new SegmentType(SCHEMA);
    public static final SegmentType Package = new SegmentType(PACKAGE);
    public static final SegmentType Package_Body = new SegmentType(PACKAGE_BODY);
    public static final SegmentType Type = new SegmentType(TYPE);
    public static final SegmentType SubType = new SegmentType(SUBTYPE);
    public static final SegmentType Field = new SegmentType(FIELD);
    public static final SegmentType Constant = new SegmentType(CONSTANT);
    public static final SegmentType Function = new SegmentType(FUNCTION);
    public static final SegmentType Procedure = new SegmentType(PROCEDURE);
    public static final SegmentType Label = new SegmentType(LABEL);
    public static final SegmentType Table = new SegmentType(TABLE);
    public static final SegmentType Column = new SegmentType(COLUMN);
    public static final SegmentType Code = new SegmentType(CODE);
    public static final SegmentType Cursor = new SegmentType(CURSOR);
    public static final SegmentType Pragma = new SegmentType(PRAGMA);
    
    private static String [] theNames = new String [] {"Schema", "Package", "PackageBody", "Type", "Subtype", "Field", "Constant", "Function", "Procedure", "Label", "Table", "Column", "Code", "Cursor", "Pragma"};
    
    private SegmentType(int id)
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
        SegmentType rhs = (SegmentType) o;
        return this.myId - rhs.myId;
    }
}
