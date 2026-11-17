package patterns;

/**
 * 5.3 模板方法 Template Method —— 可运行版（对应正文 5.3 / 图 5.3）
 *
 * 正文的 MessageBroker / Message / OrderService / StockService 在这里全部补齐。
 * 观察重点：
 *   1) handle() 是 final，子类只能改 doProcess()，骨架不会被破坏；
 *   2) validate() 是钩子方法，子类可选覆盖；
 *   3) 校验失败不 ack（正文注释里强调的点）。
 */
public class P53TemplateMethod {

    record Message(String id, String body) { }

    static class MessageBroker {
        void acknowledge(Message m) { System.out.println("  [broker] ack " + m.id()); }
    }

    static abstract class AbstractMessageHandler {
        private final MessageBroker msgBroker;
        protected AbstractMessageHandler(MessageBroker msgBroker) { this.msgBroker = msgBroker; }

        // 模板方法：final 防止子类改写整体流程
        public final void handle(Message msg) {
            if (!validate(msg)) {
                // 校验失败不 ack：交由 MQ 重投 / 死信；业务上要丢弃就应显式 nack
                System.out.println("  [骨架] 校验未通过，跳过且不 ack：" + msg.id());
                return;
            }
            doProcess(msg);
            ack(msg);
        }

        protected boolean validate(Message msg) { return true; }        // 钩子
        protected abstract void doProcess(Message msg);                  // 可变步
        protected void ack(Message msg) { msgBroker.acknowledge(msg); } // 固定步
    }

    static class OrderService {
        void syncStatus(String body) { System.out.println("  [订单处理] 同步状态：" + body); }
    }
    static class StockService {
        void syncStock(String body) { System.out.println("  [库存处理] 同步库存：" + body); }
    }

    static class OrderMessageHandler extends AbstractMessageHandler {
        private final OrderService orderService;
        OrderMessageHandler(MessageBroker b, OrderService s) { super(b); this.orderService = s; }
        protected void doProcess(Message msg) { orderService.syncStatus(msg.body()); }
    }

    static class StockMessageHandler extends AbstractMessageHandler {
        private final StockService stockService;
        StockMessageHandler(MessageBroker b, StockService s) { super(b); this.stockService = s; }
        protected void doProcess(Message msg) { stockService.syncStock(msg.body()); }

        // 覆盖钩子：空消息不处理，展示「钩子 = 可选覆盖」
        @Override
        protected boolean validate(Message msg) {
            boolean ok = msg.body() != null && !msg.body().isBlank();
            System.out.println("  [钩子] StockMessageHandler 自定义校验 = " + ok);
            return ok;
        }
    }

    public static void main(String[] args) {
        MessageBroker broker = new MessageBroker();
        OrderMessageHandler order = new OrderMessageHandler(broker, new OrderService());
        StockMessageHandler stock = new StockMessageHandler(broker, new StockService());

        System.out.println("订单消息（走完整骨架）：");
        order.handle(new Message("M1", "ORDER_PAID"));
        System.out.println("库存消息（覆盖钩子后走完整骨架）：");
        stock.handle(new Message("M2", "SKU_9_DEDUCT_2"));
        System.out.println("空消息（钩子拦截，不 ack）：");
        stock.handle(new Message("M3", ""));

        System.out.println();
        System.out.println("结论：模板方法用继承复用「骨架」，策略用组合替换「整个算法」；");
        System.out.println("      JdbcTemplate 是「模板 + 回调(RowMapper)」，不是纯继承式模板方法。");
    }
}
