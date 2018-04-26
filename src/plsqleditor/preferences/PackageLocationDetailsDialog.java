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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.parsers.ParseType;
import plsqleditor.preferences.entities.PackageLocation;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 4/03/2005
 */
public class PackageLocationDetailsDialog extends TitleAreaDialog
{
    protected String myLocation          = "";  //$NON-NLS-1$
    protected String myParseType      = "";  //$NON-NLS-1$

    protected Text   myLocationField;
    protected Combo  myParseTypeField;

    protected Button myOkButton;

    /**
     * Constructs a new file extension dialog.
     * 
     * @param parentShell the myParent shell
     */
    public PackageLocationDetailsDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(PlSqlEditorMessages.getString("PackageLocationDetails.shellTitle")); //$NON-NLS-1$
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

        setTitle(PlSqlEditorMessages.getString("PackageLocationDetails.dialogTitle")); //$NON-NLS-1$
        setMessage(PlSqlEditorMessages.getString("PackageLocationDetails.packageLocationDetailsMessage")); //$NON-NLS-1$

        // begin the layout

        Label label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("PackageLocationDetails.location")); //$NON-NLS-1$

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        ModifyListener ml = new ModifyListener()
        {
            public void modifyText(ModifyEvent event)
            {
                if (event.widget == myLocationField)
                {
                    myLocation = myLocationField.getText().trim();
                }
                else if (event.widget == myParseTypeField)
                {
                    myParseType = myParseTypeField.getText().trim();
                }
                if (myOkButton != null)
                {
                    myOkButton.setEnabled(validateDataEntered());
                }
            }
        };

        myLocationField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        //myLocationField.setEnabled(true);
        myLocationField.addModifyListener(ml);
        myLocationField.setText(getLocation());

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myLocationField.setLayoutData(data);
        myLocationField.setFocus();

        label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("PackageLocationDetails.parseType")); //$NON-NLS-1$

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        myParseTypeField = new Combo(contents, SWT.READ_ONLY);
        myParseTypeField.addModifyListener(ml);
        myParseTypeField.setItems(ParseType.theNames);
        myParseTypeField.setText(getParseType());
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myParseTypeField.setLayoutData(data);

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
        if (myLocation.length() == 0)
        {
            setErrorMessage("Location is empty");
            return false;
        }
        if (myParseType.length() == 0)
        {
            setErrorMessage("Parsetype is empty");
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
    public String getLocation()
    {
        return myLocation;
    }

    /**
     * This method gets the name of the schema.
     * 
     * @return the name of the schema.
     */
    public String getParseType()
    {
        return myParseType;
    }

    /**
     * This method sets the location list string on the object.
     * 
     * @param location The comma separated list of locations where this schema may contain
     *            packages.
     */
    public void setLocation(String location)
    {
        myLocation = location;
    }

    /**
     * This method sets the parsetype of the package source.
     * 
     * @param parseType The parsetype of the source.
     */
    public void setParseType(String parseType)
    {
        myParseType = parseType;
    }

    public PackageLocation getPackageLocation()
    {
        return new PackageLocation(getLocation(),ParseType.getParseType(getParseType()));
    }
}
