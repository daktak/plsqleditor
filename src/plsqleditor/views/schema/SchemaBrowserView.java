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
import org.eclipse.jface.viewers.ViewerComparator;
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

import au.com.gts.data.ForeignKeyConstraint;
import plsqleditor.PlsqleditorPlugin;
import plsqleditor.actions.LookupFileAction;
import plsqleditor.actions.OpenLocationType;

/**
 * This view shows data obtained from the singleton
 * SchemaBrowserContentProvider.
 */

public class SchemaBrowserView extends ViewPart {
    public static final String theId = "plsqleditor.views.SchemaBrowserView";
    TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action goToFileAction;
    private Action refreshAction;

    /*
     * The content provider class is responsible for providing objects to the view.
     * It can wrap existing objects in adapters or simply return objects as-is.
     * These objects may be sensitive to the current input of the view, or ignore it
     * and always show the same content (like Task List, for example).
     */

    class ViewLabelProvider extends LabelProvider {
	public String getText(Object obj) {
	    if (obj != null && obj instanceof TreeObject) {
		return ((TreeObject) obj).getDisplayName();
	    }
	    return String.valueOf(obj);
	}

	public Image getImage(Object obj) {
	    PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();

	    String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
	    if (obj instanceof TreeParent) {
		TreeParent tp = (TreeParent) obj;
		imageKey = tp.getImageKey();
		if (imageKey == null) {
		    imageKey = ISharedImages.IMG_OBJ_FOLDER;
		}
	    } else if (obj instanceof TreeObject) {
		TreeObject treeObj = (TreeObject) obj;
		Image image = plugin.getImageRegistry().get(treeObj.getImageKey());
		if (image != null) {
		    return image;
		}
	    }
	    Image image = plugin.getImageRegistry().get(imageKey);
	    if (image == null) {
		image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	    }
	    return image;
	}
    }

    /**
     * The constructor.
     */
    public SchemaBrowserView() {
	//
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
	viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	drillDownAdapter = new DrillDownAdapter(viewer);
	viewer.setContentProvider(SchemaBrowserContentProvider.getInstance());
	viewer.setLabelProvider(new ViewLabelProvider());
	viewer.setComparator(new ViewerComparator());
	viewer.setInput(getViewSite());
	makeActions();
	hookContextMenu();
	hookDoubleClickAction();
	contributeToActionBars();
	getSite().setSelectionProvider(viewer);
    }

    private void hookContextMenu() {
	MenuManager menuMgr = new MenuManager("#PopupMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
	    public void menuAboutToShow(IMenuManager manager) {
		SchemaBrowserView.this.fillContextMenu(manager);
	    }
	});
	Menu menu = menuMgr.createContextMenu(viewer.getControl());
	viewer.getControl().setMenu(menu);
	getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
	IActionBars bars = getViewSite().getActionBars();
	fillLocalPullDown(bars.getMenuManager());
	fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
	manager.add(goToFileAction);
	manager.add(new Separator());
    }

    void fillContextMenu(IMenuManager manager) {
	manager.add(goToFileAction);
	manager.add(new Separator());

	drillDownAdapter.addNavigationActions(manager);
	// Other plug-ins can contribute there actions here
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
	manager.add(refreshAction);
	manager.add(goToFileAction);
	manager.add(new Separator());
	drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions() {
	refreshAction = new Action() {
	    public void run() {
		SchemaBrowserContentProvider provider = (SchemaBrowserContentProvider) viewer.getContentProvider();
		provider.refresh(true);
		viewer.refresh();
	    }
	};
	refreshAction.setText("Refresh");
	refreshAction.setToolTipText("Refresh the tree from the top");
	refreshAction.setImageDescriptor(PlsqleditorPlugin.getImageDescriptor("icons/NewSheet.gif"));

	goToFileAction = new Action() {
	    public void run() {
		goToFile();
	    }
	};
	goToFileAction.setText("Go to File");
	goToFileAction.setToolTipText("This takes you to the file if there is a file representing this object");
	goToFileAction.setImageDescriptor(
		PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OPEN_MARKER));
    }

    /**
     * This method navigates from the selected node in the schema browser to the
     * file that the node is backed by. If the node is not backed by a file, a
     * message will pop up indicating this.
     */
    void goToFile() {
	ISelection selection = viewer.getSelection();
	Object obj = ((IStructuredSelection) selection).getFirstElement();
	if (obj instanceof TreeObject) {
	    TreeObject treeObject = (TreeObject) obj;
	    IFile file = treeObject.getFile();
	    if (file != null) {
		IWorkbenchPart part = getSite().getPart();
		String schemaName = treeObject.getNameForType("Schema");
		String packageName = treeObject.getNameForType("Package");

		LookupFileAction.openEditor(part, treeObject.getName(), OpenLocationType.Method, schemaName,
			packageName);
	    } else {
		// TODO extend this to use the db loaded version if there is no
		// file based one
		showMessage("There is no file for the object [" + obj + "]");
	    }
	} else {
	    showMessage("Not a tree object");
	}
    }

    private void hookDoubleClickAction() {
	viewer.addDoubleClickListener(new IDoubleClickListener() {
	    public void doubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof TreeObject) {
		    TreeObject treeObject = (TreeObject) obj;
		    String type = treeObject.getType();
		    if (type.equals("Package")) {
			// TODO extend this to deal with functions, procs,
			// types, triggers
			goToFile();
		    } else if (type.equals("Segment")) {
			goToFile();
		    } else if (type.equals("Constraint")) {
			Object tobj = treeObject.getObject();
			if (tobj instanceof ForeignKeyConstraint) {
			    // TODO navigate to the pk column
			} else {
			    showMessage("No navigation from this object");
			}
		    } else {
			showMessage("No navigation from this object");
		    }
		}
	    }
	});
    }

    void showMessage(String message) {
	MessageDialog.openInformation(viewer.getControl().getShell(), "Schema Browser", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
	viewer.getControl().setFocus();
    }
}