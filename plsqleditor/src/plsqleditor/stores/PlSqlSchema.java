package plsqleditor.stores;

import java.util.ArrayList;
import java.util.HashMap;
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
    private String                    myName;
    private List<Source>              mySources = new ArrayList<Source>();
    private Map<String, PlSqlPackage> myPackages;

    public PlSqlSchema(String name, Source source)
    {
        assert (name != null) : "Name is null";
        myName = name;
        if (source != null)
        {
            mySources.add(source);
        }
        myPackages = new HashMap<String, PlSqlPackage>();
    }

    /**
     * This method returns the packages.
     * 
     * @return {@link #myPackages}.
     */
    public Map<String, PlSqlPackage> getPackages()
    {
        return myPackages;
    }

    public PlSqlPackage getPackage(String name)
    {
        return myPackages.get(name);
    }

    public PlSqlPackage addPackage(PlSqlPackage pkg)
    {
        return myPackages.put(pkg.getName(), pkg);
    }

    /**
     * This method sets the ...
     * 
     * @param packages The packages to set.
     */
    public void setPackages(Map<String, PlSqlPackage> packages)
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
        return mySources.toArray(new Source[mySources.size()]);
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
            for (Source src : mySources)
            {
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
