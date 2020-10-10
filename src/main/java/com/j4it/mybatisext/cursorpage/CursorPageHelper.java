package com.j4it.mybatisext.cursorpage;

import com.j4it.mybatisext.cursorpage.page.PageAutoDialect;
import com.j4it.mybatisext.cursorpage.page.PageMethod;
import com.j4it.mybatisext.cursorpage.dialect.AbstractDialect;
import com.j4it.mybatisext.cursorpage.dialect.IDialect;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Objects;
import java.util.Properties;

/**
 * @author meanfu
 * @date 2019/09/05
 */
public class CursorPageHelper extends PageMethod implements IDialect {
    private PageAutoDialect pageAutoDialect;

    @Override
    public void setProperties(Properties properties) {
        // 可以设置全局的参数和针对特定数据源的参数
        pageAutoDialect = new PageAutoDialect();
        pageAutoDialect.setProperties(properties);
    }

    @Override
    public boolean skip(MappedStatement mappedStatement) {
        Page page = getLocalPage();
        if (Objects.isNull(page)) {
            return true;
        } else {
            pageAutoDialect.initDelegateDialect(mappedStatement);
            return false;
        }
    }

    @Override
    public String getCountSql(String sql) {
        return pageAutoDialect.getDelegateDialect().getCountSql(sql);
    }

    @Override
    public boolean afterCount(long count) {
        return pageAutoDialect.getDelegateDialect().afterCount(count);
    }

    @Override
    public boolean skipPage(MappedStatement mappedStatement) {
        return pageAutoDialect.getDelegateDialect().skipPage(mappedStatement);
    }

    @Override
    public String getPageSql(String sql) {
        return pageAutoDialect.getDelegateDialect().getPageSql(sql);
    }

    @Override
    public void afterCompletion() {
        AbstractDialect delegateDialect = pageAutoDialect.getDelegateDialect();
        if (Objects.nonNull(delegateDialect)) {
            delegateDialect.afterCompletion();
        }
        removeLocalPage();
    }
}
