package cn.yitulin.ci.infrastructure.service;

import cn.hutool.json.JSONUtil;
import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.common.exception.ErrorEnum;
import cn.yitulin.ci.infrastructure.model.PluginConfig;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.ServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/20 14:15
 * modified : 💧💨🔥
 */
@Slf4j
public class PluginConfigService {

    public static PluginConfigService getInstance() {
        return ServiceManager.getService(PluginConfigService.class);
    }

    public static final String GLOBAL_CONFIG_KEY = "CI_GLOBAL_CONFIG";

    public PluginConfig read() {
        String value = PropertiesComponent.getInstance().getValue(GLOBAL_CONFIG_KEY);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        PluginConfig pluginConfig = JSONUtil.toBean(value, PluginConfig.class);
        return pluginConfig;
    }

    public void save(PluginConfig pluginConfig) {
        if (Objects.isNull(pluginConfig)) {
            return;
        }
        PropertiesComponent.getInstance().setValue(GLOBAL_CONFIG_KEY, JSONUtil.toJsonStr(pluginConfig));
        String configFileDirectory = pluginConfig.getConfigFileDirectory();
        String configFilePath = configFileDirectory + "/" + Constants.CONFIG_FILE_NAME;
        File file = new File(configFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileUtils.write(file, Constants.CONFIG_FILE_TEMPLATE, Charset.forName("UTF-8"));
            } catch (IOException e) {
                ErrorEnum.CONFIG_FILE_INIT_ERROR.showErrorDialog();
                return;
            }
        }
    }

}
