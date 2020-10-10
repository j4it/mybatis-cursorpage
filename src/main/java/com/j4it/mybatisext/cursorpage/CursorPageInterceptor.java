package com.j4it.mybatisext.cursorpage;

import com.j4it.mybatisext.cursorpage.constant.SearchDirection;
import com.j4it.mybatisext.cursorpage.dialect.IDialect;
import com.j4it.mybatisext.cursorpage.util.PluginUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 游标分页拦截器
 *
 * @author meanfu
 * @date 2019/09/05
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class CursorPageInterceptor implements Interceptor {
    private static final Log log = LogFactory.getLog(CursorPageInterceptor.class);
    private volatile IDialect dialect;
    private String defaultDialectClass = "com.j4it.mybatisext.cursorpage.CursorPageHelper";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Connection connection = (Connection) invocation.getArgs()[0];
            StatementHandler statementHandler = PluginUtil.getRealTarget(invocation.getTarget(), StatementHandler.class);
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            checkDialect();
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            if (SqlCommandType.SELECT != mappedStatement.getSqlCommandType()
                    || StatementType.CALLABLE == mappedStatement.getStatementType()) {
                return invocation.proceed();
            }

            if (!dialect.skip(mappedStatement)) {
                BoundSql boundSql = (BoundSql) metaObject.getValue(PluginUtil.DELEGATE_BOUNDSQL);
                String originalSql = boundSql.getSql();
                // parse sql
                Select selectStatement = (Select) CCJSqlParserUtil.parse(originalSql);
                Page page = CursorPageHelper.getLocalPage();
                String originSqlConcatOrderBy = concatOrderBy(originalSql, selectStatement, page);

                // 查询总数
                String countSql = dialect.getCountSql(originSqlConcatOrderBy);
                if (log.isDebugEnabled()) {
                    log.debug("countSql: " + countSql);
                }
                Long count = countQuery(countSql, mappedStatement, boundSql, connection);
                if (!dialect.afterCount(count) || dialect.skipPage(mappedStatement)) {
                    metaObject.setValue(PluginUtil.DELEGATE_BOUNDSQL_SQL, emptyResult(originalSql, selectStatement));
                    return invocation.proceed();
                }
                // 分页查询的sql
                String pageSql = dialect.getPageSql(originSqlConcatOrderBy);
                if (log.isDebugEnabled()) {
                    log.debug("pageSql: " + pageSql);
                }
                metaObject.setValue(PluginUtil.DELEGATE_BOUNDSQL_SQL, pageSql);
                return invocation.proceed();
            } else {
                return invocation.proceed();
            }
        } finally {
            if (Objects.nonNull(dialect)) {
                dialect.afterCompletion();
            }
        }
    }

    /**
     * 通过Spring Bean方式配置拦截器的时候，如果没有配置属性则不会自动执行setProperties方法，因而dialect会出现null
     */
    private void checkDialect() {
        if (Objects.isNull(dialect)) {
            synchronized (defaultDialectClass) {
                if (Objects.isNull(dialect)) {
                    setProperties(new Properties());
                }
            }
        }
    }

    private static String concatOrderBy(String originalSql, Select selectStatement, Page page) {
        SelectBody selectBody = selectStatement.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            SearchDirection searchDirection = SearchDirection.getDirection(page.getDirection());
            boolean isAsc = Objects.nonNull(searchDirection) && Objects.equals(searchDirection.getSqlOrder(), "ASC");

            PlainSelect plainSelect = (PlainSelect) selectBody;
            List<OrderByElement> orderByElements = new ArrayList<>();
            OrderByElement orderByCursor = new OrderByElement();
            orderByCursor.setExpression(new Column(page.getCursorColumn()));
            orderByCursor.setAsc(isAsc);
            orderByElements.add(orderByCursor);
            OrderByElement orderById = new OrderByElement();
            orderById.setExpression(new Column(page.getIdColumn()));
            orderById.setAsc(isAsc);
            orderByElements.add(orderById);
            plainSelect.setOrderByElements(orderByElements);
            return plainSelect.toString();
        } else {
            return originalSql;
        }
    }

    private static String emptyResult(String originalSql, Select selectStatement) {
        SelectBody selectBody = selectStatement.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            Expression where = plainSelect.getWhere();
            NotEqualsTo empty = new NotEqualsTo();
            empty.setLeftExpression(new LongValue(1L));
            empty.setRightExpression(new LongValue(1L));
            AndExpression and = new AndExpression(where, empty);
            plainSelect.setWhere(and);
            return plainSelect.toString();
        } else {
            return originalSql;
        }
    }

    private static Long countQuery(String sql, MappedStatement mappedStatement, BoundSql boundSql, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
            parameterHandler.setParameters(statement);
            long count = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getLong(1);
                }
            }
            return count;
        } catch (Exception e) {
            throw new CursorPageException(e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        dialect = new CursorPageHelper();
        dialect.setProperties(properties);
    }
}
