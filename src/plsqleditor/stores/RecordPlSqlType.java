package plsqleditor.stores;

import java.util.Map;
import java.util.TreeMap;

import au.com.zinescom.util.UsefulOperations;


public class RecordPlSqlType extends PlSqlType
{
    public static final Object RECORD = "RECORD";
    public static final Object OBJECT = "OBJECT";
    @SuppressWarnings("rawtypes")
	private Map myContainedData;

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public RecordPlSqlType(String schemaName,
                           String packageName,
                           String name,
                           String[] fieldNames,
                           String[] containedTypes)
    {
        super(schemaName, packageName, name);
        if (fieldNames == null || containedTypes == null)
        {
            throw new IllegalStateException("Either fieldNames [" + fieldNames
                    + "] or containedTypes [" + containedTypes + "] is null");
        }
        if (fieldNames.length != containedTypes.length)
        {
            throw new IllegalStateException("fieldNames length [" + fieldNames.length
                    + "] (with fields " + UsefulOperations.arrayToString(fieldNames, null, ",") + ")" +
                    " is not equal to containedTypes length [" + containedTypes.length + "] " +
                    " with contained types " + UsefulOperations.arrayToString(containedTypes, null, ",") );
        }
        myContainedData = new TreeMap();
        for (int i = 0; i < containedTypes.length; i++)
        {
            myContainedData.put(fieldNames[i], containedTypes[i]);
        }
    }

    public String[] getAutoCompleteFields()
    {
        return getContainedFieldNames();
    }

    public boolean isAggregate()
    {
        return true;
    }

    /**
     * This method indicates whether the supplied name is a field and that
     * field is an aggregate.
     * 
     * @param field The name of the field being checked.
     * 
     * @return <code>true</code> if the named <code>field</code> is actually a field
     *         of this type, and is an aggregate itself, and false otherwise.
     */
    public boolean isFieldAggregate(String field)
    {
        PlSqlType type = getTypeForField(field);
        if (type == null)
        {
            return false;
        }
        else
        {
            return type.isAggregate();
        }
    }
    
    public PlSqlType getTypeForField(String field)
    {
        return (PlSqlType) myContainedData.get(field);
    }

    @SuppressWarnings("unchecked")
	public PlSqlType[] getContainedTypes()
    {
        return (PlSqlType[]) myContainedData.values().toArray(new String[myContainedData.size()]);
    }

    @SuppressWarnings("unchecked")
	public String[] getContainedFieldNames()
    {
        return (String[]) myContainedData.keySet().toArray(new String[myContainedData.size()]);
    }
}
