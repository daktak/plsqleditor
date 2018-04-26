/**
 * 
 */
package plsqleditor.views;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.db.DbmsOutput;

/**
 * @author Toby Zines
 * 
 */
public class DbmsOutputView extends ViewPart
{
    public static String          theId = "plsqleditor.views.DbmsOutputView";
    private static DbmsOutputView theInstance;
    private Action                mySetDbmsOutputOnAction;
    private Action                mySetDbmsOutputOffAction;
    CTabFolder                    myDbmsOutputsFolder;

    /**
     * The constructor.
     */
    public DbmsOutputView()
    {
        theInstance = this;
    }

    public static synchronized DbmsOutputView getInstance()
    {
        if (theInstance == null)
        {
            theInstance = new DbmsOutputView();
        }
        return theInstance;
    }

    /**
     * This method gets the currently selected text field, or null if there are none.
     * 
     * @return the currently selected text field, or null if there are none.
     */
    private Text getCurrentText()
    {
        CTabItem selection = myDbmsOutputsFolder.getSelection();
        if (selection != null)
        {
            return (Text) selection.getControl();
        }
        return null;
    }

    public void createPartControl(Composite parent)
    {
        myDbmsOutputsFolder = new CTabFolder(parent, SWT.BORDER);
        makeActions();
        hookContextMenu();
        contributeToActionBars();
    }

    /**
     * This method gets the text field for the given schema if dbms output is set on for that
     * schema. Otherwise it returns null.
     * 
     * @param schema
     *            The name of the schema whose dbms output text is required.
     * 
     * @param setSelection
     *            If this is <code>true</code>, this text field should be brought to the front.
     * 
     * @return The text field for the given schema if dbms output is set on for that schema, and
     *         null otherwise.
     */
    private Text getText(String schema, boolean setSelection)
    {
        CTabItem item = getItem(schema);

        if (item != null && setSelection)
        {
            myDbmsOutputsFolder.setSelection(item);
        }
        return item == null ? null : (Text) item.getControl();
    }

    /**
     * This method gets the CTabItem that contains the text field used to display dbms output for
     * the schema with the supplied <code>schema</code> name.
     * 
     * @param schema
     *            The name of the schema whose owning CTabItem is sought.
     * 
     * @return the CTabItem that contains the text field used to display dbms output for the schema
     *         with the supplied <code>schema</code> name.
     */
    private CTabItem getItem(String schema)
    {
        CTabItem[] items = myDbmsOutputsFolder.getItems();
        for (int i = 0; i < items.length; i++)
        {
            CTabItem item = items[i];
            if (item.getText().equals(schema))
            {
                return item;
            }
        }
        return null;
    }

    void setDbmsOutput(String schema, boolean on)
    {
        CTabItem item = getItem(schema);
        if (item == null)
        {
            if (on)
            {
                // haven't found it, must set it up
                Text dbmsText = new Text(myDbmsOutputsFolder, SWT.BORDER | SWT.MULTI | SWT.WRAP
                        | SWT.V_SCROLL | SWT.READ_ONLY);
                dbmsText.setLayoutData(new GridData(GridData.FILL_BOTH));

                item = new CTabItem(myDbmsOutputsFolder, SWT.NONE);

                item.setText(schema);
                item.setToolTipText("Dbms Output for " + schema);
                item.setControl(dbmsText);
            }
        }
        try
        {
            if (!on)
            {
                item.dispose();
                DbUtility.getDbmsOutput(PlsqleditorPlugin.getDefault().getProject(),schema).disable();
            }
            else
            {
                myDbmsOutputsFolder.setSelection(item);
                DbUtility.getDbmsOutput(PlsqleditorPlugin.getDefault().getProject(),schema).enable(1000000);
            }
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addMessage(String schema)
    {
        if (myDbmsOutputsFolder == null)
        {
            // we haven't been initialised yet, so we can't possibly  
            // have any open dbms outputs
            return;
        }
        Text text = getText(schema, true);
        if (text != null)
        {
            String message = getMessage(schema);
            String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
            message = time + ": " + message + "\n";
            text.setText(message + text.getText());
            getSite().getPage().activate(theInstance);
        }
    }

    /**
     * This method gets a dbms output message for a particular <code>schema</code>.
     * 
     * @param schema
     *            The schema whose dbms output (for the single schema connection) is desired.
     * 
     * @return The dbms output message generated since the last call to this method for the given
     *         <code>schema</code>.
     */
    private String getMessage(String schema)
    {
        try
        {
            DbmsOutput output = DbUtility.getDbmsOutput(PlsqleditorPlugin.getDefault().getProject(),schema);
            return output.show();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "Unable to obtain dbms output: " + e;
        }
    }

    /**
     * This method sets the focus of the current text field (the text field in the currently
     * selected tab in the tab folder).
     */
    public void setFocus()
    {
        Text txt = getCurrentText();
        if (txt != null)
        {
            txt.setFocus();
        }
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                DbmsOutputView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(myDbmsOutputsFolder);
        myDbmsOutputsFolder.setMenu(menu);
        // getSite().registerContextMenu(menuMgr, myDbmsOutputsFolder);
    }

    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager)
    {
        manager.add(mySetDbmsOutputOnAction);
        manager.add(new Separator());
        manager.add(mySetDbmsOutputOffAction);
    }

    void fillContextMenu(IMenuManager manager)
    {
        manager.add(mySetDbmsOutputOnAction);
        manager.add(mySetDbmsOutputOffAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(mySetDbmsOutputOnAction);
        manager.add(mySetDbmsOutputOffAction);
    }

    private void makeActions()
    {
        mySetDbmsOutputOnAction = new Action()
        {
            public void run()
            {
                String currentSchema = PlsqleditorPlugin.getDefault().getCurrentSchema();
                if (MessageDialog.openQuestion(myDbmsOutputsFolder.getShell(),
                                               "Dbms Output",
                                               "Do you wish to turn on dbms output for "
                                                       + currentSchema))
                {
                    setDbmsOutput(currentSchema, true);
                }
            }
        };
        String tooltip = "Turn Dbms Output On for Current Schema";
        mySetDbmsOutputOnAction.setText(tooltip);
        mySetDbmsOutputOnAction.setToolTipText(tooltip);
        mySetDbmsOutputOnAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

        mySetDbmsOutputOffAction = new Action()
        {
            public void run()
            {
                CTabItem item = myDbmsOutputsFolder.getSelection();
                if (item != null)
                {
                    String selectedSchema = item.getText();
                    if (MessageDialog.openQuestion(myDbmsOutputsFolder.getShell(),
                                                   "Dbms Output",
                                                   "Do you wish to turn off dbms output for "
                                                           + selectedSchema))
                    {
                        setDbmsOutput(selectedSchema, false);
                    }
                }
            }
        };
        tooltip = "Turn Dbms Output Off for Current Schema";
        mySetDbmsOutputOffAction.setText(tooltip);
        mySetDbmsOutputOffAction.setToolTipText(tooltip);
        mySetDbmsOutputOffAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
    }
}
