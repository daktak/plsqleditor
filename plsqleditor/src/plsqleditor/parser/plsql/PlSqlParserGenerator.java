package plsqleditor.parser.plsql;

import java.util.HashMap;
import java.util.Map;

import plsqleditor.parser.framework.AnyTokenSpecification;
import plsqleditor.parser.framework.DelimiterSpec;
import plsqleditor.parser.framework.IParseSpecification;
import plsqleditor.parser.framework.OrGroup;
import plsqleditor.parser.framework.SequentialCompositeParseSpecification;
import plsqleditor.parser.framework.SequentialLoopingParseSpecification;
import plsqleditor.parser.framework.SimpleParseSpecification;
import plsqleditor.parser.framework.TypedParseSpecification;

public class PlSqlParserGenerator
{
    private Map myExpressionMap = new HashMap();

    public PlSqlParserGenerator()
    {
        //
    }

    // PL/SQL specifications

    /**
     * This method gets a pl/sql expression specification.
     */
    public IParseSpecification getExpression()
    {
        SequentialCompositeParseSpecification expression = (SequentialCompositeParseSpecification) myExpressionMap
                .get("Expression");
        if (expression == null)
        {
            expression = new SequentialCompositeParseSpecification("Expression");
            myExpressionMap.put("Expression", expression);

            OrGroup innerExpression = new OrGroup("InnerExpression");
            // should replace getBasicBooleanExpression with
            // getOtherBooleanExpression
            innerExpression.addParseSpecification(getBasicBooleanExpression(), 1, 1);
            innerExpression.addParseSpecification(getNumericSubExpression(), 1, 1);
            innerExpression.addParseSpecification(getTextSpecification(), 1, 1);
            innerExpression.addParseSpecification(getSqlExpression(), 1, 1); // new
            innerExpression.addParseSpecification(getFunctionStatement(), 1, 1);
            innerExpression.addParseSpecification(getVariableName(), 1, 1); // new

            // this is for the numeric subexpr - it is here because we are
            // munging the data together
            SequentialCompositeParseSpecification exponent = new SequentialCompositeParseSpecification(
                    "Exponent");
            exponent.addParseSpecification(getDelimiterSpec("ToThePowerOf", "**", true), 1, 1);
            exponent.addParseSpecification(getExpression(), 1, 1);

            SequentialCompositeParseSpecification primaryExpressionNonBrackets = new SequentialCompositeParseSpecification(
                    "PrimaryExpressionNonBrackets");
            SequentialCompositeParseSpecification primaryExpressionBrackets = new SequentialCompositeParseSpecification(
                    "PrimaryExpressionBrackets");
            OrGroup primaryExpression = new OrGroup("PrimaryExpression");

            primaryExpressionNonBrackets
                    .addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            primaryExpressionNonBrackets.addParseSpecification(innerExpression, 1, 1);
            primaryExpressionNonBrackets.addParseSpecification(exponent, 0, 1);

            primaryExpressionBrackets.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            primaryExpressionBrackets.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            primaryExpressionBrackets.addParseSpecification(innerExpression, 1, 1);
            primaryExpressionBrackets.addParseSpecification(getCloseBracketDelimiter(), 1, 1);
            primaryExpressionBrackets.addParseSpecification(exponent, 0, 1);

            primaryExpression.addParseSpecification(primaryExpressionBrackets, 1, 1);
            primaryExpression.addParseSpecification(primaryExpressionNonBrackets, 1, 1);

            OrGroup groupingConcatenatorList = new OrGroup("GroupingConcatenatorList");
            groupingConcatenatorList.addParseSpecification(getAndDelimiterSpec(), 1, 1);
            groupingConcatenatorList.addParseSpecification(getDelimiterSpec("Or", "OR"), 1, 1);
            groupingConcatenatorList.addParseSpecification(getOperator(), 1, 1);
            // IParseSpecification groupingConcatenatorList = getOperator();

            OrGroup afterSecondaryExpression = new OrGroup("AfterSecondaryExpression");

            afterSecondaryExpression.addParseSpecification(getSecondHalfOtherBooleanForm(), 1, 1);
            afterSecondaryExpression.addParseSpecification(primaryExpression, 1, 1);

            SequentialCompositeParseSpecification secondaryExpression = new SequentialCompositeParseSpecification(
                    "SecondaryExpression");
            secondaryExpression.addParseSpecification(groupingConcatenatorList, 1, 1);
            secondaryExpression.addParseSpecification(afterSecondaryExpression, 1, 1);

            expression.addParseSpecification(primaryExpression, 1, 1);
            expression.addParseSpecification(secondaryExpression, 0, Integer.MAX_VALUE);
        }
        return expression;
    }

    /**
     * This method gets the numeric sub expression value to be added to the the
     * expression mechanism. It ignores basic variable name, constant name,
     * function name and literal calls because these can all be handled
     * externally. It ignores the exponent aspect of the subexpression, because
     * this is handled by the expression that adds this specification to itself.
     * Altogether, this breaks several parsing rules concerning types. However,
     * the primary purpose of this parser is to manage categorisation of tokens,
     * not discovery of type errors.
     */
    private IParseSpecification getNumericSubExpression()
    {
        OrGroup numericSubexpression = (OrGroup) myExpressionMap.get("NumericSubexpression");
        if (numericSubexpression == null)
        {
            // ignore function call, variable and constant name, literal and
            // indicator - all done by function statement et al

            numericSubexpression = new OrGroup("NumericSubexpression");
            myExpressionMap.put(numericSubexpression.getName(), numericSubexpression);
            // do rowcount seq
            SequentialCompositeParseSpecification rowCountSequence = new SequentialCompositeParseSpecification(
                    "RowCountSequence");
            DelimiterSpec sqlDelim = getDelimiterSpec("Sql", "SQL");
            OrGroup varOrSql = new OrGroup("VarOrSQL");
            varOrSql.addParseSpecification(getVariableName(), 1, 1);
            varOrSql.addParseSpecification(sqlDelim, 1, 1);
            rowCountSequence.addParseSpecification(varOrSql, 1, 1);
            rowCountSequence.addParseSpecification(getDelimiterSpec("Percentage", "%", true), 1, 1);
            rowCountSequence.addParseSpecification(getDelimiterSpec("RowCount", "ROWCOUNT"), 1, 1);

            // do bulk rowcount
            SequentialCompositeParseSpecification bulkRowCount = new SequentialCompositeParseSpecification(
                    "BulkRowCount");
            bulkRowCount.addParseSpecification(sqlDelim, 1, 1);
            bulkRowCount.addParseSpecification(getDelimiterSpec("Percentage", "%",true), 1, 1);
            bulkRowCount.addParseSpecification(getDelimiterSpec("BulkRowCount", "BULKROWCOUNT"),
                                               1,
                                               1);
            bulkRowCount.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            bulkRowCount.addParseSpecification(getExpression(), 1, 1);
            bulkRowCount.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            // do collection
            OrGroup nextPrior = new OrGroup("NextPrior");
            nextPrior.addParseSpecification(getDelimiterSpec("Next", "NEXT"), 1, 1);
            nextPrior.addParseSpecification(getDelimiterSpec("Prior", "PRIOR"), 1, 1);

            SequentialCompositeParseSpecification nextPriorSequence = new SequentialCompositeParseSpecification(
                    "NextPriorSequence");
            nextPriorSequence.addParseSpecification(nextPrior, 1, 1);
            nextPriorSequence.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            nextPriorSequence.addParseSpecification(getExpression(), 1, 1);
            nextPriorSequence.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            OrGroup collectionEnd = new OrGroup("CollectionEnd");
            collectionEnd.addParseSpecification(getDelimiterSpec("Count", "COUNT"), 1, 1);
            collectionEnd.addParseSpecification(getDelimiterSpec("First", "FIRST"), 1, 1);
            collectionEnd.addParseSpecification(getDelimiterSpec("Last", "LAST"), 1, 1);
            collectionEnd.addParseSpecification(getDelimiterSpec("Limit", "LIMIT"), 1, 1);
            collectionEnd.addParseSpecification(nextPriorSequence, 1, 1);

            SequentialCompositeParseSpecification collectionIdentifier = new SequentialCompositeParseSpecification(
                    "CollectionIdentifier");
            collectionIdentifier.addParseSpecification(getVariableName(), 1, 1);
            collectionIdentifier.addParseSpecification(getDotDelimiter(), 1, 1);
            collectionIdentifier.addParseSpecification(collectionEnd, 1, 1);

            // ignore exponent, because this is done higher up
            numericSubexpression.addParseSpecification(rowCountSequence, 1, 1);
            numericSubexpression.addParseSpecification(bulkRowCount, 1, 1);
            numericSubexpression.addParseSpecification(collectionIdentifier, 1, 1);
        }
        return numericSubexpression;
    }

    /**
     * This method gets the specification for a comma followed by pl/sql
     * expression.
     */
    private IParseSpecification getCommaExpression()
    {
        SequentialCompositeParseSpecification commaExpression = (SequentialCompositeParseSpecification) myExpressionMap
                .get("CommaExpression");
        if (commaExpression == null)
        {
            commaExpression = new SequentialCompositeParseSpecification("CommaExpression");
            myExpressionMap.put("CommaExpression", commaExpression);
            commaExpression.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaExpression.addParseSpecification(getExpression(), 1, 1);
        }
        return commaExpression;
    }

