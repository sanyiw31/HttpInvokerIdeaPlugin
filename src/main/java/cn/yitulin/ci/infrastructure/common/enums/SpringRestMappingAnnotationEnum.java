package cn.yitulin.ci.infrastructure.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpringRestMappingAnnotationEnum {

    DEFAULT("org.springframework.web.bind.annotation.RequestMapping"),
    GET("org.springframework.web.bind.annotation.GetMapping"),
    POST("org.springframework.web.bind.annotation.PostMapping"),
    PUT("org.springframework.web.bind.annotation.PutMapping"),
    DELETE("org.springframework.web.bind.annotation.DeleteMapping"),
    ;

    private String annotation;

    public static SpringRestMappingAnnotationEnum getByAnnotation(String annotation) {
        for (SpringRestMappingAnnotationEnum value : SpringRestMappingAnnotationEnum.values()) {
            if (value.getAnnotation().equals(annotation)) {
                return value;
            }
        }
        return null;
    }

}
