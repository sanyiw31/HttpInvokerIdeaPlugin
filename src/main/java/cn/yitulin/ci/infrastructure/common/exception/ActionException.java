package cn.yitulin.ci.infrastructure.common.exception;

import com.intellij.openapi.ui.Messages;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ActionException extends RuntimeException {

    private String title;

    public ActionException(String message, String title) {
        super(message);
        this.title = title;
    }

    public void showErrorDialog() {
        Messages.showErrorDialog(getMessage(), getTitle());
    }
}
