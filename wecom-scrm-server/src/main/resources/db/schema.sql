-- Initialize Database Schema for WeCom SCRM (MVP)

-- 1. 内部员工表
CREATE TABLE IF NOT EXISTS `wecom_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `userid` VARCHAR(64) NOT NULL UNIQUE COMMENT '企微账号(员工ID)',
    `name` VARCHAR(64) NOT NULL COMMENT '姓名',
    `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `department_ids` VARCHAR(255) DEFAULT NULL COMMENT '所属部门(JSON格式或逗号分隔)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1=已激活, 2=已禁用, 4=未激活, 5=退出企业',
    `scrm_status` TINYINT DEFAULT 0 COMMENT 'SCRM系统自定义状态: 0=正常, 1=封禁',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信内部员工表';

-- 2. 外部客户表
CREATE TABLE IF NOT EXISTS `wecom_customer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `external_userid` VARCHAR(64) NOT NULL UNIQUE COMMENT '企微外部联系人ID',
    `name` VARCHAR(64) NOT NULL COMMENT '客户微信昵称或企微名称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `type` TINYINT DEFAULT 1 COMMENT '联系人类型: 1=微信用户, 2=企微用户',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0=未知, 1=男性, 2=女性',
    `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信开放平台的唯一身份标识',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信外部客户表';

-- 3. 客户跟进关系表 (员工-客户多对多关系)
CREATE TABLE IF NOT EXISTS `wecom_customer_relation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `external_userid` VARCHAR(64) NOT NULL COMMENT '企微外部联系人ID',
    `userid` VARCHAR(64) NOT NULL COMMENT '企微账号(员工ID)',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '该员工对客户的备注名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '该员工对客户的描述',
    `add_way` INT DEFAULT 0 COMMENT '客户添加来源',
    `state` VARCHAR(64) DEFAULT NULL COMMENT '企业自定义的state参数',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0=正常, 1=已删除, 2=已流失',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '该员工添加此客户的时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_userid_external` (`userid`, `external_userid`),
    KEY `idx_external_userid` (`external_userid`),
    KEY `idx_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进关系归属表';

-- 4. 同步日志表
CREATE TABLE IF NOT EXISTS `wecom_sync_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `sync_type` VARCHAR(32) NOT NULL COMMENT '同步类型: USER_SYNC, CUSTOMER_SYNC等',
    `status` TINYINT NOT NULL COMMENT '状态: 0=进行中, 1=成功, 2=失败',
    `error_msg` TEXT DEFAULT NULL COMMENT '失败时的错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步任务日志表';

-- 5. 企业标签组表
CREATE TABLE IF NOT EXISTS `wecom_tag_group` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企微标签组ID',
    `group_name` VARCHAR(64) NOT NULL COMMENT '标签组名称',
    `order_num` INT DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信标签组表';

