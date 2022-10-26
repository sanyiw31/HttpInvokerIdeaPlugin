package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DomainConfig {

    private String domain;

    private Map<String, String> headers;

    private List<CookieConfig> cookies;

}
