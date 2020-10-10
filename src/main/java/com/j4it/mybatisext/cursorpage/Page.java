package com.j4it.mybatisext.cursorpage;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象
 *
 * @author meanfu
 * @date 2019/09/03
 */
public class Page implements Closeable {
    /**
     * 拉取数据的游标值
     */
    private String cursorValue;
    /**
     * 作为游标的列名
     */
    private String cursorColumn;
    /**
     * 前一页最后一条记录的id
     */
    private String lastIdOfPrevPage = "";
    /**
     * 主键的列名
     */
    private String idColumn;
    /**
     * 每页显示记录数。如果pageSize<=0，则不执行分页查询，只返回totalCount
     */
    private int pageSize;
    /**
     * 拉取数据的方向，可选值为backward和forward
     */
    private String direction;
    /**
     * 总记录数
     */
    private long totalCount;

    public Page() {
    }

    public Page(String cursorValue, String cursorColumn, String lastIdOfPrevPage, String idColumn, int pageSize, String direction) {
        this.cursorValue = cursorValue;
        this.cursorColumn = cursorColumn;
        this.lastIdOfPrevPage = lastIdOfPrevPage;
        this.idColumn = idColumn;
        this.pageSize = pageSize;
        this.direction = direction;
    }

    public String getCursorValue() {
        return cursorValue;
    }

    public void setCursorValue(String cursorValue) {
        this.cursorValue = cursorValue;
    }

    public String getCursorColumn() {
        return cursorColumn;
    }

    public void setCursorColumn(String cursorColumn) {
        this.cursorColumn = cursorColumn;
    }

    public String getLastIdOfPrevPage() {
        return lastIdOfPrevPage;
    }

    public void setLastIdOfPrevPage(String lastIdOfPrevPage) {
        this.lastIdOfPrevPage = lastIdOfPrevPage;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    private <E> PageResult<E> toPageResult(Object result, Class<E> clazz) {
        PageResult<E> pageResult = new PageResult<>();
        pageResult.setTotalCount(this.totalCount);
        pageResult.setPageSize(this.pageSize);
        if (result instanceof List) {
            List objectList = (List) result;
            List<E> resultList = new ArrayList<>();
            for (Object o : objectList) {
                if (clazz.isInstance(o)) {
                    resultList.add(clazz.cast(o));
                }
            }
            pageResult.setList(resultList);
            pageResult.setCurrentSize(resultList.size());
        }
        return pageResult;
    }

    public <E> PageResult<E> doSelect(ISelect select, Class<E> clazz) {
        Object result = select.doSelect();
        return this.toPageResult(result, clazz);
    }

    @Override
    public String toString() {
        return "Page{" + "totalCount=" + totalCount + "} " + super.toString();
    }

    @Override
    public void close() throws IOException {
        CursorPageHelper.removeLocalPage();
    }
}
