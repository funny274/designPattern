package patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * 5.2 观察者 Observer —— 可运行版（对应正文 5.2 / 图 5.2）
 *
 * 正文用 Spring 的 ApplicationEventPublisher + @EventListener；
 * 这里用纯 JDK 复刻它的核心机制（按事件类型找监听器 → 逐个同步调用），
 * 让你不装 Spring 也能看清「一对多解耦」到底发生了什么。
 *
 * 演示两个面试点：
 *   1) 默认是同步、同线程，监听器抛异常会向发布方传播（没有内置异常隔离）；
 *   2) 新增副作用只加一个 Listener，发布方一行不改。
 */
public class P52Observer {

    record Order(Long id, Long userId) { }
    static class OrderCreatedEvent {
        private final Order order;
        OrderCreatedEvent(Order order) { this.order = order; }
        Order getOrder() { return order; }
    }

    interface Listener {
        void onOrderCreated(OrderCreatedEvent e);
    }

    // ===== 极简事件总线（等价于 Spring 容器里 ApplicationEventPublisher 的角色）=====
    static class EventPublisher {
        private final List<Listener> listeners = new ArrayList<>();
        void register(Listener l) { listeners.add(l); }
        void publishEvent(OrderCreatedEvent e) {
            for (Listener l : listeners) {
                l.onOrderCreated(e);   // 同步调用：异常会直接冒到 publishEvent 的调用方
            }
        }
    }

    // ===== 发布方 =====
    static class OrderService {
        private final EventPublisher publisher;
        OrderService(EventPublisher publisher) { this.publisher = publisher; }
        Long create(Order order) {
            System.out.println("  [主流程] 保存订单主记录 " + order.id());
            publisher.publishEvent(new OrderCreatedEvent(order));
            return order.id();
        }
    }

    // ===== 三个订阅方（与图 5.2 / 正文代码一一对应）=====
    static class CouponListener implements Listener {
        public void onOrderCreated(OrderCreatedEvent e) { System.out.println("    · 发券：用户 " + e.getOrder().userId()); }
    }
    static class StockListener implements Listener {
        public void onOrderCreated(OrderCreatedEvent e) { System.out.println("    · 扣库存：订单 " + e.getOrder().id()); }
    }
    static class LedgerListener implements Listener {
        public void onOrderCreated(OrderCreatedEvent e) { System.out.println("    · 记账：订单 " + e.getOrder().id()); }
    }

    public static void main(String[] args) {
        EventPublisher publisher = new EventPublisher();
        publisher.register(new CouponListener());
        publisher.register(new StockListener());
        publisher.register(new LedgerListener());

        System.out.println("下单一次，主流程只发一个事件：");
        new OrderService(publisher).create(new Order(1L, 10086L));

        System.out.println("\n新增一个副作用（积分），发布方代码不变：");
        publisher.register(e -> System.out.println("    · 加积分：用户 " + e.getOrder().userId()));
        new OrderService(publisher).create(new Order(2L, 10087L));

        System.out.println("\n演示同步异常的传播：");
        EventPublisher bad = new EventPublisher();
        bad.register(e -> { throw new IllegalStateException("发券失败"); });
        bad.register(new StockListener());
        try {
            bad.publishEvent(new OrderCreatedEvent(new Order(3L, 10088L)));
        } catch (IllegalStateException ex) {
            System.out.println("  发布方捕获到监听器异常：" + ex.getMessage());
            System.out.println("  → 后面的扣库存监听器没有执行，主流程也受影响");
        }

        System.out.println();
        System.out.println("结论：@EventListener 默认同步；@TransactionalEventListener 只是改触发时机，不自动异步；");
        System.out.println("      要隔离异常需自行 try-catch、配置 @Async 的异常处理器，或改用 MQ / Outbox。");
    }
}
