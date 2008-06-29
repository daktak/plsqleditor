package plsqleditor;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import plsqleditor.views.DbmsOutputView;
import plsqleditor.views.ScratchPadView;
import plsqleditor.views.schema.SchemaBrowserView;

/**
 * This class represents the perspective that this plugin uses.
 */
public class Perspective implements IPerspectiveFactory
{
    /**
     * The constructor.
     */
    public Perspective()
    {
        //
    }

    /**
     * Insert the method's description here.
     * 
     * @see IPerspectiveFactory#createInitialLayout
     */
    public void createInitialLayout(IPageLayout layout)
    {
        String editorArea = layout.getEditorArea();
        
        defineActions(layout);
        
        // Left side
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
        left.addView(IPageLayout.ID_RES_NAV);
        left.addView(SchemaBrowserView.theId);

        // Right side
        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.75f, editorArea);
        right.addView(IPageLayout.ID_OUTLINE);

        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
        bottom.addView(ScratchPadView.theId);
        bottom.addView(DbmsOutputView.theId);
    }

    /**
     * Defines the initial actions for a page.  
     */
    public void defineActions(IPageLayout layout) {
        // Add "new wizards".
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$

        // Add "show views".
        layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
        layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        
        layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective");
        //layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");

        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
    }

}
