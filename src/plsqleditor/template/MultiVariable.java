package plsqleditor.template;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.templates.TemplateVariable;


/**
 * This class represents a template variable that can store multiple entries.
 * It is copied from the jdt ui packages.
 */
public class MultiVariable extends TemplateVariable
{
    private final Map myValueMap   = new HashMap();
    private Object    mySet;
    private Object    myDefaultKey = null;

    public MultiVariable(String type, String defaultValue, int[] offsets)
    {
        super(type, defaultValue, offsets);
        myValueMap.put(myDefaultKey, new String[]{defaultValue});
        mySet = getDefaultValue();
    }

    /**
     * Sets the values of this variable under a specific set.
     * 
     * @param set the set identifier for which the values are valid
     * @param values the possible values of this variable
     */
    public void setValues(Object set, String[] values)
    {
        Assert.isNotNull(set);
        Assert.isTrue(values.length > 0);
        myValueMap.put(set, values);
        if (myDefaultKey == null)
        {
            myDefaultKey = set;
            mySet = getDefaultValue();
        }
    }


    /*
     * @see org.eclipse.jface.text.templates.TemplateVariable#setValues(java.lang.String[])
     */
    public void setValues(String[] values)
    {
        if (myValueMap != null)
        {
            Assert.isNotNull(values);
            Assert.isTrue(values.length > 0);
            myValueMap.put(myDefaultKey, values);
            mySet = getDefaultValue();
        }
    }


    /*
     * @see org.eclipse.jface.text.templates.TemplateVariable#getValues()
     */
    public String[] getValues()
    {
        return (String[]) myValueMap.get(myDefaultKey);
    }

    /**
     * Returns the choices for the set identified by <code>set</code>.
     * 
     * @param set the set identifier
     * @return the choices for this variable and the given set, or
     *         <code>null</code> if the set is not defined.
     */
    public String[] getValues(Object set)
    {
        return (String[]) myValueMap.get(set);
    }

    /**
     * @return
     */
    public Object getSet()
    {
        return mySet;
    }

    public void setSet(Object set)
    {
        mySet = set;
    }
}
