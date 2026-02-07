-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE test;

-- 创建项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    owner VARCHAR(50) NULL,
    status INT NOT NULL COMMENT '0=DRAFT, 1=ACTIVE, 2=ARCHIVED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `external_call_log` (
                                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trace_id` varchar(64) NOT NULL COMMENT '请求追踪ID',
    `request_id` varchar(64) DEFAULT NULL COMMENT '请求ID',
    `service` varchar(50) NOT NULL COMMENT '服务名称',
    `target_url` varchar(255) NOT NULL COMMENT '目标URL',
    `http_method` varchar(10) NOT NULL COMMENT 'HTTP方法',
    `query_string` varchar(1000) DEFAULT NULL COMMENT '查询字符串',
    `http_status` int(11) DEFAULT NULL COMMENT 'HTTP状态码',
    `success` tinyint(1) NOT NULL COMMENT '是否成功',
    `attempt` int(11) NOT NULL COMMENT '尝试次数',
    `duration_ms` bigint(20) NOT NULL COMMENT '耗时毫秒',
    `exception_type` varchar(100) DEFAULT NULL COMMENT '异常类型',
    `exception_message` varchar(1000) DEFAULT NULL COMMENT '异常消息',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_service` (`service`),
    KEY `idx_created_at` (`created_at`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方调用日志表';