package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ParameterDesc implements Serializable {

    private static final long serialVersionUID = 2646699746127900426L;
    /**
     * 名字
     */
    private String name;

    /**
     * 类型
     */
    private Class<?> type;

    /**
     * 默认值
     */
    private Object defaultValue;

}
