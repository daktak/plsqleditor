package plsqleditor.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.util.StreamProcessor;

/**
 * This class provides pldoc execute functionality via the O/S's native pldoc
 * application.
 * 
 * @version $Id: PlDocProcessExecutor.java,v 1.1.2.1 2005/11/27 20:52:01 tobyz
 *          Exp $
 * 
 * Created on 16/03/2005
 */
public class PlDocProcessExecutor
{
    public PlDocProcessExecutor()
    {
        //
    }

    public void executePlDoc(IFile[] files)
    {
        String overview = "";
        String docTitle = "";
        String outputDirectory = "";
        String exitOnError = "";
        String definesFile = "";
        String styleSheetFile = "";
        String namesCase = "";

        IPreferenceStore prefs = PlsqleditorPlugin.getDefault().getPreferenceStore();
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        
        String workspacePath = workspaceRoot.getLocation().toOSString();
        System.out.println("Workspace location is " + workspacePath);
        String prefsString = prefs.getString(PreferenceConstants.P_PLDOC_OVERVIEW);
        if (prefsString.length() > 0)
        {
            overview = "-overview " + prefsString + " ";
        }
        prefsString = prefs.getString(PreferenceConstants.P_PLDOC_DOCTITLE);
        if (prefsString.length() > 0)
        {
            docTitle = "-doctitle \"" + prefsString + "\" ";
        }
        if (prefs.getBoolean(PreferenceConstants.P_PLDOC_EXITONERROR))
        {
            exitOnError = "-exitonerror ";
        }
        prefsString = prefs.getString(PreferenceConstants.P_PLDOC_NAMECASE);
        if (prefsString.equals(PreferenceConstants.C_NAMECASE_LOWER))
        {
            namesCase = "-nameslowercase ";
        }
        else if (prefsString.equals(PreferenceConstants.C_NAMECASE_UPPER))
        {
            namesCase = "-namesuppercase ";
        }

        prefsString = prefs.getString(PreferenceConstants.P_PLDOC_DEFINESFILE);
        if (prefsString.length() > 0)
        {
            definesFile = "-definesfile " + prefsString + " ";
        }

        String pldocPath = prefs.getString(PreferenceConstants.P_PLDOC_PATH);
        if (pldocPath.length() == 0)
        {
            PlsqleditorPlugin.log(("Failed to execute Pldoc because pldoc path is not set"), null);
        }
        String extraParams = prefs.getString(PreferenceConstants.P_PLDOC_EXTRA_PARAMS);

        String outputDirectoryUse = prefs.getString(PreferenceConstants.P_PLDOC_OUTPUT_DIR_USE);


        String outputDirString = prefs.getString(PreferenceConstants.P_PLDOC_OUTPUT_DIRECTORY);

        if (outputDirString.length() > 0)
        {
            outputDirectory = "-d " + outputDirString + " ";
        }
        else
        {
            // TODO make the default directory related to the directory of the
            // files
            outputDirectory = "-d " + workspacePath + " ";
        }
        if (outputDirectoryUse.equals(PreferenceConstants.C_OUTPUTDIR_ABSOLUTE))
        {
            runPlDoc(workspacePath,
                     overview,
                     docTitle,
                     outputDirectory,
                     exitOnError,
                     definesFile,
                     styleSheetFile,
                     namesCase,
                     pldocPath,
                     extraParams,
                     files);
        }
        else
        {
            if (outputDirString.length() == 0
                    && !outputDirectoryUse.equals(PreferenceConstants.C_OUTPUTDIR_PROJECT_RELATIVE))
            {
                outputDirString = workspacePath;
            }
            HashMap<String,List<IFile>> gatheredFiles = gatherFiles(files);
            for (String folderName : gatheredFiles.keySet())
			{
                List<IFile> filelist = gatheredFiles.get(folderName);
                IFile file = (IFile) filelist.get(0);
                String outputDir = null;
                if (outputDirectoryUse.equals(PreferenceConstants.C_OUTPUTDIR_FS_RELATIVE))
                {
                    outputDir = outputDirString + file.getParent().getFullPath().toString();
                    outputDirectory = "-d " + outputDir + " ";
                }
                else
                // if
                // (outputDirectoryUse.equals(PreferenceConstants.C_OUTPUTDIR_PROJECT_RELATIVE))
                {
                    outputDir = workspacePath + file.getProject().getFullPath().toString() + "/"
                            + outputDirString + "/"
                            + file.getParent().getProjectRelativePath().toString();
                    outputDirectory = "-d " + outputDir + " ";
                }

                buildOutputDirectory(outputDir);

                IFile[] filesToProcess = (IFile[]) filelist.toArray(new IFile[filelist.size()]);
                runPlDoc(workspacePath,
                         overview,
                         docTitle,
                         outputDirectory,
                         exitOnError,
                         definesFile,
                         styleSheetFile,
                         namesCase,
                         pldocPath,
                         extraParams,
                         filesToProcess);
            }
        }

    }

