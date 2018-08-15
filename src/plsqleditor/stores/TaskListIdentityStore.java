package plsqleditor.stores;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

public class TaskListIdentityStore
{
    private static TaskListIdentityStore theInstance;
    private Map<String,Integer> myTodoMarkers;
    private TaskListIdentityStore()
    {
        myTodoMarkers = new HashMap<String,Integer>();
        myTodoMarkers.put("TODO", Integer.valueOf(IMarker.PRIORITY_NORMAL));
        myTodoMarkers.put("FIXME", Integer.valueOf(IMarker.PRIORITY_HIGH));
    }

    public static TaskListIdentityStore instance()
    {
        if (theInstance == null)
        {
            theInstance = new TaskListIdentityStore();
        }
        return theInstance;
    }

    public Map<String,Integer> getMarkers()
    {
        return myTodoMarkers;
    }
}
