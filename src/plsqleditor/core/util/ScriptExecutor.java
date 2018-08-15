package plsqleditor.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.TextUtilities;

import plsqleditor.PlsqleditorPlugin;


/**
 * Base class for classes representing executable "helper" plsql scripts and their
 * mandatory command line parameters. A subclass instance can be used to execute its script on
 * various inputs, and possibly with additional command line parameters.
 */
public abstract class ScriptExecutor
{
    //~ Instance fields

    private ILog log;

    //~ Constructors

    protected ScriptExecutor(ILog log)
    {
        this.log = log;
    }

    //~ Methods

    /**
     * Runs the script with no stdin and the given additional command line parameters.
     *
     * @return execution results
     */
    public final ProcessOutput run(List args) throws CoreException
    {
        return run("", args);
    }

    /**
     * Runs the script with the given stdin and no additional command line parameters.
     *
     * @param text text passed to script over stdin
     *
     * @return execution results
     */
    public final ProcessOutput run(String text) throws CoreException
    {
        return run(text, null);
    }

    /**
     * Runs the script with the given stdin and additional command line parameters.
     *
     * @param text text passed to script over stdin
     * @param additionalArgs additional command line arguments passed to the script during execution
     *
     * @return execution results
     */
    public final ProcessOutput run(String text, List additionalArgs) throws CoreException
    {
        File workingDir = getWorkingDir();

        PlsqlExecutor executor = new PlsqlExecutor(
            getCharsetName(), ignoresBrokenPipe());
        try
        {
            List<String> cmdArgs = new ArrayList<String>(1);
            cmdArgs.add(getExecutable());
            cmdArgs.addAll(getCommandLineOpts(additionalArgs));

            ProcessOutput output = executor.execute(workingDir, cmdArgs, text);

            /*
             * there are times that stderr and stdout are both set, even though an error has not
             * occurred (Devel::Refactor processes exhibit this behaviour).
             *
             * given that, logging the stderr as a warning will help the end user to figure out what
             * is going on (and assist in logging bug reports, etc)
             */
            if ((output.stderr != null) && ! output.stderr.equals(""))
            {
                log.log(StatusFactory.createWarning(PlsqleditorPlugin.getPluginId(),
                        "Plsql Process stderr: " + output.stderr, null));
            }

            return output;
        }
        finally
        {
            executor.dispose();
        }
    }
    
    /**
     * @return name of a supported
     *         {@link java.nio.charset.Charset </code>charset<code>}
     *         which should be used to encode/decode communication with
     *         the Perl interpreter, or null to use platform default
     */
    protected String getCharsetName()
    {
        return null;
    }

    /**
     * @return the name of the script that will be executed
     */
    protected abstract String getExecutable();

    /**
     * @return the directory (relative to the plugin root) containing the script to be executed
     */
    protected abstract String getScriptDir();

    /**
     * Constructs the list of command line options passed to the script at runtime.
     *
     * <p>Default implementation returns the <code>additionalOptions</code> if specified, otherwise
     * an empty list is returned. Sub-classes are free to override to append (or prepend) other
     * script-specific options not provided by the caller.</p>
     *
     * @param additionalOptions additional cmd line arguments
     *
     * @return complete list of command line arguments
     */
    protected List<String> getCommandLineOpts(List additionalOptions)
    {
        return (additionalOptions != null) ? additionalOptions : Collections.emptyList();
    }

    /**
     * @return the system's line seperator, falling back to "\n" if it can not be determined
     */
    protected String getSystemLineSeparator()
    {
        String separator = System.getProperty("line.separator");

        // in the off chance this could ever happen...
        if (separator == null)
        {
            separator = "\n";
        }

        return separator;
    }

    /**
     * @return the line separator used in the passed text string
     */
    protected String getLineSeparator(String text)
    {
        return TextUtilities.determineLineDelimiter(text, getSystemLineSeparator());
    }

    protected String getPluginId()
    {
        return PlsqleditorPlugin.getPluginId();
    }

    /**
     * @return true if broken pipe exceptions from the executed script should be ignored, false
     *         otherwise (default)
     *
     * @see plsqleditor.core.util.ProcessExecutor#ignoreBrokenPipe
     */
    protected boolean ignoresBrokenPipe()
    {
        return false;
    }

    protected void log(IStatus status)
    {
        log.log(status);
    }

    protected int parseInt(String s)
    {
        try
        {
            return Integer.valueOf(s).intValue();
        }
        catch (NumberFormatException e)
        {
        	// stacktrace will provide exact error location
            log.log(StatusFactory.createError(getPluginId(), e.getMessage(), e));
            // results in "Unknown" being displayed in the Problems view
            return -1;
        }
    }

    private File extractScripts() throws CoreException
    {
        return ResourceUtilities.extractResources(PlsqleditorPlugin.getDefault(), "plsqlutils/");
    }

    private File getWorkingDir() throws CoreException
    {
        try
        {
            File scriptsLocation = extractScripts();

            if (scriptsLocation == null)
            {
                URL url =
                    new URL(PlsqleditorPlugin.getDefault().getBundle().getEntry("/"),
                        getScriptDir());
                URL workingURL = FileLocator.toFileURL(url);
                return new File(workingURL.getPath());
            }
            return new File(scriptsLocation.getParentFile(), getScriptDir());
        }
        catch (IOException e)
        {
            log.log(StatusFactory.createError(getPluginId(), e.getMessage(), e));
            return null;
        }
    }
}