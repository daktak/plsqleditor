package plsqleditor.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.ChangeSchemaForPackageAction;
import plsqleditor.actions.CommentBlockAction;
import plsqleditor.actions.DatabaseAction;
import plsqleditor.actions.ExecutePlDocAction;
import plsqleditor.actions.ExecuteScratchSqlAction;
import plsqleditor.actions.FormatSourceAction;
import plsqleditor.actions.GenerateHeaderAction;
import plsqleditor.actions.Jump2BracketAction;
import plsqleditor.actions.LoadToDatabaseAction;
import plsqleditor.actions.LowerCaseAction;
import plsqleditor.actions.ManageConnectionDetailsAction;
import plsqleditor.actions.RefreshErrorStatusAction;
import plsqleditor.actions.ShowDebugInfoAction;
import plsqleditor.actions.UpperCaseAction;
import plsqleditor.editors.model.SourceFile;
import plsqleditor.parsers.Segment;
import plsqleditor.preferences.MarkOccurrencesPreferences;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.TaskListIdentityStore;
import plsqleditor.views.schema.SchemaBrowserContentProvider;

public class PlSqlEditor extends TextEditor
{
	public static final String PLSQLEDITOR_LOADTODATABASE_DEF_ID = "plsqleditor.loadToDatabase";
	public static final String PLSQLEDITOR_LOADTODATABASE_ID = PLSQLEDITOR_LOADTODATABASE_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID = "plsqleditor.refreshErrorStatus";
	private static final String PLSQLEDITOR_REFRESH_ERROR_STATUS_ID = PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_GENERATEHEADER_DEF_ID = "plsqleditor.generateHeader";
	private static final String PLSQLEDITOR_GENERATEHEADER_ID = PLSQLEDITOR_GENERATEHEADER_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_EXECUTE_SQL_DEF_ID = "plsql.file.executeSql";
	private static final String PLSQLEDITOR_EXECUTE_SQL_ID = PLSQLEDITOR_EXECUTE_SQL_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_LOWERCASE_DEF_ID = "plsqleditor.lowerCase";
	private static final String PLSQLEDITOR_LOWERCASE_ID = PLSQLEDITOR_LOWERCASE_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_UPPERCASE_DEF_ID = "plsqleditor.upperCase";
	private static final String PLSQLEDITOR_UPPERCASE_ID = PLSQLEDITOR_UPPERCASE_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_SHOWDEBUG_DEF_ID = "plsqleditor.showDebugInfo";
	private static final String PLSQLEDITOR_SHOWDEBUG_ID = PLSQLEDITOR_SHOWDEBUG_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_COMMIT_DEF_ID = "plsql.db.commit";
	private static final String PLSQLEDITOR_COMMIT_ID = PLSQLEDITOR_COMMIT_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_ROLLBACK_DEF_ID = "plsql.db.rollback";
	private static final String PLSQLEDITOR_ROLLBACK_ID = PLSQLEDITOR_ROLLBACK_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_COMMENT_DEF_ID = "plsqleditor.commentblock";
	private static final String PLSQLEDITOR_COMMENT_ID = PLSQLEDITOR_COMMENT_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_UNCOMMENT_DEF_ID = "plsqleditor.uncommentblock";
	private static final String PLSQLEDITOR_UNCOMMENT_ID = PLSQLEDITOR_UNCOMMENT_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_FORMAT_DEF_ID = "plsqleditor.formatSource";
	private static final String PLSQLEDITOR_FORMAT_ID = PLSQLEDITOR_FORMAT_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_GENERATE_PLDOC_DEF_ID = "plsqleditor.generatePlDoc";
	public static final String PLSQLEDITOR_GENERATE_PLDOC_ID = PLSQLEDITOR_GENERATE_PLDOC_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID = "plsqleditor.changeSchemaForPackage";
	static final String PLSQLEDITOR_CHANGE_SCHEMA_ID = PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID
			+ ".action";
	private static final String PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_DEF_ID = "plsqleditor.manageConnectionDetails";
	protected static final String PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_ID = PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_DEF_ID
			+ ".action";

