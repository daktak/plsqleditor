package org.boomsticks.plsqleditor.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChangeSchemaForPackageDialog extends Dialog
{

    /**
     * The title of the dialog.
     */
    private String          title;

    /**
     * The message to display, or <code>null</code> if none.
     */
    private String          message;

    /**
     * The input value; the empty string by default.
     */
    private String          myPackageName = ""; //$NON-NLS-1$

    /**
     * The input validator, or <code>null</code> if none.
     */
    private IInputValidator validator;

    /**
     * Ok button widget.
     */
    private Button          okButton;

    /**
     * Package input text widget.
     */
    private Text            myPackageText;

    /**
     * Schema input text widget.
     */
    private Text            mySchemaText;

    /**
     * Error message label widget.
     */
    private Text            errorMessageText;

    private String          mySchemaName;

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
    public ChangeSchemaForPackageDialog(Shell parentShell,
                                        String dialogTitle,
                                        String dialogMessage,
                                        String packageName,
                                        IInputValidator validator)
    {
        super(parentShell);
        this.title = dialogTitle;
        message = dialogMessage;
        mySchemaName = "";
        myPackageName = packageName;
        this.validator = validator;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == IDialogConstants.OK_ID)
        {
            myPackageName = myPackageText.getText();
            mySchemaName = mySchemaText.getText();
        }
        else
        {
            myPackageName = null;
            mySchemaName = null;
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        if (title != null) shell.setText(title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the
        // ok
        // button
        mySchemaText.setFocus();
        if (mySchemaName != null)
        {
            mySchemaText.setText(mySchemaName);
            mySchemaText.selectAll();
        }
        if (myPackageName != null)
        {
            myPackageText.setText(myPackageName);
            myPackageText.selectAll();
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent)
    {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        // create message
        if (message != null)
        {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(message);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                    | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }

        Label label = new Label(composite, SWT.WRAP);
        label.setText("Schema");
        // GridData data = new GridData(GridData.GRAB_HORIZONTAL |
        // GridData.GRAB_VERTICAL
        // | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        mySchemaText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        mySchemaText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        mySchemaText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        label = new Label(composite, SWT.WRAP);
        label.setText("Package");
        data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
                | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        myPackageText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        myPackageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        myPackageText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }
        });

        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

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
     * Returns the text area.
     * 
     * @return the text area
     */
    protected Text getPackageText()
    {
        return myPackageText;
    }

    /**
     * Returns the text area.
     * 
     * @return the text area
     */
    protected Text getSchemaText()
    {
        return mySchemaText;
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
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getPackageName()
    {
        return myPackageName;
    }

    /**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getSchemaName()
    {
        return mySchemaName;
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
            errorMessage = validator.isValid(myPackageText.getText());
        }
        setErrorMessage(errorMessage);
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK
     * button is disabled.
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
