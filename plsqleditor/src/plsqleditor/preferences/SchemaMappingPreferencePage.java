package plsqleditor.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.misc.Assert;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.preferences.entities.PackageDetails;
import plsqleditor.preferences.entities.SchemaDetails;
import plsqleditor.stores.SchemaRegistry;

public class SchemaMappingPreferencePage extends PreferencePage
        implements
            IWorkbenchPreferencePage,
            Listener
{
    protected Table                      mySchemasTable;

    protected Button                     myAddSchemaDetailsButton;

    protected Button                     myRemoveSchemaDetailsButton;

    protected Table                      myPackageTable;

    protected Button                     myAddPackageButton;

    protected Button                     myRemovePackageButton;

    protected Label                      myPackageLabel;

    protected IWorkbench                 myWorkbench;

    protected List<Image>                myImagesToDispose;

    protected Map<PackageDetails, Image> myPackagesToImages;

    /**
     * Add a new resource type to the collection shown in the top of the page. This is typically
     * called after the extension dialog is shown to the user.
     * 
     * @param newName the new name
     * @param newLocation the new extension
     */
    public void addSchemaDetails(String newName, String newLocation, String newPassword)
    {
        // Either a file name or extension must be provided
        Assert.isTrue((newName != null && newName.length() != 0)
                || (newLocation != null && newLocation.length() != 0)
                || (newPassword != null && newPassword.length() != 0));

        SchemaDetails details;
        TableItem[] items = mySchemasTable.getItems();
        boolean found = false;
        int i = 0;

        while (i < items.length && !found)
        {
            details = (SchemaDetails) items[i].getData();
            int result = newName.compareToIgnoreCase(details.getName());
            if (result == 0)
            {
                // Same resource type not allowed!
                MessageDialog
                        .openInformation(getControl().getShell(),
                                         PlSqlEditorMessages
                                                 .getString("SchemaMappingPreference.existsTitle"), //$NON-NLS-1$
                                         PlSqlEditorMessages
                                                 .format("SchemaMappingPreference.existsMessage", new Object[]{newName})); //$NON-NLS-1$
                return;
            }

            if (result < 0)
            {
                found = true;
            }
            else
            {
                i++;
            }
        }

        // Create the new type and insert it
        details = new SchemaDetails(newName, newLocation, newPassword);
        TableItem item = newSchemaDetails(details, i, true);
        mySchemasTable.setFocus();
        mySchemasTable.showItem(item);
        fillPackageDetailsTable();
    }

    /**
     * Creates the page's UI content.
     */
    protected Control createContents(Composite parent)
    {
        myImagesToDispose = new ArrayList<Image>();
        myPackagesToImages = new HashMap<PackageDetails, Image>(50);
        Font font = parent.getFont();

        // define container & its gridding
        Composite pageComponent = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        pageComponent.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        pageComponent.setLayoutData(data);
        pageComponent.setFont(font);

        // layout the contents

        // layout the top table & its buttons
        Label schemaDetailsLabel = new Label(pageComponent, SWT.LEFT);
        schemaDetailsLabel.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.schemaDetails")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        schemaDetailsLabel.setLayoutData(data);
        schemaDetailsLabel.setFont(font);

        mySchemasTable = new Table(pageComponent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        mySchemasTable.addListener(SWT.Selection, this);
        mySchemasTable.addListener(SWT.DefaultSelection, this);

        mySchemasTable.setLinesVisible(true);
        mySchemasTable.setHeaderVisible(true);
        String[] titles = { "Name    ", "Location   ", "Password" };
        int[] widths = { 50, 50 * 2, 50};
        for (int i = 0; i < titles.length; i++)
        {
            TableColumn column = new TableColumn(mySchemasTable, SWT.NULL);
            column.setText(titles[i]);
            column.setWidth(widths[i]); // doesn't seem to work
        }
        for (int i = 0; i < titles.length; i++)
        {
            mySchemasTable.getColumn(i).pack();
        }

        mySchemasTable.addMouseMoveListener(new MouseMoveListener()
        {
            public void mouseMove(MouseEvent event)
            {
                Rectangle clientArea = mySchemasTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = mySchemasTable.getTopIndex();
                while (index < mySchemasTable.getItemCount())
                {
                    boolean visible = false;
                    TableItem item = mySchemasTable.getItem(index);
                    int columnCount = mySchemasTable.getColumnCount();
                    for (int i = 0; i < columnCount; i++)
                    {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt))
                        {
                            mySchemasTable.setToolTipText(((SchemaDetails)item.getData()).getLocation());
                            break;
                        }
                        if (!visible && rect.intersects(clientArea))
                        {
                            visible = true;
                        }
                    }
                    if (!visible)
                        return;
                    index++;
                }
                mySchemasTable.setToolTipText("");
            }
        });

        //mySchemasTable.setSize(mySchemasTable.computeSize(SWT.DEFAULT, 200));
        Menu menu = new Menu(getShell(), SWT.POP_UP);
        mySchemasTable.setMenu(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Delete Selection");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                mySchemasTable.remove(mySchemasTable.getSelectionIndices());
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Add Schema");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                promptForSchemaDetails();
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Modify Schema");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                promptForModifySchemaDetails();
            }
        });

        data = new GridData(GridData.FILL_HORIZONTAL);
        int availableRows = DialogUtil.availableRows(pageComponent);

        data.heightHint = mySchemasTable.getItemHeight() * (availableRows / 8);
        mySchemasTable.setLayoutData(data);
        mySchemasTable.setFont(font);

        Composite groupComponent = new Composite(pageComponent, SWT.NULL);
        GridLayout groupLayout = new GridLayout();
        groupLayout.marginWidth = 0;
        groupLayout.marginHeight = 0;
        groupComponent.setLayout(groupLayout);
        data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        groupComponent.setLayoutData(data);
        groupComponent.setFont(font);

        myAddSchemaDetailsButton = new Button(groupComponent, SWT.PUSH);
        myAddSchemaDetailsButton.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.add")); //$NON-NLS-1$
        myAddSchemaDetailsButton.addListener(SWT.Selection, this);
        myAddSchemaDetailsButton.setLayoutData(data);
        myAddSchemaDetailsButton.setFont(font);
        setButtonLayoutData(myAddSchemaDetailsButton);

        myRemoveSchemaDetailsButton = new Button(groupComponent, SWT.PUSH);
        myRemoveSchemaDetailsButton.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.remove")); //$NON-NLS-1$
        myRemoveSchemaDetailsButton.addListener(SWT.Selection, this);
        myRemoveSchemaDetailsButton.setFont(font);
        setButtonLayoutData(myRemoveSchemaDetailsButton);

        // Spacer
        Label spacer = new Label(pageComponent, SWT.LEFT);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        spacer.setLayoutData(data);

        // layout the bottom table & its buttons
        myPackageLabel = new Label(pageComponent, SWT.LEFT);
        myPackageLabel.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.associatedPackages")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        myPackageLabel.setLayoutData(data);
        myPackageLabel.setFont(font);

        myPackageTable = new Table(pageComponent, SWT.SINGLE | SWT.BORDER);
        myPackageTable.addListener(SWT.Selection, this);
        myPackageTable.addListener(SWT.DefaultSelection, this);

        myPackageTable.setLinesVisible(true);
        myPackageTable.setHeaderVisible(true);
        titles = new String [] { "Name    ", "Location   " };
        widths = new int[] { 50, 50 * 2, 50};
        for (int i = 0; i < titles.length; i++)
        {
            TableColumn column = new TableColumn(myPackageTable, SWT.NULL);
            column.setText(titles[i]);
            column.setWidth(widths[i]); // doesn't seem to work
        }
        for (int i = 0; i < titles.length; i++)
        {
            myPackageTable.getColumn(i).pack();
        }

        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = myPackageTable.getItemHeight() * 7;
        myPackageTable.setLayoutData(data);
        myPackageTable.setFont(font);

        groupComponent = new Composite(pageComponent, SWT.NULL);
        groupLayout = new GridLayout();
        groupLayout.marginWidth = 0;
        groupLayout.marginHeight = 0;
        groupComponent.setLayout(groupLayout);
        data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        groupComponent.setLayoutData(data);
        groupComponent.setFont(font);

        myAddPackageButton = new Button(groupComponent, SWT.PUSH);
        myAddPackageButton.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.addPackage")); //$NON-NLS-1$
        myAddPackageButton.addListener(SWT.Selection, this);
        myAddPackageButton.setLayoutData(data);
        myAddPackageButton.setFont(font);
        setButtonLayoutData(myAddPackageButton);

        myRemovePackageButton = new Button(groupComponent, SWT.PUSH);
        myRemovePackageButton.setText(PlSqlEditorMessages
                .getString("SchemaMappingPreference.removePackage")); //$NON-NLS-1$
        myRemovePackageButton.addListener(SWT.Selection, this);
        myRemovePackageButton.setFont(font);
        setButtonLayoutData(myRemovePackageButton);

        fillSchemaDetailsTable();
        if (mySchemasTable.getItemCount() > 0)
        {
            mySchemasTable.setSelection(0);
        }
        fillPackageDetailsTable();
        updateEnabledState();

        // myWorkbench.getHelpSystem().setHelp(parent,someid);
        return pageComponent;
    }

    /**
     * 
     */
    protected void promptForModifySchemaDetails()
    {
        SchemaDetails sd = getSelectedSchemaDetails();
        SchemaDetailsDialog dialog = new SchemaDetailsDialog(getControl().getShell());
        dialog.setName(sd.getName());
        dialog.setLocation(sd.getLocation());
        dialog.setPassword(sd.getPassword());
        dialog.setNameEnabled(false);
        if (dialog.open() == Window.OK)
        {
            sd.setLocation(dialog.getLocation());
            sd.setPassword(dialog.getPassword());
        }
    }

    /**
     * The preference page is going to be disposed. So deallocate all allocated SWT resources that
     * aren't disposed automatically by disposing the page (i.e fonts, cursors, etc). Subclasses
     * should reimplement this method to release their own allocated SWT resources.
     */
    public void dispose()
    {
        super.dispose();
        if (myImagesToDispose != null)
        {
            for (Iterator e = myImagesToDispose.iterator(); e.hasNext();)
            {
                ((Image) e.next()).dispose();
            }
            myImagesToDispose = null;
        }
        if (myPackagesToImages != null)
        {
            for (Iterator e = myPackagesToImages.values().iterator(); e.hasNext();)
            {
                ((Image) e.next()).dispose();
            }
            myPackagesToImages = null;
        }
    }

    /**
     * Hook method to get a page specific preference store. Reimplement this method if a page don't
     * want to use its parent's preference store.
     */
    protected IPreferenceStore doGetPreferenceStore()
    {
        return PlsqleditorPlugin.getDefault().getPreferenceStore();
    }

    protected void fillPackageDetailsTable()
    {
        myPackageTable.removeAll();
        SchemaDetails schemaDetails = getSelectedSchemaDetails();
        if (schemaDetails != null)
        {
            PackageDetails[] array = schemaDetails.getPackages();
            for (int i = 0; i < array.length; i++)
            {
                PackageDetails packageDetails = array[i];
                TableItem item = new TableItem(myPackageTable, SWT.NULL);
                item.setData(packageDetails);
                item.setText(0,packageDetails.getName());
                item.setText(1,packageDetails.getLocation());
                item.setImage(getImage(packageDetails));
            }
        }
    }

    /**
     * Place the existing schema details in the table
     */
    protected void fillSchemaDetailsTable()
    {
        // Populate the table with the items
        SchemaDetails[] array = PlsqleditorPlugin.getDefault().getSchemaRegistry()
                .getSchemaMappings();
        for (int i = 0; i < array.length; i++)
        {
            SchemaDetails mapping = array[i];
            mapping = (SchemaDetails) mapping.clone(); // want a copy
            newSchemaDetails(mapping, i, false);
        }
    }

    /**
     * Returns the image associated with the given editor.
     */
    protected Image getImage(PackageDetails details)
    {
        Image image = myPackagesToImages.get(details);
        if (image == null)
        {
            image = details.getImageDescriptor().createImage();
            myPackagesToImages.put(details, image);
        }
        return image;
    }

    protected SchemaDetails getSelectedSchemaDetails()
    {
        TableItem[] items = mySchemasTable.getSelection();
        if (items.length > 0)
        {
            return (SchemaDetails) items[0].getData(); // Table is single select
        }
        return null;
    }

    protected PackageDetails[] getAssociatedPackageDetails()
    {
        if (getSelectedSchemaDetails() == null)
        {
            return null;
        }
        if (myPackageTable.getItemCount() > 0)
        {
            ArrayList<PackageDetails> packageList = new ArrayList<PackageDetails>();
            for (int i = 0; i < myPackageTable.getItemCount(); i++)
            {
                packageList.add((PackageDetails) myPackageTable.getItem(i).getData());
            }

            return packageList.toArray(new PackageDetails[packageList.size()]);
        }
        return null;
    }

    public void handleEvent(Event event)
    {
        if (event.widget == myAddSchemaDetailsButton)
        {
            promptForSchemaDetails();
        }
        else if (event.widget == myRemoveSchemaDetailsButton)
        {
            removeSelectedSchemaDetails();
        }
        else if (event.widget == myAddPackageButton)
        {
            promptForPackageDetails();
        }
        else if (event.widget == myRemovePackageButton)
        {
            removeSelectedPackage();
        }
        else if (event.widget == mySchemasTable)
        {
            fillPackageDetailsTable();
        }

        updateEnabledState();

    }

    /**
     * @see IWorkbenchPreferencePage
     */
    public void init(IWorkbench aWorkbench)
    {
        this.myWorkbench = aWorkbench;
        noDefaultAndApplyButton();
    }

    /*
     * Create a new <code>TableItem</code> to represent the resource type editor description
     * supplied.
     */
    protected TableItem newSchemaDetails(SchemaDetails mapping, int index, boolean selected)
    {
        Image image = mapping.getImageDescriptor().createImage(false);
        if (image != null)
        {
            myImagesToDispose.add(image);
        }

        TableItem item = new TableItem(mySchemasTable, SWT.NULL, index);
        if (image != null)
        {
            item.setImage(image);
        }

        item.setText(0, mapping.getName());
        item.setText(1, mapping.getLocation());
        item.setText(2, mapping.getPassword());
        item.setData(mapping);
        if (selected)
        {
            mySchemasTable.setSelection(index);
        }

        return item;
    }

    /**
     * This is a hook for sublcasses to do special things when the ok button is pressed. For example
     * reimplement this method if you want to save the page's data into the preference bundle.
     */
    public boolean performOk()
    {
        TableItem[] items = mySchemasTable.getItems();
        SchemaDetails[] schemaDetails = new SchemaDetails[items.length];
        for (int i = 0; i < items.length; i++)
        {
            schemaDetails[i] = (SchemaDetails) (items[i].getData());
        }
        SchemaRegistry registry = PlsqleditorPlugin.getDefault().getSchemaRegistry();

        // to
        // allow
        // save to be
        // called
        registry.setSchemaMappings(schemaDetails);
        registry.saveSchemaMappings(true);
        return true;
    }

    /**
     * Prompt for editor.
     */
    public void promptForPackageDetails()
    {
        PackageDetailsDialog dialog = new PackageDetailsDialog(getControl().getShell());
//        dialog.setMessage(PlSqlEditorMessages
//                .format("PackageSelectionDialog.ChoosePackageForSchema",
//                        new Object[]{getSelectedSchemaDetails().getName()}));
        if (dialog.open() == Window.OK)
        {
            PackageDetails pd = dialog.getPackageDetails();
            if (pd != null)
            {
                int i = myPackageTable.getItemCount();
                TableItem item = new TableItem(myPackageTable, SWT.NULL, i);
                item.setData(pd);
                item.setText(0, pd.getName());
                item.setText(1, pd.getLocation());
                //item.setImage(getImage(pd));
                myPackageTable.setSelection(i);
                myPackageTable.setFocus();
                getSelectedSchemaDetails().addPackage(pd);
                updateSelectedResourceType(); // in case of new default
            }
        }
    }

    /**
     * Prompt for resource type.
     */
    public void promptForSchemaDetails()
    {
        SchemaDetailsDialog dialog = new SchemaDetailsDialog(getControl().getShell());
        if (dialog.open() == Window.OK)
        {
            String name = dialog.getName();
            String location = dialog.getLocation();
            String password = dialog.getPassword();
            addSchemaDetails(name, location, password);
        }
    }

    /**
     * Remove the editor from the table
     */
    public void removeSelectedPackage()
    {
        TableItem[] items = myPackageTable.getSelection();
        if (items.length > 0)
        {
            getSelectedSchemaDetails().removePackage((PackageDetails) items[0].getData());
            items[0].dispose(); // Table is single selection
        }
    }

    /**
     * Remove the type from the table
     */
    public void removeSelectedSchemaDetails()
    {
        TableItem[] items = mySchemasTable.getSelection();
        if (items.length > 0)
        {
            items[0].dispose(); // Table is single selection
        }
        // Clear out the editors too
        myPackageTable.removeAll();
    }

    /**
     * Update the enabled state.
     */
    public void updateEnabledState()
    {
        // Update enabled state
        boolean resourceTypeSelected = mySchemasTable.getSelectionIndex() != -1;
        boolean editorSelected = myPackageTable.getSelectionIndex() != -1;

        myRemoveSchemaDetailsButton.setEnabled(resourceTypeSelected);
        myPackageLabel.setEnabled(resourceTypeSelected);
        myAddPackageButton.setEnabled(resourceTypeSelected);
        myRemovePackageButton.setEnabled(editorSelected);
    }

    /**
     * Update the selected type.
     */
    public void updateSelectedResourceType()
    {
        // TableItem item = resourceTypeTable.getSelection()[0]; //Single select
        // Image image = ((IFileEditorMapping)item.getData()).getImageDescriptor().getImage();
        // imagesToDispose.addElement(image);
        // item.setImage(image);
    }
}
