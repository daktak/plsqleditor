package plsqleditor.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class PlSqlDocumentProvider extends FileDocumentProvider
{
    protected IDocument createDocument(Object element) throws CoreException
    {
        IDocument document = super.createDocument(element);
        if (document != null)
        {
            IDocumentPartitioner partitioner = new FastPartitioner(new PlSqlPartitionScanner(),
                    new String[]{PlSqlPartitionScanner.PLSQL_MULTILINE_COMMENT,
                PlSqlPartitionScanner.PL_DOC});
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return document;
    }
}
