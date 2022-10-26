package cn.yitulin.ci.infrastructure.common.enums;

import cn.yitulin.ci.infrastructure.common.util.MacKeyringFetchUtil;
import cn.yitulin.ci.infrastructure.common.util.OperateSystemUtil;
import cn.yitulin.ci.infrastructure.common.util.SystemPropertyUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/3/10 01:08
 * modified : 💧💨🔥
 */
@Getter
@AllArgsConstructor
public enum BrowserEnum {

    /**
     * 谷歌浏览器
     */
    GOOGLE_CHROME("Google Chrome") {
        @Override
        public String fetchCookiesDbPath() {
            if (OperateSystemUtil.isMac()) {
                return SystemPropertyUtil.getUserRootPath() + "/Library/Application Support/Google/Chrome/Default/Cookies";
            }
            return null;
        }

        @Override
        public String fetchCookiesKeyring() {
            if (OperateSystemUtil.isMac()) {
                try {
                    return MacKeyringFetchUtil.getMacKeyringPassword("Chrome Safe Storage");
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
    },

    /**
     * 微软浏览器
     */
    MICROSOFT_EDGE("Microsoft Edge") {
        @Override
        public String fetchCookiesDbPath() {
            if (OperateSystemUtil.isMac()) {
                return SystemPropertyUtil.getUserRootPath() + "/Library/Application Support/Microsoft Edge/Default/Cookies";
            }
            return null;
        }

        @Override
        public String fetchCookiesKeyring() {
            if (OperateSystemUtil.isMac()) {
                try {
                    return MacKeyringFetchUtil.getMacKeyringPassword("Microsoft Edge Safe Storage");
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
    },
    ;

    private String name;

    abstract public String fetchCookiesDbPath();

    abstract public String fetchCookiesKeyring();

    public static BrowserEnum getBrowserEnumByName(String name) {
        for (BrowserEnum browserEnum : values()) {
            if (browserEnum.getName().equals(name)) {
                return browserEnum;
            }
        }
        return null;
    }
}
