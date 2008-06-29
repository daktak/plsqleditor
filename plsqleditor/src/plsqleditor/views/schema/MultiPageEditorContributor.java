package plsqleditor.views.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.process.PlDocProcessExecutor;
import plsqleditor.util.StreamProcessor;

/**
 * Manages the installation/deinstallation of global actions for multi-page
 * editors. Responsible for the redirection of global actions to the active
 * editor. Multi-page contributor replaces the contributors for the individual
 * editors in the multi-page editor.
 */
public class MultiPageEditorContributor extends MultiPageEditorActionBarContributor
{
    private IEditorPart activeEditorPart;
    private Action      runPlDocAction;

    /**
     * Creates a multi-page contributor.
     */
    public MultiPageEditorContributor()
    {
        super();
        createActions();
    }

    /**
     * Returns the action registed with the given text editor.
     * 
     * @return IAction or null if editor is null.
     */
    protected IAction getAction(ITextEditor editor, String actionID)
    {
        return (editor == null ? null : editor.getAction(actionID));
    }

    /*
     * (non-JavaDoc) Method declared in
     * AbstractMultiPageEditorActionBarContributor.
     */

    public void setActivePage(IEditorPart part)
    {
        if (activeEditorPart == part) return;

        activeEditorPart = part;

        IActionBars actionBars = getActionBars();
        if (actionBars != null)
        {

            ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;

            actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
                                              getAction(editor, ITextEditorActionConstants.DELETE));
            actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
                                              getAction(editor, ITextEditorActionConstants.UNDO));
            actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
                                              getAction(editor, ITextEditorActionConstants.REDO));
            actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
                                              getAction(editor, ITextEditorActionConstants.CUT));
            actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                                              getAction(editor, ITextEditorActionConstants.COPY));
            actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                                              getAction(editor, ITextEditorActionConstants.PASTE));
            actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                                              getAction(editor,
                                                        ITextEditorActionConstants.SELECT_ALL));
            actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
                                              getAction(editor, ITextEditorActionConstants.FIND));
            actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
                                              getAction(editor, IDEActionFactory.BOOKMARK.getId()));
            actionBars.updateActionBars();
        }
    }

    private void createActions()
    {
        runPlDocAction = new Action()
        {
            public void run()
            {
                PlDocProcessExecutor processExecutor = new PlDocProcessExecutor();
                processExecutor.executePlDoc(null);
                MessageDialog.openInformation(null,
                                              "Generate Pldoc",
                                              "Documentation generated");
            }
        };
        runPlDocAction.setText("Execute PlDoc");
        runPlDocAction.setToolTipText("Generate PlDoc information");
        try
        {
            final URL installUrl = PlsqleditorPlugin.getDefault().getDescriptor().getInstallURL();
            final URL imageUrl = new URL(installUrl, "icons/run_pldoc.gif");
            runPlDocAction.setImageDescriptor(ImageDescriptor.createFromURL(imageUrl));
        }
        catch (MalformedURLException e)
        {
            runPlDocAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                    .getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));

        }
    }
    
    public void contributeToMenu(IMenuManager manager)
    {
        IMenuManager menu = new MenuManager("Pl/Sql Editor &Menu");
        manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
        menu.add(runPlDocAction);
    }

    public void contributeToToolBar(IToolBarManager manager)
    {
        manager.add(new Separator());
        manager.add(runPlDocAction);
    }
}
