package cn.yitulin.ci.infrastructure.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/3/10 01:29
 * modified : 💧💨🔥
 */
public class SystemPropertyUtil {

    public static String getUserRootPath() {
        return System.getProperty("user.home");
    }

    public static String getOperatingSystem() {
        return System.getProperty("os.name");
    }

    public static String getIP() throws UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        return ip.getHostAddress();
    }

    public static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

}
