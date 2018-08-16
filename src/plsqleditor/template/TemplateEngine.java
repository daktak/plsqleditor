package plsqleditor.template;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlTextHover;

/**
 * This class represents an engine for generating templates for auto completion.
 * 
 * @author Toby Zines
 */
public class TemplateEngine
{
    private static final String LINE_SELECTION = "${line_selection}";
    private static final String WORD_SELECTION = "${word_selection}";
    private TemplateContextType myContextType;
    private ArrayList<TemplateProposal>           myProposals;

    /**
     * This constructor creates the template engine with the supplied context type.
     * 
     * @param contextType
     */
    public TemplateEngine(TemplateContextType contextType)
    {
        myProposals = new ArrayList<TemplateProposal>();
        Assert.isNotNull(contextType);
        myContextType = contextType;
    }

    /**
     * This method removes all of the current proposals from {@link #myProposals}.
     */
    public void reset()
    {
        myProposals.clear();
    }

    /**
     * This method gets all the results from {@link #myProposals}.
     * 
     * @return {@link #myProposals}.
     */
    public TemplateProposal[] getResults()
    {
        return (TemplateProposal[]) myProposals.toArray(new TemplateProposal[myProposals.size()]);
    }

    /**
     * This method uses the current <code>completionPosition</code> to determine which template
     * completions are valid.
     * 
     * @param viewer The viewer of the text we will emit completions into.
     * 
     * @param completionPosition The current position of the cursor.
     */
    public void complete(ITextViewer viewer, int completionPosition)
    {
        if (!(myContextType instanceof PlSqlContextType) && !(myContextType instanceof PlDocContextType))
        {
            return;
        }
        IDocument document = viewer.getDocument();
        Point selection = viewer.getSelectedRange();
        String selectedText = null;

        IRegion region = null;

        if (selection.y != 0)
        {
            try
            {
                selectedText = document.get(selection.x, selection.y);
                region = new Region(selection.x, selection.y);
            }
            catch (BadLocationException _ex)
            {
                //
            }
        }
        else
        {
            region = PlSqlTextHover.getRegion(document, completionPosition);
            if (region == null || (region.getOffset() == 0 && region.getLength() == 0))
            {
                region = new Region(completionPosition, 0);
            }
        }
        DocumentTemplateContext context = null;
        if (myContextType instanceof PlSqlContextType)
        {
            context = ((PlSqlContextType) myContextType).createContext(document, region
                                                                       .getOffset(), region.getLength());
        }
        else if (myContextType instanceof PlDocContextType)
        {
            context = ((PlDocContextType) myContextType).createContext(document, region
                                                             .getOffset(), region.getLength());
        }
        context.setVariable(GlobalTemplateVariables.SELECTION, selectedText);
        context.setVariable(PlSqlContextType.FILE_NAME, PlsqleditorPlugin.getDefault().getCurrentFileName());

        if (selectedText == null)
        {
            try
            {
                selectedText = document.get(region.getOffset(), region.getLength());
                //selectedText = selectedText.substring(selectedText.indexOf('.') + 1);
            }
            catch (BadLocationException e)
            {
                selectedText = "";
                e.printStackTrace();
            }
        }

        Template templates[] = TemplateEditorUI.getDefault().getTemplateStore().getTemplates(myContextType.getId());
        if (selection.y == 0)
        {
            for (int i = 0; i != templates.length; i++)
            {
                if (context.canEvaluate(templates[i]))
                {
                    Template t = templates[i];
                    // fix for ignoring case
                    if (t.getName().toUpperCase().startsWith(selectedText.toUpperCase()))
                    {
                        myProposals.add(new TemplateProposal(t, context, region, PlatformUI
                                .getWorkbench().getSharedImages()
                                .getImage(ISharedImages.IMG_OBJ_FILE)));
                    }
                }
            }
        }
        else
        {
            boolean multipleLinesSelected = areMultipleLinesSelected(viewer);
            for (int i = 0; i != templates.length; i++)
            {
                Template template = templates[i];
                if (context.canEvaluate(template)
                        && template.getContextTypeId().equals(context.getContextType().getId())
                        && (!multipleLinesSelected
                                && template.getPattern().indexOf(WORD_SELECTION) != -1 || multipleLinesSelected
                                && template.getPattern().indexOf(LINE_SELECTION) != -1))
                {
                    myProposals
                            .add(new TemplateProposal(templates[i], context, region, PlatformUI
                                    .getWorkbench().getSharedImages()
                                    .getImage(ISharedImages.IMG_OBJ_FILE)));
                }
            }

        }
    }

    private boolean areMultipleLinesSelected(ITextViewer viewer)
    {
        if (viewer == null)
        {
            return false;
        }
        Point s = viewer.getSelectedRange();
        if (s.y == 0)
        {
            return false;
        }
        try
        {
            IDocument document = viewer.getDocument();
            int startLine = document.getLineOfOffset(s.x);
            int endLine = document.getLineOfOffset(s.x + s.y);
            IRegion line = document.getLineInformation(startLine);
            return startLine != endLine || s.x == line.getOffset() && s.y == line.getLength();
        }
        catch (BadLocationException _ex)
        {
            return false;
        }
    }
}
