/*
 * Created on 21/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.rules.NonKeywordsRules;
import plsqleditor.rules.PlSqlWhitespaceDetector;
import plsqleditor.rules.PlSqlWordDetector;


/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 21/02/2005
 */
public class PlSqlCodeScanner extends RuleBasedScanner
{
    public PlSqlCodeScanner(ColorManager provider)
    {
        IToken defaultToken = new Token(new TextAttribute(provider
                .getColor(IPlSqlColorConstants.DEFAULT)));
        IToken sqlkeyword = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_KEYWORD_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 1));
        IToken builtInFuncs = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_BUILT_IN_FUNCS_COLOUR,
                PreferenceConstants.P_BACKGROUND_COLOUR, 1));
        IToken operator = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_OPERATOR_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0));
        IToken type = new Token(new ConfigurableTextAttribute(PreferenceConstants.P_TYPE_COLOUR,
                PreferenceConstants.P_BACKGROUND_COLOUR, 0));
        IToken constants = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_CONSTANT_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0));
        IToken string = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_STRING_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0));
        IToken comment = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_COMMENT_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0));

        List rules = new ArrayList();
        rules.add(new WhitespaceRule(new PlSqlWhitespaceDetector()));

        rules.add(new EndOfLineRule("//", comment));
        rules.add(new SingleLineRule("\"", "\"", string, '\\'));

        // fix for 1439880 - Keywords as functions/procedures
        WordRule wordRule = new WordRule(new PlSqlWordDetector(true), Token.UNDEFINED);
        for (int i = 0; i < CONSTANTS.length; i++)
        {
            wordRule.addWord(CONSTANTS[i], constants);
            wordRule.addWord(CONSTANTS[i].toLowerCase(), constants);
        }
        for (int i = 0; i < DATATYPES.length; i++)
        {
            wordRule.addWord(DATATYPES[i], type);
            wordRule.addWord(DATATYPES[i].toLowerCase(), type);
        }
        for (int i = 0; i < KEYWORDS.length; i++)
        {
            wordRule.addWord(KEYWORDS[i], sqlkeyword);
            wordRule.addWord(KEYWORDS[i].toLowerCase(), sqlkeyword);
        }
        for (int i = 0; i < BUILT_IN_FUNCTIONS.length; i++)
        {
            wordRule.addWord(BUILT_IN_FUNCTIONS[i], builtInFuncs);
            wordRule.addWord(BUILT_IN_FUNCTIONS[i].toLowerCase(), builtInFuncs);
        }
        rules.add(wordRule);

        // fix for 1439880 - Keywords as functions/procedures
        WordRule dotEnabledRule = new WordRule(new PlSqlWordDetector(false), defaultToken);
        for (int i = 0; i < DOT_ENABLED_KEYWORDS.length; i++)
        {
            dotEnabledRule.addWord(DOT_ENABLED_KEYWORDS[i], sqlkeyword);
            dotEnabledRule.addWord(DOT_ENABLED_KEYWORDS[i].toLowerCase(), sqlkeyword);
        }
        rules.add(dotEnabledRule);

        NumberRule numberRule = new NumberRule(type);
        rules.add(numberRule);

        NonKeywordsRules specialRule = new NonKeywordsRules(operator);
        for (int i = 0; i < OPERATORS.length; i++)
        {
            specialRule.addWord(OPERATORS[i]);
        }
        rules.add(specialRule);

        IRule result[] = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }

    private static final String   OPERATORS[]            = {"(", ")", "!=", "%", "&", "*", "**",
            "+", "-", "/", ":=", "<", "<=", "<>", "=", ">", ">=", "||", "\""};
    public static final String    DATATYPES[]            = {"BFILE", "BINARY_INTEGER", "BLOB",
            "BOOLEAN", "CHAR", "CHAR_BASE", "CLOB", "DATE", "DECIMAL", "FLOAT", "INTEGER", "LONG",
            "MLSLABEL", "NATURAL", "NATURALN", "NCHAR", "NCLOB", "NUMBER", "NUMBERBASE",
            "NVARCHAR2", "PLS_INTEGER", "POSITIVE", "POSITIVEN", "RAW", "REAL", "ROWID",
            "SMALLINT", "VARCHAR2"                       };

    public static final String    BUILT_IN_FUNCTIONS[]   = {"CHR", "CONCAT", "INITCAP", "INSTR",
            "INSTRB", "LENGTH", "LENGTHB", "LOWER", "LPAD", "LTRIM", "REPLACE", "RPAD", "RTRIM",
            "SUBSTR", "SUBSTRB", "TRANSLATE", "TRIM", "UPPER"};

    protected static final String DOT_ENABLED_KEYWORDS[] = {"COUNT", "FIRST", "FOUND", "LAST"};
    // added fixes for 1436711 - Syntax Highlighting
    protected static final String KEYWORDS[]             = {"ACCESS", "ACTIVATE", "ADD", "ADMIN",
            "AFTER", "ALL", "ALLOCATE", "ALL_ROWS", "ALTER", "ANALYZE", "AND", "ANY", "ARRAY",
            "ARRAYLEN", "AS", "ASC", "AT", "AUDIT", "AUTHENTICATED", "AUTHORIZATION", "AUTOEXTEND",
            "AUTOMATIC", "AVG", "BACKUP", "BECOME", "BEFORE", "BEGIN", "BETWEEN", "BITMAP",
            "BLOCK", "BODY", "BULK", "BY", "CACHE", "CANCEL", "CASCADE", "CASE", "CAST", "CFILE",
            "CHAINED", "CHANGE", "CHARACTER", "CHAR_CS", "CHECK", "CHECKPOINT", "CHOOSE", "CHUNK",
            "CLEAR", "CLOSE", "CLUSTER", "COALESCE", "COLLECT", "COLUMN", "COLUMNS", "COMMENT",
            "COMMIT", "COMPATIBILITY", "COMPILE", "COMPLETE", "COMPRESS", "COMPUTE", "CONNECT",
            "CONSTANT", "CONSTRAINT", "CONSTRAINTS", "CONTENTS", "CONTINUE", "CONTROLFILE", "COST",
            "CREATE", "CURRENT", "CURRVAL", "CURSOR", "CYCLE", "DANGLING", "DATABASE", "DATAFILE",
            "DAY", "DBA", "DEALLOCATE", "DEBUG", "DEBUGOFF", "DEBUGON", "DECODE", "DECLARE",
            "DEFAULT", "DEFERRABLE", "DEFERRED", "DEFINTION", "DEGREE", "DELAY", "DELETE", "DESC",
            "DIGITS", "DIRECTORY", "DISABLE", "DISCONNECT", "DISPOSE", "DISTINCT", "DISTRIBUTED",
            "DO", "DOUBLE", "DROP", "EACH", "ELSE", "ELSIF", "ENABLE", "END", "ENFORCE", "ENTRY",
            "ESCAPE", "ESTIMATE", "EVENTS", "EXCEPTION", "EXCEPTIONS", "EXCEPTION_INIT",
            "EXCHANGE", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXIT", "EXPIRE", "EXPLAIN",
            "EXTENDS", "EXTENT", "EXTENTS", "EXTERNALLY", "EXTRACT", "FAST", "FETCH", "FILE",
            "FIRST_ROWS", "FLUSH", "FOR", "FORALL", "FORCE", "FOREIGN", "FORM", "FREELIST",
            "FREELISTS", "FROM", "FULL", "FUNCTION", "GENERIC", "GLOBAL", "GLOBAL_NAME", "GOTO",
            "GRANT", "GROUP", "GROUPS", "HASH", "HASHKEYS", "HAVING", "HEADER", "HEAP", "HOUR",
            "IDENTIFIED", "IDLE_TIME", "IF", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX",
            "INDEXED", "INDEXES", "INDICATOR", "IND_PARTITION", "INITIAL", "INITIALLY", "INITRANS",
            "INSERT", "INSTANCE", "INSTANCES", "INSTEAD", "INTERFACE", "INTERSECT", "INTERVAL",
            "INTO", "IS", "IS NULL", "ISOLATION", "ISOLATION_LEVEL", "JAVA", "KEEP", "KEY", "KILL",
            "LAYER", "LESS", "LEVEL", "LIBRARY", "LIKE", "LIMIT", "LIMITED", "LINK", "LIST", "LOB",
            "LOCAL", "LOCK", "LOGFILE", "LOGGING", "LOOP", "MASTER", "MAX", "MAXEXTENTS", "MEMBER",
            "MIN", "MINEXTENTS", "MINIMUM", "MINUS", "MINUTE", "MINVALUE", "MOD", "MODE", "MODIFY",
            "MONTH", "MOUNT", "MOVE", "MULTISET", "NATIONAL", "NCHAR_CS", "NEEDED", "NESTED",
            "NETWORK", "NEW", "NEXT", "NEXTVAL", "NLS_CALENDAR", "NLS_CHARACTERSET",
            "NLS_ISO_CURRENCY", "NLS_LANGUAGE", "NLS_NUMERIC_", "NLS_SORT", "NLS_TERRITORY",
            "NOARCHIVELOG", "NOAUDIT", "NOCACHE", "NOCOMPRESS", "NOCOPY", "NOCYCLE", "NOFORCE",
            "NOLOGGING", "NOMAXVALUE", "NOMINVALUE", "NONE", "NOORDER", "NOOVERIDE", "NOPARALLEL",
            "NORESETLOGS", "NOREVERSE", "NORMAL", "NOSORT", "NOT", "NOTHING", "NOWAIT",
            "NO_DATA_FOUND", "NULLIF", "NUMERIC", "OBJECT", "OCIROWID", "OF", "OFF", "OFFLINE",
            "OID", "OIDINDEX", "OLD", "ON", "ONLINE", "ONLY", "OPCODE", "OPAQUE", "OPEN",
            "OPERATOR", "OPTIMAL", "OPTIMIZER_GOAL", "OPTION", "OR", "ORDER", "ORGANIZATION",
            "OTHERS", "OUT", "OVERFLOW", "OWN", "PACKAGE", "PARALLEL", "PARTITION", "PASSWORD",
            "PCTFREE", "PCTINCREASE", "PCTUSED", "PERMANENT", "PLAN", "PLSQL_DEBUG", "PRAGMA",
            "PRECISION", "PRESERVE", "PRIMARY", "PRIOR", "PRIVATE", "PRIVILEGE", "PRIVILEGES",
            "PROCEDURE", "PROFILE", "PUBLIC", "PURGE", "QUEUE", "QUOTA", "RAISE", "RANGE",
            "REBUILD", "RECORD", "RECOVER", "RECOVERABLE", "RECOVERY", "REF", "REFERENCES",
            "REFERENCING", "REFRESH", "RELEASE", "RENAME", "REPLACE", "RESET", "RESETLOGS",
            "RESIZE", "RESOURCE", "RESTRICTED", "RETURN", "RETURNING", "REUSE", "REVERSE",
            "REVOKE", "ROLE", "ROLES", "ROLLBACK", "ROW", "ROWLABEL", "ROWNUM", "ROWTYPE", "ROWS",
            "RULE", "SAMPLE", "SAVEPOINT", "SCHEMA", "SCOPE", "SECOND", "SELECT", "SEPARATE",
            "SEQUENCE", "SERIALIZABLE", "SESSION", "SET", "SHARE", "SHARED", "SHARED_POOL",
            "SHRINK", "SIGNTYPE", "SIZE", "SNAPSHOT", "SOME", "SORT", "SPACE", "SPECIFICATION",
            "SPLIT", "SQL", "SQLCODE", "SQLERRM", "SQLERROR", "SQL_TRACE", "STANDBY", "START",
            "STATEMENT", "STATEMENT_ID", "STATISTICS", "STDDEV", "STOP", "STORAGE", "STORE",
            "STRUCTURE", "SUBTYPE", "SUCCESSFUL", "SWITCH", "SYNONYM", "SYSDBA", "SYSDATE",
            "SYSOPER", "SYSTEM", "TABLE", "TABLES", "TABLESPACE", "TASK", "TEMPORARY", "TERMINATE",
            "THAN", "THE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_REGION", "TIMEZONE_ABBR",
            "TIMEZONE_MINUTE", "TIMEZONE_HOUR", "TO", "TOO_MANY_ROWS", "TRACE", "TRACING",
            "TRANSACTION", "TRANSITIONAL", "TRIGGER", "TRIGGERS", "TRUNCATE", "TYPE", "UID",
            "UNDER", "UNDO", "UNION", "UNIQUE", "UNLIMITED", "UNLOCK", "UNRECOVERABLE", "UNTIL",
            "UNUSABLE", "UNUSED", "UPDATABLE", "UPDATE", "USAGE", "USE", "USER", "USING",
            "VALIDATE", "VALIDATION", "VALUE", "VALUES", "VARCHAR", "VARIANCE", "VARRAY",
            "VARYING", "VIEW", "VIEWS", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHOUT",
            "WORK", "WRITE", "YEAR", "ZONE"              };

    protected static String       CONSTANTS[]            = {"FALSE", "NULL", "TRUE"};

}
