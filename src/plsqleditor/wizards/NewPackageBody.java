package plsqleditor.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.stores.PackageStore;

/**
 * This is a new PlSqlEditor Package Body wizard. Its role is to create a new
 * file resource in the provided container. If the container resource (a folder
 * or a project) is selected in the workspace when the wizard is opened, it will
 * accept it as the target container. The wizard creates one file with the
 * extension "pkb".
 */
public class NewPackageBody extends Wizard implements INewWizard
{
	private NewPackageBodyPage page;
	private ISelection selection;

	/**
	 * Constructor for NewPackageBody.
	 */
	public NewPackageBody()
	{
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages()
	{
		page = new NewPackageBodyPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish()
	{
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException
			{
				try
				{
					doFinish(containerName, fileName, monitor);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				}
				finally
				{
					monitor.done();
				}
			}
		};
		try
		{
			getContainer().run(true, false, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(String containerName, String fileName,
			IProgressMonitor monitor) throws CoreException
	{
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try
		{
			InputStream stream = openContentStream(fileName);
			if (file.exists())
			{
				file.setContents(stream, true, true, monitor);
			}
			else
			{
				file.create(stream, true, monitor);
			}
			stream.close();
		}
		catch (IOException e)
		{
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try
				{
					IDE.openEditor(page, file, true);
				}
				catch (PartInitException e)
				{
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will initialise file contents with sample text depending on the type.
	 */
	private InputStream openContentStream(String fileName)
	{
		String contents = null;
		// TODO get the schema and package name
		String schemaPackageDelimiter = PackageStore.getSchemaPackageDelimiter(PlsqleditorPlugin.getDefault().getProject());
		String schemaName = fileName.substring(0,fileName.indexOf(schemaPackageDelimiter));
		String packageName = fileName.substring(fileName.indexOf(schemaPackageDelimiter)+1,
				fileName.lastIndexOf("."));
		if (fileName.endsWith(".pkb"))
		{
			contents = "CREATE OR REPLACE PACKAGE BODY "
					+ schemaName + "." + packageName
					+ " IS\n"
					+ "/*\n"
					+ " * File documentation goes here.  This is required for the outline to work correctly.\n"
					+ " *\n"
					+ " * Below here are the details for the header.  This is verbatim generated into the header file.\n"
					+ " * The verbatim copying starts below the line with the word \"header\" and the word \"details\" next\n"
					+ " * to each other.  You should not put those words together anywhere else in this file documentation\n"
					+ " * section.  This would cause the outline parser (and auto completion parser) to fail.\n"
					+ " * header details\n"
					+ "   gc_package_name CONSTANT VARCHAR2(50) := '"
					+ fileName
					+ "';\n"
					+ " * end header details\n"
					+ " * The line above (with the header's details ending mentioned) indicates the end of\n"
					+ " * the verbatim copying.\n"
					+ " *\n"
					+ " * @authid CURRENT_USER\n"
					+ " *   ^   ^   ^\n"
					+ " * The line above allows the package header generator to correctly generate the\n"
					+ " * command CREATE OR REPLACE PACKAGE "
					+ schemaName + "." + packageName
					+ "\n"
					+ " *         AS\n"
					+ " *         AUTHID CURRENT_USER\n"
					+ " * @schema "
					+ schemaName
					+ "\n"
					+ " */\n"
					+ " \n"
					+ "/**\n"
					+ " * This is a test file.  This part of the documentation is generated as a package header\n"
					+ " * when using pldoc generation.  This is indicated by the use of the headcom tag below.\n"
					+ " *\n"
					+ " * @headcom\n"
					+ " */\n"
					+ " \n"
					+ "   /**\n"
					+ "    * This is a generic pl doc method documentation.  It is auto generated by the typing of\n"
					+ "    * the \"/**\" and pressing enter.\n"
					+ "    * There is also auto completion of tags within this section, auto completion of parameters\n"
					+ "    * when using content assist after the word \"@param\" and standard code auto completion after\n"
					+ "    * \"@refer\", \"@see\" and \"{@link\".\n"
					+ "    * Using html tags is <b>highlighted</b> (as seen by the bold markers).\n"
					+ "    *\n"
					+ "    * @param pin_object_id the object id of this call.\n"
					+ "    * The above param tag can have auto complete (Ctrl-Space) applied to it to generate any of the\n"
					+ "    * parameters in the declared method.\n"
					+ "    *\n"
					+ "    * @param pin_traversal_context_id a dummy number\n"
					+ "    *\n"
					+ "    * @see do_something(NUMBER, VARCHAR2);\n"
					+ "    * The above see tag can have auto complete (Ctrl-Space) applied to it to generate standard\n"
					+ "    * auto complete functionality.\n"
					+ "    * \n"
					+ "    * @pragma RESTRICT_REFERENCES( traverse, WNDS, WNPS, RNDS)\n"
					+ "    *   ^   ^   ^\n"
					+ "    * The line above allows the package header generator to correctly generate the command\n"
					+ "    * PRAGMA RESTRICT_REFERENCES(traverse, WNDS, WNPS, RNDS);\n"
					+ "    * This pragma should ALWAYS be on one line, and should not have a semi colon after it\n"
					+ "    * \n"
					+ "    * @return something useful to someone hopefully\n"
					+ "    */\n"
					+ "FUNCTION traverse(\n"
					+ "    pin_object_id  IN NUMBER,\n"
					+ "    pin_traversalcontext_id IN NUMBER)\n"
					+ "RETURN NUMBER\n"
					+ "AS\n"
					+ "    ln_number NUMBER;\n"
					+ "    ls_val    VARCHAR2(100);\n"
					+ "BEGIN\n"
					+ "    ls_val := gc_package_name;\n"
					+ "    dbms_output.put_line(pin_object_id);\n"
					+ "    -- if you hover over the ^^^ pin_object_id above, it will produce the documentation from the\n"
					+ "    -- @param pldoc tag in the pl doc section of this method.\n"
					+ "    ln_number :=         do_something(pin_obj => ln_number, pin_extra => 'extra');\n"
					+ "    -- if you hover over the ^^^ do_something above, it will produce the documentation from the\n"
					+ "    -- pl doc for the do_something below.  This documentation will also work for documentation\n"
					+ "    -- from other packages.\n"
					+ "    RETURN ln_number;\n"
					+ "END traverse;\n"
					+ "\n"
					+ "/**\n"
					+ " * This method will be private because of the use of the \"@private\" tag,\n"
					+ " * so it will not appear in the header file, but it will have pl doc\n"
					+ " * documentation generated.\n"
					+ " *\n"
					+ " * TODO <-- this will appear in the task list as a medium priority task\n"
					+ " *\n"
					+ " * FIXME <-- this will appear in the task list as a high priority task\n"
					+ " *\n"
					+ " * @param pin_obj The id of the object to operate on.\n"
					+ " * @param pin_extra\n"
					+ " * @return NUMBER\n"
					+ " *\n"
					+ " * @private\n"
					+ " */\n"
					+ "FUNCTION do_something(pin_obj IN NUMBER,\n"
					+ "                      pin_extra IN VARCHAR2)\n"
					+ "                       RETURN NUMBER IS\n"
					+ "BEGIN\n"
					+ "	RETURN 0;\n"
					+ "END do_something;\n"
					+ "/*\n"
					+ " * This method will not be in the header file, and will not have pl documentation generated\n"
					+ " * because is has only one star after the opening slash.\n"
					+ " * @return VARCHAR2 (useless, because this won't be pl doc'ed.\n"
					+ " */\n" + "FUNCTION get_name RETURN VARCHAR2 IS\n"
					+ "BEGIN\n" + "    RETURN gc_class_name;\n"
					+ "END get_name;\n" + "END " + packageName + ";\n"
					+ "/\n" + "SHOW ERRORS PACKAGE BODY " + packageName + ";\n";
		}
		else if (fileName.endsWith(".pkg"))
		{
			contents = "-- don't comment above this line.. (using /**/)\n"
					+ "-- this problem may be resolved in future releases\n"
					+ "-- this is a demo file of what a pkg file must look like to work properly\n"
					+ "-- any issues that you have with the format of this file should be raised\n"
					+ "-- as bug requests :-)\n" + "CREATE OR REPLACE PACKAGE "
					+ schemaName + "." + packageName
					+ " IS\n"
					+ "/*\n"
					+ " * This comment here is VITAL - without this the parse will not work properly. \n"
					+ " */\n"
					+ "/**\n"
					+ " * @param pis_thing\n"
					+ " */\n"
					+ "    FUNCTION do_stuff (\n"
					+ "      pis_thing IN varchar2)\n"
					+ "      RETURN VARCHAR2;\n"
					+ "      \n"
					+ "END "
					+ packageName
					+ ";\n"
					+ "/\n"
					+ "\n"
					+ "-- no comments here either\n"
					+ "-- put them after the create or replace line\n"
					+ "CREATE OR REPLACE PACKAGE BODY "
					+ schemaName + "." + packageName
					+ " IS\n"
					+ "/*\n"
					+ " * \n"
					+ " */\n"
					+ " \n"
					+ "/**\n"
					+ " * @param pis_thing\n"
					+ " */\n"
					+ "   FUNCTION do_stuff (\n"
					+ "      pis_thing IN varchar2)\n"
					+ "      RETURN VARCHAR2\n"
					+ "   IS\n"
					+ "      ls_proc_name  varchar2(100) := '"
					+ packageName
					+ ".' || 'do_stuff';\n"
					+ "   BEGIN\n"
					+ "       return null;\n"
					+ "   END do_stuff;\n"
					+ "   \n"
					+ "END " + packageName + ";\n" + "/ \n\n";
		}
		else if (fileName.endsWith(".pkh"))
		{
			contents = "CREATE OR REPLACE PACKAGE " + schemaName + "." + packageName + "\n AS\n"
					+ "/*\n" + " */\n" + "/**\n" + " * @param pis_thing\n"
					+ " */\n" + "    FUNCTION do_stuff (\n"
					+ "      pis_thing IN varchar2)\n"
					+ "      RETURN VARCHAR2;\n" + "      \n" + "END "
					+ packageName + ";\n" + "/ \n" + "SHOW ERRORS PACKAGE "
					+ packageName + ";\n";

		}
		else if (fileName.endsWith(".sql"))
		{
			contents = "-- This is the initial file contents for *.sql file to be edited \n"
					+ "-- in the plsql editor";
		}
		else
		{
			contents = "-- new file of undetermined type";
		}
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException
	{
		IStatus status = new Status(IStatus.ERROR, "test", IStatus.OK, message,
				null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.selection = selection;
	}
}
