package cn.yitulin.ci.infrastructure.service;

import cn.hutool.json.JSONUtil;
import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.model.InvokeLog;
import cn.yitulin.ci.infrastructure.model.PluginConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.components.ServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/21 20:01
 * modified : 💧💨🔥
 */
@Slf4j
public class InvokeLogService {

    public static InvokeLogService getInstance() {
        return ServiceManager.getService(InvokeLogService.class);
    }

    private Cache<String, InvokeLog> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();

    public InvokeLog readLog(boolean useCache, String key) {
        if (useCache) {
            InvokeLog cacheIfPresent = readFromCache(key);
            if (Objects.nonNull(cacheIfPresent)) {
                return cacheIfPresent;
            }
        }
        readAllLocalLogUpdateCache();
        return readFromCache(key);
    }

    private InvokeLog readFromCache(String key) {
        if (Objects.isNull(cache.getIfPresent(key))) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(cache.getIfPresent(key)), InvokeLog.class);
    }

    public void insertLog(InvokeLog InvokeLog) throws IOException {
        log.info("start insertLog,InvokeLog:[{}]", JSONUtil.toJsonStr(InvokeLog));
        readAllLocalLogUpdateCache();
        if (cache.size() >= Constants.INVOKE_LOG_MAX_SIZE) {
            int anInt = RandomUtils.nextInt(Constants.INVOKE_LOG_MAX_SIZE);
            List<InvokeLog> collect = cache.asMap().values().stream().map(item -> JSONUtil.toBean(JSONUtil.toJsonStr(item), InvokeLog.class)).collect(Collectors.toList());
            InvokeLog remove = collect.remove(anInt);
            cache.invalidate(remove.getKey());
        }
        cache.put(InvokeLog.getKey(), InvokeLog);
        PluginConfig pluginConfig = PluginConfigService.getInstance().read();
        if (Objects.isNull(pluginConfig) || StringUtils.isEmpty(pluginConfig.getConfigFileDirectory())) {
            return;
        }
        File file = new File(pluginConfig.getConfigFileDirectory() + "/" + Constants.LOG_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtils.write(file, JSONUtil.toJsonStr(cache.asMap()), Charset.forName("UTF-8"));
        log.info("finish insertLog");
    }

    private void readAllLocalLogUpdateCache() {
        PluginConfig pluginConfig = PluginConfigService.getInstance().read();
        if (Objects.isNull(pluginConfig) || StringUtils.isEmpty(pluginConfig.getConfigFileDirectory())) {
            return;
        }
        try {
            File file = new File(pluginConfig.getConfigFileDirectory() + "/" + Constants.LOG_FILE_NAME);
            if (!file.exists()) {
                return;
            }
            String fileToString = FileUtils.readFileToString(file, Charset.defaultCharset());
            Map<String, InvokeLog> map = JSONUtil.toBean(fileToString, Map.class);
            cache.putAll(map);
        } catch (IOException e) {
            log.error("读取json文件失败,filePath:[{}],错误信息:[{}]", pluginConfig.getConfigFileDirectory(), e.getMessage(), e);
        }
    }

}
