package plsqleditor.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This class represents a single set of connection details.
 */
public class ConnectionDetails
{
    private static final String OPEN_CONNECTION_DETAILS  = "<ConnectionDetails>";
    private static final String CLOSE_CONNECTION_DETAILS = "</ConnectionDetails>";
    private static final String OPEN_NAME                = "<ConnectionName>";
    private static final String CLOSE_NAME               = "</ConnectionName>";
    private static final String OPEN_CONNECTION_STRING   = "<ConnectionString>";
    private static final String CLOSE_CONNECTION_STRING  = "</ConnectionString>";
    private static final String OPEN_SCHEMA              = "<ConnectionSchema>";
    private static final String CLOSE_SCHEMA             = "</ConnectionSchema>";
    private static final String OPEN_PASSWORD            = "<ConnectionPassword>";
    private static final String CLOSE_PASSWORD           = "</ConnectionPassword>";

    private String              myName;
    private String              myConnectString;
    private String              mySchemaName;
    private String              myPassword;

    public ConnectionDetails(String name, String connectString, String schema, String password)
    {
        myName = name;
        mySchemaName = schema;
        myPassword = password;
        myConnectString = connectString;
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
    protected void setName(String name)
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
    protected void setPassword(String password)
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
        ConnectionDetails det = new ConnectionDetails(myName, myConnectString, mySchemaName, myPassword);
        return det;
    }

    /**
     * @param sb
     */
    public void writeToBuffer(StringBuffer sb)
    {
        sb.append(OPEN_CONNECTION_DETAILS);
        sb.append(OPEN_NAME);
        sb.append(myName);
        sb.append(CLOSE_NAME);
        sb.append(OPEN_CONNECTION_STRING);
        sb.append(myConnectString);
        sb.append(CLOSE_CONNECTION_STRING);
        sb.append(OPEN_SCHEMA);
        sb.append(mySchemaName);
        sb.append(CLOSE_SCHEMA);
        sb.append(OPEN_PASSWORD);
        sb.append(myPassword);
        sb.append(CLOSE_PASSWORD).append("\n");
        sb.append(CLOSE_CONNECTION_DETAILS);
    }

    public int readFromBuffer(String sb, int location)
    {
        String substring = sb.substring(location);
        int end = substring.length();

        Pattern p = Pattern.compile(OPEN_CONNECTION_DETAILS + 
                                    OPEN_NAME + "([^<]*?)" + CLOSE_NAME + 
                                    OPEN_CONNECTION_STRING + "([^<]*?)" + CLOSE_CONNECTION_STRING + 
                                    OPEN_SCHEMA + "([^<]*?)" + CLOSE_SCHEMA +
                                    OPEN_PASSWORD + "([^<]*?)" + CLOSE_PASSWORD + CLOSE_CONNECTION_DETAILS);
        Matcher m = p.matcher(substring);
        if (m.find())
        {
            myName = m.group(1);
            myConnectString = m.group(2);
            mySchemaName = m.group(3);
            myPassword = m.group(4);
            end = m.end();
        }
        return location + end;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof ConnectionDetails))
        {
            return false;
        }
        ConnectionDetails rhs = (ConnectionDetails) obj;
        return new EqualsBuilder().append(this.myName, rhs.myName).append(this.myConnectString,
                                                                          rhs.myConnectString)
                .append(this.myPassword, rhs.myPassword).append(this.mySchemaName, rhs.mySchemaName)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myName).append(this.myConnectString)
                .append(this.myPassword).append(this.mySchemaName).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        ConnectionDetails rhs = (ConnectionDetails) o;
        return new CompareToBuilder().append(this.myName, rhs.myName).append(this.myConnectString,
                                                                             rhs.myConnectString)
                .append(this.myPassword, rhs.myPassword).append(this.mySchemaName, rhs.mySchemaName)
                .toComparison();
    }

    /**
     * @return the connectString
     */
    public String getConnectString()
    {
        return myConnectString;
    }

    /**
     * @param connectString the connectString to set
     */
    protected void setConnectString(String connectString)
    {
        myConnectString = connectString;
    }

    /**
     * @return the schemaName
     */
    public String getSchemaName()
    {
        return mySchemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    protected void setSchemaName(String schemaName)
    {
        mySchemaName = schemaName;
    }
}
