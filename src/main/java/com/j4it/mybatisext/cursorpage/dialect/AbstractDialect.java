package com.j4it.mybatisext.cursorpage.dialect;

import com.j4it.mybatisext.cursorpage.CursorPageHelper;
import com.j4it.mybatisext.cursorpage.Page;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Properties;

/**
 * @author meanfu
 * @date 2019/09/05
 */
public abstract class AbstractDialect implements IDialect {
    protected final Log log = LogFactory.getLog(getClass());

    public Page getLocalPage() {
        return CursorPageHelper.getLocalPage();
    }

    @Override
    public void setProperties(Properties properties) {
    }

    @Override
    public boolean skip(MappedStatement mappedStatement) {
        return true;
    }

    @Override
    public boolean afterCount(long count) {
        Page page = getLocalPage();
        page.setTotalCount(count);
        return count > 0;
    }

    @Override
    public boolean skipPage(MappedStatement mappedStatement) {
        Page page = getLocalPage();
        // pageSize <= 0, 不执行分页查询
        return page.getPageSize() <= 0;
    }

    @Override
    public void afterCompletion() {
    }
}
