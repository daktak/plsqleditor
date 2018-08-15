/**
 * 
 */
package plsqleditor.preferences;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import plsqleditor.Utils;
import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.preferences.entities.PackageDetails;
import plsqleditor.preferences.entities.PackageLocation;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id: PackageDetailsDialog.java,v 1.1.2.1 2005/04/07 06:34:27 tobyz
 *          Exp $
 * 
 * Created on 4/03/2005
 */
public class PackageDetailsDialog extends TitleAreaDialog implements Listener
{
    protected String myPackageName = ""; //$NON-NLS-1$
    protected Text   myPackageNameField;

    protected Button myOkButton;
    Table            myLocationsTable;
    private List<PackageLocation>     myLocations = new ArrayList<PackageLocation>();

    /**
     * Constructs a new file extension dialog.
     * 
     * @param parentShell the myParent shell
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
     * Creates and returns the contents of the upper part of the dialog (above
     * the button bar).
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
        Font font = parent.getFont();

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
                myOkButton.setEnabled(validateDataEntered());
            }
        };

        myPackageNameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        myPackageNameField.setText(myPackageName);
        myPackageNameField.addModifyListener(ml);

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        myPackageNameField.setLayoutData(data);
        myPackageNameField.setFocus();

        label = new Label(contents, SWT.LEFT);
        label.setText(PlSqlEditorMessages.getString("PackageDetails.packageLocations")); //$NON-NLS-1$

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        myLocationsTable = new Table(contents, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        myLocationsTable.addListener(SWT.Selection, this);
        myLocationsTable.addListener(SWT.DefaultSelection, this);

        myLocationsTable.setLinesVisible(true);
        myLocationsTable.setHeaderVisible(true);
        String[] titles = {"Location                        ", "ParseType           "};
        int[] widths = {70, 70 * 2};
        for (int i = 0; i < titles.length; i++)
        {
            TableColumn column = new TableColumn(myLocationsTable, SWT.NULL);
            column.setText(titles[i]);
            column.setWidth(widths[i]); // doesn't seem to work
        }
        addLocationsToTable(myLocations);
        for (int i = 0; i < titles.length; i++)
        {
            myLocationsTable.getColumn(i).pack();
        }

        myLocationsTable.addMouseMoveListener(new MouseMoveListener()
        {
            public void mouseMove(MouseEvent event)
            {
                Rectangle clientArea = myLocationsTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = myLocationsTable.getTopIndex();
                while (index < myLocationsTable.getItemCount())
                {
                    boolean visible = false;
                    TableItem item = myLocationsTable.getItem(index);
                    int columnCount = myLocationsTable.getColumnCount();
                    for (int i = 0; i < columnCount; i++)
                    {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt))
                        {
                            PackageLocation pl = (PackageLocation) item.getData();
                            myLocationsTable.setToolTipText(pl.getLocation());
                            break;
                        }
                        if (!visible && rect.intersects(clientArea))
                        {
                            visible = true;
                        }
                    }
                    if (!visible) return;
                    index++;
                }
                myLocationsTable.setToolTipText("");
            }
        });

        // myLocationsTable.setSize(myLocationsTable.computeSize(SWT.DEFAULT,
        // 200));
        Menu menu = new Menu(getShell(), SWT.POP_UP);
        myLocationsTable.setMenu(menu);

        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Add Location");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                promptForLocationDetails();
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Modify Location");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                promptForModifyLocationDetails();
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Delete Selection");
        item.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                int [] selections  = myLocationsTable.getSelectionIndices();
                TableItem [] items = myLocationsTable.getSelection();
                for (int i = 0; i < items.length; i++)
                {
                    PackageLocation pl = (PackageLocation) items[0].getData();
                    removeLocation(pl);
                }
                myLocationsTable.remove(selections);
            }
        });
        data = new GridData(GridData.FILL_HORIZONTAL);
        int availableRows = Utils.availableRows(contents);

        data.heightHint = myLocationsTable.getItemHeight() * (availableRows / 8);
        myLocationsTable.setLayoutData(data);
        myLocationsTable.setFont(font);

        Dialog.applyDialogFont(parentComposite);
        return contents;
    }

    protected void removeLocation(PackageLocation pl)
    {
        for (int i = myLocations.size() - 1; i <=0; i--)
        {
            PackageLocation location = (PackageLocation) myLocations.get(i);
            if (location.getLocation().equals(pl.getLocation()))
            {
                myLocations.remove(i);
            }
        }
    }

    /**
     * Prompt for location details.
     */
    public void promptForLocationDetails()
    {
        PackageLocationDetailsDialog dialog = new PackageLocationDetailsDialog(getShell());
        if (dialog.open() == Window.OK)
        {
            PackageLocation pl = dialog.getPackageLocation();
            if (pl != null)
            {
                int i = myLocationsTable.getItemCount();
                TableItem item = new TableItem(myLocationsTable, SWT.NULL, i);
                item.setData(pl);
                item.setText(0, pl.getLocation());
                item.setText(1, pl.getParseType().toString());
                // item.setImage(getImage(pd));
                myLocationsTable.setSelection(i);
                myLocationsTable.setFocus();
                addLocation(pl);
            }
        }
    }

