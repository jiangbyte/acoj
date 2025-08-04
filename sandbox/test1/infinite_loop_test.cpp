#include <iostream>

using namespace std;

int main() {
    // 无限循环
    while(true) {
        // 做一些工作避免被优化掉
        static int counter = 0;
        counter++;
    }
    
    return 0;
}