import os
import json
import glob
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# --- 全局配置 ---
sns.set(style="whitegrid", context="paper", font='SimHei')
plt.rcParams['font.sans-serif'] = ['SimHei', 'Arial Unicode MS', 'Heiti TC', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False
plt.rcParams['figure.dpi'] = 300
plt.rcParams['savefig.dpi'] = 300
plt.rcParams['pdf.fonttype'] = 42

def extract_metrics_from_custom_json(file_path, folder_name):
    """解析 k6OriginalMetrics 结构的 JSON"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        if 'k6OriginalMetrics' not in data:
            return None

        k6_data = data['k6OriginalMetrics']
        metrics = k6_data.get('metrics', {})

        if not metrics:
            return None

        # 1. 并发数
        vus_max = 0
        if 'vus_max' in metrics:
            vals = metrics['vus_max'].get('values', {})
            vus_max = vals.get('value', vals.get('max', 0))
        if vus_max == 0:
            vus_max = int(folder_name)

        # 2. TPS
        tps = 0.0
        if 'http_reqs' in metrics:
            tps = metrics['http_reqs'].get('values', {}).get('rate', 0.0)

        # 3. 响应时间 (ms -> s)
        avg_rt = 0.0
        p95_rt = 0.0
        if 'http_req_duration' in metrics:
            vals = metrics['http_req_duration'].get('values', {})
            avg_rt = (vals.get('avg') or 0) / 1000.0
            p95_rt = (vals.get('p(95)') or 0) / 1000.0

        # 4. 错误率
        error_rate = 0.0
        if 'http_req_failed' in metrics:
            rate = metrics['http_req_failed'].get('values', {}).get('rate', 0.0)
            error_rate = rate * 100.0

        return {
            'concurrency': int(vus_max),
            'tps': tps,
            'avg_rt': avg_rt,
            'p95_rt': p95_rt,
            'error_rate': error_rate,
            'file_name': os.path.basename(file_path)
        }
    except Exception as e:
        return None

def main():
    current_dir = os.getcwd()
    final_data = []
    # 用于箱线图的原始数据列表：每个元素是 {'concurrency': 100, 'rt': 2.5}
    boxplot_data = []

    print("🔍 [专业绘图版 + 箱线图] 正在解析数据...")
    folders = [f for f in os.listdir(current_dir) if os.path.isdir(os.path.join(current_dir, f)) and f.isdigit()]
    if not folders:
        print("❌ 未找到数字命名的文件夹。")
        return
    folders.sort(key=lambda x: int(x))

    for folder_name in folders:
        folder_path = os.path.join(current_dir, folder_name)
        json_files = glob.glob(os.path.join(folder_path, '*.json'))
        if not json_files: continue

        batch_results = []
        for f_path in json_files:
            res = extract_metrics_from_custom_json(f_path, folder_name)
            if res:
                batch_results.append(res)
                # 收集单次测试的响应时间用于箱线图
                # 注意：这里我们使用每个JSON文件中的 avg_rt 作为一个样本点
                # 如果你的JSON里有更细粒度的每次请求数据，可以在这里展开循环
                boxplot_data.append({'concurrency': res['concurrency'], 'rt': res['avg_rt']})

        if batch_results:
            df_batch = pd.DataFrame(batch_results)
            summary = {
                'concurrency': int(folder_name),
                'samples': len(batch_results),
                'avg_tps': df_batch['tps'].mean(),
                'std_tps': df_batch['tps'].std(),
                'avg_rt': df_batch['avg_rt'].mean(),
                'std_rt': df_batch['avg_rt'].std(),
                'p95_rt': df_batch['p95_rt'].mean(),
                'error_rate': df_batch['error_rate'].mean()
            }
            final_data.append(summary)
            print(f"✅ {folder_name} VUs: TPS={summary['avg_tps']:.2f}, P95={summary['p95_rt']:.2f}s")

    if not final_data:
        print("❌ 无有效数据。")
        return

    df_final = pd.DataFrame(final_data)

    # ================= 1. 生成中文横向表格 =================
    column_mapping = {
        'concurrency': '并发数 (VUs)',
        'samples': '测试次数',
        'avg_tps': '平均吞吐量 (req/s)',
        'std_tps': '吞吐量标准差',
        'avg_rt': '平均响应时间 (s)',
        'std_rt': '响应时间标准差',
        'p95_rt': 'P95 响应时间 (s)',
        'error_rate': '错误率 (%)'
    }

    df_cn = df_final.rename(columns=column_mapping)
    df_cn = df_cn.set_index('并发数 (VUs)').T

    csv_path = 'performance_summary_cn_transpose.csv'
    df_cn.to_csv(csv_path, float_format='%.4f', encoding='utf-8-sig')
    print(f"💾 中文横向表格已保存: {csv_path}")

    # ================= 1.1 生成中文纵向表格 =================
    df_cn_vertical = df_final.rename(columns=column_mapping)
    csv_path_vertical = 'performance_summary_cn_vertical.csv'
    df_cn_vertical.to_csv(csv_path_vertical, float_format='%.4f', index=False, encoding='utf-8-sig')
    print(f"💾 中文纵向表格已保存: {csv_path_vertical}")

    # ================= 2. 生成趋势图 (Trend Chart) =================
    print("📈 正在绘制趋势图...")
    fig1, ax1 = plt.subplots(figsize=(12, 7))

    x = df_final['concurrency']
    color_tps = '#2E86AB'
    ax1.set_xlabel('并发用户数 (Virtual Users)', fontsize=14, fontweight='bold', labelpad=10)
    ax1.set_ylabel('吞吐量 (Requests / sec)', color=color_tps, fontsize=13, fontweight='bold')

    yerr = df_final['std_tps']
    has_std = not (yerr.isnull().all() or (yerr == 0).all())

    if has_std:
        ax1.errorbar(x, df_final['avg_tps'], yerr=yerr, fmt='-o', color=color_tps,
                     linewidth=2.5, markersize=8, capsize=6, markerfacecolor='white',
                     markeredgewidth=2, label='吞吐量 (TPS)', zorder=5)
        ax1.fill_between(x, df_final['avg_tps'] - yerr, df_final['avg_tps'] + yerr,
                         color=color_tps, alpha=0.15, label='TPS 波动范围')
    else:
        ax1.plot(x, df_final['avg_tps'], color=color_tps, marker='o', linewidth=2.5,
                 markersize=8, markerfacecolor='white', markeredgewidth=2, label='吞吐量 (TPS)', zorder=5)

    ax1.tick_params(axis='y', labelcolor=color_tps, labelsize=11)
    ax1.grid(True, linestyle='--', alpha=0.4, color='#cccccc', zorder=0)

    max_tps = df_final['avg_tps'].max()
    ax1.set_ylim(0, max_tps * 1.25)

    for i, v in enumerate(df_final['avg_tps']):
        offset = yerr[i] if has_std else 0
        ax1.text(x[i], v + offset + (max_tps*0.02), f'{v:.1f}',
                 color=color_tps, ha='center', fontsize=10, fontweight='bold')

    # --- 右轴：响应时间 ---
    ax2 = ax1.twinx()
    color_rt = '#D64550'
    color_p95 = '#F18F01'

    ax2.set_ylabel('响应时间 (Seconds)', color=color_rt, fontsize=13, fontweight='bold')
    ax2.tick_params(axis='y', labelcolor=color_rt, labelsize=11)

    ax2.plot(x, df_final['avg_rt'], color=color_rt, marker='s',
                        linestyle='--', linewidth=2, markersize=7, label='平均响应时间 (Avg)', zorder=4)
    ax2.plot(x, df_final['p95_rt'], color=color_p95, marker='^',
                        linestyle='-.', linewidth=2, markersize=7, label='P95 响应时间', zorder=4)

    max_rt = df_final['p95_rt'].max()
    ax2.set_ylim(0, max_rt * 1.3)

    # --- 标注错误率和拐点 ---
    turning_point_found = False
    for i, row in df_final.iterrows():
        xc = row['concurrency']
        yc = row['avg_tps']
        err = row['error_rate']

        if err > 0.5:
            y_text_pos = yc - (max_tps * 0.15) if yc > max_tps * 0.6 else yc + (max_tps * 0.05)
            ax1.text(xc, y_text_pos, f'Err: {err:.1f}%',
                     color='darkred', ha='center', fontsize=9, fontweight='bold',
                     bbox=dict(facecolor='#fff0f0', edgecolor='red', boxstyle='round,pad=0.3', alpha=0.8),
                     zorder=10)

            if not turning_point_found and err > 1.0:
                turning_point_found = True
                ax1.axvline(x=xc, color='gray', linestyle=':', linewidth=2, alpha=0.6, zorder=1)
                ax1.text(xc, max_tps * 1.15, '性能拐点', color='black',
                         ha='center', fontsize=11, fontweight='extra bold',
                         bbox=dict(facecolor='yellow', edgecolor='orange', boxstyle='round,pad=0.4'),
                         zorder=10)

    lines_1, labels_1 = ax1.get_legend_handles_labels()
    lines_2, labels_2 = ax2.get_legend_handles_labels()
    all_lines = lines_1 + lines_2
    all_labels = labels_1 + labels_2

    fig1.legend(all_lines, all_labels, loc='lower center', ncol=4,
               fontsize=11, frameon=True, shadow=True, bbox_to_anchor=(0.5, -0.08))

    plt.title('系统性能负载测试趋势分析 (多轮测试平均值)', fontsize=16, fontweight='bold', pad=20)
    plt.tight_layout(rect=[0, 0.05, 1, 1])

    img_path_trend = 'performance_analysis_pro.png'
    plt.savefig(img_path_trend, dpi=300, bbox_inches='tight')
    print(f"📈 趋势图已保存: {img_path_trend}")
    plt.close(fig1) # 释放内存

    # ================= 3. 生成响应时间箱线图 (Boxplot) =================
    print("📦 正在绘制响应时间分布箱线图...")
    fig2, ax = plt.subplots(figsize=(10, 6))

    df_box = pd.DataFrame(boxplot_data)

    # 定义颜色映射字典：将并发数映射到具体颜色
    # 确保这里的键 (100, 150, 200) 与你数据中的 concurrency 值一致且顺序对应
    unique_conc = sorted(df_box['concurrency'].unique())
    custom_palette = {conc: color for conc, color in zip(unique_conc, ['#4CAF50', '#FF9800', '#F44336', '#9C27B0', '#00BCD4', '#FFEB3B', '#795548', '#607D8B'])}

    # 【修改点】：添加 hue='concurrency'，并设置 legend=False 以避免自动生成多余图例
    sns.boxplot(x='concurrency', y='rt', data=df_box, ax=ax,
                hue='concurrency', palette=custom_palette,
                linewidth=1.5, legend=False)

    # 添加抖动散点图 (Stripplot) - 同样需要处理 hue 以避免警告
    # 注意：stripplot 不需要 palette，我们直接用 color 或者让它继承 boxplot 的颜色逻辑
    # 为了简单且避免警告，这里我们只画黑色的点，或者手动循环画
    sns.stripplot(x='concurrency', y='rt', data=df_box, ax=ax,
                  color='black', size=4, alpha=0.6, jitter=True)

    ax.set_xlabel('并发用户数 (Virtual Users)', fontsize=14, fontweight='bold')
    ax.set_ylabel('单次测试平均响应时间 (Seconds)', fontsize=14, fontweight='bold')
    ax.set_title('不同负载下响应时间分布与长尾效应分析', fontsize=16, fontweight='bold', pad=15)
    ax.grid(axis='y', linestyle='--', alpha=0.3)

    # 移除默认的 hue 标签 (有时会自动生成一个 title)
    ax.get_legend().remove() if ax.get_legend() else None

    # 添加均值标记线
    means = df_box.groupby('concurrency')['rt'].mean()
    for i, conc in enumerate(means.index):
        mean_val = means[conc]
        # 在箱线图上方画一个红色的菱形表示均值
        ax.plot(i, mean_val, 'D', color='darkred', markersize=10, label='均值' if i==0 else "")
        ax.text(i, mean_val + 0.2, f'{mean_val:.2f}s', ha='center', color='darkred', fontweight='bold')

    # 自定义图例 (完全手动控制，不受 seaborn 内部逻辑影响)
    from matplotlib.lines import Line2D
    legend_elements = [
        Line2D([0], [0], marker='D', color='w', markerfacecolor='darkred', markersize=10, label='平均响应时间'),
        Line2D([0], [0], marker='o', color='w', markerfacecolor='gray', markersize=6, alpha=0.6, label='单次测试样本'),
        Line2D([0], [0], color='#4CAF50', lw=2, label='四分位距 (低负载)'),
        Line2D([0], [0], color='#F44336', lw=2, label='四分位距 (高负载)')
    ]
    ax.legend(handles=legend_elements, loc='upper left', fontsize=10, frameon=True, shadow=True)

    plt.tight_layout()
    img_path_box = 'response_time_boxplot.png'
    plt.savefig(img_path_box, dpi=300, bbox_inches='tight')
    print(f"📦 箱线图已保存: {img_path_box}")
    plt.close(fig2)

    print("\n🎉 所有任务完成！请查看生成的文件：")
    print(f"   1. 横向表格: {csv_path}")
    print(f"   2. 纵向表格: {csv_path_vertical}")
    print(f"   3. 趋势图: {img_path_trend}")
    print(f"   4. 箱线图: {img_path_box}")

if __name__ == '__main__':
    main()