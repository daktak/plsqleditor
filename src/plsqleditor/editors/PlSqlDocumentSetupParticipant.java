/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import plsqleditor.PlsqleditorPlugin;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @deprecated Not required any more
 */
public class PlSqlDocumentSetupParticipant implements IDocumentSetupParticipant
{

    public PlSqlDocumentSetupParticipant()
    {
        //
    }

    public void setup(IDocument document)
    {
        if (document instanceof IDocumentExtension3)
        {
            IDocumentExtension3 extension3 = (IDocumentExtension3) document;
            IDocumentPartitioner partitioner = new FastPartitioner(PlsqleditorPlugin
                    .getDefault().getPlSqlPartitionScanner(),
                    PlSqlPartitionScanner.PLSQL_PARTITION_TYPES);
            extension3.setDocumentPartitioner(PlsqleditorPlugin.PLSQL_PARTITIONING, partitioner);
            partitioner.connect(document);
        }
    }
}
