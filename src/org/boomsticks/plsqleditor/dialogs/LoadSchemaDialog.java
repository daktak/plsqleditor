package org.boomsticks.plsqleditor.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.GenerateHeaderAndLoadBothToDatabaseAction;
import plsqleditor.actions.GenerateHeaderAndLoadBothToDatabaseAction.GenerateHeaderAndLoadResult;
import plsqleditor.actions.LoadToDatabaseAction;

public class LoadSchemaDialog extends Dialog {
    class PackageDetail {
	boolean myExecute = true;
	String myName;
	IFile myFile;
	String myLocation;
	String myHeaderStatus;
	String myBodyStatus;
	Exception myException;

	public PackageDetail(boolean execute, String name, IFile file, String location, String headerStatus,
		String bodyStatus, Exception exception) {
	    super();
	    myExecute = execute;
	    myName = name;
	    myFile = file;
	    myLocation = location;
	    myHeaderStatus = headerStatus;
	    myBodyStatus = bodyStatus;
	    myException = exception;
	}

	public boolean isExecute() {
	    return myExecute;
	}

	public void setExecute(boolean execute) {
	    myExecute = execute;
	}

	public String getName() {
	    return myName;
	}

	public IFile getFile() {
	    return myFile;
	}

	public String getLocation() {
	    return myLocation;
	}

	public String getHeaderStatus() {
	    return myHeaderStatus;
	}

	public String getBodyStatus() {
	    return myBodyStatus;
	}

	public void setHeaderStatus(String headerStatus) {
	    myHeaderStatus = headerStatus;
	}

	public void setBodyStatus(String bodyStatus) {
	    myBodyStatus = bodyStatus;
	}

	public void setException(Exception exception) {
	    myException = exception;
	}

	public Exception getException() {
	    return myException;
	}
    }

    static class PackageDetailLabelProvider extends LabelProvider implements ITableLabelProvider {

	// Names of images used to represent checkboxes
	public static final String CHECKED_IMAGE = "tick";
	public static final String UNCHECKED_IMAGE = "cross";
	public static final String SUCCESS_IMAGE = "SUCCESS";
	public static final String FAILURE_IMAGE = "FAILURE";

	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 */
	private Image getImage(boolean isSelected) {
	    String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
	    return PlsqleditorPlugin.getDefault().getImageRegistry().get(key);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object element, int columnIndex) {
	    String result = "";
	    PackageDetail detail = (PackageDetail) element;
	    switch (columnIndex) {
	    case 0:
		// result = Boolean.toString(detail.isExecute());
		break;
	    case 1:
		result = detail.getName();
		break;
	    case 2:
		result = detail.getLocation();
		break;
	    case 3:
		result = detail.getHeaderStatus();
		break;
	    case 4:
		result = detail.getBodyStatus();
		break;
	    case 5:
		result = detail.getException() == null ? "None" : detail.getException().toString();
		break;
	    default:
		break;
	    }
	    return result;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
	    PackageDetail detail = (PackageDetail) element;
	    Image result = null;
	    switch (columnIndex) {
	    case 0:
		result = getImage(detail.isExecute());
		break;
	    case 3:
		result = PlsqleditorPlugin.getDefault().getImageRegistry().get(detail.getHeaderStatus());
		break;
	    case 4:
		result = PlsqleditorPlugin.getDefault().getImageRegistry().get(detail.getBodyStatus());
		break;
	    default:
		break;
	    }
	    return result;
	}
    }

    /**
     * This is a sorter for the PackageDetails table in the Load Schema dialog.
     */
    class PackageDetailComparator extends ViewerComparator {
	/**
	 * Constructor argument values that indicate to sort items by description, owner
	 * or percent complete.
	 */
	public static final int EXECUTE_COLUMN = 0;
	public final static int NAME_COLUMN = 1;
	public final static int LOCATION_COLUMN = 2;
	public final static int HEADER_COLUMN = 3;
	public final static int BODY_COLUMN = 4;
	public final static int EXCEPTION_COLUMN = 5;

	// Criteria that the instance uses
	private int criteria;

