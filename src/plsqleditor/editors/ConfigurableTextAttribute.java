/*
 * Created on 27/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 27/02/2005
 */
public class ConfigurableTextAttribute extends TextAttribute
{
    private String myForegroundString;
    //private String myBackGroundString;
    private int myStyle;
	private IPreferenceStore myStore;

    /**
     * This method
     * 
     * @return The preferences store.
     */
    private IPreferenceStore prefs()
    {
        return myStore;
    }

    static ColorManager cm()
    {
        return PlsqleditorPlugin.getDefault().getPlSqlColorProvider();
    }

    /**
     * @param foreground
     * @param background
     * @param style
     */
    public ConfigurableTextAttribute(IPreferenceStore store, String foreground, String background, int style)
    {
        super(cm().getColor(PreferenceConverter.getColor(store, foreground)), null,
        		/*cm().getColor(PreferenceConverter.getColor(store, background)),*/ style);
        myForegroundString = foreground;
        //myBackGroundString = background;
        myStore = store;
        myStyle = style;
    }

    /**
     * Returns the attribute's foreground color.
     * 
     * @return the attribute's foreground color
     */
    public Color getForeground()
    {
        return cm().getColor(PreferenceConverter.getColor(prefs(), myForegroundString));
    }

    /**
     * Returns the attribute's background color.
     * 
     * @return the attribute's background color
     */
//    public Color getBackground()
//    {
//        return cm().getColor(PreferenceConverter.getColor(prefs(), myBackGroundString));
//    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.TextAttribute#getStyle()
	 */
	@Override
	public int getStyle()
	{
		if (myStyle == -1)
		{
	    	int style = SWT.NORMAL;
	        String boldPref = myForegroundString + PreferenceConstants.EDITOR_BOLD_SUFFIX;
	        if (prefs().getBoolean(boldPref))
	        {
	        	style = SWT.BOLD;
	        }
	        return style;
		}
		return myStyle;
	}
}
