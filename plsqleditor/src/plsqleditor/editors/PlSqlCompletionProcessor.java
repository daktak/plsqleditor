/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.Segment;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */
public class PlSqlCompletionProcessor implements IContentAssistProcessor
{
    public static char [] autoCompleteDelimiters = new char [] {' ', '\t', '(', ';'};

    private static final String[]          fgProposals;
    private static final SortedSet<String> ACS;

    static
    {
        ACS = new TreeSet<String>();

        for (String s : PlSqlCodeScanner.CONSTANTS)
        {
            ACS.add(s);
        }
        for (String s : PlSqlCodeScanner.DATATYPES)
        {
            ACS.add(s);
        }
        for (String s : PlSqlCodeScanner.KEYWORDS)
        {
            ACS.add(s);
        }

        fgProposals = ACS.toArray(new String[ACS.size()]);
    }

    protected IContextInformationValidator fValidator;

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


    public PlSqlCompletionProcessor()
    {
        fValidator = new Validator();
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        try
        {
            IDocument doc = viewer.getDocument();
            int line = doc.getLineOfOffset(documentOffset);
            int start = doc.getLineOffset(line);
            int length = documentOffset - start;
            String lineOfText = doc.get(start, length);
            int lastNonUsableCharacter = -1;
            for (char c : autoCompleteDelimiters)
            {
                lastNonUsableCharacter = Math.max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
            }

            String currText = lineOfText.substring(lastNonUsableCharacter + 1).toUpperCase();
            List<Segment> completions = new ArrayList<Segment>();

            PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
            List<Segment> thisDocsSegments = plugin.getSegments(doc);

            Position dummyPosition = new Position(0);

            // check qualifiers
            if (currText.contains("."))
            {
                int lastDotIndex = currText.lastIndexOf(".");
                String prior = currText.substring(0, lastDotIndex).toLowerCase();
                currText = currText.substring(lastDotIndex + 1);
                if (prior.contains("."))
                {
                    // currText is text after first dot
                    int index = prior.lastIndexOf(".");
                    String schema = prior.substring(0, index);
                    String packageName = prior.substring(index + 1);
                    for (Segment segment : plugin.getSegments(schema, packageName))
                    {
                        checkSegment(documentOffset, currText, completions, segment, false);
                    }
                }
                else
                {
                    // only one dot, could be schema or package name
                    // currText is text after the dot
                    String schema = null;
                    String packageName = null;
                    List<String> schemas = plugin.getSchemas();
                    if (schemas.contains(prior))
                    {
                        schema = prior;
                        for (String str : plugin.getPackages(schema))
                        {
                            if (str != null && str.toUpperCase().startsWith(currText))
                            {
                                completions.add(new Segment(str, dummyPosition,
                                        Segment.SegmentType.Package));
                            }
                        }
                    }
                    else
                    {
                        schema = plugin.getCurrentSchema();
                        packageName = prior;
                        if (packageName != null)
                        {
                            for (Segment segment : plugin.getSegments(schema, packageName))
                            {
                                checkSegment(documentOffset, currText, completions, segment, false);
                            }
                        }
                    }
                }
            }
            else
            // no dots
            {
                for (String str : plugin.getSchemas())
                {
                    if (str != null && str.toUpperCase().startsWith(currText))
                    {
                        completions
                                .add(new Segment(str, dummyPosition, Segment.SegmentType.Schema));
                    }
                }
                String schema = plugin.getCurrentSchema();
                if (schema != null)
                {
                    for (String str : plugin.getPackages(schema))
                    {
                        if (str.toUpperCase().startsWith(currText))
                        {
                            completions
                                    .add(new Segment(str, dummyPosition, Segment.SegmentType.Package));
                        }
                    }
                }
                for (Segment segment : thisDocsSegments)
                {
                    checkSegment(documentOffset, currText, completions, segment, true);
                }
                for (String string : ACS)
                {
                    Segment segment = new Segment(string, dummyPosition, Segment.SegmentType.Label);
                    if (string.startsWith(currText))
                    {
                        completions.add(segment);
                    }
                }
            }

            ICompletionProposal result[] = new ICompletionProposal[completions.size()];
            int index = 0;
            int currTextLength = currText.length();

            for (Segment proposal : completions)
            {
                String proposalString = proposal.getPresentationName(true, true, true);
                String replacementString = proposalString.replaceAll(" IN OUT ", " => ");
                replacementString = replacementString.replaceAll(" IN ", " => ");
                replacementString = replacementString.replaceAll(" OUT ", " => ");
                replacementString = replacementString.replaceAll(" : .*", "");
                IContextInformation info = new ContextInformation(
                        proposalString,
                        MessageFormat
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
        catch (BadLocationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void checkSegment(int documentOffset,
                              String currText,
                              List<Segment> completions,
                              Segment segment,
                              boolean addLocals)
    {
        if (segment.getName().toUpperCase().startsWith(currText))
        {
            completions.add(segment);
        }
        if (addLocals)
        {
            addLocals(documentOffset, currText, completions, segment);
        }
    }

    private void addLocals(int documentOffset,
                           String currText,
                           List<Segment> completions,
                           Segment segment)
    {
        if (segment.contains(documentOffset))
        {
            for (Segment local : segment.getFieldList())
            {
                if (local.getName().toUpperCase().startsWith(currText))
                {
                    completions.add(local);
                }
            }
            for (Segment parameter : segment.getParameterList())
            {
                if (parameter.getName().toUpperCase().startsWith(currText))
                {
                    completions.add(parameter);
                }
            }
        }
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        IContextInformation result[] = new IContextInformation[5];
        for (int i = 0; i < result.length; i++)

            result[i] = new ContextInformation(MessageFormat.format(PlSqlEditorMessages
                    .getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[]{
                    new Integer(i), new Integer(documentOffset)}), MessageFormat
                    .format(PlSqlEditorMessages
                                    .getString("CompletionProcessor.ContextInfo.value.pattern"),
                            new Object[]{new Integer(i), new Integer(documentOffset - 5),
                                    new Integer(documentOffset + 5)}));

        return result;
    }

    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return (new char[]{'.'});
    }

    public char[] getContextInformationAutoActivationCharacters()
    {
        return (new char[]{'#'});
    }

    public IContextInformationValidator getContextInformationValidator()
    {
        return fValidator;
    }

    public String getErrorMessage()
    {
        return null;
    }

    /**
     * This method returns the fgProposals.
     * 
     * @return {@link #fgProposals}.
     */
    protected static String[] getFgProposals()
    {
        return fgProposals;
    }
}