	/**
	 * Creates a resource sorter that will use the given sort criteria.
	 *
	 * @param criteria the sort criterion to use: one of <code>NAME</code> or
	 *                 <code>TYPE</code>
	 */
	public PackageDetailComparator(int criteria) {
	    super();
	    this.criteria = criteria;
	}

	/*
	 * (non-Javadoc) Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
	    PackageDetail task1 = (PackageDetail) o1;
	    PackageDetail task2 = (PackageDetail) o2;

	    switch (criteria) {
	    case EXECUTE_COLUMN:
		return Boolean.valueOf(task1.isExecute()).compareTo(Boolean.valueOf(task2.isExecute()));
	    case NAME_COLUMN:
		return task1.getName().compareTo(task2.getName());
	    case LOCATION_COLUMN:
		return task1.getLocation().compareTo(task2.getLocation());
	    case HEADER_COLUMN:
		return task1.getHeaderStatus().compareTo(task2.getHeaderStatus());
	    case BODY_COLUMN:
		return task1.getBodyStatus().compareTo(task2.getBodyStatus());
	    case EXCEPTION_COLUMN:
		return 0;
	    default:
		return 0;
	    }
	}

	/**
	 * Returns the sort criteria of this this sorter.
	 *
	 * @return the sort criterion
	 */
	public int getCriteria() {
	    return criteria;
	}
    }

    /**
     * Class that plays the role of the domain model in the LoadSchema dialog. This
     * will access the generated package list.
     */
    public class PackageDetailList {
	private final int COUNT = 10;
	private List<PackageDetail> packageDetails = new ArrayList<PackageDetail>(COUNT);
	private Set<IPackageDetailListViewer> changeListeners = new HashSet<IPackageDetailListViewer>();

	/**
	 * Constructor
	 */
	public PackageDetailList(HashMap<String, IFile> packagesMap) {
	    super();
	    for (Iterator<String> packageIterator = packagesMap.keySet().iterator(); packageIterator.hasNext();) {
		String packageName = packageIterator.next();
		IFile file = packagesMap.get(packageName);
		PackageDetail pd = new PackageDetail(true, packageName, file,
			file.getProjectRelativePath().toPortableString(), "Not Executed", "Not Executed", null);
		packageDetails.add(pd);
	    }
	}

	public void packageDetailChanged(PackageDetail pd) {
	    for (IPackageDetailListViewer viewer : changeListeners) {
		viewer.updatePackageDetail(pd);
	    }
	}

	/**
	 * @param viewer
	 */
	public void removeChangeListener(IPackageDetailListViewer viewer) {
	    changeListeners.remove(viewer);
	}

	/**
	 * @param viewer
	 */
	public void addChangeListener(IPackageDetailListViewer viewer) {
	    changeListeners.add(viewer);
	}

	public List<PackageDetail> getPackageDetails() {
	    return packageDetails;
	}
    }

    /**
     * InnerClass that acts as a proxy for the ExampleTaskList providing content for
     * the Table. It implements the ITaskListViewer interface since it must register
     * changeListeners with the ExampleTaskList
     */
    class PackageDetailContentProvider implements IStructuredContentProvider, IPackageDetailListViewer {
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	    if (newInput != null)
		((PackageDetailList) newInput).addChangeListener(this);
	    if (oldInput != null)
		((PackageDetailList) oldInput).removeChangeListener(this);
	}

	public void dispose() {
	    packageDetailList.removeChangeListener(this);
	}

	// Return the tasks as an array of Objects
	public Object[] getElements(Object parent) {
	    return packageDetailList.getPackageDetails().toArray();
	}

	@Override
	public void addPackageDetail(PackageDetail detail) {
	    tableViewer.add(detail);
	}

	@Override
	public void removePackageDetail(PackageDetail detail) {
	    tableViewer.remove(detail);
	}

