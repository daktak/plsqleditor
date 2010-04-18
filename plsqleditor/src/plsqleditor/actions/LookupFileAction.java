package plsqleditor.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.MultiPagePlsqlEditor;
import plsqleditor.editors.PlSqlCompletionProcessor;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.parsers.Segment;

/**
 * This action executes (for the plsqleditor plugin) the standard "F3" functionality 
 * that is offered by the java editor.
 * 
 * TODO This class may do a getSegments too often (every time a lookup is performed)
 * This could be more efficient.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class LookupFileAction implements IWorkbenchWindowActionDelegate
{
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
        String msg = null;
        if (part == null)
        {
            msg = "The editor part associated with the lookupfile action is null";
            PlsqleditorPlugin.log(msg, new Exception(msg));
            return;
        }
        IEditorSite site = part.getEditorSite();
        if (site == null)
        {
            msg = "The editor site associated with the lookupfile action is null";
            PlsqleditorPlugin.log(msg, new Exception(msg));
            return;
        }
        ISelectionProvider selectionProvider = site.getSelectionProvider();
        if (selectionProvider == null)
        {
            msg = "The selection provider associated with the lookupfile action is null";
            PlsqleditorPlugin.log(msg, new Exception(msg));
            return;
        }
        ISelection selection = selectionProvider.getSelection();
        
        int documentOffset = 0;
        if (selection instanceof TextSelection)
        {
            TextSelection textSelection = (TextSelection) selection;
            documentOffset = textSelection.getOffset();
        }

        try
        {
            if (part instanceof MultiPagePlsqlEditor)
            {
                MultiPagePlsqlEditor mpe = (MultiPagePlsqlEditor) part;
                PlSqlEditor editor = mpe.getEditor();
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

                int lastNonUsableCharacter = -1;
                for (int i = 0; i < PlSqlCompletionProcessor.autoCompleteDelimiters.length; i++)
                {
                    char c = PlSqlCompletionProcessor.autoCompleteDelimiters[i];
                    lastNonUsableCharacter = Math
                            .max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
                }

                String identifier = lineOfText.substring(lastNonUsableCharacter + 1);
                int documentOffsetIndex = lotStart - (lastNonUsableCharacter + 2);

                PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();

                // check qualifiers
                if (identifier.indexOf(".") != -1)
                {
                    int lastDotIndex = identifier.lastIndexOf(".");
                    String prior = identifier.substring(0, lastDotIndex).toLowerCase();
                    identifier = identifier.substring(lastDotIndex + 1);
                    if (prior.indexOf(".") != -1)
                    {
                        // identifier is myOutputText after first dot
                        int index = prior.lastIndexOf(".");
                        String schema = prior.substring(0, index);
                        String packageName = prior.substring(index + 1);
                        openEditor(part, identifier, documentOffsetIndex < index ? OpenLocationType.Schema : (documentOffsetIndex < lastDotIndex ? OpenLocationType.Package : OpenLocationType.Method), schema, packageName);
                    }
                    else
                    {
                        // only one dot, could be schema or package name
                        // currText is myOutputText after the dot
                        String schema = plugin.getCurrentSchema();
                        String packageName = prior;
                        openEditor(part, identifier, documentOffsetIndex < lastDotIndex ? OpenLocationType.Package : OpenLocationType.Method, schema, packageName);
                    }
                }
                else
                {
                    List segments = plugin.getSegments(input.getFile(), doc, false);
                    navigateToSegment(part, identifier, segments, documentOffset);
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
     * 
     * @return <code>true</code> if the segment was found in the document, and false otherwise.
     */
    private static boolean navigateToSegment(IEditorPart part, String identifier, List segments, int documentOffset)
    {
        Segment s = PlSqlParserManager.instance().findNamedSegment(segments, identifier, documentOffset);
        if (s != null)
        {
            // TODO differentiate between methods of the same name
            Position p = s.getPosition();
            part.getSite().getSelectionProvider().setSelection(new TextSelection(p.getOffset(), p.getLength()));
            return true;
        }
        return false;
    }

    /**
     * This method opens an editor and moves the selection to the segment represented by
     * the <code>identifier</code>.
     * 
     * @param part The editor (or other part) from which the <code>identifier</code> was originally located.
     * @param identifier The string name of the package, procedure, function, constant etc 
     *        that is being opened.
     * @param openLocType The type of location we expect to open.
     * @param schema
     * @param packageName
     */
    public static void openEditor(IWorkbenchPart part, String identifier, OpenLocationType openLocType, String schema, String packageName)
    {
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        IFile [] files =  plugin.getFiles(schema, packageName);
        if (files.length > 0)
        {
            for (int i = 0; i < files.length; i++)
            {
                IFile file = files[i];
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
                            List<?> segments = plugin.getSegments(file, ((MultiPagePlsqlEditor) epart).getEditor().getDocumentProvider().getDocument(epart.getEditorInput()), false);
                            if (navigateToSegment(epart, identifier, segments, 0))
                            {
                                break;
                            }
                        }
                    }
                }
                catch (PartInitException e)
                {
                    e.printStackTrace();
                }
            }
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
     * We will cache myWindow object in order to be able to provide myParent shell for the message
     * dialog.
     * 
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window)
    {
        this.myWindow = window;
    }
}