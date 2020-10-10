package com.j4it.mybatisext.cursorpage.dialect;

import com.j4it.mybatisext.cursorpage.Page;
import com.j4it.mybatisext.cursorpage.constant.SearchDirection;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author meanfu
 * @date 2019/09/05
 */
public class MySqlDialect extends AbstractDialect {
    @Override
    public String getCountSql(String sql) {
        Page page = getLocalPage();
        String chev = geChevron(page.getDirection());
        String countSql = String.format("SELECT COUNT(1) FROM ( %s ) count_t WHERE %s %s '%s'", sql, page.getCursorColumn(), chev, page.getCursorValue());
        if (StringUtils.isNotBlank(page.getIdColumn()) && StringUtils.isNotBlank(page.getLastIdOfPrevPage())) {
            countSql = String.format(countSql + " OR ( %s %s '%s' AND %s %s '%s' )", page.getCursorColumn(), "=", page.getCursorValue(), page.getIdColumn(), chev, page.getLastIdOfPrevPage());
        }
        return countSql;
    }

    @Override
    public String getPageSql(String sql) {
        Page page = getLocalPage();
        String chev = geChevron(page.getDirection());
        String pageSql = String.format("SELECT * FROM ( %s ) page_t WHERE %s %s '%s'", sql, page.getCursorColumn(), chev, page.getCursorValue());
        if (StringUtils.isNotBlank(page.getIdColumn()) && StringUtils.isNotBlank(page.getLastIdOfPrevPage())) {
            pageSql = String.format(pageSql + " OR ( %s %s '%s' AND %s %s '%s' )", page.getCursorColumn(), "=", page.getCursorValue(), page.getIdColumn(), chev, page.getLastIdOfPrevPage());
        }
        pageSql = pageSql + " LIMIT " + page.getPageSize();
        return pageSql;
    }

    private static String geChevron(String direction) {
        return Objects.equals(SearchDirection.FORWARD.getFlag(), direction) ? ">" : "<";
    }
}
