package com.j4it.mybatisext.cursorpage.page;

import com.j4it.mybatisext.cursorpage.Page;
import com.j4it.mybatisext.cursorpage.util.DateUtil;
import com.j4it.mybatisext.cursorpage.util.StringPool;
import com.j4it.mybatisext.cursorpage.constant.CursorFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

/**
 * 分页方法
 *
 * @author meanfu
 * @date 2019/09/04
 */
public abstract class PageMethod {
    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();
    protected static final String CURSOR_DELIMITER = StringPool.COLON;

    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void removeLocalPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * 游标值为时间戳格式的分页方法
     *
     * @param cursor
     * @param cursorColumn
     * @param idColumn
     * @param pageSize
     * @param direction
     * @return
     */
    public static Page startPage(String cursor, String cursorColumn, String idColumn, int pageSize, String direction) {
        return startPage(cursor, CURSOR_DELIMITER, cursorColumn, idColumn, CursorFormat.TIMESTAMP, pageSize, direction);
    }

    /**
     * 可设置游标值格式的分页方法
     *
     * @param cursor
     * @param cursorColumn
     * @param idColumn
     * @param cursorFormat
     * @param pageSize
     * @param direction
     * @return
     */
    public static Page startPage(String cursor, String cursorColumn, String idColumn, CursorFormat cursorFormat, int pageSize, String direction) {
        return startPage(cursor, CURSOR_DELIMITER, cursorColumn, idColumn, cursorFormat, pageSize, direction);
    }

    public static Page startPage(String cursor, String cursorDelimiter, String cursorColumn, String idColumn, CursorFormat cursorFormat, int pageSize, String direction) {
        Page page = new Page();
        convertCursor(cursor, cursorDelimiter, page);
        if (Objects.equals(CursorFormat.DATETIME, cursorFormat)) {
            page.setCursorValue(DateUtil.dateToLocalDateTime(new Date(Long.valueOf(page.getCursorValue()))).format(DateUtil.STANDARD_TIME_FORMATTER));
        }
        page.setCursorColumn(cursorColumn);
        page.setIdColumn(idColumn);
        page.setPageSize(pageSize);
        page.setDirection(direction);
        setLocalPage(page);
        return page;
    }

    public static void convertCursor(String cursor, String cursorDelimiter, Page page) {
        if (StringUtils.isBlank(cursor)) {
            cursor = "";
        }
        String[] splitStr = cursor.split(cursorDelimiter);
        if (splitStr.length == 2) {
            page.setCursorValue(splitStr[0]);
            page.setLastIdOfPrevPage(splitStr[1]);
        } else if (splitStr.length == 1) {
            page.setCursorValue(splitStr[0]);
        } else {
            throw new RuntimeException("游标错误");
        }
    }
}
