import pandas as pd
import requests
import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
import os
from datetime import datetime


def create_output_directory():
    """创建按时间戳命名的输出目录"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f"login_test_results_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    print(f"创建输出目录: {output_dir}")
    return output_dir


def read_users_from_csv(filename):
    """从CSV文件读取用户数据"""
    try:
        df = pd.read_csv(filename, encoding='utf-8-sig')
        users = df.to_dict('records')
        print(f"成功从 {filename} 读取 {len(users)} 个用户数据")
        return users
    except Exception as e:
        print(f"读取CSV文件失败: {e}")
        return []


def login_user(user_data, timeout=10):
    """单个用户登录请求"""
    url = "http://8.130.21.17:89/api/v1/sys/auth/login"

    # 构造请求数据
    payload = {
        "username": user_data["用户名"],
        "email": user_data["邮箱"],
        "password": user_data["密码"],
        "captchaCode": 9926,
        "platform": 'CLIENT',
    }

    headers = {
        "Content-Type": "application/json",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    try:
        response = requests.post(
            url,
            json=payload,
            headers=headers,
            timeout=timeout,
            verify=False  # 如果证书有问题可以跳过验证
        )

        result = {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "邮箱": user_data["邮箱"],
            "密码": user_data["密码"],
            "状态码": response.status_code,
            "响应时间": response.elapsed.total_seconds(),
            "成功": response.status_code == 200
        }

        # 尝试解析响应内容
        try:
            response_data = response.json()
            result["Token"] = json.dumps(response_data, ensure_ascii=False)
            if "data" in response_data:
                result["Token"] = response_data.get("data", "")
        except:
            result["Token"] = response.text[:100]  # 只取前100个字符
            result["token"] = "解析失败"

        return result

    except requests.exceptions.Timeout:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "邮箱": user_data["邮箱"],
            "密码": user_data["密码"],
            "状态码": "超时",
            "响应时间": timeout,
            "成功": False,
            "Token": "请求超时",
            "token": "无"
        }
    except requests.exceptions.ConnectionError:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "状态码": "连接错误",
            "响应时间": 0,
            "成功": False,
            "Token": "连接错误",
            "token": "无"
        }
    except Exception as e:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "状态码": "异常",
            "响应时间": 0,
            "成功": False,
            "Token": f"其他错误: {str(e)}",
            "token": "无"
        }


def batch_register(users, max_workers=10, delay=0.1):
    """批量登录用户"""
    results = []
    success_count = 0
    fail_count = 0

    print(f"开始批量登录 {len(users)} 个用户，并发数: {max_workers}")
    start_time = time.time()

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        # 提交所有任务
        future_to_user = {executor.submit(login_user, user): user for user in users}

        # 处理完成的任务
        for i, future in enumerate(as_completed(future_to_user)):
            result = future.result()
            results.append(result)

            if result["成功"]:
                success_count += 1
                status = "✓"
            else:
                fail_count += 1
                status = "✗"

            # 打印进度
            print(
                f"{status} 进度: {i + 1}/{len(users)} | 用户: {result['用户名']} | 状态: {result['状态码']} | 耗时: {result['响应时间']:.2f}s")

            # 添加延迟以避免对服务器造成过大压力
            time.sleep(delay)

    end_time = time.time()
    total_time = end_time - start_time

    print(f"\n批量登录完成!")
    print(f"总用户数: {len(users)}")
    print(f"成功: {success_count}")
    print(f"失败: {fail_count}")
    print(f"成功率: {success_count / len(users) * 100:.2f}%")
    print(f"总耗时: {total_time:.2f}秒")
    print(f"平均每个请求: {total_time / len(users):.2f}秒")

    return results


def save_results(results, output_dir):
    """保存登录结果到CSV文件"""
    filename = f'{output_dir}/登录结果.csv'
    df = pd.DataFrame(results)
    df.to_csv(filename, index=False, encoding='utf-8-sig')
    print(f"登录结果已保存到 {filename}")
    return filename


def update_csv_with_tokens(results, csv_file):
    """将登录成功的Token更新到CSV文件中"""
    try:
        df = pd.read_csv(csv_file, encoding='utf-8-sig')
        
        token_updates = {}
        for result in results:
            if result['成功'] and result.get('Token'):
                user_id = result['ID']
                token_updates[user_id] = result['Token']
        
        if token_updates:
            df['Token'] = df['ID'].map(token_updates).fillna('')
            df.to_csv(csv_file, index=False, encoding='utf-8-sig')
            print(f"已将 {len(token_updates)} 个Token更新到 {csv_file}")
        else:
            print("没有成功的登录，未更新Token")
            
    except Exception as e:
        print(f"更新CSV文件失败: {e}")


def main():
    """主函数"""
    # 创建输出目录
    output_dir = create_output_directory()

    # 读取用户数据
    csv_file = '测试用户数据_1000个.csv'

    if not os.path.exists(csv_file):
        print(f"CSV文件 {csv_file} 不存在，请先运行生成用户数据的脚本")
        return

    users = read_users_from_csv(csv_file)
    if not users:
        print("没有读取到用户数据，程序退出")
        return

    # 询问是否继续
    # choice = input(f"即将向 {len(users)} 个用户发送登录请求，是否继续? (y/n): ")
    # if choice.lower() != 'y':
    #     print("操作已取消")
    #     return

    # 设置并发参数
    try:
        max_workers = int(input("请输入并发数 (默认10): ") or "10")
        delay = float(input("请输入请求间隔秒数 (默认0.1): ") or "0.1")
    except ValueError:
        print("输入无效，使用默认值")
        max_workers = 10
        delay = 0.1

    # 执行批量登录
    results = batch_register(users, max_workers=max_workers, delay=delay)

    # 保存结果
    save_results(results, output_dir)

    # 显示统计信息
    print("\n=== 详细统计 ===")
    status_summary = pd.DataFrame(results)['状态码'].value_counts()
    for status, count in status_summary.items():
        print(f"状态码 {status}: {count} 次")

    # 将Token更新到CSV文件
    update_csv_with_tokens(results, csv_file)

    print(f"\n所有测试结果已保存到目录: {output_dir}")


if __name__ == "__main__":
    # 禁用SSL警告（如果证书有问题）
    import urllib3

    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    main()