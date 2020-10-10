package com.j4it.mybatisext.cursorpage;

/**
 * @author meanfu
 * @date 2019/09/05
 */
@FunctionalInterface
public interface ISelect {
    Object doSelect();
}