package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
public class InvokeBody implements Serializable {

    private static final long serialVersionUID = 1711037656889461468L;
    /**
     * 域名
     */
    private String domainName;
    /**
     * url
     */
    private String url;
    /**
     * http方法
     */
    private String httpMethod;
    /**
     * 参数个数
     */
    private Map<String, Object> params;

}