    /**
     * This method returns the parser for parsing variable names that might
     * appear on the left hand side of an assignment statement, or anywhere that
     * an expression was required.
     * 
     * @return
     */
    public IParseSpecification getVariableName()
    {
        OrGroup variableName = (OrGroup) myExpressionMap.get("VariableName");
        if (variableName == null)
        {
            // create VariableName parser
            variableName = new OrGroup("VariableName");
            myExpressionMap.put("VariableName", variableName);

            // create CollectionSequence parser
            SequentialCompositeParseSpecification collectionSequence = new SequentialCompositeParseSpecification(
                    "CollectionSequence");
            SimpleParseSpecification name = new SimpleParseSpecification("Name");
            DelimiterSpec openBracket = getOpenBracketDelimiter();
            DelimiterSpec closeBracket = getCloseBracketDelimiter();

            collectionSequence.addParseSpecification(getSchemaDotSpec(), 0, 1);
            collectionSequence.addParseSpecification(getPackageDotSpec(), 0, 1);
            collectionSequence.addParseSpecification(name, 1, 1);
            collectionSequence.addParseSpecification(openBracket, 1, 1);
            collectionSequence.addParseSpecification(getExpression(), 1, 1);
            collectionSequence.addParseSpecification(closeBracket, 1, 1);

            // cursor variable name just uses objectOrRecordSequence

            // create the host variable sequence
            DelimiterSpec colon = getColonDelimiterSpec();
            SequentialCompositeParseSpecification indicatorName = new SequentialCompositeParseSpecification(
                    "IndicatorName");
            indicatorName.addParseSpecification(colon, 1, 1);
            indicatorName.addParseSpecification(name, 1, 1);

            SequentialCompositeParseSpecification hostVariableSeq = new SequentialCompositeParseSpecification(
                    "HostVariableSeq");
            hostVariableSeq.addParseSpecification(colon, 1, 1);
            hostVariableSeq.addParseSpecification(name, 1, 1);
            hostVariableSeq.addParseSpecification(indicatorName, 0, 1);

            IParseSpecification hostCursorVariableSeq = getHostCursorVariableNameSpec();

            // create the object or record sequence
            SequentialCompositeParseSpecification dotFieldName = new SequentialCompositeParseSpecification(
                    "DotFieldName");
            dotFieldName.addParseSpecification(getDotDelimiter(), 1, 1);
            dotFieldName.addParseSpecification(name, 1, 1);

            SequentialCompositeParseSpecification objectOrRecordSequence = new SequentialCompositeParseSpecification(
                    "ObjectOrRecordSequence");
            objectOrRecordSequence.addParseSpecification(getSchemaDotSpec(), 0, 1);
            objectOrRecordSequence.addParseSpecification(getPackageDotSpec(), 0, 1);
            objectOrRecordSequence.addParseSpecification(name, 1, 1);
            objectOrRecordSequence.addParseSpecification(dotFieldName, 0, 1);

            variableName.addParseSpecification(collectionSequence, 1, 1);
            variableName.addParseSpecification(hostVariableSeq, 1, 1);
            variableName.addParseSpecification(hostCursorVariableSeq, 1, 1);
            variableName.addParseSpecification(objectOrRecordSequence, 1, 1);
        }
        return variableName;
    }

    /**
     * This method gets a pl/sql function statement expression.
     */
    public IParseSpecification getFunctionStatement()
    {
        SequentialCompositeParseSpecification functionStatement = (SequentialCompositeParseSpecification) myExpressionMap
                .get("FunctionStatement");
        if (functionStatement == null)
        {
            functionStatement = new SequentialCompositeParseSpecification("FunctionStatement");
            myExpressionMap.put("FunctionStatement", functionStatement);
            // create ParameterName parser
            SimpleParseSpecification parameterName = new SimpleParseSpecification("ParameterName");
            DelimiterSpec describes = getDelimiterSpec("Describes", "=>", true);
            SequentialCompositeParseSpecification paramIdentifier = new SequentialCompositeParseSpecification(
                    "ParamIdentifier");
            paramIdentifier.addParseSpecification(parameterName, 1, 1);
            paramIdentifier.addParseSpecification(describes, 1, 1);

            // create Param parser
            IParseSpecification expression = getExpression();
            SequentialCompositeParseSpecification param = new SequentialCompositeParseSpecification(
                    "Param");
            param.addParseSpecification(paramIdentifier, 0, 1);
            param.addParseSpecification(expression, 1, 1);

            // create CommaParam parser
            DelimiterSpec comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaParam = new SequentialCompositeParseSpecification(
                    "CommaParam");
            commaParam.addParseSpecification(comma, 1, 1);
            commaParam.addParseSpecification(param, 1, 1);

            // create justBrackets
            DelimiterSpec openBracket = getOpenBracketDelimiter();
            DelimiterSpec closeBracket = getCloseBracketDelimiter();
            SequentialCompositeParseSpecification justBrackets = new SequentialCompositeParseSpecification(
                    "JustBrackets");
            justBrackets.addParseSpecification(openBracket, 1, 1);
            justBrackets.addParseSpecification(closeBracket, 1, 1);

            // create bracketsParam
            SequentialCompositeParseSpecification bracketsParam = new SequentialCompositeParseSpecification(
                    "BracketsParam");
            bracketsParam.addParseSpecification(openBracket, 1, 1);
            bracketsParam.addParseSpecification(param, 1, 1);
            bracketsParam.addParseSpecification(closeBracket, 1, 1);

            // create bracketsMultiParam
            SequentialCompositeParseSpecification bracketsMultiParam = new SequentialCompositeParseSpecification(
                    "BracketsMultiParam");
            bracketsMultiParam.addParseSpecification(openBracket, 1, 1);
            bracketsMultiParam.addParseSpecification(param, 1, 1);
            bracketsMultiParam.addParseSpecification(commaParam, 1, Integer.MAX_VALUE);
            bracketsMultiParam.addParseSpecification(closeBracket, 1, 1);

            // create Params
            OrGroup params = new OrGroup("Params");
            params.addParseSpecification(justBrackets, 1, 1);
            params.addParseSpecification(bracketsParam, 1, 1);
            params.addParseSpecification(bracketsMultiParam, 1, 1);

            // create schema
            IParseSpecification schemaOrPackageName = getSchemaDotSpec();

            // create package
            IParseSpecification pkg = getPackageDotSpec();

            // create FunctionStatement
            SimpleParseSpecification functionName = new SimpleParseSpecification("FunctionName");
            functionStatement.addParseSpecification(schemaOrPackageName, 0, 1);
            functionStatement.addParseSpecification(pkg, 0, 1);
            functionStatement.addParseSpecification(functionName, 1, 1);
            functionStatement.addParseSpecification(params, 0, Integer.MAX_VALUE);
        }

        return functionStatement;
    }


    // SQL specifications

    /**
     * This method gets a sql select statement specification.
     */
    public IParseSpecification getSqlSelectStatementSpec()
    {
        SequentialCompositeParseSpecification select = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlSelectCommand");
        if (select == null)
        {
            select = new SequentialCompositeParseSpecification("SqlSelectCommand");
            myExpressionMap.put(select.getName(), select);
            select.addParseSpecification(getSqlSubquerySpec(), 1, 1);
            select.addParseSpecification(getOrderByClause(), 0, 1);
            select.addParseSpecification(getForUpdateClause(), 0, 1);
        }
        return select;
    }

    /**
     * This method gets a sql update statement specification.
     */
    public IParseSpecification getSqlUpdateStatementSpec()
    {
        SequentialCompositeParseSpecification update = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlUpdateCommand");
        if (update == null)
        {
            update = new SequentialCompositeParseSpecification("SqlUpdateCommand");
            myExpressionMap.put(update.getName(), update);

            OrGroup exprOrSubquery = new OrGroup("ExprOrSubquery");
            exprOrSubquery.addParseSpecification(getSqlExpression(), 1, 1);
            exprOrSubquery.addParseSpecification(getSqlSubquerySpec(), 1, 1);

            SimpleParseSpecification column = new SimpleParseSpecification("Column");
            SequentialCompositeParseSpecification columnAssignment = new SequentialCompositeParseSpecification(
                    "ColumnAssignment");
            columnAssignment.addParseSpecification(column, 1, 1);
            columnAssignment.addParseSpecification(getDelimiterSpec("Equals", "=",true), 1, 1);
            columnAssignment.addParseSpecification(exprOrSubquery, 1, 1);

            IParseSpecification comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaColumnAssignment = new SequentialCompositeParseSpecification(
                    "CommaColumnAssignment");
            commaColumnAssignment.addParseSpecification(comma, 1, 1);
            commaColumnAssignment.addParseSpecification(columnAssignment, 1, 1);

            update.addParseSpecification(getDelimiterSpec("Update", "UPDATE"), 1, 1);
            update.addParseSpecification(getDirectTableSpec(), 1, 1);
            update.addParseSpecification(getExpressionQualifier(), 0, 1);
            update.addParseSpecification(getDelimiterSpec("Set", "SET"), 1, 1);
            update.addParseSpecification(columnAssignment, 1, 1);
            update.addParseSpecification(commaColumnAssignment, 0, Integer.MAX_VALUE);
            update.addParseSpecification(getWhereBlock(), 0, 1);
        }
        return update;
    }

    /**
     * This method gets a sql insert statement specification.
     */
    public IParseSpecification getSqlInsertStatementSpec()
    {
        SequentialCompositeParseSpecification insert = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlInsertCommand");
        if (insert == null)
        {
            insert = new SequentialCompositeParseSpecification("SqlInsertCommand");
            myExpressionMap.put(insert.getName(), insert);

            SimpleParseSpecification column = new SimpleParseSpecification("Column");
            IParseSpecification comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaColumn = new SequentialCompositeParseSpecification(
                    "CommaColumn");
            commaColumn.addParseSpecification(comma, 1, 1);
            commaColumn.addParseSpecification(column, 1, 1);

            SequentialCompositeParseSpecification columnSpec = new SequentialCompositeParseSpecification(
                    "ColumnSpec");
            columnSpec.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            columnSpec.addParseSpecification(column, 1, 1);
            columnSpec.addParseSpecification(commaColumn, 0, Integer.MAX_VALUE);
            columnSpec.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            SequentialCompositeParseSpecification valuesSpec = new SequentialCompositeParseSpecification(
                    "ValuesSpec");
            valuesSpec.addParseSpecification(getDelimiterSpec("Values", "VALUES"), 1, 1);
            valuesSpec.addParseSpecification(getExpressionList(), 1, 1);

            OrGroup insertionData = new OrGroup("InsertionData");
            insertionData.addParseSpecification(valuesSpec, 1, 1);
            insertionData.addParseSpecification(getSqlSubquerySpec(), 1, 1);

            insert.addParseSpecification(getDelimiterSpec("Insert", "INSERT"), 1, 1);
            insert.addParseSpecification(getDelimiterSpec("Into", "INTO"), 1, 1);
            insert.addParseSpecification(getDirectTableSpec(), 1, 1);
            insert.addParseSpecification(columnSpec, 1, 1);
            insert.addParseSpecification(insertionData, 1, 1);
        }
        return insert;
    }