    /**
     * 
     */
    protected void promptForModifyLocationDetails()
    {
        PackageLocation pl = getSelectedPackageLocation();

        PackageLocationDetailsDialog dialog = new PackageLocationDetailsDialog(getShell());
        dialog.setLocation(pl.getLocation());
        dialog.setParseType(pl.getParseType().toString());
        if (dialog.open() == Window.OK)
        {
            PackageLocation newLocation = dialog.getPackageLocation();
            pl.setParseType(newLocation.getParseType());
            addLocation(newLocation);
        }
    }

    /**
     * This method adds a location to {@link #myLocations}.
     * If it is only modifying, then the location details will not be increased in size, only modified.
     * 
     * @param newLocation The modified location.
     */
    private void addLocation(PackageLocation newLocation)
    {
        for (Iterator<PackageLocation> it = myLocations.iterator(); it.hasNext();)
        {
            PackageLocation loc = (PackageLocation) it.next();
            if (loc.getLocation().equals(newLocation.getLocation()))
            {
                loc.setParseType(newLocation.getParseType());
                return;
            }
        }
        myLocations.add(newLocation);
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
        // check for empty name and extension
        if (myPackageName.length() == 0)
        {
            setErrorMessage("Package Name is empty");
            return false;
        }
        if (myLocations.size() == 0)
        {
            setErrorMessage("You must specify at least one location");
        }
        setErrorMessage(null);
        return true;
    }

    /**
     * Get the package details object.
     * 
     * @return the package details in its entirety.
     */
    public PackageDetails getPackageDetails()
    {
        return new PackageDetails(myPackageName, (PackageLocation[]) myLocations
                .toArray(new PackageLocation[myLocations.size()]));
    }

    public void handleEvent(Event event)
    {
        updateEnabledState();
    }
    
    /**
     * Update the enabled state.
     */
    public void updateEnabledState()
    {
        // Update enabled state
        myOkButton.setEnabled(validateDataEntered());
    }


    protected PackageLocation getSelectedPackageLocation()
    {
        TableItem[] items = myLocationsTable.getSelection();
        if (items.length > 0)
        {
            return (PackageLocation) items[0].getData(); // Table is single
            // select
        }
        return null;
    }

    public void setPackageName(String packageName)
    {
        myPackageName = packageName;
    }

    public void setLocations(PackageLocation[] locations)
    {
        myLocations = new ArrayList<PackageLocation>();
        for (int i = 0; i < locations.length; i++)
        {
            myLocations.add(locations[i]);
        }
    }

    private void addLocationsToTable(List<PackageLocation> locations)
    {
        TableItem item = null;
        int index = 0;
        for (Iterator<PackageLocation> it = locations.iterator(); it.hasNext();)
        {
            PackageLocation location = (PackageLocation) it.next();
            item = newPackageLocation(location, index++, false);
        }
        if (item != null)
        {
            myLocationsTable.setFocus();
            myLocationsTable.showItem(item);
        }
    }

    /*
     * Create a new <code>TableItem</code> to represent ...
     */
    protected TableItem newPackageLocation(PackageLocation loc, int index, boolean selected)
    {
        TableItem item = new TableItem(myLocationsTable, SWT.NULL, index);
        item.setText(0, loc.getLocation());
        item.setText(1, loc.getParseType().toString());
        item.setData(loc);
        if (selected)
        {
            myLocationsTable.setSelection(index);
        }
        return item;
    }
}