	private PlSqlContentOutlinePage fOutlinePage;
	private ProjectionSupport fProjectionSupport;
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final String FOLD_START = "--#startFolding";
	public static final String FOLD_END = "--#endFolding";

	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;
	private OccurrencesUpdater occurrencesUpdater;
	private ISourceViewer mySourceViewer;
	private PlsqlPairMatcher bracketMatcher;
	private boolean syncFromOutline = false;
	private boolean syncToOutline;
	protected SourceFile mySource;
	private FoldReconciler myFoldReconciler;
	private BracketInserter bracketInserter;

	public PlSqlEditor()
	{
		// this line should remove the need for the plsql document setup
		// participant, but it doesn't
		setDocumentProvider(PlsqleditorPlugin.getDefault()
				.getDocumentProvider());
		setKeyBindingScopes(new String[] { "plsql.editor" });
	}

	public void updateTodoTags(List<Position> positions)
	{
		Map<String, Integer> taskMap = TaskListIdentityStore.instance()
				.getMarkers();

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
			IDocumentProvider provider = getDocumentProvider();
			if (provider == null)
			{
				return;
			}
			IDocument document = provider.getDocument(input);
			for (Position p : positions)
			{
				try
				{
					String detail = document.get(p.offset, p.length);
					Integer priority = Integer.valueOf(IMarker.PRIORITY_NORMAL);
					for (String marker : taskMap.keySet())
					{
						if (detail.startsWith(marker))
						{
							priority = taskMap.get(marker);
							break;
						}
					}
					Map<String, Object> attributes = new HashMap<String, Object>();
					// MarkerUtilities.setLineNumber(attributes, line + 1);
					MarkerUtilities.setCharStart(attributes, p.offset);
					MarkerUtilities.setCharEnd(attributes, p.offset + p.length);

					attributes.put(IMarker.PRIORITY, priority);
					MarkerUtilities.setMessage(attributes, detail);
					int severity = IMarker.SEVERITY_INFO;
					attributes.put(IMarker.SEVERITY, Integer.valueOf(severity));
					MarkerUtilities
							.createMarker(file, attributes, IMarker.TASK);
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
				System.out.println("input type is "
						+ input.getClass().getName());
			}
			else
			{
				System.out.println("Input is null");
			}
		}
	}

