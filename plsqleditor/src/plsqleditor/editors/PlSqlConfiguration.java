package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.doc.PlSqlDocAutoEditStrategy;
import plsqleditor.doc.PlSqlDocCompletionProcessor;
import plsqleditor.doc.PlSqlDocScanner;
import plsqleditor.preferences.PreferenceConstants;

public class PlSqlConfiguration extends SourceViewerConfiguration
{
    static class SingleTokenScanner extends BufferedRuleBasedScanner
    {
        public SingleTokenScanner(TextAttribute attribute)
        {
            setDefaultReturnToken(new Token(attribute));
        }
    }

    private PlSqlDoubleClickStrategy2 doubleClickStrategy;
    private PlSqlCodeScanner          codeScanner;
    private PlSqlDocScanner           fDocScanner;

    public PlSqlConfiguration()
    {
        //
    }

    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
    {
        return PlSqlPartitionScanner.PLSQL_PARTITION_TYPES;
    }

    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
    {
        if (doubleClickStrategy == null)
        {
            doubleClickStrategy = new PlSqlDoubleClickStrategy2();
        }
        return doubleClickStrategy;
    }

    protected PlSqlCodeScanner getPlSqlCodeScanner()
    {
        if (codeScanner == null)
        {
            ColorManager provider = PlsqleditorPlugin.getDefault().getPlSqlColorProvider();
            codeScanner = new PlSqlCodeScanner(provider);
            codeScanner.setDefaultReturnToken(new Token(new TextAttribute(provider
                    .getColor(IPlSqlColorConstants.DEFAULT))));
        }
        return codeScanner;
    }

    /**
     * This method gets the pl doc scanner to use in the source configuration.
     * 
     * @return The pl doc scanner.
     */
    public RuleBasedScanner getPlDocScanner()
    {
        if (fDocScanner == null)
        {
            ColorManager provider = PlsqleditorPlugin.getDefault().getPlSqlColorProvider();
            fDocScanner = new PlSqlDocScanner(provider);
            fDocScanner.setDefaultReturnToken(new Token(new TextAttribute(provider
                    .getColor(IPlSqlColorConstants.JAVADOC_DEFAULT))));
        }
        return fDocScanner;
    }

    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return new PlSqlAnnotationHover();
    }

    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
    {
        return new IAutoEditStrategy[]{(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)
                ? new PlSqlAutoEditStrategy()
                : PlSqlPartitionScanner.PL_DOC.equals(contentType)
                        ? new PlSqlDocAutoEditStrategy()
                        : new DefaultIndentLineAutoEditStrategy())};
    }

    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
    {
        return PlsqleditorPlugin.PLSQL_PARTITIONING;
    }

    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
    {
        ContentAssistant assistant = new ContentAssistant();
        assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        assistant.setContentAssistProcessor(new PlSqlCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(new PlSqlDocCompletionProcessor(), PlSqlPartitionScanner.PL_DOC);
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(500);
        assistant.setProposalPopupOrientation(10);
        assistant.setContextInformationPopupOrientation(20);
        assistant.setContextInformationPopupBackground(PlsqleditorPlugin.getDefault().getPlSqlColorProvider()
                .getColor(new RGB(150, 150, 0)));
        return assistant;
    }

    public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType)
    {
        return IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null;
    }

    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
    {
        List<String> list = new ArrayList<String>();
        int tabWidth = 4;
        boolean useSpaces = true;
        for (int i = 0; i <= tabWidth; i++)
        {
            StringBuffer prefix = new StringBuffer();
            if (useSpaces)
            {
                for (int j = 0; j + i < tabWidth; j++)
                {
                    prefix.append(' ');
                }

                if (i != 0)
                {
                    prefix.append('\t');
                }
            }
            else
            {
                for (int j = 0; j < i; j++)
                {
                    prefix.append(' ');
                }

                if (i != tabWidth)
                {
                    prefix.append('\t');
                }
            }
            list.add(prefix.toString());
        }

        list.add("");
        return list.toArray(new String[list.size()]);
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getPlSqlCodeScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getPlDocScanner());
        // dr = new DefaultDamagerRepairer(new SingleTokenScanner(new ConfigurableTextAttribute(
        // PreferenceConstants.P_JAVADOC_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0)));
        reconciler.setDamager(dr, PlSqlPartitionScanner.PL_DOC);
        reconciler.setRepairer(dr, PlSqlPartitionScanner.PL_DOC);
        dr = new DefaultDamagerRepairer(new SingleTokenScanner(new ConfigurableTextAttribute(
                PreferenceConstants.P_COMMENT_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0)));
        reconciler.setDamager(dr, PlSqlPartitionScanner.PLSQL_MULTILINE_COMMENT);
        reconciler.setRepairer(dr, PlSqlPartitionScanner.PLSQL_MULTILINE_COMMENT);
        return reconciler;
    }

    public int getTabWidth(ISourceViewer sourceViewer)
    {
        return 4;
    }

    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
    {
        return new PlSqlTextHover();
    }
}
