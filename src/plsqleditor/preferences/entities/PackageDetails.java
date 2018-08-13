/**
 * 
 */
package plsqleditor.preferences.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import plsqleditor.parsers.ParseType;


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
    private static final String OPEN_LOCATIONS        = "<PackageLocations>";
    private static final String CLOSE_LOCATIONS       = "</PackageLocations>";
    private static final String OPEN_LOCATION         = "<PackageLocation>";
    private static final String CLOSE_LOCATION        = "</PackageLocation>";
    private static final String OPEN_LOCATION_STR     = "<PackageLocationStr>";
    private static final String CLOSE_LOCATION_STR    = "</PackageLocationStr>";
    private static final String OPEN_PARSETYPE        = "<ParseType>";
    private static final String CLOSE_PARSETYPE       = "</ParseType>";

    private String              myName;
    private PackageLocation[]   myPackageLocations;

    public PackageDetails(String name, String location, ParseType parseType)
    {
        myName = name;
        myPackageLocations = new PackageLocation[]{new PackageLocation(location, parseType)};
    }

    public PackageDetails(String name, PackageLocation[] locations)
    {
        myName = name;
        myPackageLocations = (PackageLocation[]) locations.clone();
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
     * @return The image descriptor for this object.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
    }

    public Object clone()
    {
        return new PackageDetails(myName, myPackageLocations);
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
        sb.append(OPEN_LOCATIONS);
        for (int i = 0; i < myPackageLocations.length; i++)
        {
            sb.append(OPEN_LOCATION);
            sb.append(OPEN_LOCATION_STR);
            sb.append(myPackageLocations[i].getLocation());
            sb.append(CLOSE_LOCATION_STR);
            sb.append(OPEN_PARSETYPE);
            sb.append(myPackageLocations[i].getParseType());
            sb.append(CLOSE_PARSETYPE);
            sb.append(CLOSE_LOCATION);
        }
        sb.append(CLOSE_LOCATIONS);
        sb.append(CLOSE_PACKAGE_DETAILS).append("\n");
    }

    public int readFromBuffer(String sb, int location)
    {
        String substring = sb.substring(location);
        int end = location + substring.length();

        Pattern p = Pattern.compile(OPEN_PACKAGE_DETAILS + OPEN_NAME + "([^<]*?)" + CLOSE_NAME
                + OPEN_LOCATIONS + "(.*?)" + CLOSE_LOCATIONS + CLOSE_PACKAGE_DETAILS);
        Pattern locsPattern = Pattern.compile(OPEN_LOCATION_STR + "([^<]*?)" + CLOSE_LOCATION_STR
                + OPEN_PARSETYPE + "([^<]*?)" + CLOSE_PARSETYPE);

        Matcher m = p.matcher(substring);
        if (m.find())
        {
            myName = m.group(1);
            String locations = m.group(2);
            end = m.end();

            m = locsPattern.matcher(locations);
            int currentLoc = 0;
            List locationList = new ArrayList();
            while (m.find(currentLoc))
            {
                String loc = m.group(1);
                String parseType = m.group(2);
                currentLoc = m.end();
                locationList.add(new PackageLocation(loc, ParseType.getParseType(parseType)));
            }
            myPackageLocations = (PackageLocation[]) locationList
                    .toArray(new PackageLocation[locationList.size()]);
        }
        else
        {
            // must still be old version of file
            // this is only to support the first conversion
            p = Pattern.compile(OPEN_PACKAGE_DETAILS + OPEN_NAME + "([^<]*?)" + CLOSE_NAME
                    + OPEN_LOCATION + "([^<]*?)" + CLOSE_LOCATION + CLOSE_PACKAGE_DETAILS);
            m = p.matcher(substring);
            if (m.find())
            {
                myName = m.group(1);
                String locStr = m.group(2);
                ParseType parseType = ParseType.SqlScript;
                if (locStr.indexOf(".pkb") != -1)
                {
                    parseType = ParseType.Package_Body;
                }
                else if (locStr.indexOf(".pkh") != -1)
                {
                    parseType = ParseType.Package;
                }
                else if (locStr.indexOf(".pkg") != -1)
                {
                    parseType = ParseType.Package_Header_And_Body;
                }
                myPackageLocations = new PackageLocation[] {new PackageLocation(locStr, parseType)};
                end = m.end();
            }

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
        return new EqualsBuilder().append(this.myName, rhs.myName).append(this.myPackageLocations,
                                                                          rhs.myPackageLocations)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myName).append(this.myPackageLocations)
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
        return new CompareToBuilder().append(this.myName, rhs.myName)
                .append(this.myPackageLocations, rhs.myPackageLocations).toComparison();
    }

    public PackageLocation[] getLocations()
    {
        return myPackageLocations;
    }
    
    public void setLocations(PackageLocation [] locations)
    {
        myPackageLocations = locations;
    }

    public void addLocation(PackageLocation newLocation)
    {
        String locationStr = newLocation.getLocation();
        for (int i = 0; i < myPackageLocations.length; i++)
        {
            if (myPackageLocations[i].getLocation().equals(locationStr))
            {
                myPackageLocations[i] = newLocation;
                return;
            }
        }
        PackageLocation[] newLocations = new PackageLocation[myPackageLocations.length + 1];
        newLocations[newLocations.length - 1] = newLocation;
        System.arraycopy(myPackageLocations, 0, newLocations, 0, myPackageLocations.length);
        myPackageLocations = newLocations;
    }

    public String getLocationsAsString()
    {
        StringBuffer toReturn = new StringBuffer();
        for (int i = 0; i < myPackageLocations.length; i++)
        {
            PackageLocation loc = myPackageLocations[i];
            String location = loc.getLocation();
            if (location != null)
            {
                toReturn.append(location);
                ParseType parseType = loc.getParseType();
                if (parseType != null)
                {
                    toReturn.append(":");
                    toReturn.append(parseType.toString());
                }
                if (i != myPackageLocations.length - 1)
                {
                    toReturn.append(",");
                }
            }
        }
        return toReturn.toString();
    }
}
