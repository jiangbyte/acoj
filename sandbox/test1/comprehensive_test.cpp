#include <iostream>
#include <vector>
#include <thread>
#include <chrono>
#include <fstream>

using namespace std;

int main() {
    // 1. 读取输入
    string input;
    getline(cin, input);
    cout << "Input: " << input << endl;
    
    // 2. 内存测试
    vector<int> memory_test(1000000, 42); // 分配约4MB内存
    cout << "Memory test passed" << endl;
    
    // 3. 时间测试
    this_thread::sleep_for(chrono::milliseconds(500));
    cout << "Time test passed" << endl;
    
    // 4. 多线程测试
    thread t([](){
        cout << "Thread running" << endl;
    });
    t.join();
    cout << "Thread test passed" << endl;
    
    // 5. 文件系统测试 (应该被沙箱阻止)
    ofstream file("test.txt");
    if(file) {
        file << "Test content" << endl;
        file.close();
        cout << "File test passed (but should have been blocked)" << endl;
    } else {
        cout << "File test correctly blocked by sandbox" << endl;
    }
    
    return 0;
}