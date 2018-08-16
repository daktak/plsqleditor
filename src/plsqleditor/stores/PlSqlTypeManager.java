package plsqleditor.stores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlCodeScanner;
import plsqleditor.parsers.AbstractPlSqlParser;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

public class PlSqlTypeManager
{
    private static final Map<String, PlSqlType> theBaseTypes;
    private static Map<IProject, PlSqlTypeManager>       theTypeManagers = new HashMap<IProject, PlSqlTypeManager>();

    @SuppressWarnings("rawtypes")
	private Map<String, Map>              mySchemaAndPackageQualifiedTypes;

    static
    {
        theBaseTypes = new HashMap<String, PlSqlType>(PlSqlCodeScanner.DATATYPES.length);
        for (int i = 0; i < PlSqlCodeScanner.DATATYPES.length; i++)
        {
            PlSqlType type = new PlSqlType(null, null, PlSqlCodeScanner.DATATYPES[i]);
            theBaseTypes.put(type.getName(), type);
        }
        theBaseTypes.put("VARCHAR", new PlSqlType(null, null, "VARCHAR"));
    }

    @SuppressWarnings("rawtypes")
	private PlSqlTypeManager()
    {
        mySchemaAndPackageQualifiedTypes = new HashMap<String, Map>();
    }

    public static PlSqlType getBaseType(String typeName)
    {
        return (PlSqlType) theBaseTypes.get(typeName);
    }

    /**
     * This method returns the base type of a particular supplied plsqltype
     * <code>toCheck</code>. If the parent type is null, or cannot be found
     * in our system, the type <code>toCheck</code> is returned.
     * 
     * @param toCheck The PlSqlType whose base type is sought.
     * 
     * @return The base type of the PlSqlType <code>toCheck</code> or itself
     *         if the base type cannot be found.
     */
    public PlSqlType getBaseType(PlSqlType toCheck)
    {
        String parentTypeName = toCheck.getParentTypeName();
        if (parentTypeName == null)
        {
            return toCheck;
        }
        else
        {
            PlSqlType type = getType(parentTypeName);
            if (type != null)
            {
                return type;
            }
            return toCheck; // nothing else to do here atm
        }
    }



    public static PlSqlTypeManager getTypeManager(IProject forProject)
    {
        if (forProject == null)
        {
            forProject = PlsqleditorPlugin.getDefault().getProject();
        }
        PlSqlTypeManager manager = (PlSqlTypeManager) theTypeManagers.get(forProject);
        if (manager == null)
        {
            manager = new PlSqlTypeManager();
            theTypeManagers.put(forProject, manager);
        }
        return manager;
    }

    public PlSqlType storeType(String packageName, Segment segment)
    {
        String name = segment.getName();
        
        String schemaName = PlsqleditorPlugin.getDefault().getCurrentSchema();
        PlSqlType type = getType(schemaName, packageName, name);
        if (type == null)
        {
            SegmentType segmentType = segment.getType();
            if (segmentType == SegmentType.SubType)
            {
                // PlSqlType parentType = null;
                String returnType = segment.getReturnType();
                String parentTypeName = parseTypeFrom(returnType);
                // parentType = getType(parentTypeName);
                type = storeType(schemaName, packageName, name, parentTypeName);
            }
            else if (segmentType == SegmentType.Type)
            {
                String returnType = segment.getReturnType();
                List<String> fieldNamesList = new ArrayList<String>();
                List<Object> typeNamesList = new ArrayList<Object>();
                String typeType = null;
                PlSqlType indexType = null;
                // TODO get details
                // group(1) = field name
                // group(2) = field type
                // group(3) = remainder of fields
                // like RECORD(a NUMBER, b NUMBER, c VARCHAR2(1000))
                // or TABLE OF compare_record INDEX BY BINARY_INTEGER
                final Pattern recordTypeParamsPattern = Pattern
                        .compile(AbstractPlSqlParser.RECORD
                        		// fix for 1553097 - Parsing error for Package body w/ RECORD type and PLDOC
                                + "\\s*\\([^\\w\\.\\%\\d_,]*([\\w\\.\\%\\d_]+)\\s+([\\w\\.\\%\\d_]+)[^,]*(,.*)?");
                // TODO fix the table matcher to be correct
                final Pattern tableTypeParamsPattern = Pattern
                        .compile(AbstractPlSqlParser.TABLE_OF
                                + "\\s*([\\w\\.\\%\\d_]+)\\s*(\\(\\d+\\))?(\\s+[Ii][Nn][Dd][Ee][Xx]\\s+[Bb][Yy]\\s+[\\w\\.\\%\\d_]+)?.*");
                Matcher paramsMatcher = recordTypeParamsPattern.matcher(returnType);
                if (paramsMatcher.matches())
                {
                    typeType = "RECORD";
                    processRecord(fieldNamesList, typeNamesList, paramsMatcher);
                }
                else if ((paramsMatcher = tableTypeParamsPattern.matcher(returnType)).matches())
                {
                    typeType = "TABLE";
                    indexType = processTable(typeNamesList, paramsMatcher);
                }
                else
                {
                    System.out.println("The type is not determinable : " + returnType);
                }
                String[] fieldNames = (String[]) fieldNamesList.toArray(new String[fieldNamesList
                        .size()]);
                String[] containedTypes = (String[]) typeNamesList.toArray(new String[typeNamesList
                        .size()]);
                type = storeType(schemaName,
                                 packageName,
                                 name,
                                 fieldNames,
                                 containedTypes,
                                 typeType,
                                 indexType);
            }
        }
        segment.setReferredData(type);
        return type;
    }

