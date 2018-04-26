package plsqleditor.core.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;

/**
 * Responsible for execution of external, non-interactive sqlplus processes
 * which expect input in form of command-line parameters and stdin and provide
 * output through stdout and/or stderr.
 * 
 */
public class PlsqlExecutor
{
	private boolean disposed;
	private final ProcessExecutor executor;

	/**
	 * Creates a new PlsqlExecutor.
	 */
	public PlsqlExecutor()
	{
		this(null, false);
	}

	/**
	 * This constructor helps with testing, other clients should not use it.
	 */
	public PlsqlExecutor(ProcessExecutor executor)
	{
		this.executor = executor;
	}

	/**
	 * @param charsetName
	 *            The name of a supported {@link java.nio.charset.Charset
	 *            </code>charset<code>}
	 * @param ignoreBrokenPipe
	 *            see {@link ProcessExecutor#ignoreBrokenPipe}
	 */
	public PlsqlExecutor(String charsetName, boolean ignoreBrokenPipe)
	{
		executor = new ProcessExecutor(charsetName);
		if (ignoreBrokenPipe)
		{
			executor.ignoreBrokenPipe();
		}
	}

	/**
	 * Releases resources held by this PlsqlExecutor. This PlsqlExecutor must no
	 * longer be used after dispose.
	 */
	public void dispose()
	{
		disposed = true;
		executor.dispose();
	}

	/**
	 * Executes the Plsql interpreter in the given working directory with the
	 * given command-line parameters and input. The execution is
	 * project-neutral, controlled only by global preferences.
	 * 
	 * @param workingDir
	 *            working directory for the interpreter
	 * @param args
	 *            command-line arguments (Strings)
	 * @param input
	 *            input passed to the interpreter via stdin
	 */
	public ProcessOutput execute(File workingDir, List<String> args,
			String input) throws CoreException
	{
		if (disposed)
			throw new IllegalStateException("PlsqlExecutor disposed");

		List<String> commandLine = PlsqlExecutableUtilities
				.getPlsqlCommandLine();
		if (args != null)
		{
			commandLine.addAll(args);
		}

		try
		{
			return executor.execute(commandLine, input, workingDir);
		}
		catch (InterruptedException e)
		{
			throwCoreException(e);
			return null;
		}
		catch (IOException e)
		{
			throwCoreException(e, commandLine);
			return null;
		}
	}

	/**
	 * Executes a sqlplus script contained in the given resource. This resource
	 * is assumed to be contained in a sqlplus project. Project settings control
	 * the execution.
	 * 
	 * @param resource
	 *            script resource
	 * @param args
	 *            additional command-line arguments for the sqlplus interpreter,
	 *            or null if none
	 * @param sourceCode
	 *            source code of the script
	 */
	public ProcessOutput execute(IResource resource, List<String> args,
			String sourceCode) throws CoreException
	{
		if (disposed)
		{
			throw new IllegalStateException("PlsqlExecutor disposed");
		}
		if (sourceCode.length() < 1)
		{
			return new ProcessOutput("", "");
		}

		List<String> commandLine = PlsqlExecutableUtilities
				.getPlsqlCommandLine();
		if (args != null)
		{
			commandLine.addAll(args);
		}

		try
		{
			return executor.execute(commandLine, sourceCode,
					getPlsqlWorkingDir(resource));
		}
		catch (InterruptedException e)
		{
			throwCoreException(e);
			return null;
		}
		catch (IOException e)
		{
			throwCoreException(e, commandLine);
			return null;
		}
	}

	/**
	 * Executes a sqlplus script within the context of the given text editor.
	 * This method is a shorthand for {@link #execute(IResource, List, String)}.
	 * 
	 * @param editor
	 *            parent folder of the edited file is used as working directory
	 *            for the sqlplus interpreter
	 * @param args
	 *            additional command-line arguments for the sqlplus interpreter,
	 *            or null if none
	 * @param sourceCode
	 *            source code of the script
	 */
	public ProcessOutput execute(ITextEditor editor, List<String> args,
			String sourceCode) throws CoreException
	{
		return execute(((IFileEditorInput) editor.getEditorInput()).getFile(),
				args, sourceCode);
	}

	protected File getPlsqlWorkingDir(IPath resourceLocation)
	{
		return new File(resourceLocation.makeAbsolute().removeLastSegments(1)
				.toString());
	}

	protected File getPlsqlWorkingDir(IResource resource)
	{
		return getPlsqlWorkingDir(resource.getLocation());
	}

	private void throwCoreException(InterruptedException e)
			throws CoreException
	{
		// InterruptedExceptions during execute happen when the operation
		// is aborted (and therefore fails). They should not occur during
		// normal operation, but do not necessarily indicate misconfigurations
		// or bugs.

		Status status = new Status(Status.WARNING, PlsqleditorPlugin
				.getPluginId(), IStatus.OK,
				"Execution of a sqlplus process was aborted.", e);
		throw new CoreException(status);
	}

	private void throwCoreException(IOException e, List<String> commandLine)
			throws CoreException
	{
		// An IOException during execute means that the sqlplus process could
		// either not start (most likely misconfiguration) or aborted in
		// an unexpected manner (which is beyond our control). We report
		// this as a severe error.

		Status status = new Status(Status.ERROR, PlsqleditorPlugin
				.getPluginId(), IStatus.OK, "Failed to execute command line: "
				+ getCommandLineString(commandLine), e);
		throw new CoreException(status);
	}

	private String getCommandLineString(List<String> commandLine)
	{
		StringBuffer buf = new StringBuffer();

		for (String str : commandLine)
		{
			if (buf.length() > 0)
			{
				buf.append(' ');
			}
			str = '"' + str.replaceAll("\"", "\\\"") + '"';
			buf.append(str);
		}
		return buf.toString();
	}
}