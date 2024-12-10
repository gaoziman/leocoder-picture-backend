-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS lg_picture
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用创建的数据库
USE lg_picture;


-- 用户表
CREATE TABLE IF NOT EXISTS user (
                                    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
                                    user_account    VARCHAR(256) NOT NULL COMMENT '账号',
                                    user_password   VARCHAR(512) NOT NULL COMMENT '密码',
                                    user_name       VARCHAR(256) NULL COMMENT '用户昵称',
                                    user_avatar     VARCHAR(1024) NULL COMMENT '用户头像',
                                    user_profile    VARCHAR(512) NULL COMMENT '用户简介',
                                    user_role       VARCHAR(256) DEFAULT 'user' NOT NULL COMMENT '用户角色：user/admin',
                                    edit_time       DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
                                    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                                    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
                                    is_delete       TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                                    UNIQUE KEY uk_user_account (user_account),
                                    INDEX idx_user_name (user_name)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;