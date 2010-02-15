package plsqleditor.actions;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.parsers.PackageBodyParser;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

public class FormatSourceAction extends SelectedTextAction
{
    private static PackageBodyParser theBodyParser = new PackageBodyParser();

    public FormatSourceAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        Shell shell = new Shell();

        String [] packageName = new String[1];
        List segments = null;
        try
        {
            segments = theBodyParser.parseFile(doc, packageName, new SegmentType[0]);
            for (Iterator it = segments.iterator(); it.hasNext();)
            {
                Segment seg = (Segment) it.next();
                System.out.println(seg.format());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            MessageDialog.openInformation(shell, "Toby's PL SQL Editor", e.getMessage());
        }
    }
}
