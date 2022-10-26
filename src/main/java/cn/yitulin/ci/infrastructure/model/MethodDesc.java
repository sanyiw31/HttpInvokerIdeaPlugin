package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * author : ⚡️
 * description :
 * date : Created in 2021/11/7 20:29
 * modified : 💧💨🔥
 */
@Data
@Builder
public class MethodDesc implements Serializable {

    private static final long serialVersionUID = 3584677819492333898L;
    /**
     * 包路径
     */
    private String packagePath;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 注解desc
     */
    private SpringMappingAnnotationDesc annotationDesc;

    /**
     * params desc
     */
    private List<ParameterDesc> parameterDescs;

    public String buildApiPath() {
        if (Objects.isNull(annotationDesc)) {
            return "";
        }
        String classMappingPath = annotationDesc.getClassMappingPath();
        String methodMappingPath = annotationDesc.getMethodMappingPath();
        if (!classMappingPath.startsWith("/")) {
            classMappingPath = "/" + classMappingPath;
        }
        if (classMappingPath.endsWith("/")) {
            classMappingPath = classMappingPath.substring(0, classMappingPath.length() - 1);
        }
        if (!methodMappingPath.startsWith("/")) {
            methodMappingPath = "/" + methodMappingPath;
        }
        if (methodMappingPath.endsWith("/")) {
            methodMappingPath = methodMappingPath.substring(0, methodMappingPath.length() - 1);
        }
        return classMappingPath + methodMappingPath;
    }

    public String buildUrl(String domain) {
        if (Objects.isNull(annotationDesc)) {
            return domain;
        }
        String apiPath = buildApiPath();
        if (domain.endsWith("/")) {
            apiPath = apiPath.substring(1);
        }
        return domain + apiPath;
    }

    public String concatServerName() {
        return packagePath + "." + interfaceName;
    }

    public String concatSimpleMethodName() {
        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(this.getInterfaceName())
                .append(".").append(this.getMethodName());
        return methodSignature.toString();
    }

    public String concatFullMethodName() {
        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(this.getMethodName()).append("(");
        for (ParameterDesc paramsDesc : parameterDescs) {
            if (!methodSignature.toString().endsWith("(")) {
                methodSignature.append(",");
            }
            methodSignature.append(paramsDesc.getType().getName());
        }
        methodSignature.append(")");
        return methodSignature.toString();
    }

    public String concatMethodSignature() {
        return concatServerName() + "." + concatFullMethodName();
    }

}
