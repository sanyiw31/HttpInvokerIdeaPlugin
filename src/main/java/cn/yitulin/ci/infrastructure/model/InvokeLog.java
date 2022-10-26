package cn.yitulin.ci.infrastructure.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/21 19:37
 * modified : 💧💨🔥
 */
@Data
public class InvokeLog implements Serializable {

    private static final long serialVersionUID = -3765557716467544054L;
    /**
     * 关键
     */
    private String key;
    /**
     * 调用一次
     */
    private LocalDateTime invokeTime;
    /**
     * 参数个数
     */
    private Map<String, Object> params;

    /**
     * 响应数据
     */
    private String responseData;

}
