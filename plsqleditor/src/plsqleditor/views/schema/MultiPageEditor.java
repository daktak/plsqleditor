package plsqleditor.views.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.editors.PlSqlContentOutlinePage;
import plsqleditor.editors.PlSqlEditor;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * </ul>
 */
public class MultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener
{

    /** The text editor used in page 0. */
    PlSqlEditor             editor;

    private PlSqlContentOutlinePage myOutlinePage;

    /**
     * Creates a multi-page editor example.
     */
    public MultiPageEditor()
    {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

    }

    public Object getAdapter(Class adapter)
    {
        if (IContentOutlinePage.class.equals(adapter))
        {
            if (myOutlinePage == null)
            {
                // TODO fix
                //myOutlinePage = new PlSqlContentOutlinePage(editor);
            }
            return myOutlinePage;
        }
        return editor.getAdapter(adapter);
    }

    /**
     * Creates page 0 of the multi-page editor, which contains a text editor.
     */
    void createPage0()
    {
        try
        {
            editor = new PlSqlEditor();

            int index = addPage(editor, getEditorInput());
            setPageText(index, editor.getTitle());
            setPartName(editor.getTitle());
        }
        catch (PartInitException e)
        {
            ErrorDialog.openError(getSite().getShell(),
                                  "Error creating nested text editor",
                                  null,
                                  e.getStatus());
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which allows you to change the
     * font used in page 2.
     */
    void createPage1()
    {

        Composite composite = new Composite(getContainer(), SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        layout.numColumns = 2;

        Button exeButton = new Button(composite, SWT.NONE);
        GridData gd = new GridData(GridData.BEGINNING);
        gd.horizontalSpan = 2;
        exeButton.setLayoutData(gd);
        exeButton.setText("Execute sql script");

        exeButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                // TODO fix
                //executeSqlScript(null);
            }
        });

        int index = addPage(composite);
        setPageText(index, "Actions");
    }

