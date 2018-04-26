package plsqleditor.stores;

import java.util.StringTokenizer;

/**
 * This class represents a PlSqlType - both the built-in pl/sql types and types
 * that are declared within packages.
 * 
 * @author Toby Zines
 */
public class PlSqlType
{
    /**
     * This is the name of the schema that owns this type.
     */
    private String mySchemaName;

    /**
     * This is the name of the package that owns this type.
     */
    private String myPackageName;

    /**
     * This is a fully qualified parent type name.
     */
    private String myParentTypeName = null;

    /**
     * This is the name of the type.
     */
    private String myName;

    /**
     * This constructor creates the plsql type.
     * 
     * @param schemaName The schema that owns this type.
     * 
     * @param packageName The package that owns this type.
     * 
     * @param name The name of this type.
     * 
     * @param parentTypeName This is a qualified parent type name. The
     *            qualification could be full, partial or none at all. For
     *            example, it could be schmName.pkgName.typeName, or
     *            pkgName.typeName, or typeName. It will be converted to a fully
     *            qualified name (based on the supplied <code>schemaName</code>,
     *            <code>packageName</code> and <code>parentTypeName</code>.
     */
    public PlSqlType(String schemaName, String packageName, String name, String parentTypeName)
    {
        mySchemaName = schemaName;
        myPackageName = packageName;
        if (parentTypeName == null)
        {
            throw new IllegalStateException("Cannot have null parent");
        }
        myName = name;
        myParentTypeName = parentTypeName;

        StringTokenizer strTok = new StringTokenizer(myParentTypeName, ".");
        int numTokens = strTok.countTokens();
        if (numTokens == 3)
        {
            // do nothing, myParentTypeName is correct
        }
        else if (numTokens == 2)
        {
            myParentTypeName = mySchemaName + "." + myParentTypeName;
        }
        else if (numTokens == 1)
        {
            myParentTypeName = mySchemaName + "." + myPackageName + "." + myParentTypeName;
        }
    }

    /**
     * This constructor creates a base type.
     * 
     * @param name
     */
    PlSqlType(String schemaName, String packageName, String name)
    {
        mySchemaName = schemaName;
        myPackageName = packageName;
        myName = name;
    }

    /**
     * This method gets the name of this type.
     * 
     * @return {@link #myName}.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * This method gets the name of the package owning this type.
     * 
     * @return {@link #myPackageName}.
     */
    public String getPackageName()
    {
        return myPackageName;
    }

    /**
     * This method gets the name of the schema owning this type.
     * 
     * @return {@link #mySchemaName}.
     */
    public String getSchemaName()
    {
        return mySchemaName;
    }

    /**
     * This method gets the (possibly qualified) name of the parent type of this
     * type.
     * 
     * @return {@link #myParentTypeName}.
     */
    public String getParentTypeName()
    {
        return myParentTypeName;
    }

    /**
     * This method indicates whether this plsql type is an aggregate (composite
     * or array type) or not (standard type).
     * 
     * @return <code>true</code> if this is an aggregate type, such as an
     *         object, record, table or array.
     */
    public boolean isAggregate()
    {
        return false;
    }

    /**
     * This returns the list of fields to use in auto completion for this type.
     * It defaults to an empty array in this class.
     * 
     * @return the list of fields to use in auto completion for this type.
     */
    public String[] getAutoCompleteFields()
    {
        return new String[0];
    }
}