    /**
     * This method processes a record. The section should look something like:
     * <code>
     * TABLE OF compare_record INDEX BY BINARY_INTEGER
     * </code>
     * 
     * @param fieldNamesList The list of field names to add to.
     * 
     * @param typeNamesList The list of type names to add to.
     * 
     * @param paramsMatcher The original parameters matcher. group(1) contains
     *            the first field name, group(2) contains the first field type
     *            name.
     */
    private PlSqlType processTable(List<Object> typeNamesList, Matcher paramsMatcher)
    {
        String grp2 = paramsMatcher.group(2);
        typeNamesList.add(paramsMatcher.group(1) + grp2 == null ? "" : grp2);
        String remainder = paramsMatcher.group(3);
        if (remainder != null && remainder.length() > 0)
        {
            final Pattern indexMatcher = Pattern
                    .compile("[Ii][Nn][Dd][Ee][Xx]\\s+[Bb][Yy]\\s+([\\w\\.\\%\\d_]+)");
            Matcher m = indexMatcher.matcher(remainder);
            if (m.matches())
            {
                String typeName = m.group(1);
                PlSqlType type = getBaseType(typeName);
                return type;
            }
        }
        return null;
    }

    /**
     * This method processes a record. The section should look something like:
     * <code>
     * , b NUMBER, c VARCHAR2(1000),d other.type_here
     * </code>
     * 
     * @param fieldNamesList The list of field names to add to.
     * 
     * @param typeNamesList The list of type names to add to.
     * 
     * @param paramsMatcher The original parameters matcher. group(1) contains
     *            the first field name, group(2) contains the first field type
     *            name.
     */
    private void processRecord(List<String> fieldNamesList, List<Object> typeNamesList, Matcher paramsMatcher)
    {
        fieldNamesList.add(paramsMatcher.group(1));
        typeNamesList.add(paramsMatcher.group(2));
        String toSplit = paramsMatcher.group(3);

        if (toSplit != null)
        {
        	// TODO this pattern only extracts "aaa VARCHAR, bbb varchar" 
        	//                            from "aaaa VARCHAR2(20), bbb varchar(20)"
        	// should fix this to extract the varchar qualifiers
            final Pattern individualParams = Pattern
                    .compile(",[^\\w\\.\\%\\d_,]*([\\w\\.\\%\\d_]+)\\s+([\\w\\.\\%\\d_]+)[^\\w\\.\\%\\d_,]*.*");

            Matcher individualParamsMatcher = individualParams.matcher(toSplit);
            int findLocation = 0;
            while (individualParamsMatcher.find(findLocation))
            {
                fieldNamesList.add(individualParamsMatcher.group(1));
                typeNamesList.add(individualParamsMatcher.group(2));
                findLocation = individualParamsMatcher.end(2);
            }
        }
    }

    private String parseTypeFrom(String returnType)
    {
        String returnTypeName = null;
        Pattern pattern = Pattern.compile("\\s*([^(;]+).*");
        Matcher m = pattern.matcher(returnType);
        if (m.matches())
        {
            returnTypeName = m.group(1);
        }
        return returnTypeName;
    }

