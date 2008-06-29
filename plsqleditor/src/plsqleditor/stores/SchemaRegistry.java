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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private IPath               myFileStorePath;
    private SchemaDetails[]     mySchemaMappings;
    private List                myListeners;
    private static final Object OPEN_SCHEMA_DETAILS_LIST  = "<SchemaDetailsList>";
    private static final Object CLOSE_SCHEMA_DETAILS_LIST = "</SchemaDetailsList>";
    private boolean             myIsUpdated               = false;
    private String              myProjectName;

    public interface RegistryUpdateListener
    {
        public void registryUpdated();
    }

    /**
     * @param fileStorePath
     */
    public SchemaRegistry(String projectName, IPath fileStorePath)
    {
        myProjectName = projectName;
        myFileStorePath = fileStorePath;
        myListeners = new ArrayList();
    }

    /**
     * @param schemaDetails
     */
    public void setSchemaMappings(SchemaDetails[] schemaDetails)
    {
        myIsUpdated = true;
        List schemaMappings = new ArrayList();
        SortedSet names = new TreeSet();
        for (int i = 0; i < schemaDetails.length; i++)
        {
            SchemaDetails sd = schemaDetails[i];
            if (!names.contains(sd.getName()))
            {
                schemaMappings.add(sd);
                names.add(sd.getName());
            }
        }
        mySchemaMappings = (SchemaDetails[]) schemaMappings.toArray(new SchemaDetails[schemaMappings.size()]);
    }

    /**
     */
    public void saveSchemaMappings(boolean updateListeners)
    {
        if (myIsUpdated)
        {
            File schemaMappingsFile = getStorageFile();
            StringBuffer sb = new StringBuffer();

            sb.append(OPEN_SCHEMA_DETAILS_LIST).append("\n");
            for (int i = 0; i < mySchemaMappings.length; i++)
            {
                SchemaDetails details = mySchemaMappings[i];
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
        for (Iterator it = myListeners.iterator(); it.hasNext();)
        {
            RegistryUpdateListener l = (RegistryUpdateListener) it.next();
            l.registryUpdated();
        }
    }

    /**
     * @return The list of schema details objects stored in this registry.
     */
    public SchemaDetails[] getSchemaMappings()
    {
        if (mySchemaMappings == null)
        {
            try
            {
                File f = getStorageFile();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
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
                List details = new ArrayList();
                int location = 0;
                while (location < buffer.length())
                {
                    SchemaDetails sd = new SchemaDetails("", new ArrayList(), "");
                    location = sd.readFromBuffer(buffer, location);
                    if (sd.getName().trim().length() > 0)
                    {
                        details.add(sd);
                    }
                }
                mySchemaMappings = (SchemaDetails[]) details.toArray(new SchemaDetails[details.size()]);
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
        for (int i = 0; i < details.length; i++)
        {
            SchemaDetails sd = details[i];
            if (sd.getName().equals(schema))
            {
                return sd.getPassword();
            }
        }
        return null;
    }

    /**
     * @return The file that stores this schema registry.
     */
    private File getStorageFile()
    {
        return myFileStorePath.append(myProjectName + "_schemaMappingsFile.store").toFile();
    }

    void setUpdated(boolean b)
    {
        myIsUpdated = b;
    }
}
