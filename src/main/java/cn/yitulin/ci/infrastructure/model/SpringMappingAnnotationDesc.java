package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * author : ⚡️
 * description : SpringWebMappingMethodAnnotationDesc
 * date : Created in 2022/6/21 16:56
 * modified : 💧💨🔥
 */
@Data
@Builder
public class SpringMappingAnnotationDesc implements Serializable {

    private static final long serialVersionUID = 1456236516021073637L;
    /**
     * http方法
     *
     * @see cn.yitulin.ci.infrastructure.common.enums.HttpMethodEnum
     */
    private String httpMethod;

    /**
     * 类映射路径
     */
    private String classMappingPath;

    /**
     * 方法映射路径
     */
    private String methodMappingPath;

}
