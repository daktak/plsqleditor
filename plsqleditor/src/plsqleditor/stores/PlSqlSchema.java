package plsqleditor.stores;

import java.util.HashMap;
import java.util.Map;

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
    private Source  mySource;
    private Map<String, PlSqlPackage> myPackages;
    
    public PlSqlSchema(String name, Source source)
    {
        myName = name;
        mySource = source;
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
    public void setPackages(Map<String,PlSqlPackage> packages)
    {
        myPackages = packages;
    }
    

    /**
     * This method returns the source.
     * 
     * @return {@link #mySource}.
     */
    public Source getSource()
    {
        return mySource;
    }
    

    /**
     * This method sets the ...
     *
     * @param source The source to set.
     */
    public void setSource(Source source)
    {
        mySource = source;
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
