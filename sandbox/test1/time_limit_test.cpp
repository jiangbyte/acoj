#include <iostream>
#include <chrono>
#include <thread>

using namespace std;

int main() {
    // 模拟长时间运行 (10秒)
    this_thread::sleep_for(chrono::seconds(10));
    
    cout << "Finished after delay" << endl;
    return 0;
}