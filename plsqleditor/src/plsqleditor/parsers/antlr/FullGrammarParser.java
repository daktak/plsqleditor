package plsqleditor.parsers.antlr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

import au.com.alcatel.fulfil.tools.codecheck.parser.PlSqlLexer;
import au.com.alcatel.fulfil.tools.codecheck.parser.PlSqlParser;

public class FullGrammarParser implements plsqleditor.parsers.PlSqlParser
{
    public List<Segment> parseFile(IDocument document, String[] packageName, SegmentType[] filters)
            throws IOException
    {
        List<Segment> toReturn = new ArrayList<Segment>();
        PlSqlLexer lexer = new PlSqlLexer(new ANTLRNoCaseStringStream(document.get()));
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        PlSqlParser parser = new PlSqlParser(tokens);
        
        final PlSqlParser.start_rule_return ptree;
        try
        {
            ptree = parser.start_rule();
            toReturn.add(new RootSegment(ptree, tokens));
        }
        catch (RecognitionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * This method parses the body of the file in question.
     * 
     * @param currentLineOffset The current line we are parsing.
     * 
     * @param document The document we are parsing (from line
     *            <code>currentLineOffset</code>). This is only used to
     *            determine context sensitive offset details. The actual input
     *            is parsed off the supplied <code>file</code>.
     * 
     * @param file The file from which we are actually parsing the data.
     * 
     * @param segments The list of segments to add to when segments are parsed
     *            from the <code>file</code>.
     * 
     * @param packageSegment The segment representing the package that this file
     *            is creating.
     */
    public int parseBody(int currentLineOffset,
                         IDocument document,
                         BufferedReader br,
                         List segments,
                         Segment packageSegment) throws IOException, BadLocationException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    

}
