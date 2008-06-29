package plsqleditor.objects;

public interface IType
{

    boolean isMember() throws PlSqlModelException;

    IPlSqlElement getParent();
}
