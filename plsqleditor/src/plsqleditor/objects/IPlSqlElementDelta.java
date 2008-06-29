package plsqleditor.objects;

public interface IPlSqlElementDelta
{

    int F_CONTENT = 0;
    int F_PRIMARY_RESOURCE = 1;
    IPlSqlElement getElement();
    IPlSqlElementDelta[] getAffectedChildren();
}
