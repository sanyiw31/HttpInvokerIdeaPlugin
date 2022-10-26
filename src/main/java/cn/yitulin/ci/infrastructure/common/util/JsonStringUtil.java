package cn.yitulin.ci.infrastructure.common.util;

import cn.hutool.json.JSONUtil;

import java.util.Objects;

public class JsonStringUtil {

    private JsonStringUtil() {
    }

    public static String toStr(Object obj) {
        if (Objects.isNull(obj)) {
            return "";
        }
        if (JSONUtil.isTypeJSON(obj.toString())) {
            return JSONUtil.toJsonStr(obj);
        }
        return String.valueOf(obj);
    }

    public static String toPrettyStr(Object obj) {
        if (Objects.isNull(obj)) {
            return "";
        }
        if (JSONUtil.isTypeJSON(obj.toString())) {
            return JSONUtil.toJsonPrettyStr(obj);
        }
        return String.valueOf(obj);
    }

    public static String toFormatJsonStr(Object obj) {
        if (Objects.isNull(obj)) {
            return "";
        }
        return JSONUtil.formatJsonStr(toStr(obj));
    }

}
