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
import plsqleditor.preferences.entities.PackageDetails;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 4/03/2005
 */
public class PackageDetailsDialog extends TitleAreaDialog
{
    protected String myPackageName = "";    //$NON-NLS-1$
    protected String myLocation = "";    //$NON-NLS-1$

    protected Text   myPackageNameField;
    protected Text   myLocationField;

    protected Button myOkButton;

    /**
     * Constructs a new file extension dialog.
     * 
     * @param parentShell the parent shell
     */
    public PackageDetailsDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(PlSqlEditorMessages.getString("PackageDetails.shellTitle")); //$NON-NLS-1$
        //$NON-NLS-1$
        // PlatformUI.getWorkbench().getHelpSystem()
        // .setHelp(shell, IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG);
    }

    /**
     * Creates and returns the contents of the upper part of the dialog (above the button bar).
     * 
     * Subclasses should overide.
     * 
     * @param parent the parent composite to contain the dialog area
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

        setTitle(PlSqlEditorMessages.getString("PackageDetails.dialogTitle")); //$NON-NLS-1$
        setMessage(PlSqlEditorMessages.getString("PackageDetails.PackageDetailsMessage")); //$NON-NLS-1$

        // begin the layout

        Label label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("PackageDetails.packageName")); //$NON-NLS-1$

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        ModifyListener ml = new ModifyListener()
        {
            public void modifyText(ModifyEvent event)
            {
                if (event.widget == myPackageNameField)
                {
                    myPackageName = myPackageNameField.getText().trim();
                }
                else if (event.widget == myLocationField)
                {
                    myLocation = myLocationField.getText().trim();
                }
                myOkButton.setEnabled(validateDataEntered());
            }
        };
        
        myPackageNameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        myPackageNameField.addModifyListener(ml);

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myPackageNameField.setLayoutData(data);
        myPackageNameField.setFocus();

        label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("PackageDetails.packageLocation")); //$NON-NLS-1$

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        myLocationField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        myLocationField.addModifyListener(ml);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myLocationField.setLayoutData(data);

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
     * Validate the user input for a file type
     */
    boolean validateDataEntered()
    {
        // We need kernel api to validate the extension or a myName

        // check for empty name and extension
        if (myPackageName.length() == 0)
        {
            setErrorMessage("Package Name is empty");
            return false;
        }
        if (myLocation.length() == 0)
        {
            setErrorMessage("Location is empty");
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    /**
     * Get the password.
     * 
     * @return the password
     */
    public PackageDetails getPackageDetails()
    {
        return new PackageDetails(myPackageName, myLocation);
    }
}
