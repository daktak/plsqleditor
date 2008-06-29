//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\DelimiterManager.java

package plsqleditor.parser.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import plsqleditor.parser.util.IDelimiterSpecification;

public class DelimiterManager
{
    private static DelimiterManager   theInstance;
    private Map                       myDelimitersMap;
    private IDelimiterSpecification[] myDelimiters = null;

    /**
     * @roseuid 431A54AC01C5
     */
    private DelimiterManager()
    {
        myDelimitersMap = new HashMap();
    }

    public static DelimiterManager instance()
    {
        if (theInstance == null)
        {
            theInstance = new DelimiterManager();
        }
        return theInstance;
    }

    /**
     * @param spec
     * @roseuid 4313B344032C
     */
    public void addDelimiterSpec(DelimiterSpec spec)
    {
        myDelimitersMap.put(spec.getString(), spec);
        myDelimiters = null;
    }

    /**
     * @param spec
     * @return boolean
     * @roseuid 4313B3590148
     */
    public boolean isDelimiter(String spec)
    {
        return myDelimitersMap.containsKey(spec);
    }

    /**
     * This method gets the list of delimiters that start with the characters
     * specified in the given string.
     * 
     * @param delimiterPrefix
     * @return plsqleditor.parser.util.IDelimiterSpecification[]
     * @roseuid 4313B39801E4
     */
    public IDelimiterSpecification[] getPossibleDelimiters(String delimiterPrefix)
    {
        List toReturn = new ArrayList();
        for (Iterator it = myDelimitersMap.values().iterator(); it.hasNext();)
        {
            DelimiterSpec spec = (DelimiterSpec) it.next();
            if (spec.getString().startsWith(delimiterPrefix))
            {
                toReturn.add(spec);
            }
        }
        return (IDelimiterSpecification[]) toReturn.toArray(new IDelimiterSpecification[toReturn
                .size()]);
    }

    public IDelimiterSpecification[] getAllDelimiters()
    {
        if (myDelimiters == null)
        {
            myDelimiters = (IDelimiterSpecification[]) myDelimitersMap.values()
                    .toArray(new IDelimiterSpecification[myDelimitersMap.size()]);
        }
        return myDelimiters;
    }
}
