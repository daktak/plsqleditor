package plsqleditor.parser.plsql;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.framework.ContainsLoopException;
import plsqleditor.parser.framework.IParseResult;
import plsqleditor.parser.framework.IParseSpecification;
import plsqleditor.parser.framework.ParseException;
import plsqleditor.parser.plsql.PlSqlParserGenerator;
import plsqleditor.parser.util.IInput;
import plsqleditor.parser.util.StringInput;
import junit.framework.TestCase;

public class TestPlSqlParserGenerator extends TestCase
{
    
    public void testHostVariableSequence()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification hostCursorVariableName = generator.getHostCursorVariableNameSpec();
        
        IInput[] inputs = new StringInput[]{new StringInput(":variable_name")};

        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
                hostCursorVariableName.checkForInfiniteLoop(new Stack());
                IParseResult result = hostCursorVariableName.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }
    /**
     * This method tests all the combos of a function statement with a simple
     * string variable name for a parameter.
     */
    public void testFunctionStatementBasicParams()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification functionStatement = generator.getFunctionStatement();
        StringInput schmPkgFuncNoBrackets = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses");
        StringInput pkgFuncNoBrackets = new StringInput("ip_address_manager.allocate_addresses");
        StringInput funcNoBrackets = new StringInput("allocate_addresses");
        StringInput schmPkgFuncBrackets = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses()");
        StringInput pkgFuncBrackets = new StringInput("ip_address_manager.allocate_addresses()");
        StringInput funcBrackets = new StringInput("allocate_addresses()");

        StringInput schmPkgFuncOneNonQualifiedParam = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(param1)");
        StringInput pkgFuncOneNonQualifiedParam = new StringInput(
                "ip_address_manager.allocate_addresses(param1)");
        StringInput funcOneNonQualifiedParam = new StringInput("allocate_addresses(param1)");

        StringInput schmPkgFuncTwoNonQualifiedParams = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(param1, param2)");
        StringInput pkgFuncTwoNonQualifiedParams = new StringInput(
                "ip_address_manager.allocate_addresses(param1, param2)");
        StringInput funcTwoNonQualifiedParams = new StringInput(
                "allocate_addresses(param1, param2)");

        StringInput schmPkgFuncOneQualifiedParam = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(p1name => param1)");
        StringInput pkgFuncOneQualifiedParam = new StringInput(
                "ip_address_manager.allocate_addresses(p1name => param1)");
        StringInput funcOneQualifiedParam = new StringInput("allocate_addresses(p1name => param1)");

        StringInput schmPkgFuncTwoQualifiedParams = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(p1name => param1"
                        + "                                            ,p2name => param2)");
        StringInput pkgFuncTwoQualifiedParams = new StringInput(
                "ip_address_manager.allocate_addresses(p1name => param1"
                        + "                                     ,p2name => param2)");
        StringInput funcTwoQualifiedParams = new StringInput("allocate_addresses(p1name => param1"
                + "                  ,p2name => param2)");

        IInput[] inputs = new StringInput[]{schmPkgFuncNoBrackets, pkgFuncNoBrackets,
                funcNoBrackets, schmPkgFuncBrackets, pkgFuncBrackets, funcBrackets,
                schmPkgFuncOneNonQualifiedParam, pkgFuncOneNonQualifiedParam,
                funcOneNonQualifiedParam, schmPkgFuncTwoNonQualifiedParams,
                pkgFuncTwoNonQualifiedParams, funcTwoNonQualifiedParams,
                schmPkgFuncOneQualifiedParam, pkgFuncOneQualifiedParam, funcOneQualifiedParam,
                schmPkgFuncTwoQualifiedParams, pkgFuncTwoQualifiedParams, funcTwoQualifiedParams};

        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
            	functionStatement.checkForInfiniteLoop(new Stack());
                IParseResult result = functionStatement.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    /**
     * This method tests all the combos of a function statement with a complex
     * expression for a parameter.
     */
    public void testFunctionStatementComplexParams()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification functionStatement = generator.getFunctionStatement();

        StringInput schmPkgFuncOneNonQualifiedParam = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(invmgr.somepkg.result())");
        StringInput pkgFuncOneNonQualifiedParam = new StringInput(
                "ip_address_manager.allocate_addresses(someotherpkg.result(a,b,c))");
        StringInput funcOneNonQualifiedParam = new StringInput(
                "allocate_addresses(pkg2.param_func(p1name => a))");

        StringInput schmPkgFuncTwoNonQualifiedParams = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(pkg2.param_func(p1name => a), param2)");
        StringInput pkgFuncTwoNonQualifiedParams = new StringInput(
                "ip_address_manager.allocate_addresses(pkg2.param_func(p1name => a, p2name=>b), a * a, b/a,c)");
        
        StringInput preFuncTwoNonQualifiedParams1 = new StringInput("pkg2.param_func(p1name => a * a, p2name=>b OR FALSE)");
        StringInput preFuncTwoNonQualifiedParams2 = new StringInput("allocate_addresses(pkg2.param_func(p1name => a * a, p2name=>b OR FALSE))");
        StringInput preFuncTwoNonQualifiedParams3 = new StringInput("allocate_addresses(a * (a-5))");
        StringInput preFuncTwoNonQualifiedParams4 = new StringInput("allocate_addresses(pkg2.param_func(p1name => a * a, p2name=>b OR FALSE), a * (a-5))");
        
        StringInput funcTwoNonQualifiedParams = new StringInput(
                "allocate_addresses(pkg2.param_func(p1name => a * a, p2name=>b OR FALSE), a * (a - 5), param_func2(d || '.txt',b/a),c OR NOT (f AND TRUE))");

        StringInput schmPkgFuncOneQualifiedParam = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(p1name => pkg2.param_func(p1name => a, p2name=>b))");
        StringInput pkgFuncOneQualifiedParam = new StringInput(
                "ip_address_manager.allocate_addresses(p1name => a * a)");
        StringInput funcOneQualifiedParam = new StringInput(
                "allocate_addresses(p1name => param_func2(d,b/a))");

        StringInput schmPkgFuncTwoQualifiedParams = new StringInput(
                "svcmgr.ip_address_manager.allocate_addresses(p1name => pkg2.param_func(p1name => a, p2name=>b)"
                        + "                                            ,p2name => (a/b)*a)");
        StringInput pkgFuncTwoQualifiedParams = new StringInput(
                "ip_address_manager.allocate_addresses(p1name => param1"
                        + "                                     ,p2name => param_func2(d,b/a))");
        StringInput funcTwoQualifiedParams = new StringInput("allocate_addresses(p1name => param1"
                + "                  ,p2name => f(a) + y(b))");

        IInput[] inputs = new StringInput[]{schmPkgFuncOneNonQualifiedParam,
                pkgFuncOneNonQualifiedParam, funcOneNonQualifiedParam,
                preFuncTwoNonQualifiedParams1,preFuncTwoNonQualifiedParams2,
                preFuncTwoNonQualifiedParams3,preFuncTwoNonQualifiedParams4,
                schmPkgFuncTwoNonQualifiedParams, pkgFuncTwoNonQualifiedParams,
                funcTwoNonQualifiedParams, schmPkgFuncOneQualifiedParam, pkgFuncOneQualifiedParam,
                funcOneQualifiedParam, schmPkgFuncTwoQualifiedParams, pkgFuncTwoQualifiedParams,
                funcTwoQualifiedParams};

        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
            	functionStatement.checkForInfiniteLoop(new Stack());
                IParseResult result = functionStatement.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    public void testVariableName()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification variableNameSpec = generator.getVariableName();

        List inputList = new ArrayList();

        inputList.add(new StringInput("var"));
        inputList.add(new StringInput("object.fieldname"));
        inputList.add(new StringInput("schema.pkg.fieldname"));
        inputList.add(new StringInput("catalog.schema.pkg.recordname"));
        inputList.add(new StringInput("schema.pkg.collection(1)"));
        inputList.add(new StringInput("schema.pkg.collection(index)"));
        inputList.add(new StringInput(":variable_name"));
        inputList.add(new StringInput(":hostname:variablename"));
        inputList.add(new StringInput("collection(index)"));
        inputList.add(new StringInput("collection(5)"));

        StringInput[] inputs = (StringInput[]) inputList.toArray(new StringInput[inputList.size()]);
        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
            	variableNameSpec.checkForInfiniteLoop(new Stack());
                IParseResult result = variableNameSpec.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    public void testGetSqlExpression()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification sqlExpression = generator.getSqlExpression();

        List inputList = new ArrayList();

        inputList.add(new StringInput("var"));
        inputList.add(new StringInput("object.fieldname"));
        inputList.add(new StringInput("schema.pkg.fieldname"));
        inputList.add(new StringInput("catalog.schema.pkg.recordname"));
        inputList.add(new StringInput("schema.pkg.collection(1)"));
        inputList.add(new StringInput("schema.pkg.collection(index)"));
        inputList.add(new StringInput(":variable_name"));
        inputList.add(new StringInput(":hostname:variablename"));
        inputList.add(new StringInput("collection(index)"));
        inputList.add(new StringInput("collection(5)"));

        inputList.add(new StringInput("svcmgr.ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("svcmgr.ip_address_manager.allocate_addresses(a,b,c)"));
        inputList
                .add(new StringInput(
                        "svcmgr.ip_address_manager.allocate_addresses(schm.pkg.meth(n,v),schm.pkg.field,pkg.method(1,12 * 4))"));
        inputList.add(new StringInput("'afsdffd'"));
        inputList.add(new StringInput("1 + 4"));
        inputList.add(new StringInput("5 * 8 / 5"));
        inputList.add(new StringInput("'afsdffd' || fieldName(1)"));
        inputList.add(new StringInput("'afs''d ''ffd' || fieldName(i+1)"));
        inputList.add(new StringInput("value + 5"));
        inputList.add(new StringInput("cast(expr as schm.pkg.specified_type)"));
        inputList
                .add(new StringInput("cast (common.fss_varchar_table as dmbs_sql.varchar2_table)"));
        inputList.add(new StringInput("cast( var AS NUMBER)"));
        /* inputList.add(new StringInput("cast( decode('a','b',var_c) AS NUMBER)"));
        inputList
                .add(new StringInput(
                        "cast( schm.pkg.method(param1, param2, 'quoted''param') || other_var AS common.utilities.fss_varchar_table)"));

        // will cause problem because first character will be interpreted as ''
        // rather than '
        //inputList.add(new StringInput(
        //        "cast( schm.pkg.method(param1, param2, '''quoted''param')as varchar2_table)"));
        inputList.add(new StringInput("decode(expr,search,result,default)"));
        inputList.add(new StringInput("decode(expr,search,result)"));
        inputList.add(new StringInput("decode(expr,search,result,search2,result2,'UNFOUND')"));
        inputList.add(new StringInput("decode(expr,search,result,search2,result2,NULL)"));
        inputList.add(new StringInput("decode(expr,search,result,'search2','re''sult2')"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,i * 5,search2,result2,'begin ' || var || ' end',result3,search4,result4)"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,result,search2,result2,'begin ' || var || ' end',var,search4,result4,dflt)"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,result,search2,result2,'begin ' || var || ' end',var,search4,result4,dflt)"));
        inputList.add(new StringInput("decode(packge.getResults(a,4+b)" + "      ,search,result"
                + "      ,pkg2.getSearchValue()"
                + "      ,pkg.getCorrespondingResult(pkg2.getSearchValue(),NULL)"
                + "      ,'begin ' || var || ' end'" + "      ,var" + "      ,search4"
                + "      ,result4" + "      ,dflt)"));
        inputList.add(new StringInput("decode(packge.getResults(a,4+b)" + "      ,search,result"
                + "      ,cast ( pkg2.getSearchValue() as VARCHAR2)"
                + "      ,pkg.getCorrespondingResult(pkg2.getSearchValue(),NULL)"
                + "      ,'begin ' || var || ' end'" + "      ,var" + "      ,search4"
                + "      ,result4" + "      ,dflt)"));
    //*/
        StringInput[] inputs = (StringInput[]) inputList.toArray(new StringInput[inputList.size()]);
        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
            	sqlExpression.checkForInfiniteLoop(new Stack());
                IParseResult result = sqlExpression.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    public void testGetExpression()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification expression = generator.getExpression();

        List inputList = new ArrayList();

        inputList.add(new StringInput("var"));
        inputList.add(new StringInput("object.fieldname"));
        inputList.add(new StringInput("schema.pkg.fieldname"));
        inputList.add(new StringInput("schema.pkg.collection(1)"));
        inputList.add(new StringInput("schema.pkg.collection(index)"));
        inputList.add(new StringInput(":variable_name"));
        inputList.add(new StringInput(":hostname:variablename"));
        inputList.add(new StringInput("collection(index)"));
        inputList.add(new StringInput("collection(5)"));

        inputList.add(new StringInput("a_function()"));
        inputList.add(new StringInput("pkg.a_function()"));
        inputList.add(new StringInput("pkg_syn.a_function()"));
        inputList.add(new StringInput("svcmgr.ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("ip_address_manager.allocate_addresses()"));
        inputList.add(new StringInput("svcmgr.ip_address_manager.allocate_addresses(a,b,c)"));
        inputList
                .add(new StringInput(
                        "svcmgr.ip_address_manager.allocate_addresses(schm.pkg.meth(n,v),schm.pkg.field,pkg.method(1,12 * 4))"));
        inputList.add(new StringInput("'afsdffd'"));
        inputList.add(new StringInput("1 + 4"));
        inputList.add(new StringInput("5 * 8 / 5"));
        inputList.add(new StringInput("'afsdffd' || fieldName(1)"));
        inputList.add(new StringInput("'afs''d ''ffd' || fieldName(i+1)"));
        inputList.add(new StringInput("value + 5"));
        inputList.add(new StringInput("cast(expr as schm.pkg.specified_type)"));
        inputList
                .add(new StringInput("cast (common.fss_varchar_table as dmbs_sql.varchar2_table)"));
        inputList.add(new StringInput("cast( var AS NUMBER)"));
        inputList.add(new StringInput("cast( decode('a','b',var_c) AS NUMBER)"));
        inputList
                .add(new StringInput(
                        "cast( schm.pkg.method(param1, param2, 'quoted''param') || other_var AS common.utilities.fss_varchar_table)"));

        // will cause problem because first character will be interpreted as ''
        // rather than '
        //inputList.add(new StringInput(
        //        "cast( schm.pkg.method(param1, param2, '''quoted''param')as varchar2_table)"));
        inputList.add(new StringInput("decode(expr,search,result,default)"));
        inputList.add(new StringInput("decode(expr,search,result)"));
        inputList.add(new StringInput("decode(expr,search,result,search2,result2,'UNFOUND')"));
        inputList.add(new StringInput("decode(expr,search,result,search2,result2,NULL)"));
        inputList.add(new StringInput("decode(expr,search,result,'search2','re''sult2')"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,i * 5,search2,result2,'begin ' || var || ' end',result3,search4,result4)"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,result,search2,result2,'begin ' || var || ' end',var,search4,result4,dflt)"));
        inputList
                .add(new StringInput(
                        "decode(expr,search,result,search2,result2,'begin ' || var || ' end',var,search4,result4,dflt)"));
        inputList.add(new StringInput("decode(packge.getResults(a,4+b)" + "      ,search,result"
                + "      ,pkg2.getSearchValue()"
                + "      ,pkg.getCorrespondingResult(pkg2.getSearchValue(),NULL)"
                + "      ,'begin ' || var || ' end'" + "      ,var" + "      ,search4"
                + "      ,result4" + "      ,dflt)"));
        inputList.add(new StringInput("decode(packge.getResults(a,4+b)" + "      ,search,result"
                + "      ,cast ( pkg2.getSearchValue() as VARCHAR2)"
                + "      ,pkg.getCorrespondingResult(pkg2.getSearchValue(),NULL)"
                + "      ,'begin ' || var || ' end'" + "      ,var" + "      ,search4"
                + "      ,result4" + "      ,dflt)"));

        StringInput[] inputs = (StringInput[]) inputList.toArray(new StringInput[inputList.size()]);
        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
                expression.checkForInfiniteLoop(new Stack());
                IParseResult result = expression.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    public void testGetSqlCondition()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification sqlCondition = generator.getSqlCondition();

        List inputList = new ArrayList();

        // null
        inputList.add(new StringInput("var is null"));
        inputList.add(new StringInput("var is not null"));
        inputList.add(new StringInput("schm.pkg.result(a,b,g/5) is NOT NULL"));
        // compound null
        inputList.add(new StringInput("( var is null )"));
        inputList.add(new StringInput("NOT ( var is null )"));
        inputList.add(new StringInput("( NOT var is null )"));
        inputList.add(new StringInput("( NOT ( var is null ))"));

        // range
        inputList.add(new StringInput(
                "schm.pkg.result(a,b,g/5) NOT between 1 and pkg2.getEndBetween()"));
        inputList.add(new StringInput("var between low_var and common.high_var_const"));
        // compound range
        inputList.add(new StringInput(
                "NOT schm.pkg.result(a,b,g/5) NOT between 1 and pkg2.getEndBetween()"));
        inputList.add(new StringInput("NOT (var between low_var and common.high_var_const)"));
        inputList.add(new StringInput("(var between low_var and common.high_var_const)"));

        // exists
        inputList.add(new StringInput("exists ( select * from tab )"));
        // compound exists
        inputList.add(new StringInput("(exists ( select * from tab ))"));
        inputList.add(new StringInput("NOT (exists ( select * from tab ))"));
        inputList.add(new StringInput("NOT ( NOT exists ( select * from tab ))"));

        // like
      
        inputList.add(new StringInput("var = othervar"));
        //inputList.add(new StringInput("var = true"));
        inputList.add(new StringInput("var <> othervar"));
        inputList.add(new StringInput("var NOT LIKE '%hello'"));
        inputList.add(new StringInput("schm.pkg.get_result(4) LIKE my_like_comparator"));
        inputList.add(new StringInput("schm.pkg.get_result(4) LIKE my_like_comparator escape 'G'"));
        inputList.add(new StringInput("schm.pkg.get_result(4) LIKE 'hi there' escape '/'"));
        inputList.add(new StringInput(
                "schm.pkg.get_result(4) LIKE 'start%' || my_like_comparator || '%'"));
        inputList.add(new StringInput(
                "schm.pkg.get_result(4) LIKE 'start%' || my_like_comparator || '%' escape '/'"));
        // compound like
        inputList.add(new StringInput("(var NOT LIKE '%hello')"));
        inputList.add(new StringInput("NOT schm.pkg.get_result(4) LIKE my_like_comparator"));
        inputList.add(new StringInput(
                "(not schm.pkg.get_result(4) LIKE 'start%' || my_like_comparator || '%')"));
        inputList
                .add(new StringInput(
                        "( not schm.pkg.get_result(4) LIKE 'start%' || my_like_comparator || '%' escape '/')"));

        // membership
        inputList.add(new StringInput("var NOT in ('hello')"));
        inputList.add(new StringInput("var NOT in ('hello', 'goodbye')"));
        inputList.add(new StringInput(
                "pkg.method(param1,param2/5,param3 || '.ext') IN('hello', pkg.const_value)"));
        inputList.add(new StringInput("schm.pkg.check_value NOT in (var_a, var_b)"));

        inputList.add(new StringInput("var NOT in (select * from tab)"));
        inputList
                .add(new StringInput(
                        "var NOT in (select first_entry from ports )"));
//        inputList
//        .add(new StringInput(
//                "var NOT in select first_entry from ports"));
        inputList
        .add(new StringInput(
                "var NOT in (select first_entry as \"first entry\" from ports where second_entry > 5)"));
        inputList.add(new StringInput(
                "pkg.method(param1,param2/5,param3 || '.ext') IN(select 1 from dual)"));
        inputList
                .add(new StringInput(
                        "schm.pkg.check_value NOT in (select a from b where val > val2/5 and val in (10,20,30))"));
        // compound membership
        inputList.add(new StringInput("(var NOT in (select * from tab))"));
        inputList
                .add(new StringInput(
                        "NOT (NOT var NOT in (select first_entry as \"first entry\" from ports where second_entry > 5))"));
        inputList.add(new StringInput(
                "NOT (pkg.method(param1,param2/5,param3 || '.ext') IN(select 1 from dual))"));
        inputList
                .add(new StringInput(
                        "( NOT schm.pkg.check_value NOT in (select a from b where val > val2/5 and val in (10,20,30)))"));

        // simple comparison
        inputList.add(new StringInput("'afsdffd' <> 'hi'"));
        inputList.add(new StringInput("1 + 4 = schm.pkg.static_var"));
        inputList.add(new StringInput("5 * 8 / 5 < my_var"));
        inputList.add(new StringInput("'afsdffd' || fieldName(1) != value || 'other string'"));
        inputList.add(new StringInput("'afsdffd' || fieldName(1) ^= value || pkg.const_val"));
        /*inputList.add(new StringInput("value - 5 <= (select count(1) from t_ports)"));
        inputList.add(new StringInput("value + 5 >= value * 5"));
        inputList
                .add(new StringInput(
                        "cast(value as Number) + 5 > (select max(port_number) from t_port p where p.parent_id = my_known_card_id)"));
        // compound simple comparison
        inputList.add(new StringInput("('afsdffd' <> 'hi')"));
        inputList.add(new StringInput("( NOT 1 + 4 = schm.pkg.static_var)"));
        inputList.add(new StringInput("NOT ('afsdffd' || fieldName(1) ^= value || pkg.const_val)"));
        inputList
                .add(new StringInput(
                        "NOT ( NOT cast(value as Number) + 5 > (select max(port_number) from t_port p where p.parent_id = my_known_card_id))"));

        // group comparison
        inputList.add(new StringInput("value - 5 <= ANY (select port_count from t_ports)"));
        inputList
                .add(new StringInput(
                        "cast(value as Number) + 5 > ALL (select port_number from t_port p where p.parent_id = my_known_card_id)"));
        inputList
                .add(new StringInput(
                        "'name looks like' <> SOME (select port_name from t_port p where p.parent_id = my_known_card_id)"));

        inputList.add(new StringInput(
                "(value - 5, value, value+5) = ANY (select port_count from t_ports)"));
        inputList
                .add(new StringInput(
                        "(cast(value as Number) + 5, 4) != ALL (select port_number from t_port p where p.parent_id = my_known_card_id)"));
        inputList
                .add(new StringInput(
                        "('name looks like', NULL, ' ') = SOME (select port_name from t_port p where p.parent_id = my_known_card_id)"));
        // compound group comparison
        inputList.add(new StringInput("(value - 5 <= ANY (select port_count from t_ports))"));
        inputList
                .add(new StringInput(
                        "NOT(cast(value as Number) + 5 > ALL (select port_number from t_port p where p.parent_id = my_known_card_id))"));
        inputList
                .add(new StringInput(
                        "NOT(NOT 'name looks like' <> SOME (select port_name from t_port p where p.parent_id = my_known_card_id))"));

        inputList.add(new StringInput(
                "((value - 5, value, value+5) = ANY (select port_count from t_ports))"));
        inputList
                .add(new StringInput(
                        "NOT (cast(value as Number) + 5, 4) != ALL (select port_number from t_port p where p.parent_id = my_known_card_id)"));
        inputList
                .add(new StringInput(
                        "NOT(NOT('name looks like', NULL, ' ') = SOME (select port_name from t_port p where p.parent_id = my_known_card_id))"));
        //  */
        StringInput[] inputs = (StringInput[]) inputList.toArray(new StringInput[inputList.size()]);
        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
            	sqlCondition.checkForInfiniteLoop(new Stack());
                IParseResult result = sqlCondition.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    /**
     * This method tests that a sql query specification can be correctly parsed.
     */
    public void testGetSqlQuerySpec()
    {
        PlSqlParserGenerator generator = new PlSqlParserGenerator();
        IParseSpecification query = generator.getSqlQuerySpec();

        List inputList = new ArrayList();

        inputList.add(new StringInput("select * from tab"));
        inputList.add(new StringInput("select object blah from tab"));
        inputList.add(new StringInput("select object as doodah from tab"));
        inputList.add(new StringInput("select object \"x\" from tab"));
        inputList.add(new StringInput("select object \"doodah\" from tab"));
        inputList.add(new StringInput("select object from tab"));
        inputList.add(new StringInput("select object \"hah hah\" from tab"));
        inputList.add(new StringInput("select object as \"blah blah\" from tab"));
        inputList.add(new StringInput("select t.* from tab"));
        inputList.add(new StringInput("select t.name, t.tabtype from tab t"));
        inputList.add(new StringInput("select count(object_name) from t_ports"));
        
        
        StringInput[] inputs = (StringInput[]) inputList.toArray(new StringInput[inputList.size()]);
        Position curPos = new Position(0, 0);
        Position resultingPos = new Position(0, 0);

        for (int i = 0; i < inputs.length; i++)
        {
            IInput input = inputs[i];
            try
            {
                query.checkForInfiniteLoop(new Stack());
                IParseResult result = query.parse(curPos, resultingPos, input);
                assertEquals(input.get(), result.getText());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                assertTrue("Caught unexpected exception", false);
            }
            catch (ContainsLoopException e)
            {
                e.printStackTrace();
                assertTrue("Caught infinite loop exception", false);
            }
        }
    }

    public void testGetSqlSubQuerySpec()
    {
        assertTrue(false);
    }

    public void testGetSqlSelectStatemet()
    {
        assertTrue(false);
    }

    public void testGetSqlInsertStatemet()
    {
        assertTrue(false);
    }

    public void testGetSqlUpdateStatemet()
    {
        assertTrue(false);
    }

    public void testGetSqlDeleteStatemet()
    {
        assertTrue(false);
    }
}
