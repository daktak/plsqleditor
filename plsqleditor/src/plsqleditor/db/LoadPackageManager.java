package plsqleditor.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility.ConnectionContainer;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.parsers.StringLocationMap;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.process.CannotCompleteException;
import plsqleditor.process.SqlPlusProcessExecutor;
import plsqleditor.stores.PackageStore;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 *          Created on 2/03/2005
 */
public class LoadPackageManager
{
	private SQLErrorDetail[] mySQLErrors = new SQLErrorDetail[0];
	private static LoadPackageManager theInstance;
	private Map<String, ResultSetWrapper> myResultSetWrapperMap;
	private Map<String, ConnectionHolder> myFileToSpecificConnectionMap = new HashMap<String, ConnectionHolder>();

	class ConnectionHolder extends Object
	{
		private ConnectionContainer myConnection;
		ConnectionDetails myDetails;
		private ResultSetWrapper myRSW;
		private IFile myFile;

		ConnectionHolder(ConnectionDetails details, IFile file)
		{
			myFile = file;
			myDetails = details;
		}

		ConnectionContainer getConnection() throws SQLException
		{
			if (myConnection == null)
			{
				myConnection = DbUtility.getFixedConnection(myFile, myDetails);
			}
			return myConnection;
		}

		public ResultSetWrapper getResultSetWrapper()
		{
			return myRSW;
		}

		public void setResultSetWrapper(ResultSetWrapper rsw)
		{
			myRSW = rsw;
		}

		public ConnectionDetails getDetails()
		{
			return myDetails;
		}

		// no need to clear the connection, since
		// this will be handled by something holding the pool that
		// refers to these connections
		//myConnection.closeConnection();
	}

	/**
	 * This constructor creates the LoadPackageManager, instantiating the map of
	 * resultset wrappers.
	 */
	private LoadPackageManager()
	{
		myResultSetWrapperMap = new HashMap<String,ResultSetWrapper>();
	}

	public synchronized static LoadPackageManager instance()
	{
		if (theInstance == null)
		{
			theInstance = new LoadPackageManager();
		}
		return theInstance;
	}

	/**
	 * This method executes a piece of code (primarily a package body or header,
	 * or similar piece of ddl (rather than dml) into the database. This may be
	 * executed using jdbc or sqlplus depending on configuration set up in the
	 * {@link plsqleditor.preferences.DbSetupPreferencePage}.
	 * 
	 * @param schema
	 *            The schema against which to execute the code.
	 * 
	 * @param packageName
	 *            The name of the package being executed.
	 * 
	 * @param toLoad
	 *            The code describing the package to be executed.
	 * 
	 * @param type
	 *            The type of the code (pkb,pkh,sql etc - see
	 *            {@link PackageType}).
	 * 
	 * @return The errors from either the execution of the code, the failure to
	 *         retrieve the connection, or the resulting compile errors from the
	 *         loaded code.
	 */
	public synchronized SQLErrorDetail[] execute(IFile file, IProject project,
			String schema, String packageName, String toLoad, PackageType type)
	{
		SQLErrorDetail[] details = null;
		PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
		IPreferenceStore prefs = plugin.getPreferenceStore();
		boolean useLocalClient = prefs
				.getBoolean(PreferenceConstants.P_USE_LOCAL_CLIENT);

		String executable = prefs
				.getString(PreferenceConstants.P_SQLPLUS_EXECUTABLE);

		if (useLocalClient && (executable != null && executable.length() > 0))
		{
			String passwd = DbUtility.getPasswordForSchema(schema);
			String sid = DbUtility.getSid(project);

			SqlPlusProcessExecutor sqlplus = new SqlPlusProcessExecutor(
					executable, schema, passwd, sid);
			try
			{
				sqlplus.execute(toLoad);
				// TODO need to check if there is no connection configured
				details = getErrorDetails(project, schema, packageName, type);
			}
			catch (CannotCompleteException e)
			{
				final String msg = "Failed to execute sqlplus for package "
						+ packageName + ": " + e;
				details = new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
			}
		}
		else
		{
			if (type == PackageType.Package_Header_And_Body)
			{
				// split the file into two
				IDocument document = PackageStore.getDoc(file);
				List<Segment> segments = plugin.getSegments(file, document, true);

				// getSegments(schema,packageName);
				SortedSet<Segment> sortedSegments = new TreeSet<Segment>();
				sortedSegments.addAll(segments);

				for (Iterator<Segment> it = sortedSegments.iterator(); it.hasNext();)
				{
					Segment segment = (Segment) it.next();
					int lastPosition = 0;
					if (segment instanceof PackageSegment)
					{
						if (segment.getType() == SegmentType.Package_Body)
						{
							lastPosition = segment.getPosition().offset;
							int packageBodyStartingLine = 0;
							try
							{
								packageBodyStartingLine = document
										.getLineOfOffset(lastPosition);
							}
							catch (BadLocationException e)
							{
								e.printStackTrace(); // should not happen
							}
							String packageHeader = toLoad.substring(0,
									lastPosition);
							packageHeader = modifyCodeToLoad(packageHeader,
									packageName);
							String packageBody = toLoad.substring(lastPosition);
							packageBody = modifyCodeToLoad(packageBody,
									packageName);
							details = loadSinglePieceOfCode(file, project,
									schema, packageName, packageHeader,
									PackageType.Package);
							SQLErrorDetail[] details2 = loadSinglePieceOfCode(
									file, project, schema, packageName,
									packageBody, PackageType.Package_Body);
							SQLErrorDetail[] newDetails = new SQLErrorDetail[details.length
									+ details2.length];
							System.arraycopy(details, 0, newDetails, 0,
									details.length);
							for (int i = 0, j = details.length; j < newDetails.length; i++, j++)
							{
								SQLErrorDetail oldDetails2 = details2[i];
								newDetails[j] = new SQLErrorDetail(oldDetails2
										.getRow()
										+ packageBodyStartingLine, oldDetails2
										.getColumn(), oldDetails2.getText());
							}
							// System.arraycopy(details2,0,newDetails,details.length,details2.length);
							return newDetails;
						}
					}
				}
				final String msg = "Failed to execute package header and body together for "
						+ packageName;
				return new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
			}
			else
			{
				toLoad = modifyCodeToLoad(toLoad, packageName);
				details = loadSinglePieceOfCode(file, project, schema,
						packageName, toLoad, type);
			}
		}

		return details;
	}

