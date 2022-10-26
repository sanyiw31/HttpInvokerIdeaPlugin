package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/21 16:56
 * modified : 💧💨🔥
 */
@Data
@Builder
public class Invoker implements Serializable {

    private static final long serialVersionUID = 1108235176949139352L;
    /**
     * 方法签名
     */
    @NotNull
    private MethodDesc methodDesc;

    /**
     * 事件ID
     */
    @NotNull
    private Long eventId;

    /**
     * 调用内容
     */
    private InvokeBody invokeBody;

    private boolean generateCurlText;

    public String concatLogKey(String env) {
        return env + ":::" + methodDesc.concatMethodSignature();
    }

    public static Invoker build(MethodDesc methodDesc) {
        return Invoker.builder()
                .methodDesc(methodDesc)
                .eventId(System.currentTimeMillis())
                .build();
    }

}
