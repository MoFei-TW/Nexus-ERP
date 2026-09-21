-- ==============================================================================
-- MOFEI ERP 系統網站 - 資料庫結構定義腳本 (schema.sql)
-- DBMS: Microsoft SQL Server (MSSQL)
-- 目標資料庫: MOFEI
-- ==============================================================================

-- 1. 建立資料庫 (若不存在)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'MOFEI')
BEGIN
    CREATE DATABASE [MOFEI] COLLATE Chinese_Taiwan_Stroke_CI_AS;
END
GO

USE [MOFEI];
GO

-- 2. 建立資料表 (依外鍵相依順序建立)

-- ------------------------------------------------------------------------------
-- 2.1 員工主檔 (employee)
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[employee]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[employee] (
        [employee_id]       CHAR(7)         NOT NULL,
        [employee_name]     NVARCHAR(50)    NOT NULL,
        [password]          VARCHAR(100)    NOT NULL,
        [force_change_pwd]  BIT             NOT NULL DEFAULT 1,
        [phone]             VARCHAR(12)     NOT NULL,
        [email]             NVARCHAR(100)   NOT NULL,
        [status]            NVARCHAR(10)    NOT NULL DEFAULT N'在職',
        [role]              NVARCHAR(20)    NOT NULL DEFAULT N'EMPLOYEE',
        [created_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        [updated_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT [PK_employee] PRIMARY KEY CLUSTERED ([employee_id] ASC),
        CONSTRAINT [UK_employee_email] UNIQUE ([email]),
        CONSTRAINT [CK_employee_phone] CHECK ([phone] LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9]'),
        CONSTRAINT [CK_employee_email] CHECK ([email] LIKE '%_@__%.__%'),
        CONSTRAINT [CK_employee_status] CHECK ([status] IN (N'在職', N'離職', N'停權')),
        CONSTRAINT [CK_employee_role] CHECK ([role] IN (N'ADMIN', N'EMPLOYEE'))
    );
END
GO

-- ------------------------------------------------------------------------------
-- 2.2 產品主檔 (product)
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[product]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[product] (
        [product_id]        CHAR(7)         NOT NULL,
        [product_name]      NVARCHAR(100)   NOT NULL,
        [attributes]        NVARCHAR(255)   NULL,
        [price]             DECIMAL(18, 2)  NOT NULL DEFAULT 0,
        [stock]             INT             NOT NULL DEFAULT 26,
        [employee_id]       CHAR(7)         NOT NULL,
        [status]            NVARCHAR(10)    NOT NULL DEFAULT N'正常販售',
        [created_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        [updated_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT [PK_product] PRIMARY KEY CLUSTERED ([product_id] ASC),
        CONSTRAINT [UK_product_name] UNIQUE ([product_name]),
        CONSTRAINT [FK_product_employee] FOREIGN KEY ([employee_id]) REFERENCES [dbo].[employee] ([employee_id]),
        CONSTRAINT [CK_product_price] CHECK ([price] >= 0),
        CONSTRAINT [CK_product_stock] CHECK ([stock] >= 0),
        CONSTRAINT [CK_product_status] CHECK ([status] IN (N'正常販售', N'缺貨中', N'下架停售'))
    );
END
GO

-- ------------------------------------------------------------------------------
-- 2.3 客戶主檔 (customer)
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[customer]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[customer] (
        [customer_id]       VARCHAR(9)      NOT NULL,
        [customer_name]     NVARCHAR(50)    NOT NULL,
        [phone]             VARCHAR(20)     NOT NULL,
        [email]             NVARCHAR(100)   NULL,
        [company_name]      NVARCHAR(100)   NULL,
        [tax_id]            VARCHAR(9)      NULL,
        [country]           NVARCHAR(30)    NOT NULL DEFAULT N'台灣',
        [city]              NVARCHAR(30)    NOT NULL,
        [district]          NVARCHAR(30)    NOT NULL,
        [address]           NVARCHAR(200)   NOT NULL,
        [employee_id]       CHAR(7)         NOT NULL,
        [created_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        [updated_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT [PK_customer] PRIMARY KEY CLUSTERED ([customer_id] ASC),
        CONSTRAINT [FK_customer_employee] FOREIGN KEY ([employee_id]) REFERENCES [dbo].[employee] ([employee_id]),
        CONSTRAINT [CK_customer_phone] CHECK ([phone] LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]-[0-9][0-9][0-9]'),
        CONSTRAINT [CK_customer_email] CHECK ([email] IS NULL OR [email] LIKE '%_@__%.__%'),
        CONSTRAINT [CK_customer_tax_id] CHECK ([tax_id] IS NULL OR [tax_id] LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]')
    );
END
GO

-- ------------------------------------------------------------------------------
-- 2.4 訂單主檔 (orders)
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[orders]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[orders] (
        [order_id]          VARCHAR(25)     NOT NULL,
        [customer_id]       VARCHAR(9)      NOT NULL,
        [employee_id]       CHAR(7)         NOT NULL,
        [total_amount]      DECIMAL(18, 2)  NOT NULL DEFAULT 0,
        [status]            NVARCHAR(10)    NOT NULL DEFAULT N'處理中',
        [recipient_name]    NVARCHAR(50)    NOT NULL,
        [recipient_phone]   VARCHAR(20)     NOT NULL,
        [recipient_address] NVARCHAR(255)   NOT NULL,
        [created_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT [PK_orders] PRIMARY KEY CLUSTERED ([order_id] ASC),
        CONSTRAINT [FK_orders_customer] FOREIGN KEY ([customer_id]) REFERENCES [dbo].[customer] ([customer_id]),
        CONSTRAINT [FK_orders_employee] FOREIGN KEY ([employee_id]) REFERENCES [dbo].[employee] ([employee_id]),
        CONSTRAINT [CK_orders_total_amount] CHECK ([total_amount] >= 0),
        CONSTRAINT [CK_orders_status] CHECK ([status] IN (N'待付款', N'處理中', N'已出貨', N'已完成', N'已取消'))
    );
END
GO

-- ------------------------------------------------------------------------------
-- 2.5 訂單明細 (order_detail)
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[order_detail]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[order_detail] (
        [order_detail_id]       INT IDENTITY(1,1)   NOT NULL,
        [order_id]              VARCHAR(25)         NOT NULL,
        [product_id]            CHAR(7)             NOT NULL,
        [product_name_snapshot] NVARCHAR(100)       NOT NULL,
        [unit_price]            DECIMAL(18, 2)      NOT NULL DEFAULT 0,
        [quantity]              INT                 NOT NULL DEFAULT 1,
        [discount_amount]       DECIMAL(18, 2)      NOT NULL DEFAULT 0,
        [subtotal]              DECIMAL(18, 2)      NOT NULL DEFAULT 0,
        [created_at]            DATETIME2           NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT [PK_order_detail] PRIMARY KEY CLUSTERED ([order_detail_id] ASC),
        CONSTRAINT [FK_order_detail_orders] FOREIGN KEY ([order_id]) REFERENCES [dbo].[orders] ([order_id]) ON DELETE CASCADE,
        CONSTRAINT [FK_order_detail_product] FOREIGN KEY ([product_id]) REFERENCES [dbo].[product] ([product_id]),
        CONSTRAINT [CK_order_detail_unit_price] CHECK ([unit_price] >= 0),
        CONSTRAINT [CK_order_detail_quantity] CHECK ([quantity] > 0),
        CONSTRAINT [CK_order_detail_discount] CHECK ([discount_amount] >= 0),
        CONSTRAINT [CK_order_detail_subtotal] CHECK ([subtotal] >= 0)
    );
END
GO

-- ------------------------------------------------------------------------------
-- 3. 建立常用查詢索引
-- ------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_employee_name' AND object_id = OBJECT_ID(N'[dbo].[employee]'))
    CREATE NONCLUSTERED INDEX [IX_employee_name] ON [dbo].[employee] ([employee_name]);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_product_name' AND object_id = OBJECT_ID(N'[dbo].[product]'))
    CREATE NONCLUSTERED INDEX [IX_product_name] ON [dbo].[product] ([product_name]);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_customer_name' AND object_id = OBJECT_ID(N'[dbo].[customer]'))
    CREATE NONCLUSTERED INDEX [IX_customer_name] ON [dbo].[customer] ([customer_name]);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_orders_customer' AND object_id = OBJECT_ID(N'[dbo].[orders]'))
    CREATE NONCLUSTERED INDEX [IX_orders_customer] ON [dbo].[orders] ([customer_id]);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_orders_employee' AND object_id = OBJECT_ID(N'[dbo].[orders]'))
    CREATE NONCLUSTERED INDEX [IX_orders_employee] ON [dbo].[orders] ([employee_id]);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_order_detail_order' AND object_id = OBJECT_ID(N'[dbo].[order_detail]'))
    CREATE NONCLUSTERED INDEX [IX_order_detail_order] ON [dbo].[order_detail] ([order_id]);
GO
