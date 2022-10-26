package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CookieConfig {

    private String domain;

    private String sourceName;

    private String targetName;

}
