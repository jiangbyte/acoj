# 简化版本 - 直接使用
import os
from pathlib import Path

def export_simple_directory(source_dir, output_file="output.txt"):
    """简化版的目录导出函数"""

    def get_tree(dir_path, prefix=""):
        tree = ""
        items = sorted(os.listdir(dir_path))
        for i, item in enumerate(items):
            full_path = os.path.join(dir_path, item)
            is_last = i == len(items) - 1
            connector = "└── " if is_last else "├── "

            tree += prefix + connector + item + "\n"

            if os.path.isdir(full_path):
                extension = "    " if is_last else "│   "
                tree += get_tree(full_path, prefix + extension)
        return tree

    with open(output_file, 'w', encoding='utf-8') as out_f:
        # 写入目录结构
        out_f.write("目录结构:\n")
        out_f.write(".\n")
        out_f.write(get_tree(source_dir))
        out_f.write("\n" + "="*50 + "\n\n")

        # 写入文件内容
        for root, dirs, files in os.walk(source_dir):
            for file in files:
                file_path = os.path.join(root, file)
                try:
                    # 跳过大文件和非文本文件
                    if os.path.getsize(file_path) > 1024*1024:
                        continue

                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()

                    relative_path = os.path.relpath(file_path, source_dir)
                    out_f.write(f"\n文件: {relative_path}\n")
                    out_f.write("-"*50 + "\n")
                    out_f.write(content + "\n")

                except:
                    continue

# 使用示例
if __name__ == "__main__":
    export_simple_directory("./internal", "simple_export.txt")
    print("导出完成!")