	@Override
	public void updatePackageDetail(PackageDetail detail) {
	    tableViewer.update(detail, null);
	}
    }

    class PackageDetailCellModifier implements ICellModifier {
	private Viewer viewer;

	public PackageDetailCellModifier(Viewer viewer) {
	    this.viewer = viewer;
	}

	public boolean canModify(Object element, String property) {
	    return true;
	}

	public Object getValue(Object element, String property) {
	    PackageDetail p = (PackageDetail) element;
	    if (columnNames[0].equals(property))
		return Boolean.valueOf(p.isExecute());
	    else if (columnNames[1].equals(property))
		return p.getName();
	    else if (columnNames[2].equals(property))
		return p.getLocation();
	    else if (columnNames[3].equals(property))
		return p.getHeaderStatus();
	    else if (columnNames[4].equals(property))
		return p.getBodyStatus();
	    else if (columnNames[5].equals(property))
		return p.getException();
	    else
		return null;
	}

	public void modify(Object element, String property, Object value) {
	    if (element instanceof Item)
		element = ((Item) element).getData();

	    PackageDetail p = (PackageDetail) element;
	    if (columnNames[0].equals(property))
		p.setExecute(((Boolean) value).booleanValue());
	    else if (columnNames[1].equals(property))
		return;
	    else if (columnNames[2].equals(property))
		return;
	    else if (columnNames[3].equals(property))
		return; // p.setHeaderStatus((String)
			// value);
	    else if (columnNames[4].equals(property))
		return; // p.setBodyStatus((String)
			// value);
	    else if (columnNames[5].equals(property))
		return;

	    viewer.refresh();
	}
    }

    /**
     * The title of the dialog.
     */
    private String myTitle;

    private String myMessage;

    /**
     * Error message label widget.
     */
    private Text errorMessageText;

    /**
     * Ok button widget.
     */
    private Button okButton;

    private Button executeButton;

    /**
     * Cancel button widget.
     */
    private Button cancelButton;

    private boolean hasExecuted = false;

    private Table table;
    TableViewer tableViewer;

    // Create a ExampleTaskList and assign it to an instance variable
    PackageDetailList packageDetailList;
    // Set the table column property names
    private final String EXECUTE_COLUMN = "Execute";
    private final String NAME_COLUMN = "Package Name";
    private final String LOCATION_COLUMN = "Location";
    private final String HEADER_COLUMN = "Header Result";
    private final String BODY_COLUMN = "Body Result";
    private final String EXCEPTION_COLUMN = "Exception Message";

    // Set column names
    private String[] columnNames = new String[] { EXECUTE_COLUMN, NAME_COLUMN, LOCATION_COLUMN, HEADER_COLUMN,
	    BODY_COLUMN, EXCEPTION_COLUMN };

    private Label myMessageLabel;

    /**
     * Create the Table
     */
    private void createTable(Composite parent) {
	int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

	table = new Table(parent, style);

	GridData gridData = new GridData(GridData.FILL_BOTH);
	gridData.grabExcessVerticalSpace = true;
	gridData.horizontalSpan = 3;
	table.setLayoutData(gridData);

	table.setLinesVisible(true);
	table.setHeaderVisible(true);

	// 1st column with image/checkboxes - NOTE: The SWT.CENTER has no
	// effect!!
	TableColumn column = new TableColumn(table, SWT.CENTER, 0);
	column.setText(columnNames[0]);
	column.setWidth(50);
	// Add listener to column so tasks are sorted by execute (or not) when
	// clicked
	column.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
		tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.EXECUTE_COLUMN));
	    }
	});

	// 2nd column with name
	column = new TableColumn(table, SWT.LEFT, 1);
	column.setText(columnNames[1]);
	column.setWidth(100);
	// Add listener to column so tasks are sorted by name when
	// clicked
	column.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
		tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.NAME_COLUMN));
	    }
	});

	// 3rd column with location
	column = new TableColumn(table, SWT.LEFT, 2);
	column.setText(columnNames[2]);
	column.setWidth(200);
	// Add listener to column so tasks are sorted by location when
	// clicked
	column.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
		tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.LOCATION_COLUMN));
	    }
	});

	// 4th column with header status
	column = new TableColumn(table, SWT.CENTER, 3);
	column.setText(columnNames[3]);
	column.setWidth(90);
	column.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
		tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.HEADER_COLUMN));
	    }
	});

	// 5th column with body status
	column = new TableColumn(table, SWT.CENTER, 4);
	column.setText(columnNames[4]);
	column.setWidth(80);
	column.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
		tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.BODY_COLUMN));
	    }
	});
	// 6th column with exception
	column = new TableColumn(table, SWT.CENTER, 5);
	column.setText(columnNames[5]);
	column.setWidth(200);
    }

    void executeAndNotify() {
	if (!hasExecuted) {
	    boolean success = executeSchemaLoad();
	    hasExecuted = true;
	    executeButton.setEnabled(false);
	    if (success)
		myMessageLabel.setText("Completed Successfully");
	    else
		myMessageLabel.setText("Some errors were discovered");
	    tableViewer.setCellModifier(null);
	}
    }

    /**
     * Create the TableViewer
     */
    private void createTableViewer() {
	tableViewer = new TableViewer(table);
	tableViewer.setUseHashlookup(true);

	tableViewer.setColumnProperties(columnNames);

	// Create the cell editors
	CellEditor[] editors = new CellEditor[columnNames.length];

	// Column 1 : execute or not (Checkbox)
	editors[0] = new CheckboxCellEditor(table);

	// Column 2 : Package Name (Free text)
	TextCellEditor textEditor = new TextCellEditor(table);
	((Text) textEditor.getControl()).setTextLimit(60);
	editors[1] = textEditor;

	// Column 3 : Location (Free text)
	editors[2] = new TextCellEditor(table);

	// Column 4 : header status
	textEditor = new TextCellEditor(table);
	editors[3] = textEditor;

	// Column 5 : body status
	textEditor = new TextCellEditor(table);
	editors[4] = textEditor;

	// Column 6 : exception
	textEditor = new TextCellEditor(table);
	editors[5] = textEditor;

	// Assign the cell editors to the viewer
	tableViewer.setCellEditors(editors);

	tableViewer.setCellModifier(new PackageDetailCellModifier(tableViewer));

	// Set the cell modifier for the viewer
	// tableViewer.setCellModifier(new ExampleCellModifier(this));
	// Set the default sorter for the viewer
	tableViewer.setComparator(new PackageDetailComparator(PackageDetailComparator.NAME_COLUMN));
    }

    protected void createButtonsForButtonBar(Composite parent) {
	// create OK and Cancel buttons by default
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);

	okButton.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		executeAndNotify();
	    }
	});

	executeButton = new Button(parent, SWT.PUSH);
	executeButton.setText("Execute");
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	gridData.widthHint = 80;
	executeButton.setLayoutData(gridData);
	executeButton.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		executeAndNotify();
	    }
	});
	// Create and configure the "Cancel" button
	cancelButton = new Button(parent, SWT.PUSH | SWT.CENTER);
	cancelButton.setText("Cancel");
	gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	gridData.widthHint = 80;
	cancelButton.setLayoutData(gridData);

	cancelButton.addSelectionListener(new SelectionAdapter() {
	    // Remove the selection and refresh the view
	    public void widgetSelected(SelectionEvent e) {
		close();
	    }
	});
    }

    /**
     * This loads all the files identified in the schema. If there are any load
     * failures, this will return false.
     *
     * @return true if there are no load failures, false otherwise.
     */
    public boolean executeSchemaLoad() {
	// set the env variables to not tell of each loading
	System.setProperty(LoadToDatabaseAction.IGNORE_STANDARD_MESSAGES, "irrelevantValue");

	boolean success = true;
	// generate and load the header and body of each
	// only leave open failed files
	for (PackageDetail pkgDetail : packageDetailList.getPackageDetails()) {
	    String pkgName = pkgDetail.getName();
	    IFile fileToProcess = pkgDetail.getFile();
	    if (pkgDetail.isExecute()) {
		try {
		    GenerateHeaderAndLoadResult result = GenerateHeaderAndLoadBothToDatabaseAction
			    .generateHeaderAndLoadBothToDb(fileToProcess);
		    pkgDetail.setHeaderStatus(result.isHeaderSuccessful() ? "SUCCESS" : "FAILURE");
		    pkgDetail.setBodyStatus(result.isBodySuccessful() ? "SUCCESS" : "FAILURE");
		    pkgDetail.setException(result.getException());
		    packageDetailList.packageDetailChanged(pkgDetail);
		    boolean localSuccess = result.isBodySuccessful() & result.isHeaderSuccessful();
		    if (localSuccess) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorReference[] refs = page.getEditorReferences();
			List<IEditorReference> refList = new ArrayList<IEditorReference>();
			String pkgFilename = pkgDetail.getFile().getName();
			pkgFilename = pkgFilename.substring(0, pkgFilename.lastIndexOf("."));
			for (IEditorReference ref : refs) {
			    if (ref.getName().startsWith(pkgFilename))
				refList.add(ref);
			}
			page.closeEditors(refList.toArray(new IEditorReference[refList.size()]), false);
		    }
		    success &= localSuccess;
		} catch (Exception e) {
		    e.printStackTrace();
		    MessageDialog.openError(getShell(), "Failed to complete the generate and load task for " + pkgName,
			    e.toString());
		    success = false;
		}
	    }
	}

	// unset the env variables to not tell of each loading
	System.getProperties().remove(LoadToDatabaseAction.IGNORE_STANDARD_MESSAGES);

	return success;
    }

    /**
     * Return the column names in a collection
     *
     * @return List containing column names
     */
    public java.util.List<String> getColumnNames() {
	return Arrays.asList(columnNames);
    }

    /**
     * @return currently selected item
     */
    public ISelection getSelection() {
	return tableViewer.getSelection();
    }

    /**
     * Return the ExampleTaskList
     */
    public PackageDetailList getPackageDetailsList() {
	return packageDetailList;
    }

    /**
     * Return the parent composite
     */
    public Control getControl() {
	return table.getParent();
    }

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog will
     * have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     *
     * @param parentShell   the parent shell, or <code>null</code> to create a
     *                      top-level shell
     * @param dialogTitle   the dialog title, or <code>null</code> if none
     * @param dialogMessage the dialog message, or <code>null</code> if none
     * @param validator     an input validator, or <code>null</code> if none
     */
    public LoadSchemaDialog(Shell parentShell, String dialogTitle, String dialogMessage,
	    HashMap<String, IFile> packageMap) {
	super(parentShell);
	this.myTitle = dialogTitle;
	myMessage = dialogMessage;
	// The input for the table viewer is the instance of ExampleTaskList
	packageDetailList = new PackageDetailList(packageMap);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
	if (buttonId == IDialogConstants.OK_ID) {
	    // do nothing
	} else {
	    // do nothing
	}
	super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    protected void configureShell(Shell shell) {
	super.configureShell(shell);
	if (myTitle != null) {
	    shell.setText(myTitle);
	}
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
	// create composite
	Composite composite = (Composite) super.createDialogArea(parent);
	GridLayout gridLayout = new GridLayout(3, false);
	gridLayout.marginWidth = 5;
	gridLayout.marginHeight = 5;
	gridLayout.verticalSpacing = 0;
	gridLayout.horizontalSpacing = 0;
	composite.setLayout(gridLayout);
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
	composite.setLayoutData(gridData);

	// Set numColumns to 3 for the buttons
	GridLayout layout = new GridLayout(3, false);
	layout.marginWidth = 4;
	composite.setLayout(layout);

	// Create the table
	createTable(composite);

	// Create and setup the TableViewer
	createTableViewer();
	tableViewer.setContentProvider(new PackageDetailContentProvider());
	tableViewer.setLabelProvider(new PackageDetailLabelProvider());
	tableViewer.setInput(packageDetailList);

	// create message
	if (myMessage != null) {
	    myMessageLabel = new Label(composite, SWT.WRAP);
	    myMessageLabel.setText(myMessage);
	    GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
		    | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
	    data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
	    data.horizontalSpan = 3;
	    myMessageLabel.setLayoutData(data);
	    myMessageLabel.setFont(parent.getFont());
	}

	errorMessageText = new Text(composite, SWT.READ_ONLY);
	errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	errorMessageText.setEditable(false);

	applyDialogFont(composite);
	return composite;
    }

    /**
     * Returns the ok button.
     *
     * @return the ok button
     */
    protected Button getOkButton() {
	return okButton;
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button is
     * disabled.
     *
     * @param errorMessage the error message, or <code>null</code> to clear
     */
    public void setErrorMessage(String errorMessage) {
	errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
	okButton.setEnabled(errorMessage == null);
	errorMessageText.getParent().update();
    }
}
