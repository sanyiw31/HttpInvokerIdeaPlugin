package cn.yitulin.ci.infrastructure.common.util;


public class OperateSystemUtil {

    public static boolean isWindows() {
        return (SystemPropertyUtil.getOperatingSystem().toLowerCase().indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (SystemPropertyUtil.getOperatingSystem().toLowerCase().indexOf("mac") >= 0);
    }

}
