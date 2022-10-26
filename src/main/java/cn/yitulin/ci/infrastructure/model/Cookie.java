package cn.yitulin.ci.infrastructure.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author : ⚡️
 * @date : Created in 2022/3/9 20:05
 * description :
 * modified :
 */
@Data
@Builder
public class Cookie {

    protected String domain;
    protected String name;
    protected String value;
    protected Date createTime;
    protected Date expireTime;

}
