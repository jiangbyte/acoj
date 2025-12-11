# 设置 avatar 和 background 为随机图片

UPDATE sys_user SET avatar = CONCAT('https://picsum.photos', '/400/400') WHERE deleted = 0;
UPDATE sys_user SET background = CONCAT('https://picsum.photos', '/1200/400') WHERE deleted = 0;
