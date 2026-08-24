# 设置 avatar 和 background 为随机图片

UPDATE sys_user SET avatar = CONCAT('https://picsum.photos', '/400/400') WHERE deleted = 0;
UPDATE sys_user SET background = CONCAT('https://picsum.photos', '/1200/400') WHERE deleted = 0;

# 应用 Logo 切到前端本地简约标（C2）
UPDATE sys_config SET value = '/logo.svg', description = '建议 1:1 图片；默认使用前端 public/logo.svg' WHERE code = 'APP_LOGO';
