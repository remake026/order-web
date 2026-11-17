-- ============================================================
-- 点菜系统数据库初始化脚本
-- 只在 MySQL 数据卷首次创建时自动执行（docker-entrypoint-initdb.d）
-- 若想重新初始化：docker compose down -v && docker compose up -d
-- ============================================================

CREATE DATABASE IF NOT EXISTS ordering
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ordering;

-- ---------- 分类 ----------
CREATE TABLE IF NOT EXISTS category (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50)  NOT NULL COMMENT '分类名称',
    sort_no INT          NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '菜品分类';

-- ---------- 菜品 ----------
CREATE TABLE IF NOT EXISTS dish (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    category_id  BIGINT        NOT NULL COMMENT '所属分类',
    name         VARCHAR(100)  NOT NULL COMMENT '菜品名称',
    price        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    description  VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    image_url    VARCHAR(500)  DEFAULT NULL COMMENT '图片地址，形如 /uploads/xxx.jpg',
    is_available TINYINT       NOT NULL DEFAULT 1 COMMENT '1 上架 / 0 下架',
    sort_no      INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dish_category (category_id),
    KEY idx_dish_available (is_available)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '菜品';

-- ---------- 订单主表 ----------
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    table_id     VARCHAR(50)   NOT NULL COMMENT '桌号',
    remark       VARCHAR(500)  DEFAULT NULL COMMENT '顾客备注',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总额',
    status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待处理 / COMPLETED 已完成',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_orders_table (table_id),
    KEY idx_orders_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '订单';

-- ---------- 订单明细 ----------
CREATE TABLE IF NOT EXISTS order_item (
    id        BIGINT        NOT NULL AUTO_INCREMENT,
    order_id  BIGINT        NOT NULL COMMENT '所属订单',
    dish_id   BIGINT        NOT NULL COMMENT '菜品 ID',
    dish_name VARCHAR(100)  NOT NULL COMMENT '下单时的菜名快照',
    price     DECIMAL(10,2) NOT NULL COMMENT '下单时的单价快照',
    quantity  INT           NOT NULL COMMENT '数量',
    PRIMARY KEY (id),
    KEY idx_item_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '订单明细';

-- ---------- 演示分类 ----------
INSERT INTO category (name, sort_no) VALUES
    ('热菜', 1),
    ('凉菜', 2),
    ('主食', 3),
    ('饮料', 4);

-- ---------- 演示菜品 ----------
INSERT INTO dish (category_id, name, price, description, image_url, is_available, sort_no) VALUES
    (1, '宫保鸡丁',   38.00, '经典川菜，鸡丁花生，微辣回甜', NULL, 1, 1),
    (1, '鱼香肉丝',   32.00, '咸甜酸辣，下饭首选',           NULL, 1, 2),
    (1, '麻婆豆腐',   26.00, '麻辣鲜香，嫩豆腐配牛肉末',     NULL, 1, 3),
    (1, '糖醋里脊',   36.00, '外酥里嫩，酸甜可口',           NULL, 1, 4),
    (2, '拍黄瓜',     12.00, '蒜香清爽，解腻开胃',           NULL, 1, 1),
    (2, '口水鸡',     28.00, '红油麻辣，鸡肉嫩滑',           NULL, 1, 2),
    (2, '凉拌木耳',   16.00, '爽脆爽口',                     NULL, 1, 3),
    (3, '米饭',        2.00, '东北五常大米',                 NULL, 1, 1),
    (3, '蛋炒饭',     15.00, '粒粒分明，锅气十足',           NULL, 1, 2),
    (4, '可乐',        5.00, '冰镇罐装',                     NULL, 1, 1),
    (4, '酸梅汤',      8.00, '自制解腻',                     NULL, 1, 2);
