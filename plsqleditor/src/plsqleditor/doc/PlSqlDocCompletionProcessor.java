package plsqleditor.doc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlCompletionProcessor;
import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.parsers.Segment;

/**
 * @author Toby Zines
 * 
 */
public class PlSqlDocCompletionProcessor implements IContentAssistProcessor
{
    protected final static String[]  fgProposals      = PlSqlDocScanner.fgKeywords;
    private PlSqlCompletionProcessor myPlSqlCompletor = new PlSqlCompletionProcessor();

    private IDocument                myCurrentDoc;

    private List<Segment>            myCurrentSegments;
    final String                     paramString      = "@param";
    final String                     throwsString     = "@throws";
    final String[]                   linkStrings      = new String[]{"{@link", "@see", "@refer"};

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument doc = viewer.getDocument();
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        if (myCurrentDoc != doc)
        {
            myCurrentDoc = doc;
            myCurrentSegments = plugin.getSegments(doc);
        }
        List<Segment> completions = new ArrayList<Segment>();
        String currText = null;
        try
        {
            int line = doc.getLineOfOffset(documentOffset);
            int start = doc.getLineOffset(line);
            int length = documentOffset - start;
            String lineOfText = doc.get(start, length);
            int lastNonUsableCharacter = -1;
            for (char c : PlSqlCompletionProcessor.autoCompleteDelimiters)
            {
                lastNonUsableCharacter = Math
                        .max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
            }
            currText = lineOfText.substring(lastNonUsableCharacter + 1).toUpperCase();

            Position dummyPosition = new Position(0);

            // check qualifiers
            if (currText.contains("@"))
            {
                int atIndex = currText.lastIndexOf("@");
                currText = currText.substring(atIndex);

                List<String> proposals = Arrays.asList(fgProposals);
                for (String str : proposals)
                {
                    if (str != null && str.toUpperCase().startsWith(currText))
                    {
                        completions.add(new Segment(str, dummyPosition, Segment.SegmentType.Label));
                    }
                }
            }
            else
            // no at
            {
                int lastPreviousUsableCharacter = lastNonUsableCharacter - 1;
                for (int i = lastPreviousUsableCharacter; i >= 0; i--)
                {
                    char c = lineOfText.charAt(i);
                    if (Character.isJavaIdentifierPart(c))
                    {
                        lastPreviousUsableCharacter = i;
                        break;
                    }
                }
                if (!addParamCompletions(documentOffset,
                                         completions,
                                         currText,
                                         lineOfText,
                                         lastNonUsableCharacter))
                {
                    if (!addThrowsCompletions(documentOffset,
                                              completions,
                                              currText,
                                              lineOfText,
                                              lastNonUsableCharacter))
                    {
                        currText = addLinkCompletions(documentOffset,
                                                      completions,
                                                      currText,
                                                      lineOfText,
                                                      lastNonUsableCharacter);
                    }
                }
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        ICompletionProposal result[] = new ICompletionProposal[completions.size()];

        int index = 0;
        int currTextLength = currText.length();

        for (Segment proposal : completions)
        {
            String proposalString = proposal.getPresentationName(true, false, true);
            String replacementString = proposalString;
            IContextInformation info = new ContextInformation(proposalString, MessageFormat
                    .format(PlSqlEditorMessages
                                    .getString("CompletionProcessor.Proposal.ContextInfo.pattern"),
                            new Object[]{proposalString}));
            result[index++] = new CompletionProposal(
                    replacementString,
                    documentOffset - currTextLength,
                    currTextLength,
                    replacementString.length(),
                    plugin.getImageRegistry().get(proposal.getType().toString()),
                    proposalString,
                    info,
                    MessageFormat
                            .format(PlSqlEditorMessages
                                            .getString("CompletionProcessor.Proposal.hoverinfo.pattern"),
                                    new Object[]{proposal}));
        }
        return result;
    }

    /**
     * This method adds the list of parameters avaiable for the currently documented procedure or
     * function to the list of possible auto <code>completions</code>.
     * 
     * @param documentOffset
     *            The current location of the cursor in the document when auto completion was
     *            instantiated.
     * 
     * @param completions
     *            The list of segment completions to which will be added any new completions
     *            discovered based on the location of the cursor and the context it is in.
     * 
     * @param currText
     *            The current piece of text preceding the cursor (back until the first non word
     *            character, listed in {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText
     *            The single line of text from the beginning of the line up until the cursor
     *            location.
     * 
     * @param lastCharacterInPreviousWord
     *            The index into the <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and <code>false</code> otherwise.
     */
    private boolean addParamCompletions(int documentOffset,
                                        List<Segment> completions,
                                        String currText,
                                        String lineOfText,
                                        int lastCharacterInPreviousWord)
    {
        int prevIndex = lastCharacterInPreviousWord - paramString.length();
        if (prevIndex >= 0)
        {
            String prevText = lineOfText.substring(prevIndex, lastCharacterInPreviousWord);
            if (prevText.equals(paramString))
            {
                Segment foundSegment = null;
                for (Segment s : myCurrentSegments)
                {
                    // assumes the segments are in order of location in text
                    if (s.getPosition().getOffset() > documentOffset)
                    {
                        foundSegment = s;
                        break;
                    }
                }
                if (foundSegment != null)
                {
                    for (Segment p : foundSegment.getParameterList())
                    {
                        if (p.getName().toUpperCase().startsWith(currText))
                        {
                            completions.add(p);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * This method adds the list of exceptions thrown by the currently documented procedure or
     * function to the list of possible auto <code>completions</code>.
     * 
     * @param documentOffset
     *            The current location of the cursor in the document when auto completion was
     *            instantiated.
     * 
     * @param completions
     *            The list of segment completions to which will be added any new completions
     *            discovered based on the location of the cursor and the context it is in.
     * 
     * @param currText
     *            The current piece of text preceding the cursor (back until the first non word
     *            character, listed in {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText
     *            The single line of text from the beginning of the line up until the cursor
     *            location.
     * 
     * @param lastCharacterInPreviousWord
     *            The index into the <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and <code>false</code> otherwise.
     */
    private boolean addThrowsCompletions(int documentOffset,
                                         List<Segment> completions,
                                         String currText,
                                         String lineOfText,
                                         int lastCharacterInPreviousWord)
    {
        int prevIndex = lastCharacterInPreviousWord - throwsString.length();
        if (prevIndex >= 0)
        {
            String prevText = lineOfText.substring(prevIndex, lastCharacterInPreviousWord);
            if (prevText.equals(throwsString))
            {
                // don't know
                return true;
            }
        }
        return false;
    }

    /**
     * This method adds the standard auto complete list of completions to the list of
     * <code>completions</code> 
     * 
     * @param documentOffset
     *            The current location of the cursor in the document when auto completion was
     *            instantiated.
     * 
     * @param completions
     *            The list of segment completions to which will be added any new completions
     *            discovered based on the location of the cursor and the context it is in.
     * 
     * @param currText
     *            The current piece of text preceding the cursor (back until the first non word
     *            character, listed in {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText
     *            The single line of text from the beginning of the line up until the cursor
     *            location.
     * 
     * @param lastCharacterInPreviousWord
     *            The index into the <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and <code>false</code> otherwise.
     */
    private String addLinkCompletions(int documentOffset,
                                      List<Segment> completions,
                                      String currText,
                                      String lineOfText,
                                      int lastCharacterInPreviousWord)
    {
        for (String linkString : linkStrings)
        {
            int prevIndex = lastCharacterInPreviousWord - linkString.length();
            if (prevIndex >= 0)
            {
                String prevText = lineOfText.substring(prevIndex, lastCharacterInPreviousWord);
                if (prevText.equals(linkString))
                {
                    return myPlSqlCompletor.computeCompletionSegments(completions,
                                                                      documentOffset,
                                                                      lineOfText,
                                                                      lastCharacterInPreviousWord,
                                                                      myCurrentDoc);
                }
            }
        }
        return currText;
    } /*
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
