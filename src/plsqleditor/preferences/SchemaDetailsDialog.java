/**
 * 
 */
package plsqleditor.preferences;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import plsqleditor.editors.PlSqlEditorMessages;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 4/03/2005
 */
public class SchemaDetailsDialog extends TitleAreaDialog
{
    protected String myName          = "";  //$NON-NLS-1$
    protected String myPassword      = "";  //$NON-NLS-1$
    // This field represents the locations for this schema in a comma separated list.
    protected String myLocations     = "";  //$NON-NLS-1$

    protected Text   mySchemaNameField;
    protected Text   mySchemaLocationField;
    protected Text   myPasswordField;

    protected Button myOkButton;
    private boolean  myIsNameEnabled = true;

    /**
     * Constructs a new file extension dialog.
     * 
     * @param parentShell the myParent shell
     */
    public SchemaDetailsDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(PlSqlEditorMessages.getString("SchemaDetails.shellTitle")); //$NON-NLS-1$
        //$NON-NLS-1$
        // PlatformUI.getWorkbench().getHelpSystem()
        // .setHelp(shell, IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG);
    }

    /**
     * Creates and returns the contents of the upper part of the dialog (above the button bar).
     * 
     * Subclasses should overide.
     * 
     * @param parent the myParent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent)
    {
        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        // create a composite with standard margins and spacing
        Composite contents = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        contents.setLayout(layout);
        contents.setLayoutData(new GridData(GridData.FILL_BOTH));
        contents.setFont(parentComposite.getFont());

        setTitle(PlSqlEditorMessages.getString("SchemaDetails.dialogTitle")); //$NON-NLS-1$
        setMessage(PlSqlEditorMessages.getString("SchemaDetails.schemaDetailsMessage")); //$NON-NLS-1$

        // begin the layout

        Label label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("SchemaDetails.schemaName")); //$NON-NLS-1$

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        ModifyListener ml = new ModifyListener()
        {
            public void modifyText(ModifyEvent event)
            {
                if (event.widget == mySchemaNameField)
                {
                    myName = mySchemaNameField.getText().trim();
                }
                else if (event.widget == mySchemaLocationField)
                {
                    myLocations = mySchemaLocationField.getText().trim();
                }
                else if (event.widget == myPasswordField)
                {
                    myPassword = myPasswordField.getText().trim();
                }
                if (myOkButton != null)
                {
                    myOkButton.setEnabled(validateDataEntered());
                }
            }
        };

        mySchemaNameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        mySchemaNameField.setEnabled(myIsNameEnabled);
        mySchemaNameField.addModifyListener(ml);
        mySchemaNameField.setText(getName());

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        mySchemaNameField.setLayoutData(data);
        mySchemaNameField.setFocus();

        label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("SchemaDetails.schemaLocation")); //$NON-NLS-1$

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        mySchemaLocationField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        mySchemaLocationField.addModifyListener(ml);
        mySchemaLocationField.setText(getLocations());
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        mySchemaLocationField.setLayoutData(data);

        label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("SchemaDetails.password")); //$NON-NLS-1$

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        myPasswordField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        myPasswordField.addModifyListener(ml);
        myPasswordField.setText(getPassword());

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myPasswordField.setLayoutData(data);

        Dialog.applyDialogFont(parentComposite);

        return contents;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        myOkButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        myOkButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * This method determines whether the data in the schema details is valid.
     */
    boolean validateDataEntered()
    {
        if (myName.length() == 0)
        {
            setErrorMessage("Schema Name is empty");
            return false;
        }
        if (myPassword.length() == 0)
        {
            setErrorMessage("Password is empty");
            return false;
        }
        if (myPassword.length() == 0)
        {
            setErrorMessage("Schema Location is empty");
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    /**
     * This method gets the location list string from the object.
     * 
     * @return The comma separated list of locations where this schema may contain packages.
     */
    public String getLocations()
    {
        return myLocations;
    }

    /**
     * This method gets the name of the schema.
     * 
     * @return the name of the schema.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Get the password.
     * 
     * @return the password
     */
    public String getPassword()
    {
        return myPassword;
    }

    /**
     * This method sets the location list string on the object.
     * 
     * @param locations The comma separated list of locations where this schema may contain
     *            packages.
     */
    public void setLocations(String locations)
    {
        myLocations = locations;
    }

    /**
     * This method sets the name of the schema.
     * 
     * @param name The name of the schema.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * This method sets the password for the schema for oracle logon purposes.
     * 
     * @param password The schema's password.
     */
    public void setPassword(String password)
    {
        myPassword = password;
    }

    /**
     * This method sets whether the name field should be enabled for this dialog.
     * 
     * @param isEnabled
     */
    public void setNameEnabled(boolean isEnabled)
    {
        myIsNameEnabled = isEnabled;
    }
}
