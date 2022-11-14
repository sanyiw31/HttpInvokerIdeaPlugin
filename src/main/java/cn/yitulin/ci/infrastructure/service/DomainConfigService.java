package cn.yitulin.ci.infrastructure.service;

import cn.hutool.json.JSONUtil;
import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.common.exception.ErrorEnum;
import cn.yitulin.ci.infrastructure.model.DomainConfig;
import cn.yitulin.ci.infrastructure.model.PluginConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.components.ServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class DomainConfigService {

    public static DomainConfigService getInstance() {
        return ServiceManager.getService(DomainConfigService.class);
    }

    private Cache<String, DomainConfig> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    public DomainConfig readByDomain(boolean useCache, String domain) {
        log.info("start readByDomain,useCache:[{}],domain:[{}]", useCache, domain);
        if (useCache) {
            DomainConfig domainConfig = readFromCache(domain, true);
            log.info("finish readByDomain, cache:[{}]", domainConfig);
            return domainConfig;
        }
        readFromDiskAndRefreshCache();
        DomainConfig ifPresent = cache.getIfPresent(domain);
        log.info("finish readByDomain, response:[{}]", ifPresent);
        return ifPresent;
    }

    public List<String> readAllDomainName() {
        log.info("start readAllDomainName");
        readFromDiskAndRefreshCache();
        List<String> domainNames = cache.asMap().keySet().stream().collect(Collectors.toList());
        log.info("finish readAllDomainName, response:[{}]", domainNames);
        return domainNames;
    }

    private DomainConfig readFromCache(String domain, boolean refreshCache) {
        DomainConfig ifPresent = cache.getIfPresent(domain);
        if (Objects.nonNull(ifPresent)) {
            return ifPresent;
        }
        if (!refreshCache) {
            return null;
        }
        readFromDiskAndRefreshCache();
        return cache.getIfPresent(domain);
    }

    private void readFromDiskAndRefreshCache() {
        PluginConfig pluginConfig = PluginConfigService.getInstance().read();
        if (Objects.isNull(pluginConfig) || StringUtils.isEmpty(pluginConfig.getConfigFileDirectory())) {
            return;
        }
        try {
            File file = new File(pluginConfig.getConfigFileDirectory() + "/" + Constants.CONFIG_FILE_NAME);
            if (!file.exists()) {
                ErrorEnum.CONFIG_FILE_NOT_EXISTS.showErrorDialog();
                return;
            }
            String fileToString = FileUtils.readFileToString(file, Charset.defaultCharset());
            List<DomainConfig> domainConfigs = JSONUtil.toList(fileToString, DomainConfig.class);
            log.info("域名配置：[{}]", domainConfigs);
            if (CollectionUtils.isNotEmpty(domainConfigs)) {
                domainConfigs.stream().forEach(item -> cache.put(StringUtils.isNotEmpty(item.getName()) ? item.getName() : item.getDomain(), item));
            }
        } catch (IOException e) {
            log.error("读取json文件失败,filePath:[{}],错误信息:[{}]", pluginConfig.getConfigFileDirectory(), e.getMessage(), e);
            ErrorEnum.CONFIG_FILE_DATA_NOT_LEGAL_JSON.showErrorDialog(e.getMessage());
        }
    }

}
