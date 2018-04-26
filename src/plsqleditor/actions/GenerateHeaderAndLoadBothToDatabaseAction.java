package plsqleditor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.db.LoadPackageManager;
import plsqleditor.editors.MultiPagePlsqlEditor;
import plsqleditor.editors.PlSqlEditor;

/**
 * This class manages the loading of a whole file to a database.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 *          Created on 15/03/2005
 */
public class GenerateHeaderAndLoadBothToDatabaseAction extends AbstractHandler
{
	protected LoadPackageManager theLoadPackageManager = LoadPackageManager
			.instance();

	public static class GenerateHeaderAndLoadResult
	{
		private boolean myIsHeaderSuccessful = false;
		private boolean myIsBodySuccessful = false;
		private Exception myException;

		protected GenerateHeaderAndLoadResult(boolean header, boolean body, Exception e)
		{
			myIsBodySuccessful = body;
			myIsHeaderSuccessful = header;
			myException = e;
		}

		public boolean isHeaderSuccessful()
		{
			return myIsHeaderSuccessful;
		}

		public boolean isBodySuccessful()
		{
			return myIsBodySuccessful;
		}

		public Exception getException()
		{
			return myException;
		}

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IFile)
		{
			IFile f = (IFile) firstElement;
			GenerateHeaderAndLoadResult result = generateHeaderAndLoadBothToDb(f);
			Exception e = result.getException();
			if (e != null)
			{
				MessageDialog.openError(HandlerUtil.getActiveShell(event),
						"Failed to complete the generate and load task", e
								.toString());
			}
		}
		else
		{
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Information", "Please select a PlSQL Body file");
		}
		return null;
	}

	public static GenerateHeaderAndLoadResult generateHeaderAndLoadBothToDb(IFile f)
	{
		boolean header = false;
		boolean body = false;
		Exception ex = null;
		try
		{
			String headerFilename = GenerateHeaderAction.generateHeader(f);
			IContainer parent = f.getParent();
			// fix for bug 1455136 - header generation fails at folder level
			IFile pkhFile = parent.getFile(new Path(headerFilename));
			header = navigateAndLoadFileToDb(pkhFile);
			body = navigateAndLoadFileToDb(f);
		}
		catch (Exception e)
		{
			ex = e;
		}
		return new GenerateHeaderAndLoadResult(header, body, ex);
	}

	/**
	 * This call indicates whether a given call loaded with or without errors.
	 * 
	 * @param file
	 * @return <code>true</code> when the call actually loaded with no errors,
	 *         <code>false</code> when the call loaded, but got errors in the file.
	 * @throws PartInitException when the call fails to execute at all.
	 */
	private static boolean navigateAndLoadFileToDb(IFile file)
			throws PartInitException
	{
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart iep = IDE.openEditor(page, file);
		IEditorInput input = iep.getEditorInput();

		IEditorPart editorPart = page.findEditor(input);
		ITextEditor editor = null;
		if (editorPart instanceof MultiPagePlsqlEditor)
		{
			MultiPagePlsqlEditor mpe = (MultiPagePlsqlEditor) editorPart;
			editor = mpe.getEditor();
		}
		else if (editorPart instanceof ITextEditor)
		{
			editor = (ITextEditor) editorPart;
		}
		editor.getAction(PlSqlEditor.PLSQLEDITOR_LOADTODATABASE_ID).run();
		boolean successful = false;
		try
		{
			IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, 2);
			successful = markers == null || markers.length == 0;
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		return successful;
	}
}
