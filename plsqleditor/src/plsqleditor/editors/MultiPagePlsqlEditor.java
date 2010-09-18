package plsqleditor.editors;

import org.boomsticks.plsqleditor.dialogs.openconnections.IConnectionListViewer;
import org.boomsticks.plsqleditor.dialogs.openconnections.LiveConnection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
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
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.javawiki.swt.common.ImageCombo;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.ConnectionDetails;
import plsqleditor.db.ConnectionRegistry;
import plsqleditor.db.ConnectionRegistry.RegistryUpdateListener;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.preferences.PreferenceConstants;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * </ul>
 */
public class MultiPagePlsqlEditor extends MultiPageEditorPart implements
		IResourceChangeListener, RegistryUpdateListener, IConnectionListViewer, ITextEditor//, INavigationLocationProvider
{

	public static final String SCHEMA_DEFAULT = "Schema Default";

	/** The text editor used in page 0. */
	private PlSqlEditor editor;

	/**
	 * @return the editor
	 */
	public PlSqlEditor getEditor()
	{
		return editor;
	}

	private PlSqlContentOutlinePage myOutlinePage;

	private Text myCurrentSchemaText;
	private Text myCurrentDbConnectionValue;
	private Text myCurrentDbSchemaValue;
	/**
	 * The connection type - this can be "Schema based" or
	 * "Specific to this file".
	 */
	private Text myConnectionTypeValue;

	CLabel myConnectionDetailsLabel;
	ImageCombo myConnectionDetailsCombo;
	String myCurrentConnection;
	Color myDefaultLabelColour;

	private Text myParserText;

	private Text myFileTypeText;

	private Text myCurrentPackageText;

	Button mySetConnectionButton;

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPagePlsqlEditor()
	{
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter)
	{
		if (IContentOutlinePage.class.equals(adapter))
		{
			if (myOutlinePage == null)
			{
				myOutlinePage = (PlSqlContentOutlinePage) editor
						.getAdapter(adapter);
			}
			return myOutlinePage;
		}
		return editor.getAdapter(adapter);
	}

	private PlSqlContentOutlinePage getOutlinePage()
	{
		if (myOutlinePage == null)
		{
			myOutlinePage = (PlSqlContentOutlinePage) editor
					.getAdapter(IContentOutlinePage.class);
		}
		return myOutlinePage;
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
					"Error creating nested plsql editor", null, e.getStatus());
		}
	}

