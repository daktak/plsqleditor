/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import au.com.gts.data.Column;
import au.com.gts.data.Table;
import au.com.zinescom.util.UsefulOperations;

public class PlSqlTextHover implements ITextHover
{
    private List<Segment>         myCurrentSegments;
    public static char[] searchTextDelimiters = new char[]{' ', '\t', '(', ';', ',', ')', '\r', '\n', '|'};
    PlsqleditorPlugin myPluginRef = PlsqleditorPlugin.getDefault();

    public PlSqlTextHover()
    {
        //
    }

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        IDocument doc = textViewer.getDocument();
        myCurrentSegments = PlsqleditorPlugin.getDefault().getCurrentSegments(doc);
        if (hoverRegion != null)
        {
            try
            {
                if (hoverRegion.getLength() > -1)
                {
                    int documentOffset = hoverRegion.getOffset();
                    String searchString = doc.get(documentOffset, hoverRegion.getLength());

                    List<Segment> segments = getMatchingSegments(searchString, doc, documentOffset);
                    
                    if (segments.size() > 0)
                    {
                        final String PRE_SPACES = "   ";
                        final String _COLON_ = " : ";
                        Segment foundSegment = (Segment) segments.get(0);
                        SegmentType type = foundSegment.getType();
                        if (type == SegmentType.Table)
                        {
                            Table t = (Table) foundSegment.getReferredData();
                            StringBuffer sb = new StringBuffer();
                            if (t != null)
                            {
                                sb.append("Table ").append(t.getName());
                                List<?> cols = t.getColumns();
                                int maxLength = 0;
                                for (Iterator<?> it = cols.iterator(); it.hasNext();)
                                {
                                    Column col = (Column) it.next();
                                    int length = col.getName().length();
                                    maxLength = maxLength > length ? maxLength : length;
                                }

                                for (Iterator<?> it = cols.iterator(); it.hasNext();)
                                {
                                    StringBuffer tmpSb = new StringBuffer(PRE_SPACES);
                                    Column col = (Column) it.next();
                                    tmpSb.append(UsefulOperations.pad(col.getName(), UsefulOperations.SPACE, maxLength)).append(_COLON_);
                                    tmpSb.append(col.getSQLTypeName());

                                    sb.append(UsefulOperations.NEWLINE).append(tmpSb.toString());
                                }
                                return sb.toString();
                            }
                            else
                            {
                                return "This is a table";
                            }
                        }
                        else if (type == SegmentType.Column)
                        {
                            Column c = (Column) foundSegment.getReferredData();
                            if (c != null)
                            {
                                StringBuffer sb = new StringBuffer();
                                sb.append("Column from table ").append(c.getTable().getName());
                                sb.append("\n").append(PRE_SPACES).append(c.getName());
                                sb.append(_COLON_).append(c.getSQLTypeName());
                                return sb.toString();
                            }
                            else
                            {
                                return "This is a column";
                            }
                        }
                        else if (type == SegmentType.Constant)
                        {
                            // TODO change parsing to pick up the constant part of the segment, and return it here
                            return "Constant: " + foundSegment.getReferredData() +
                            "\n" +
                            foundSegment.getDocumentation();
                        }
                        else if (type == SegmentType.Field)
                        {
                            return foundSegment.getPresentationName(true, true, true) +
                            "\n" +
                            foundSegment.getDocumentation();
                        }
                        return foundSegment.getDocumentation(true);
                    }
                    return textViewer.getDocument().get(hoverRegion.getOffset(),
                                                        hoverRegion.getLength());
                }
            }
            catch (BadLocationException _ex)
            {
                //
            }
        }
        return PlSqlEditorMessages.getString("PlSqlTextHover.emptySelection");
    }

    /**
     * This method gets the sequence of characters around the supplied <code>offset</code> position
     * which is in fact the location of the mouse hovering over the text.
     * 
     * @param textViewer The viewer of the text in question.
     * 
     * @param offset The hover offset to search from.
     * 
     * @return The region of text that surrounds the <code>offset</code> position. The text includes
     *         any sequence of characters not broken by a character listed in the
     *         {@link #searchTextDelimiters}.  
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        IDocument doc = textViewer.getDocument();
        IRegion toReturn = getRegion(doc, offset);

        if (toReturn != null)
        {
            return toReturn;
        }
        
        Point selection = textViewer.getSelectedRange();
        if (selection.x <= offset && offset < selection.x + selection.y)
        {
            toReturn = new Region(selection.x, selection.y);
        }
        else
        {
            toReturn = new Region(offset, 0);
        }
        return toReturn;
    }

    /**
     * This method gets the sequence of characters around the supplied <code>offset</code> position.
     * 
     * @param textViewer The viewer of the text in question.
     * 
     * @param offset The offset to search from.
     * 
     * @return The region of text that surrounds the <code>offset</code> position. The text includes
     *         any sequence of characters not broken by a character listed in the
     *         {@link #searchTextDelimiters}. This will return the region 0,0 if the hover is over the first
     *         letter of the line.  
     */
    public static IRegion getRegion(IDocument doc, int offset)
    {
        IRegion toReturn = null;
        try
        {
            int line = doc.getLineOfOffset(offset);
            int linestart = doc.getLineOffset(line);
            int length = doc.getLineLength(line);
            int mouseLocation = offset - linestart;
            if (mouseLocation < 0)
            {
                mouseLocation = 1;
            }
            String lineOfText = doc.get(linestart, length/* - 1*/);
            int start = mouseLocation - 1;
            int end = mouseLocation - 1;
            for (; start >= 0; start--)
            {
                if (matchesDelimiter(lineOfText.charAt(start)))
                {
                    break;
                }
            }
            for (; end >= 0 && end < length - 1; end++)
            {
                if (matchesDelimiter(lineOfText.charAt(end)))
                {
                    break;
                }
            }
            int lngth = end - (start + 1);
            if (lngth <= 0)
            {
                return new Region(0,0);
            }
            toReturn = new Region(start + linestart + 1, lngth < 0 ? 0 : lngth);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return toReturn;
    }

    private List<Segment> getMatchingSegments(String fullyQualifiedSegmentName, IDocument doc, int offset)
    {
        List<Segment> toReturn = new ArrayList<Segment>();
        String lastSegmentName = fullyQualifiedSegmentName.toUpperCase();
        
        // Fix for Support Request 1418289 - "Describe" right-click
        PlSqlCompletionProcessor.computeMatchedSegments(toReturn, offset, doc, lastSegmentName, "", myCurrentSegments, true);
        return toReturn;
    }

    /**
     * @param c
     * @return <code>true</code> if the character <code>c</code> matches one of the
     *         {@link #searchTextDelimiters}.
     */
    private static boolean matchesDelimiter(char c)
    {
        for (int i = 0; i < searchTextDelimiters.length; i++)
        {
            if (searchTextDelimiters[i] == c)
            {
                return true;
            }
        }
        return false;
    }
}
