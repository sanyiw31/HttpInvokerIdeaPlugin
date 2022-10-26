package cn.yitulin.ci.action;

import cn.yitulin.ci.infrastructure.common.Constants;
import cn.yitulin.ci.infrastructure.common.util.PsiTypeUtil;
import cn.yitulin.ci.infrastructure.common.enums.SpringRestMappingAnnotationEnum;
import cn.yitulin.ci.infrastructure.common.exception.ActionException;
import cn.yitulin.ci.infrastructure.model.*;
import cn.yitulin.ci.infrastructure.service.PluginConfigService;
import cn.yitulin.ci.ui.ControllerInvokeFrame;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * author : ⚡️
 * description :
 * date : Created in 2021/10/29 5:08 下午
 * modified : 💧💨🔥
 */
@Slf4j
public class ControllerMethodInvokeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        log.info("start actionPerformed");
        try {
            checkPluginSetting();
        } catch (ActionException actionException) {
            Messages.showErrorDialog(actionException.getMessage(), actionException.getTitle());
            return;
        }
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiMethod)) {
            Messages.showErrorDialog("仅支持对方法使用", "不适用的目标");
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiMethod psiMethod = (PsiMethod) psiElement;
        MethodDesc methodDesc;
        try {
            methodDesc = createMethodDesc(psiFile, psiMethod);
        } catch (ActionException actionException) {
            Messages.showErrorDialog(actionException.getMessage(), actionException.getTitle());
            return;
        }
        if (Objects.isNull(methodDesc)) {
            log.error("execute actionPerformed,方法签名构建失败,无法唤起插件调用窗口.");
            return;
        }
        Invoker invoker = Invoker.build(methodDesc);
        new ControllerInvokeFrame(invoker);
        log.info("finish actionPerformed");
    }

    private void checkPluginSetting() throws ActionException {
        PluginConfig pluginConfig = PluginConfigService.getInstance().read();
        if (Objects.isNull(pluginConfig) || Strings.isNullOrEmpty(pluginConfig.getConfigFileDirectory())) {
            throw new ActionException(Constants.MISS_SETTING, Constants.PLUGIN_WARNING_TITLE);
        }
    }

    private MethodDesc createMethodDesc(PsiFile psiFile, PsiMethod psiMethod) throws ActionException {
        SpringMappingAnnotationDesc springMappingAnnotationDesc = buildSpringMappingAnnotationDesc(psiMethod);
        List<ParameterDesc> parameterDescs = buildParameterDescs(psiMethod);
        String packagePath = pickPackagePath(psiFile, psiMethod);
        String interfaceName = pickInterfaceName(psiMethod);
        return MethodDesc.builder()
                .packagePath(packagePath)
                .interfaceName(interfaceName)
                .methodName(psiMethod.getName())
                .annotationDesc(springMappingAnnotationDesc)
                .parameterDescs(parameterDescs)
                .build();
    }

    private SpringMappingAnnotationDesc buildSpringMappingAnnotationDesc(PsiMethod psiMethod) {
        return SpringMappingAnnotationDesc.builder()
                .httpMethod(pickMethodMappingType(psiMethod))
                .methodMappingPath(pickMethodMappingPath(psiMethod))
                .classMappingPath(pickClassMappingPath(psiMethod))
                .build();
    }

    private List<ParameterDesc> buildParameterDescs(PsiMethod psiMethod) {
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        List<ParameterDesc> parameterDescs = Lists.newArrayList();
        for (PsiParameter psiParameter : psiParameters) {
            ParameterDesc parameterDesc = ParameterDesc.builder()
                    .name(psiParameter.getName())
                    .type(psiParameter.getType().getClass())
                    .defaultValue(PsiTypeUtil.getDefaultValue(psiParameter.getType()))
                    .build();
            parameterDescs.add(parameterDesc);
        }
        return parameterDescs;
    }

    private String pickPackagePath(PsiFile psiFile, PsiMethod psiMethod) {
        if (psiMethod.getParent() instanceof PsiClassImpl) {
            return ((PsiJavaFile) psiFile.getContainingFile()).getPackageName();
        } else if (psiMethod.getParent() instanceof ClsClassImpl) {
            String interfaceQualifiedName = ((ClsClassImpl) psiMethod.getParent()).getQualifiedName();
            return interfaceQualifiedName.substring(0, interfaceQualifiedName.lastIndexOf("."));
        } else {
            throw new ActionException(Constants.INCORRECT_INVOKE_LOCATION, Constants.PLUGIN_ERROR_TITLE);
        }
    }

    private String pickInterfaceName(PsiMethod psiMethod) {
        if (psiMethod.getParent() instanceof PsiClassImpl) {
            return ((PsiClassImpl) psiMethod.getParent()).getName();
        } else if (psiMethod.getParent() instanceof ClsClassImpl) {
            String interfaceQualifiedName = ((ClsClassImpl) psiMethod.getParent()).getQualifiedName();
            return interfaceQualifiedName.substring(interfaceQualifiedName.lastIndexOf(".") + 1);
        } else {
            throw new ActionException(Constants.INCORRECT_INVOKE_LOCATION, Constants.PLUGIN_ERROR_TITLE);
        }
    }

    private String pickMethodMappingType(PsiMethod psiMethod) {
        for (PsiAnnotation psiAnnotation : psiMethod.getAnnotations()) {
            SpringRestMappingAnnotationEnum annotationEnum = SpringRestMappingAnnotationEnum.getByAnnotation(psiAnnotation.getQualifiedName());
            if (Objects.isNull(annotationEnum) || SpringRestMappingAnnotationEnum.DEFAULT.equals(annotationEnum)) {
                continue;
            }
            return annotationEnum.name();
        }
        return SpringRestMappingAnnotationEnum.GET.name();
    }

    private String pickMethodMappingPath(PsiMethod psiMethod) {
        return pickMappingPath(psiMethod.getAnnotations());
    }

    private String pickClassMappingPath(PsiMethod psiMethod) {
        if (psiMethod.getParent() instanceof PsiClassImpl) {
            PsiClassImpl psiClass = (PsiClassImpl) psiMethod.getParent();
            return pickMappingPath(psiClass.getAnnotations());
        } else if (psiMethod.getParent() instanceof ClsClassImpl) {
            ClsClassImpl clsClass = (ClsClassImpl) psiMethod.getParent();
            return pickMappingPath(clsClass.getAnnotations());
        } else {
            throw new ActionException(Constants.INCORRECT_INVOKE_LOCATION, Constants.PLUGIN_ERROR_TITLE);
        }
    }

    private String pickMappingPath(PsiAnnotation[] psiAnnotations) {
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            SpringRestMappingAnnotationEnum annotationEnum = SpringRestMappingAnnotationEnum.getByAnnotation(psiAnnotation.getQualifiedName());
            if (Objects.isNull(annotationEnum)) {
                continue;
            }
            String text = psiAnnotation.findAttributeValue("value").getText();
            if (text.startsWith("\"")) {
                text = text.substring(1);
            }
            if (text.endsWith("\"")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.equals("{}")) {
                return "";
            }
            return text;
        }
        return "";
    }

}