//	public IEditorSite getEditorSite()
//	{
//		if (editor != null)
//		{
//			return editor.getEditorSite();
//		}
//		return super.getEditorSite();
//	}
	/**
	 * Creates page 1 of the multipage editor, which allows you to see the
	 * following:
	 * <ul>
	 * <li>current schema</li>
	 * <li>current package name</li>
	 * <li>current DB connection (set to global with schema, project override
	 * with schema, or specific</li>
	 * </ul>
	 * <p>
	 * It also allows you to change the following:
	 * <ul>
	 * <li>The DB connection (to global with schema, project with schema or
	 * specific</li>
	 * <li>current schema for the package</li>
	 * <li>to create new database connections and to edit existing ones</li>
	 * </ul>
	 * <p>
	 * It also allows you to generate the pl doc for the file.
	 */
	void createPage1()
	{
		Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 1;

		// Location
		Group location = new Group(composite, SWT.NULL);
		location.setText("Location");
		layout = new GridLayout();
		layout.numColumns = 3;
		location.setLayout(layout);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, false);
		gd.horizontalSpan = 1;
		location.setLayoutData(gd);

		// current data
		new CLabel(location, SWT.NONE).setText("Current Schema:");

		myCurrentSchemaText = new Text(location, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gd.horizontalSpan = 1;
		myCurrentSchemaText.setLayoutData(gd);

		Button chgPackageButton = new Button(location, SWT.PUSH);
		gd = new GridData(GridData.CENTER, GridData.CENTER, true, false);
		gd.horizontalSpan = 1;
		chgPackageButton.setLayoutData(gd);
		chgPackageButton.setText("Change Package for Schema");

		chgPackageButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				// initiate ChangeSchemaForPackage action
				IAction action = getEditor().getAction(
						PlSqlEditor.PLSQLEDITOR_CHANGE_SCHEMA_ID);
				action.run();
			}
		});

		new CLabel(location, SWT.NONE).setText("Current Package:");
		myCurrentPackageText = new Text(location, SWT.SINGLE | SWT.BORDER);
		myCurrentPackageText.setText("TBD");
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gd.horizontalSpan = 2;
		myCurrentPackageText.setLayoutData(gd);

		new CLabel(location, SWT.NONE).setText("File Type");
		myFileTypeText = new Text(location, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gd.horizontalSpan = 2;
		myFileTypeText.setLayoutData(gd);

		new CLabel(location, SWT.NONE).setText("Parser");
		myParserText = new Text(location, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gd.horizontalSpan = 2;
		myParserText.setLayoutData(gd);

		setLocationDetailsEditable(false);

		// Database connectivity
		Group dbConnectivity = new Group(composite, SWT.NULL);
		dbConnectivity.setText("Database Connectivity");
		layout = new GridLayout();
		layout.numColumns = 2;
		dbConnectivity.setLayout(layout);
		gd = new GridData(GridData.FILL, GridData.FILL, true, false);
		gd.horizontalSpan = 1;
		dbConnectivity.setLayoutData(gd);

		// Url
		new CLabel(dbConnectivity, SWT.NONE).setText("DB Connection URL:");

		myCurrentDbConnectionValue = new Text(dbConnectivity, SWT.SINGLE
				| SWT.BORDER);
		myCurrentDbConnectionValue.setText("");
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		myCurrentDbConnectionValue.setLayoutData(gd);
		myCurrentDbConnectionValue.setEditable(false);

		// Schema
		new CLabel(dbConnectivity, SWT.NONE).setText("Current Db Schema:");

		myCurrentDbSchemaValue = new Text(dbConnectivity, SWT.SINGLE
				| SWT.BORDER);
		myCurrentDbSchemaValue.setText("");
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		myCurrentDbSchemaValue.setLayoutData(gd);
		myCurrentDbSchemaValue.setEditable(false);

		// Connection Type
		new CLabel(dbConnectivity, SWT.NONE).setText("DB Connection Type:");

		myConnectionTypeValue = new Text(dbConnectivity, SWT.SINGLE
				| SWT.BORDER);
		myConnectionTypeValue.setText("");
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		myConnectionTypeValue.setLayoutData(gd);
		myConnectionTypeValue.setEditable(false);

		// Change Connection
		myConnectionDetailsLabel = new CLabel(dbConnectivity, SWT.NONE);
		myConnectionDetailsLabel.setText("Change Connection:");
		myDefaultLabelColour = myConnectionDetailsLabel.getBackground();
		myCurrentConnection = SCHEMA_DEFAULT;

		myConnectionDetailsCombo = new ImageCombo(dbConnectivity, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		myConnectionDetailsCombo.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false));

		ConnectionDetails[] availableConnectionDetails = getConnectionRegistry()
				.getConnectionDetails();

		Image defaultConnection = null; // TODO get the non live image
		Image specificConnection = null; // TODO get the live image
		myConnectionDetailsCombo.add(SCHEMA_DEFAULT, defaultConnection);
		for (int i = 0; i < availableConnectionDetails.length; i++)
		{
			ConnectionDetails cd = availableConnectionDetails[i];
			myConnectionDetailsCombo.add(cd.getName(), specificConnection);
		}
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		myConnectionDetailsCombo.setLayoutData(gd);

		myConnectionDetailsCombo.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				int selectionIndex = myConnectionDetailsCombo
						.getSelectionIndex();
				String newSelection = myConnectionDetailsCombo.getItem(
						selectionIndex).getText();
				if (newSelection.equals(myCurrentConnection))
				{
					myConnectionDetailsLabel
							.setBackground(myDefaultLabelColour);
					mySetConnectionButton.setEnabled(false);
				}
				else
				{
					myConnectionDetailsLabel.setBackground(new ColorManager()
							.getColor(new RGB(255, 10, 10)));
					mySetConnectionButton.setEnabled(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0)
			{
				// do nothing
			}
		});

		getConnectionRegistry().addListener(this);

		String connectionForFile = SCHEMA_DEFAULT;
		// TODO in the future, if we remember specific connections, we could set
		// one here
		myConnectionDetailsCombo.setText(connectionForFile);

		// Manage Connection Details
		Button manageConnectionDetailsButton = new Button(dbConnectivity,
				SWT.PUSH);

		manageConnectionDetailsButton.setText("Manage Connection Details");

		// button pressing functionality
		manageConnectionDetailsButton
				.addSelectionListener(new SelectionListener()
				{
					@Override
					public void widgetSelected(SelectionEvent event)
					{
						// initiate ChangeSchemaForPackage action
						IAction action = getEditor()
								.getAction(
										PlSqlEditor.PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_ID);
						action.run();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent event)
					{
						// initiate ChangeSchemaForPackage action
						IAction action = getEditor()
								.getAction(
										PlSqlEditor.PLSQLEDITOR_MANAGE_CONNECTION_DETAILS_ID);
						action.run();
					}
				});
		// end button pressing functionality

		mySetConnectionButton = new Button(dbConnectivity, SWT.PUSH);
		mySetConnectionButton.setText("Set Selected Connection");

		// button pressing functionality
		mySetConnectionButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				processSelectedConnection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event)
			{
				processSelectedConnection();
			}
		});
		// end button pressing functionality

		mySetConnectionButton.setEnabled(false);

		ConnectionDetails fcd = LoadPackageManager.instance()
				.getFixedConnectionName(getFile());

		if (fcd == null)
		{
			setSchemaDefaultConnectionDetails();
		}
		else
		{
			setSpecificConnectionDetails(fcd.getName(), fcd.getConnectString(),
					fcd.getSchemaName());
			PlsqleditorPlugin.getDefault().getOpenConnectionList()
					.addChangeListener(this);
		}

		// pl doc
		Button pldocButton = new Button(composite, SWT.NONE);
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		pldocButton.setLayoutData(gd);
		pldocButton.setText("Create PL Doc");

		pldocButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				getEditor()
						.getAction(PlSqlEditor.PLSQLEDITOR_GENERATE_PLDOC_ID)
						.runWithEvent(new Event());
			}
		});

		int index = addPage(composite);
		setPageText(index, "Details");
	}

	/**
	 * 
	 * @param text
	 */
	void processSelectedConnection()
	{
		int selectedIndex = myConnectionDetailsCombo.getSelectionIndex();
		if (selectedIndex < 0)
		{
			MessageDialog.openError(getSite().getShell(),
					"Toby's PL SQL Editor",
					"You have not selected a connection to set");
		}
		else
		{
			String name = myConnectionDetailsCombo.getItem(selectedIndex)
					.getText();
			if (name.equals(SCHEMA_DEFAULT))
			{
				LoadPackageManager.instance().removeFixedConnection(getFile());
				setSchemaDefaultConnectionDetails();
				PlsqleditorPlugin.getDefault().getOpenConnectionList()
						.removeChangeListener(this);
			}
			else
			{
				ConnectionDetails details = getConnectionRegistry()
						.getConnectionDetailsByName(name);
				LoadPackageManager.instance().setFixedConnection(getFile(),
						details);
				PlsqleditorPlugin.getDefault().getOpenConnectionList()
						.addChangeListener(this);
				setSpecificConnectionDetails(details.getName(), details
						.getConnectString(), details.getSchemaName());
			}
			myConnectionDetailsLabel.setBackground(myDefaultLabelColour);
		}
	}

	private void setSpecificConnectionDetails(String name,
			String connectString, String schemaName)
	{
		myConnectionDetailsCombo.setText(name);
		myCurrentDbConnectionValue.setText(connectString);
		myCurrentDbSchemaValue.setText(schemaName);
		myConnectionTypeValue.setText("Specific for this file");
		myCurrentConnection = name;
		mySetConnectionButton.setEnabled(false);
	}

	private void setSchemaDefaultConnectionDetails()
	{
		myConnectionDetailsCombo.setText(SCHEMA_DEFAULT);
		myCurrentConnection = SCHEMA_DEFAULT;

		String defaultType = "Global based Schema Default";
		IProject project = getFile().getProject();

		IPreferenceStore prefs = PlsqleditorPlugin.getDefault()
				.getPreferenceStore();
		String url = "";
		IFile file = getFile();
		String user = PlsqleditorPlugin.getDefault().getPackageStore(file)
				.findSchemaNameForFile(file);
		if (user == null)
		{
			user = "";
		}

		try
		{
			String isUsingLocalSettings = project
					.getPersistentProperty(new QualifiedName("",
							PreferenceConstants.USE_LOCAL_DB_SETTINGS));
			if (Boolean.valueOf(isUsingLocalSettings).booleanValue())
			{
				defaultType = "Project based Schema Default";
				url = project.getPersistentProperty(new QualifiedName("",
						PreferenceConstants.P_URL));
			}
			else
			{
				url = prefs.getString(PreferenceConstants.P_URL);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		myCurrentDbConnectionValue.setText(url);
		myCurrentDbSchemaValue.setText(user);
		myConnectionTypeValue.setText(defaultType);
		mySetConnectionButton.setEnabled(false);
	}

	private void setLocationDetailsEditable(boolean b)
	{
		myCurrentSchemaText.setEditable(b);
		myCurrentPackageText.setEditable(b);
		myFileTypeText.setEditable(b);
		myParserText.setEditable(b);
	}

	/**
	 * This method gets the file that this editor is processing.
	 * 
	 * @return the file being processed by this editor.
	 */
	public IFile getFile()
	{
		return getEditor().getFile();
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
	 * This method overrides the setting of focus to the underlying editor
	 */
	public void setFocus()
	{
		super.setFocus();
		getEditor().setFocus();
		String currentSchema = null;
		try
		{
			currentSchema = PlsqleditorPlugin.getDefault().getCurrentSchema();
			myCurrentSchemaText.setText(currentSchema);
			// set package
			myCurrentPackageText.setText(PlSqlParserManager
					.getPackageName(getFile()));
			// set file type
			myFileTypeText.setText(PlSqlParserManager.getType(getFile())
					.toString());
			myParserText.setText("Regex Parser");

			// TODO db details
			// myCurrentDbConnectionValue.setText("");
			// myCurrentDbSchemaValue.setText("");
			// myConnectionTypeValue.setText("");
			// myConnectionDetailsCombo.select(0);
		}
		catch (Exception e)
		{
			myFileTypeText.setText("Unknown");
			myCurrentSchemaText.setText("No Known Schema");
			e.printStackTrace();
		}
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose()
	{
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		getConnectionRegistry().removeListener(this);
		// this call will cope if there is no change listener
		PlsqleditorPlugin.getDefault().getOpenConnectionList()
				.removeChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor)
	{
		getEditor(0).doSave(monitor);
		getOutlinePage().update();
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
		getOutlinePage().update();
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
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException
	{
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
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
     * 
     */
	protected void pageChange(int newPageIndex)
	{
		super.pageChange(newPageIndex);
		// do stuff specific to changing page
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
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++)
					{
						if (((FileEditorInput) getEditor().getEditorInput())
								.getFile().getProject().equals(
										event.getResource()))
						{
							IEditorPart editorPart = pages[i]
									.findEditor(getEditor().getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	@Override
	public void registryUpdated()
	{
		ConnectionDetails[] cds = getConnectionRegistry()
				.getConnectionDetails();
		String currentText = myConnectionDetailsCombo.getText();
		myConnectionDetailsCombo.removeAll();

		String[] items = new String[cds.length + 1];
		items[0] = SCHEMA_DEFAULT;
		int i = 1;
		for (ConnectionDetails cd : cds)
		{
			items[i++] = cd.getName();
		}
		myConnectionDetailsCombo.setItems(items);
		myConnectionDetailsCombo.setText(currentText);
	}

	ConnectionRegistry getConnectionRegistry()
	{
		return PlsqleditorPlugin.getDefault().getConnectionRegistry();
	}

	@Override
	public void addConnection(LiveConnection task)
	{
		// do nothing
	}

	@Override
	public void removeConnection(LiveConnection task)
	{
		String type = task.getType();
		if (type.equals(LiveConnection.FILE_CONNECTION))
		{
			String filename = task.getFilename();
			if (filename.matches(".*?" + getFile().getName() + "$"))
			{
				setSchemaDefaultConnectionDetails();
			}
		}
	}

	@Override
	public void updateConnection(LiveConnection task)
	{
		String type = task.getType();
		if (type.equals(LiveConnection.FILE_CONNECTION))
		{
			String filename = task.getFilename();
			if (filename.matches(".*?" + getFile().getName() + "$"))
			{
				task.getFilename();
				setSpecificConnectionDetails(
						myConnectionDetailsCombo.getText(), task.getUrl(), task
								.getUser());
			}
		}
	}

	@Override
	public IDocumentProvider getDocumentProvider()
	{
		if (editor != null)
		{
			return editor.getDocumentProvider();
		}
		return null;
	}

	@Override
	public void close(boolean save)
	{
		if (editor != null)
		{
			editor.close(save);
			}
	}

	@Override
	public boolean isEditable()
	{
		if (editor != null)
		{
			return editor.isEditable();
		}
		return false;
	}

	@Override
	public void doRevertToSaved()
	{
		if (editor != null)
		{
			editor.doRevertToSaved();
		}
	}

	@Override
	public void setAction(String actionID, IAction action)
	{
		if (editor != null)
		{
			editor.setAction(actionID, action);
		}
	}

	@Override
	public IAction getAction(String actionId)
	{
		if (editor != null)
		{
			return editor.getAction(actionId);
		}
		return null;
	}

	@Override
	public void setActionActivationCode(String actionId,
			char activationCharacter, int activationKeyCode,
			int activationStateMask)
	{
		if (editor != null)
		{
			editor.setActionActivationCode(actionId, activationCharacter, activationKeyCode, activationStateMask);
		}
	}

	@Override
	public void removeActionActivationCode(String actionId)
	{
		if (editor != null)
		{
			editor.removeActionActivationCode(actionId);
		}
	}

	@Override
	public boolean showsHighlightRangeOnly()
	{
		if (editor != null)
		{
			return editor.showsHighlightRangeOnly();
		}
		return false;
	}

	@Override
	public void showHighlightRangeOnly(boolean showHighlightRangeOnly)
	{
		if (editor != null)
		{
			editor.showHighlightRangeOnly(showHighlightRangeOnly);
		}
	}

	@Override
	public void setHighlightRange(int offset, int length, boolean moveCursor)
	{
		if (editor != null)
		{
			editor.setHighlightRange(offset, length, moveCursor);
		}
	}

	@Override
	public IRegion getHighlightRange()
	{
		if (editor != null)
		{
			return editor.getHighlightRange();
		}
		return null;
	}

	@Override
	public void resetHighlightRange()
	{
		if (editor != null)
		{
			editor.resetHighlightRange();
		}
	}

	@Override
	public ISelectionProvider getSelectionProvider()
	{
		if (editor != null)
		{
			return editor.getSelectionProvider();
		}
		return null;
	}

	@Override
	public void selectAndReveal(int offset, int length)
	{
		if (editor != null)
		{
			editor.selectAndReveal(offset, length);
		}
	}
}
