-- 创建数据库
CREATE DATABASE IF NOT EXISTS bl_picture DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换数据库
USE bl_picture;

-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id            BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userAccount   VARCHAR(256)                           NOT NULL COMMENT '账号',
    userPassword  VARCHAR(512)                           NOT NULL COMMENT '密码',
    userName      VARCHAR(256)                           NULL COMMENT '用户昵称',
    userAvatar    VARCHAR(1024)                          NULL COMMENT '用户头像',
    userProfile   VARCHAR(512)                           NULL COMMENT '用户简介',
    userRole      VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin',
    editTime      DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    createTime    DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime    DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete      TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
#     扩展思路
    vipExpireTime DATETIME                               NULL COMMENT '会员过期时间',
    vipCode       VARCHAR(128)                           NULL COMMENT '会员兑换码',
    vipNumber     BIGINT                                 NULL COMMENT '会员编号',
    shareCode     VARCHAR(20)  DEFAULT NULL COMMENT '分享码',
    inviteUser    BIGINT       DEFAULT NULL COMMENT '邀请用户 id',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE IF NOT EXISTS picture
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    url          VARCHAR(512)                       NOT NULL COMMENT '图片 url',
    name         VARCHAR(128)                       NOT NULL COMMENT '图片名称',
    introduction VARCHAR(512)                       NULL COMMENT '简介',
    category     VARCHAR(64)                        NULL COMMENT '分类',
    tags         VARCHAR(512)                       NULL COMMENT '标签（JSON 数组）',
    picSize      BIGINT                             NULL COMMENT '图片体积',
    picWidth     INT                                NULL COMMENT '图片宽度',
    picHeight    INT                                NULL COMMENT '图片高度',
    picScale     DOUBLE                             NULL COMMENT '图片宽高比例',
    picFormat    VARCHAR(32)                        NULL COMMENT '图片格式',
    userId       BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete     TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) COMMENT '图片' COLLATE = utf8mb4_unicode_ci;

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';
ALTER TABLE picture
    ADD COLUMN originSize bigint NULL COMMENT '原图大小',
    ADD COLUMN originUrl varchar(512) NULL COMMENT '原图 url';
ALTER TABLE `bl_picture`.`picture`
    ADD COLUMN `resourceStatus` tinyint DEFAULT 0 NOT NULL COMMENT '资源状态：0-存在存储服务器中、1-从存储服务器中删除';


-- 分类标签表
CREATE TABLE IF NOT EXISTS category_tag
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    type       TINYINT                            NOT NULL COMMENT '类型（0-分类、1-标签）',
    name       VARCHAR(128)                       NOT NULL COMMENT '名称',
    useNum     INT      DEFAULT 0                 NOT NULL COMMENT '使用数量',
    userId     BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_userId (userId)
) COMMENT '分类标签表' COLLATE = utf8mb4_unicode_ci;

-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 添加新列
ALTER TABLE picture
    ADD COLUMN spaceId  bigint  null comment '空间 id（为空表示公共空间）';
-- 创建索引
CREATE INDEX idx_spaceId ON picture (spaceId);

-- 添加新列
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';
-- 创建索引
CREATE INDEX idx_picColor ON picture (picColor);

-- 添加新列, 空间类型
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有 1-团队';
CREATE INDEX idx_spaceType ON space (spaceType);

-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),                    -- 提升按空间查询的性能
    INDEX idx_userId (userId)                       -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;

-- 会员标识
ALTER TABLE user
    ADD COLUMN vipSign varchar(255) NULL COMMENT '会员标识：VIP/SVIP';