	protected String modifyCodeToLoad(String toLoad, String packageName)
	{
		// FIX for bug 1363370 - jdbc forces code onto one line, commented out
		// next line
		// toLoad = StringLocationMap.replacePlSqlSingleLineComments(toLoad);
		toLoad = StringLocationMap.replaceNewLines(toLoad);
		toLoad = StringLocationMap.escapeSingleCommentedQuotes(toLoad);
		String terminator = "\\W*[Ee][Nn][Dd] +" + packageName + "\\W*;";
		int end = toLoad.length();
		Pattern p = Pattern.compile(terminator);
		Matcher m = p.matcher(toLoad);
		while (m.find())
		{
			end = m.end();
		}
		toLoad = toLoad.substring(0, end);
		return toLoad;
	}

	/**
	 * This method executes a single piece of code in the database. If the file
	 * does not have a specific connection open against it, this function will
	 * then commits the loaded code (via auto commit on close functionality)
	 * before closing the connection, and freeing it back to the connection
	 * pool. This is always executed by jdbc. If the file has a specific
	 * connection against it, that connection is chosen and no commit is
	 * executed.
	 * 
	 * @param file
	 *            The file that is being executed on. This is required in case
	 *            there is a specific connection set against the file.
	 * 
	 * @param schemaName
	 *            The schema against which to retrieve a connection.
	 * 
	 * @param packageName
	 *            The name of the package (piece of code) being executed.
	 * 
	 * @param toLoad
	 *            The string to execute.
	 * 
	 * @param type
	 *            The type of the code being executed (pkb,pkh,sql etc - see
	 *            {@link PackageType}).
	 * 
	 * @return The errors from either the execution of the code, the failure to
	 *         retrieve the connection, or the resulting compile errors from the
	 *         loaded code.
	 */
	private synchronized SQLErrorDetail[] loadSinglePieceOfCode(IFile file,
			IProject project, String schemaName, String packageName,
			String toLoad, PackageType type)
	{
		Connection c = null;
		String fullFilename = file.getFullPath().toString();
		if (myFileToSpecificConnectionMap.containsKey(fullFilename))
		{
			ConnectionHolder ch = myFileToSpecificConnectionMap
					.get(fullFilename);
			try
			{
				c = ch.getConnection().connection;
				// TODO check whether there was an update
				return loadCode(c, packageName, toLoad, type);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				final String msg = "Failed to retrieve connection for file "
						+ fullFilename + " with type " + type + " : "
						+ e.getMessage();
				return new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
			}
		}
		else
		{
			try
			{
				c = DbUtility.getTempConnection(project, schemaName);
				return loadCode(c, packageName, toLoad, type);
			}
			catch (SQLException e)
			{
				final String msg = "Failed to retrieve connection for schema "
						+ schemaName + " package " + packageName + " and type "
						+ type + " : " + e.getMessage();
				return new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
			}
			finally
			{
				if (c != null)
				{
					DbUtility.free(project, schemaName, c);
				}
			}
		}
	}

