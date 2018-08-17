package org.boomsticks.plsqleditor.dialogs.openconnections;

/**
 * A LiveConnection has the following properties: type, url, filename and timeUp
 *
 * @author Toby Zines
 */
public class LiveConnection
{

    public static final String DBA_CONNECTION    = "DbaConnection";
    public static final String SCHEMA_CONNECTION = "SchemaConnection";
    public static final String FILE_CONNECTION   = "FileConnection";

    private String             type              = "";                // dba
                                                                       // connection,
                                                                       // schema
                                                                       // connection,
    // specific connection
    private String             url               = "";
    private String             user              = "";
    private String             filename          = "?";
    private String             project           = "?";

    /**
     * Create a task with an initial description
     *
     * @param url
     */
    public LiveConnection(String url)
    {

        super();
        setUrl(url);
    }

    /**
     * @return true if completed, false otherwise
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return String task description
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return String task owner
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @return The project owning the connection.
     *
     */
    public String getProject()
    {
        return project;
    }

    /**
     * Set the 'type' property
     *
     * @param b
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Set the 'url' property
     *
     * @param string
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * Set the 'filename' property
     *
     * @param string
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Set the 'project' property
     *
     * @param i
     */
    public void setProject(String i)
    {
        project = i;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the user
     */
    public String getUser()
    {
        return user;
    }

}