    private void buildOutputDirectory(String outputDirectory)
    {
        IPath path = new Path(outputDirectory);
        int segCount = path.segmentCount();
        for (int i = segCount - 1; i >= 0; i--)
        {
            IPath toCreate = path.removeLastSegments(i);
            File fileRepresentation = toCreate.toFile();
            if (!fileRepresentation.exists())
            {
                fileRepresentation.mkdir();
            }
        }
    }

    /**
     * This method actually runs the pldoc process once.
     * 
     * @param overview
     * @param docTitle
     * @param outputDirectory
     * @param exitOnError
     * @param definesFile
     * @param styleSheetFile
     * @param namesCase
     * @param pldocPath
     * @param extraParams
     * @param fileNamesBuffer
     */
    private void runPlDoc(String installPath,
                          String overview,
                          String docTitle,
                          String outputDirectory,
                          String exitOnError,
                          String definesFile,
                          String styleSheetFile,
                          String namesCase,
                          String pldocPath,
                          String extraParams,
                          IFile[] files)
    {
        String osName = System.getProperty("os.name");
        String[] cmd = new String[3];

        StringBuffer fileNamesBuffer = new StringBuffer();
        for (int i = 0; i < files.length; i++)
        {
            fileNamesBuffer.append(" ").append(installPath).append("/").append(files[i]
                    .getFullPath());
        }

        if (osName.indexOf("indows") != -1)
        {
            cmd[0] = "cmd.exe";
            cmd[1] = "/C";
            cmd[2] = "call " + pldocPath + "\\pldoc.bat " + overview + docTitle + outputDirectory
                    + exitOnError + definesFile + styleSheetFile + namesCase + extraParams
                    + fileNamesBuffer;
        }
        else
        {
            cmd[0] = "sh";
            cmd[1] = "-c";
            cmd[2] = pldocPath + "/pldoc.sh " + overview + docTitle + outputDirectory + exitOnError
                    + definesFile + styleSheetFile + namesCase + extraParams + fileNamesBuffer;
        }

        PlsqleditorPlugin.log("Executing command: " + cmd[2], null);

        Runtime rt = Runtime.getRuntime();
        try
        {
            Process proc = rt.exec(cmd);
            StreamProcessor errorProcessor = new StreamProcessor(proc.getErrorStream(),
                    "Std  Error");
            StreamProcessor outputProcessor = new StreamProcessor(proc.getInputStream(),
                    "Std Output");

            errorProcessor.start();
            outputProcessor.start();
            int exitVal = proc.waitFor();
            PlsqleditorPlugin.log(("Process exitValue: " + exitVal), null);
        }
        catch (IOException e)
        {
            PlsqleditorPlugin.log("PlDoc execution error", e);
        }
        catch (Exception e)
        {
            PlsqleditorPlugin.log("PlDoc execution error", e);
        }
    }

    /**
     * This method gathers the supplied <code>files</code> into a hashmap of
     * lists of files based on the name of the folder they come from.
     * 
     * @param files The list of files to sort into lists based on location.
     * 
     * @return A map of folderName -=> list of IFiles.
     */
    private HashMap<String,List<IFile>> gatherFiles(IFile[] files)
    {
        HashMap<String,List<IFile>> map = new HashMap<String,List<IFile>>();
        for (int i = 0; i < files.length; i++)
        {
            IFile file = files[i];
            String folderName = file.getParent().toString();
            List<IFile> filesInFolder = map.get(folderName);
            if (filesInFolder == null)
            {
                filesInFolder = new ArrayList<IFile>();
                map.put(folderName, filesInFolder);
            }
            filesInFolder.add(file);
        }
        return map;
    }
}
