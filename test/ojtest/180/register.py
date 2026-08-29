import pandas as pd
import requests
import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
import os
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from datetime import datetime


def create_output_directory():
    """创建按时间戳命名的输出目录"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f"register_test_results_{timestamp}"
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


def register_user(user_data, timeout=10):
    """单个用户注册请求"""
    url = "http://localhost:89/api/v1/sys/auth/register"
#     url = "http://47.99.236.69:93/api/v1/sys/auth/register"

    # 构造请求数据
    payload = {
        "username": user_data["用户名"],
        "email": user_data["邮箱"],
        "password": user_data["密码"],
        "captchaCode": 9927,
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
            "状态码": response.status_code,
            "响应时间": response.elapsed.total_seconds(),
            "成功": response.status_code == 200
        }

        # 尝试解析响应内容
        try:
            response_data = response.json()
            result["响应数据"] = json.dumps(response_data, ensure_ascii=False)
            if "token" in response_data:
                result["token"] = response_data.get("token", "")[:50] + "..." if len(
                    response_data.get("token", "")) > 50 else response_data.get("token", "")
        except:
            result["响应数据"] = response.text[:100]  # 只取前100个字符
            result["token"] = "解析失败"

        return result

    except requests.exceptions.Timeout:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "状态码": "超时",
            "响应时间": timeout,
            "成功": False,
            "响应数据": "请求超时",
            "token": "无"
        }
    except requests.exceptions.ConnectionError:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "状态码": "连接错误",
            "响应时间": 0,
            "成功": False,
            "响应数据": "连接错误",
            "token": "无"
        }
    except Exception as e:
        return {
            "ID": user_data["ID"],
            "用户名": user_data["用户名"],
            "状态码": "异常",
            "响应时间": 0,
            "成功": False,
            "响应数据": f"其他错误: {str(e)}",
            "token": "无"
        }


def batch_register(users, max_workers=10, delay=0.1):
    """批量注册用户"""
    results = []
    success_count = 0
    fail_count = 0

    print(f"开始批量注册 {len(users)} 个用户，并发数: {max_workers}")
    start_time = time.time()

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        # 提交所有任务
        future_to_user = {executor.submit(register_user, user): user for user in users}

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

    print(f"\n批量注册完成!")
    print(f"总用户数: {len(users)}")
    print(f"成功: {success_count}")
    print(f"失败: {fail_count}")
    print(f"成功率: {success_count / len(users) * 100:.2f}%")
    print(f"总耗时: {total_time:.2f}秒")
    print(f"平均每个请求: {total_time / len(users):.2f}秒")

    return results


def save_results(results, output_dir):
    """保存注册结果到CSV文件"""
    filename = f'{output_dir}/注册结果.csv'
    df = pd.DataFrame(results)
    df.to_csv(filename, index=False, encoding='utf-8-sig')
    print(f"注册结果已保存到 {filename}")
    return filename


def calculate_performance_metrics(results):
    """计算详细的性能指标"""
    df = pd.DataFrame(results)
    success_count = df['成功'].sum()
    response_times = [x for x in df['响应时间'] if isinstance(x, (int, float)) and x > 0]

    if not response_times:
        return {}

    metrics = {
        'total_requests': len(df),
        'success_count': success_count,
        'fail_count': len(df) - success_count,
        'success_rate': success_count / len(df) * 100,
        'mean_response_time': np.mean(response_times),
        'median_response_time': np.median(response_times),
        'min_response_time': np.min(response_times),
        'max_response_time': np.max(response_times),
        'std_response_time': np.std(response_times),
        'p50_response_time': np.percentile(response_times, 50),
        'p75_response_time': np.percentile(response_times, 75),
        'p90_response_time': np.percentile(response_times, 90),
        'p95_response_time': np.percentile(response_times, 95),
        'p99_response_time': np.percentile(response_times, 99),
        'p999_response_time': np.percentile(response_times, 99.9),
    }

    return metrics


def create_success_rate_chart(results, output_dir):
    """创建成功率饼图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)
    success_count = df['成功'].sum()
    fail_count = len(df) - success_count
    sizes = [success_count, fail_count]
    labels = [f'Success\n{success_count}', f'Failed\n{fail_count}']
    colors = ['#66c2a5', '#fc8d62']

    plt.figure(figsize=(8, 6))
    plt.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=90)
    plt.title('register Success Rate')
    plt.tight_layout()
    plt.savefig(f'{charts_dir}/success_rate.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成成功率饼图")


def create_status_code_chart(results, output_dir):
    """创建状态码分布图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)
    status_counts = df['状态码'].value_counts().head(10)

    plt.figure(figsize=(10, 6))
    plt.bar(range(len(status_counts)), status_counts.values, color='#8da0cb')
    plt.title('Status Code Distribution')
    plt.xlabel('Status Code')
    plt.ylabel('Count')
    plt.xticks(range(len(status_counts)), [str(x) for x in status_counts.index], rotation=45)

    # 在柱子上显示数值
    for i, v in enumerate(status_counts.values):
        plt.text(i, v + 0.1, str(v), ha='center', va='bottom')

    plt.tight_layout()
    plt.savefig(f'{charts_dir}/status_code_distribution.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成状态码分布图")


def create_response_time_histogram(results, output_dir):
    """创建响应时间分布直方图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)
    response_times = [x for x in df['响应时间'] if isinstance(x, (int, float)) and x > 0]

    if response_times:
        metrics = calculate_performance_metrics(results)

        plt.figure(figsize=(12, 6))
        plt.hist(response_times, bins=30, color='#e78ac3', alpha=0.7, edgecolor='black')
        plt.title('Response Time Distribution')
        plt.xlabel('Response Time (seconds)')
        plt.ylabel('Request Count')

        # 添加多个百分位线
        percentiles = {
            'Mean': metrics['mean_response_time'],
            'Median': metrics['median_response_time'],
            'P95': metrics['p95_response_time'],
            'P99': metrics['p99_response_time']
        }

        colors = ['red', 'green', 'orange', 'purple']
        linestyles = ['--', '--', ':', '-.']

        for i, (label, value) in enumerate(percentiles.items()):
            plt.axvline(value, color=colors[i], linestyle=linestyles[i],
                        label=f'{label}: {value:.3f}s', alpha=0.8)

        plt.legend()
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        plt.savefig(f'{charts_dir}/response_time_histogram.png', dpi=300, bbox_inches='tight')
        plt.close()
        print("成功生成响应时间分布直方图")


