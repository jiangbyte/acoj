import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

public class MpIdGenerator {
    public static void main(String[] args) {
        // 方式1：使用默认生成器
        IdentifierGenerator generator = new DefaultIdentifierGenerator();
        
        // 生成10个ID
        for (int i = 0; i < 10; i++) {
            Number id = generator.nextId(new Object());
            System.out.println( id);
//            System.out.println("INSERT INTO `sys_category`");
//            System.out.println("VALUES ('" + id + "', '分类名称" + (i+1) + "', 0, '2025-10-21 12:25:12', '1', '2025-10-22 12:25:12', '1');");
        }
        
        // 方式2：获取单例
        Number singleId = DefaultIdentifierGenerator.getInstance().nextId(new Object());
        System.out.println("单个ID: " + singleId);
    }
}