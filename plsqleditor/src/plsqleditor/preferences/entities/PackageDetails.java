/**
 * 
 */
package plsqleditor.preferences.entities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;


/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 4/03/2005
 * 
 */
public class PackageDetails implements Cloneable
{
    private static final String OPEN_PACKAGE_DETAILS  = "<PackageDetails>";
    private static final String CLOSE_PACKAGE_DETAILS = "</PackageDetails>";
    private static final String OPEN_NAME             = "<PackageName>";
    private static final String CLOSE_NAME            = "</PackageName>";
    private static final String OPEN_LOCATION         = "<PackageLocation>";
    private static final String CLOSE_LOCATION        = "</PackageLocation>";

    private String              myName;
    private String              myLocation;

    public PackageDetails(String name, String location)
    {
        myName = name;
        myLocation = location;
    }

    /**
     * This method returns the location.
     * 
     * @return {@link #myLocation}.
     */
    public String getLocation()
    {
        return myLocation;
    }


    /**
     * This method sets the ...
     * 
     * @param location The location to set.
     */
    public void setLocation(String location)
    {
        myLocation = location;
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


    /**
     * This method sets the ...
     * 
     * @param name The name to set.
     */
    public void setName(String name)
    {
        myName = name;
    }


    /**
     * @return
     */
    public ImageDescriptor getImageDescriptor()
    {
        return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
    }

    public Object clone()
    {
        return new PackageDetails(myName, myLocation);
    }

    /**
     * @param sb
     */
    public void writeToBuffer(StringBuffer sb)
    {
        sb.append(OPEN_PACKAGE_DETAILS);
        sb.append(OPEN_NAME);
        sb.append(myName);
        sb.append(CLOSE_NAME);
        sb.append(OPEN_LOCATION);
        sb.append(myLocation);
        sb.append(CLOSE_LOCATION);
        sb.append(CLOSE_PACKAGE_DETAILS).append("\n");
    }

    public int readFromBuffer(String sb, int location)
    {
        String substring = sb.substring(location);
        int end = location + substring.length();

        Pattern p = Pattern.compile(OPEN_PACKAGE_DETAILS + OPEN_NAME + "([^<]*?)" + CLOSE_NAME
                + OPEN_LOCATION + "([^<]*?)" + CLOSE_LOCATION + CLOSE_PACKAGE_DETAILS);
        Matcher m = p.matcher(substring);
        if (m.find())
        {
            myName = m.group(1);
            myLocation = m.group(2);
            end = m.end();
        }
        return location + end;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof PackageDetails))
        {
            return false;
        }
        PackageDetails rhs = (PackageDetails) obj;
        return new EqualsBuilder().append(this.myName, rhs.myName).append(this.myLocation,
                                                                          rhs.myLocation)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myName).append(this.myLocation)
                .toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        PackageDetails rhs = (PackageDetails) o;
        return new CompareToBuilder().append(this.myName, rhs.myName).append(this.myLocation,
                                                                             rhs.myLocation)
                .toComparison();
    }

}
