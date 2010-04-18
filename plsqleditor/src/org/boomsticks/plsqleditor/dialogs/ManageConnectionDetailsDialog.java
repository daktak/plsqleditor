package org.boomsticks.plsqleditor.dialogs;

import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.ConnectionDetails;
import plsqleditor.db.ConnectionRegistry;
import plsqleditor.db.DbUtility;
import plsqleditor.db.LoadPackageManager;
import au.com.zinescom.util.UsefulOperations;

public class ManageConnectionDetailsDialog extends Dialog
{

    /**
     * The title of the dialog.
     */
    private String                    myTitle;

    private String                    myMessage;

    private IInputValidator           validator;

    /**
     * Connection Details input/modification text widget.
     */
    Text                              myConnectionDetailsNameText;

    /**
     * Connection Details input/modification text widget.
     */
    Text                              myConnectionUrlText;

    /**
     * Schema input text widget.
     */
    Text                              mySchemaNameText;

    /**
     * Schema input text widget.
     */
    Text                              myPasswordText;

    List                              myExistingConnectionDetails;

    boolean                           myIsAdding          = false;
    boolean                           myIsUpdating        = false;

    /**
     * Error message label widget.
     */
    private Text                      errorMessageText;

    /**
     * Update button widget.
     */
    Button                            myUpdateButton;

    /**
     * Commit button widget.
     */
    Button                            myCommitButton;

    /**
     * Add button widget.
     */
    Button                            myAddNewButton;

    /**
     * Test button widget.
     */
    private Button                    myTestButton;

    /**
     * Save button widget.
     */
    private Button                    mySaveButton;

    /**
     * Ok button widget.
     */
    private Button                    okButton;

    java.util.List<ConnectionDetails> myConnectionDetails = new ArrayList<ConnectionDetails>();

    Button                            myCancelCommitButton;

