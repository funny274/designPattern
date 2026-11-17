package patterns;

import java.util.HashMap;
import java.util.Map;

/**
 * 3.2 装饰器 Decorator —— 可运行版（对应正文 3.2 / 图 3.2）
 *
 * 正文用 Spring（@Repository + @Primary + @Qualifier）；这里用纯 JDK 手动装配，
 * 依赖关系完全一致：装饰器持有一个被装饰者 + 一个 Cache。
 *
 * 观察重点：同一个接口，套一层就多一种能力，可继续叠加（正文的 LruCache / BlockingCache）。
 */
public class P32Decorator {

    record Goods(Long id, String name) { }

    interface GoodsRepository {
        Goods getById(Long id);
    }

    interface Cache {
        Goods get(Long id);
        void put(Long id, Goods g);
    }

    static class MapCache implements Cache {
        private final Map<Long, Goods> store = new HashMap<>();
        public Goods get(Long id)  { return store.get(id); }
        public void put(Long id, Goods g) { store.put(id, g); }
    }

    // ===== 被装饰者：真的去「查库」=====
    static class DbGoodsRepository implements GoodsRepository {
        private int queryCount = 0;
        public Goods getById(Long id) {
            queryCount++;
            System.out.println("  → 查数据库 id=" + id);
            return new Goods(id, "商品-" + id);
        }
        int queryCount() { return queryCount; }
    }

    // ===== 装饰器：实现同一接口 + 持有被装饰者 =====
    static class CachedGoodsRepository implements GoodsRepository {
        private final GoodsRepository delegate;
        private final Cache cache;
        CachedGoodsRepository(GoodsRepository delegate, Cache cache) {
            this.delegate = delegate;
            this.cache = cache;
        }
        public Goods getById(Long id) {
            Goods cached = cache.get(id);
            if (cached != null) {
                System.out.println("  → 命中缓存 id=" + id + "（不查库）");
                return cached;
            }
            Goods g = delegate.getById(id);
            cache.put(id, g);
            return g;
        }
    }

    // ===== 第二个装饰器：演示「可随意叠加」=====
    static class LoggedGoodsRepository implements GoodsRepository {
        private final GoodsRepository delegate;
        LoggedGoodsRepository(GoodsRepository delegate) { this.delegate = delegate; }
        public Goods getById(Long id) {
            System.out.println("  [LOG] 查询商品 " + id);
            return delegate.getById(id);
        }
    }

    public static void main(String[] args) {
        DbGoodsRepository db = new DbGoodsRepository();
        // 对应 Spring 里 @Qualifier("dbGoodsRepository") 注入底层、@Primary 暴露装饰版
        GoodsRepository repo = new LoggedGoodsRepository(new CachedGoodsRepository(db, new MapCache()));

        System.out.println("第一次查询：");
        System.out.println("  结果 = " + repo.getById(1L));
        System.out.println("第二次查询：");
        System.out.println("  结果 = " + repo.getById(1L));

        System.out.println();
        System.out.println("实际查库次数 = " + db.queryCount() + "（两次查询只查了一次库）");
        System.out.println("结论：装饰器 = 实现同一接口 + 持有被装饰者 + 由调用方显式组合；");
        System.out.println("      代理结构相同但意图是「控制访问」，且常由框架自动生成。");
    }
}
