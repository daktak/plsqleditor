package org.boomsticks.plsqleditor.dialogs;

import java.util.Arrays;

import org.boomsticks.plsqleditor.dialogs.openconnections.IConnectionListViewer;
import org.boomsticks.plsqleditor.dialogs.openconnections.LiveConnection;
import org.boomsticks.plsqleditor.dialogs.openconnections.LiveConnectionLabelProvider;
import org.boomsticks.plsqleditor.dialogs.openconnections.LiveConnectionSorter;
import org.boomsticks.plsqleditor.dialogs.openconnections.OpenConnectionList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class ManageOpenConnectionsDialog extends Dialog
{

    /**
     * The title of the dialog.
     */
    private String          myTitle;

    private String          myMessage;

    /**
     * Error message label widget.
     */
    private Text            errorMessageText;

    /**
     * Update button widget.
     */
    private Button          myDisconnectButton;

    /**
     * Ok button widget.
     */
    private Button          okButton;

    private Table           table;
    TableViewer             tableViewer;
    // Create a ExampleTaskList and assign it to an instance variable
    OpenConnectionList      liveConnectionList;
    // Set the table column property names
    private final String    DEFAULT_SCHEMA_COLUMN = "IsDefault";
    private final String    URL_COLUMN            = "Url";
    private final String    FILE_NAME_COLUMN      = "Filename";
    private final String    PROJECT_COLUMN        = "Project";
    private final String    USER_COLUMN           = "User";

    // Set column names
    private String[]        columnNames           = new String[]{DEFAULT_SCHEMA_COLUMN, URL_COLUMN,
    		FILE_NAME_COLUMN, PROJECT_COLUMN, USER_COLUMN};

    /**
     * InnerClass that acts as a proxy for the ExampleTaskList providing content
     * for the Table. It implements the ITaskListViewer interface since it must
     * register changeListeners with the ExampleTaskList
     */
    class LiveConnectionContentProvider
            implements
                IStructuredContentProvider,
                IConnectionListViewer
    {
        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
            if (newInput != null) ((OpenConnectionList) newInput).addChangeListener(this);
            if (oldInput != null) ((OpenConnectionList) oldInput).removeChangeListener(this);
        }

        public void dispose()
        {
            liveConnectionList.removeChangeListener(this);
        }

        // Return the tasks as an array of Objects
        public Object[] getElements(Object parent)
        {
            return liveConnectionList.getConnections().toArray();
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#addTask(ExampleTask)
         */
        public void addConnection(LiveConnection task)
        {
            tableViewer.add(task);
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#removeTask(ExampleTask)
         */
        public void removeConnection(LiveConnection task)
        {
            tableViewer.remove(task);
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#updateTask(ExampleTask)
         */
        public void updateConnection(LiveConnection task)
        {
            tableViewer.update(task, null);
        }
    }

    /**
     * Create the Table
     */
    private void createTable(Composite parent)
    {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
                | SWT.HIDE_SELECTION;

        table = new Table(parent, style);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 4;
        table.setLayoutData(gridData);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        // 1st column with image/checkboxes - NOTE: The SWT.CENTER has no
        // effect!!
        TableColumn column = new TableColumn(table, SWT.CENTER, 0);
        column.setText("!");
        column.setWidth(20);

        // 2nd column with Connection Url
        column = new TableColumn(table, SWT.LEFT, 1);
        column.setText("Connection Url");
        column.setWidth(400);
        // Add listener to column so tasks are sorted by url when
        // clicked
        column.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setComparator(new LiveConnectionSorter(LiveConnectionSorter.URL));
            }
        });

        // 3rd column with Project
        column = new TableColumn(table, SWT.LEFT, 2);
        column.setText("File Associated");
        column.setWidth(100);
        column.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setComparator(new LiveConnectionSorter(LiveConnectionSorter.LAST_FILE));
            }
        });

        // 4th column with file associated
        column = new TableColumn(table, SWT.CENTER, 3);
        column.setText("Project");
        column.setWidth(80);
        column.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setComparator(new LiveConnectionSorter(LiveConnectionSorter.PROJECT));
            }
        });

        // 5th column with file associated
        column = new TableColumn(table, SWT.CENTER, 4);
        column.setText("User");
        column.setWidth(80);
        column.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                tableViewer.setComparator(new LiveConnectionSorter(LiveConnectionSorter.USER));
            }
        });
    }

    /**
     * Create the TableViewer
     */
    private void createTableViewer()
    {
        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);

        tableViewer.setColumnProperties(columnNames);

        // Create the cell editors
        CellEditor[] editors = new CellEditor[columnNames.length];

        // Column 1 : Default or Schemabased (Checkbox)
        editors[0] = new CheckboxCellEditor(table);

        // Column 2 : Url (Free text)
        TextCellEditor textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).setTextLimit(60);
        editors[1] = textEditor;

        // Column 3 : Uptime
        editors[2] = new TextCellEditor(table);
        // new ComboBoxCellEditor(table, liveConnectionList.getOwners(),
        // SWT.READ_ONLY);

        // Column 4 : last file associated
        textEditor = new TextCellEditor(table);
        editors[3] = textEditor;

        // Column 5 : user/schema associated
        textEditor = new TextCellEditor(table);
        editors[4] = textEditor;

        // Assign the cell editors to the viewer
        tableViewer.setCellEditors(editors);
        // Set the cell modifier for the viewer
        // tableViewer.setCellModifier(new ExampleCellModifier(this));
        // Set the default sorter for the viewer
        tableViewer.setComparator(new LiveConnectionSorter(LiveConnectionSorter.LAST_FILE));
    }

    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        // Create and configure the "Delete" button
        myDisconnectButton = new Button(parent, SWT.PUSH | SWT.CENTER);
        myDisconnectButton.setText("Delete");
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.widthHint = 80;
        myDisconnectButton.setLayoutData(gridData);

        myDisconnectButton.addSelectionListener(new SelectionAdapter()
        {
            // Remove the selection and refresh the view
            public void widgetSelected(SelectionEvent e)
            {
                LiveConnection conn = (LiveConnection) ((IStructuredSelection) tableViewer
                        .getSelection()).getFirstElement();
                if (conn != null)
                {
                    liveConnectionList.removeConnection(conn);
                }
            }
        });
    }

    /**
     * Return the column names in a collection
     *
     * @return List containing column names
     */
    public java.util.List<String> getColumnNames()
    {
        return Arrays.asList(columnNames);
    }

    /**
     * @return currently selected item
     */
    public ISelection getSelection()
    {
        return tableViewer.getSelection();
    }

    /**
     * Return the parent composite
     */
    public Control getControl()
    {
        return table.getParent();
    }

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog
     * will have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     *
     * @param parentShell the parent shell, or <code>null</code> to create a
     *            top-level shell
     * @param dialogTitle the dialog title, or <code>null</code> if none
     * @param dialogMessage the dialog message, or <code>null</code> if none
     */
    public ManageOpenConnectionsDialog(Shell parentShell,
                                       String dialogTitle,
                                       String dialogMessage,
                                       OpenConnectionList openConnectionlist)
    {
        super(parentShell);
        this.myTitle = dialogTitle;
        myMessage = dialogMessage;
        liveConnectionList = openConnectionlist;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == IDialogConstants.OK_ID)
        {
            // do nothing
        }
        else
        {
            // do nothing
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        if (myTitle != null)
        {
            shell.setText(myTitle);
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent)
    {
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
        tableViewer.setContentProvider(new LiveConnectionContentProvider());
        tableViewer.setLabelProvider(new LiveConnectionLabelProvider());
        // The input for the table viewer is the liveConnectionList
        tableViewer.setInput(liveConnectionList);

        // create message
        if (myMessage != null)
        {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(myMessage);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                    | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            data.horizontalSpan = 3;
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }


        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        errorMessageText.setEditable(false);

        applyDialogFont(composite);
        return composite;
    }

    /**
     * Returns the ok button.
     *
     * @return the ok button
     */
    protected Button getOkButton()
    {
        return okButton;
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button
     * is disabled.
     *
     * @param errorMessage the error message, or <code>null</code> to clear
     */
    public void setErrorMessage(String errorMessage)
    {
        errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
        okButton.setEnabled(errorMessage == null);
        errorMessageText.getParent().update();
    }
}