	/**
	 * This method loads a file into the database, returning any errors it
	 * found.
	 * 
	 * @param c
	 *            The connection to use to load the database.
	 * 
	 * @param packageName
	 *            The name of the package being loaded, for error purposes.
	 * 
	 * @param toLoad
	 *            The text representation of the entire package.
	 * 
	 * @param type
	 *            The type of thing being loaded. (package/package body etc).
	 * 
	 * @return The list of errors from the compile, or null if there were none.
	 */
	private SQLErrorDetail[] loadCode(Connection c, String packageName,
			String toLoad, PackageType type)
	{
		PreparedStatement s = null;
		try
		{
			if (type == PackageType.Sql)
			{
				setErrors(getGeneralErrorDetails(c));
			}
			s = c.prepareStatement(toLoad);
			s.execute();

			String warning = getErrorStatus(c, s, packageName, type);
			if (warning != null)
			{
				return mySQLErrors;
			}
			return null;
		}
		catch (SQLException e)
		{
			DbUtility.printErrors(e);
			DbUtility.close(s);
			final String msg = "Failed to execute" + packageName + " code: "
					+ e.getMessage();
			return new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
		}
	}

	/**
	 * This method loads the supplied piece of code <code>toLoad</code> into the
	 * database using a connection based on the supplied <code>schemaName</code>
	 * unless there is a specific connection set for the given file. In this
	 * case the specific connection is used, and the connection remains open. 
	 * The connection is left unfreed until the caller closes the returned 
	 * result set wrapper.
	 * However, a subsequent call to this method will close any previous result
	 * set for the given <code>schemaName</code> (or file). If there is no
	 * result set resulting from the execution, the connection is freed.
	 * 
	 * @param file
	 *            The file that contains the code to load. If this file has a
	 *            specific connection against it, that connection will be used
	 *            instead of the default schema connection, and it will not be
	 *            autoclosed. Instead, the connection will remain as an open
	 *            session.
	 * @param schemaName
	 *            The name of the schema in which to execute the code
	 *            <code>toLoad</code>.
	 * 
	 * @param toLoad
	 *            The code to execute in the database.
	 * 
	 * @return The results set (wrapped) resulting from the execution of the
	 *         code <code>toLoad</code>. If there was no result set, null is
	 *         returned.
	 * 
	 * @throws SQLException
	 *             when the executed code fails for some reason.
	 */
	public synchronized ResultSetWrapper loadCode(IFile file, IProject project,
			String schemaName, String toLoad) throws SQLException
	{
		String fullFilename = file.getFullPath().toString();
		if (myFileToSpecificConnectionMap.containsKey(fullFilename))
		{
			ConnectionHolder ch = myFileToSpecificConnectionMap
					.get(fullFilename);
			ConnectionContainer cc = ch.getConnection();
			ResultSetWrapper oldRw = ch.getResultSetWrapper();
			if (oldRw != null)
			{
				oldRw.close();
				ch.setResultSetWrapper(null);
			}
			return DbUtility.loadCode(cc, toLoad);
		}

		ResultSetWrapper oldRw = myResultSetWrapperMap.get(schemaName);
		if (oldRw != null)
		{
			oldRw.close();
			myResultSetWrapperMap.remove(schemaName);
		}
		long startTime = System.nanoTime();
		ResultSetWrapper rw = DbUtility.loadCode(project, schemaName, toLoad);

		long stopTime = System.nanoTime();
		long diffTime = elapsed(startTime, stopTime);

		rw.setElapsedTime(diffTime);
		myResultSetWrapperMap.put(schemaName, rw);
		return rw;
	}