def create_response_time_boxplot(results, output_dir):
    """创建响应时间箱线图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)
    response_times = [x for x in df['响应时间'] if isinstance(x, (int, float)) and x > 0]

    if response_times:
        plt.figure(figsize=(8, 6))
        plt.boxplot(response_times)
        plt.title('Response Time Box Plot')
        plt.ylabel('Response Time (seconds)')
        plt.tight_layout()
        plt.savefig(f'{charts_dir}/response_time_boxplot.png', dpi=300, bbox_inches='tight')
        plt.close()
        print("成功生成响应时间箱线图")


def create_response_time_trend(results, output_dir):
    """创建响应时间趋势图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)

    plt.figure(figsize=(12, 6))
    plt.plot(range(len(df)), df['响应时间'], 'o-', alpha=0.5, markersize=2, linewidth=0.5)
    plt.title('Response Time Trend')
    plt.xlabel('Request Sequence')
    plt.ylabel('Response Time (seconds)')
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(f'{charts_dir}/response_time_trend.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成响应时间趋势图")


def create_performance_summary(results, output_dir):
    """创建性能指标汇总图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    metrics = calculate_performance_metrics(results)

    plt.figure(figsize=(10, 8))
    plt.axis('off')

    if metrics:
        summary_text = f"""
Performance Summary:
Total Requests: {metrics['total_requests']}
Success: {metrics['success_count']}
Failed: {metrics['fail_count']}
Success Rate: {metrics['success_rate']:.2f}%

Response Time Statistics:
Mean: {metrics['mean_response_time']:.3f}s
Median: {metrics['median_response_time']:.3f}s
Min: {metrics['min_response_time']:.3f}s
Max: {metrics['max_response_time']:.3f}s
Std Dev: {metrics['std_response_time']:.3f}s

Percentile Analysis:
P50: {metrics['p50_response_time']:.3f}s
P75: {metrics['p75_response_time']:.3f}s
P90: {metrics['p90_response_time']:.3f}s
P95: {metrics['p95_response_time']:.3f}s
P99: {metrics['p99_response_time']:.3f}s
P99.9: {metrics['p999_response_time']:.3f}s
"""
    else:
        summary_text = "No valid response time data available"

    plt.text(0.1, 0.9, summary_text, transform=plt.gca().transAxes,
             verticalalignment='top', fontsize=11, family='monospace', linespacing=1.5)
    plt.title('Performance Summary Report')
    plt.tight_layout()
    plt.savefig(f'{charts_dir}/performance_summary.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成性能指标汇总图")


def create_percentile_chart(results, output_dir):
    """创建百分位响应时间图表"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    metrics = calculate_performance_metrics(results)

    if not metrics:
        return

    # 定义要显示的百分位
    percentiles = [50, 75, 90, 95, 99, 99.9]
    percentile_values = [
        metrics['p50_response_time'],
        metrics['p75_response_time'],
        metrics['p90_response_time'],
        metrics['p95_response_time'],
        metrics['p99_response_time'],
        metrics['p999_response_time']
    ]

    plt.figure(figsize=(10, 6))
    bars = plt.bar(range(len(percentiles)), percentile_values, color='skyblue', alpha=0.7)
    plt.title('Response Time Percentiles')
    plt.xlabel('Percentile')
    plt.ylabel('Response Time (seconds)')
    plt.xticks(range(len(percentiles)), [f'P{p}' for p in percentiles])

    # 在柱子上显示数值
    for i, (bar, value) in enumerate(zip(bars, percentile_values)):
        plt.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.001,
                 f'{value:.3f}s', ha='center', va='bottom')

    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(f'{charts_dir}/response_time_percentiles.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成百分位响应时间图表")


def create_detailed_status_chart(results, output_dir):
    """创建详细状态码分布饼图"""
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    df = pd.DataFrame(results)
    status_data = df['状态码'].value_counts()

    plt.figure(figsize=(10, 8))
    plt.pie(status_data.values, labels=[f'{k} ({v})' for k, v in status_data.items()],
            autopct='%1.1f%%', startangle=90)
    plt.title('Detailed Status Code Distribution')
    plt.tight_layout()
    plt.savefig(f'{charts_dir}/detailed_status_distribution.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("成功生成详细状态码分布图")


def print_detailed_metrics(results):
    """打印详细的性能指标到控制台"""
    metrics = calculate_performance_metrics(results)

    print("\n" + "=" * 50)
    print("详细性能指标分析")
    print("=" * 50)

    if metrics:
        print(f"总请求数: {metrics['total_requests']}")
        print(f"成功请求: {metrics['success_count']}")
        print(f"失败请求: {metrics['fail_count']}")
        print(f"成功率: {metrics['success_rate']:.2f}%")
        print("\n响应时间统计:")
        print(f"  平均值: {metrics['mean_response_time']:.3f}s")
        print(f"  中位数: {metrics['median_response_time']:.3f}s")
        print(f"  最小值: {metrics['min_response_time']:.3f}s")
        print(f"  最大值: {metrics['max_response_time']:.3f}s")
        print(f"  标准差: {metrics['std_response_time']:.3f}s")

        print("\n百分位分析:")
        print(f"  P50: {metrics['p50_response_time']:.3f}s")
        print(f"  P75: {metrics['p75_response_time']:.3f}s")
        print(f"  P90: {metrics['p90_response_time']:.3f}s")
        print(f"  P95: {metrics['p95_response_time']:.3f}s")
        print(f"  P99: {metrics['p99_response_time']:.3f}s")
        print(f"  P99.9: {metrics['p999_response_time']:.3f}s")
    else:
        print("没有有效的响应时间数据")


def save_performance_metrics(results, output_dir):
    """保存性能指标到文件"""
    metrics = calculate_performance_metrics(results)
    if metrics:
        metrics_file = f"{output_dir}/performance_metrics.txt"
        with open(metrics_file, 'w', encoding='utf-8') as f:
            f.write("详细性能指标分析\n")
            f.write("=" * 50 + "\n")
            f.write(f"总请求数: {metrics['total_requests']}\n")
            f.write(f"成功请求: {metrics['success_count']}\n")
            f.write(f"失败请求: {metrics['fail_count']}\n")
            f.write(f"成功率: {metrics['success_rate']:.2f}%\n\n")

            f.write("响应时间统计:\n")
            f.write(f"  平均值: {metrics['mean_response_time']:.3f}s\n")
            f.write(f"  中位数: {metrics['median_response_time']:.3f}s\n")
            f.write(f"  最小值: {metrics['min_response_time']:.3f}s\n")
            f.write(f"  最大值: {metrics['max_response_time']:.3f}s\n")
            f.write(f"  标准差: {metrics['std_response_time']:.3f}s\n\n")

            f.write("百分位分析:\n")
            f.write(f"  P50: {metrics['p50_response_time']:.3f}s\n")
            f.write(f"  P75: {metrics['p75_response_time']:.3f}s\n")
            f.write(f"  P90: {metrics['p90_response_time']:.3f}s\n")
            f.write(f"  P95: {metrics['p95_response_time']:.3f}s\n")
            f.write(f"  P99: {metrics['p99_response_time']:.3f}s\n")
            f.write(f"  P99.9: {metrics['p999_response_time']:.3f}s\n")

        print(f"性能指标已保存到 {metrics_file}")


def create_visualization(results, output_dir):
    """创建所有可视化图表"""
    print("\n正在生成可视化图表...")

    # 创建charts子目录
    charts_dir = f"{output_dir}/charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    # 生成各个单独的图表
    create_success_rate_chart(results, output_dir)
    create_status_code_chart(results, output_dir)
    create_response_time_histogram(results, output_dir)
    create_response_time_boxplot(results, output_dir)
    create_response_time_trend(results, output_dir)
    create_performance_summary(results, output_dir)
    create_percentile_chart(results, output_dir)
    create_detailed_status_chart(results, output_dir)

    # 保存性能指标到文件
    save_performance_metrics(results, output_dir)

    # 打印详细指标到控制台
    print_detailed_metrics(results)

    print(f"\n所有可视化图表已保存到 {charts_dir} 目录")


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
    # choice = input(f"即将向 {len(users)} 个用户发送注册请求，是否继续? (y/n): ")
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

    # 执行批量注册
    results = batch_register(users, max_workers=max_workers, delay=delay)

    # 保存结果
    save_results(results, output_dir)

    # 显示统计信息
    print("\n=== 详细统计 ===")
    status_summary = pd.DataFrame(results)['状态码'].value_counts()
    for status, count in status_summary.items():
        print(f"状态码 {status}: {count} 次")

    # 生成可视化图表
    create_visualization(results, output_dir)

    print(f"\n所有测试结果已保存到目录: {output_dir}")


if __name__ == "__main__":
    # 禁用SSL警告（如果证书有问题）
    import urllib3

    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    # 设置matplotlib后端为Agg，避免PyCharm显示问题
    import matplotlib

    matplotlib.use('Agg')  # 使用非交互式后端

    main()