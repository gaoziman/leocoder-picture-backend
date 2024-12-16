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

-- 图片表
create table if not exists picture
(
    id              bigint auto_increment comment 'id' primary key,
    url             varchar(512)                       not null comment '图片 url',
    name            varchar(128)                       not null comment '图片名称',
    introduction    varchar(512)                       null comment '简介',
    category        varchar(64)                        null comment '分类',
    tags            varchar(512)                       null comment '标签（JSON 数组）',
    pic_size        bigint                             null comment '图片体积',
    pic_width       int                                null comment '图片宽度',
    pic_height      int                                null comment '图片高度',
    pic_scale       double                             null comment '图片宽高比例',
    pic_format      varchar(32)                        null comment '图片格式',
    user_id         bigint                             not null comment '创建用户 id',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    edit_time       datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_user_id (user_id)            -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;