package patterns;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 1.3 单例 Singleton —— 可运行版（对应正文 1.3 / 图 1.3）
 *
 * 三种线程安全写法：双重检查锁（必须 volatile）、静态内部类 Holder、枚举。
 * 同时演示正文提到的两个坑：
 *   - getInstance(workerId) 只有第一次传的参数生效；
 *   - 手写单例可被反射破坏，枚举天然免疫。
 */
public class P13Singleton {

    // ===== 写法一：DCL + volatile =====
    static class SnowflakeIdGenerator {
        private static volatile SnowflakeIdGenerator instance;
        private final long workerId;

        private SnowflakeIdGenerator(long workerId) { this.workerId = workerId; }

        public static SnowflakeIdGenerator getInstance(long workerId) {
            if (instance == null) {
                synchronized (SnowflakeIdGenerator.class) {
                    if (instance == null) {
                        instance = new SnowflakeIdGenerator(workerId);
                    }
                }
            }
            return instance;
        }
        public long getWorkerId() { return workerId; }
    }

    // ===== 写法二：静态内部类 Holder（懒加载 + JVM 保证线程安全）=====
    static class HolderSingleton {
        private HolderSingleton() { }
        private static class Holder {
            private static final HolderSingleton INSTANCE = new HolderSingleton();
        }
        public static HolderSingleton getInstance() { return Holder.INSTANCE; }
    }

    // ===== 写法三：枚举（防反射 / 防序列化破坏）=====
    enum ConfigCenter {
        INSTANCE;
        private final Map<String, String> config = Map.of("timeout", "3000", "retry", "2");
        public String get(String key) { return config.get(key); }
    }

    public static void main(String[] args) throws Exception {
        // 1) 并发下只创建一个实例
        int n = 8;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        AtomicReference<SnowflakeIdGenerator> first = new AtomicReference<>();
        java.util.Set<SnowflakeIdGenerator> seen =
                java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                SnowflakeIdGenerator g = SnowflakeIdGenerator.getInstance(1L);
                first.compareAndSet(null, g);
                seen.add(g);
                done.countDown();
            }).start();
        }
        start.countDown();
        done.await();
        System.out.println("DCL 并发获取 " + n + " 次，实际不同实例数 = " + seen.size());

        // 2) 坑：后续传入的 workerId 被静默忽略
        SnowflakeIdGenerator again = SnowflakeIdGenerator.getInstance(99L);
        System.out.println("第一次 workerId=1，第二次传入 99，实际拿到 = " + again.getWorkerId()
                + "  → 参数被忽略，这是正文标注的生产风险");

        // 3) Holder / 枚举
        System.out.println("Holder 两次获取是否同一对象："
                + (HolderSingleton.getInstance() == HolderSingleton.getInstance()));
        System.out.println("枚举读取配置 timeout = " + ConfigCenter.INSTANCE.get("timeout"));

        // 4) 反射可破坏普通单例，枚举不行
        try {
            java.lang.reflect.Constructor<SnowflakeIdGenerator> c =
                    SnowflakeIdGenerator.class.getDeclaredConstructor(long.class);
            c.setAccessible(true);
            SnowflakeIdGenerator hacked = c.newInstance(7L);
            System.out.println("反射造出的新实例是否等于单例：" + (hacked == first.get()) + "  → 手写单例被破坏");
        } catch (Exception e) {
            System.out.println("反射被安全限制拦截：" + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("结论：Spring 项目里优先交给容器管理（容器内每个 Bean 定义一个实例）；");
        System.out.println("      手写单例只在「Spring 管不到的纯工具/静态资源」时才需要，且不是分布式全局唯一。");
    }
}
