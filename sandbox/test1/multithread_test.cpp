#include <iostream>
#include <thread>
#include <vector>

using namespace std;

void worker(int id) {
    cout << "Thread " << id << " working" << endl;
}

int main() {
    vector<thread> threads;
    
    // 创建多个线程
    for(int i = 0; i < 5; ++i) {
        threads.emplace_back(worker, i);
    }
    
    // 等待所有线程完成
    for(auto& t : threads) {
        t.join();
    }
    
    cout << "All threads completed" << endl;
    return 0;
}