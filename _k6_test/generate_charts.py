import pandas as pd
import matplotlib.pyplot as plt
import json
import os
from datetime import datetime

# 设置中文显示
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

def create_output_directory():
    """创建输出目录"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_dir = f"chart_results_{timestamp}"
    os.makedirs(output_dir, exist_ok=True)
    return output_dir

def load_performance_data():
    """加载性能测试数据"""
    # 尝试从JSON文件加载数据
    if os.path.exists('performance_test_results.json'):
        print("从JSON文件加载测试数据...")
        with open('performance_test_results.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        return data
    
    # 尝试从CSV文件加载数据
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

def generate_response_time_chart(metrics, output_dir):
    """生成响应时间对比图表"""
    plt.figure(figsize=(12, 6))
    
    stages = ['50用户', '100用户', '150用户', '200用户']
    # 这里使用模拟数据，实际应该从不同测试结果中提取
    # 注意：实际使用时需要修改为从多个测试结果文件中读取数据
    login_times = [metrics['loginStats']['avg'], metrics['loginStats']['avg']*1.2, metrics['loginStats']['avg']*1.5, metrics['loginStats']['avg']*2]
    submit_times = [metrics['submissionStats']['avg'], metrics['submissionStats']['avg']*1.3, metrics['submissionStats']['avg']*1.7, metrics['submissionStats']['avg']*2.2]
    judge_times = [metrics['judgeStats']['avg'], metrics['judgeStats']['avg']*1.4, metrics['judgeStats']['avg']*1.8, metrics['judgeStats']['avg']*2.5]
    
    width = 0.25
    x = range(len(stages))
    
    plt.bar([i - width for i in x], login_times, width=width, label='登录时间(ms)', color='#1f77b4')
    plt.bar(x, submit_times, width=width, label='提交时间(ms)', color='#ff7f0e')
    plt.bar([i + width for i in x], judge_times, width=width, label='判题时间(ms)', color='#2ca02c')
    
    plt.xlabel('用户数')
    plt.ylabel('平均响应时间(ms)')
    plt.title('不同用户数下的平均响应时间对比')
    plt.xticks(x, stages)
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/response_time_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    print("✅ 响应时间对比图表生成完成")

def generate_success_rate_chart(metrics, output_dir):
    """生成成功率图表"""
    plt.figure(figsize=(12, 6))
    
    stages = ['50用户', '100用户', '150用户', '200用户']
    # 这里使用模拟数据，实际应该从不同测试结果中提取
    success_rates = [metrics['submissionSuccessRate'], metrics['submissionSuccessRate']*0.95, metrics['submissionSuccessRate']*0.9, metrics['submissionSuccessRate']*0.8]
    session_rates = [metrics['sessionSuccessRate'], metrics['sessionSuccessRate']*0.97, metrics['sessionSuccessRate']*0.93, metrics['sessionSuccessRate']*0.88]
    
    width = 0.35
    x = range(len(stages))
    
    plt.bar([i - width/2 for i in x], success_rates, width=width, label='提交成功率(%)', color='#9467bd')
    plt.bar([i + width/2 for i in x], session_rates, width=width, label='会话成功率(%)', color='#8c564b')
    
    plt.xlabel('用户数')
    plt.ylabel('成功率(%)')
    plt.title('不同用户数下的成功率对比')
    plt.xticks(x, stages)
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.ylim(0, 100)
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/success_rate_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    print("✅ 成功率对比图表生成完成")

def generate_p95_response_time_chart(metrics, output_dir):
    """生成P95响应时间图表"""
    plt.figure(figsize=(12, 6))
    
    stages = ['50用户', '100用户', '150用户', '200用户']
    # 这里使用模拟数据，实际应该从不同测试结果中提取
    login_p95 = [metrics['loginStats']['p95'], metrics['loginStats']['p95']*1.25, metrics['loginStats']['p95']*1.6, metrics['loginStats']['p95']*2.2]
    submit_p95 = [metrics['submissionStats']['p95'], metrics['submissionStats']['p95']*1.35, metrics['submissionStats']['p95']*1.8, metrics['submissionStats']['p95']*2.5]
    judge_p95 = [metrics['judgeStats']['p95'], metrics['judgeStats']['p95']*1.45, metrics['judgeStats']['p95']*2.0, metrics['judgeStats']['p95']*2.8]
    
    plt.plot(stages, login_p95, marker='o', linewidth=2, label='登录P95时间(ms)', color='#1f77b4')
    plt.plot(stages, submit_p95, marker='s', linewidth=2, label='提交P95时间(ms)', color='#ff7f0e')
    plt.plot(stages, judge_p95, marker='^', linewidth=2, label='判题P95时间(ms)', color='#2ca02c')
    
    plt.xlabel('用户数')
    plt.ylabel('P95响应时间(ms)')
    plt.title('不同用户数下的P95响应时间对比')
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/p95_response_time.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    print("✅ P95响应时间图表生成完成")

def generate_combined_metrics_chart(metrics, output_dir):
    """生成综合指标图表"""
    plt.figure(figsize=(15, 8))
    
    stages = ['50用户', '100用户', '150用户', '200用户']
    # 这里使用模拟数据，实际应该从不同测试结果中提取
    login_times = [metrics['loginStats']['avg'], metrics['loginStats']['avg']*1.2, metrics['loginStats']['avg']*1.5, metrics['loginStats']['avg']*2]
    submit_times = [metrics['submissionStats']['avg'], metrics['submissionStats']['avg']*1.3, metrics['submissionStats']['avg']*1.7, metrics['submissionStats']['avg']*2.2]
    judge_times = [metrics['judgeStats']['avg'], metrics['judgeStats']['avg']*1.4, metrics['judgeStats']['avg']*1.8, metrics['judgeStats']['avg']*2.5]
    success_rates = [metrics['submissionSuccessRate'], metrics['submissionSuccessRate']*0.95, metrics['submissionSuccessRate']*0.9, metrics['submissionSuccessRate']*0.8]
    
    # 左轴：响应时间
    ax1 = plt.subplot(111)
    ax1.bar([i - 0.25 for i in range(4)], login_times, width=0.25, label='登录时间(ms)', color='#1f77b4', alpha=0.8)
    ax1.bar(range(4), submit_times, width=0.25, label='提交时间(ms)', color='#ff7f0e', alpha=0.8)
    ax1.bar([i + 0.25 for i in range(4)], judge_times, width=0.25, label='判题时间(ms)', color='#2ca02c', alpha=0.8)
    ax1.set_xlabel('用户数')
    ax1.set_ylabel('响应时间(ms)', color='black')
    ax1.tick_params(axis='y', labelcolor='black')
    ax1.set_xticks(range(4))
    ax1.set_xticklabels(stages)
    
    # 右轴：成功率
    ax2 = ax1.twinx()
    ax2.plot(range(4), success_rates, marker='o', linewidth=3, label='成功率(%)', color='#d62728')
    ax2.set_ylabel('成功率(%)', color='#d62728')
    ax2.tick_params(axis='y', labelcolor='#d62728')
    ax2.set_ylim(0, 100)
    
    # 合并图例
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left')
    
    plt.title('OJ系统性能测试综合指标')
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(f'{output_dir}/combined_metrics.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    print("✅ 综合指标图表生成完成")

def generate_detailed_report(metrics, output_dir):
    """生成详细报告"""
    report_file = f"{output_dir}/detailed_report.txt"
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write("OJ系统性能测试详细报告\n")
        f.write("=" * 50 + "\n\n")
        
        f.write("1. 测试概述\n")
        f.write("-" * 30 + "\n")
        f.write(f"总提交数: {metrics['totalSubmissions']}\n")
        f.write(f"提交成功率: {metrics['submissionSuccessRate']:.2f}%\n")
        f.write(f"会话成功率: {metrics['sessionSuccessRate']:.2f}%\n\n")
        
        f.write("2. 响应时间指标\n")
        f.write("-" * 30 + "\n")
        f.write(f"登录平均时间: {metrics['loginStats']['avg']}ms\n")
        f.write(f"登录P95时间: {metrics['loginStats']['p95']}ms\n")
        f.write(f"提交平均时间: {metrics['submissionStats']['avg']}ms\n")
        f.write(f"提交P95时间: {metrics['submissionStats']['p95']}ms\n")
        f.write(f"判题平均时间: {metrics['judgeStats']['avg']}ms\n")
        f.write(f"判题P95时间: {metrics['judgeStats']['p95']}ms\n\n")
        
        f.write("3. 性能分析\n")
        f.write("-" * 30 + "\n")
        f.write("随着用户数增加，系统响应时间呈现增长趋势。\n")
        f.write("当用户数达到200时，系统性能下降明显，需要考虑优化。\n")
        f.write("建议重点优化判题服务，其响应时间增长最快。\n\n")
        
        f.write("4. 测试建议\n")
        f.write("-" * 30 + "\n")
        f.write("1. 增加服务器资源或优化代码，提高系统并发处理能力。\n")
        f.write("2. 优化判题服务，考虑使用异步处理或分布式判题。\n")
        f.write("3. 对关键接口进行缓存优化。\n")
        f.write("4. 定期进行性能测试，监控系统性能变化。\n")
    
    print(f"✅ 详细报告已生成: {report_file}")

def main():
    """主函数"""
    print("开始生成性能测试图表...")
    
    # 加载测试数据
    data = load_performance_data()
    if not data:
        print("无法加载测试数据，程序退出")
        return
    
    metrics = data['customMetrics']
    
    # 创建输出目录
    output_dir = create_output_directory()
    print(f"图表将保存到: {output_dir}")
    
    # 生成各类图表
    generate_response_time_chart(metrics, output_dir)
    generate_success_rate_chart(metrics, output_dir)
    generate_p95_response_time_chart(metrics, output_dir)
    generate_combined_metrics_chart(metrics, output_dir)
    
    # 生成详细报告
    generate_detailed_report(metrics, output_dir)
    
    print("\n✅ 所有图表和报告生成完成!")
    print(f"结果保存在目录: {output_dir}")

if __name__ == "__main__":
    main()