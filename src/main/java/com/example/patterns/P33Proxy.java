package patterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * 3.3 代理 Proxy —— 可运行版（对应正文 3.3 / 图 3.3 + ProxyFactory 代码）
 *
 * 正文图 3.3 画的是静态代理结构，代码是 JDK 动态代理；两者结构等价。
 * 本文件跑的是动态代理，并演示两个面试点：
 *   1) method.invoke 抛出的 InvocationTargetException 要还原成原始异常；
 *   2) 目标对象内部 this.xxx() 自调用不会经过代理（AOP 失效经典坑）。
 */
public class P33Proxy {

    record Order(String orderNo) { }

    interface OrderService {
        Long create(Order order);
        void createWithSelfCall(Order order);
    }

    static class OrderServiceImpl implements OrderService {
        public Long create(Order order) {
            System.out.println("  [目标对象] 真正执行下单 " + order.orderNo());
            return 1L;
        }
        // 自调用：create() 是 this.create(...)，不会经过代理
        public void createWithSelfCall(Order order) {
            System.out.println("  [目标对象] createWithSelfCall 内部调用 this.create()");
            this.create(order);
        }
    }

    static final class ProxyFactory {
        @SuppressWarnings("unchecked")
        static <T> T wrapLogging(T target, Class<T> iface) {
            return (T) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{ iface },
                    (proxy, method, args) -> {
                        long start = System.nanoTime();
                        try {
                            return method.invoke(target, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();   // 还原业务异常，别把反射包装异常抛给上层
                        } finally {
                            System.out.println("  [代理] " + method.getName() + " 耗时 "
                                    + (System.nanoTime() - start) / 1_000_000 + "ms");
                        }
                    });
        }
    }

    public static void main(String[] args) {
        OrderService proxy = ProxyFactory.wrapLogging(new OrderServiceImpl(), OrderService.class);

        System.out.println("通过代理调用 create()：");
        Long id = proxy.create(new Order("SO001"));
        System.out.println("  返回订单 id = " + id);

        System.out.println("\n自调用场景（观察日志条数）：");
        proxy.createWithSelfCall(new Order("SO002"));
        System.out.println("  ↑ 只有 createWithSelfCall 一条代理日志，内部 create() 没有再被拦截");

        System.out.println("\n结论：JDK 动态代理基于接口（CGLIB 基于继承）；");
        System.out.println("      Spring 事务/@Async 等通知在 this.xxx() 自调用时会失效，需拆分到另一个 Bean 或用 AopContext。");
    }
}
