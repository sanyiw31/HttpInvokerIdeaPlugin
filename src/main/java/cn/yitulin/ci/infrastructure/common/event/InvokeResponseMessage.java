package cn.yitulin.ci.infrastructure.common.event;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/24 14:46
 * modified : 💧💨🔥
 */
@Data
public class InvokeResponseMessage {

    private long eventId;
    private LocalDateTime time;
    private String response;

    public static InvokeResponseMessage create(long eventId, String response) {
        InvokeResponseMessage message = new InvokeResponseMessage();
        message.setEventId(eventId);
        message.setTime(LocalDateTime.now());
        message.setResponse(response);
        return message;
    }

}
