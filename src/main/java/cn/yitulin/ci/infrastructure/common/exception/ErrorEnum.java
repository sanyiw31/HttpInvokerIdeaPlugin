package cn.yitulin.ci.infrastructure.common.exception;

import com.intellij.openapi.ui.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorEnum {

    UN_SUPPORT_OPERATE_SYSTEM_ERROR("兼容性错误", "不支持当前系统"),

    CONFIG_FILE_NOT_EXISTS("插件配置错误", "插件配置文件不存在，请检查配置文件夹下是否存在config.json"),
    CONFIG_FILE_INIT_ERROR("插件配置错误", "插件配置文件初始化创建失败，请前往配置文件夹下手动config.json"),
    CONFIG_FILE_DATA_NOT_LEGAL_JSON("插件配置错误", "插件配置文件的数据是非法的JSON"),
    CONFIG_COOKIE_DB_PATH_ILLEGAL("插件配置错误", "Cookie数据库路径非法"),

    PARAMETER_ANALYSIS_ERROR("参数错误", "参数JSON错误"),
    ;

    private String title;
    private String message;

    public void showErrorDialog() {
        Messages.showErrorDialog(message, title);
    }

    public void showErrorDialog(String extMessage) {
        Messages.showErrorDialog(message + "\n" + extMessage, title);
    }

}
