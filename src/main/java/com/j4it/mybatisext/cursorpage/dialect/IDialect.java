package com.j4it.mybatisext.cursorpage.dialect;

import org.apache.ibatis.mapping.MappedStatement;

import java.util.Properties;

/**
 * 数据库方言
 *
 * @author meanfu
 * @date 2019/09/05
 */
public interface IDialect {
    /**
     * 设置Interceptor参数
     *
     * @param properties
     */
    void setProperties(Properties properties);

    /**
     * 是否跳过count和分页查询
     *
     * @param mappedStatement
     * @return
     */
    boolean skip(MappedStatement mappedStatement);

    /**
     * 生成count查询的sql
     *
     * @return
     */
    String getCountSql(String sql);

    /**
     * 执行完count查询后的处理
     *
     * @param count
     * @return true 继续查询, false 直接返回
     */
    boolean afterCount(long count);

    /**
     * 是否跳过分页查询
     *
     * @param mappedStatement
     * @return
     */
    boolean skipPage(MappedStatement mappedStatement);

    /**
     * 生成分页查询的sql
     *
     * @return
     */
    String getPageSql(String sql);

    /**
     * 完成所有任务后执行
     */
    void afterCompletion();
}