    /**
     * This method gets a sql delete statement specification.
     */
    public IParseSpecification getSqlDeleteStatementSpec()
    {
        SequentialCompositeParseSpecification delete = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlDeleteCommand");
        if (delete == null)
        {
            delete = new SequentialCompositeParseSpecification("SqlDeleteCommand");
            myExpressionMap.put(delete.getName(), delete);
            delete.addParseSpecification(getDelimiterSpec("Delete", "DELETE"), 1, 1);
            delete.addParseSpecification(getDelimiterSpec("From", "FROM"), 1, 1);
            delete.addParseSpecification(getDirectTableSpec(), 1, 1);
            delete.addParseSpecification(getWhereBlock(), 0, 1);
        }
        return delete;
    }

    /**
     * This method gets a sql subquery specification. It is used in update,
     * insert and select statements. It is the bulk part of a select statement.
     */
    public IParseSpecification getSqlSubquerySpec()
    {
        SequentialCompositeParseSpecification sqlSubquery = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlSubquery");
        if (sqlSubquery == null)
        {
            sqlSubquery = new SequentialCompositeParseSpecification("SqlSubquery");
            myExpressionMap.put("SqlSubquery", sqlSubquery);

            OrGroup q = new OrGroup("Q");
            q.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);
            q.addParseSpecification(getSqlQuerySpec(), 1, 1);

            SequentialCompositeParseSpecification intersectAll = new SequentialCompositeParseSpecification(
                    "IntersectAll");
            intersectAll.addParseSpecification(getDelimiterSpec("Intersect", "INTERSECT"), 1, 1);
            intersectAll.addParseSpecification(getDelimiterSpec("All", "ALL"), 0, 1);

            SequentialCompositeParseSpecification unionAll = new SequentialCompositeParseSpecification(
                    "UnionAll");
            intersectAll.addParseSpecification(getDelimiterSpec("Union", "UNION"), 1, 1);
            intersectAll.addParseSpecification(getDelimiterSpec("All", "ALL"), 0, 1);

            OrGroup unionIntersect = new OrGroup("UnionIntersect");
            unionIntersect.addParseSpecification(getDelimiterSpec("TableMinus", "MINUS"), 1, 1);
            unionIntersect.addParseSpecification(intersectAll, 1, 1);
            unionIntersect.addParseSpecification(unionAll, 1, 1);

            SequentialCompositeParseSpecification unionQ = new SequentialCompositeParseSpecification(
                    "UnionQ");
            unionQ.addParseSpecification(unionIntersect, 1, 1);
            unionQ.addParseSpecification(q, 1, 1);

            sqlSubquery.addParseSpecification(q, 1, 1);
            sqlSubquery.addParseSpecification(unionQ, 0, Integer.MAX_VALUE);
        }
        return sqlSubquery;
    }

    /**
     * This method gets a sql query spec specification. It is used by select and
     * update statements. In particular it is used by the subquery
     * specification.
     */
    public IParseSpecification getSqlQuerySpec()
    {
        SequentialCompositeParseSpecification sqlQuerySpec = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlQuerySpec");
        if (sqlQuerySpec == null)
        {
            sqlQuerySpec = new SequentialCompositeParseSpecification("SqlQuerySpec");
            myExpressionMap.put("SqlQuerySpec", sqlQuerySpec);

            //
            // do select
            //
            DelimiterSpec select = getDelimiterSpec("Select", "SELECT");

            //
            // do hint
            //
            // TODO fix up the hint
            SimpleParseSpecification hint = new SimpleParseSpecification("Hint");

            //
            // do selection and comma selection
            //
            DelimiterSpec distinct = getDelimiterSpec("Distinct", "DISTINCT");
            DelimiterSpec all = getDelimiterSpec("All", "ALL");

            OrGroup distinctAll = new OrGroup("DistinctAll");
            distinctAll.addParseSpecification(distinct, 1, 1);
            distinctAll.addParseSpecification(all, 1, 1);

            IParseSpecification star = getDelimiterSpec("Star", "*", true);

            SequentialCompositeParseSpecification expressionSelection = new SequentialCompositeParseSpecification(
                    "ExpressionSelection");
            expressionSelection.addParseSpecification(getExpression(), 1, 1);
            expressionSelection.addParseSpecification(getExpressionQualifier(), 0, 1);

            SequentialCompositeParseSpecification qualifiedSelection = new SequentialCompositeParseSpecification(
                    "QualifiedSelection");
            qualifiedSelection.addParseSpecification(getPackageDotSpec(), 1, 1);
            qualifiedSelection.addParseSpecification(star, 1, 1);

            OrGroup selection = new OrGroup("Selection");
            selection.addParseSpecification(star, 1, 1);
            selection.addParseSpecification(qualifiedSelection, 1, 1);
            selection.addParseSpecification(expressionSelection, 1, 1);
            
            IParseSpecification comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaSelection = new SequentialCompositeParseSpecification(
                    "CommaSelection");
            commaSelection.addParseSpecification(comma, 1, 1);
            commaSelection.addParseSpecification(selection, 1, 1);

            //
            // do from
            //
            IParseSpecification from = getDelimiterSpec("From", "FROM");

            //
            // do tableRefOrOdbcJoins
            //
            SequentialCompositeParseSpecification directTable = (SequentialCompositeParseSpecification) getDirectTableSpec();

            SequentialCompositeParseSpecification subqueryTable = new SequentialCompositeParseSpecification(
                    "SubqueryTable");
            subqueryTable.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            subqueryTable.addParseSpecification(getSqlSubquerySpec(), 1, 1);
            subqueryTable.addParseSpecification(getOrderByClause(), 0, 1);
            subqueryTable.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            OrGroup tableOrSubquery = new OrGroup("TableOrSubquery");
            tableOrSubquery.addParseSpecification(directTable, 1, 1);
            tableOrSubquery.addParseSpecification(subqueryTable, 1, 1);

            SequentialCompositeParseSpecification tableRef = new SequentialCompositeParseSpecification(
                    "TableRef");
            tableRef.addParseSpecification(tableOrSubquery, 1, 1);
            tableRef.addParseSpecification(getExpressionQualifier(), 0, 1);

            SequentialCompositeParseSpecification odbcJoinTable = new SequentialCompositeParseSpecification(
                    "OdbcJoinTable");
            odbcJoinTable.addParseSpecification(getDelimiterSpec("OpenBrace", "{",true), 1, 1);
            odbcJoinTable.addParseSpecification(getDelimiterSpec("OJ", "OJ"), 1, 1);
            odbcJoinTable.addParseSpecification(directTable, 1, 1);
            odbcJoinTable.addParseSpecification(getDelimiterSpec("CloseBrace", "}",true), 1, 1);

            OrGroup tableRefOrOdbcJoin = new OrGroup("TableRefOrOdbcJoin");
            tableRefOrOdbcJoin.addParseSpecification(odbcJoinTable, 1, 1);
            tableRefOrOdbcJoin.addParseSpecification(tableRef, 1, 1);

            SequentialCompositeParseSpecification commaTableRefOrOdbcJoin = new SequentialCompositeParseSpecification(
                    "CommaTableRefOrOdbcJoin");
            commaTableRefOrOdbcJoin.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaTableRefOrOdbcJoin.addParseSpecification(tableRefOrOdbcJoin, 1, 1);

            SequentialCompositeParseSpecification tableRefOrOdbcJoins = new SequentialCompositeParseSpecification(
                    "TableRefOrOdbcJoins");
            tableRefOrOdbcJoins.addParseSpecification(tableRefOrOdbcJoins, 1, 1);
            tableRefOrOdbcJoins
                    .addParseSpecification(commaTableRefOrOdbcJoin, 0, Integer.MAX_VALUE);

            //
            // do whereBlock
            //
            SequentialCompositeParseSpecification whereBlock = (SequentialCompositeParseSpecification) getWhereBlock();

            //
            // do startWithOrGroupBy
            //
            SequentialCompositeParseSpecification startWithBlock = new SequentialCompositeParseSpecification(
                    "StartWithBlock");
            startWithBlock.addParseSpecification(getDelimiterSpec("Start", "START"), 1, 1);
            startWithBlock.addParseSpecification(getDelimiterSpec("With", "WITH"), 1, 1);
            startWithBlock.addParseSpecification(getSqlCondition(), 1, 1);

            SequentialCompositeParseSpecification startWith = new SequentialCompositeParseSpecification(
                    "StartWith");
            startWith.addParseSpecification(startWithBlock, 0, 1);
            startWith.addParseSpecification(getDelimiterSpec("Connect", "CONNECT"), 1, 1);
            startWith.addParseSpecification(getDelimiterSpec("By", "BY"), 1, 1);
            startWith.addParseSpecification(getSqlCondition(), 1, 1);

            SequentialCompositeParseSpecification groupBy = new SequentialCompositeParseSpecification(
                    "GroupBy");
            groupBy.addParseSpecification(getDelimiterSpec("Group", "GROUP"), 1, 1);
            groupBy.addParseSpecification(getDelimiterSpec("By", "BY"), 1, 1);
            groupBy.addParseSpecification(getSqlExpression(), 1, 1);
            groupBy.addParseSpecification(getCommaSqlExpression(), 0, Integer.MAX_VALUE);

            OrGroup startWithOrGroupBy = new OrGroup("StartWithOrGroupBy");
            startWithOrGroupBy.addParseSpecification(startWith, 1, 1);
            startWithOrGroupBy.addParseSpecification(groupBy, 1, 1);

            //
            // do havingBlock
            //
            SequentialCompositeParseSpecification havingBlock = new SequentialCompositeParseSpecification(
                    "HavingBlock");
            havingBlock.addParseSpecification(getDelimiterSpec("Having", "HAVING"), 1, 1);
            havingBlock.addParseSpecification(getSqlCondition(), 1, 1);

            // create sqlQuerySpec
            sqlQuerySpec.addParseSpecification(select, 1, 1);
            //sqlQuerySpec.addParseSpecification(hint, 0, 1);
            sqlQuerySpec.addParseSpecification(distinctAll, 0, 1);
            sqlQuerySpec.addParseSpecification(selection, 1, 1);
            sqlQuerySpec.addParseSpecification(commaSelection, 0, Integer.MAX_VALUE);
            sqlQuerySpec.addParseSpecification(from, 1, 1);
            sqlQuerySpec.addParseSpecification(tableRefOrOdbcJoin, 1, 1);
            sqlQuerySpec.addParseSpecification(whereBlock, 0, 1);
            sqlQuerySpec.addParseSpecification(startWithOrGroupBy, 0, 1);
            sqlQuerySpec.addParseSpecification(havingBlock, 0, 1);
        }

        return sqlQuerySpec;
    }

    private IParseSpecification getOrderByClause()
    {
        SequentialCompositeParseSpecification orderByClause = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlOrderByClause");
        if (orderByClause == null)
        {
            orderByClause = new SequentialCompositeParseSpecification("SqlOrderByClause");
            myExpressionMap.put(orderByClause.getName(), orderByClause);

            OrGroup ascDesc = new OrGroup("AscDesc");
            ascDesc.addParseSpecification(getDelimiterSpec("Ascending", "ASC"), 1, 1);
            ascDesc.addParseSpecification(getDelimiterSpec("Descending", "DESC"), 1, 1);

            TypedParseSpecification position = new TypedParseSpecification("Position",
                    TypedParseSpecification.INTEGER);
            OrGroup exprPosAlias = new OrGroup("ExprPosAlias");
            exprPosAlias.addParseSpecification(getSqlExpression(), 1, 1); // covers
            // c_alias
            exprPosAlias.addParseSpecification(position, 1, 1);
            // exprPosAlias.addParseSpecification(alias, 1, 1); // not needed
            // because of expression

            SequentialCompositeParseSpecification orderBySpec = new SequentialCompositeParseSpecification(
                    "OrderBySpec");
            orderBySpec.addParseSpecification(exprPosAlias, 1, 1);
            orderBySpec.addParseSpecification(ascDesc, 0, 1);

            DelimiterSpec comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaOrderBySpec = new SequentialCompositeParseSpecification(
                    "CommaOrderBySpec");
            commaOrderBySpec.addParseSpecification(comma, 1, 1);
            commaOrderBySpec.addParseSpecification(orderBySpec, 1, 1);

            orderByClause.addParseSpecification(getDelimiterSpec("Order", "ORDER"), 1, 1);
            orderByClause.addParseSpecification(getDelimiterSpec("By", "BY"), 1, 1);
            orderByClause.addParseSpecification(orderBySpec, 1, 1);
            orderByClause.addParseSpecification(commaOrderBySpec, 0, Integer.MAX_VALUE);
        }
        return orderByClause;
    }

    private IParseSpecification getForUpdateClause()
    {
        SequentialCompositeParseSpecification forUpdateClause = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlForUpdateClause");
        if (forUpdateClause == null)
        {
            forUpdateClause = new SequentialCompositeParseSpecification("SqlForUpdateClause");
            myExpressionMap.put(forUpdateClause.getName(), forUpdateClause);

            DelimiterSpec comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaFullColumn = new SequentialCompositeParseSpecification(
                    "CommaFullColumn");
            commaFullColumn.addParseSpecification(comma, 1, 1);
            commaFullColumn.addParseSpecification(getColumnSpecification(), 1, 1);

            SequentialCompositeParseSpecification forUpdateEnd = new SequentialCompositeParseSpecification(
                    "ForUpdateEnd");
            forUpdateEnd.addParseSpecification(getDelimiterSpec("Of", "OF"), 1, 1);
            forUpdateEnd.addParseSpecification(getColumnSpecification(), 1, 1);
            forUpdateEnd.addParseSpecification(commaFullColumn, 0, Integer.MAX_VALUE);

            forUpdateClause.addParseSpecification(getDelimiterSpec("For", "For"), 1, 1);
            forUpdateClause.addParseSpecification(getDelimiterSpec("Update", "UPDATE"), 1, 1);
            forUpdateClause.addParseSpecification(forUpdateEnd, 0, 1);
        }
        return forUpdateClause;
    }

    private IParseSpecification getSqlSubqueryBracketsSpec()
    {
        SequentialCompositeParseSpecification subqueryBracketsSpec = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlSubqueryBracketsSpec");
        if (subqueryBracketsSpec == null)
        {
            // create up front to avoid recursive calling causing an infinite
            // loop
            subqueryBracketsSpec = new SequentialCompositeParseSpecification(
                    "SqlSubqueryBracketsSpec");
            myExpressionMap.put("SqlSubqueryBracketsSpec", subqueryBracketsSpec);

            subqueryBracketsSpec.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            subqueryBracketsSpec.addParseSpecification(getSqlSubquerySpec(), 1, 1);
            subqueryBracketsSpec.addParseSpecification(getCloseBracketDelimiter(), 1, 1);
        }
        return subqueryBracketsSpec;
    }

    public IParseSpecification getSqlCondition()
    {
        SequentialCompositeParseSpecification condition = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlCondition");
        if (condition == null)
        {
            // create up front to avoid recursive calling causing an infinite
            // loop
            condition = new SequentialCompositeParseSpecification("SqlCondition");
            myExpressionMap.put("SqlCondition", condition);

            //
            // create compoundCondition
            //
            SequentialCompositeParseSpecification bracketsCondition = new SequentialCompositeParseSpecification(
                    "BracketsCondition");
            bracketsCondition.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            bracketsCondition.addParseSpecification(getSqlCondition(), 1, 1);
            bracketsCondition.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            SequentialCompositeParseSpecification notCondition = new SequentialCompositeParseSpecification(
                    "NotCondition");
            notCondition.addParseSpecification(getDelimiterSpec("Not", "NOT"), 1, 1);
            notCondition.addParseSpecification(getSqlCondition(), 1, 1);

            OrGroup compoundCondition = new OrGroup("CompoundCondition");
            compoundCondition.addParseSpecification(bracketsCondition, 1, 1);
            compoundCondition.addParseSpecification(notCondition, 1, 1);

            //
            // create simpleComparison
            //
            OrGroup exprOrSubquery = new OrGroup("ExprOrSubquery");
            exprOrSubquery.addParseSpecification(getSqlExpression(), 1, 1);
            exprOrSubquery.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);

            SequentialCompositeParseSpecification simpleComparison = new SequentialCompositeParseSpecification(
                    "SimpleComparison");
            simpleComparison.addParseSpecification(getSqlExpression(), 1, 1);
            simpleComparison.addParseSpecification(getRelationalOperatorGroup(), 1, 1);
            simpleComparison.addParseSpecification(exprOrSubquery, 1, 1);

            //
            // create membershipCondition
            //
            OrGroup exprListOrSubquery = new OrGroup("ExprListOrSubquery");
            exprListOrSubquery.addParseSpecification(getExpressionList(), 1, 1);
            exprListOrSubquery.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);

            SequentialCompositeParseSpecification membershipCondition = new SequentialCompositeParseSpecification(
                    "MembershipCondition");
            membershipCondition.addParseSpecification(getSqlExpression(), 1, 1);
            membershipCondition.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            membershipCondition.addParseSpecification(getDelimiterSpec("In", "IN"), 1, 1);
            membershipCondition.addParseSpecification(exprListOrSubquery, 1, 1);

            //
            // create groupComparison
            //
            OrGroup anySomeAll = new OrGroup("AnySomeAll");
            anySomeAll.addParseSpecification(getDelimiterSpec("Any", "ANY"), 1, 1);
            anySomeAll.addParseSpecification(getDelimiterSpec("Some", "SOME"), 1, 1);
            anySomeAll.addParseSpecification(getDelimiterSpec("All", "ALL"), 1, 1);


            SequentialCompositeParseSpecification exprGroupComparison = new SequentialCompositeParseSpecification(
                    "ExprGroupComparison");
            exprGroupComparison.addParseSpecification(getSqlExpression(), 1, 1);
            exprGroupComparison.addParseSpecification(getRelationalOperatorGroup(), 1, 1);
            exprGroupComparison.addParseSpecification(anySomeAll, 1, 1);
            exprGroupComparison.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);

            OrGroup eqNotEq = new OrGroup("EqNotEq");
            anySomeAll.addParseSpecification(getDelimiterSpec("Equals", "=", true), 1, 1);
            anySomeAll.addParseSpecification(getDelimiterSpec("NotEquals", "!=", true), 1, 1);

            SequentialCompositeParseSpecification exprListGroupComparison = new SequentialCompositeParseSpecification(
                    "ExprListGroupComparison");
            exprListGroupComparison.addParseSpecification(getExpressionList(), 1, 1);
            exprListGroupComparison.addParseSpecification(eqNotEq, 1, 1);
            exprListGroupComparison.addParseSpecification(anySomeAll, 1, 1);
            exprListGroupComparison.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);

            OrGroup groupComparison = new OrGroup("GroupComparison");
            groupComparison.addParseSpecification(exprGroupComparison, 1, 1);
            groupComparison.addParseSpecification(exprListGroupComparison, 1, 1);

            //
            // create rangeCondition
            //
            SequentialCompositeParseSpecification rangeCondition = new SequentialCompositeParseSpecification(
                    "RangeCondition");
            rangeCondition.addParseSpecification(getSqlExpression(), 1, 1);
            rangeCondition.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            rangeCondition.addParseSpecification(getDelimiterSpec("Between", "BETWEEN"), 1, 1);
            rangeCondition.addParseSpecification(getSqlExpression(), 1, 1);
            rangeCondition.addParseSpecification(getAndDelimiterSpec(), 1, 1);
            rangeCondition.addParseSpecification(getSqlExpression(), 1, 1);

            //
            // create nullCondition
            //
            SequentialCompositeParseSpecification nullCondition = new SequentialCompositeParseSpecification(
                    "NullCondition");
            nullCondition.addParseSpecification(getSqlExpression(), 1, 1);
            nullCondition.addParseSpecification(getDelimiterSpec("Is", "IS"), 1, 1);
            nullCondition.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            nullCondition.addParseSpecification(getDelimiterSpec("Null", "NULL"), 1, 1);

            //
            // create existsCondition
            //
            SequentialCompositeParseSpecification existsCondition = new SequentialCompositeParseSpecification(
                    "ExistsCondition");
            existsCondition.addParseSpecification(getDelimiterSpec("Exists", "EXISTS"), 1, 1);
            existsCondition.addParseSpecification(getSqlSubqueryBracketsSpec(), 1, 1);

            //
            // create likeCondition
            //
            AnyTokenSpecification singleChar = new AnyTokenSpecification("SingleChar");
            SequentialCompositeParseSpecification escapeLike = new SequentialCompositeParseSpecification(
                    "EscapeLike");
            escapeLike.addParseSpecification(getDelimiterSpec("Escape", "ESCAPE"), 1, 1);
            escapeLike.addParseSpecification(getDelimiterSpec("Quote", "'", true), 1, 1);
            escapeLike.addParseSpecification(singleChar, 1, 1);
            escapeLike.addParseSpecification(getDelimiterSpec("Quote", "'", true), 1, 1);

            SequentialCompositeParseSpecification likeCondition = new SequentialCompositeParseSpecification(
                    "LikeCondition");
            likeCondition.addParseSpecification(getSqlExpression(), 1, 1);
            likeCondition.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            likeCondition.addParseSpecification(getDelimiterSpec("Like", "LIKE"), 1, 1);
            likeCondition.addParseSpecification(getSqlExpression(), 1, 1);
            likeCondition.addParseSpecification(escapeLike, 0, 1);

            //
            // final preparation
            //
            OrGroup conditionHolder = new OrGroup("ConditionHolder");
            conditionHolder.addParseSpecification(compoundCondition, 1, 1);
            conditionHolder.addParseSpecification(simpleComparison, 1, 1);
            conditionHolder.addParseSpecification(membershipCondition, 1, 1);
            conditionHolder.addParseSpecification(groupComparison, 1, 1);
            conditionHolder.addParseSpecification(rangeCondition, 1, 1);
            conditionHolder.addParseSpecification(nullCondition, 1, 1);
            conditionHolder.addParseSpecification(existsCondition, 1, 1);
            conditionHolder.addParseSpecification(likeCondition, 1, 1);

            OrGroup andOrGroup = new OrGroup("AndOrGroup");
            andOrGroup.addParseSpecification(getAndDelimiterSpec(), 1, 1);
            andOrGroup.addParseSpecification(getDelimiterSpec("Or", "OR"), 1, 1);

            SequentialCompositeParseSpecification secondaryCondition = new SequentialCompositeParseSpecification(
                    "SecondaryCondition");
            secondaryCondition.addParseSpecification(andOrGroup, 1, 1);
            secondaryCondition.addParseSpecification(conditionHolder, 1, 1);

            condition.addParseSpecification(conditionHolder, 1, 1);
            condition.addParseSpecification(secondaryCondition, 0, Integer.MAX_VALUE);
        }
        return condition;
    }

    private IParseSpecification getSqlFunctionExpression()
    {
        SequentialCompositeParseSpecification functionExpression = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlFunctionExpression");
        if (functionExpression == null)
        {
            // create up front to avoid recursive calling causing an infinite
            // loop
            functionExpression = new SequentialCompositeParseSpecification("SqlFunctionExpression");
            myExpressionMap.put("SqlFunctionExpression", functionExpression);

            // create ParameterName parser
            // SimpleParseSpecification parameterName = new
            // SimpleParseSpecification("ParameterName");
            // DelimiterSpec describes = new DelimiterSpec("Describes", "=>");
            // SequentialCompositeParseSpecification paramIdentifier = new
            // SequentialCompositeParseSpecification(
            // "ParamIdentifier");
            // paramIdentifier.addParseSpecification(parameterName, 1, 1);
            // paramIdentifier.addParseSpecification(describes, 1, 1);

            // create Param parser
            IParseSpecification expression = getSqlExpression();
            // SequentialCompositeParseSpecification param = new
            // SequentialCompositeParseSpecification(
            // "Param");
            // param.addParseSpecification(paramIdentifier, 0, 1);
            // param.addParseSpecification(expression, 1, 1);

            // create CommaParam parser
            DelimiterSpec comma = getCommaDelimiterSpec();
            SequentialCompositeParseSpecification commaSqlExpr = new SequentialCompositeParseSpecification(
                    "CommaSqlExpression");
            commaSqlExpr.addParseSpecification(comma, 1, 1);
            commaSqlExpr.addParseSpecification(expression, 1, 1);

            // create distinct all group and param
            SequentialCompositeParseSpecification distinctAllGroup = new SequentialCompositeParseSpecification(
                    "DistinctAllGroup");
            distinctAllGroup.addParseSpecification(getDelimiterSpec("Distinct", "DISTINCT"), 1, 1);
            distinctAllGroup.addParseSpecification(getDelimiterSpec("All", "ALL"), 1, 1);

            SequentialCompositeParseSpecification distinctAllGroupExpr = new SequentialCompositeParseSpecification(
                    "distinctAllGroupExpr");
            distinctAllGroupExpr.addParseSpecification(distinctAllGroup, 0, 1);
            distinctAllGroupExpr.addParseSpecification(expression, 1, 1);

            // create bracketsParam
            DelimiterSpec openBracket = getOpenBracketDelimiter();
            DelimiterSpec closeBracket = getCloseBracketDelimiter();
            SequentialCompositeParseSpecification bracketsParam = new SequentialCompositeParseSpecification(
                    "BracketsParam");
            bracketsParam.addParseSpecification(openBracket, 1, 1);
            bracketsParam.addParseSpecification(distinctAllGroupExpr /* param */, 0, 1);
            bracketsParam.addParseSpecification(closeBracket, 1, 1);

            // create bracketsMultiParam
            SequentialCompositeParseSpecification bracketsMultiParam = new SequentialCompositeParseSpecification(
                    "BracketsMultiParam");
            bracketsMultiParam.addParseSpecification(openBracket, 1, 1);
            bracketsMultiParam.addParseSpecification(distinctAllGroupExpr, 0, 1);
            bracketsMultiParam.addParseSpecification(commaSqlExpr, 1, Integer.MAX_VALUE);
            bracketsMultiParam.addParseSpecification(closeBracket, 1, 1);

            // create Params
            OrGroup params = new OrGroup("Params");
            params.addParseSpecification(bracketsParam, 1, 1);
            params.addParseSpecification(bracketsMultiParam, 1, 1);

            // create schema
            IParseSpecification schemaOrPackageName = getSchemaDotSpec();

            // create package
            IParseSpecification pkg = getPackageDotSpec();

            // create functionExpression
            SimpleParseSpecification functionName = getSimpleParseSpecification("FunctionName");
            functionExpression.addParseSpecification(schemaOrPackageName, 0, 1);
            functionExpression.addParseSpecification(pkg, 0, 1);
            functionExpression.addParseSpecification(functionName, 1, 1);
            functionExpression.addParseSpecification(params, 0, Integer.MAX_VALUE);
        }

        return functionExpression;
    }

    private IParseSpecification getWhereBlock()
    {
        SequentialCompositeParseSpecification whereBlock = (SequentialCompositeParseSpecification) myExpressionMap
                .get("WhereBlock");
        if (whereBlock == null)
        {
            whereBlock = new SequentialCompositeParseSpecification("WhereBlock");
            myExpressionMap.put(whereBlock.getName(), whereBlock);
            whereBlock.addParseSpecification(getDelimiterSpec("Where", "WHERE"), 1, 1);
            whereBlock.addParseSpecification(getSqlCondition(), 1, 1);
        }
        return whereBlock;
    }

    /**
     * This method returns a specification that identifies a table or view that
     * may be qualified by a schema.
     */
    private IParseSpecification getDirectTableSpec()
    {
        SequentialCompositeParseSpecification directTable = (SequentialCompositeParseSpecification) myExpressionMap
                .get("DirectTable");
        if (directTable == null)
        {
            directTable = new SequentialCompositeParseSpecification("DirectTable");
            myExpressionMap.put(directTable.getName(), directTable);
            SimpleParseSpecification tableOrView = new SimpleParseSpecification("TableOrView");
            directTable.addParseSpecification(getSchemaDotSpec(), 0, 1);
            directTable.addParseSpecification(tableOrView, 1, 1);
        }
        return directTable;
    }

    private IParseSpecification getExpressionList()
    {
        SequentialCompositeParseSpecification sqlExpressionList = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlExpressionList");
        if (sqlExpressionList == null)
        {
            // create up front to avoid recursive calling causing an infinite
            // loop
            sqlExpressionList = new SequentialCompositeParseSpecification("SqlExpressionList");
            myExpressionMap.put("SqlExpressionList", sqlExpressionList);

            SequentialCompositeParseSpecification commaExpression = new SequentialCompositeParseSpecification(
                    "CommaExpression");
            commaExpression.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaExpression.addParseSpecification(getSqlExpression(), 1, 1);

            sqlExpressionList.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            sqlExpressionList.addParseSpecification(getSqlExpression(), 1, 1);
            sqlExpressionList.addParseSpecification(commaExpression, 0, Integer.MAX_VALUE);
            sqlExpressionList.addParseSpecification(getCloseBracketDelimiter(), 1, 1);
        }
        return sqlExpressionList;
    }

    /**
     * This method gets the specification for a piece of text that may be used
     * to qualify an expression. It is primarily used to alias tables and
     * columns.
     */
    private IParseSpecification getExpressionQualifier()
    {
        SequentialCompositeParseSpecification expressionQualifier = (SequentialCompositeParseSpecification) myExpressionMap
                .get("ExpressionQualifier");
        if (expressionQualifier == null)
        {
            expressionQualifier = new SequentialCompositeParseSpecification("ExpressionQualifier");
            myExpressionMap.put("ExpressionQualifier", expressionQualifier);

            AnyTokenSpecification anything = new AnyTokenSpecification("Anything");
            SequentialLoopingParseSpecification quotedAlias = new SequentialLoopingParseSpecification(
                    "QuotedAlias");
            quotedAlias.addParseSpecification(getDelimiterSpec("DoubleQuote", "\"", true), 1, 1);
            quotedAlias.addParseSpecification(anything, 1, Integer.MAX_VALUE);
            quotedAlias.addParseSpecification(getDelimiterSpec("DoubleQuote", "\"", true), 1, 1);

            SimpleParseSpecification alias = new SimpleParseSpecification("Alias");

            OrGroup columnAlias = new OrGroup("ColumnAlias");
            columnAlias.addParseSpecification(quotedAlias, 1, 1);
            columnAlias.addParseSpecification(alias, 1, 1);

            expressionQualifier.addParseSpecification(getDelimiterSpec("As", "AS"), 0, 1);
            expressionQualifier.addParseSpecification(columnAlias, 1, 1);
        }
        return expressionQualifier;
    }

    private IParseSpecification getSchemaDotSpec()
    {
        SequentialCompositeParseSpecification schemaDot = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SchemaDot");
        if (schemaDot == null)
        {
            schemaDot = new SequentialCompositeParseSpecification("SchemaDot");
            myExpressionMap.put("SchemaDot", schemaDot);

            SimpleParseSpecification schemaOrPackageName = new SimpleParseSpecification(
                    "SchemaOrPackageName");
            DelimiterSpec dot = getDotDelimiter();

            schemaDot.addParseSpecification(schemaOrPackageName, 1, 1);
            schemaDot.addParseSpecification(dot, 1, 1);
        }
        return schemaDot;
    }

    private IParseSpecification getPackageDotSpec()
    {
        SequentialCompositeParseSpecification pkg = (SequentialCompositeParseSpecification) myExpressionMap
                .get("PackageDot");
        if (pkg == null)
        {
            pkg = new SequentialCompositeParseSpecification("PackageDot");
            myExpressionMap.put("PackageDot", pkg);
            SimpleParseSpecification packageName = new SimpleParseSpecification("PackageName");
            pkg.addParseSpecification(packageName, 1, 1);
            pkg.addParseSpecification(getDotDelimiter(), 1, 1);
        }
        return pkg;
    }

    private IParseSpecification getColumnSpecification()
    {
        SequentialCompositeParseSpecification column = (SequentialCompositeParseSpecification) myExpressionMap
                .get("FullColumn");
        if (column == null)
        {
            column = new SequentialCompositeParseSpecification("FullColumn");
            myExpressionMap.put("FullColumn", column);
            SimpleParseSpecification colName = new SimpleParseSpecification("ColumnName");
            column.addParseSpecification(getSchemaDotSpec(), 0, 1);
            column.addParseSpecification(getPackageDotSpec(), 0, 1);
            column.addParseSpecification(colName, 1, 1);
        }
        return column;
    }

    private IParseSpecification getTextSpecification()
    {
        SequentialLoopingParseSpecification text = (SequentialLoopingParseSpecification) myExpressionMap
                .get("SqlText");
        if (text == null)
        {
            text = new SequentialLoopingParseSpecification("SqlText");
            myExpressionMap.put("SqlText", text);

            AnyTokenSpecification anything = new AnyTokenSpecification("Anything");
            OrGroup quoteOrText = new OrGroup("TwoQuotesOrText");
            quoteOrText.addParseSpecification(getDelimiterSpec("TwoQuotes", "''", true), 1, 1);
            quoteOrText.addParseSpecification(anything, 1, 1);

            IParseSpecification quote = getDelimiterSpec("Quote", "'", true);
            text.addParseSpecification(quote, 1, 1);
            text.addParseSpecification(quoteOrText, 0, Integer.MAX_VALUE);
            text.addParseSpecification(quote, 1, 1);
        }
        return text;
    }

    public IParseSpecification getSqlExpression()
    {
        SequentialCompositeParseSpecification expression = (SequentialCompositeParseSpecification) myExpressionMap
                .get("SqlExpression");
        if (expression == null)
        {
            expression = new SequentialCompositeParseSpecification("SqlExpression");
            myExpressionMap.put("SqlExpression", expression);

            //
            // build simple sql expression
            //
            OrGroup simpleSqlExpression = new OrGroup("SimpleSqlExpression");

            simpleSqlExpression.addParseSpecification(getDelimiterSpec("Null", "NULL"), 1, 1);

            simpleSqlExpression.addParseSpecification(getTextSpecification(), 1, 1);

            SequentialCompositeParseSpecification fullCatalogColumn = new SequentialCompositeParseSpecification(
                    "FullCatalogColumn");

            SimpleParseSpecification catalog = new SimpleParseSpecification("Catalog");
            SimpleParseSpecification schema = new SimpleParseSpecification("Schema");
            SimpleParseSpecification tableOrView = new SimpleParseSpecification("TableOrView");
            SimpleParseSpecification colName = new SimpleParseSpecification("ColumnName");

            fullCatalogColumn.addParseSpecification(catalog, 1, 1);
            fullCatalogColumn.addParseSpecification(getDotDelimiter(), 1, 1);
            fullCatalogColumn.addParseSpecification(schema, 1, 1);
            fullCatalogColumn.addParseSpecification(getDotDelimiter(), 1, 1);
            fullCatalogColumn.addParseSpecification(tableOrView, 1, 1);
            fullCatalogColumn.addParseSpecification(getDotDelimiter(), 1, 1);
            fullCatalogColumn.addParseSpecification(colName, 1, 1);

            simpleSqlExpression.addParseSpecification(fullCatalogColumn, 1, 1);
            TypedParseSpecification number = new TypedParseSpecification("Number",
                    TypedParseSpecification.INTEGER);
            simpleSqlExpression.addParseSpecification(number, 1, 1);

            SequentialCompositeParseSpecification sequence = new SequentialCompositeParseSpecification(
                    "Sequence");
            SimpleParseSpecification sequenceName = new SimpleParseSpecification("SequenceName");
            sequence.addParseSpecification(getSchemaDotSpec(), 0, 1);
            sequence.addParseSpecification(getPackageDotSpec(), 0, 1);
            sequence.addParseSpecification(sequenceName, 1, 1);
            sequence.addParseSpecification(getDotDelimiter(), 1, 1);

            OrGroup sequenceIndex = new OrGroup("SequenceIndex");
            sequenceIndex.addParseSpecification(getDelimiterSpec("Currval", "CURRVAL"), 1, 1);
            sequenceIndex.addParseSpecification(getDelimiterSpec("Nextval", "NEXTVAL"), 1, 1);

            sequence.addParseSpecification(sequenceIndex, 1, 1);

            simpleSqlExpression.addParseSpecification(sequence, 1, 1);
            // simpleSqlExpression.addParseSpecification(getColumnSpecification(),
            // 1, 1);

            //
            // build function sql expression
            //
            IParseSpecification functionSqlExpression = getSqlFunctionExpression();

            //
            // build decode sql expression
            //
            //SimpleParseSpecification search = getSimpleParseSpecification("Search");
            //SimpleParseSpecification result = getSimpleParseSpecification("Result");

            SequentialCompositeParseSpecification searchCommaResult = new SequentialCompositeParseSpecification(
                    "SearchCommaResult");

            //searchCommaResult.addParseSpecification(search, 1, 1);
            searchCommaResult.addParseSpecification(getExpression(), 1, 1);
            searchCommaResult.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            //searchCommaResult.addParseSpecification(result, 1, 1);
            searchCommaResult.addParseSpecification(getExpression(), 1, 1);
            
            SequentialCompositeParseSpecification commaSearchResult = new SequentialCompositeParseSpecification(
                    "CommaSearchResult");
            commaSearchResult.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaSearchResult.addParseSpecification(searchCommaResult, 1, 1);

            SequentialCompositeParseSpecification searchAndResult = new SequentialCompositeParseSpecification(
                    "SearchAndResult");
            searchAndResult.addParseSpecification(searchCommaResult, 1, 1);
            searchAndResult.addParseSpecification(commaSearchResult, 0, Integer.MAX_VALUE);

            SequentialCompositeParseSpecification commaDefault = new SequentialCompositeParseSpecification(
                    "CommmaDefault");
            commaDefault.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaDefault.addParseSpecification(getSqlExpression(), 1, 1);

            SequentialCompositeParseSpecification decodeSqlExpression = new SequentialCompositeParseSpecification(
                    "DecodeSqlExpression");
            decodeSqlExpression.addParseSpecification(getDelimiterSpec("Decode", "DECODE"), 1, 1);
            decodeSqlExpression.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            decodeSqlExpression.addParseSpecification(getSqlExpression(), 1, 1);
            decodeSqlExpression.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            decodeSqlExpression.addParseSpecification(searchAndResult, 1, 1);
            decodeSqlExpression.addParseSpecification(commaDefault, 0, 1);
            decodeSqlExpression.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            //
            // build cast sql expression
            //
            
            SequentialCompositeParseSpecification castSqlExpression = new SequentialCompositeParseSpecification(
                    "CastSqlExpression");
            castSqlExpression.addParseSpecification(getDelimiterSpec("Cast", "CAST"), 1, 1);
            castSqlExpression.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            castSqlExpression.addParseSpecification(getSqlExpression(), 1, 1);
            castSqlExpression.addParseSpecification(getDelimiterSpec("As", "AS"), 1, 1);
            castSqlExpression.addParseSpecification(getVariableName(), 1, 1);
            castSqlExpression.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            //
            // build compound sql expression
            //
            OrGroup compoundSqlExpression = new OrGroup("CompoundSqlExpression");

            SequentialCompositeParseSpecification bracketsExpression = new SequentialCompositeParseSpecification(
                    "BracketsExpression");
            bracketsExpression.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            bracketsExpression.addParseSpecification(getSqlExpression(), 1, 1);
            bracketsExpression.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            OrGroup plusMinus = new OrGroup("PlusMinus");
            plusMinus.addParseSpecification(getDelimiterSpec("Plus", "+", true), 1, 1);
            plusMinus.addParseSpecification(getDelimiterSpec("Minus", "-", true), 1, 1);

            SequentialCompositeParseSpecification plusMinusExpression = new SequentialCompositeParseSpecification(
                    "PlusMinusExpression");
            plusMinusExpression.addParseSpecification(plusMinus, 1, 1);
            plusMinusExpression.addParseSpecification(getSqlExpression(), 1, 1);

            SequentialCompositeParseSpecification priorColumnName = new SequentialCompositeParseSpecification(
                    "PriorColumnName");
            priorColumnName.addParseSpecification(getDelimiterSpec("Prior", "PRIOR"), 1, 1);
            priorColumnName.addParseSpecification(getColumnSpecification(), 1, 1);

            compoundSqlExpression.addParseSpecification(bracketsExpression, 1, 1);
            compoundSqlExpression.addParseSpecification(plusMinusExpression, 1, 1);
            compoundSqlExpression.addParseSpecification(priorColumnName, 1, 1);

            //
            // build variable sql expression
            //
            SimpleParseSpecification hostVar = getSimpleParseSpecification("HostVariable");
            SimpleParseSpecification indicatorVar = getSimpleParseSpecification("IndicatorVariable");

            SequentialCompositeParseSpecification indicatorSequence = new SequentialCompositeParseSpecification(
                    "IndicatorSequence");
            indicatorSequence.addParseSpecification(getDelimiterSpec("Indicator", "INDICATOR"),
                                                    0,
                                                    1);
            indicatorSequence.addParseSpecification(getColonDelimiterSpec(), 1, 1);
            indicatorSequence.addParseSpecification(indicatorVar, 1, 1);

            SequentialCompositeParseSpecification variableSqlExpression = new SequentialCompositeParseSpecification(
                    "VariableSqlExpression");
            variableSqlExpression.addParseSpecification(getColonDelimiterSpec(), 1, 1);
            variableSqlExpression.addParseSpecification(hostVar, 1, 1);
            variableSqlExpression.addParseSpecification(indicatorSequence, 0, 1);

            //
            // final preparation
            //
            OrGroup sqlExpressionHolder = new OrGroup("SqlExpressionHolder");
            sqlExpressionHolder.addParseSpecification(decodeSqlExpression, 1, 1);
            sqlExpressionHolder.addParseSpecification(castSqlExpression, 1, 1);
            sqlExpressionHolder.addParseSpecification(compoundSqlExpression, 1, 1);
            sqlExpressionHolder.addParseSpecification(simpleSqlExpression, 1, 1);
            sqlExpressionHolder.addParseSpecification(functionSqlExpression, 1, 1);
            sqlExpressionHolder.addParseSpecification(variableSqlExpression, 1, 1);

            IParseSpecification operator = getOperator();

            SequentialCompositeParseSpecification secondarySqlExpression = new SequentialCompositeParseSpecification(
                    "SecondarySqlExpression");
            secondarySqlExpression.addParseSpecification(operator, 1, 1);
            secondarySqlExpression.addParseSpecification(sqlExpressionHolder, 1, 1);

            expression.addParseSpecification(sqlExpressionHolder, 1, 1);
            expression.addParseSpecification(secondarySqlExpression, 0, Integer.MAX_VALUE);
        }
        return expression;
    }

    private IParseSpecification getOperator()
    {
        OrGroup operator = (OrGroup) myExpressionMap.get("Operator");
        if (operator == null)
        {
            operator = new OrGroup("Operator");
            myExpressionMap.put("Operator", operator);
            operator.addParseSpecification(getDelimiterSpec("Plus", "+", true), 1, 1);
            operator.addParseSpecification(getDelimiterSpec("Minus", "-", true), 1, 1);
            operator.addParseSpecification(getDelimiterSpec("Divide", "/", true), 1, 1);
            operator.addParseSpecification(getDelimiterSpec("Multiply", "*", true), 1, 1);
            operator.addParseSpecification(getDelimiterSpec("Concatenate", "||", true), 1, 1);
        }
        return operator;
    }

    protected IParseSpecification getHostCursorVariableNameSpec()
    {
        SequentialCompositeParseSpecification spec = (SequentialCompositeParseSpecification) myExpressionMap
                .get("HostCursorVariableSeq");
        if (spec == null)
        {
            SimpleParseSpecification name = new SimpleParseSpecification("Name");

            // create the host cursor variable sequence
            spec = new SequentialCompositeParseSpecification("HostCursorVariableSeq");
            spec.addParseSpecification(getColonDelimiterSpec(), 1, 1);
            spec.addParseSpecification(name, 1, 1);
            myExpressionMap.put("HostCursorVariableSeq", spec);
        }
        return spec;
    }

    /**
     * This method gets the parts of boolean that are not captured by the
     * standard expression. It ignores function calls, variables and named
     * constants. It only includes literals, and the alternate boolean form.
     */
    private IParseSpecification getBasicBooleanExpression()
    {
        OrGroup booleanLiteral = new OrGroup("BooleanLiteral");
        booleanLiteral.addParseSpecification(getTrueDelimiterSpec(), 1, 1);
        booleanLiteral.addParseSpecification(getFalseDelimiterSpec(), 1, 1);

        OrGroup booleanBase = new OrGroup("BooleanBase");
        booleanBase.addParseSpecification(booleanLiteral, 1, 1);
        // booleanBase.addParseSpecification(getFunctionStatement(), 1, 1);
        // booleanBase.addParseSpecification(getVariableName(), 1, 1);
        // the above two also do constant, and are taken care of by expression
        booleanBase.addParseSpecification(getFirstHalfOtherBooleanForm(), 1, 1);

        // DelimiterSpec not = getDelimiterSpec("Not", "NOT");

        // SequentialCompositeParseSpecification notBooleanBase = new
        // SequentialCompositeParseSpecification(
        // "NotBooleanBase");
        // notBooleanBase.addParseSpecification(not, 1, 1);
        // notBooleanBase.addParseSpecification(booleanBase, 1, 1);

        // DelimiterSpec and = getAndDelimiterSpec();
        // DelimiterSpec or = getOrDelimiterSpec();

        // OrGroup andOrGroup = new OrGroup("AndOrGroup");
        // andOrGroup.addParseSpecification(and, 1, 1);
        // andOrGroup.addParseSpecification(or, 1, 1);

        // OrGroup primaryBooleanExpression = new
        // OrGroup("PrimaryBooleanExpression");
        // primaryBooleanExpression.addParseSpecification(notBooleanBase, 1, 1);
        // primaryBooleanExpression.addParseSpecification(booleanBase, 1, 1);

        // SequentialCompositeParseSpecification secondaryBooleanExpression =
        // new SequentialCompositeParseSpecification(
        // "SecondaryBooleanExpression");
        // secondaryBooleanExpression.addParseSpecification(andOrGroup, 1, 1);
        // secondaryBooleanExpression.addParseSpecification(primaryBooleanExpression,
        // 1, 1);

        // SequentialCompositeParseSpecification booleanExpression = new
        // SequentialCompositeParseSpecification(
        // "BasicBooleanExpression");
        // booleanExpression.addParseSpecification(primaryBooleanExpression, 1,
        // 1);
        // booleanExpression.addParseSpecification(secondaryBooleanExpression,
        // 0, Integer.MAX_VALUE);

        // return booleanExpression;
        return booleanBase;
    }

    private IParseSpecification getFirstHalfOtherBooleanForm()
    {
        OrGroup otherBooleanForm = (OrGroup) myExpressionMap.get("FirstHalfOtherBooleanForm");

        if (otherBooleanForm == null)
        {
            otherBooleanForm = new OrGroup("FirstHalfOtherBooleanForm");
            myExpressionMap.put(otherBooleanForm.getName(), otherBooleanForm);

            // do collection boolean
            SequentialCompositeParseSpecification collectionNameExistsSequence = new SequentialCompositeParseSpecification(
                    "CollectionNameExistsSequence");
            SimpleParseSpecification name = new SimpleParseSpecification("Name");
            TypedParseSpecification index = new TypedParseSpecification("index",
                    TypedParseSpecification.INTEGER);

            collectionNameExistsSequence.addParseSpecification(name, 1, 1);
            collectionNameExistsSequence.addParseSpecification(getDotDelimiter(), 1, 1);
            collectionNameExistsSequence.addParseSpecification(getExistsDelimiterSpec(), 1, 1);
            collectionNameExistsSequence.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            collectionNameExistsSequence.addParseSpecification(index, 1, 1);
            collectionNameExistsSequence.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            // do cursor boolean
            SimpleParseSpecification sql = new SimpleParseSpecification("SQL");
            OrGroup cursorBooleanStart = new OrGroup("cursorBooleanStart");
            cursorBooleanStart.addParseSpecification(name, 1, 1);
            cursorBooleanStart.addParseSpecification(getHostCursorVariableNameSpec(), 1, 1);
            cursorBooleanStart.addParseSpecification(sql, 1, 1);

            OrGroup cursorBooleanEnd = new OrGroup("CursorBooleanEnd");
            cursorBooleanEnd.addParseSpecification(getFoundDelimiterSpec(), 1, 1);
            cursorBooleanEnd.addParseSpecification(getIsOpenDelimiterSpec(), 1, 1);
            cursorBooleanEnd.addParseSpecification(getNotFoundDelimiterSpec(), 1, 1);

            SequentialCompositeParseSpecification cursorBoolean = new SequentialCompositeParseSpecification(
                    "CursorBoolean");
            cursorBoolean.addParseSpecification(cursorBooleanStart, 1, 1);
            cursorBoolean.addParseSpecification(getPercentageDelimiterSpec(), 1, 1);
            cursorBoolean.addParseSpecification(cursorBooleanEnd, 1, 1);

            // do easy half of other boolean
            otherBooleanForm.addParseSpecification(collectionNameExistsSequence, 1, 1);
            otherBooleanForm.addParseSpecification(cursorBoolean, 1, 1);
        }
        return otherBooleanForm;
    }

    private IParseSpecification getSecondHalfOtherBooleanForm()
    {
        OrGroup expressionBooleanEnd = (OrGroup) myExpressionMap.get("ExpressionBooleanEnd");

        if (expressionBooleanEnd == null)
        {
            expressionBooleanEnd = new OrGroup("ExpressionBooleanEnd");
            myExpressionMap.put(expressionBooleanEnd.getName(), expressionBooleanEnd);

            // do expression sequence boolean
            SequentialCompositeParseSpecification rhsExpression = new SequentialCompositeParseSpecification(
                    "RhsExpression");
            rhsExpression.addParseSpecification(getRelationalOperatorGroup(), 1, 1);
            rhsExpression.addParseSpecification(getExpression(), 1, 1);

            SequentialCompositeParseSpecification nullRhsExpression = new SequentialCompositeParseSpecification(
                    "NullRhsExpression");
            nullRhsExpression.addParseSpecification(getDelimiterSpec("Is", "IS"), 1, 1);
            nullRhsExpression.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            nullRhsExpression.addParseSpecification(getDelimiterSpec("Null", "NULL"), 1, 1);

            // like pattern
            SequentialCompositeParseSpecification likePatternExpression = new SequentialCompositeParseSpecification(
                    "LikePatternExpression");
            likePatternExpression.addParseSpecification(getDelimiterSpec("Like", "LIKE"), 1, 1);
            likePatternExpression.addParseSpecification(getTextSpecification(), 1, 1);

            // between
            SequentialCompositeParseSpecification betweenExpression = new SequentialCompositeParseSpecification(
                    "BetweenExpression");
            betweenExpression.addParseSpecification(getDelimiterSpec("Between", "BETWEEN"), 1, 1);
            betweenExpression.addParseSpecification(getExpression(), 1, 1);
            betweenExpression.addParseSpecification(getAndDelimiterSpec(), 1, 1);
            betweenExpression.addParseSpecification(getExpression(), 1, 1);

            // in
            SequentialCompositeParseSpecification inExpression = new SequentialCompositeParseSpecification(
                    "InExpression");
            inExpression.addParseSpecification(getDelimiterSpec("In", "IN"), 1, 1);
            inExpression.addParseSpecification(getOpenBracketDelimiter(), 1, 1);
            inExpression.addParseSpecification(getExpression(), 1, 1);
            inExpression.addParseSpecification(getCommaExpression(), 0, 1);
            inExpression.addParseSpecification(getCloseBracketDelimiter(), 1, 1);

            // complex or group
            OrGroup complexOrGroup = new OrGroup("ComplexOrGroup");
            complexOrGroup.addParseSpecification(likePatternExpression, 1, 1);
            complexOrGroup.addParseSpecification(betweenExpression, 1, 1);
            complexOrGroup.addParseSpecification(inExpression, 1, 1);

            SequentialCompositeParseSpecification complexRhsExpression = new SequentialCompositeParseSpecification(
                    "ComplexRhsExpression");
            complexRhsExpression.addParseSpecification(getDelimiterSpec("Not", "NOT"), 0, 1);
            complexRhsExpression.addParseSpecification(complexOrGroup, 1, 1);

            expressionBooleanEnd.addParseSpecification(rhsExpression, 1, 1);
            expressionBooleanEnd.addParseSpecification(nullRhsExpression, 1, 1);
            expressionBooleanEnd.addParseSpecification(complexRhsExpression, 1, 1);
        }
        return expressionBooleanEnd;
    }



    /**
     * This method gets the specification for a comma followed by sql
     * expression.
     */
    private IParseSpecification getCommaSqlExpression()
    {
        SequentialCompositeParseSpecification commaSqlExpression = new SequentialCompositeParseSpecification(
                "CommaSqlExpression");
        if (commaSqlExpression == null)
        {
            commaSqlExpression = new SequentialCompositeParseSpecification("CommaSqlExpression");
            myExpressionMap.put("CommaSqlExpression", commaSqlExpression);
            commaSqlExpression.addParseSpecification(getCommaDelimiterSpec(), 1, 1);
            commaSqlExpression.addParseSpecification(getSqlExpression(), 1, 1);
        }
        return commaSqlExpression;
    }

    private OrGroup getRelationalOperatorGroup()
    {
        OrGroup relationalOperatorGroup = (OrGroup) myExpressionMap.get("RelationalOperatorGroup");
        if (relationalOperatorGroup == null)
        {
            relationalOperatorGroup = new OrGroup("RelationalOperatorGroup");
            myExpressionMap.put("RelationalOperatorGroup", relationalOperatorGroup);
            relationalOperatorGroup.addParseSpecification(getDelimiterSpec("Equals", "=", true),
                                                          1,
                                                          1);
            relationalOperatorGroup
                    .addParseSpecification(getDelimiterSpec("NotEquals", "!=",true), 1, 1);
            relationalOperatorGroup
                    .addParseSpecification(getDelimiterSpec("NotEquals2", "^=", true), 1, 1);
            relationalOperatorGroup
                    .addParseSpecification(getDelimiterSpec("NotEquals3", "<>", true), 1, 1);
            relationalOperatorGroup.addParseSpecification(getDelimiterSpec("LessThanEquals",
                                                                           "<=",
                                                                           true), 1, 1);
            relationalOperatorGroup.addParseSpecification(getDelimiterSpec("GreaterThanEquals",
                                                                           ">=",
                                                                           true), 1, 1);
            relationalOperatorGroup
                    .addParseSpecification(getDelimiterSpec("GreaterThan", ">", true), 1, 1);
            relationalOperatorGroup.addParseSpecification(getDelimiterSpec("LessThan", "<"),
                                                          1,
                                                          1);
        }
        return relationalOperatorGroup;
    }

    /**
     * This method gets a simple parse specification with the supplied name. If
     * one is not already stored with this name, it will be created. If the
     * specification returned is NOT a simple parse specification, a
     * ClassCastException will be thrown.
     * 
     * @param name The name of the parse specification to retrieve.
     */
    private SimpleParseSpecification getSimpleParseSpecification(String name)
    {
        SimpleParseSpecification toReturn = (SimpleParseSpecification) myExpressionMap.get(name);
        if (toReturn == null)
        {
            toReturn = new SimpleParseSpecification(name);
            myExpressionMap.put(name, toReturn);
        }
        return toReturn;
    }

    /**
     * This method gets a delimiter specification with the supplied name and
     * actual delimiter. If one is not already stored with this name, it will be
     * created. If a delimiter spec with the supplied
     * <code>actualDelimiter</code> already exists with a different name, this
     * will be ignored, and a new one will be created with the new
     * <code>name</code> supplied. If there is already a specification stored
     * against the supplied <code>name</code> but with a different delimiter,
     * then an IllegalArgumentException will be thrown. If the specification
     * returned is NOT a delimiter specification, a ClassCastException will be
     * thrown.
     * 
     * @param name The name of the spec to retrieve or create.
     * 
     * @param actualDelimiter The actual delimiter that the retrieved spec
     *            should look for.
     */
    private DelimiterSpec getDelimiterSpec(String name, String actualDelimiter, boolean isStandalone)
    {
        DelimiterSpec spec = (DelimiterSpec) myExpressionMap.get(name);
        if (spec == null)
        {
            spec = new DelimiterSpec(name, actualDelimiter, isStandalone);
            myExpressionMap.put(name, spec);
        }
        if (!spec.getString().equals(actualDelimiter))
        {
            throw new IllegalArgumentException("A delimiter spec with the name [" + name
                    + "] already exists, but the actual delimiter is [" + spec.getString()
                    + "] when it should be [" + actualDelimiter + "]");
        }
        return spec;
    }

    private DelimiterSpec getDelimiterSpec(String name, String actualDelimiter)
    {
        return getDelimiterSpec(name, actualDelimiter, false);
    }

    private DelimiterSpec getColonDelimiterSpec()
    {
        return getDelimiterSpec("Colon", ":", true);
    }

    private DelimiterSpec getCommaDelimiterSpec()
    {
        return getDelimiterSpec("Comma", ",", true);
    }

    private DelimiterSpec getOrDelimiterSpec()
    {
        return getDelimiterSpec("Or", "OR");
    }

    private DelimiterSpec getAndDelimiterSpec()
    {
        return getDelimiterSpec("And", "AND");
    }

    private DelimiterSpec getTrueDelimiterSpec()
    {
        return getDelimiterSpec("True", "TRUE");
    }

    private DelimiterSpec getFalseDelimiterSpec()
    {
        return getDelimiterSpec("False", "FALSE");
    }

    private DelimiterSpec getExistsDelimiterSpec()
    {
        return getDelimiterSpec("Exists", "EXISTS");
    }

    private DelimiterSpec getPercentageDelimiterSpec()
    {
        return getDelimiterSpec("Percentage", "%", true);
    }

    private DelimiterSpec getFoundDelimiterSpec()
    {
        return getDelimiterSpec("Found", "FOUND");
    }

    private DelimiterSpec getIsOpenDelimiterSpec()
    {
        return getDelimiterSpec("IsOpen", "ISOPEN");
    }

    private DelimiterSpec getNotFoundDelimiterSpec()
    {
        return getDelimiterSpec("NotFound", "NOTFOUND");
    }

    private DelimiterSpec getCloseBracketDelimiter()
    {
        return getDelimiterSpec("CloseBracket", ")", true);
    }

    private DelimiterSpec getOpenBracketDelimiter()
    {
        return getDelimiterSpec("OpenBracket", "(", true);
    }

    private DelimiterSpec getDotDelimiter()
    {
        return getDelimiterSpec("Dot", ".", true);
    }
}
