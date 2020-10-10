package com.j4it.mybatisext.cursorpage.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 拉取数据的方向
 *
 * @author meanfu
 * @date 2019/09/06
 */
public enum SearchDirection {

    FORWARD("forward", "ASC"),
    BACKWARD("backward", "DESC");

    private String flag;
    private String sqlOrder;

    public String getFlag() {
        return flag;
    }

    public String getSqlOrder() {
        return sqlOrder;
    }

    SearchDirection(String flag, String sqlOrder) {
        this.flag = flag;
        this.sqlOrder = sqlOrder;
    }

    public static SearchDirection getDirection(String flag) {
        return StringUtils.isNotBlank(flag)
                ? Arrays.stream(values()).filter(searchDirection -> Objects.equals(searchDirection.getFlag(), flag)).findFirst().orElse(null)
                : null;
    }
}
