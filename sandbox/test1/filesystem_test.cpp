#include <iostream>
#include <fstream>

using namespace std;

int main() {
    // 尝试写入文件
    ofstream outfile("test.txt");
    if(outfile.is_open()) {
        outfile << "This is a test file" << endl;
        outfile.close();
        cout << "File written successfully" << endl;
    } else {
        cerr << "Failed to open file" << endl;
        return 1;
    }
    
    return 0;
}