package plsqleditor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.ChangeSchemaForPackageAction;
import plsqleditor.actions.CommentBlockAction;
import plsqleditor.actions.DatabaseAction;
import plsqleditor.actions.ExecutePlDocAction;
import plsqleditor.actions.ExecuteScratchSqlAction;
import plsqleditor.actions.FormatSourceAction;
import plsqleditor.actions.GenerateHeaderAction;
import plsqleditor.actions.LoadToDatabaseAction;
import plsqleditor.actions.LowerCaseAction;
import plsqleditor.actions.RefreshErrorStatusAction;
import plsqleditor.actions.ShowDebugInfoAction;
import plsqleditor.actions.UpperCaseAction;
import plsqleditor.stores.TaskListIdentityStore;
import plsqleditor.views.schema.SchemaBrowserContentProvider;

public class PlSqlEditor extends TextEditor
{
    private static final String       PLSQLEDITOR_LOADTODATABASE_DEF_ID       = "plsqleditor.loadToDatabase";
    private static final String       PLSQLEDITOR_LOADTODATABASE_ID           = PLSQLEDITOR_LOADTODATABASE_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID = "plsqleditor.refreshErrorStatus";
    private static final String       PLSQLEDITOR_REFRESH_ERROR_STATUS_ID     = PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_GENERATEHEADER_DEF_ID       = "plsqleditor.generateHeader";
    private static final String       PLSQLEDITOR_GENERATEHEADER_ID           = PLSQLEDITOR_GENERATEHEADER_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_EXECUTE_SQL_DEF_ID          = "plsql.file.executeSql";
    private static final String       PLSQLEDITOR_EXECUTE_SQL_ID              = PLSQLEDITOR_EXECUTE_SQL_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_LOWERCASE_DEF_ID            = "plsqleditor.lowerCase";
    private static final String       PLSQLEDITOR_LOWERCASE_ID                = PLSQLEDITOR_LOWERCASE_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_UPPERCASE_DEF_ID            = "plsqleditor.upperCase";
    private static final String       PLSQLEDITOR_UPPERCASE_ID                = PLSQLEDITOR_UPPERCASE_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_SHOWDEBUG_DEF_ID            = "plsqleditor.showDebugInfo";
    private static final String       PLSQLEDITOR_SHOWDEBUG_ID                = PLSQLEDITOR_SHOWDEBUG_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_COMMIT_DEF_ID               = "plsql.db.commit";
    private static final String       PLSQLEDITOR_COMMIT_ID                   = PLSQLEDITOR_COMMIT_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_ROLLBACK_DEF_ID             = "plsql.db.rollback";
    private static final String       PLSQLEDITOR_ROLLBACK_ID                 = PLSQLEDITOR_ROLLBACK_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_COMMENT_DEF_ID              = "plsqleditor.commentblock";
    private static final String       PLSQLEDITOR_COMMENT_ID                  = PLSQLEDITOR_COMMENT_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_UNCOMMENT_DEF_ID            = "plsqleditor.uncommentblock";
    private static final String       PLSQLEDITOR_UNCOMMENT_ID                = PLSQLEDITOR_UNCOMMENT_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_FORMAT_DEF_ID               = "plsqleditor.formatSource";
    private static final String       PLSQLEDITOR_FORMAT_ID                   = PLSQLEDITOR_FORMAT_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_GENERATE_PLDOC_DEF_ID       = "plsqleditor.generatePlDoc";
    private static final String       PLSQLEDITOR_GENERATE_PLDOC_ID           = PLSQLEDITOR_GENERATE_PLDOC_DEF_ID
                                                                                      + ".action";
    private static final String       PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID        = "plsqleditor.changeSchemaForPackage";
    private static final String       PLSQLEDITOR_CHANGE_SCHEMA_ID            = PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID
                                                                                      + ".action";
    private PlSqlContentOutlinePage   fOutlinePage;
    private ProjectionSupport         fProjectionSupport;
    public static final String        NEW_LINE                                = System
                                                                                      .getProperty("line.separator");
    public static final String        FOLD_START                              = "--#startFolding";
    public static final String        FOLD_END                                = "--#endFolding";


    private Annotation[]              oldAnnotations;
    private ProjectionAnnotationModel annotationModel;

    public PlSqlEditor()
    {
        //
    }

