package org.boomsticks.plsqleditor.dialogs.openconnections;

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
    public static final String   CHECKED_IMAGE   = "tick";
    public static final String   UNCHECKED_IMAGE = "cross";

    /**
     * Returns the image with the given key, or <code>null</code> if not found.
     */
    private Image getImage(boolean isSelected)
    {
        String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
        return PlsqleditorPlugin.getDefault().getImageRegistry().get(key);
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
            case 0 : // type is schema based
            	result = conn.getType();
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
            case 4 :
            	result = conn.getUser();
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
        return (columnIndex == 0) ? //
                getImage(!((LiveConnection) element).getType().equals(MultiPagePlsqlEditor.SCHEMA_DEFAULT))
                : null;
    }

}
