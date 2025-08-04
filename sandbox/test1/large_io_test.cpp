#include <iostream>
#include <string>

using namespace std;

int main() {
    // 生成大量输出
    for(int i = 0; i < 100000; ++i) {
        cout << "Line " << i << ": This is a test line to generate large output" << endl;
    }
    
    return 0;
}