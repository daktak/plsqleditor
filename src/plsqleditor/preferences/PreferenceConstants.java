package plsqleditor.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Constant definitions for the plsqleditor plug-in preferences.
 */
public class PreferenceConstants
{
	public static final String P_BUILT_IN_FUNCS_COLOUR = "builtInFuncsColour";

	public static final String P_IS_SHOWING_PARAMETER_IN_OUT = "isShowingParameterInOut";

	public static final String P_IS_SHOWING_PARAMETER_TYPE = "isShowingParameterType";

	public static final String P_IS_SHOWING_PARAMETER_NAME = "isShowingParameterName";

	public static final String P_IS_SHOWING_PARAMETER_LIST = "isShowingParameterList";

	public static final String P_IS_SHOWING_RETURN_TYPE = "isShowingReturnType";

	public static final String P_PARAMETERS_ON_NEWLINE = "parametersOnNewline";
	public static final String P_FIRST_PARAMETER_ON_NEWLINE = "firstParameterOnNewline";
	public static final String P_COMMA_ON_NEWLINE = "commaOnNewline";
	public static final String P_METHOD_SEMI_COLON_AT_END = "semiColonAtEndOfMethods";
	public static final String P_METHOD_ALIGN_ARROWS = "alignMethodAssignmentArrows";
	public static final String P_METHOD_USE_PARAM_NAMES_NOT_TYPES = "useParamterNamesInsteadOfTypes";

	public static final String P_LOWERCASE_KEYWORDS = "lowerCaseKeywords";

	public static final String P_CONTENT_ASSIST_AUTO_INSERT = "contentAssistAutoInsertSingleMethod";
	public static final String P_CONTENT_ASSIST_AUTO_ACTIVATION = "contentAssistAutoActivation";

	public static final String P_DRIVER = "dbDriver";
	public static final String P_URL = "dbUrl";
	public static final String P_USER = "dbUser";
	public static final String P_PASSWORD = "dbPassword";
	public static final String P_USE_LOCAL_CLIENT = "useLocalClient";
	public static final String P_SQLPLUS_EXECUTABLE = "sqlPlusExecutable";
	public static final String P_SQLPLUS_INTERPRETER_TYPE = "sqlPlusInterpreterType";
	/**
	 * One of the allowed values for P_SQLPLUS_INTERPRETER_TYPE
	 */
	public static final String P_SQLPLUS_INTERPRETER_TYPE_STANDARD = "Standard";

	/**
	 * One of the allowed values for P_SQLPLUS_INTERPRETER_TYPE
	 */
	public static final String P_SQLPLUS_INTERPRETER_TYPE_CYGWIN = "Cygwin";

	public static final String P_INIT_CONNS = "dbInitialConnections";
	public static final String P_MAX_CONNS = "dbMaxConnections";

	public static final String P_AUTO_COMMIT_ON_CLOSE = "isAutoCommittingOnClose";
	public static final String P_NUM_RESULT_SET_ROWS = "numResultSetRows";

	public static final String P_SCHEMA_PACKAGE_DELIMITER = "schemaPackageDelimiter";
	public static final String P_EDITOR_TAB_WIDTH = "plsqlTabWidth";
	public static final String P_SCHEMA_BROWSER_FILTER_LIST = "plsqlSchemaBrowserFilterList";

	public static final String P_PROJECT_ROOT = "projectRoot";

	public static final String USE_LOCAL_DB_SETTINGS = "UseLocalDbSettings";

	// pl doc settings
	public static final String P_PLDOC_OVERVIEW = "PlDocOverview";
	public static final String P_PLDOC_DOCTITLE = "PlDocTitle";
	public static final String P_PLDOC_OUTPUT_DIRECTORY = "PlDocOutputDirectory";
	public static final String P_PLDOC_EXITONERROR = "PlDocExitOnError";
	public static final String P_PLDOC_NAMECASE = "PlDocNameCase";
	public static final String C_NAMECASE_UPPER = "upper";
	public static final String C_NAMECASE_LOWER = "lower";
	public static final String C_NAMECASE_NEITHER = "neither";
	public static final String P_PLDOC_OUTPUT_DIR_USE = "PlDocOutputDirectoryUsage";
	public static final String C_OUTPUTDIR_ABSOLUTE = "absolute";
	public static final String C_OUTPUTDIR_FS_RELATIVE = "filesystemRelative";
	public static final String C_OUTPUTDIR_PROJECT_RELATIVE = "projectRelative";
	public static final String P_PLDOC_DEFINESFILE = "PlDocDefinesFile";
	public static final String P_PLDOC_STYLESHEETFILE = "PlDocStyleSheetFile";
	public static final String P_PLDOC_PATH = "PlDocPath";
	public static final String P_PLDOC_EXTRA_PARAMS = "PlDocExtraParams";

	public static final String P_HDR_GENERATION_USE = "headerGenerationUse";
	public static final String C_HDR_GENERATION_DONT_CHECK = "headerGenerationDontCheck";
	public static final String C_HDR_GENERATION_ALWAYS_SAVE = "headerGenerationAlwaysSave";
	public static final String C_HDR_GENERATION_PROMPT_FOR_SAVE = "headerGenerationPromptForSave";

	public static final String P_LOAD_TO_DB_USE = "loadToDatabaseUse";
	public static final String C_LOAD_TO_DB_DONT_CHECK = "loadToDatabaseDontCheck";
	public static final String C_LOAD_TO_DB_ALWAYS_SAVE = "loadToDatabaseAlwaysSave";
	public static final String C_LOAD_TO_DB_PROMPT_FOR_SAVE = "loadToDatabasePromptForSave";

	public static final String EDITOR_LINE_WRAP = "editorLineWrap";

