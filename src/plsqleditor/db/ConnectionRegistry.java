package plsqleditor.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 * This class represents the stored connection details that can be used by
 * anyone to reconnect to a remembered database as a particular user.
 * 
 * TODO make a common superclass of this and the schema registry 
 * in preparation for more registry classes.
 * 
 * @author Toby Zines
 */
public class ConnectionRegistry
{
    private IPath                          myFileStorePath;
    private Map<String, ConnectionDetails> myConnectionDetails;
    private List<RegistryUpdateListener>   myListeners;
    private static final Object            OPEN_CONNECTION_DETAILS_LIST  = "<ConnectionDetailsList>";
    private static final Object            CLOSE_CONNECTION_DETAILS_LIST = "</ConnectionDetailsList>";
    private boolean                        myIsUpdated                   = false;

    public interface RegistryUpdateListener
    {
        public void registryUpdated();
    }

    /**
     * This method initiates the connection registry, indicating where
     * the file storage location is.
     * 
     * @param fileStorePath The location to store the registry file.
     */
    public ConnectionRegistry(IPath fileStorePath)
    {
        myFileStorePath = fileStorePath;
        myListeners = new ArrayList<RegistryUpdateListener>();
    }

    /**
     * This method sets the full list of connection details into 
     * myConnectionDetails.
     * 
     * @param connectionDetails
     */
    public void setConnectionDetails(ConnectionDetails[] connectionDetails)
    {
        myIsUpdated = true;
        myConnectionDetails = new HashMap<String,ConnectionDetails>();
        for (int i = 0; i < connectionDetails.length; i++)
        {
            myConnectionDetails.put(connectionDetails[i].getName(),connectionDetails[i]);
        }
    }

    /**
     * This method saves the connection details by writing the 
     * current myConnectionDetails to file.
     */
    public void saveConnectionDetails(boolean updateListeners)
    {
        if (myIsUpdated)
        {
            File connectionDetailsFile = getStorageFile();
            StringBuffer sb = new StringBuffer();

            sb.append(OPEN_CONNECTION_DETAILS_LIST).append("\n");
            for (ConnectionDetails details : myConnectionDetails.values())
            {
                details.writeToBuffer(sb);
            }
            sb.append(CLOSE_CONNECTION_DETAILS_LIST).append("\n");

            String result = sb.toString();

            try
            {
                FileWriter fw = new FileWriter(connectionDetailsFile, false);
                fw.write(result);
                fw.flush();
                fw.close();
                if (updateListeners)
                {
                    updateListeners();
                }
                myIsUpdated = false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addListener(RegistryUpdateListener l)
    {
        myListeners.add(l);
    }

    /**
     * This method updates the registry listeners.
     */
    private void updateListeners()
    {
        for (RegistryUpdateListener l : myListeners)
        {
            l.registryUpdated();
        }
    }

    /**
     * @return The list of connection details objects stored in this registry.
     */
    public ConnectionDetails[] getConnectionDetails()
    {
        if (myConnectionDetails == null)
        {
            try
            {
                File f = getStorageFile();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f)));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    if (line.trim().length() > 0)
                    {
                        sb.append(line);
                    }
                }
                String buffer = sb.toString();
                List<ConnectionDetails> details = new ArrayList<ConnectionDetails>();
                int location = 0;
                while (location < buffer.length())
                {
                    ConnectionDetails sd = new ConnectionDetails("", "", "", "");
                    location = sd.readFromBuffer(buffer, location);
                    if (sd.getName().trim().length() > 0)
                    {
                        details.add(sd);
                    }
                }
                setConnectionDetails(details.toArray(new ConnectionDetails[details.size()]));
            }
            catch (IOException e)
            {
                myConnectionDetails = new HashMap<String,ConnectionDetails>();
            }
        }
        return myConnectionDetails.values().toArray(new ConnectionDetails[myConnectionDetails.size()]);
    }

    /**
     * @return The file that stores this schema registry.
     */
    private File getStorageFile()
    {
        return myFileStorePath.append("ConnectionDetailsFile.store").toFile();
    }

    void setUpdated(boolean b)
    {
        myIsUpdated = b;
    }

    /**
     * This method adds a set of connection details to the class
     * if there is not already a connection details with the supplied
     * connect string.
     * 
     * @param name The display name for the connection details.
     * @param connectString The connect string (for jdbc).
     * @param schemaName The schema name
     * @param password The password for the schema.
     * @return The created connection details
     * @throws IllegalStateException when the connect string 
     * already exists.
     */
    public ConnectionDetails addConnectionDetails(String name,
                                                  String connectString,
                                                  String schemaName,
                                                  String password)
    {
        if (myConnectionDetails.containsKey(name))
        {
            throw new IllegalStateException("The connection details with name [" +
                                            name + "] are already stored.");
        }
        ConnectionDetails cd = new ConnectionDetails(name, connectString, schemaName, password);
        myConnectionDetails.put(name, cd);
        setUpdated(true);
        return cd;
    }
    
    /**
     * This method gets the connection details object with the supplied 
     * name, or null if it does not exist.
     * 
     * @param name The name of the connection details whose remaining details are sought.
     * @return the connection details object with the supplied 
     * name, or null if it does not exist.
     */
    public ConnectionDetails getConnectionDetailsByName(String name)
    {
        return myConnectionDetails.get(name);
    }
    
    public ConnectionDetails removeConnectionDetails(String name)
    {
        myIsUpdated = true;
        return myConnectionDetails.remove(name);
    }

    /**
     * This method gets the connection details object with the supplied 
     * connect string, or null if it does not exist.
     * 
     * @param connectString The connect string whose remaining details 
     *        are sought.
     * @return the connection details object with the supplied 
     * connect string, or null if it does not exist.
     */
    public ConnectionDetails getConnectionDetailsByConnectString(String connectString)
    {
        for (ConnectionDetails details : myConnectionDetails.values())
        {
            if (details.getConnectString().equals(connectString))
            {
                return details;
            }
        }
        return null;
    }

    public ConnectionDetails updateConnectionDetails(String name,
                                                     String url,
                                                     String schemaName,
                                                     String password)
    {
        ConnectionDetails cd = getConnectionDetailsByName(name);
        cd.setConnectString(url);
        cd.setSchemaName(schemaName);
        cd.setPassword(password);
        myIsUpdated = true;
        return cd;
    }

    public void removeListener(RegistryUpdateListener l)
    {
        myListeners.remove(l);
    }
}
