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

-- 21. 阅文产品配置表
CREATE TABLE IF NOT EXISTS `wecom_yuewen_product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_name` VARCHAR(128) NOT NULL COMMENT '产品名称',
    `app_flag` VARCHAR(64) NOT NULL UNIQUE COMMENT 'APPFLAG',
    `wx_app_id` VARCHAR(64) DEFAULT NULL COMMENT '微信公众号AppId',
    `status` TINYINT DEFAULT 1 COMMENT '启用状态: 1=启用, 0=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅文产品管理配置表';

-- 22. 阅文用户表
CREATE TABLE IF NOT EXISTS `wecom_yuewen_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `guid` BIGINT NOT NULL COMMENT '阅文用户ID',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信OpenId',
    `nickname` VARCHAR(128) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像URL',
    `app_flag` VARCHAR(64) NOT NULL COMMENT '所属产品标识',
    `wx_app_id` VARCHAR(64) DEFAULT NULL COMMENT '微信公众号AppId',
    `external_userid` VARCHAR(64) DEFAULT NULL COMMENT '关联企微外部联系人ID',
    `charge_amount` BIGINT DEFAULT 0 COMMENT '累计充值金额(分)',
    `charge_num` INT DEFAULT 0 COMMENT '累计充值次数',
    `is_subscribe` TINYINT DEFAULT 0 COMMENT '是否关注: 0=否, 1=是',
    `regist_time` DATETIME DEFAULT NULL COMMENT '用户在阅文的注册时间',
    `vip_end_time` DATETIME DEFAULT NULL COMMENT '会员到期时间',
    `channel_name` VARCHAR(128) DEFAULT NULL COMMENT '来源渠道',
    `book_name` VARCHAR(255) DEFAULT NULL COMMENT '来源书籍',
    `yuewen_update_time` DATETIME DEFAULT NULL COMMENT '用户在阅文的更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_app_openid` (`app_flag`, `openid`),
    KEY `idx_guid` (`guid`),
    KEY `idx_external_userid` (`external_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅文同步用户表';
 
-- 23. 阅文消费记录表
CREATE TABLE IF NOT EXISTS `wecom_yuewen_consume_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `app_flag` VARCHAR(64) NOT NULL COMMENT '所属产品标识',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信OpenId',
    `guid` BIGINT DEFAULT NULL COMMENT '阅文 user ID',
    `order_id` VARCHAR(128) DEFAULT NULL COMMENT '订单号',
    `consume_id` VARCHAR(128) DEFAULT NULL COMMENT '消费订单号id',
    `worth_amount` INT DEFAULT 0 COMMENT '消费有价币金额',
    `free_amount` INT DEFAULT 0 COMMENT '消费免费币金额',
    `consume_time` DATETIME DEFAULT NULL COMMENT '消费时间',
    `book_id` BIGINT DEFAULT NULL COMMENT '订阅作品id',
    `book_name` VARCHAR(255) DEFAULT NULL COMMENT '订阅作品名称',
    `chapter_id` VARCHAR(128) DEFAULT NULL COMMENT '订阅作品章节id',
    `chapter_name` VARCHAR(255) DEFAULT NULL COMMENT '订阅作品章节名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_app_openid` (`app_flag`, `openid`),
    KEY `idx_guid` (`guid`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅文消费记录表';

-- 24. 常读产品配置表
CREATE TABLE IF NOT EXISTS `wecom_changdu_product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_name` VARCHAR(128) NOT NULL COMMENT '产品名称',
    `distributor_id` BIGINT NOT NULL UNIQUE COMMENT '分销ID',
    `app_id` INT DEFAULT NULL COMMENT '平台生成AppId',
    `app_type` TINYINT DEFAULT NULL COMMENT '1-快应用, 3-微信',
    `wx_app_id` VARCHAR(64) DEFAULT NULL COMMENT '微信公众号AppId',
    `status` TINYINT DEFAULT 1 COMMENT '启用状态: 1=启用, 0=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常读产品管理配置表';

-- 25. 常读用户表
CREATE TABLE IF NOT EXISTS `wecom_changdu_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `distributor_id` BIGINT NOT NULL COMMENT '分销ID',
    `encrypted_device_id` VARCHAR(128) NOT NULL COMMENT '加密设备ID',
    `device_brand` VARCHAR(64) DEFAULT NULL COMMENT '手机厂商',
    `media_source` VARCHAR(64) DEFAULT NULL COMMENT '媒体来源',
    `book_source` VARCHAR(128) DEFAULT NULL COMMENT '书籍来源',
    `recharge_times` BIGINT DEFAULT 0 COMMENT '充值次数',
    `recharge_amount` BIGINT DEFAULT 0 COMMENT '累计充值金额',
    `balance_amount` BIGINT DEFAULT 0 COMMENT '余额',
    `register_time` VARCHAR(32) DEFAULT NULL COMMENT '注册时间',
    `promotion_id` VARCHAR(64) DEFAULT NULL COMMENT '推广链id',
    `promotion_name` VARCHAR(255) DEFAULT NULL COMMENT '推广链名',
    `book_name` VARCHAR(255) DEFAULT NULL COMMENT '来源书名',
    `external_id` VARCHAR(64) DEFAULT NULL COMMENT '关联企微外部联系人ID',
    `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenId',
    `nickname` VARCHAR(128) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_dist_device` (`distributor_id`, `encrypted_device_id`),
    KEY `idx_external_id` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常读同步用户表';

-- 26. 常读充值记录表
CREATE TABLE IF NOT EXISTS `wecom_changdu_recharge_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `distributor_id` BIGINT NOT NULL COMMENT '分销ID',
    `trade_no` BIGINT NOT NULL UNIQUE COMMENT '常读订单ID',
    `out_trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方订单号',
    `device_id` VARCHAR(128) DEFAULT NULL COMMENT '设备唯一标识',
    `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenId',
    `external_id` VARCHAR(64) DEFAULT NULL COMMENT '企微用户ID',
    `pay_way` VARCHAR(32) DEFAULT NULL COMMENT '支付方式',
    `pay_fee` BIGINT DEFAULT 0 COMMENT '支付金额',
    `status` VARCHAR(32) DEFAULT NULL COMMENT '支付状态',
    `book_id` BIGINT DEFAULT NULL COMMENT '充值来源书籍ID',
    `book_name` VARCHAR(255) DEFAULT NULL COMMENT '书名',
    `promotion_id` BIGINT DEFAULT NULL COMMENT '推广链ID',
    `pay_time` VARCHAR(32) DEFAULT NULL COMMENT '订单支付时间',
    `order_create_time` VARCHAR(32) DEFAULT NULL COMMENT '订单创建时间',
    `recharge_type` TINYINT DEFAULT NULL COMMENT '充值类型: 0-单次充值, 1-会员充值, 2-整剧购买',
    `order_type` TINYINT DEFAULT 1 COMMENT '订单类型: 1-虚拟支付, 2-非虚拟支付',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_dist_trade` (`distributor_id`, `trade_no`),
    KEY `idx_external_id` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常读充值记录表';