	public static final String EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE = "editorSyncOutlineOnCursorMove";

	public static final String SOURCE_FOLDING = "sourceFolding";

	public static final String PLDOC_FOLDING = "plDocFolding";

	public static final String METHOD_FOLDING = "methodFolding";

	/**
	 * A named preference that controls whether bracket matching highlighting is
	 * turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight matching
	 * brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$

	public static final String AUTO_COMPLETION_QUOTE1 = "autoCompleteSingleQuotes";

	public static final String AUTO_COMPLETION_QUOTE2 = "autoCompleteDoubleQuotes";

	public static final String AUTO_COMPLETION_BRACKET1 = "autoCompleteSquareBrackets";

	public static final String AUTO_COMPLETION_BRACKET3 = "autoCompleteRoundBrackets";

	public static final String EDITOR_SMART_HOME_END = "useSmartHomeEnd";

	/**
	 * A named preference that holds the color used as the text foreground. This
	 * value has not effect if the system default color is used.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_FOREGROUND_COLOR = AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND;

	/**
	 * A named preference that describes if the system default foreground color
	 * is used as the text foreground.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_FOREGROUND_DEFAULT_COLOR = AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;

	/**
	 * A named preference that holds the color used as the text background. This
	 * value has not effect if the system default color is used.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String P_BACKGROUND_COLOUR = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;

	/**
	 * A named preference that describes if the system default background color
	 * is used as the text background.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_BACKGROUND_DEFAULT_COLOR = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;

	/**
	 * Preference key suffix for bold text style preference keys.
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_BOLD_SUFFIX = "Bold";

	public static final String P_KEYWORD_COLOUR = "keyWordColour";
	public static final String P_STRING_COLOUR = "stringColour";
	public static final String P_COMMENT_COLOUR = "commentColour";
	public static final String P_TYPE_COLOUR = "typeColour";
	public static final String P_CONSTANT_COLOUR = "constantColour";
	public static final String P_OPERATOR_COLOUR = "operatorColour";
	public static final String P_DEFAULT_CODE_COLOUR = "defaultForegroundColour";
	public static final String P_JAVADOC_COLOUR = "javadocColour";

	public static final String P_STRING_COLOUR_BOLD = P_STRING_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_KEYWORD_COLOUR_BOLD = P_KEYWORD_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_TYPE_COLOUR_BOLD = P_TYPE_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_COMMENT_COLOUR_BOLD = P_COMMENT_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_JAVADOC_COLOUR_BOLD = P_JAVADOC_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_CONSTANT_COLOUR_BOLD = P_CONSTANT_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_DEFAULT_CODE_COLOUR_BOLD = P_DEFAULT_CODE_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_OPERATOR_COLOUR_BOLD = P_OPERATOR_COLOUR
			+ EDITOR_BOLD_SUFFIX;
	public static final String P_BUILT_IN_FUNCS_COLOUR_BOLD = P_BUILT_IN_FUNCS_COLOUR
			+ EDITOR_BOLD_SUFFIX;

	private static final Object[] DEFAULT_COLORS = {
			EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192),
			P_STRING_COLOUR, new RGB(0, 0, 0), P_KEYWORD_COLOUR,
			new RGB(160, 32, 240), P_DEFAULT_CODE_COLOUR, new RGB(0, 0, 0),
			P_COMMENT_COLOUR, new RGB(178, 0, 34), P_TYPE_COLOUR,
			new RGB(178, 34, 0), P_CONSTANT_COLOUR, new RGB(0, 0, 255),
			P_JAVADOC_COLOUR, new RGB(178, 0, 34), P_OPERATOR_COLOUR,
			new RGB(178, 34, 0), P_BUILT_IN_FUNCS_COLOUR, new RGB(150,150,0)};

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store
	 *            the preference store to be initialized
	 */
	public static void initializeDefaultValues(IPreferenceStore store)
	{
		store.setDefault(EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);
		store.setDefault(EDITOR_SMART_HOME_END, true);
		store.setDefault(EDITOR_LINE_WRAP, false);
		store.setDefault(SOURCE_FOLDING, true);
		store.setDefault(PLDOC_FOLDING, false);
		store.setDefault(METHOD_FOLDING, false);
		store.setDefault(AUTO_COMPLETION_QUOTE1, true);
		store.setDefault(AUTO_COMPLETION_QUOTE2, true);
		store.setDefault(AUTO_COMPLETION_BRACKET1, true);
		// store.setDefault(AUTO_COMPLETION_BRACKET2, true);
		store.setDefault(AUTO_COMPLETION_BRACKET3, true);
		// store.setDefault(AUTO_COMPLETION_BRACKET4, true);
		store.setDefault(EDITOR_MATCHING_BRACKETS, true);

		for (int i = 0; i < DEFAULT_COLORS.length; i += 2)
		{
			PreferenceConverter.setDefault(store, (String) DEFAULT_COLORS[i],
					(RGB) DEFAULT_COLORS[i + 1]);
		}

		store.setDefault(P_STRING_COLOUR_BOLD, false);
		store.setDefault(P_KEYWORD_COLOUR_BOLD, false);
		store.setDefault(P_COMMENT_COLOUR_BOLD, false);
		store.setDefault(P_TYPE_COLOUR_BOLD, false);
		store.setDefault(P_JAVADOC_COLOUR_BOLD, true);
		store.setDefault(P_CONSTANT_COLOUR_BOLD, false);
		store.setDefault(P_DEFAULT_CODE_COLOUR_BOLD, false);
		store.setDefault(P_OPERATOR_COLOUR_BOLD, false);
		store.setDefault(P_BUILT_IN_FUNCS_COLOUR_BOLD, false);
	}
}
