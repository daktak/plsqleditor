/*
 * Created on 21/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.rules.PlSqlWhitespaceDetector;
import plsqleditor.rules.PlSqlWordDetector;
import plsqleditor.rules.NonKeywordsRules;


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

        List<IRule> rules = new ArrayList<IRule>();
        rules.add(new WhitespaceRule(new PlSqlWhitespaceDetector()));

        rules.add(new EndOfLineRule("//", comment));
        rules.add(new SingleLineRule("\"", "\"", string, '\\'));
        rules.add(new SingleLineRule("'", "'", string, '\\'));

        WordRule wordRule = new WordRule(new PlSqlWordDetector(), defaultToken);
        for (int i = 0; i < CONSTANTS.length; i++)
        {
            wordRule.addWord(CONSTANTS[i], constants);
        }
        for (int i = 0; i < DATATYPES.length; i++)
        {
            wordRule.addWord(DATATYPES[i], type);
        }
        for (int i = 0; i < KEYWORDS.length; i++)
        {
            wordRule.addWord(KEYWORDS[i], sqlkeyword);
        }
        rules.add(wordRule);

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

    private static final String   OPERATORS[] = {"(", ")", "!=", "%", "&", "*", "**", "+", "-",
            "/", ":=", "<", "<=", "<>", "=", ">", ">=", "||", "\""};
    protected static final String DATATYPES[] = {"BFILE", "BINARY_INTEGER", "BLOB", "BOOLEAN",
            "CHAR", "CLOB", "DATE", "FLOAT", "INTEGER", "LONG", "MLSLABEL", "NCHAR", "NCLOB",
            "NUMBER", "NVARCHAR2", "PLS_INTEGER", "RAW", "ROWID", "VARCHAR2"};
    protected static final String KEYWORDS[]  = {"WHEN", "PACKAGE", "CREATE", "FUNCTION", "IS",
            "RETURN", "RETURNING", "BEGIN", "END", "EXCEPTION", "WHEN", "THEN", "PROCEDURE",
            "EXIT", "BODY", "REPLACE", "ACCESS", "ACTIVATE", "ADD", "ADMIN", "AFTER", "ALL",
            "ALLOCATE", "ALL_ROWS", "ALTER", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "AT",
            "AUDIT", "AUTHENTICATED", "AUTHORIZATION", "AUTOEXTEND", "AUTOMATIC", "BACKUP",
            "BECOME", "BEFORE", "BETWEEN", "BITMAP", "BLOCK", "BY", "CACHE", "CANCEL", "CASCADE",
            "CAST", "CFILE", "CHAINED", "CHANGE", "CHARACTER", "CHAR_CS", "CHECK", "CHECKPOINT",
            "CHOOSE", "CHUNK", "CLEAR", "CLUSTER", "COALESCE", "COLUMN", "COLUMNS", "COMMENT",
            "COMMIT", "COMPATIBILITY", "COMPILE", "COMPLETE", "COMPRESS", "COMPUTE", "CONNECT",
            "CONSTRAINT", "CONSTRAINTS", "CONTENTS", "CONTINUE", "CONTROLFILE", "COST", "COUNT",
            "CURRENT", "CURSOR", "CYCLE", "DANGLING", "DATABASE", "DATAFILE", "DBA", "DEALLOCATE",
            "DEBUG", "DEFERRABLE", "DEFERRED", "DEGREE", "DELETE", "DESC", "DIRECTORY", "DISABLE",
            "DISCONNECT", "DISTINCT", "DISTRIBUTED", "DOUBLE", "DROP", "EACH", "ENABLE", "ENFORCE",
            "ENTRY", "ESCAPE", "ESTIMATE", "EVENTS", "EXCEPTIONS", "EXCHANGE", "EXCLUDING",
            "EXCLUSIVE", "EXECUTE", "EXISTS", "EXPIRE", "EXPLAIN", "EXTENT", "EXTENTS",
            "EXTERNALLY", "FAST", "FILE", "FIRST", "FIRST_ROWS", "FLUSH", "FORCE", "FOREIGN",
            "FOUND", "FREELIST", "FREELISTS", "FROM", "FULL", "GLOBAL", "GLOBAL_NAME", "GRANT",
            "GROUP", "GROUPS", "HASH", "HASHKEYS", "HAVING", "HEADER", "HEAP", "IDENTIFIED",
            "IDLE_TIME", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX", "INDEXED",
            "INDEXES", "INDICATOR", "IND_PARTITION", "INITIAL", "INITIALLY", "INITRANS", "INSERT",
            "INSTANCE", "INSTANCES", "INSTEAD", "INTERSECT", "INTO", "IS NULL", "ISOLATION",
            "ISOLATION_LEVEL", "KEEP", "KEY", "KILL", "LAST", "LAYER", "LESS", "LEVEL", "LIBRARY",
            "LIKE", "LIMIT", "LINK", "LIST", "LOB", "LOCAL", "LOCK", "LOGFILE", "LOGGING",
            "MASTER", "MAXEXTENTS", "MEMBER", "MINEXTENTS", "MINIMUM", "MINUS", "MINVALUE", "MODE",
            "MODIFY", "MOUNT", "MOVE", "MULTISET", "NATIONAL", "NCHAR_CS", "NEEDED", "NESTED",
            "NETWORK", "NEW", "NEXT", "NLS_CALENDAR", "NLS_CHARACTERSET", "NLS_ISO_CURRENCY",
            "NLS_LANGUAGE", "NLS_NUMERIC_", "NLS_SORT", "NLS_TERRITORY", "NOARCHIVELOG", "NOAUDIT",
            "NOCACHE", "NOCOMPRESS", "NOCYCLE", "NOFORCE", "NOLOGGING", "NOMAXVALUE", "NOMINVALUE",
            "NONE", "NOORDER", "NOOVERIDE", "NOPARALLEL", "NORESETLOGS", "NOREVERSE", "NORMAL",
            "NOSORT", "NOT", "NOTHING", "NOWAIT", "NUMERIC", "OBJECT", "OF", "OFF", "OFFLINE",
            "OID", "OIDINDEX", "OLD", "ON", "ONLINE", "ONLY", "OPCODE", "OPEN", "OPTIMAL",
            "OPTIMIZER_GOAL", "OPTION", "OR", "ORDER", "OUT", "OVERFLOW", "OWN", "PARALLEL", "PARTITION",
            "PASSWORD", "PCTFREE", "PCTINCREASE", "PCTUSED", "PERMANENT", "PLAN", "PLSQL_DEBUG",
            "PRECISION", "PRESERVE", "PRIMARY", "PRIOR", "PRIVATE", "PRIVILEGE", "PRIVILEGES",
            "PROFILE", "PUBLIC", "PURGE", "QUEUE", "QUOTA", "RANGE", "REBUILD", "RECOVER",
            "RECOVERABLE", "RECOVERY", "REF", "REFERENCES", "REFERENCING", "REFRESH", "RENAME",
            "RESET", "RESETLOGS", "RESIZE", "RESOURCE", "RESTRICTED", "REUSE", "REVERSE", "REVOKE",
            "ROLE", "ROLES", "ROLLBACK", "ROW", "ROWLABEL", "ROWNUM", "ROWS", "RULE", "SAMPLE",
            "SAVEPOINT", "SCHEMA", "SCOPE", "SELECT", "SEQUENCE", "SERIALIZABLE", "SESSION", "SET",
            "SHARE", "SHARED", "SHARED_POOL", "SHRINK", "SIZE", "SNAPSHOT", "SOME", "SORT",
            "SPECIFICATION", "SPLIT", "SQLERROR", "SQL_TRACE", "STANDBY", "START", "STATEMENT_ID",
            "STATISTICS", "STOP", "STORAGE", "STORE", "STRUCTURE", "SUCCESSFUL", "SWITCH",
            "SYNONYM", "SYSDBA", "SYSOPER", "SYSTEM", "TABLE", "TABLES", "TABLESPACE", "TEMPORARY",
            "THAN", "THE", "TIME", "TIMESTAMP", "TO", "TRACE", "TRACING", "TRANSACTION",
            "TRANSITIONAL", "TRIGGER", "TRIGGERS", "TRUNCATE", "TYPE", "UNDER", "UNDO", "UNION",
            "UNIQUE", "UNLIMITED", "UNLOCK", "UNRECOVERABLE", "UNTIL", "UNUSABLE", "UNUSED",
            "UPDATABLE", "UPDATE", "USAGE", "USE", "USING", "VALIDATE", "VALIDATION", "VALUE",
            "VALUES", "VARCHAR", "VARRAY", "VARYING", "VIEW", "WHENEVER", "WHERE", "WITH",
            "WITHOUT", "WORK", "ARRAYLEN", "CASE", "CLOSE", "CONSTANT", "CURRVAL", "DEBUGOFF",
            "DEBUGON", "DECLARE", "DEFAULT", "DEFINTION", "DELAY", "DIGITS", "DISPOSE", "DO",
            "ELSE", "ELSIF", "EXCEPTION_INIT", "FETCH", "FOR", "FORM", "GENERIC", "GOTO", "IF",
            "INTERFACE", "LIMITED", "LOOP", "NEXTVAL", "PRAGMA", "RAISE", "RECORD", "RELEASE",
            "ROWTYPE", "SIGNTYPE", "SPACE", "SQL", "STATEMENT", "SUBTYPE", "TASK", "TERMINATE",
            "VIEWS", "WHILE"                  };

    protected static String       CONSTANTS[] = {"FALSE", "NULL", "TRUE"};

}
