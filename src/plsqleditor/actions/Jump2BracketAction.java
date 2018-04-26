package plsqleditor.actions;

import org.eclipse.jface.action.Action;

import plsqleditor.editors.PlSqlEditor;


public class Jump2BracketAction extends Action
{
    
    private PlSqlEditor myEditor;


	public Jump2BracketAction(PlSqlEditor editor)
    {
        myEditor = editor;
        setId(getPlsqlEditorId());
    }

    
    public static String getPlsqlEditorId()
	{
		return "plsqleditor.actions.Jump2BracketAction";
	}


	protected void doRun()
    {
        myEditor.jumpToMatchingBracket();
    }
}
