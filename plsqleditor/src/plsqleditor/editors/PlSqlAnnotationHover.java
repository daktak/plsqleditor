package plsqleditor.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 22/02/2005
 */
public class PlSqlAnnotationHover implements IAnnotationHover
{

    public PlSqlAnnotationHover()
    {
        // do nothing
    }

    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber)
    {
        IDocument document = sourceViewer.getDocument();
        try
        {
            IRegion info = document.getLineInformation(lineNumber);
            return document.get(info.getOffset(), info.getLength());
        }
        catch (BadLocationException _ex)
        {
            return null;
        }
    }
}
