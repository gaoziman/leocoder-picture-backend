package org.leocoder.picture.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:08
 * @description : 用户角色枚举类
 */
@Getter
public enum UserRoleEnum {
    USER("普通用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    // 使用静态 Map 缓存所有枚举值
    private static final Map<String, UserRoleEnum> VALUE_MAP = new HashMap<>();

    // 在静态代码块中初始化缓存
    static {
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            VALUE_MAP.put(roleEnum.getValue(), roleEnum);
        }
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举类型，找不到则返回 null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        return VALUE_MAP.get(value);
    }
}
