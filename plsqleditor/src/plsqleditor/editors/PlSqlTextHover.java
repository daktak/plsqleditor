/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.*;
import org.eclipse.swt.graphics.Point;

public class PlSqlTextHover implements ITextHover
{

    public PlSqlTextHover()
    {
        //
    }

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        if (hoverRegion != null) try
        {
            if (hoverRegion.getLength() > -1) return textViewer.getDocument()
                    .get(hoverRegion.getOffset(), hoverRegion.getLength());
        }
        catch (BadLocationException _ex)
        {
            //
        }
        return PlSqlEditorMessages.getString("PlSqlTextHover.emptySelection");
    }

    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        Point selection = textViewer.getSelectedRange();
        if (selection.x <= offset && offset < selection.x + selection.y)
        {
            return new Region(selection.x, selection.y);
        }
        else
        {
            return new Region(offset, 0);
        }
    }
}
