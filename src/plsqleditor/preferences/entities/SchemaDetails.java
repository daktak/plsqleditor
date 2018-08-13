package plsqleditor.preferences.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * This class represents a schema and the details it contains.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 */
public class SchemaDetails implements Cloneable
{
    private static final String  OPEN_SCHEMA_DETAILS  = "<SchemaDetails>";
    private static final String  CLOSE_SCHEMA_DETAILS = "</SchemaDetails>";
    private static final String  OPEN_NAME            = "<Name>";
    private static final String  CLOSE_NAME           = "</Name>";
    private static final String  OPEN_LOCATIONS       = "<Locations>";
    private static final String  CLOSE_LOCATIONS      = "</Locations>";
    private static final String  OPEN_LOCATION        = "<Location>";
    private static final String  CLOSE_LOCATION       = "</Location>";
    private static final String  OPEN_PASSWORD        = "<Password>";
    private static final String  CLOSE_PASSWORD       = "</Password>";
    private static final String  OPEN_PACKAGES        = "<Packages>";
    private static final String  CLOSE_PACKAGES       = "</Packages>";

    private String               myName;
    private String               myPassword;
    private List<String>         myLocations;
    private List<PackageDetails> myPackageDetails;

    public SchemaDetails(String name, List<String> locations, String password)
    {
        myName = name;
        myLocations = locations;
        myPassword = password;
        myPackageDetails = new ArrayList<PackageDetails>();
    }

    /**
     * This method returns the location.
     * 
     * @return {@link #myLocations}.
     */
    public List<String> getLocations()
    {
        return myLocations;
    }

    public String getLocationString()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> it = getLocations().iterator(); it.hasNext();)
        {
            String loc = it.next();
            sb.append(loc).append(",");
        }
        if (sb.length() > 0)
        {
            return sb.toString().substring(0, sb.length() - 1);
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * This method sets the ...
     * 
     * @param location The location to set.
     */
    public void addLocation(String location)
    {
        if (!myLocations.contains(location))
        {
            myLocations.add(location);
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
     * This method returns the password.
     * 
     * @return {@link #myPassword}.
     */
    public String getPassword()
    {
        return myPassword;
    }


    /**
     * This method sets the ...
     * 
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
        myPassword = password;
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
        SchemaDetails det = new SchemaDetails(myName, myLocations, myPassword);
        for (Iterator<PackageDetails> it = myPackageDetails.iterator(); it.hasNext();)
        {
            PackageDetails pd = it.next();
            det.addPackage((PackageDetails) pd.clone());
        }
        return det;
    }


    /**
     * @return The list of package details in this schema.
     */
    public PackageDetails[] getPackages()
    {
        return myPackageDetails.toArray(new PackageDetails[myPackageDetails
                .size()]);
    }

    /**
     * @param details
     */
    public void removePackage(PackageDetails details)
    {
        myPackageDetails.remove(details);
    }

    /**
     * @param sb
     */
    public void writeToBuffer(StringBuffer sb)
    {
        sb.append(OPEN_SCHEMA_DETAILS);
        sb.append(OPEN_NAME);
        sb.append(myName);
        sb.append(CLOSE_NAME);
        sb.append(OPEN_LOCATIONS);
        for (Object element : myLocations)
        {
            String loc = (String) element;
            sb.append(OPEN_LOCATION);
            sb.append(loc);
            sb.append(CLOSE_LOCATION);
        }
        sb.append(CLOSE_LOCATIONS);
        sb.append(OPEN_PASSWORD);
        sb.append(myPassword);
        sb.append(CLOSE_PASSWORD).append("\n");
        sb.append(OPEN_PACKAGES).append("\n");
        for (Iterator<PackageDetails> it = myPackageDetails.iterator(); it.hasNext();)
        {
            PackageDetails p = it.next();
            p.writeToBuffer(sb);
        }
        sb.append(CLOSE_PACKAGES).append("\n");
        sb.append(CLOSE_SCHEMA_DETAILS);
    }

    public int readFromBuffer(String sb, int location)
    {
        String substring = sb.substring(location);
        int end = substring.length();

        Pattern p = Pattern.compile(OPEN_SCHEMA_DETAILS + OPEN_NAME + "([^<]*?)" + CLOSE_NAME
                + OPEN_LOCATIONS + "(.*?)" + CLOSE_LOCATIONS + OPEN_PASSWORD + "([^<]*?)"
                + CLOSE_PASSWORD + OPEN_PACKAGES + "(.*?)" + CLOSE_PACKAGES + CLOSE_SCHEMA_DETAILS);
        Matcher m = p.matcher(substring);
        if (m.find())
        {
            myName = m.group(1);
            String locations = m.group(2);
            Pattern locPattern = Pattern.compile(OPEN_LOCATION + "([^<]*?)" + CLOSE_LOCATION);
            Matcher locMatcher = locPattern.matcher(locations);
            myLocations = new ArrayList<String>();
            while (locMatcher.find())
            {
                myLocations.add(locMatcher.group(1));
            }

            myPassword = m.group(3);
            String packageDetails = m.group(4);
            int pLocation = 0;
            int length = packageDetails.length();
            while (pLocation < length)
            {
                PackageDetails pd = new PackageDetails("", "", null);
                pLocation = pd.readFromBuffer(packageDetails, pLocation);
                if (pd.getName().trim().length() > 0)
                {
                    addPackage(pd);
                }
            }
            end = m.end();
        }
        return location + end;
    }

    /**
     * @param pd
     * @return <code>true</code> if the package was added and <code>false</code>
     *         otherwise.
     */
    public boolean addPackage(PackageDetails pd)
    {
        String pdName = pd.getName();
        PackageLocation[] newLocations = pd.getLocations();
        boolean[] alreadyThere = new boolean[newLocations.length];
        boolean somethingNew = false;
        for (Iterator<PackageDetails> it = myPackageDetails.iterator(); it.hasNext();)
        {
            PackageDetails details = it.next();
            if (details.getName().equals(pdName))
            {
                PackageLocation[] locations = details.getLocations();
                for (int i = 0; i < locations.length; i++)
                {
                    for (int j = 0; j < newLocations.length; j++)
                    {
                        PackageLocation newLocation = newLocations[j];
                        if (locations[i].getLocation().equals(newLocation.getLocation()))
                        {
                            alreadyThere[j] = true;
                            break;
                        }
                    }
                }
                for (int i = 0; i < alreadyThere.length; i++)
                {
                    if (!alreadyThere[i])
                    {
                        somethingNew = true;
                    }
                }
                if (somethingNew)
                {
                    // TODO could optimise
                    for (int i = 0; i < newLocations.length; i++)
                    {
                        details.addLocation(newLocations[i]);
                    }
                }
                return somethingNew;
            }
        }
        myPackageDetails.add(pd);
        return true;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof SchemaDetails))
        {
            return false;
        }
        SchemaDetails rhs = (SchemaDetails) obj;
        return new EqualsBuilder().append(this.myName, rhs.myName).append(this.myPackageDetails,
                                                                          rhs.myPackageDetails)
                .append(this.myLocations, rhs.myLocations).append(this.myPassword, rhs.myPassword)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myName).append(this.myPackageDetails)
                .append(this.myLocations).append(this.myPassword).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        SchemaDetails rhs = (SchemaDetails) o;
        return new CompareToBuilder().append(this.myName, rhs.myName).append(this.myPackageDetails,
                                                                             rhs.myPackageDetails)
                .append(this.myLocations, rhs.myLocations).append(this.myPassword, rhs.myPassword)
                .toComparison();
    }
}
