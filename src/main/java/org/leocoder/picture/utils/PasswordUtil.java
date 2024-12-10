package org.leocoder.picture.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:02
 * @description : 密码工具类
 */

public class PasswordUtil {

    /**
     * 根据固定输入生成确定的20位盐值
     *
     * @param input 固定输入，例如用户名或其他固定标识
     * @return 生成的固定盐值
     */
    public static String generateFixedSalt(String input) {
        try {
            // 使用 SHA-256 哈希算法
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 对输入进行哈希计算
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 转换为 Base64 编码，取前20位
            String base64Hash = Base64.getEncoder().encodeToString(hash);
            // 取前20位作为盐值
            return base64Hash.substring(0, 20);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成固定盐值：", e);
        }
    }

    public static void main(String[] args) {
        // 测试生成固定盐值
        String input = "leocoder";
        String salt = generateFixedSalt(input);
        System.out.println("生成的固定盐值: " + salt);
    }
}