-- 6. 企业标签表
CREATE TABLE IF NOT EXISTS `wecom_tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tag_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企微标签ID',
    `group_id` VARCHAR(64) NOT NULL COMMENT '所属标签组ID',
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `order_num` INT DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信标签表';

-- 7. 客户标签关系表
CREATE TABLE IF NOT EXISTS `wecom_customer_tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `external_userid` VARCHAR(64) NOT NULL COMMENT '外部联系人ID',
    `tag_id` VARCHAR(64) NOT NULL COMMENT '标签ID',
    `userid` VARCHAR(64) NOT NULL COMMENT '添加标签的员工ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_customer_tag_user` (`external_userid`, `tag_id`, `userid`),
    KEY `idx_external_userid` (`external_userid`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关系表';

-- 8. 部门表
CREATE TABLE IF NOT EXISTS `wecom_department` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `department_id` BIGINT NOT NULL UNIQUE COMMENT '企微部门ID',
    `name` VARCHAR(128) NOT NULL COMMENT '部门名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父部门ID',
    `order` BIGINT DEFAULT NULL COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信部门表';

-- 9. 朋友圈任务表
CREATE TABLE IF NOT EXISTS `wecom_moment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `moment_id` VARCHAR(64) DEFAULT NULL COMMENT '企微朋友圈任务ID',
    `jobid` VARCHAR(64) DEFAULT NULL COMMENT '异步任务ID',
    `text` TEXT DEFAULT NULL COMMENT '朋友圈文字内容',
    `attachments` TEXT DEFAULT NULL COMMENT '朋友圈附件(JSON格式)',
    `visible_range_type` TINYINT DEFAULT 0 COMMENT '可见范围类型: 0=全部, 1=部分',
    `visible_range_users` TEXT DEFAULT NULL COMMENT '可见范围明细(JSON格式)',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0=处理中, 1=已发布, 2=发布失败',
    `creator_userid` VARCHAR(64) DEFAULT NULL COMMENT '创建者员工ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信朋友圈任务表';

-- 10. 朋友圈员工发布记录表
CREATE TABLE IF NOT EXISTS `wecom_moment_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `moment_id` BIGINT NOT NULL COMMENT '本系统朋友圈任务自增ID',
    `userid` VARCHAR(64) NOT NULL COMMENT '员工ID',
    `publish_status` TINYINT DEFAULT 0 COMMENT '发布状态: 0=未发布, 1=已发布',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_moment_user` (`moment_id`, `userid`),
    KEY `idx_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈员工发布明细表';

-- 11. 客户欢迎语配置表
CREATE TABLE IF NOT EXISTS `wecom_welcome_msg` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL COMMENT '欢迎语名称',
    `text` TEXT DEFAULT NULL COMMENT '欢迎语文字内容',
    `attachments` JSON DEFAULT NULL COMMENT '附件内容(JSON格式)',
    `user_ids` JSON DEFAULT NULL COMMENT '适用员工ID列表(JSON格式)',
    `department_ids` JSON DEFAULT NULL COMMENT '适用部门ID列表(JSON格式)',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否为默认欢迎语: 0=否, 1=是',
    `sys_create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `sys_update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信客户欢迎语配置表';

-- 12. 客户群发任务表
CREATE TABLE IF NOT EXISTS `wecom_customer_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_name` VARCHAR(128) DEFAULT NULL COMMENT '任务名称',
    `send_type` TINYINT DEFAULT 0 COMMENT '发送类型: 0=立即发送, 1=定时发送',
    `send_time` DATETIME DEFAULT NULL COMMENT '定时发送时间',
    `target_type` TINYINT DEFAULT 0 COMMENT '发送范围: 0=全部客户, 1=筛选客户',
    `target_condition` TEXT DEFAULT NULL COMMENT '筛选条件(JSON格式)',
    `content` TEXT DEFAULT NULL COMMENT '文字内容',
    `attachments` TEXT DEFAULT NULL COMMENT '附件内容(JSON格式)',
    `sender_list` TEXT DEFAULT NULL COMMENT '发送人员工ID(JSON格式)',
    `msgid` VARCHAR(64) DEFAULT NULL COMMENT '企微群发消息ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0=待发送, 1=发送中, 2=已完成, 3=发送失败',
    `fail_msg` TEXT DEFAULT NULL COMMENT '失败原因',
    `creator_userid` VARCHAR(64) DEFAULT NULL COMMENT '创建人员工ID',
    `target_count` INT DEFAULT 0 COMMENT '目标客户数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信客户群发任务表';

-- 13. 客户群发执行记录表
CREATE TABLE IF NOT EXISTS `wecom_customer_message_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT NOT NULL COMMENT '群发任务ID',
    `external_userid` VARCHAR(64) NOT NULL COMMENT '外部联系人ID',
    `userid` VARCHAR(64) NOT NULL COMMENT '员工ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0=待执行, 1=已发送, 2=发送失败, 3=未送达',
    `send_time` DATETIME DEFAULT NULL COMMENT '实际发送时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_message_id` (`message_id`),
    KEY `idx_userid` (`userid`),
    KEY `idx_external_userid` (`external_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户群发执行明细表';

-- 14. 客户群表
CREATE TABLE IF NOT EXISTS `wecom_group_chat` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企微群ID',
    `name` VARCHAR(128) DEFAULT NULL COMMENT '群名称',
    `owner` VARCHAR(64) DEFAULT NULL COMMENT '群主ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '企微端群创建时间',
    `notice` TEXT DEFAULT NULL COMMENT '群公告',
    `member_count` INT DEFAULT 0 COMMENT '成员人数',
    `status` INT DEFAULT 0 COMMENT '状态: 0=正常, 1=已解散, 2=已删除',
    `sys_create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `sys_update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信客户群表';

-- 15. 客户群成员表
CREATE TABLE IF NOT EXISTS `wecom_group_chat_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id` VARCHAR(64) NOT NULL COMMENT '企微群ID',
    `userid` VARCHAR(64) NOT NULL COMMENT '成员ID',
    `type` TINYINT DEFAULT NULL COMMENT '类型: 1=内部员工, 2=外部联系人',
    `join_time` DATETIME DEFAULT NULL COMMENT '入群时间',
    `join_scene` INT DEFAULT NULL COMMENT '入群方式',
    `invitor` VARCHAR(64) DEFAULT NULL COMMENT '邀请人ID',
    UNIQUE KEY `uk_chat_user` (`chat_id`, `userid`),
    KEY `idx_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户群成员明细表';

-- 16. 客户群群发任务表
CREATE TABLE IF NOT EXISTS `wecom_group_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_name` VARCHAR(128) DEFAULT NULL COMMENT '任务名称',
    `send_type` TINYINT DEFAULT 0 COMMENT '发送类型: 0=立即发送, 1=定时发送',
    `send_time` DATETIME DEFAULT NULL COMMENT '定时发送时间',
    `target_type` TINYINT DEFAULT 0 COMMENT '发送范围: 0=所有客户群, 1=筛选客户群',
    `target_condition` TEXT DEFAULT NULL COMMENT '筛选条件(JSON格式)',
    `content` TEXT DEFAULT NULL COMMENT '文字内容',
    `attachments` TEXT DEFAULT NULL COMMENT '附件内容(JSON格式)',
    `msgid` VARCHAR(64) DEFAULT NULL COMMENT '企微群发消息ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0=待发送, 1=发送中, 2=已完成, 3=发送失败',
    `fail_msg` TEXT DEFAULT NULL COMMENT '失败原因',
    `creator_userid` VARCHAR(64) DEFAULT NULL COMMENT '创建人员工ID',
    `target_count` INT DEFAULT 0 COMMENT '目标客户群数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信客户群群发任务表';

-- 17. 企业管理配置表 (主数据库)
CREATE TABLE IF NOT EXISTS `wecom_enterprise` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `corp_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '企业微信CorpId',
    `name` VARCHAR(128) NOT NULL COMMENT '企业名称',
    `agent_id` INT NOT NULL COMMENT '应用AgentId',
    `agent_secret` VARCHAR(128) NOT NULL COMMENT '应用Secret',
    `token` VARCHAR(128) DEFAULT NULL COMMENT '回调验证Token',
    `encoding_aes_key` VARCHAR(128) DEFAULT NULL COMMENT '回调加密AES Key',
    `db_url` VARCHAR(255) NOT NULL COMMENT '租户数据库JDBC URL',
    `db_username` VARCHAR(64) NOT NULL COMMENT '数据库用户名',
    `db_password` VARCHAR(64) NOT NULL COMMENT '数据库密码',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业基本信息配置表';

-- 18. 企业微信事件回调记录表
CREATE TABLE IF NOT EXISTS `wecom_event_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `corp_id` VARCHAR(64) NOT NULL COMMENT '所属企业ID',
    `msg_type` VARCHAR(32) COMMENT '消息类型',
    `event` VARCHAR(32) COMMENT '事件类型',
    `change_type` VARCHAR(64) COMMENT '变更类型',
    `external_userid` VARCHAR(64) COMMENT '外部联系人ID',
    `userid` VARCHAR(64) COMMENT '涉及员工ID',
    `content` TEXT COMMENT '事件明细内容(JSON)',
    `status` TINYINT DEFAULT 0 COMMENT '处理状态: 0=待处理, 1=成功, 2=失败',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `error_msg` TEXT COMMENT '错误日志',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_corp_id` (`corp_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信事件回调记录与补偿表';



-- 19. 微信公众号配置表
CREATE TABLE IF NOT EXISTS `wecom_mp_account` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL COMMENT '公众号名称',
    `app_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '公众号AppId',
    `secret` VARCHAR(128) NOT NULL COMMENT '公众号Secret',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信公众号配置表';

-- 20. 微信公众号用户表
CREATE TABLE IF NOT EXISTS `wecom_mp_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `openid` VARCHAR(64) NOT NULL COMMENT '微信OpenId',
    `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionId',
    `nickname` VARCHAR(128) DEFAULT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `mp_app_id` VARCHAR(64) NOT NULL COMMENT '所属公众号AppId',
    `mp_name` VARCHAR(128) DEFAULT NULL COMMENT '所属公众号名称',
    `subscribe_time` DATETIME DEFAULT NULL COMMENT '关注时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_mp_openid` (`mp_app_id`, `openid`),
    KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信公众号用户表';
