package com.j4it.mybatisext.cursorpage.page;

import com.j4it.mybatisext.cursorpage.dialect.MySqlDialect;
import com.j4it.mybatisext.cursorpage.CursorPageException;
import com.j4it.mybatisext.cursorpage.dialect.AbstractDialect;
import com.j4it.mybatisext.cursorpage.dialect.IDialect;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author meanfu
 * @date 2019/09/04
 */
public class PageAutoDialect {
    private static Map<String, Class<? extends IDialect>> dialectMap = new HashMap<>();

    public static void registerDialect(String alias, Class<? extends IDialect> dialectClass) {
        dialectMap.put(alias, dialectClass);
    }

    static {
        registerDialect("mysql", MySqlDialect.class);
    }

    private Properties properties;
    private AbstractDialect delegateDialect;
    private Map<String, AbstractDialect> urlDialectMap = new ConcurrentHashMap<>();
    private ReentrantLock lock = new ReentrantLock();

    public void initDelegateDialect(MappedStatement mappedStatement) {
        if (Objects.isNull(delegateDialect)) {
            delegateDialect = getDialect(mappedStatement);
        }
    }

    public AbstractDialect getDelegateDialect(){
        return delegateDialect;
    }

    private AbstractDialect getDialect(MappedStatement mappedStatement) {
        DataSource dataSource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
        String jdbcUrl = getJdbcUrl(dataSource);
        if (urlDialectMap.containsKey(jdbcUrl)) {
            return urlDialectMap.get(jdbcUrl);
        }
        try {
            lock.lock();
            if (urlDialectMap.containsKey(jdbcUrl)) {
                return urlDialectMap.get(jdbcUrl);
            }
            if (Objects.isNull(jdbcUrl) || jdbcUrl.length() == 0) {
                throw new CursorPageException("无法获取jdbcUrl");
            }
            String dialectAlias = getDialectAlias(jdbcUrl);
            if (Objects.isNull(dialectAlias)) {
                throw new CursorPageException("无法获取数据库类型");
            }
            AbstractDialect dialect = initDialect(dialectAlias);
            urlDialectMap.put(jdbcUrl, dialect);
            return dialect;
        } finally {
            lock.unlock();
        }
    }

    private String getJdbcUrl(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new CursorPageException(e);
        }
    }

    private static String getDialectAlias(String jdbcUrl) {
        int startIndex;
        String alias;
        if ((startIndex = jdbcUrl.indexOf("jdbc:")) != -1) {
            startIndex = startIndex + 5;
            int endIndex = jdbcUrl.indexOf(":", startIndex);
            alias = jdbcUrl.substring(startIndex, endIndex);
            return Objects.nonNull(dialectMap.get(alias)) ? alias : null;
        }
        return null;
    }

    private AbstractDialect initDialect(String dialectClassStr) {
        AbstractDialect dialect;
        try {
            Class dialectClass = findDialectClass(dialectClassStr);
            if (AbstractDialect.class.isAssignableFrom(dialectClass)) {
                dialect = (AbstractDialect) dialectClass.newInstance();
            } else {
                throw new CursorPageException("方言必须是实现" + AbstractDialect.class.getCanonicalName() + "接口的实现类");
            }
        } catch (Exception e) {
            throw new CursorPageException("初始化dialect[" + dialectClassStr + "]时出错:" + e.getMessage());
        }
        dialect.setProperties(properties);
        return dialect;
    }

    private Class findDialectClass(String className) throws Exception {
        if (dialectMap.containsKey(className)) {
            return dialectMap.get(className);
        } else {
            return Class.forName(className);
        }
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
