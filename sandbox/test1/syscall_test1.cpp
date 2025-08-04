#include <iostream>
#include <fstream>
#include <unistd.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <cstring>

// 测试基本的文件操作
void test_file_operations() {
    std::cout << "=== 测试文件操作 ===" << std::endl;
    
    // 创建并写入文件
    std::ofstream outfile("testfile.txt");
    if (outfile.is_open()) {
        outfile << "这是一些测试内容\n";
        outfile.close();
        std::cout << "文件写入成功" << std::endl;
    } else {
        std::cerr << "无法打开文件进行写入" << std::endl;
    }

    // 读取文件
    std::ifstream infile("testfile.txt");
    if (infile.is_open()) {
        std::string line;
        while (std::getline(infile, line)) {
            std::cout << "读取内容: " << line << std::endl;
        }
        infile.close();
    } else {
        std::cerr << "无法打开文件进行读取" << std::endl;
    }

    // 删除文件
    if (remove("testfile.txt") == 0) {
        std::cout << "文件删除成功" << std::endl;
    } else {
        std::cerr << "文件删除失败" << std::endl;
    }
}

// 测试进程创建
void test_process_creation() {
    std::cout << "\n=== 测试进程创建 ===" << std::endl;
    
    pid_t pid = fork();
    if (pid == 0) {
        // 子进程
        std::cout << "这是子进程 (PID: " << getpid() << ")" << std::endl;
        sleep(1);
        _exit(0);
    } else if (pid > 0) {
        // 父进程
        std::cout << "这是父进程 (PID: " << getpid() << "), 创建了子进程 " << pid << std::endl;
        wait(nullptr);
        std::cout << "子进程已退出" << std::endl;
    } else {
        std::cerr << "fork() 失败" << std::endl;
    }
}

// 测试系统资源限制
void test_resource_limits() {
    std::cout << "\n=== 测试资源限制 ===" << std::endl;
    
    // 获取并显示当前限制
    struct rlimit limit;
    
    getrlimit(RLIMIT_STACK, &limit);
    std::cout << "栈大小限制: " << limit.rlim_cur << " (当前), " << limit.rlim_max << " (最大)" << std::endl;
    
    getrlimit(RLIMIT_NOFILE, &limit);
    std::cout << "文件描述符限制: " << limit.rlim_cur << " (当前), " << limit.rlim_max << " (最大)" << std::endl;
    
    getrlimit(RLIMIT_AS, &limit);
    std::cout << "虚拟内存限制: " << limit.rlim_cur << " (当前), " << limit.rlim_max << " (最大)" << std::endl;
}

// 测试受限的系统调用
void test_restricted_syscalls() {
    std::cout << "\n=== 测试受限系统调用 ===" << std::endl;
    
    // 尝试执行新程序
    std::cout << "尝试执行 ls 命令..." << std::endl;
    if (execl("/bin/ls", "ls", "-l", nullptr) == -1) {
        perror("execl 失败");
    }
    
    // 尝试改变工作目录
    std::cout << "尝试改变工作目录..." << std::endl;
    if (chdir("/tmp") == -1) {
        perror("chdir 失败");
    } else {
        std::cout << "当前目录: ";
        system("pwd");
    }
}

// 测试内存分配
void test_memory_allocation() {
    std::cout << "\n=== 测试内存分配 ===" << std::endl;
    
    const size_t size = 1024 * 1024; // 1MB
    char* memory = new char[size];
    
    if (memory) {
        std::cout << "成功分配 " << size << " 字节内存" << std::endl;
        
        // 使用内存
        memset(memory, 'A', size);
        std::cout << "内存使用测试完成" << std::endl;
        
        delete[] memory;
    } else {
        std::cerr << "内存分配失败" << std::endl;
    }
}

// 测试CPU时间消耗
void test_cpu_usage() {
    std::cout << "\n=== 测试CPU使用 ===" << std::endl;
    
    std::cout << "开始CPU密集型计算..." << std::endl;
    volatile double sum = 0;
    for (long i = 0; i < 100000000; i++) {
        sum += i * 0.1;
    }
    std::cout << "计算完成，结果: " << sum << std::endl;
}

int main() {
    std::cout << "=== 系统调用测试程序开始 ===" << std::endl;
    
    // 测试基本文件操作
    test_file_operations();
    
    // 测试进程创建
    test_process_creation();
    
    // 测试资源限制
    test_resource_limits();
    
    // 测试内存分配
    test_memory_allocation();
    
    // 测试CPU使用
    test_cpu_usage();
    
    // 测试受限系统调用（放在最后，因为可能会终止程序）
    test_restricted_syscalls();
    
    std::cout << "\n=== 所有测试完成 ===" << std::endl;
    return 0;
}