	public void updateFoldingStructure(List<Position> positions)
	{
		Annotation[] annotations = new Annotation[positions.size()];

		// this will hold the new annotations along
		// with their corresponding positions
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

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
			annotationModel.modifyAnnotations(oldAnnotations, newAnnotations,
					null);
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
		setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] {
				EditorsUI.getPreferenceStore(),
				PlsqleditorPlugin.getDefault().getPreferenceStore(),
				this.getPreferenceStore() }));

		setSourceViewerConfiguration(new PlSqlConfiguration(PlsqleditorPlugin
				.getDefault().getPreferenceStore(), this));
	}

	public void dispose()
	{
		uninstallAnnotationListener();
		if (fOutlinePage != null)
		{
			fOutlinePage.setInput(null);
		}
		if (mySourceViewer instanceof ITextViewerExtension
				&& bracketInserter != null)
		{
			((ITextViewerExtension) mySourceViewer)
					.removeVerifyKeyListener(bracketInserter);
		}

		super.dispose();
	}

	private class DefineFoldingRegionAction extends TextEditorAction
	{
		private IAnnotationModel getAnnotationModel(ITextEditor editor)
		{
			return (IAnnotationModel) editor
					.getAdapter(ProjectionAnnotationModel.class);
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
							IDocument document = editor.getDocumentProvider()
									.getDocument(editor.getEditorInput());
							int offset = document.getLineOffset(start);
							int endOffset = document.getLineOffset(end + 1);
							document.replace(offset, 0, FOLD_START + NEW_LINE);
							document.replace(endOffset
									+ (FOLD_START + NEW_LINE).length(), 0,
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

		public DefineFoldingRegionAction(ResourceBundle bundle, String prefix,
				ITextEditor editor)
		{
			super(bundle, prefix, editor);
		}
	}

	protected void createActions()
	{
		super.createActions();
		IAction a = new TextOperationAction(PlSqlEditorMessages
				.getResourceBundle(), "ContentAssistProposal.", this, 13);
		a
				.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.proposals");
		setAction("ContentAssistProposal", a);
		a = new TextOperationAction(PlSqlEditorMessages.getResourceBundle(),
				"ContentAssistTip.", this, 14);
		a
				.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.contextInformation");
		setAction("ContentAssistTip", a);
		a = new DefineFoldingRegionAction(PlSqlEditorMessages
				.getResourceBundle(), "DefineFoldingRegion.", this);
		setAction("DefineFoldingRegion", a);
		a = new LoadToDatabaseAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_LOADTODATABASE_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_LOADTODATABASE_DEF_ID);
		setAction(PLSQLEDITOR_LOADTODATABASE_ID, a);
		a = new RefreshErrorStatusAction(PlSqlEditorMessages
				.getResourceBundle(),
				PLSQLEDITOR_REFRESH_ERROR_STATUS_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_REFRESH_ERROR_STATUS_DEF_ID);
		setAction(PLSQLEDITOR_REFRESH_ERROR_STATUS_ID, a);
		a = new GenerateHeaderAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_GENERATEHEADER_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_GENERATEHEADER_DEF_ID);
		setAction(PLSQLEDITOR_GENERATEHEADER_ID, a);
		a = new ExecuteScratchSqlAction(
				PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_EXECUTE_SQL_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_EXECUTE_SQL_DEF_ID);
		setAction(PLSQLEDITOR_EXECUTE_SQL_ID, a);
		a = new LowerCaseAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_LOWERCASE_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_LOWERCASE_DEF_ID);
		setAction(PLSQLEDITOR_LOWERCASE_ID, a);
		a = new UpperCaseAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_UPPERCASE_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_UPPERCASE_DEF_ID);
		setAction(PLSQLEDITOR_UPPERCASE_ID, a);
		a = new ShowDebugInfoAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_SHOWDEBUG_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_SHOWDEBUG_DEF_ID);
		setAction(PLSQLEDITOR_SHOWDEBUG_ID, a);
		a = new DatabaseAction.CommitAction(PlSqlEditorMessages
				.getResourceBundle(), PLSQLEDITOR_COMMIT_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_COMMIT_DEF_ID);
		setAction(PLSQLEDITOR_COMMIT_ID, a);
		a = new DatabaseAction.RollbackAction(PlSqlEditorMessages
				.getResourceBundle(), PLSQLEDITOR_ROLLBACK_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_ROLLBACK_DEF_ID);
		setAction(PLSQLEDITOR_ROLLBACK_ID, a);
		a = new CommentBlockAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_COMMENT_ID + ".", this, true);
		a.setActionDefinitionId(PLSQLEDITOR_COMMENT_DEF_ID);
		setAction(PLSQLEDITOR_COMMENT_ID, a);
		a = new CommentBlockAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_UNCOMMENT_ID + ".", this, false);
		a.setActionDefinitionId(PLSQLEDITOR_UNCOMMENT_DEF_ID);
		setAction(PLSQLEDITOR_UNCOMMENT_ID, a);
		a = new FormatSourceAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_FORMAT_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_FORMAT_DEF_ID);
		setAction(PLSQLEDITOR_FORMAT_ID, a);

		a = new ExecutePlDocAction(PlSqlEditorMessages.getResourceBundle(),
				PLSQLEDITOR_GENERATE_PLDOC_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_GENERATE_PLDOC_DEF_ID);
		setAction(PLSQLEDITOR_GENERATE_PLDOC_ID, a);

		a = new ChangeSchemaForPackageAction(PlSqlEditorMessages
				.getResourceBundle(), PLSQLEDITOR_CHANGE_SCHEMA_ID + ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_CHANGE_SCHEMA_DEF_ID);
		setAction(PLSQLEDITOR_CHANGE_SCHEMA_ID, a);

		a = new ManageConnectionDetailsAction(PlSqlEditorMessages
				.getResourceBundle(), PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_ID
				+ ".", this);
		a.setActionDefinitionId(PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_DEF_ID);
		setAction(PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_ID, a);

		a = new Jump2BracketAction(this);
		a.setActionDefinitionId("plsqleditor.commands.jumpToBracket");
		setAction(Jump2BracketAction.getPlsqlEditorId(), a);
	}

	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		if (fOutlinePage != null && mySource != null)
		{
			mySource.parse();
			fOutlinePage.update();
		}
	}

	public void doSave(IProgressMonitor monitor)
	{
		super.doSave(monitor);
		if (fOutlinePage != null && mySource != null)
		{
			mySource.parse();
			fOutlinePage.update();
		}
	}

	public void doSaveAs()
	{
		super.doSaveAs();
		if (fOutlinePage != null && mySource != null)
		{
			mySource.parse();
			fOutlinePage.update();
		}
	}

	public void doSetInput(IEditorInput input) throws CoreException
	{
		super.doSetInput(input);

		if (getSourceViewer() != null)
		{
			// The editor is being reused (e.g. when the user clicks on matches
			// found through a search). Make sure we synchronize with the new
			// content.
			if (input instanceof IFileEditorInput)
			{
				IFileEditorInput ifeInput = (IFileEditorInput) input;
				mySource = new SourceFile(getSourceViewer().getDocument(),
						ifeInput.getFile());
			}
			reconcile();
		}
	}

	/**
	 * Updates the editor's dependent views and state after a document change.
	 * This method is only intended for use by {@link PerlReconcilingStrategy}.
	 */
	public void reconcile()
	{
		// the problem is, we might be called after the ISourceViewer
		// has been disposed; this occurs BEFORE dispose() is invoked
		// on the Editor, so there seems to be no good way to
		// synchronise properly
		if (mySourceViewer == null)
		{
			return;
		}
		StyledText widget = mySourceViewer.getTextWidget();
		if (widget == null || widget.isDisposed())
		{
			return;
		}
		Display display = widget.getDisplay();
		if (display == null)
		{
			return;
		}
		final IDocument doc = mySourceViewer.getDocument();
		if (doc == null)
		{
			return;
		}

		// We reconcile on the main (Display) thread in order to avoid
		// race conditions due to user's modifications; this also means
		// that the reconciling has to be FAST in order to keep the GUI
		// responsive.
		//
		display.syncExec(new Runnable()
		{
			public void run()
			{
				mySource.parse(); // does nothing atm

				if (fOutlinePage != null)
				{
					fOutlinePage.setInput(mySource);
				}
				if (myFoldReconciler != null)
				{
					myFoldReconciler.reconcile();
				}
			}
		});
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
				System.out.println("input type is "
						+ input.getClass().getName());
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
			addAction(menu, "PLSQL", PLSQLEDITOR_GENERATE_PLDOC_ID);
		}
		addAction(menu, "PLSQL", PLSQLEDITOR_EXECUTE_SQL_ID);
		addAction(menu, "PLSQL", PLSQLEDITOR_CHANGE_SCHEMA_ID);
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(@SuppressWarnings("rawtypes") Class required)
	{
		if (ProjectionAnnotationModel.class.equals(required))
		{
			if (this.fProjectionSupport != null)
			{
				Object result = this.fProjectionSupport.getAdapter(
						mySourceViewer, required);
				if (result != null)
				{
					return result;
				}
			}
		}
		if (IContentOutlinePage.class.equals(required))
		{
			if (fOutlinePage == null)
			{
				fOutlinePage = new PlSqlContentOutlinePage(
						getDocumentProvider(), this);
				if (mySource != null)
				{
					mySource.parse();
					fOutlinePage.setInput(mySource);
				}
				// / if (getEditorInput() != null)
				// {
				// fOutlinePage.setInput(getEditorInput());
				// }
				fOutlinePage
						.addSelectionChangedListener(new OutlineSelectionListener());
			}
			return fOutlinePage;
		}

		// TODO maybe remove this
		if (fProjectionSupport != null)
		{
			Object adapter = fProjectionSupport.getAdapter(getSourceViewer(),
					required);
			if (adapter != null)
			{
				return adapter;
			}
		}
		return super.getAdapter(required);
	}

	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles)
	{
		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		mySourceViewer = new PlSqlSourceViewer(parent, ruler, fOverviewRuler,
				isOverviewRulerVisible(), styles, getPreferenceStore());
		installBracketMatcher();
		getSourceViewerDecorationSupport(mySourceViewer);

		mySourceViewer.getTextWidget().setWordWrap(
				getPreferenceStore().getBoolean(
						PreferenceConstants.EDITOR_LINE_WRAP));

		return mySourceViewer;
	}

	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		ProjectionViewer viewer = installProjectSupport();
		annotationModel = viewer.getProjectionAnnotationModel();
		installAnnotationListener();
		installCaretMoveListener();
		installFoldReconciler();
		installBracketInserter();

		mySource = new SourceFile(mySourceViewer.getDocument(), getFile());

		reconcile();
	}

	private void installFoldReconciler()
	{
		myFoldReconciler = new FoldReconciler(this);
	}

	/**
	 * @return
	 */
	private ProjectionViewer installProjectSupport()
	{
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		fProjectionSupport = new ProjectionSupport(viewer,
				getAnnotationAccess(), getSharedColors());
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error");
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		return viewer;
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
			SchemaBrowserContentProvider.getInstance().refresh(false);
		}
		// hopefully the lines below are replaced with setKeyBindingScopes(x) in
		// the constructor

		// String[] scopes = getEditorSite().getKeyBindingService().getScopes();
		// List<String> newScopes = new ArrayList<String>();
		// newScopes.add("plsql.editor");
		// for (int i = 0; i < scopes.length; i++)
		// {
		// String scope = scopes[i];
		// if (!newScopes.contains(scope))
		// {
		// newScopes.add(scope);
		// }
		// }
		// getEditorSite().getKeyBindingService().setScopes(
		// (String[]) newScopes.toArray(new String[newScopes.size()]));
		super.setFocus();
	}

	public IFile getFile()
	{
		return ((IFileEditorInput) getEditorInput()).getFile();
	}

	protected void createNavigationActions()
	{
		super.createNavigationActions();

		IAction action;
		StyledText textWidget = getSourceViewer().getTextWidget();

		action = new SmartLineStartAction(textWidget, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_START);
		setAction(ITextEditorActionDefinitionIds.LINE_START, action);

		action = new SmartLineStartAction(textWidget, true);
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_START);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_START, action);

		// action = new NextWordAction(ST.WORD_NEXT, false, false);
		// action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		// setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		// textWidget.setKeyBinding(SWT.MOD1 | SWT.ARROW_RIGHT, SWT.NULL);
		//
		// action = new NextWordAction(ST.SELECT_WORD_NEXT, true, false);
		// action
		// .setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		// setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		// textWidget.setKeyBinding(SWT.MOD1 | SWT.MOD2 | SWT.ARROW_RIGHT,
		// SWT.NULL);
		//
		// action = new NextWordAction(ST.DELETE_WORD_NEXT, false, true);
		// action
		// .setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
		// setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
		// textWidget.setKeyBinding(SWT.MOD1 | SWT.DEL, SWT.NULL);
		//
		// action = new PreviousWordAction(ST.WORD_PREVIOUS, false, false);
		// action
		// .setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		// setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		// textWidget.setKeyBinding(SWT.MOD1 | SWT.ARROW_LEFT, SWT.NULL);
		//
		// action = new PreviousWordAction(ST.SELECT_WORD_PREVIOUS, true,
		// false);
		// action
		// .setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		// setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS,
		// action);
		// textWidget
		// .setKeyBinding(SWT.MOD1 | SWT.MOD2 | SWT.ARROW_LEFT, SWT.NULL);
		//
		// action = new PreviousWordAction(ST.DELETE_WORD_PREVIOUS, false,
		// true);
		// action
		// .setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
		// setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD,
		// action);
		// textWidget.setKeyBinding(SWT.MOD1 | SWT.BS, SWT.NULL);
	}

	protected void handlePreferenceStoreChanged(PropertyChangeEvent event)
	{
		if (mySourceViewer == null || mySourceViewer.getTextWidget() == null
				|| bracketInserter == null)
		{
			return;
		}

		try
		{
			if (event.getProperty().equals(
					MarkOccurrencesPreferences.MARK_OCCURRENCES))
			{
				boolean oldValue = event.getOldValue() != null ? Boolean
						.valueOf(event.getOldValue().toString()).booleanValue()
						: false;

				boolean newValue = event.getOldValue() != null ? Boolean
						.valueOf(event.getNewValue().toString()).booleanValue()
						: false;

				if (newValue != oldValue)
				{
					if (newValue) installAnnotationListener();
					else uninstallAnnotationListener();
				}
			}
			else if (event.getProperty().equals(
					PreferenceConstants.EDITOR_LINE_WRAP))
			{
				boolean oldValue = event.getOldValue() != null ? Boolean
						.valueOf(event.getOldValue().toString()).booleanValue()
						: false;

				boolean newValue = event.getOldValue() != null ? Boolean
						.valueOf(event.getNewValue().toString()).booleanValue()
						: false;

				if (newValue != oldValue)
				{
					mySourceViewer.getTextWidget().setWordWrap(newValue);
				}
			}
			else
			{
				reconfigureBracketInserter();
			}
		}
		finally
		{
			super.handlePreferenceStoreChanged(event);
		}
	}

	private void caretMoved()
	{
		if (!getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE))
		{
			return;
		}
		if (syncFromOutline)
		{
			syncFromOutline = false;
			return;
		}
		if (fOutlinePage == null || mySourceViewer.getDocument() == null
				|| mySourceViewer.getDocument() == null)
		{
			return;
		}

