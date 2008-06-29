package plsqleditor.objects;

public interface IMember extends IPlSqlElement
{

    ISourceRange getNameRange() throws PlSqlModelException;
}
