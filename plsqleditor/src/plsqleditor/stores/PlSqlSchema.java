package plsqleditor.stores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 8/03/2005
 * 
 */
public class PlSqlSchema
{
    private String myName;
    private List   mySources = new ArrayList();
    private Map    myPackages;

    public PlSqlSchema(String name, Source source)
    {
        // TODO, put back in for 1.4
        // assert (name != null) : "Name is null";
        myName = name;
        if (source != null)
        {
            mySources.add(source);
        }
        myPackages = new HashMap();
    }

    /**
     * This method returns the packages.
     * 
     * @return {@link #myPackages}.
     */
    public Map getPackages()
    {
        return myPackages;
    }

    public PlSqlPackage getPackage(String name)
    {
        return (PlSqlPackage) myPackages.get(name);
    }

    public PlSqlPackage addPackage(PlSqlPackage pkg)
    {
        return (PlSqlPackage) myPackages.put(pkg.getName(), pkg);
    }

    public void removePackage(PlSqlPackage pkg)
    {
        myPackages.remove(pkg.getName());
    }
    
    public int size()
    {
        return myPackages.size();
    }
    
    /**
     * This method sets the ...
     * 
     * @param packages The packages to set.
     */
    public void setPackages(Map packages)
    {
        myPackages = packages;
    }


    /**
     * This method returns the source.
     * 
     * @return {@link #mySources}.
     */
    public Source[] getSources()
    {
        return (Source[]) mySources.toArray(new Source[mySources.size()]);
    }


    /**
     * This method sets the ...
     * 
     * @param source The source to set.
     */
    public void addSource(Source source)
    {
        if (!mySources.contains(source))
        {
            mySources.add(source);
            Source toRemove = null;
            for (Iterator it = mySources.iterator(); it.hasNext();)
            {
                Source src = (Source) it.next();
                IPath path = src.getSource();
                if (path == null || path.toString().trim().length() == 0)
                {
                    toRemove = src;
                    break;
                }
            }
            if (toRemove != null)
            {
                mySources.remove(toRemove);
            }
        }
    }


    /**
     * This method returns the name.
     * 
     * @return {@link #myName}.
     */
    public String getName()
    {
        return myName;
    }

}