    public void updateTodoTags(List positions)
    {
        Map taskMap = TaskListIdentityStore.instance().getMarkers();

        IEditorInput input = getEditorInput();
        if (input instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) input;
            IFile file = fileEditorInput.getFile();
            int depth = IResource.DEPTH_INFINITE;
            try
            {
                file.deleteMarkers(IMarker.TASK, true, depth);
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
            IDocument document = getDocumentProvider().getDocument(input);
            for (Iterator it = positions.iterator(); it.hasNext();)
            {
                try
                {
                    Position p = (Position) it.next();
                    String detail = document.get(p.offset, p.length);
                    Integer priority = new Integer(IMarker.PRIORITY_NORMAL);
                    for (Iterator it2 = taskMap.keySet().iterator(); it2.hasNext();)
                    {
                        String marker = (String) it2.next();
                        if (detail.startsWith(marker))
                        {
                            priority = (Integer) taskMap.get(marker);
                            break;
                        }
                    }
                    Map attributes = new HashMap();
                    // MarkerUtilities.setLineNumber(attributes, line + 1);
                    MarkerUtilities.setCharStart(attributes, p.offset);
                    MarkerUtilities.setCharEnd(attributes, p.offset + p.length);

                    attributes.put(IMarker.PRIORITY, priority);
                    MarkerUtilities.setMessage(attributes, detail);
                    int severity = IMarker.SEVERITY_INFO;
                    attributes.put(IMarker.SEVERITY, new Integer(severity));
                    MarkerUtilities.createMarker(file, attributes, IMarker.TASK);
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            if (input != null)
            {
                System.out.println("input type is " + input.getClass().getName());
            }
            else
            {
                System.out.println("Input is null");
            }
        }
    }

    public void updateFoldingStructure(List positions)
    {
        Annotation[] annotations = new Annotation[positions.size()];

        // this will hold the new annotations along
        // with their corresponding positions
        HashMap newAnnotations = new HashMap();

        for (int i = 0; i < positions.size(); i++)
        {
            ProjectionAnnotation annotation = new ProjectionAnnotation();

            newAnnotations.put(annotation, positions.get(i));

            annotations[i] = annotation;
        }

        if (annotationModel == null)
        {
            System.out.println("The annotation model is currently null");
            new Exception().printStackTrace();
        }
        else
        {
            annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
        }

        oldAnnotations = annotations;
    }

    /**
     * This method initialises the editor. It is called on creation of the
     * editor in the work bench, NOT on setting it active.
     */
    protected void initializeEditor()
    {
        super.initializeEditor();
        setSourceViewerConfiguration(new PlSqlConfiguration(this));
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
            return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
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
                            document.replace(offset, 0, FOLD_START + NEW_LINE);
                            document.replace(endOffset + (FOLD_START + NEW_LINE).length(),
                                             0,
                                             FOLD_END + NEW_LINE);
                            // this works because of the assigned
                            // PlSqlReconcilingStrategy updating the folding
                            // regions
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
        a = new LoadToDatabaseAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_LOADTODATABASE_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_LOADTODATABASE_DEF_ID);
        setAction(PLSQLEDITOR_LOADTODATABASE_ID, a);
        a = new RefreshErrorStatusAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_REFRESH_ERROR_STATUS_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID);
        setAction(PLSQLEDITOR_REFRESH_ERROR_STATUS_ID, a);
        a = new GenerateHeaderAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_GENERATEHEADER_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_GENERATEHEADER_DEF_ID);
        setAction(PLSQLEDITOR_GENERATEHEADER_ID, a);
        a = new ExecuteScratchSqlAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_EXECUTE_SQL_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_EXECUTE_SQL_DEF_ID);
        setAction(PLSQLEDITOR_EXECUTE_SQL_ID, a);
        a = new LowerCaseAction(PlSqlEditorMessages.getResourceBundle(), PLSQLEDITOR_LOWERCASE_ID
                + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_LOWERCASE_DEF_ID);
        setAction(PLSQLEDITOR_LOWERCASE_ID, a);
        a = new UpperCaseAction(PlSqlEditorMessages.getResourceBundle(), PLSQLEDITOR_UPPERCASE_ID
                + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_UPPERCASE_DEF_ID);
        setAction(PLSQLEDITOR_UPPERCASE_ID, a);
        a = new ShowDebugInfoAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_SHOWDEBUG_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_SHOWDEBUG_DEF_ID);
        setAction(PLSQLEDITOR_SHOWDEBUG_ID, a);
        a = new DatabaseAction.CommitAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_COMMIT_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_COMMIT_DEF_ID);
        setAction(PLSQLEDITOR_COMMIT_ID, a);
        a = new DatabaseAction.RollbackAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_ROLLBACK_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_ROLLBACK_DEF_ID);
        setAction(PLSQLEDITOR_ROLLBACK_ID, a);
        a = new CommentBlockAction(PlSqlEditorMessages.getResourceBundle(), PLSQLEDITOR_COMMENT_ID
                + ".", this, true);
        a.setActionDefinitionId(PLSQLEDITOR_COMMENT_DEF_ID);
        setAction(PLSQLEDITOR_COMMENT_ID, a);
        a = new CommentBlockAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_UNCOMMENT_ID + ".", this, false);
        a.setActionDefinitionId(PLSQLEDITOR_UNCOMMENT_DEF_ID);
        setAction(PLSQLEDITOR_UNCOMMENT_ID, a);
        a = new FormatSourceAction(PlSqlEditorMessages.getResourceBundle(), PLSQLEDITOR_FORMAT_ID
                + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_FORMAT_DEF_ID);
        setAction(PLSQLEDITOR_FORMAT_ID, a);

        a = new ExecutePlDocAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_GENERATE_PLDOC_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_GENERATE_PLDOC_DEF_ID);
        setAction(PLSQLEDITOR_GENERATE_PLDOC_ID, a);

        a = new ChangeSchemaForPackageAction(PlSqlEditorMessages.getResourceBundle(),
                PLSQLEDITOR_CHANGE_SCHEMA_ID + ".", this);
        a.setActionDefinitionId(PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID);
        setAction(PLSQLEDITOR_CHANGE_SCHEMA_ID, a);
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
        // TODO get some kind of selection listener
        // getSite().getPage().addSelectionListener(theSelectionListener);
    }

    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, "ContentAssistProposal");
        addAction(menu, "ContentAssistTip");
        addAction(menu, "DefineFoldingRegion");
        // menu.add(new GroupMarker("PLSQL"));
        menu.add(new Separator("PLSQL"));
        addAction(menu, "PLSQL", PLSQLEDITOR_LOADTODATABASE_ID);
        addAction(menu, "PLSQL", PLSQLEDITOR_REFRESH_ERROR_STATUS_ID);

        IEditorInput input = getEditorInput();
        String filename = "";
        if (input instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) input;
            IFile file = fileEditorInput.getFile();
            filename = file.getName();
        }
        else
        {
            if (input != null)
            {
                System.out.println("input type is " + input.getClass().getName());
            }
            else
            {
                System.out.println("Input is null");
            }
        }
        if (filename.indexOf(".pkh") != -1)
        {
            addAction(menu, "PLSQL", PLSQLEDITOR_GENERATE_PLDOC_ID);
        }
        else if (filename.indexOf(".pkb") != -1)
        {
            addAction(menu, "PLSQL", PLSQLEDITOR_GENERATEHEADER_ID);
        }
        addAction(menu, "PLSQL", PLSQLEDITOR_EXECUTE_SQL_ID);
        addAction(menu, "PLSQL", PLSQLEDITOR_CHANGE_SCHEMA_ID);
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
            if (adapter != null)
            {
                return adapter;
            }
        }
        return super.getAdapter(required);
    }

    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess = createAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());
        ISourceViewer viewer = new PlSqlSourceViewer(parent, ruler, getOverviewRuler(),
                                                    isOverviewRulerVisible(), styles, getPreferenceStore());
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
        annotationModel = viewer.getProjectionAnnotationModel();
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

    public void setFocus()
    {
        // String filename = getSite().getPart().getTitle();
        IEditorInput input = getEditorInput();
        IDocument doc = getDocumentProvider().getDocument(input);
        if (input instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) input;
            IFile file = fileEditorInput.getFile();

            PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
            plugin.setCurrentFile(file);
            plugin.setProject(file.getProject());
            plugin.loadPackageFile(file, doc, true);
            // TODO figure out a better place for this
            SchemaBrowserContentProvider.getInstance().refresh();
        }
        String[] scopes = getEditorSite().getKeyBindingService().getScopes();
        List newScopes = new ArrayList();
        newScopes.add("plsql.editor");
        for (int i = 0; i < scopes.length; i++)
        {
            String scope = scopes[i];
            if (!newScopes.contains(scope))
            {
                newScopes.add(scope);
            }
        }
        getEditorSite().getKeyBindingService().setScopes((String[]) newScopes
                .toArray(new String[newScopes.size()]));
        super.setFocus();
    }
}
