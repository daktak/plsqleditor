package plsqleditor.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;

/**
 * Responsible for the basic command lines used to invoke the Perl interpreter.
 * 
 * @author luelljoc
 * @author jploski
 */
public class PlsqlExecutableUtilities
{
	private static final Pattern CYGWIN_PATH_TRANSLATION = Pattern
			.compile("^([a-z]):(.*)$");

	private PlsqlExecutableUtilities()
	{
	}

	/**
	 * @return a list of Strings representing the command line used to invoke
	 *         the sqlplus interpreter, according to PLSQL editor's global
	 *         preferences; if PLSQL is set up properly, the returned list
	 *         should at the very least contain a path to the interpreter's
	 *         executable
	 */
	public static List<String> getPlsqlCommandLine()
	{
		String sqlplusExe = PlsqleditorPlugin.getDefault().getPlsqlExecutable();

		try
		{
			IStringVariableManager varMgr = VariablesPlugin.getDefault()
					.getStringVariableManager();

			sqlplusExe = varMgr.performStringSubstitution(sqlplusExe);
		}
		catch (CoreException e)
		{
			PlsqleditorPlugin
					.getDefault()
					.getLog()
					.log(
							new Status(
									Status.ERROR,
									PlsqleditorPlugin.getPluginId(),
									IStatus.OK,
									"Variable substitution failed for Perl executable. "
											+ "The literal value \""
											+ sqlplusExe
											+ "\" will be used. "
											+ "Check sqlplus executable in PLSQL Preferences.",
									e));
		}

		return new ArrayList<String>(CommandLineTokenizer.tokenize(sqlplusExe));
	}

	/**
	 * @return path to the sqlplus interpreter's executable, according to EPIC's
	 *         global preferences, or null if no path has been configured yet
	 */
	public static String getPlsqlInterpreterPath()
	{
		List<String> commandLine = getPlsqlCommandLine();
		if (commandLine.isEmpty())
		{
			return null;
		}
		return commandLine.get(0).replace('\\', '/');
	}

	private static boolean isCygwinInterpreter()
	{
		String type = PlsqleditorPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_SQLPLUS_INTERPRETER_TYPE);

		return type
				.equals(PreferenceConstants.P_SQLPLUS_INTERPRETER_TYPE_CYGWIN);
	}

	/**
	 * @param absolute
	 *            path to some directory, as returned by File.getAbsolutePath
	 * @return the same path normalized to / as separators and translated for
	 *         Cygwin, if necessary
	 */
	public static String resolveIncPath(String path)
	{
		path = path.replace('\\', '/');
		if (isCygwinInterpreter())
		{
			path = translatePathForCygwin(path);
		}
		return path;
	}

	// package-scope visibility to enable testing
	static String translatePathForCygwin(String path)
	{
		path = path.replace('\\', '/');
		path = path.toLowerCase();

		Matcher m = CYGWIN_PATH_TRANSLATION.matcher(path);

		if (m.matches())
		{
			StringBuffer buf = new StringBuffer();
			buf.append(CygwinMount.drivePrefix());
			buf.append(m.group(1));
			buf.append(m.group(2));
			return buf.toString();
		}
		else return path;
	}
}