	/**
	 * This method returns the number of nanoseconds passed.
	 * 
	 * @param start
	 * @param end
	 * @param freq
	 * @return the time elapsed between the <code>start</code> and
	 *         <code>end</code> over the <code>freq</code>.
	 */
	public static long elapsed(long start, long end)
	{
		return (end - start);
	}

	/**
	 * This method returns the result set wrapper that is currently stored
	 * against the supplied <code>schemaName</code>. This represents the output
	 * of the last piece of code executed in the database against that schema.
	 * This may be null if there have been no pieces of code executed against
	 * this schema, or the last execution had no result.
	 * 
	 * @param schemaName
	 *            The name of the schema whose last result set is sought.
	 * 
	 * @return the result set wrapper that is currently stored against the
	 *         supplied <code>schemaName</code>.
	 */
	public ResultSetWrapper getResultSetWrapper(String schemaName)
	{
		return myResultSetWrapperMap.get(schemaName);
	}

	/**
	 * This method gets the error status of the statement <code>s</code>. If
	 * there have been warnings, the error details will be initialised with the
	 * new errors, and the first warning message will be returned to indicate
	 * something is wrong.
	 * 
	 * @param s
	 *            The statement whose warning status is being checked.
	 * 
	 * @param packageName
	 *            The name of the package/package body that was just created in
	 *            the statement <code>s</code>.
	 * 
	 * @param packageType
	 *            The type of package errors we are searching for on failure.
	 * 
	 * @return The message from the first warning, or null if there were no
	 *         warnings.
	 */
	private String getErrorStatus(Connection c, Statement s,
			String packageName, PackageType packageType)
	{
		try
		{
			SQLWarning warning = s.getWarnings();

			if (warning != null)
			{
				if (packageType == PackageType.Sql)
				{
					// TODO this piece of code is mildly faulty 
					// i need to grab the blocks of code and execute
					// each of them separately. Then i need to add the offset
					// of the previous blocks to each call.
					// the load here doesn't work properly on sql
					SQLErrorDetail[] details = getGeneralErrorDetails(c);
					mergeErrors(details);
				}
				else
				{
					// DbUtility.printWarnings(warning);
					SQLErrorDetail[] details = getErrorDetails(c, packageName,
							packageType);

					setErrors(details);
				}
				return warning.getMessage();
			}
			else
			{
				return null;
			}
		}
		catch (SQLException sqle)
		{
			return "Failed to find warnings because of " + sqle;
		}
	}

	private void mergeErrors(SQLErrorDetail[] details)
	{
		List<SQLErrorDetail> newDetails = new ArrayList<SQLErrorDetail>();
		for (int i = 0; i < details.length; i++)
		{
			boolean add = true;
			for (int j = 0; j < mySQLErrors.length; j++)
			{
				if (details[i].toString().equals(mySQLErrors[j].toString()))
				{
					add = false;
					break;
				}
			}
			if (add)
			{
				newDetails.add(details[i]);
			}
		}
		setErrors(newDetails.toArray(new SQLErrorDetail[newDetails.size()]));
	}

	public SQLErrorDetail[] getErrorDetails(IProject project,
			String schemaName, String packageName, PackageType errorType)
	{
		Connection c = null;
		try
		{
			c = DbUtility.getTempConnection(project, schemaName);
			return getErrorDetails(c, packageName, errorType);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new SQLErrorDetail[] { new SQLErrorDetail(0, 0, e.toString()) };
		}
		finally
		{
			if (c != null)
			{
				DbUtility.free(project, schemaName, c);
			}
		}
	}

	/**
	 * This method gets all the error details currently in the db for the 
	 * schema that backs the supplied connection <code>c</code>.
	 * 
	 * @param c The connection to use to create the error request statement.
	 * 
	 * @return The list of error details concerning the compile problems.
	 */
	private SQLErrorDetail[] getGeneralErrorDetails(Connection c)
	{
		// fix for [ 1539302 ] Load to database report errors from other schema
		String sql = "select line, position, text, name from user_errors"
				+ " order by sequence";

		SQLErrorDetail detail = null;
		SQLErrorDetail[] toReturn = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try
		{
			s = c.prepareStatement(sql);
			rs = s.executeQuery();
			List<SQLErrorDetail> v = new Vector<SQLErrorDetail>();
			while (rs.next())
			{
				detail = new SQLErrorDetail(rs.getInt(1), rs.getInt(2), rs
						.getString(4)
						+ ": " + rs.getString(3));
				v.add(detail);
			}
			toReturn = v.toArray(new SQLErrorDetail[v.size()]);
			s.close();
		}
		catch (SQLException e)
		{
			final String msg = "Failed to retrieve error details: " + e;
			System.out.println(msg);
			toReturn = new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
		}
		finally
		{
			DbUtility.close(s);
			DbUtility.close(rs);
		}
		return toReturn;
	}

