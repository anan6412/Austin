-- 创建数据库
CREATE DATABASE IF NOT EXISTS lifecircle DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lifecircle;

-- 创建帖子表
CREATE TABLE IF NOT EXISTS `posts` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键 ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户 ID',
    `text` TEXT NOT NULL COMMENT '帖子内容',
    `images` LONGTEXT COMMENT '图片列表 (JSON 格式)',
    `lng` DOUBLE NOT NULL COMMENT '经度',
    `lat` DOUBLE NOT NULL COMMENT '纬度',
    `address` VARCHAR(500) COMMENT '地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_created_at` (`created_at` DESC),
    INDEX `idx_location` (`lng`, `lat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
