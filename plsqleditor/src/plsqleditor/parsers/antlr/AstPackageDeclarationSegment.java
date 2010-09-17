/**
 * 
 */
package plsqleditor.parsers.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

/**
 * @author Toby Zines
 * 
 */
public class AstPackageDeclarationSegment extends AstDeclarationSegment
{
    private static final String PackageNameSegmentName = "PACKAGE_NAME";
    public AstPackageDeclarationSegment(Tree tree, CommonTokenStream stream)
    {
        super(tree, "CREATE_PACKAGE", stream);
        int childCount = getTree().getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            Tree child = getTree().getChild(i);
            String childText = child.getText();
            if (childText !=  null && childText.equals(PackageNameSegmentName))
            {
                setName(child.getChild(0).getText());
            }
        }
    }
    @Override
    protected String getSegmentNameNodeName()
    {
        return PackageNameSegmentName;
    }

}
