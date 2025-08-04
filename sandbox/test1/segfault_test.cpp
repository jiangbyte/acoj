#include <iostream>

using namespace std;

int main() {
    // 故意造成段错误
    int* ptr = nullptr;
    *ptr = 42;
    
    cout << "This line should not be reached" << endl;
    return 0;
}