    /**
     * Creates the pages of the multi-page editor.
     */
    protected void createPages()
    {
        createPage0();
        createPage1();
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors.
     * Subclasses may extend.
     */
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    /**
     * Saves the multi-page editor's document.
     */
    public void doSave(IProgressMonitor monitor)
    {
        getEditor(0).doSave(monitor);
        myOutlinePage.update();
    }

    /**
     * Saves the multi-page editor's document as another file. Also updates the
     * text for page 0's tab, and updates this multi-page editor's input to
     * correspond to the nested editor's.
     */
    public void doSaveAs()
    {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
        myOutlinePage.update();
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker)
    {
        setActivePage(0);
        // editor.gotoMarker(marker);
        IDE.gotoMarker(getEditor(0), marker);
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method
     * checks that the input is an instance of <code>IFileEditorInput</code>.
     */
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
    {
        if (!(editorInput instanceof IFileEditorInput)) throw new PartInitException(
                "Invalid Input: Must be IFileEditorInput");
        super.init(site, editorInput);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    protected void pageChange(int newPageIndex)
    {
        super.pageChange(newPageIndex);
        if (newPageIndex == 0)
        {
            //
        }
    }

    /**
     * Closes all project files on project close.
     */
    public void resourceChanged(final IResourceChangeEvent event)
    {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
        {
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (int i = 0; i < pages.length; i++)
                    {
                        if (((FileEditorInput) editor.getEditorInput()).getFile().getProject()
                                .equals(event.getResource()))
                        {
                            IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    void executeSqlScript(IProject project)
    {
        try
        {
            Connection conn = DbUtility.getDbaConnectionPool(project).getConnection();
            // execute package
            String bufPackage;

            IDocumentProvider docProvider = editor.getDocumentProvider();
            IDocument doc = docProvider.getDocument(editor.getEditorInput());

            bufPackage = doc.get();
            String packageName = "NONAME";
            String packageType = "PACKAGE";

            int endMarker = 0;
            if (bufPackage.indexOf("IS") > 0)
            {
                endMarker = bufPackage.indexOf("IS");
            }
            else
            {
                if (bufPackage.indexOf("is") > 0)
                {
                    endMarker = bufPackage.indexOf("is");
                }
            }
            if (endMarker > 0)
            {
                if (bufPackage.indexOf("PACKAGE BODY") > 0)
                {
                    packageName = bufPackage.substring(bufPackage.indexOf("PACKAGE BODY") + 12,
                                                       bufPackage.indexOf("IS") - 1).trim();
                    packageType = "PACKAGE BODY";
                }
                else
                {
                    if (bufPackage.indexOf("PACKAGE") > 0)
                    {
                        packageName = bufPackage.substring(bufPackage.indexOf("PACKAGE") + 7,
                                                           bufPackage.indexOf("IS") - 1).trim();
                        packageType = "PACKAGE";
                    }
                }
            }
            PlsqleditorPlugin.getDefault().log(">" + packageName + "<", null);
            PreparedStatement pstmt = conn.prepareStatement(bufPackage);
            pstmt.execute();
            pstmt.close();

            // verify for compilation errors
            Statement stmt = conn.createStatement();

            ResultSet rset = stmt
                    .executeQuery("select STATUS from all_objects where object_name = '"
                            + packageName + "'");
            String isValid;
            isValid = "INVALID";
            while (rset.next())
            {
                isValid = rset.getString("STATUS");
            }
            rset.close();
            // First clean up all existing markers on resource
            clearErrorMarkers(extractResource(editor));
            // For each plsql error we set a marker on the resource
            if (isValid.equals("INVALID"))
            {
                rset = stmt.executeQuery("select * from user_errors where name = '" + packageName
                        + "' and TYPE='" + packageType + "'");
                while (rset.next())
                {
                    setErrorMarker(extractResource(editor), rset.getInt("LINE"), rset
                            .getString("TEXT"));
                }
                rset.close();
            }

            stmt.close();
        }
        catch (SQLException e)
        {
            // check if we have SQL92 token error and set error marker on line
            // containing illegal token
            if (e.toString().endsWith(": font"))
            {
                String strText;
                String strMarker;

                strText = e.toString().substring(e.toString().indexOf(':', 30) + 2,
                                                 e.toString().lastIndexOf(':'));
                PlsqleditorPlugin.getDefault().log("Other error: " + strText, null);
                IDocumentProvider dp = editor.getDocumentProvider();
                IDocument doc = dp.getDocument(editor.getEditorInput());
                // First clean up all existing markers on resource
                clearErrorMarkers(extractResource(editor));
                try
                {
                    setErrorMarker(extractResource(editor), doc.computeNumberOfLines(doc.get()
                            .substring(0, Integer.parseInt(strText))), e.toString());
                }
                catch (Exception ex)
                {
                    PlsqleditorPlugin.getDefault().log("Other error: " + ex.toString(), ex);
                }

                try
                {
                    strMarker = "<problem>";
                    doc.replace(Integer.parseInt(strText), 1, strMarker);
                }
                catch (BadLocationException ex)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (Exception ex)
                {
                    PlsqleditorPlugin.getDefault().log("Other error: " + ex.toString(), ex);
                }
            }
            else
            {
                PlsqleditorPlugin.getDefault().log("Error: >" + e.toString() + "<", e);
            }
        }
        catch (Exception e)
        {
            PlsqleditorPlugin.getDefault().log("Other error: " + e.toString(), e);
        }

    }

    void setErrorMarker(IResource res, int line, String msg)
    {
        try
        {
            Map attributes = new HashMap();
            attributes.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
            attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH));
            MarkerUtilities.setLineNumber(attributes, line);
            MarkerUtilities.setMessage(attributes, msg);
            MarkerUtilities.createMarker(res, attributes, IMarker.PROBLEM);

        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }

    void clearErrorMarkers(IResource resource)
    {
        try
        {
            resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }

    IResource extractResource(IEditorPart editor)
    {
        IEditorInput input = editor.getEditorInput();
        if (!(input instanceof IFileEditorInput)) return null;
        return ((IFileEditorInput) input).getFile();
    }
}