//      try
//      {
            syncToOutline = true;

            int caretOffset = mySourceViewer.getSelectedRange().x;
//          int caretLine = mySourceViewer.getDocument().getLineOfOffset(
//                  caretOffset);

            fOutlinePage.updateSelection(caretOffset);
            syncToOutline = false;
//      }
//      catch (BadLocationException e)
//      {
//          e.printStackTrace();
//      }
//      finally
//      {
//          syncToOutline = false;
//      }
	}

	/**
	 * @return the edited workspace resource or null if the editor's input is
	 *         not a workspace resource (example: remote files opened while
	 *         browsing CVS are not resources)
	 */
	public IResource getResource()
	{
		return (IResource) ((IAdaptable) getEditorInput())
				.getAdapter(IResource.class);
	}

	protected void configureSourceViewerDecorationSupport(
			SourceViewerDecorationSupport support)
	{
		support.setCharacterPairMatcher(bracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(
				PreferenceConstants.EDITOR_MATCHING_BRACKETS,
				PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);

		super.configureSourceViewerDecorationSupport(support);
	}

	private void installBracketInserter()
	{
		bracketInserter = new BracketInserter(PlsqleditorPlugin.getDefault()
				.getLog());

		reconfigureBracketInserter();

		if (mySourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) mySourceViewer)
					.prependVerifyKeyListener(bracketInserter);

		bracketInserter.setViewer(mySourceViewer);
	}

	private void reconfigureBracketInserter()
	{
		IPreferenceStore preferenceStore = getPreferenceStore();

		bracketInserter.setCloseBracketsEnabled(preferenceStore
				.getBoolean(PreferenceConstants.AUTO_COMPLETION_BRACKET1));
		// bracketInserter.setCloseBracesEnabled(
		// preferenceStore.getBoolean(PreferenceConstants.AUTO_COMPLETION_BRACKET2));
		bracketInserter.setCloseParensEnabled(preferenceStore
				.getBoolean(PreferenceConstants.AUTO_COMPLETION_BRACKET3));
		// bracketInserter.setCloseAngularBracketsEnabled(
		// preferenceStore.getBoolean(PreferenceConstants.AUTO_COMPLETION_BRACKET4));
		bracketInserter.setCloseDoubleQuotesEnabled(preferenceStore
				.getBoolean(PreferenceConstants.AUTO_COMPLETION_QUOTE1));
		bracketInserter.setCloseSingleQuotesEnabled(preferenceStore
				.getBoolean(PreferenceConstants.AUTO_COMPLETION_QUOTE2));
	}

	private void installBracketMatcher()
	{
		bracketMatcher = new PlsqlPairMatcher(PlsqleditorPlugin.getDefault()
				.getLog());
		bracketMatcher.setViewer(mySourceViewer);
	}

	private void installCaretMoveListener()
	{
		new CaretMoveListener().install(getSelectionProvider());
	}

	private void installAnnotationListener()
	{
		if (occurrencesUpdater == null)
		{
			occurrencesUpdater = new OccurrencesUpdater();
		}

		occurrencesUpdater.install(mySourceViewer);
	}

	private void uninstallAnnotationListener()
	{
		if (occurrencesUpdater == null)
		{
			return;
		}

		occurrencesUpdater.uninstall();
	}

	/**
	 * Provided that the given document contains a bracket-like character at the
	 * given offset, returns the offset of the matching (pair) character (if
	 * found). Otherwise, returns -1.
	 * 
	 * @param exact
	 *            if false, search for the match within currently displayed text
	 *            only; if true, search in the entire document
	 */
	public int findMatchingBracket(final IDocument document, final int offset,
			boolean exact)
	{
		if (exact)
		{
			bracketMatcher.setViewer(null);
		}
		try
		{
			final int[] ret = new int[1];

			IRegion matchRegion = bracketMatcher.match(document, offset);

			if (matchRegion == null)
			{
				ret[0] = -1;
			}
			else
			{
				ret[0] = matchRegion.getOffset() == offset - 1 ? matchRegion
						.getOffset()
						+ matchRegion.getLength() - 1 : matchRegion.getOffset();
			}

			return ret[0];
		}
		finally
		{
			if (exact)
			{
				bracketMatcher.setViewer(mySourceViewer);
			}
		}
	}

	/**
	 * Provided that the caret's current position is after a bracket-like
	 * character, jumps to its matching character (if found). Otherwise, this
	 * method has no effect.
	 * 
	 */
	public void jumpToMatchingBracket()
	{
		int caretOffset = mySourceViewer.getSelectedRange().x;
		int matchOffset = findMatchingBracket(mySourceViewer.getDocument(),
				caretOffset, true);

		if (matchOffset == -1) return;

		mySourceViewer.revealRange(matchOffset + 1, 1);
		mySourceViewer.setSelectedRange(matchOffset + 1, 0);
	}

	/**
	 * Tracks caret movements in order to update selection in the outline page.
	 * Implementation borrowed from
	 * org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.
	 */
	private class CaretMoveListener implements ISelectionChangedListener
	{
		/**
		 * Installs this selection changed listener with the given selection
		 * provider. If the selection provider is a post selection provider,
		 * post selection changed events are the preferred choice, otherwise
		 * normal selection changed events are requested.
		 * 
		 * @param selectionProvider
		 */
		public void install(ISelectionProvider selectionProvider)
		{
			if (selectionProvider == null) return;

			if (selectionProvider instanceof IPostSelectionProvider)
			{
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			}
			else
			{
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		public void selectionChanged(SelectionChangedEvent event)
		{
			ISelection selection = event.getSelection();
			if (selection instanceof TextSelection)
			{
				TextSelection textSelection = (TextSelection) selection;
				IDocument doc = getDocumentProvider().getDocument(
						getEditorInput());
				int startLine = textSelection.getStartLine() + 1;
				int length = textSelection.getLength();
				String msg = startLine + " : " + length;
				try
				{
//					int fullOffset = textSelection.getOffset() + length;
//					int lineOfOffset = doc.getLineOfOffset(fullOffset);
//					int finalOffset = (fullOffset
//							- (doc.getLineOffset(lineOfOffset)) + 1);
					int startOffset = textSelection.getOffset() - doc.getLineOffset(startLine-1) + 1;
					msg = startLine + " : " + /*finalOffset*/startOffset + " : " + length;
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
				getEditorSite().getActionBars().getStatusLineManager()
						.setMessage(msg);
			}
			PlSqlEditor.this.caretMoved();
		}

		/**
		 * Removes this selection changed listener from the given selection
		 * provider.
		 * 
		 * @param selectionProvider
		 *            the selection provider
		 *
		public void uninstall(ISelectionProvider selectionProvider)
		{
			if (selectionProvider == null) return;

			if (selectionProvider instanceof IPostSelectionProvider)
			{
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			}
			else
			{
				selectionProvider.removeSelectionChangedListener(this);
			}
		}*/
	}

	/**
	 * Tracks selection in the outline page in order to highlight methods
	 * declarations in source code.
	 */
	private class OutlineSelectionListener implements ISelectionChangedListener
	{
		public void selectionChanged(SelectionChangedEvent event)
		{
			if (syncToOutline) return;
			if (event == null) return;
			if (!(event.getSelection() instanceof IStructuredSelection))
				return;

			IStructuredSelection sel = (IStructuredSelection) event
					.getSelection();
			if (!(sel.getFirstElement() instanceof Segment)) return;

			Segment elem = (Segment) sel.getFirstElement();
			syncFromOutline = true;
			selectAndReveal(elem.getPosition().offset, elem.getName().length());
		}
	}

	/**
	 * This action implements smart home.
	 * 
	 * Instead of going to the start of a line it does the following:
	 * 
	 * - if smart home/end is enabled and the caret is after the line's first
	 * non-whitespace then the caret is moved directly before it; beginning of a
	 * comment ('#') counts as whitespace - if the caret is before the line's
	 * first non-whitespace, the caret is moved to the beginning of the line -
	 * if the caret is at the beginning of the line, see first case.
	 */
	private class SmartLineStartAction extends LineStartAction
	{
		/**
		 * @param textWidget
		 *            the editor's styled text widget
		 * @param doSelect
		 *            a boolean flag which tells if the text up to the beginning
		 *            of the line should be selected
		 */
		public SmartLineStartAction(StyledText textWidget, boolean doSelect)
		{
			super(textWidget, doSelect);
		}

		protected int getLineStartPosition(final IDocument document,
				final String line, final int length, final int offset)
		{
			int index = super.getLineStartPosition(document, line, length,
					offset);

			if (index < length - 1 && line.charAt(index) == '#')
			{
				index++;
				while (index < length
						&& Character.isWhitespace(line.charAt(index)))
					index++;
			}
			return index;
		}
	}

	public SourceFile getSourceFile()
	{
		return mySource;
	}

    public boolean isSyncingToOutline()
    {
        return syncToOutline;
    }
}
