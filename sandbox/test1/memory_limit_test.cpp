#include <iostream>
#include <vector>

using namespace std;

int main() {
    // 尝试分配大量内存 (1GB)
    size_t size = 1024 * 1024 * 1024; // 1GB
    vector<char> memory(size, 'A');
    
    cout << "Allocated " << size << " bytes" << endl;
    return 0;
}