package org.boomsticks.plsqleditor.dialogs.openconnections;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.MultiPagePlsqlEditor;


/**
 * Label provider for the TableViewerExample
 * 
 * @see org.eclipse.jface.viewers.LabelProvider
 */
public class LiveConnectionLabelProvider extends LabelProvider implements ITableLabelProvider
{

    // Names of images used to represent checkboxes
    public static final String   CHECKED_IMAGE   = "checked";
    public static final String   UNCHECKED_IMAGE = "unchecked";

    // For the checkbox images
    private static ImageRegistry imageRegistry   = new ImageRegistry();

    /**
     * Note: An image registry owns all of the image objects registered with it,
     * and automatically disposes sof them the SWT Display is disposed.
     */
    static
    {
        String iconPath = "icons/";
        imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(PlsqleditorPlugin.class,
                                                                        iconPath + CHECKED_IMAGE
                                                                                + ".gif"));
        imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(PlsqleditorPlugin.class,
                                                                          iconPath
                                                                                  + UNCHECKED_IMAGE
                                                                                  + ".gif"));
    }

    /**
     * Returns the image with the given key, or <code>null</code> if not found.
     */
    private Image getImage(boolean isSelected)
    {
        String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
        return imageRegistry.get(key);
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        String result = "";
        LiveConnection conn = (LiveConnection) element;
        switch (columnIndex)
        {
            case 0 : // COMPLETED_COLUMN
                break;
            case 1 :
                result = conn.getUrl();
                break;
            case 2 :
                result = conn.getFilename();
                break;
            case 3 :
                result = conn.getProject()+ "";
                break;
            default :
                break;
        }
        return result;
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        return (columnIndex == 0) ? // COMPLETED_COLUMN?
                getImage(!((LiveConnection) element).getType().equals(MultiPagePlsqlEditor.SCHEMA_DEFAULT))
                : null;
    }

}
