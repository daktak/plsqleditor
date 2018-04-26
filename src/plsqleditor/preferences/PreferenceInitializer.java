package plsqleditor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.IPlSqlColorConstants;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = PlsqleditorPlugin.getDefault().getPreferenceStore();
//        store.setDefault(PreferenceConstants.P_BACKGROUND_COLOUR, convert(IPlSqlColorConstants.WHITE));
//        store.setDefault(PreferenceConstants.P_COMMENT_COLOUR, convert(IPlSqlColorConstants.SINGLE_LINE_COMMENT));
//        store.setDefault(PreferenceConstants.P_CONSTANT_COLOUR, convert(IPlSqlColorConstants.CONSTANT));
//        store.setDefault(PreferenceConstants.P_JAVADOC_COLOUR, convert(IPlSqlColorConstants.MULTI_LINE_COMMENT));
//        store.setDefault(PreferenceConstants.P_KEYWORD_COLOUR, convert(IPlSqlColorConstants.KEYWORD));
//        store.setDefault(PreferenceConstants.P_OPERATOR_COLOUR, convert(IPlSqlColorConstants.OPERATOR));
//        store.setDefault(PreferenceConstants.P_STRING_COLOUR, convert(IPlSqlColorConstants.STRING));
//        store.setDefault(PreferenceConstants.P_TYPE_COLOUR, convert(IPlSqlColorConstants.TYPE));
        store.setDefault(PreferenceConstants.P_EDITOR_TAB_WIDTH, 4);

        store.setDefault(PreferenceConstants.P_DRIVER,"oracle.jdbc.driver.OracleDriver");
        store.setDefault(PreferenceConstants.P_URL,"jdbc:oracle:thin:@localhost:1521:SID");
        store.setDefault(PreferenceConstants.P_USER,"<dbaUserNameHere>");
        //store.setDefault(PreferenceConstants.P_PASSWORD,"");
        store.setDefault(PreferenceConstants.P_INIT_CONNS,1);
        store.setDefault(PreferenceConstants.P_MAX_CONNS,1);
        store.setDefault(PreferenceConstants.P_NUM_RESULT_SET_ROWS,200);
        store.setDefault(PreferenceConstants.P_AUTO_COMMIT_ON_CLOSE,false);
        store.setDefault(PreferenceConstants.P_SCHEMA_BROWSER_FILTER_LIST,"\\w*SYS,SYSMAN,SYSTEM,XDB,DBSNMP,\\w+PLUGINS");
        store.setDefault(MarkOccurrencesPreferences.MARK_OCCURRENCES, true);
        PreferenceConstants.initializeDefaultValues(store);
    }

    private String convert(RGB rgb)
    {
        return rgb.red + "," + rgb.green + "," + rgb.blue;
    }

}
