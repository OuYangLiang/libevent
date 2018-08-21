CREATE TABLE IF NOT EXISTS `event_queue_0` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_1` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_2` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_3` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_4` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_5` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_6` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';

CREATE TABLE IF NOT EXISTS `event_queue_7` (
  `id`          bigint      NOT NULL  auto_increment COMMENT '主键',
  `event_type`  char(12)    NOT NULL COMMENT '事件类型',
  `event_time`  datetime    NOT NULL COMMENT '事件发生时间',
  `context`     mediumtext  NOT NULL COMMENT '事件内容',
  `group`       int         NOT NULL COMMENT '事件分组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件队列表';
