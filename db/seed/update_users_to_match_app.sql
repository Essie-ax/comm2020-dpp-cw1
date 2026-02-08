-- 把「改之前」的 users 数据更新成应用能识别的样子（不改表结构）
-- 在 Navicat 里对 comm2020_dpp 执行即可

USE comm2020_dpp;

-- 1) 把 keeper1 改成 gamekeeper1
UPDATE users SET username = 'gamekeeper1' WHERE username = 'keeper1';

-- 2) 把占位符密码改成 "password" 的 SHA-256 哈希（和代码里 PasswordUtil 一致）
UPDATE users SET password_hash = '5e884898da2847151d0e56f8dc6292773603d0d6aabbdd62a11ef721d154151d8'
WHERE user_id IN (1, 2);

-- 执行完后：player1 / gamekeeper1 用密码 password 都能登录。
