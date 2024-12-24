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


ALTER TABLE picture
    -- 添加新列
    ADD COLUMN review_status INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN review_message VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewer_id BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN review_time DATETIME NULL COMMENT '审核时间';
-- 添加新列
ALTER TABLE picture ADD COLUMN like_count INT DEFAULT 0 COMMENT '图片的点赞数';
-- 添加新列
ALTER TABLE comment ADD COLUMN like_count INT DEFAULT 0 COMMENT '评论的点赞数';
-- 添加新列    -- 添加新列
ALTER TABLE user_like ADD COLUMN is_liked INT DEFAULT 0 COMMENT '0表示未点赞，1表示已点赞';
-- 添加新列
ALTER TABLE user_like
    ADD like_type TINYINT NOT NULL DEFAULT 0 COMMENT '点赞类型，0-图片，1-评论';


-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_review_status ON picture (review_status);




-- 用户点赞表
CREATE TABLE IF NOT EXISTS user_like (
                                         id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
                                         user_id     BIGINT NOT NULL COMMENT '用户ID',
                                         picture_id  BIGINT NOT NULL COMMENT '图片ID',
                                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                                         UNIQUE KEY uk_user_picture (user_id, picture_id),  -- 防止重复点赞
                                         INDEX idx_picture_id (picture_id)                 -- 提升图片点赞查询效率
) COMMENT '用户点赞表';


-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorite (
                                             id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
                                             user_id     BIGINT NOT NULL COMMENT '用户ID',
                                             picture_id  BIGINT NOT NULL COMMENT '图片ID',
                                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                             UNIQUE KEY uk_user_picture (user_id, picture_id),
                                             INDEX idx_picture_id (picture_id)
) COMMENT '用户收藏表';


-- 图片评论表
CREATE TABLE IF NOT EXISTS comment (
                                       id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
                                       picture_id  BIGINT NOT NULL COMMENT '图片ID',
                                       user_id     BIGINT NOT NULL COMMENT '用户ID',
                                       parent_id   BIGINT NULL COMMENT '父评论ID',
                                       content     VARCHAR(1024) NOT NULL COMMENT '评论内容',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                                       INDEX idx_picture_id (picture_id),
                                       INDEX idx_parent_id (parent_id)
) COMMENT '图片评论表';


-- 用户积分记录表
CREATE TABLE IF NOT EXISTS user_points (
                                           id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '积分ID',
                                           user_id     BIGINT NOT NULL COMMENT '用户ID',
                                           action      VARCHAR(64) NOT NULL COMMENT '操作类型：like, comment, upload',
                                           points      INT NOT NULL COMMENT '积分值',
                                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                           INDEX idx_user_id (user_id)
) COMMENT '用户积分记录表';
