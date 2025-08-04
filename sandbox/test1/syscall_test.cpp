#include <iostream>
#include <cstdlib>

using namespace std;

int main() {
    // 尝试执行系统命令
    int result = system("ls -l");
    cout << "System command returned: " << result << endl;
    
    return 0;
}