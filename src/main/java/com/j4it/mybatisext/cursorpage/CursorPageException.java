package com.j4it.mybatisext.cursorpage;

/**
 * @author meanfu
 * @date 2019/09/04
 */
public class CursorPageException extends RuntimeException {

    public CursorPageException() {
        super();
    }

    public CursorPageException(String message) {
        super(message);
    }

    public CursorPageException(String message, Throwable cause) {
        super(message, cause);
    }

    public CursorPageException(Throwable cause) {
        super(cause);
    }
}
