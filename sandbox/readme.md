./sandbox  \
    -execArgv lss  \
    -compile  \
    -outputFilePath ./output.out   \
    -errorFilePath ./error.err












```bash
go run main.go
```
```bash
go run main.go \
-execArgv=ls \
-inputFilePath=input.in \
-outputFilePath=output.out \
-errorFilePath=error.err \
-print \
-compile
```
apt-get update && apt-get install -y pkg-config libseccomp-dev

go build -o sandbox main.go

go build -o sandbox ./cmd/sandbox

# 基础测试程序
```bash
# ==================== 基础测试程序 ====================
# 使用sandbox编译
./sandbox -compile \
          -execArgv="g++,./test1/basic_test.cpp,-o,./test1/basic_test1" \
          -outputFilePath=./test1/compile_output.txt \
          -errorFilePath=./test1/compile_error.txt

# 使用sandbox运行
./sandbox -execArgv=./test1/basic_test1 \
          -inputFilePath=test1/input.txt \
          -outputFilePath=./test1/output1.txt \
          -errorFilePath=./test1/error1.txt \
          -seccompRuleName=strict

# ==================== 时间限制测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,time_limit_test.cpp,-o,time_limit_test" \
          -outputFilePath=time_compile_output.txt \
          -errorFilePath=time_compile_error.txt

# 运行（设置2秒超时）
./sandbox -execArgv=./time_limit_test \
          -maxRealTime=2000 \
          -inputFilePath=test1/input.txt \
          -outputFilePath=time_output.txt \
          -errorFilePath=time_error.txt

# ==================== 内存限制测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,memory_limit_test.cpp,-o,memory_limit_test" \
          -outputFilePath=mem_compile_output.txt \
          -errorFilePath=mem_compile_error.txt

# 运行（限制100MB内存）
./sandbox -execArgv=./memory_limit_test \
          -maxMemory=102400 \
          -inputFilePath=test1/input.txt \
          -outputFilePath=mem_output.txt \
          -errorFilePath=mem_error.txt

# ==================== 无限循环测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,infinite_loop_test.cpp,-o,infinite_loop_test" \
          -outputFilePath=loop_compile_output.txt \
          -errorFilePath=loop_compile_error.txt

# 运行（1秒后终止）
./sandbox -execArgv=./infinite_loop_test \
          -maxRealTime=1000 \
          -inputFilePath=test1/input.txt \
          -outputFilePath=loop_output.txt \
          -errorFilePath=loop_error.txt

# ==================== 文件系统测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,filesystem_test.cpp,-o,filesystem_test" \
          -outputFilePath=fs_compile_output.txt \
          -errorFilePath=fs_compile_error.txt

# 运行
./sandbox -execArgv=./filesystem_test \
          -inputFilePath=test1/input.txt \
          -outputFilePath=fs_output.txt \
          -errorFilePath=fs_error.txt

# ==================== 系统调用测试 ====================
# 编译标准版本
./sandbox -compile \
          -execArgv="g++,syscall_test.cpp,-o,syscall_test,-std=c++11" \
          -outputFilePath=sys_compile_output.txt \
          -errorFilePath=sys_compile_error.txt

# 编译严格版本
./sandbox -compile \
          -execArgv="g++,syscall_test_strict.cpp,-o,syscall_test_strict,-std=c++11" \
          -outputFilePath=sys_strict_compile_output.txt \
          -errorFilePath=sys_strict_compile_error.txt

# 运行严格版本
./sandbox -execArgv=./syscall_test_strict \
          -inputFilePath=test1/input.txt \
          -outputFilePath=sys_output.txt \
          -errorFilePath=sys_error.txt \
          -seccompRuleName=strict

# ==================== 分段错误测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,segfault_test.cpp,-o,segfault_test" \
          -outputFilePath=seg_compile_output.txt \
          -errorFilePath=seg_compile_error.txt

# 运行
./sandbox -execArgv=./segfault_test \
          -inputFilePath=test1/input.txt \
          -outputFilePath=seg_output.txt \
          -errorFilePath=seg_error.txt

# ==================== 多线程测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,multithread_test.cpp,-o,multithread_test,-pthread" \
          -outputFilePath=thread_compile_output.txt \
          -errorFilePath=thread_compile_error.txt

# 运行
./sandbox -execArgv=./multithread_test \
          -inputFilePath=test1/input.txt \
          -outputFilePath=thread_output.txt \
          -errorFilePath=thread_error.txt

# ==================== 大输入输出测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,large_io_test.cpp,-o,large_io_test" \
          -outputFilePath=large_compile_output.txt \
          -errorFilePath=large_compile_error.txt

# 运行
./sandbox -execArgv=./large_io_test \
          -outputFilePath=large_output.txt \
          -errorFilePath=large_error.txt

# ==================== 综合测试 ====================
# 编译
./sandbox -compile \
          -execArgv="g++,comprehensive_test.cpp,-o,comprehensive_test,-pthread" \
          -outputFilePath=comp_compile_output.txt \
          -errorFilePath=comp_compile_error.txt

# 运行
./sandbox -execArgv=./comprehensive_test \
          -inputFilePath=test1/input.txt \
          -outputFilePath=comp_output.txt \
          -errorFilePath=comp_error.txt \
          -maxRealTime=3000 \
          -maxMemory=512000
```