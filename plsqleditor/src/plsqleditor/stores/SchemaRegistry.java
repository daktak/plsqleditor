/**
 * 
 */
package plsqleditor.stores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import plsqleditor.preferences.entities.SchemaDetails;

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
public class SchemaRegistry
{
    private IPath                        myFileStorePath;
    private SchemaDetails[]              mySchemaMappings;
    private List<RegistryUpdateListener> myListeners;
    private static final Object          OPEN_SCHEMA_DETAILS_LIST  = "<SchemaDetailsList>";
    private static final Object          CLOSE_SCHEMA_DETAILS_LIST = "</SchemaDetailsList>";
    private boolean                      myIsUpdated = false;

    public interface RegistryUpdateListener
    {
        public void registryUpdated();
    }

    /**
     * @param fileStorePath
     */
    public SchemaRegistry(IPath fileStorePath)
    {
        myFileStorePath = fileStorePath;
        myListeners = new ArrayList<RegistryUpdateListener>();
    }

    /**
     * @param schemaDetails
     */
    public void setSchemaMappings(SchemaDetails[] schemaDetails)
    {
        myIsUpdated = true;
        List<SchemaDetails> schemaMappings = new ArrayList<SchemaDetails>();
        SortedSet<String> names = new TreeSet<String>();
        for (SchemaDetails sd : schemaDetails)
        {
            if (!names.contains(sd.getName()))
            {
                schemaMappings.add(sd);
                names.add(sd.getName());
            }
        }
        mySchemaMappings = schemaMappings.toArray(new SchemaDetails[schemaMappings.size()]);
    }

    /**
     * @throws CoreException
     * 
     */
    public void saveSchemaMappings(boolean updateListeners)
    {
        if (myIsUpdated)
        {
            File schemaMappingsFile = getStorageFile();
            StringBuffer sb = new StringBuffer();

            sb.append(OPEN_SCHEMA_DETAILS_LIST).append("\n");
            for (SchemaDetails details : mySchemaMappings)
            {
                details.writeToBuffer(sb);
            }
            sb.append(CLOSE_SCHEMA_DETAILS_LIST).append("\n");

            String result = sb.toString();

            try
            {
                FileWriter fw = new FileWriter(schemaMappingsFile, false);
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
     * @return
     */
    public SchemaDetails[] getSchemaMappings()
    {
        if (mySchemaMappings == null)
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
                List<SchemaDetails> details = new ArrayList<SchemaDetails>();
                int location = 0;
                while (location < buffer.length())
                {
                    SchemaDetails sd = new SchemaDetails("", new ArrayList<String>(), "");
                    location = sd.readFromBuffer(buffer, location);
                    if (sd.getName().trim().length() > 0)
                    {
                        details.add(sd);
                    }
                }
                mySchemaMappings = details.toArray(new SchemaDetails[details.size()]);
            }
            catch (IOException e)
            {
                mySchemaMappings = new SchemaDetails[0];
            }
        }
        return mySchemaMappings;
    }

    /**
     * This method returns the list of currently known schema names.
     * 
     * @return The list of currently known schema names.
     */
    public String getPasswordForSchema(String schema)
    {
        SchemaDetails[] details = getSchemaMappings();
        for (SchemaDetails sd : details)
        {
            if (sd.getName().equals(schema))
            {
                return sd.getPassword();
            }
        }
        return null;
    }

    /**
     * @return
     */
    private File getStorageFile()
    {
        return myFileStorePath.append("schemaMappingsFile.store").toFile();
    }
}
