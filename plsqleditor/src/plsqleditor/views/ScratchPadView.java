package plsqleditor.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


/**
 * This class provides the table output from queries executed out of
 * the editor direct to the database.
 */
public class ScratchPadView extends ViewPart
{
    public static String      theId = "plsqleditor.views.ScratchPadView";
    MultiTableViewer          myTableViewer;
    private Action            myNextAction;
    private Action            myRetrieveAllAction;
    Action                    doubleClickAction;
    private Table             myOutputTable;
    private ViewLabelProvider myLabelProvider;

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        public String getColumnText(Object obj, int index)
        {
            if (obj instanceof String[])
            {
                String[] ary = (String[]) obj;
                return ary[index];
            }
            else if (obj instanceof SqlResult)
            {
                SqlResult result = (SqlResult) obj;
                return result.getText(index);
            }
            return String.valueOf(obj);
        }

        public Image getColumnImage(Object obj, int index)
        {
            return getImage(obj);
        }

        public Image getImage(Object obj)
        {
            return PlatformUI.getWorkbench().getSharedImages()
                    .getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    /**
     * The constructor.
     */
    public ScratchPadView()
    {
        System.out.println("Instantiating a ScratchPadView");
    }

    /**
     * This is a callback that will allow us to create the myTableViewer and initialize it.
     */
    public void createPartControl(Composite parent)
    {
        myTableViewer = new MultiTableViewer(parent);
        myTableViewer.setContentProvider(SqlOutputContentProvider.getInstance());
        myTableViewer.setLabelProvider(getLabelProvider());
        myTableViewer.setInput(SqlOutputContentProvider.getInstance());
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private ViewLabelProvider getLabelProvider()
    {
        if (myLabelProvider == null)
        {
            myLabelProvider = new ViewLabelProvider();
        }
        return myLabelProvider;
    }

    void resetTable(String[] titles)
    {
        int[] widths = new int[titles.length];
        for (int i = 0; i < widths.length; i++)
        {
            widths[i] = 50;
        }

        myOutputTable.removeAll();
        for (int i = 0; i < titles.length; i++)
        {
            TableColumn column = new TableColumn(myOutputTable, SWT.NULL);
            column.setText(titles[i]);
            column.setWidth(widths[i]); // doesn't seem to work
        }
        for (int i = 0; i < titles.length; i++)
        {
            myOutputTable.getColumn(i).pack();
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
                ScratchPadView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(myTableViewer.getControl());
        myTableViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, myTableViewer);
    }

    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager)
    {
        manager.add(myNextAction);
        manager.add(new Separator());
        manager.add(myRetrieveAllAction);
    }

    void fillContextMenu(IMenuManager manager)
    {
        manager.add(myNextAction);
        manager.add(myRetrieveAllAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(myNextAction);
        manager.add(myRetrieveAllAction);
    }

    private void makeActions()
    {
        myNextAction = new Action()
        {
            public void run()
            {
                int numRetrieved = SqlOutputContentProvider.getInstance().next(false);
                if (numRetrieved == 0)
                {
                    showMessage("No more results to retrieve");
                }
                else
                {
                    showMessage("Got " + numRetrieved + " next values");
                }
            }
        };
        myNextAction.setText("Next");
        String tooltip = "This retrieves the next set of results from the current open result set";
        myNextAction.setToolTipText(tooltip);
        myNextAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

        myRetrieveAllAction = new Action()
        {
            public void run()
            {
                int numRetrieved = SqlOutputContentProvider.getInstance().next(true);
                showMessage("Got all " + numRetrieved + " remaining values");
            }
        };
        myRetrieveAllAction.setText("Retrieve All");
        tooltip = "This retrieves the all the results from the current open result set";
        myRetrieveAllAction.setToolTipText(tooltip);
        myRetrieveAllAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
    }

    private void hookDoubleClickAction()
    {
/*        myTableViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                doubleClickAction.run();
            }
        });
        */
    }

    void showMessage(String message)
    {
        MessageDialog.openInformation(myTableViewer.getControl().getShell(), "Sql Output", message);
    }

    /**
     * Passing the focus request to the myTableViewer's control.
     */
    public void setFocus()
    {
        myTableViewer.getControl().setFocus();
    }
}