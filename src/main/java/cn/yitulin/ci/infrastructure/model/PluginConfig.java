package cn.yitulin.ci.infrastructure.model;

import cn.yitulin.ci.infrastructure.common.Constants;
import lombok.Data;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/17 16:09
 * modified : 💧💨🔥
 */
@Data
public class PluginConfig {

    /**
     * 配置文件目录
     */
    private String configFileDirectory;

    /**
     * 浏览器类型
     */
    private String browserType;

    /**
     * 饼干数据库路径
     */
    private String cookieDbPath;

    public String concatDefaultConfigFilePath() {
        return this.configFileDirectory + "/" + Constants.CONFIG_FILE_NAME;
    }

}
