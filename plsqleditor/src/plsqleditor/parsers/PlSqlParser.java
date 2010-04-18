package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public interface PlSqlParser
{
    public List<Segment> parseFile(IDocument document, String[] packageName, SegmentType[] filters)
    throws IOException;

    public int parseBody(int currentLineOffset,
                          IDocument document,
                          BufferedReader br,
                          List<Segment> segments,
                          Segment packageSegment) throws IOException, BadLocationException;

}
