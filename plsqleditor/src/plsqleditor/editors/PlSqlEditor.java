package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.LoadToDatabaseAction;

public class PlSqlEditor extends TextEditor
{
    private PlSqlContentOutlinePage fOutlinePage;
    private ProjectionSupport       fProjectionSupport;

    public PlSqlEditor()
    {
        super();
        // setDocumentProvider(new PlSqlDocumentProvider());
    }

    /**
     * This method initialises the editor. It is called on creation of the editor in the work bench,
     * NOT on setting it active.
     */
    protected void initializeEditor()
    {
        super.initializeEditor();
        setSourceViewerConfiguration(new PlSqlConfiguration());
    }

    public void dispose()
    {
        if (fOutlinePage != null)
        {
            fOutlinePage.setInput(null);
        }
        super.dispose();
    }

    private class DefineFoldingRegionAction extends TextEditorAction
    {

        private IAnnotationModel getAnnotationModel(ITextEditor editor)
        {
            return (IAnnotationModel) editor
                    .getAdapter(org.eclipse.jface.text.source.projection.ProjectionAnnotationModel.class);
        }

        public void run()
        {
            ITextEditor editor = getTextEditor();
            ISelection selection = editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection)
            {
                ITextSelection textSelection = (ITextSelection) selection;
                if (!textSelection.isEmpty())
                {
                    IAnnotationModel model = getAnnotationModel(editor);
                    if (model != null)
                    {
                        int start = textSelection.getStartLine();
                        int end = textSelection.getEndLine();
                        try
                        {
                            IDocument document = editor.getDocumentProvider().getDocument(editor
                                    .getEditorInput());
                            int offset = document.getLineOffset(start);
                            int endOffset = document.getLineOffset(end + 1);
                            Position position = new Position(offset, endOffset - offset);
                            model.addAnnotation(new ProjectionAnnotation(), position);
                        }
                        catch (BadLocationException _ex)
                        {
                            //
                        }
                    }
                }
            }
        }

        public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor)
        {
            super(bundle, prefix, editor);
        }
    }

    protected void createActions()
    {
        super.createActions();
        IAction a = new TextOperationAction(PlSqlEditorMessages.getResourceBundle(),
                "ContentAssistProposal.", this, 13);
        a.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.proposals");
        setAction("ContentAssistProposal", a);
        a = new TextOperationAction(PlSqlEditorMessages.getResourceBundle(), "ContentAssistTip.",
                this, 14);
        a.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.contextInformation");
        setAction("ContentAssistTip", a);
        a = new DefineFoldingRegionAction(PlSqlEditorMessages.getResourceBundle(),
                "DefineFoldingRegion.", this);
        setAction("DefineFoldingRegion", a);
        
        a = new LoadToDatabaseAction(PlSqlEditorMessages.getResourceBundle(),"LoadToDatabase.",this);
        a.setActionDefinitionId("plsqleditor.loadToDatabase");
        setAction("LoadToDatabase",a);
    }

    public void doRevertToSaved()
    {
        super.doRevertToSaved();
        if (fOutlinePage != null)
        {
            fOutlinePage.update();
        }
    }

    public void doSave(IProgressMonitor monitor)
    {
        super.doSave(monitor);
        if (fOutlinePage != null)
        {
            fOutlinePage.update();
        }
    }

    public void doSaveAs()
    {
        super.doSaveAs();
        if (fOutlinePage != null)
        {
            fOutlinePage.update();
        }
    }

    public void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);
        if (fOutlinePage != null)
        {
            fOutlinePage.setInput(input);
        }
    }

    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, "ContentAssistProposal");
        addAction(menu, "ContentAssistTip");
        addAction(menu, "DefineFoldingRegion");
    }

    public Object getAdapter(Class required)
    {
        if (org.eclipse.ui.views.contentoutline.IContentOutlinePage.class.equals(required))
        {
            if (fOutlinePage == null)
            {
                fOutlinePage = new PlSqlContentOutlinePage(getDocumentProvider(), this);
                if (getEditorInput() != null)
                {
                    fOutlinePage.setInput(getEditorInput());
                }
            }
            return fOutlinePage;
        }
        if (fProjectionSupport != null)
        {
            Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
            if (adapter != null) return adapter;
        }
        return super.getAdapter(required);
    }

    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess = createAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());
        ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(),
                isOverviewRulerVisible(), styles);
        getSourceViewerDecorationSupport(viewer);
        return viewer;
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error");
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
        fProjectionSupport.install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
    }

    protected void adjustHighlightRange(int offset, int length)
    {
        ISourceViewer viewer = getSourceViewer();
        if (viewer instanceof ITextViewerExtension5)
        {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }

    @Override
    public void setFocus()
    {
        // String filename = getSite().getPart().getTitle();
        IEditorInput input = getEditorInput();
        IDocument doc = getDocumentProvider().getDocument(input);

        if (input instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) input;
            IFile file = fileEditorInput.getFile();
            PlsqleditorPlugin.getDefault().setProject(file.getProject());
            PlsqleditorPlugin.getDefault().loadPackageFile(file, doc, true);
        }
        String[] scopes = getEditorSite().getKeyBindingService().getScopes();
        List<String> newScopes = new ArrayList<String>();
        newScopes.add("plsql.editor");
        for (String scope : scopes)
        {
            if (!newScopes.contains(scope))
            {
                newScopes.add(scope);
            }
        }
        getEditorSite().getKeyBindingService().setScopes(newScopes.toArray(new String[newScopes
                .size()]));
        super.setFocus();
    }

}
