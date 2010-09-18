package plsqleditor.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.stores.PackageStore;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (pkb, pkh, pkg, sql).
 */

public class NewPackageBodyPage extends WizardPage
{
	private Text containerText;

	private Text fileText;

	private Combo fileType;

	private ISelection selection;

	/**
	 * Constructor for NewPackageBodyPage Wizard.
	 * 
	 * @param pageName
	 */
	public NewPackageBodyPage(ISelection selection)
	{
		super("wizardPage");
		setTitle("PLSQL Editor File");
		setDescription("This wizard creates a new file with *.xxx (pkb, pkh, pkg, sql etc) extension that can be opened by a plsql editor.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 4;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				updateFilename();
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				handleBrowse();
			}
		});
		// this is a dummy spot for the 4th column of row 1
		label = new Label(container, SWT.NULL);

		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("File &type:");
		fileType = new Combo(container, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileType.setLayoutData(gd);
		fileType.setItems(new String[] { "pkb", "pkg", "pkh", "sql",
				"other (add extension to file name)" });
		fileType.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				dialogChanged();
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize()
	{
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource)
			{
				IContainer container;
				if (obj instanceof IContainer) container = (IContainer) obj;
				else container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		String schemaPackageDelimiter = PackageStore.getSchemaPackageDelimiter(PlsqleditorPlugin.getDefault().getProject());
		String schemaName = getCurrentlyDefinedSchemaName();
		fileText.setText(schemaName + schemaPackageDelimiter + "pkgNameWithoutExtension");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse()
	{
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK)
		{
			Object[] result = dialog.getResult();
			if (result.length == 1)
			{
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void updateFilename()
	{
		String schemaName = getCurrentlyDefinedSchemaName();
		String currentFilename = fileText.getText();
		String schemaPackageDelimiter = PackageStore.getSchemaPackageDelimiter(PlsqleditorPlugin.getDefault().getProject());
		String cutFilename = "";
		if (currentFilename != null && currentFilename.length() > 0)
		{
			cutFilename = currentFilename.substring(currentFilename.indexOf(schemaPackageDelimiter));
		}
		fileText.setText(schemaName + cutFilename);
	}

	private String getCurrentlyDefinedSchemaName()
	{
		String containerName = getContainerName();
		String schemaName = containerName.substring(containerName.lastIndexOf("/") + 1);
		return schemaName;
	}
	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged()
	{
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0)
		{
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0)
		{
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible())
		{
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() != 0)
		{
			IResource existingFile = ResourcesPlugin.getWorkspace().getRoot()
			.findMember(new Path(getContainerName() + "/" + fileName));
			if (existingFile != null && existingFile.exists())
			{
				updateStatus("File already exists");
				return;
			}
		}
		updateStatus(null);
	}

	private void updateStatus(String message)
	{
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName()
	{
		return containerText.getText();
	}

	public String getFileName()
	{
		return fileText.getText() + "." + fileType.getText();
	}
}