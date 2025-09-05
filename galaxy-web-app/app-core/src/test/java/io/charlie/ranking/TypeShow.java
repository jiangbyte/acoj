package io.charlie.ranking;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 07/08/2025
 * @description 测试
 */
public class TypeShow {
    public int arraySum(int[] arr) {
        // 定义累加变量
        int total = 0;
        // 循环累加
        for (int num : arr) {
            total += num;
        }
        // 返回结果
        return total;
    }

public int calculateSum(int[] numbers) {
    int result = 0;
    for (int element : numbers) {
        result += element;
    }
    return result;
}


    public int arraySumWithCheck(int[] arr) {
        // 增加数组边界检查
        if (arr == null || arr.length == 0) {
            return 0;
        }

        int total = 0;
        for (int num : arr) {
            total += num;
        }
        System.out.println("Sum calculated");  // 新增输出语句
        return total;
    }
}
