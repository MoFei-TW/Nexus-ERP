-- ==============================================================================
-- MOFEI ERP 系統網站 - 基礎種子資料腳本 (data.sql)
-- DBMS: Microsoft SQL Server (MSSQL)
-- 目標資料庫: MOFEI
-- 說明：僅保留系統管理員帳號，其餘範例資料已移除
-- ==============================================================================

USE [MOFEI];
GO

-- 1. 初始化員工主檔 (employee) - 系統管理員
-- 密碼採用 BCrypt 安全雜湊：
-- admin (密碼: admin) 的雜湊值: $2a$10$vhEArnSuz1q9OgFxYjEnWOCD.YF2oIK0l80wqOPF8bZxyPU84oKfu

IF NOT EXISTS (SELECT 1 FROM [dbo].[employee] WHERE [employee_id] = '2026000')
BEGIN
    INSERT INTO [dbo].[employee] 
        ([employee_id], [employee_name], [password], [force_change_pwd], [phone], [email], [status], [role])
    VALUES 
        ('2026000', N'系統管理員', '$2a$10$vhEArnSuz1q9OgFxYjEnWOCD.YF2oIK0l80wqOPF8bZxyPU84oKfu', 0, '0900-000-000', 'admin@mofei.com', N'在職', N'ADMIN');
END
GO
