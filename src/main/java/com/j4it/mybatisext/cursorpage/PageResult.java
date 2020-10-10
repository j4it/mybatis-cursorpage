package com.j4it.mybatisext.cursorpage;

import com.j4it.mybatisext.cursorpage.util.DateUtil;
import com.j4it.mybatisext.cursorpage.util.StringPool;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 分页结果
 *
 * @author meanfu
 * @date 2019/09/03
 */
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = -4596212748004621950L;
    public static final String NO_DATA_FLAG = "EOF";
    private static final transient Log log = LogFactory.getLog(PageResult.class);

    /**
     * 总记录数
     */
    private long totalCount;
    /**
     * 结果集
     */
    private List<T> list;
    /**
     * 下一批数据的起始游标，"EOF"表示没有数据
     */
    private String nextCursor;
    /**
     * 每页显示记录数
     */
    private int pageSize;
    /**
     * 当前页的记录数
     */
    private int currentSize;

    public PageResult() {
    }

    /**
     * 设置下一批数据的游标
     *
     * @param cursorFieldName
     * @param idFieldName
     * @return
     */
    public PageResult<T> nextCursor(String cursorFieldName, String idFieldName) {
        if (Objects.nonNull(this.list) && !this.list.isEmpty()) {
            if (totalCount <= this.list.size()) {
                this.nextCursor = NO_DATA_FLAG;
            } else {
                T last = this.list.get(this.list.size() - 1);
                if (Objects.nonNull(last)) {
                    String nextCursor = "";
                    try {
                        Class<?> clazz = last.getClass();
                        Field cursorField = findTargetField(clazz, cursorFieldName);
                        cursorField.setAccessible(true);
                        Object cursor = cursorField.get(last);
                        if (cursor instanceof Date) {
                            LocalDateTime localDateTime = DateUtil.dateToLocalDateTime((Date) cursor);
                            nextCursor = String.valueOf(DateUtil.getEpochMilli(localDateTime));
                        } else {
                            nextCursor = String.valueOf(cursor);
                        }
                        Field idField = findTargetField(clazz, idFieldName);
                        idField.setAccessible(true);
                        Object id = idField.get(last);
                        id = String.valueOf(id);
                        nextCursor = nextCursor + StringPool.COLON + id;
                    } catch (Exception e) {
                        log.error("occur error when assemble next cursor", e);
                    }
                    this.nextCursor = nextCursor;
                }
            }
        } else {
            this.nextCursor = NO_DATA_FLAG;
        }
        return this;
    }

    private static Field findTargetField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field targetField = null;
        while (Objects.isNull(targetField)) {
            try {
                targetField = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (Objects.isNull(clazz)) {
                    throw e;
                }
            }
        }
        return targetField;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
}
