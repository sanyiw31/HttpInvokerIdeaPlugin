package cn.yitulin.ci.infrastructure.common;

/**
 * author : ⚡️
 * description :
 * date : Created in 2022/6/20 10:42
 * modified : 💧💨🔥
 */
public class Constants {

    public static final String CONFIG_FILE_DIRECTORY_PLACEHOLDER = "请选择用于存储配置文件的文件夹";
    public static final String PLUGIN_WARNING_TITLE = "⚠️⚠️⚠️警⚠️告⚠️⚠️⚠️";
    public static final String PLUGIN_ERROR_TITLE = "HttpInvokerIdeaPlugin错误";
    public static final String INCORRECT_INVOKE_LOCATION = "不支持在此处进行方法调用";
    public static final String MISS_SETTING = "请先前往IDEA设置完成HttpInvokerIdeaPlugin插件配置";
    public static final String CONFIG_FILE_NAME="config.json";
    public static final String CONFIG_FILE_TEMPLATE="[\n" +
            "    {\n" +
            "        \"cookies\": [\n" +
            "        ],\n" +
            "        \"headers\": {\n" +
            "        },\n" +
            "        \"domain\": \"http://localhost:8080/\"\n" +
            "    }\n" +
            "]";
    public static final String LOG_FILE_NAME="http-invoker-log.json";
    public static final Integer INVOKE_LOG_MAX_SIZE = 500;
    public static final String DATE_PATTERN="yyyy-MM-dd HH:mm:ss";

}
