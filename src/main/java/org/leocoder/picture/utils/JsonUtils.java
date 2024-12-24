package org.leocoder.picture.utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-24 10:00
 * @description : Json 工具类
 */

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 JSON 字符串解析为 List<String>
     */
    public static List<String> parseTags(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("解析 JSON 失败：" + json, e);
        }
    }
}
