package plsqleditor.views.schema;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.LookupFileAction;
import plsqleditor.actions.OpenLocationType;
import plsqleditor.parsers.Segment;
import plsqleditor.views.schema.SchemaBrowserContentProvider.TreeObject;
import plsqleditor.views.schema.SchemaBrowserContentProvider.TreeParent;


/**
 * This view shows data obtained from the singleton
 * SchemaBrowserContentProvider.
 * <p>
 * 
 * <p>
 */

public class SchemaBrowserView extends ViewPart
{
    public static final String theId = "plsqleditor.views.SchemaBrowserView";
    TreeViewer                 viewer;
    private DrillDownAdapter   drillDownAdapter;
    private Action             goToFileAction;
    private Action             showGrantsAction;
    private Action             loadFromDatabaseAction;
    Action                     doubleClickAction;

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

    class ViewLabelProvider extends LabelProvider
    {

        public String getText(Object obj)
        {
            return obj.toString();
        }

        public Image getImage(Object obj)
        {
            PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();

            String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            if (obj instanceof TreeParent)
            {
                imageKey = ISharedImages.IMG_OBJ_FOLDER;
            }
            else if (obj instanceof TreeObject)
            {
                TreeObject treeObj = (TreeObject) obj;
                Object containedObject = treeObj.getObject();
                if (containedObject instanceof Segment)
                {
                    Segment seg = (Segment) containedObject;
                    Image image = plugin.getImageRegistry()
                            .get("Schema" + seg.getType().toString());
                    if (image != null)
                    {
                        return image;
                    }
                }
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }

    class NameSorter extends ViewerSorter
    {
        //
    }

    /**
     * The constructor.
     */
    public SchemaBrowserView()
    {
        //
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent)
    {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(SchemaBrowserContentProvider.getInstance());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                SchemaBrowserView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager)
    {
        manager.add(goToFileAction);
        manager.add(new Separator());
    }

    void fillContextMenu(IMenuManager manager)
    {
        manager.add(goToFileAction);
        manager.add(new Separator());
        boolean isSeparatorRequired = false;

        ISelection selection = viewer.getSelection();
        if (selection != null)
        {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof TreeObject)
            {
                TreeObject treeObject = (TreeObject) obj;
                String type = treeObject.getType();
                String schema = treeObject.getNameForType("Schema");
                if (schema != null && schema.length() > 0)
                {
                    loadFromDatabaseAction = SchemaBrowserContentProvider.getInstance()
                            .getOpenDatabasePackageAction(viewer, schema);
                    showGrantsAction = SchemaBrowserContentProvider.getInstance()
                            .getShowGrantsAction(viewer, schema);
                    if (type.equals("Package"))
                    {
                        isSeparatorRequired = true;
                        manager.add(showGrantsAction);
                    }
                    if (type.equals("Package"))
//                        && (treeObject instanceof TreeParent))
//                            && ((TreeParent) treeObject).getChildren().length == 0)
                    {
                        isSeparatorRequired = true;
                        manager.add(loadFromDatabaseAction);
                    }
                }
            }
            if (isSeparatorRequired)
            {
                manager.add(new Separator());
            }
        }

        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(goToFileAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions()
    {
        goToFileAction = new Action()
        {
            public void run()
            {
                goToFile();
            }

        };
        goToFileAction.setText("Go to File");
        goToFileAction
                .setToolTipText("This takes you to the file if there is a file representing this object");
        goToFileAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(SharedImages.IMG_OPEN_MARKER));

        doubleClickAction = new Action()
        {
            public void run()
            {
                goToFile();
            }
        };
    }

    /**
     * This method navigates from the selected node in the schema browser to the
     * file that the node is backed by. If the node is not backed by a file, a
     * message will pop up indicating this.
     */
    void goToFile()
    {
        ISelection selection = viewer.getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj instanceof TreeObject)
        {
            TreeObject treeObject = (TreeObject) obj;
            IFile file = treeObject.getFile();
            if (file != null)
            {
                IWorkbenchPart part = getSite().getPart();
                String schemaName = treeObject.getNameForType("Schema");
                String packageName = treeObject.getNameForType("Package");

                LookupFileAction.openEditor(part,
                                            treeObject.getName(),
                                            OpenLocationType.Method,
                                            schemaName,
                                            packageName);
            }
            else
            {
                showMessage("There is no file for the object [" + obj + "]");
            }
        }
        else
        {
            showMessage("Not a tree object");
        }
    }

    private void hookDoubleClickAction()
    {
        viewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                doubleClickAction.run();
            }
        });
    }

    void showMessage(String message)
    {
        MessageDialog.openInformation(viewer.getControl().getShell(), "Schema Browser", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }
}