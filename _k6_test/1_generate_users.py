import pandas as pd
import random
import string
import os

# 初始化
random.seed(42)  # 设置种子以确保结果可重现


def generate_username(length_range=(6, 12)):
    """生成随机用户名：6-12位小写字母"""
    length = random.randint(length_range[0], length_range[1])
    username = ''.join(random.choice(string.ascii_lowercase) for _ in range(length))
    return username


def generate_email(username):
    """生成邮箱：用户名@各类邮箱"""
    email_domains = [
        'gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com',
        '163.com', 'qq.com', 'sina.com', '126.com', 'sohu.com'
    ]
    domain = random.choice(email_domains)
    return f"{username}@{domain}"


def generate_password(length_range=(10, 20)):
    """生成随机密码：10-20位，包含字母和数字"""
    length = random.randint(length_range[0], length_range[1])
    characters = string.ascii_letters + string.digits
    password = ''.join(random.choice(characters) for _ in range(length))
    return password


def generate_users(num_users=1000):
    """生成指定数量的用户数据"""
    users = []

    for i in range(num_users):
        username = generate_username()
        email = generate_email(username)
        password = generate_password()

        user = {
            'ID': i + 1,
            '用户名': username,
            '邮箱': email,
            '密码': password
        }
        users.append(user)

    return users


def save_to_csv(users, filename='test_users.csv'):
    """将用户数据保存到CSV文件"""
    df = pd.DataFrame(users)

    # 设置列的顺序
    df = df[['ID', '用户名', '邮箱', '密码']]

    # 保存到CSV
    df.to_csv(filename, index=False, encoding='utf-8-sig')
    print(f"成功生成 {len(users)} 个用户数据，已保存到 {filename}")


def main():
    """主函数"""
    print("开始生成测试用户数据...")

    # 生成1000个用户
    users = generate_users(1000)
    save_to_csv(users, 'test_users.csv')

    print("数据生成完成！")


if __name__ == "__main__":
    main()