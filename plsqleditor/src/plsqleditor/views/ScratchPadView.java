package plsqleditor.views;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.ResultSetWrapper;


/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ScratchPadView extends ViewPart
{
    private TableViewer myTableViewer;
    private Action      action1;
    private Action      action2;
    private Action      doubleClickAction;
    private Table myOutputTable;

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

    class ViewContentProvider implements IStructuredContentProvider
    {
        private PlsqleditorPlugin myPlugin = PlsqleditorPlugin.getDefault();
        private ResultSetWrapper myCurrentResultSet;
        private boolean myIsNew;
        
        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
            System.out.println("inputChanged: Updated");
            ResultSetWrapper rs = LoadPackageManager.instance().getResultSetWrapper(myPlugin.getCurrentSchema());
            if (rs != myCurrentResultSet)
            {
                myIsNew = true;
                myCurrentResultSet = rs;
            }
        }

        public void dispose()
        {
            if (myCurrentResultSet != null)
            {
                myCurrentResultSet.close();
            }
        }

        public Object[] getElements(Object parent)
        {
            List<Object> elements = new ArrayList<Object>();
            if (myCurrentResultSet != null)
            {
                try
                {
                    ResultSetMetaData rsmd = myCurrentResultSet.getMetaData();
                    String [] columnNames = new String [rsmd.getColumnCount()];
                    while (myCurrentResultSet.next())
                    {
                        for (String column : columnNames)
                        {
                            elements.add(myCurrentResultSet.getObject(column));
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            return elements.toArray();
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        public String getColumnText(Object obj, int index)
        {
            StringTokenizer st = new StringTokenizer(getText(obj), ",");
            for (int i = 0; i < index; i++)
            {
                st.nextElement();
            }
            return st.nextToken();
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

    class NameSorter extends ViewerSorter
    {
    }

    /**
     * The constructor.
     */
    public ScratchPadView()
    {
    }

    /**
     * This is a callback that will allow us to create the myTableViewer and initialize
     * it.
     */
    public void createPartControl(Composite parent)
    {
        myOutputTable = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER | SWT.FULL_SELECTION);
//        myOutputTable.addListener(SWT.Selection, this);
//        myOutputTable.addListener(SWT.DefaultSelection, this);

        myOutputTable.setLinesVisible(true);
        myOutputTable.setHeaderVisible(true);
        String[] titles = { "First ", "Second" };
        int[] widths = { 50, 50 };
        resetTable(titles, widths);

        //myTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        myTableViewer = new TableViewer(myOutputTable);
        myTableViewer.setContentProvider(new ViewContentProvider());
        myTableViewer.setLabelProvider(new ViewLabelProvider());
        myTableViewer.setSorter(new NameSorter());
        myTableViewer.setInput(getViewSite());
        
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void resetTable(String[] titles, int[] widths)
    {
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
        manager.add(action1);
        manager.add(new Separator());
        manager.add(action2);
    }

    private void fillContextMenu(IMenuManager manager)
    {
        manager.add(action1);
        manager.add(action2);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(action1);
        manager.add(action2);
    }

    private void makeActions()
    {
        action1 = new Action()
        {
            public void run()
            {
                showMessage("Action 1 executed");
            }
        };
        action1.setText("Action 1");
        action1.setToolTipText("Action 1 tooltip");
        action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

        action2 = new Action()
        {
            public void run()
            {
                showMessage("Action 2 executed");
            }
        };
        action2.setText("Action 2");
        action2.setToolTipText("Action 2 tooltip");
        action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
        doubleClickAction = new Action()
        {
            public void run()
            {
                ISelection selection = myTableViewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                showMessage("Double-click detected on " + obj.toString());
            }
        };
    }

    private void hookDoubleClickAction()
    {
        myTableViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                doubleClickAction.run();
            }
        });
    }

    private void showMessage(String message)
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