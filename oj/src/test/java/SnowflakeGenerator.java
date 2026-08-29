import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class SnowflakeGenerator {
    public static void main(String[] args) {
//        // 创建Snowflake对象
//        Snowflake snowflake = IdUtil.getSnowflake();
//
//        // 生成10个ID
//        for (int i = 0; i < 10; i++) {
//            long id = snowflake.nextId();
//            System.out.println(id);
//        }

        String snowflakeNextIdStr = IdUtil.objectId();
        System.out.println(snowflakeNextIdStr);
        // 查看长度
        System.out.println(snowflakeNextIdStr.length());
    }
}