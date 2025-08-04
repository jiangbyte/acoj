#include <iostream>
#include <fstream>
#include <string>

using namespace std;

int main() {
    // 读取输入
    string input;
    getline(cin, input);
    
    // 处理并输出
    cout << "Processed: " << input << endl;
    cerr << "This is an error message" << endl;
    
    return 0;
}