package cn.yitulin.ci.infrastructure.common.util;

import cn.hutool.json.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @Author: ⚡️
 * @Description:
 * @Date: Created in 2021/7/12 6:52 下午
 * Modified By:
 */
public class PsiTypeUtil {

    public static final Map<String, Object> NORMAL_TYPES = Maps.newHashMap();
    public static final Map<String, Object> CUSTOM_TYPES = Maps.newHashMap();
    private static String pattern = "yyyy-MM-dd HH:mm:ss";
    private static DateFormat df = new SimpleDateFormat(pattern);
    private static final Set<String> PASS_KEY = Sets.newHashSet("serialVersionUID");

    static {
        NORMAL_TYPES.put("Object", null);
        NORMAL_TYPES.put("Boolean", false);
        NORMAL_TYPES.put("Byte", 0);
        NORMAL_TYPES.put("Short", Short.valueOf((short) 0));
        NORMAL_TYPES.put("Integer", 0);
        NORMAL_TYPES.put("Long", 0L);
        NORMAL_TYPES.put("Float", 0.0F);
        NORMAL_TYPES.put("Double", 0.0D);
        NORMAL_TYPES.put("String", "");
        NORMAL_TYPES.put("BigDecimal", 0.0);
        NORMAL_TYPES.put("Date", df.format(new Date()));
        NORMAL_TYPES.put("Timestamp", System.currentTimeMillis());
        NORMAL_TYPES.put("LocalDate", LocalDate.now().toString());
        NORMAL_TYPES.put("LocalTime", LocalTime.now().toString());
        NORMAL_TYPES.put("LocalDateTime", LocalDateTime.now().toString());
    }

    public static boolean isPrimitiveType(PsiType psiType) {
        return psiType instanceof PsiPrimitiveType;
    }

    public static boolean isNormalType(PsiType psiType) {
        return NORMAL_TYPES.containsKey(psiType.getPresentableText());
    }

    public static boolean isArrayType(PsiType psiType) {
        return psiType instanceof PsiArrayType;
    }

    public static boolean isMapType(PsiType psiType) {
        String psiTypePresentableText = psiType.getPresentableText();
        return psiTypePresentableText.matches("Map<(.*)>");
    }

    public static boolean isCollectionType(PsiType psiType) {
        return isListType(psiType) || isSetType(psiType);
    }

    public static boolean isSetType(PsiType psiType) {
        String psiTypePresentableText = psiType.getPresentableText();
        return psiTypePresentableText.matches("Set<(.*)>");
    }

    public static boolean isListType(PsiType psiType) {
        String psiTypePresentableText = psiType.getPresentableText();
        return psiTypePresentableText.matches("List<(.*)>");
    }

    public static boolean isEnumType(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(psiType);
        if (Objects.isNull(psiClass)) {
            return false;
        }
        return psiClass.isEnum();
    }

    public static Object getDefaultValue(PsiType psiType) {
        if (isPrimitiveType(psiType)) {
            return PsiTypesUtil.getDefaultValue(psiType);
        } else if (isNormalType(psiType)) {
            return NORMAL_TYPES.get(psiType.getPresentableText());
        } else if (isArrayType(psiType)) {
            PsiType deepType = psiType.getDeepComponentType();
            Object defaultValue = getDefaultValue(deepType);
            ArrayList<Object> list = Lists.newArrayList(defaultValue);
            return list;
        } else if (isCollectionType(psiType)) {
            PsiType iterableType = PsiUtil.extractIterableTypeParameter(psiType, false);
            Object defaultValue = getDefaultValue(iterableType);
            ArrayList<Object> list = Lists.newArrayList(defaultValue);
            return list;
        } else if (isMapType(psiType)) {
            return Maps.newHashMap();
        } else if (isEnumType(psiType)) {
            StringBuilder sb = new StringBuilder();
            PsiField[] fieldList = PsiUtil.resolveClassInClassTypeOnly(psiType).getFields();
            if (fieldList != null && fieldList.length > 0) {
                for (PsiField f : fieldList) {
                    if (f instanceof PsiEnumConstant) {
                        sb.append(f.getName()).append("|");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } else {
            PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
            if (CUSTOM_TYPES.containsKey(psiClass.getQualifiedName())) {
                return CUSTOM_TYPES.get(psiClass.getQualifiedName());
            }
            CUSTOM_TYPES.put(psiClass.getQualifiedName(), null);
            JSONObject jsonObject = new JSONObject();
            for (PsiField field : psiClass.getAllFields()) {
                PsiType fieldType = field.getType();
                String name = field.getName();
                if (PASS_KEY.contains(name)) {
                    continue;
                }
                Object defaultValue = PsiTypeUtil.getDefaultValue(fieldType);
                jsonObject.put(name, defaultValue);
            }
            CUSTOM_TYPES.put(psiClass.getQualifiedName(), jsonObject);
            return jsonObject;
        }
    }

}
