package com.j4it.mybatisext.cursorpage.util;

import com.google.common.base.Splitter;
import com.j4it.mybatisext.cursorpage.PageResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author meanfu
 * @date 2019/09/05
 */
public class PluginUtil {
    public static final String DELEGATE_BOUNDSQL = "delegate.boundSql";
    public static final String DELEGATE_BOUNDSQL_SQL = DELEGATE_BOUNDSQL + ".sql";
    private static final String CURSOR_TABLE_PREFIX = "t";

    public static <T> T getRealTarget(Object target, Class<T> clazz) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return getRealTarget(metaObject.getValue("h.target"), clazz);
        }
        return clazz.isInstance(target) ? clazz.cast(target) : null;
    }

    /**
     * BASE64解码游标
     *
     * @param cursor
     * @return
     */
    public static CursorDTO decodeCursor(String cursor) {
        byte[] decodedBytes = Base64.decodeBase64(cursor.getBytes());
        String decodedCursor = new String(decodedBytes);
        Map<String, String> splitCursor = Splitter.on(",").withKeyValueSeparator("=").split(decodedCursor);
        return new CursorDTO(splitCursor);
    }

    /**
     * BASE64编码游标
     *
     * @param cursorDTO
     * @return
     */
    public static String encodeCursor(CursorDTO cursorDTO) {
        String concatCursor = cursorDTO.concatEntry();
        if (Objects.equals(PageResult.NO_DATA_FLAG, concatCursor)) {
            return concatCursor;
        }
        return new String(Base64.encodeBase64URLSafe(concatCursor.getBytes()));
    }

    /**
     * 游标对象
     */
    public static class CursorDTO extends HashMap<String, String> {
        public CursorDTO() {
            super();
        }

        public CursorDTO(Map<? extends String, ? extends String> m) {
            super(m);
        }

        public static CursorDTO getInstance() {
            return new CursorDTO();
        }

        public String getSpecificCursor(int sequence) {
            if (this.isEmpty()) {
                return "";
            } else {
                return this.get(CURSOR_TABLE_PREFIX + sequence);
            }
        }

        public CursorDTO setSpecificCursor(int sequence, String cursorValue) {
            this.put(CURSOR_TABLE_PREFIX + sequence, cursorValue);
            return this;
        }

        public String concatEntry() {
            Iterator<Entry<String, String>> i = entrySet().iterator();
            if (!i.hasNext()) {
                return PageResult.NO_DATA_FLAG;
            }
            int eofCount = 0;
            StringBuilder sb = new StringBuilder();
            for (; ; ) {
                Entry<String, String> e = i.next();
                String key = e.getKey();
                String value = e.getValue();
                if (Objects.equals(value, PageResult.NO_DATA_FLAG)) {
                    eofCount++;
                }
                sb.append(key);
                sb.append('=');
                sb.append(value);
                if (!i.hasNext()) {
                    if (eofCount == this.size()) {
                        return PageResult.NO_DATA_FLAG;
                    }
                    return sb.toString();
                }
                sb.append(',');
            }
        }
    }
}
