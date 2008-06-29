package plsqleditor.objects;

public class ElementChangedEvent
{

    public static final int POST_CHANGE = 1;
    public static final int POST_RECONCILE = 2;

    public ElementChangedEvent(IPlSqlElementDelta deltaToNotify, int eventType)
    {
        // TODO Auto-generated constructor stub
    }

    public IPlSqlElementDelta getDelta()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
