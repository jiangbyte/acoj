#include <iostream>
#include <fstream>
#include <unistd.h>
#include <sys/resource.h>
#include <sys/time.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <cstring>
#include <sys/mman.h>
#include <sys/socket.h>

// 测试允许的系统调用
void test_allowed_syscalls() {
    std::cout << "\n=== 测试允许的系统调用 ===" << std::endl;
    
    // 1. 文件操作 (read/write/close)
    std::cout << "测试基本文件操作..." << std::endl;
    int fd = open("testfile.txt", O_CREAT | O_RDWR, 0644);
    if (fd >= 0) {
        write(fd, "hello", 5);
        lseek(fd, 0, SEEK_SET);
        
        char buf[6] = {0};
        read(fd, buf, 5);
        std::cout << "读取内容: " << buf << std::endl;
        
        close(fd);
        unlink("testfile.txt");
    } else {
        perror("文件操作被阻止");
    }

    // 2. 内存操作 (mmap/munmap)
    std::cout << "测试内存映射..." << std::endl;
    void* addr = mmap(nullptr, 4096, PROT_READ|PROT_WRITE, 
                     MAP_PRIVATE|MAP_ANONYMOUS, -1, 0);
    if (addr != MAP_FAILED) {
        std::cout << "内存映射成功" << std::endl;
        munmap(addr, 4096);
    } else {
        perror("内存映射被阻止");
    }

    // 移除了arch_prctl测试
    std::cout << "跳过arch_prctl测试 (ARCH_GET_FS未定义)" << std::endl;
}

// 测试被禁止的系统调用
void test_restricted_syscalls() {
    std::cout << "\n=== 测试被禁止的系统调用 ===" << std::endl;
    
    // 1. 进程创建 (fork)
    std::cout << "尝试创建进程..." << std::endl;
    pid_t pid = fork();
    if (pid == 0) {
        std::cout << "子进程运行中 (这不应该发生)" << std::endl;
        _exit(0);
    } else if (pid > 0) {
        wait(nullptr);
        std::cout << "fork() 被意外允许!" << std::endl;
    } else {
        perror("fork() 被阻止 (符合预期)");
    }

    // 2. 执行程序 (execve)
    std::cout << "尝试执行新程序..." << std::endl;
    if (execl("/bin/ls", "ls", nullptr) == -1) {
        perror("execve 被阻止 (符合预期)");
    }

    // 3. 网络操作 (socket)
    std::cout << "尝试创建socket..." << std::endl;
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock >= 0) {
        std::cout << "socket() 被意外允许!" << std::endl;
        close(sock);
    } else {
        perror("socket 被阻止 (符合预期)");
    }

    // 4. 权限操作 (setuid)
    std::cout << "尝试改变UID..." << std::endl;
    if (setuid(0) == 0) {
        std::cout << "setuid() 被意外允许!" << std::endl;
    } else {
        perror("setuid 被阻止 (符合预期)");
    }
}

// 测试其他可能允许的系统调用
void test_other_syscalls() {
    std::cout << "\n=== 测试其他系统调用 ===" << std::endl;
    
    // 1. 获取时间 (gettimeofday)
    std::cout << "尝试获取时间..." << std::endl;
    struct timeval tv;
    if (gettimeofday(&tv, nullptr) == 0) {
        std::cout << "当前时间: " << tv.tv_sec << "秒" << std::endl;
    } else {
        perror("gettimeofday 被阻止");
    }

    // 2. 内存分配 (brk)
    std::cout << "测试brk..." << std::endl;
    void* old_brk = sbrk(0);
    if (brk((char*)old_brk + 4096) == 0) {
        std::cout << "brk 操作成功" << std::endl;
        brk(old_brk); // 恢复
    } else {
        perror("brk 被阻止");
    }
}

int main() {
    std::cout << "=== Seccomp 'strict' 规则测试程序 ===" << std::endl;
    
    test_allowed_syscalls();      // 测试应该允许的系统调用
    test_other_syscalls();        // 测试可能允许的系统调用
    test_restricted_syscalls();   // 测试应该被阻止的系统调用
    
    std::cout << "\n=== 测试完成 ===" << std::endl;
    return 0;
}