import pandas as pd
import json
import os
from datetime import datetime


def load_performance_data():
    """加载性能测试数据"""
    # 从JSON文件加载数据
    if os.path.exists('performance_test_results.json'):
        print("从JSON文件加载测试数据...")
        with open('performance_test_results.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        return data
    
    # 从CSV文件加载数据
    elif os.path.exists('performance_summary.csv'):
        print("从CSV文件加载测试数据...")
        df = pd.read_csv('performance_summary.csv', encoding='utf-8-sig')
        return {
            'customMetrics': {
                'loginStats': {
                    'avg': df['登录平均时间(ms)'][0],
                    'p95': df['登录P95时间(ms)'][0],
                    'min': 0,
                    'max': 0,
                    'count': 0
                },
                'submissionStats': {
                    'avg': df['提交平均时间(ms)'][0],
                    'p95': df['提交P95时间(ms)'][0],
                    'min': 0,
                    'max': 0,
                    'count': 0
                },
                'judgeStats': {
                    'avg': df['判题平均时间(ms)'][0],
                    'p95': df['判题P95时间(ms)'][0],
                    'min': 0,
                    'max': 0,
                    'count': 0
                },
                'totalSubmissions': df['成功提交数'][0] + df['失败提交数'][0],
                'submissionSuccessRate': df['成功率(%)'][0],
                'sessionSuccessRate': df['会话成功率(%)'][0]
            }
        }
    else:
        print("未找到测试数据文件")
        return None


def generate_text_report(data):
    """生成文本格式的性能报告"""
    metrics = data['customMetrics']
    
    # 创建报告目录
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f"report_results_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    
    report_file = f"{output_dir}/detailed_report.txt"
    
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write("OJ系统性能测试详细报告\n")
        f.write("=" * 60 + "\n\n")
        
        f.write(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        f.write("1. 测试概述\n")
        f.write("-" * 30 + "\n")
        f.write(f"总提交数: {metrics['totalSubmissions']}\n")
        f.write(f"提交成功率: {metrics['submissionSuccessRate']:.2f}%\n")
        f.write(f"会话成功率: {metrics['sessionSuccessRate']:.2f}%\n\n")
        
        f.write("2. 响应时间指标 (ms)\n")
        f.write("-" * 30 + "\n")
        f.write(f"{'操作类型':<15} {'平均时间':<10} {'P95时间':<10}\n")
        f.write("-" * 30 + "\n")
        f.write(f"{'登录':<15} {metrics['loginStats']['avg']:<10.0f} {metrics['loginStats']['p95']:<10.0f}\n")
        f.write(f"{'提交':<15} {metrics['submissionStats']['avg']:<10.0f} {metrics['submissionStats']['p95']:<10.0f}\n")
        f.write(f"{'判题':<15} {metrics['judgeStats']['avg']:<10.0f} {metrics['judgeStats']['p95']:<10.0f}\n\n")
        
        f.write("3. 性能分析\n")
        f.write("-" * 30 + "\n")
        f.write("系统在50用户负载下的表现分析:\n")
        f.write(f"- 登录操作平均耗时 {metrics['loginStats']['avg']:.0f}ms，P95耗时 {metrics['loginStats']['p95']:.0f}ms\n")
        f.write(f"- 代码提交平均耗时 {metrics['submissionStats']['avg']:.0f}ms，P95耗时 {metrics['submissionStats']['p95']:.0f}ms\n")
        f.write(f"- 判题平均耗时 {metrics['judgeStats']['avg']:.0f}ms，P95耗时 {metrics['judgeStats']['p95']:.0f}ms\n\n")
        
        f.write("4. 系统稳定性评估\n")
        f.write("-" * 30 + "\n")
        if metrics['submissionSuccessRate'] >= 90:
            f.write("✅ 系统稳定性良好，提交成功率达到 {metrics['submissionSuccessRate']:.2f}%\n")
        elif metrics['submissionSuccessRate'] >= 80:
            f.write("⚠️ 系统稳定性一般，提交成功率为 {metrics['submissionSuccessRate']:.2f}%\n")
        else:
            f.write("❌ 系统稳定性较差，提交成功率仅为 {metrics['submissionSuccessRate']:.2f}%\n")
        
        # 分析瓶颈
        f.write("\n5. 性能瓶颈分析\n")
        f.write("-" * 30 + "\n")
        
        # 找出耗时最长的操作
        avg_times = {
            '登录': metrics['loginStats']['avg'],
            '提交': metrics['submissionStats']['avg'],
            '判题': metrics['judgeStats']['avg']
        }
        
        slowest_op = max(avg_times, key=avg_times.get)
        f.write(f"⏱️  耗时最长的操作: {slowest_op}，平均耗时 {avg_times[slowest_op]:.0f}ms\n")
        f.write("\n")
        
        f.write("6. 优化建议\n")
        f.write("-" * 30 + "\n")
        f.write("1. 重点优化{slowest_op}操作，考虑使用缓存或异步处理\n")
        f.write("2. 监控系统资源使用情况，特别是CPU和内存\n")
        f.write("3. 考虑增加服务器实例或使用负载均衡\n")
        f.write("4. 对高频访问的API进行性能优化\n")
        f.write("5. 定期进行性能测试，监控系统性能变化\n")
        
        f.write("\n" + "=" * 60 + "\n")
        f.write("报告生成完成\n")
        f.write("=" * 60 + "\n")
    
    print(f"✅ 详细报告已生成: {report_file}")
    
    # 同时打印一份简要报告到控制台
    print("\n" + "=" * 60)
    print("OJ系统性能测试简要报告")
    print("=" * 60)
    print(f"总提交数: {metrics['totalSubmissions']}")
    print(f"提交成功率: {metrics['submissionSuccessRate']:.2f}%")
    print(f"会话成功率: {metrics['sessionSuccessRate']:.2f}%")
    print("-" * 30)
    print(f"登录平均时间: {metrics['loginStats']['avg']:.0f}ms")
    print(f"提交平均时间: {metrics['submissionStats']['avg']:.0f}ms")
    print(f"判题平均时间: {metrics['judgeStats']['avg']:.0f}ms")
    print("-" * 30)
    print(f"耗时最长的操作: {slowest_op}")
    print("=" * 60)
    
    return output_dir


def generate_csv_report(data):
    """生成CSV格式的详细数据报告"""
    metrics = data['customMetrics']
    
    # 创建报告目录
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f"report_results_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    
    # 准备详细数据
    detailed_data = {
        '指标类型': ['登录', '提交', '判题'],
        '平均时间(ms)': [
            metrics['loginStats']['avg'],
            metrics['submissionStats']['avg'],
            metrics['judgeStats']['avg']
        ],
        'P95时间(ms)': [
            metrics['loginStats']['p95'],
            metrics['submissionStats']['p95'],
            metrics['judgeStats']['p95']
        ],
        '最小值(ms)': [
            metrics['loginStats']['min'],
            metrics['submissionStats']['min'],
            metrics['judgeStats']['min']
        ],
        '最大值(ms)': [
            metrics['loginStats']['max'],
            metrics['submissionStats']['max'],
            metrics['judgeStats']['max']
        ]
    }
    
    df = pd.DataFrame(detailed_data)
    csv_file = f"{output_dir}/detailed_performance_data.csv"
    df.to_csv(csv_file, index=False, encoding='utf-8-sig')
    
    print(f"✅ 详细数据报告已生成: {csv_file}")
    return output_dir


def main():
    """主函数"""
    print("开始生成性能测试报告...")
    
    # 加载测试数据
    data = load_performance_data()
    if not data:
        print("无法加载测试数据，程序退出")
        return
    
    # 生成详细报告
    output_dir = generate_text_report(data)
    
    # 生成CSV数据报告
    generate_csv_report(data)
    
    print(f"\n✅ 所有报告生成完成!")
    print(f"结果保存在目录: {output_dir}")


if __name__ == "__main__":
    main()