	/**
	 * This method gets the error details from the statement that attempted to
	 * create/replace the procedure/function of type <code>procType</code> and
	 * name <code>procName</code>.
	 * 
	 * @param c
	 *            The connection to use to create the error request statement.
	 * 
	 * @param packageName
	 *            The name of the package or package body that was compiled, but
	 *            caused warnings.
	 * 
	 * @param errorType
	 *            the type of the errors to look for.
	 * 
	 * @return The list of error details concerning the compile problems.
	 */
	private SQLErrorDetail[] getErrorDetails(Connection c, String packageName,
			PackageType errorType)
	{
		// fix for [ 1539302 ] Load to database report errors from other schema
		String sql = "select line, position, text from user_errors where upper(name) = upper(?) "
				+ " and type = ? order by sequence";

		SQLErrorDetail detail = null;
		SQLErrorDetail[] toReturn = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try
		{
			s = c.prepareStatement(sql);
			s.setString(1, packageName.toUpperCase());
			s
					.setString(2, errorType.toString().toUpperCase().replace(
							'_', ' '));
			rs = s.executeQuery();
			List<SQLErrorDetail> v = new Vector<SQLErrorDetail>();
			while (rs.next())
			{
				detail = new SQLErrorDetail(rs.getInt(1), rs.getInt(2), rs
						.getString(3));
				v.add(detail);
			}
			toReturn = v.toArray(new SQLErrorDetail[v.size()]);
			s.close();
		}
		catch (SQLException e)
		{
			final String msg = "Failed to retrieve error details: " + e;
			System.out.println(msg);
			toReturn = new SQLErrorDetail[] { new SQLErrorDetail(0, 0, msg) };
		}
		finally
		{
			DbUtility.close(s);
			DbUtility.close(rs);
		}
		return toReturn;
	}

	private void setErrors(SQLErrorDetail[] details)
	{
		mySQLErrors = details;
	}

	public ConnectionDetails getFixedConnectionName(IFile file)
	{
		String fullFileName = file.getFullPath().toString();
		ConnectionHolder ch = myFileToSpecificConnectionMap.get(fullFileName);
		if (ch != null)
		{
			return ch.getDetails();
		}
		return null;
	}
	
	public void removeFixedConnection(IFile file)
	{
		String fullFileName = file.getFullPath().toString();
		myFileToSpecificConnectionMap.remove(fullFileName);
		DbUtility.removeFixedConnection(file);
	}

	public void removeFixedConnection(String fullFilename)
	{
		myFileToSpecificConnectionMap.remove(fullFilename);
		DbUtility.removeFixedConnection(fullFilename);
	}

	public void setFixedConnection(IFile file, ConnectionDetails details)
	{
		String fullFileName = file.getFullPath().toString();
		ConnectionHolder ch = new ConnectionHolder(details, file);
		myFileToSpecificConnectionMap.remove(fullFileName);
		DbUtility.removeFixedConnection(file);
		myFileToSpecificConnectionMap.put(fullFileName, ch);
	}

	public String[] getFilesForConnection(ConnectionDetails cd)
	{
		List<String> files = new ArrayList<String>();

		for (String key : myFileToSpecificConnectionMap.keySet())
		{
			ConnectionDetails cd_i = myFileToSpecificConnectionMap.get(key).myDetails;
			if (cd_i.equals(cd))
			{
				files.add(key);
			}
		}
		if (files.size() > 0)
		{
			return files.toArray(new String[files.size()]);
		}
		return null;
	}

	public Map<String, ConnectionDetails> getAllSpecificConnections()
	{
		Map<String, ConnectionDetails> toReturn = new HashMap<String, ConnectionDetails>();

		for (String fullFilename : myFileToSpecificConnectionMap.keySet())
		{
			toReturn.put(fullFilename, myFileToSpecificConnectionMap
					.get(fullFilename).myDetails);
		}
		return toReturn;
	}
}
