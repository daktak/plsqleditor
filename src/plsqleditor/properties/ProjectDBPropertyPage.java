package plsqleditor.properties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.preferences.PreferenceConstants;

public class ProjectDBPropertyPage extends PropertyPage
{

	private static final String PATH_TITLE = "Path:";

	private static final String DEFAULT_PROPERTY_VALUE = "Unknown";

	private static final int TEXT_FIELD_WIDTH = 50;

	Map<String, Control> myPropertyTexts = new HashMap<String,Control>();

	Button myUseLocalDbSettingsButton;

	/**
	 * Constructor for DBProperties Page.
	 */
	public ProjectDBPropertyPage()
	{
		super();
		setDescription("Project Level Database Connectivity and DBA User Preference page");
	}

	private void addFirstSection(Composite parent)
	{
		Composite composite = createDefaultComposite(parent);

		// Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);

		// Path text field
		Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setText(((IResource) getElement()).getFullPath()
				.toString());
	}

	private void addSeparator(Composite parent)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		setupStuff(composite);
		return composite;
	}

	private void setupStuff(Composite parent)
	{
		// put the combo on
		Composite composite = createDefaultComposite(parent);

		// Label for path field
		Label useLocalLabel = new Label(composite, SWT.NONE);
		useLocalLabel.setText("Use Local DB Settings");
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		useLocalLabel.setLayoutData(gd);

		// Path text field
		myUseLocalDbSettingsButton = new Button(composite, SWT.CHECK);

		myUseLocalDbSettingsButton.addListener(SWT.Selection, new Listener()
		{

			public void handleEvent(Event event)
			{
				for (Iterator<Control> it = myPropertyTexts.values().iterator(); it
						.hasNext();)
				{
					Control control = (Control) it.next();
					control.setEnabled(myUseLocalDbSettingsButton
							.getSelection());
				}
			}
		});

		IResource resource = (IResource) getElement();
		try
		{
			String propertyValue = resource
					.getPersistentProperty(new QualifiedName("",
							PreferenceConstants.USE_LOCAL_DB_SETTINGS));
			myUseLocalDbSettingsButton.setSelection(Boolean.valueOf(
					propertyValue).booleanValue());
		}
		catch (CoreException e)
		{
			myUseLocalDbSettingsButton.setSelection(false);
		}
		addField(PreferenceConstants.P_SCHEMA_PACKAGE_DELIMITER, "Schema to Package Delimiter", parent);
		addField(PreferenceConstants.P_URL, "Ur&l", parent);
		addField(PreferenceConstants.P_USER, "Dba &User Name", parent);
		addField(PreferenceConstants.P_PASSWORD, "Dba User &Password", parent);
		addField(PreferenceConstants.P_INIT_CONNS, "&Initial Connections",
				parent);
		addField(PreferenceConstants.P_MAX_CONNS, "&Max Connections", parent);
		addField(PreferenceConstants.P_NUM_RESULT_SET_ROWS,
				"&Number of ResultSet Rows per Fetch", parent);
		addField(PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE,
				"Auto Commit Connections on Close/Shutdown", parent);
	}

	private void addField(String propertyName, String description,
			Composite parent)
	{
		Composite composite = createDefaultComposite(parent);

		// Label for owner field
		Label label = new Label(composite, SWT.NONE);
		label.setText(description);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		label.setLayoutData(gd);
		// next property text field
		Text propertyText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		propertyText.setLayoutData(gd);

		// Populate owner text field
		try
		{
			IResource resource = (IResource) getElement();
			String propertyValue = resource
					.getPersistentProperty(new QualifiedName("", propertyName));
			propertyText.setText((propertyValue != null) ? propertyValue
					: getPrefs().getString(propertyName));
		}
		catch (CoreException e)
		{
			propertyText.setText(DEFAULT_PROPERTY_VALUE);
		}
		propertyText.setEnabled(myUseLocalDbSettingsButton.getSelection());
		myPropertyTexts.put(propertyName, propertyText);
	}

	private IPreferenceStore getPrefs()
	{
		return PlsqleditorPlugin.getDefault().getPreferenceStore();
	}

	private Composite createDefaultComposite(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults()
	{
		// Populate the appropriate text field with their values
		for (Iterator<String> it = myPropertyTexts.keySet().iterator(); it.hasNext();)
		{
			String property = (String) it.next();
			Text text = (Text) myPropertyTexts.get(property);
			text.setText(getPrefs().getString(property));
		}
	}

	public boolean performOk()
	{
		// store the values of the text fields
		try
		{
			IResource resource = (IResource) getElement();
			boolean isUsingDbSettings = myUseLocalDbSettingsButton
					.getSelection();
			resource.setPersistentProperty(new QualifiedName("",
					PreferenceConstants.USE_LOCAL_DB_SETTINGS), String
					.valueOf(isUsingDbSettings));
			if (isUsingDbSettings)
			{
				for (Iterator<String> it = myPropertyTexts.keySet().iterator(); it
						.hasNext();)
				{
					String property = (String) it.next();
					Text text = (Text) myPropertyTexts.get(property);
					String value = text.getText();
					resource.setPersistentProperty(new QualifiedName("",
							property), value);
				}
			}
			DbUtility.reinit(resource.getProject());
		}
		catch (CoreException e)
		{
			return false;
		}
		return true;
	}

}