    Button                            myDeleteButton;

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
     * @param validator an input validator, or <code>null</code> if none
     */
    public ManageConnectionDetailsDialog(Shell parentShell,
                                         String dialogTitle,
                                         String dialogMessage,
                                         IInputValidator validator)
    {
        super(parentShell);
        this.myTitle = dialogTitle;
        myMessage = dialogMessage;
        this.validator = validator;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == IDialogConstants.OK_ID)
        {
            saveConnectionDetails();
        }
        else
        {
            // do nothing
        }
        super.buttonPressed(buttonId);
    }

    ConnectionRegistry getConnectionRegistry()
    {
        return PlsqleditorPlugin.getDefault().getConnectionRegistry();
    }

    void saveConnectionDetails()
    {
        getConnectionRegistry().saveConnectionDetails(true);
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
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will enable the ok button
        myConnectionDetailsNameText.setText("");
        myConnectionDetailsNameText.setFocus();
    }

    /**
     * This method loads new details, either because a new value has been
     * selected in the list, or because the add button has been pushed, and the
     * blank details will be filled in and loaded to the file system.
     * 
     * @param cd The connection details to load, or null if this is the result
     *            of an add request.
     */
    void loadNewDetails(ConnectionDetails cd)
    {
        if (cd != null)
        {
            myConnectionDetailsNameText.setText(cd.getName());
            myConnectionUrlText.setText(cd.getConnectString());
            mySchemaNameText.setText(cd.getSchemaName());
            myPasswordText.setText(cd.getPassword());
            myUpdateButton.setEnabled(true);
            myCommitButton.setEnabled(false);
            myDeleteButton.setEnabled(true);
        }
        else
        {
            myConnectionDetailsNameText.setText("");
            // myConnectionUrlText.setText("");
            // mySchemaNameText.setText("");
            // myPasswordText.setText("");
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
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        // create message
        if (myMessage == null)
        {
            myMessage = "";
        }
        Label label = new Label(composite, SWT.WRAP);
        label.setText(myMessage);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        // data.widthHint =
        // convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        // load the Existing Connection Details name
        new Label(composite, SWT.NONE).setText("Existing Connection Details");


        new Label(composite, SWT.NONE).setText("Name");
        myConnectionDetailsNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        GridData gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gd.horizontalSpan = 1;
        myConnectionDetailsNameText.setLayoutData(gd);
        myConnectionDetailsNameText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        ConnectionRegistry cr = getConnectionRegistry();
        myExistingConnectionDetails = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        setConnectionDetails(cr.getConnectionDetails());
        java.util.List<String> items = new ArrayList<String>();
        for (ConnectionDetails cd : myConnectionDetails)
        {
            items.add(cd.getName());
        }

        myExistingConnectionDetails.setItems(items.toArray(new String[items.size()]));
        gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.verticalSpan = 3;
        // int listHeight = myExistingConnectionDetails.getItemHeight() * 12;
        // Rectangle trim = myExistingConnectionDetails.computeTrim(0, 0, 0,
        // listHeight);
        // gd.heightHint = trim.height;
        myExistingConnectionDetails.setLayoutData(gd);

        myExistingConnectionDetails.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                String name = myExistingConnectionDetails.getSelection()[0];
                for (ConnectionDetails cd : myConnectionDetails)
                {
                    if (cd.getName().equals(name))
                    {
                        loadNewDetails(cd);
                        return;
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        new Label(composite, SWT.NONE).setText("URL");
        myConnectionUrlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gd.horizontalSpan = 1;
        myConnectionUrlText.setLayoutData(gd);
        myConnectionUrlText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        new Label(composite, SWT.NONE).setText("Schema");
        mySchemaNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gd.horizontalSpan = 1;
        mySchemaNameText.setLayoutData(gd);
        mySchemaNameText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        new Label(composite, SWT.NONE).setText("Password");
        myPasswordText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gd.horizontalSpan = 1;
        myPasswordText.setLayoutData(gd);
        myPasswordText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        setTextsEditable(false);

        // add Update Details button (turns on ability to change a connection
        // details
        myUpdateButton = new Button(composite, SWT.PUSH);
        myUpdateButton.setText("Update Current Connection");
        myUpdateButton.setEnabled(false);
        myUpdateButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                String name = myExistingConnectionDetails.getSelection()[0];
                if (checkConnection(name))
                {
                    myExistingConnectionDetails.setEnabled(false);
                    myCommitButton.setEnabled(true);
                    myCancelCommitButton.setEnabled(true);
                    myDeleteButton.setEnabled(false);
                    myUpdateButton.setEnabled(false);
                    setTextsEditable(true);
                    myIsAdding = false;
                    myIsUpdating = true;
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        // add Commit Changes button
        myCommitButton = new Button(composite, SWT.PUSH);
        myCommitButton.setText("Commit");
        myCommitButton.setEnabled(false);
        myCommitButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                try
                {
                    String name = myConnectionDetailsNameText.getText();
                    String url = myConnectionUrlText.getText();
                    String schemaName = mySchemaNameText.getText();
                    String password = myPasswordText.getText();

                    boolean isError = false;
                    StringBuffer errorMessage = new StringBuffer("Error: ");
                    if (name == null || name.length() == 0)
                    {
                        errorMessage.append("\nYou must specify a connection name");
                        isError = true;
                    }
                    if (url == null || url.length() == 0)
                    {
                        errorMessage.append("\nYou must specify a connection string (url)");
                        isError = true;
                    }
                    if (schemaName == null || schemaName.length() == 0)
                    {
                        errorMessage.append("\nYou must specify a schemaName (user)");
                        isError = true;
                    }
                    if (password == null || password.length() == 0)
                    {
                        errorMessage.append("\nYou must specify a password");
                        isError = true;
                    }
                    ConnectionRegistry cr = getConnectionRegistry();
                    if (isError)
                    {
                        MessageDialog.openError(getShell(), "Toby's PL SQL Editor", errorMessage
                                .toString());
                        return;
                    }
                    ConnectionDetails cd = null;
                    if (myIsAdding)
                    {
                        cd = cr.addConnectionDetails(name, url, schemaName, password);
                        myExistingConnectionDetails.add(name);
                    }
                    else if (myIsUpdating)
                    {
                        cd = cr.updateConnectionDetails(name, url, schemaName, password);
                    }
                    else
                    {
                        MessageDialog
                                .openError(getShell(),
                                           "Toby's PL SQL Editor",
                                           "Illegal State, you are neither adding nor updating a Conneciton Details");
                        return;
                    }

                    myCommitButton.setEnabled(false);
                    myCancelCommitButton.setEnabled(false);
                    myUpdateButton.setEnabled(true);
                    myAddNewButton.setEnabled(true);
                    myDeleteButton.setEnabled(true);
                    myConnectionDetails.add(cd);
                    setTextsEditable(false);
                    myExistingConnectionDetails.setEnabled(true);
                    myExistingConnectionDetails.setSelection(new String[]{name});
                    myIsAdding = false;
                    myIsUpdating = false;
                }
                catch (Exception e)
                {
                    MessageDialog.openError(getShell(), "Toby's PL SQL Editor", e.getMessage());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        // add Cancel Current Changes button
        myCancelCommitButton = new Button(composite, SWT.PUSH);
        myCancelCommitButton.setText("Cancel Current Changes");
        myCancelCommitButton.setEnabled(false);
        myCancelCommitButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                try
                {
                    myCommitButton.setEnabled(false);
                    myCancelCommitButton.setEnabled(false);
                    myUpdateButton.setEnabled(false);
                    myDeleteButton.setEnabled(false);
                    myAddNewButton.setEnabled(true);
                    setTextsEditable(false);
                    myExistingConnectionDetails.setEnabled(true);
                    myExistingConnectionDetails.deselectAll();
                    myIsAdding = false;
                    myIsUpdating = false;
                    loadNewDetails(null);
                }
                catch (Exception e)
                {
                    MessageDialog.openError(getShell(), "Toby's PL SQL Editor", e.getMessage());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        // add New Connection Details (add) button
        myAddNewButton = new Button(composite, SWT.PUSH);
        myAddNewButton.setText("Add new Connection");
        myAddNewButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                myExistingConnectionDetails.deselectAll();
                myCommitButton.setEnabled(true);
                myCancelCommitButton.setEnabled(true);
                myAddNewButton.setEnabled(false);
                myUpdateButton.setEnabled(false);
                myDeleteButton.setEnabled(false);
                myExistingConnectionDetails.setEnabled(false);
                myIsAdding = true;
                myIsUpdating = false;
                setTextsEditable(true);
                loadNewDetails(null);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        // add Delete button
        myDeleteButton = new Button(composite, SWT.PUSH);
        myDeleteButton.setText("Delete");
        myDeleteButton.setEnabled(false);
        myDeleteButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                String name = myExistingConnectionDetails.getSelection()[0];
                if (checkConnection(name))
                {
                    myExistingConnectionDetails.remove(name);
                    myConnectionDetails.remove(getConnectionDetailsIndex(name));
                    getConnectionRegistry().removeConnectionDetails(name);
                    myDeleteButton.setEnabled(false);
                    loadNewDetails(null);

                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // do nothing
            }
        });

        // add a Test Connection button
        myTestButton = new Button(composite, SWT.PUSH);
        myTestButton.setText("Test Displayed Connection");
        myTestButton.addSelectionListener(new SelectionListener()
        {
            
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                testSelectedConnection();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                testSelectedConnection();
            }
        });

        // add a Save button
        mySaveButton = new Button(composite, SWT.PUSH);
        mySaveButton.setText("Persist Changes");
        mySaveButton.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                saveConnectionDetails();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                saveConnectionDetails();
            }
        });

        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        errorMessageText.setEditable(false);

        applyDialogFont(composite);
        return composite;
    }

    protected void testSelectedConnection()
    {
        String name = myExistingConnectionDetails.getSelection()[0];
        ConnectionDetails cd = getConnectionDetails(name);
        try
        {
            DbUtility.testConnection(cd);
        }
        catch (SQLException e)
        {
            MessageDialog.openError(getShell(), "Toby's PL/SQL Editor", e.getMessage());
        }
    }

    protected int getConnectionDetailsIndex(String name)
    {
        for (int i = 0; i < myConnectionDetails.size(); i++)
        {
            ConnectionDetails cd = myConnectionDetails.get(i);
            if (cd.getName().equals(name))
            {
                return i;
            }
        }
        return -1;
    }

    protected ConnectionDetails getConnectionDetails(String name)
    {
        for (ConnectionDetails cd : myConnectionDetails)
        {
            if (cd.getName().equals(name))
            {
                return cd;
            }
        }
        return null;
    }
    /**
     * Returns <code>true</code> if it is ok to update or delete it (i.e. there
     * are no files using it) and <code>false</code> otherwise.
     * 
     * @param connectionDetailsName
     * @return
     */
    boolean checkConnection(String connectionDetailsName)
    {
        ConnectionDetails cd = myConnectionDetails.get(getConnectionDetailsIndex(connectionDetailsName));
        String[] filenames = LoadPackageManager.instance().getFilesForConnection(cd);
        if (filenames != null)
        {
            String msg = "The following files are still using the connection name "
                    + connectionDetailsName + ":\n"
                    + UsefulOperations.arrayToString(filenames, null, ",");
            MessageDialog.openError(getShell(), "Toby's PL/SQL Editor", msg);
            return false;
        }
        return true;
    }

    ConnectionDetails[] getConnectionDetails()
    {
        return myConnectionDetails.toArray(new ConnectionDetails[myConnectionDetails.size()]);
    }

    private void setConnectionDetails(ConnectionDetails[] details)
    {
        for (int i = 0; i < details.length; i++)
        {
            myConnectionDetails.add(details[i]);
        }
    }

    void setTextsEditable(boolean b)
    {
        myConnectionDetailsNameText.setEditable(b);
        myConnectionUrlText.setEditable(b);
        mySchemaNameText.setEditable(b);
        myPasswordText.setEditable(b);
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
     * Returns the text area.
     * 
     * @return the text area
     */
    protected Text getSchemaNameText()
    {
        return mySchemaNameText;
    }

    /**
     * Returns the validator.
     * 
     * @return the validator
     */
    protected IInputValidator getValidator()
    {
        return validator;
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput()
    {
        String errorMessage = null;
        if (validator != null)
        {
            errorMessage = validator.isValid(myConnectionUrlText.getText());
        }
        setErrorMessage(errorMessage);
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button
     * is disabled.
     * 
     * @param errorMessage the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage)
    {
        errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
        okButton.setEnabled(errorMessage == null);
        errorMessageText.getParent().update();
    }
}
