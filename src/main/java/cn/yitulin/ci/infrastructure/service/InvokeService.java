package cn.yitulin.ci.infrastructure.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.yitulin.ci.infrastructure.common.enums.BrowserEnum;
import cn.yitulin.ci.infrastructure.common.enums.HttpMethodEnum;
import cn.yitulin.ci.infrastructure.common.event.EventBusCenter;
import cn.yitulin.ci.infrastructure.common.event.InvokeResponseMessage;
import cn.yitulin.ci.infrastructure.common.util.BrowserCookiesReadUtil;
import cn.yitulin.ci.infrastructure.common.util.JsonStringUtil;
import cn.yitulin.ci.infrastructure.model.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.ServiceManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class InvokeService {

    private ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "InvokeService_" + r.hashCode()));

    public static InvokeService getInstance() {
        return ServiceManager.getService(InvokeService.class);
    }

    public void invoke(Invoker invoker) {
        log.info("start invoke, invoker:[{}]", JSONUtil.toJsonStr(invoker));
        executor.execute(() -> {
            DomainConfig domainConfig = readDomainConfig(invoker.getInvokeBody().getDomainName());
            HttpResponse response = null;
            try {
                if (HttpMethodEnum.GET.name().equals(invoker.getInvokeBody().getHttpMethod())) {
                    response = sendRequestWithGet(domainConfig, invoker);
                } else {
                    response = sendRequestWithMethod(domainConfig, invoker);
                }
            } catch (HttpException exception) {
                log.warn("execute invoke, http timeout.\n", exception);
                EventBusCenter.post(InvokeResponseMessage.create(invoker.getEventId(), exception.getMessage()));
                return;
            }
            if (Objects.isNull(response)) {
                return;
            }
            String responseBody = response.body();
            InvokeLog invokeLog = new InvokeLog();
            invokeLog.setKey(invoker.getInvokeBody().getUrl() + "_" + invoker.getInvokeBody().getHttpMethod());
            invokeLog.setInvokeTime(LocalDateTime.now());
            invokeLog.setParams(invoker.getInvokeBody().getParams());
            invokeLog.setResponseData(responseBody);
            try {
                InvokeLogService.getInstance().insertLog(invokeLog);
            } catch (IOException e) {
                log.error("接口调用记录写入失败,失败原因:[{}]", e.getMessage(), e);
            }
            PluginConfig pluginConfig = PluginConfigService.getInstance().read();
            if (!invoker.getInvokeBody().getDomainName().equals(pluginConfig.getLastUseDomainName())){
                pluginConfig.setLastUseDomainName(invoker.getInvokeBody().getDomainName());
                PluginConfigService.getInstance().save(pluginConfig);
            }
            EventBusCenter.post(InvokeResponseMessage.create(invoker.getEventId(), responseBody));
        });
        log.info("finish invoke");
    }

    private HttpResponse sendRequestWithMethod(DomainConfig domainConfig, Invoker invoker) throws HttpException {
        log.info("start sendRequestWithMethod,domainConfig:[{}],invoker:[{}]", domainConfig, JSONUtil.toJsonStr(invoker));
        HttpRequest request = HttpRequest.of(invoker.getInvokeBody().getUrl()).method(matchEnum(invoker.getInvokeBody().getHttpMethod())).setConnectionTimeout(3 * 1000).setReadTimeout(10 * 1000);
        request.header("Content-Type", "application/json");
        request = requestFillHeaders(domainConfig.getHeaders(), request);
        Map<String, String> cookies = pickCookies(domainConfig);
        request = requestFillCookies(cookies, request);
        if (invoker.getInvokeBody().getParams().size() == 1) {
            List<String> keys = invoker.getInvokeBody().getParams().keySet().stream().collect(Collectors.toList());
            request.body(JsonStringUtil.toStr(invoker.getInvokeBody().getParams().get(keys.get(0))));
        } else {
            request.body(JsonStringUtil.toStr(invoker.getInvokeBody().getParams()));
        }
        if (invoker.isGenerateCurlText()) {
            EventBusCenter.post(InvokeResponseMessage.create(invoker.getEventId(), generateUrl(request)));
            return null;
        }
        log.info("execute sendRequestWithMethod, request.url:[{}], request.headers:[{}], request.body:[{}]", request.getUrl(), JSONUtil.toJsonStr(request.headers()), new String(request.bodyBytes()));
        HttpResponse response = request.execute(true);
        log.info("finish sendRequestWithMethod, response:[{}]", response);
        return response;
    }

    private HttpResponse sendRequestWithGet(DomainConfig domainConfig, Invoker invoker) throws HttpException {
        log.info(" ,domainConfig:[{}],invoker:[{}]", domainConfig, JSONUtil.toJsonStr(invoker));
        String url = new StringBuilder(invoker.getInvokeBody().getUrl()).append("?").toString();
        StringBuilder paramsBuilder = new StringBuilder();
        Map<String, Object> getJsonParam = dealGetJsonParam(invoker);
        for (Map.Entry<String, Object> entry : getJsonParam.entrySet()) {
            String expressionReplace = "{" + entry.getKey() + "}";
            if (url.contains(expressionReplace)) {
                url = url.replace(expressionReplace, JsonStringUtil.toStr(entry.getValue()));
                continue;
            }
            if (paramsBuilder.length() > 0) {
                paramsBuilder.append("&");
            }
            paramsBuilder.append(entry.getKey()).append("=").append(JsonStringUtil.toStr(entry.getValue()));
        }
        String encodeParams = URLEncodeUtil.encode(paramsBuilder.toString());
        log.info("execute sendRequestWithGet, url is:[{}]", url + encodeParams);
        HttpRequest request = HttpRequest.get(url + encodeParams).setConnectionTimeout(3 * 1000).setReadTimeout(10 * 1000);
        request = requestFillHeaders(domainConfig.getHeaders(), request);
        Map<String, String> cookies = pickCookies(domainConfig);
        request = requestFillCookies(cookies, request);
        if (invoker.isGenerateCurlText()) {
            EventBusCenter.post(InvokeResponseMessage.create(invoker.getEventId(), generateUrl(request)));
            return null;
        }
        log.info("execute sendRequestWithGet, request.url:[{}], request.headers:[{}]", request.getUrl(), JSONUtil.toJsonStr(request.headers()));
        HttpResponse response = request.execute(true);
        log.info("finish sendRequestWithGet, response.status:[{}], response.body:[{}]", response.getStatus(), response.body());
        return response;
    }

    private Method matchEnum(String method) {
        switch (method) {
            case "GET":
                return Method.GET;
            case "POST":
                return Method.POST;
            case "PUT":
                return Method.PUT;
            case "DELETE":
                return Method.DELETE;
            default:
                return Method.GET;
        }
    }

    private String generateUrl(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("curl --location --request ").append(request.getMethod().name())
                .append(" '").append(request.getUrl()).append("'");
        if (CollUtil.isNotEmpty(request.headers())) {
            request.headers().entrySet().stream().forEach(entry -> {
                String key = entry.getKey();
                if ("Accept-Encoding".equals(key)) {
                    return;
                }
                List<String> value = entry.getValue();
                sb.append(" --header '").append(key).append(":").append(Joiner.on(";").join(value)).append("'");
            });
        }
        if (Objects.nonNull(request.bodyBytes()) && request.bodyBytes().length > 0) {
            sb.append(" --data-raw  ")
                    .append("'").append(new String(request.bodyBytes())).append("'");
        }
        return sb.toString();
    }

    private Map<String, Object> dealGetJsonParam(Invoker invoker) {
//        Map<String, ParameterDesc> parameterDescMap = invoker.getMethodDesc().getParameterDescs().stream().collect(Collectors.toMap(ParameterDesc::getName, item -> item));
        Map<String, Object> params = invoker.getInvokeBody().getParams();
        Map<String, Object> getRequestParams = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            ParameterDesc parameterDesc = parameterDescMap.get(entry.getKey());
            if (JSONUtil.isTypeJSONObject(JsonStringUtil.toStr(entry.getValue()))) {
                JSONObject jsonObject = JSONUtil.parseObj(entry.getValue());
                for (Map.Entry<String, Object> objectEntry : jsonObject.entrySet()) {
                    getRequestParams.put(objectEntry.getKey(), objectEntry.getValue());
                }
            } else {
                getRequestParams.put(entry.getKey(), entry.getValue());
            }
        }
        return getRequestParams;
    }

    private HttpRequest requestFillHeaders(Map<String, String> headers, HttpRequest request) {
        if (MapUtil.isEmpty(headers)) {
            return request;
        }
        for (String headerName : headers.keySet()) {
            request.header(headerName, headers.get(headerName));
        }
        return request;
    }

    private HttpRequest requestFillCookies(Map<String, String> cookies, HttpRequest request) {
        if (CollUtil.isEmpty(cookies)) {
            return request;
        }
        List<String> collect = cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.toList());
        String cookie = Joiner.on(";").join(collect);
        request.header("Cookie", cookie);
        return request;
    }

    private Map<String, String> pickCookies(DomainConfig domainConfig) {
        log.info("start pickCookies, domainConfig:[{}]", JSONUtil.toJsonStr(domainConfig));
        PluginConfig pluginConfig = PluginConfigService.getInstance().read();
        BrowserEnum browserEnum = BrowserEnum.getBrowserEnumByName(pluginConfig.getBrowserType());
        List<String> domains = domainConfig.getCookies().stream().map(CookieConfig::getDomain).collect(Collectors.toList());
        List<String> cookieNames = domainConfig.getCookies().stream().map(CookieConfig::getSourceName).collect(Collectors.toList());
        List<Cookie> cookies = BrowserCookiesReadUtil.read(browserEnum, domains, cookieNames);
        Map<String, String> cookieNameMap = domainConfig.getCookies().stream().collect(Collectors.toMap(CookieConfig::getSourceName, CookieConfig::getTargetName));

        Map<String, String> map = cookies.stream().collect(Collectors.toMap(cookie -> cookie.getDomain() + "_" + cookie.getName(), cookie -> {
            if (Objects.nonNull(cookie.getValue())) {
                return cookie.getValue();
            }
            return "";
        }));
        Map<String, String> cookieMap = Maps.newHashMap();
        domainConfig.getCookies().stream().forEach(cookieConfig -> {
            String cookieValue = map.get(cookieConfig.getDomain() + "_" + cookieConfig.getSourceName());
            cookieMap.put(cookieNameMap.get(cookieConfig.getSourceName()), cookieValue);
        });
        log.info("finish pickCookies, response:[{}]", map);
        return cookieMap;
    }

    private DomainConfig readDomainConfig(String domainName) {
        return DomainConfigService.getInstance().readByDomain(true, domainName);
    }

}
