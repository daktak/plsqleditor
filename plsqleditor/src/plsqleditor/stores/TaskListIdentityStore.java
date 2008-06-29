package plsqleditor.stores;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

public class TaskListIdentityStore
{
    private static TaskListIdentityStore theInstance;
    private Map myTodoMarkers;
    private TaskListIdentityStore()
    {
        myTodoMarkers = new HashMap();
        myTodoMarkers.put("TODO", new Integer(IMarker.PRIORITY_NORMAL));
        myTodoMarkers.put("FIXME", new Integer(IMarker.PRIORITY_HIGH));
    }

    public static TaskListIdentityStore instance()
    {
        if (theInstance == null)
        {
            theInstance = new TaskListIdentityStore();
        }
        return theInstance;
    }

    public Map getMarkers()
    {
        return myTodoMarkers;
    }
}
