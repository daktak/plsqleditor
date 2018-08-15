package plsqleditor.stores;


/**
 * This class represents an array plsql type. It could be a table or varray
 * object, such as a table of number, table of number index by binary integer,
 * or varray.
 * 
 * @author Toby Zines
 */
public class ArrayPlSqlType extends PlSqlType
{
    public static final Object TABLE = "TABLE";

    public static final Object VARRAY = "VARRAY";

    private String[]  theDefaultAutoCompleteStrings = new String[]{"FIRST", "LAST", "COUNT"};
    
    /**
     * This field indicates the type of all the entries in the array.
     */
    private String myContainedTypeName;

    /**
     * This is whether the array is a table or varray.
     */
    private String    myArrayType;
    
    /**
     * This field indicates the index type of the array. By default it is a 
     * PlSqlType.NUMBER.
     */
    @SuppressWarnings("unused")
	private PlSqlType myIndexType = PlSqlTypeManager.getBaseType("BINARY_INTEGER");

    /**
     * This creates the ArrayPlSqlType.
     * 
     * @param name This is the name of the type
     * 
     * @param containedType This is the type of the contained data. i.e. This
     *            could be a table of varchar(1000) index by binary integer, in
     *            which case the contained type would be varchar(1000).
     * 
     * @param arrayType This indicates whether the array is a varray or a table.
     * 
     * @param indexType This indicates what type of index is used in this array.
     *            i.e. This could be a table of varchar(1000) index by binary
     *            integer, in which case the index type would be binary integer.
     */
    public ArrayPlSqlType(String schemaName,
                          String packageName,
                          String name,
                          String containedTypeName,
                          String arrayType,
                          PlSqlType indexType)
    {
        super(schemaName, packageName, name);
        myContainedTypeName = containedTypeName;
        myArrayType = arrayType;
        if (indexType != null)
        {
            myIndexType = indexType;
        }
    }

    public String[] getAutoCompleteFields()
    {
        return theDefaultAutoCompleteStrings;
    }

    public boolean isAggregate()
    {
        return true;
    }

    public String getContainedTypeName()
    {
        return myContainedTypeName;
    }

    public String getArrayType()
    {
        return myArrayType;
    }
}