    /**
     * This method stores a new type in the PlSqlType manager.
     * 
     * @param name The name of the new type.
     * @param containedTypes The types contained by this type.
     * @param typeType This is TABLE, VARRAY, OBJECT or RECORD
     * @param indexType This is how the TABLE or VARRAY is indexed, o/w it
     *            should be null and will be ignored.
     * @return The newly created, or previously stored type.
     */
    public PlSqlType storeType(String schemaName,
                               String packageName,
                               String name,
                               String[] fieldNames,
                               String[] containedTypes,
                               String typeType,
                               PlSqlType indexType)
    {
        PlSqlType type = getType(schemaName, packageName, name);
        if (type == null)
        {
        	boolean isAddingType = true;
            if (typeType != null
                    && (typeType.equals(ArrayPlSqlType.TABLE) || typeType
                            .equals(ArrayPlSqlType.VARRAY)))
            {
                type = new ArrayPlSqlType(schemaName, packageName, name, containedTypes[0],
                        typeType, indexType);
            }
            else if (typeType != null
                    && (typeType.equals(RecordPlSqlType.RECORD) || typeType
                            .equals(RecordPlSqlType.OBJECT)))
            {
                type = new RecordPlSqlType(schemaName, packageName, name, fieldNames,
                        containedTypes);
            }
            else
            {
//                throw new IllegalStateException("The value for the typeType [" + typeType
//                        + "] is invalid");
            	isAddingType = false;
            }
            if (isAddingType)
            {
            	addType(schemaName, packageName, name, type);
            }
        }
        return type;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void addType(String schemaName, String packageName, String name, PlSqlType type)
    {
        if (schemaName == null)
        {
            schemaName = "default";
        }
        if (packageName == null)
        {
            packageName = "default";
        }
        Map<String, Map> schemaMap = (Map<String, Map>) mySchemaAndPackageQualifiedTypes.get(schemaName);
        if (schemaMap == null)
        {
            schemaMap = new HashMap<String, Map>();
            mySchemaAndPackageQualifiedTypes.put(schemaName, schemaMap);
        }
        Map<String, PlSqlType> packageMap = (Map<String, PlSqlType>) schemaMap.get(packageName);
        if (packageMap == null)
        {
            packageMap = new HashMap<String, PlSqlType>();
            schemaMap.put(packageName, packageMap);
        }
        packageMap.put(name, type);
    }

    /**
     * This method gets a fully qualified type name, divides it into its
     * component parts and calls {@link #getType(String, String, String)}. If
     * the name is not fully qualified, an IllegalArgumentException will be
     * thrown.
     * 
     * @param fullyQualifiedName The fully qualified name (schm.pkg.typename).
     * 
     * @return The PlSqlType corresponding to the supplied
     *         <code>fullyQualifiedName</code> or null if it is not stored.
     */
    public PlSqlType getType(String fullyQualifiedName)
    {
        StringTokenizer st = new StringTokenizer(fullyQualifiedName, ".");
        if (st.countTokens() != 3)
        {
            throw new IllegalArgumentException("The supplied fully qualified name ["
                    + fullyQualifiedName + "] is not fully qualified (does not have 3 dots in it)");

        }
        return getType(st.nextToken(), st.nextToken(), st.nextToken());
    }

    /**
     * This method gets the type with the supplied <code>name</code>.
     * 
     * @param name
     * @return the type with the supplied <code>name</code>.
     */
    public PlSqlType getType(String schemaName, String packageName, String name)
    {
        if (schemaName == null)
        {
            schemaName = "default";
        }
        if (packageName == null)
        {
            packageName = "default";
        }
        Map<?, ?> schemaMap = (Map<?, ?>) mySchemaAndPackageQualifiedTypes.get(schemaName);
        if (schemaMap == null)
        {
            if (schemaName.equals("default"))
            {
                return getBaseType(name);
            }
            else
            {
                return getType("default", packageName, name);
            }
        }
        Map<?, ?> packageMap = (Map<?, ?>) schemaMap.get(packageName);
        if (packageMap == null)
        {
            if (schemaName.equals("default") && packageName.equals("default"))
            {
                return getBaseType(name);
            }
            else
            {
                return getType("default", "default", name);
            }
        }
        PlSqlType toReturn = (PlSqlType) packageMap.get(name);
        if (toReturn == null)
        {
            toReturn = getBaseType(name);
        }
        return toReturn;
    }

    /**
     * This method stores a new type in the PlSqlType manager.
     * 
     * @param name The name of the new type.
     * @param parent The parent type of this new type. This will be set if the
     *            type is a subtype.
     * @return The newly created, or previously stored type.
     */
    public PlSqlType storeType(String schemaName, String packageName, String name, String parentName)
    {
        PlSqlType type = getType(schemaName, packageName, name);
        if (type == null)
        {
            type = new PlSqlType(schemaName, packageName, name, parentName);
            addType(schemaName, packageName, name, type);
        }
        return type;
    }

}
