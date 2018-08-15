package plsqleditor.doc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlCompletionProcessor;
import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.template.PlDocContextType;
import plsqleditor.template.TemplateEditorUI;
import plsqleditor.template.TemplateEngine;

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
    private TemplateEngine           myTemplateEngine;
    private Validator                myValidator;

    protected static class Validator
            implements
                IContextInformationValidator,
                IContextInformationPresenter
    {
        protected int fInstallOffset;

        protected Validator()
        {
            //
        }

        public boolean isContextInformationValid(int offset)
        {
            return Math.abs(fInstallOffset - offset) < 5;
        }

        public void install(IContextInformation info, ITextViewer viewer, int offset)
        {
            fInstallOffset = offset;
        }

        public boolean updatePresentation(int documentPosition, TextPresentation presentation)
        {
            return false;
        }
    }

    public PlSqlDocCompletionProcessor()
    {
        myValidator = new Validator();
        TemplateContextType contextType = TemplateEditorUI.getDefault().getContextTypeRegistry()
                .getContextType(PlDocContextType.PLDOC_CONTEXT_TYPE);
        if (contextType == null)
        {
            contextType = new PlDocContextType();
            TemplateEditorUI.getDefault().getContextTypeRegistry().addContextType(contextType);
        }
        if (contextType != null)
        {
            myTemplateEngine = new TemplateEngine(contextType);
        }
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        IDocument doc = viewer.getDocument();
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        myCurrentDoc = doc;
        myCurrentSegments = plugin.getCurrentSegments(doc);
        List<Segment> completions = new ArrayList<Segment>();
        String currText = null;
        try
        {
            int line = doc.getLineOfOffset(documentOffset);
            int start = doc.getLineOffset(line);
            int length = documentOffset - start;
            String lineOfText = doc.get(start, length);
            int lastNonUsableCharacter = -1;
            for (int i = 0; i < PlSqlCompletionProcessor.autoCompleteDelimiters.length; i++)
            {
                char c = PlSqlCompletionProcessor.autoCompleteDelimiters[i];
                lastNonUsableCharacter = Math
                        .max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
            }
            currText = lineOfText.substring(lastNonUsableCharacter + 1).toUpperCase();

            Position dummyPosition = new Position(0);

            // check qualifiers
            if (currText.indexOf("@") != -1)
            {
                int atIndex = currText.lastIndexOf("@");
                currText = currText.substring(atIndex);

                List<String> proposals = new ArrayList<String>();
                proposals.addAll(Arrays.asList(fgProposals));
                for (Iterator<String> it = proposals.iterator(); it.hasNext();)
                {
                    String str = (String) it.next();
                    if (str != null && str.toUpperCase().startsWith(currText))
                    {
                        completions.add(new Segment(str, dummyPosition, SegmentType.Label));
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

        for (Iterator<Segment> it = completions.iterator(); it.hasNext();)
        {
            Segment proposal = (Segment) it.next();
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
        if (myTemplateEngine != null)
        {
            myTemplateEngine.reset();
            myTemplateEngine.complete(viewer, documentOffset);
            TemplateProposal templateResults[] = myTemplateEngine.getResults();

            ICompletionProposal total[] = new ICompletionProposal[result.length
                    + templateResults.length];
            System.arraycopy(templateResults, 0, total, 0, templateResults.length);
            System.arraycopy(result, 0, total, templateResults.length, result.length);
            result = total;
        }
        return result;
    }

    /**
     * This method adds the list of parameters avaiable for the currently
     * documented procedure or function to the list of possible auto
     * <code>completions</code>.
     * 
     * @param documentOffset The current location of the cursor in the document
     *            when auto completion was instantiated.
     * 
     * @param completions The list of segment completions to which will be added
     *            any new completions discovered based on the location of the
     *            cursor and the context it is in.
     * 
     * @param currText The current piece of myOutputText preceding the cursor
     *            (back until the first non word character, listed in
     *            {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText The single line of myOutputText from the beginning of
     *            the line up until the cursor location.
     * 
     * @param lastCharacterInPreviousWord The index into the
     *            <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and
     *         <code>false</code> otherwise.
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
                Segment foundSegment = PlSqlParserManager.instance()
                        .findNextMethod(myCurrentSegments, documentOffset);
                if (foundSegment != null)
                {
                    for (Iterator<?> it = foundSegment.getParameterList().iterator(); it.hasNext();)
                    {
                        Segment p = (Segment) it.next();
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
     * This method adds the list of exceptions thrown by the currently
     * documented procedure or function to the list of possible auto
     * <code>completions</code>.
     * 
     * @param documentOffset The current location of the cursor in the document
     *            when auto completion was instantiated.
     * 
     * @param completions The list of segment completions to which will be added
     *            any new completions discovered based on the location of the
     *            cursor and the context it is in.
     * 
     * @param currText The current piece of myOutputText preceding the cursor
     *            (back until the first non word character, listed in
     *            {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText The single line of myOutputText from the beginning of
     *            the line up until the cursor location.
     * 
     * @param lastCharacterInPreviousWord The index into the
     *            <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and
     *         <code>false</code> otherwise.
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
     * This method adds the standard auto complete list of completions to the
     * list of <code>completions</code>
     * 
     * @param documentOffset The current location of the cursor in the document
     *            when auto completion was instantiated.
     * 
     * @param completions The list of segment completions to which will be added
     *            any new completions discovered based on the location of the
     *            cursor and the context it is in.
     * 
     * @param currText The current piece of myOutputText preceding the cursor
     *            (back until the first non word character, listed in
     *            {@link PlSqlCompletionProcessor#autoCompleteDelimiters}.
     * 
     * @param lineOfText The single line of myOutputText from the beginning of
     *            the line up until the cursor location.
     * 
     * @param lastCharacterInPreviousWord The index into the
     *            <code>lineOfText</code> of the last character of the word
     *            previous to the <code>currText</code>.
     * 
     * @return <code>true</code> if any completions were added and
     *         <code>false</code> otherwise.
     */
    private String addLinkCompletions(int documentOffset,
                                      List<Segment> completions,
                                      String currText,
                                      String lineOfText,
                                      int lastCharacterInPreviousWord)
    {
        for (int i = 0; i < linkStrings.length; i++)
        {
            String linkString = linkStrings[i];
            int prevIndex = lastCharacterInPreviousWord - linkString.length();
            if (prevIndex >= 0)
            {
                String prevText = lineOfText.substring(prevIndex, lastCharacterInPreviousWord);
                if (prevText.equals(linkString))
                {
                    currText = lineOfText.substring(lastCharacterInPreviousWord + 1).toUpperCase();
                    currText = myPlSqlCompletor
                            .computeCompletionSegments(completions,
                                                       currText,
                                                       documentOffset,
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
        return myValidator;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public String getErrorMessage()
    {
        return null;
    }
}
