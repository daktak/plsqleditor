/**
 * 
 */
package plsqleditor.doc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlCompletionProcessor;
import plsqleditor.parsers.Segment;



/**
 * @author Toby Zines
 * 
 */
public class PlSqlDocCompletionProcessor implements IContentAssistProcessor
{
    protected final static String[] fgProposals = PlSqlDocScanner.fgKeywords;

    private IDocument myCurrentDoc;

    private List<Segment> myCurrentSegments;

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument doc = viewer.getDocument();
        if (myCurrentDoc != doc)
        {
            myCurrentDoc = doc;
            myCurrentSegments = PlsqleditorPlugin.getDefault().getSegments(doc);
        }
        try
        {
            int line = doc.getLineOfOffset(documentOffset);
            int start = doc.getLineOffset(line);
            int length = documentOffset - start;
            String lineOfText = doc.get(start, length);
            int lastNonUsableCharacter = -1;
            for (char c : PlSqlCompletionProcessor.autoCompleteDelimiters)
            {
                lastNonUsableCharacter = Math.max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
            }
            String currText = lineOfText.substring(lastNonUsableCharacter + 1).toUpperCase();
            List<Segment> completions = new ArrayList<Segment>();
        }
        catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        ICompletionProposal[] result = new ICompletionProposal[fgProposals.length];
        for (int i = 0; i < fgProposals.length; i++)
            result[i] = new CompletionProposal(fgProposals[i], documentOffset, 0, fgProposals[i]
                    .length());
        return result;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public String getErrorMessage()
    {
        return null;
    }
}
