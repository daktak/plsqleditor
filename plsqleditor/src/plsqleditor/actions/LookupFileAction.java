package plsqleditor.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.parsers.Segment;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the
 * workbench and shown in the UI. When the user tries to use the action, this delegate will be
 * created and execution will be delegated to it.
 * 
 * TODO This class may do a getSegments too often (every time a lookup is performed)
 * This could be more efficient.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class LookupFileAction implements IWorkbenchWindowActionDelegate
{
    /**
     * This class represents the type of open we will perform on an editor.
     * It will either be a schema open (which does nothing), a package open, which
     * causes the package to open and no navigation to take place, or a Method open
     * which causes the package to open and the method to be navigated to.  
     * 
     * Created on 7/03/2005 
     */
    private enum OpenLocationType { Schema, Package, Method }
    
    private IWorkbenchWindow myWindow;

    /**
     * The constructor.
     */
    public LookupFileAction()
    {
        //
        //setActionDefinitionId("plsql.file.lookup"); 
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action
     * sitting in the workbench UI.
     */
    public void run(IAction action)
    {
        IEditorPart part = myWindow.getActivePage().getActiveEditor();
        ISelection selection = part.getEditorSite().getSelectionProvider().getSelection();
        
        int documentOffset = 0;
        if (selection instanceof TextSelection)
        {
            TextSelection textSelection = (TextSelection) selection;
            documentOffset = textSelection.getOffset();
        }

        try
        {
            if (part instanceof PlSqlEditor)
            {
                PlSqlEditor editor = (PlSqlEditor) part;
                FileEditorInput input = (FileEditorInput) part.getEditorInput();
                IDocument doc = editor.getDocumentProvider().getDocument(input);

                int line = doc.getLineOfOffset(documentOffset);
                int start = doc.getLineOffset(line);
                
                String lineOfText = doc.get(start, doc.getLineLength(line));
                int lotStart = documentOffset - start;
                int lotLength = lineOfText.length();
                int lotEnd;
                for (lotEnd = lotStart; lotEnd < lotLength; lotEnd++)
                {
                    char next = lineOfText.charAt(lotEnd);
                    if (!Character.isJavaIdentifierPart(next) &&
                            '.' != next)
                    {
                        break;
                    }
                }
                lineOfText = lineOfText.substring(0, lotEnd);

                int lastSpace = lineOfText.lastIndexOf(' ');
                String identifier = lineOfText.substring(lastSpace + 1);
                int documentOffsetIndex = lotStart - (lastSpace + 2);

                PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();

                // check qualifiers
                if (identifier.contains("."))
                {
                    int lastDotIndex = identifier.lastIndexOf(".");
                    String prior = identifier.substring(0, lastDotIndex).toLowerCase();
                    identifier = identifier.substring(lastDotIndex + 1);
                    if (prior.contains("."))
                    {
                        // identifier is text after first dot
                        int index = prior.lastIndexOf(".");
                        String schema = prior.substring(0, index);
                        String packageName = prior.substring(index + 1);
                        openEditor(part, identifier, documentOffsetIndex < index ? OpenLocationType.Schema : (documentOffsetIndex < lastDotIndex ? OpenLocationType.Package : OpenLocationType.Method), schema, packageName);
                    }
                    else
                    {
                        // only one dot, could be schema or package name
                        // currText is text after the dot
                        String schema = plugin.getCurrentSchema();
                        String packageName = prior;
                        openEditor(part, identifier, documentOffsetIndex < lastDotIndex ? OpenLocationType.Package : OpenLocationType.Method, schema, packageName);
                    }
                }
                else
                {
                    List<Segment> segments = plugin.getSegments(input.getFile(), input.getName(), doc);
                    navigateToSegment(part, identifier, segments);
                }
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method navigates to the segment that has the same name as the identifier.
     * It does not currently differentiate between methods with the same name but different
     * signature.
     * 
     * @param part The editor that contains the segment to navigate to.
     * 
     * @param identifier The name of the segment (function/procedure/field).
     * 
     * @param segments The list of segments with locations.
     */
    private void navigateToSegment(IEditorPart part, String identifier, List<Segment> segments)
    {
        for (Segment s : segments)
        {
            if (s.getName().equals(identifier))
            {
                // TODO differentiate between methods of the same name
                Position p = s.getPosition();
                part.getSite().getSelectionProvider().setSelection(new TextSelection(p.getOffset(), p.getLength()));
            }
        }
    }

    /**
     * This method opens an editor and moves the selection to the segment represented by
     * the <code>identifier</code>.
     * 
     * @param part The editor from which the <code>identifier</code> was originally located.
     * @param identifier
     * @param openLocType The type of location we expect to open.
     * @param schema
     * @param packageName
     */
    private void openEditor(IEditorPart part, String identifier, OpenLocationType openLocType, String schema, String packageName)
    {
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        IFile file = plugin.getFile(schema, packageName);
        FileEditorInput input = new FileEditorInput(file);
        try
        {
            if (openLocType == OpenLocationType.Schema) // documentOffset on the schema
            {
                // do nothing
            }
            else
            {
                IEditorPart epart = part.getSite().getPage().openEditor(input,
                                                    "plsqleditor.editors.PlSqlEditor");
                if (openLocType == OpenLocationType.Package) // documentOffset on the package
                {
                    // do nothing
                }
                else // openLocType == OpenLocationType.Method
                {
                    List<Segment> segments = plugin.getSegments(file, file.getName(),((PlSqlEditor) epart).getDocumentProvider().getDocument(epart.getEditorInput()));
                    navigateToSegment(epart, identifier, segments);
                }
            }
        }
        catch (PartInitException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Selection in the workbench has been changed. We can change the state of the 'real' action
     * here if we want, but this can only happen after the delegate has been created.
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        //
    }

    /**
     * We can use this method to dispose of any system resources we previously allocated.
     * 
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose()
    {
        //
    }

    /**
     * We will cache myWindow object in order to be able to provide parent shell for the message
     * dialog.
     * 
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window)
    {
        this.myWindow = window;
    }
}