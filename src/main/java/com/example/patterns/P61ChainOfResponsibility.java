package patterns;

import java.math.BigDecimal;

/**
 * 6.1 责任链 Chain of Responsibility —— 可运行版（对应正文 6.1 / 图 6.1）
 *
 * 正文里的 StockService / RiskService / ParamCheckHandler / RiskCheckHandler 全部补齐。
 *
 * 重点演示正文强调的坑：
 *   setNext() 返回的是「下一个节点」，所以链头必须单独保留；
 *   写成 chain = h1.setNext(h2).setNext(h3) 会让 chain 指向 h3，前两个节点被跳过。
 */
public class P61ChainOfResponsibility {

    record Order(String orderNo, BigDecimal amount, int stock, boolean blacklisted) { }

    static abstract class OrderCheckHandler {
        protected OrderCheckHandler next;

        // 返回 next，便于 h1.setNext(h2).setNext(h3) 连续装配
        public OrderCheckHandler setNext(OrderCheckHandler next) {
            this.next = next;
            return next;
        }

        // 模板化的链逻辑：自己失败则短路，成功则交给下一个
        public boolean check(Order order) {
            if (!doCheck(order)) return false;
            return next == null || next.check(order);
        }
        protected abstract boolean doCheck(Order order);
    }

    static class ParamCheckHandler extends OrderCheckHandler {
        protected boolean doCheck(Order o) {
            boolean ok = o != null && o.amount() != null && o.amount().signum() > 0;
            System.out.println("  · 参数校验：" + ok);
            return ok;
        }
    }
    static class StockCheckHandler extends OrderCheckHandler {
        protected boolean doCheck(Order o) {
            boolean ok = o.stock() > 0;
            System.out.println("  · 库存校验：" + ok);
            return ok;
        }
    }
    static class RiskCheckHandler extends OrderCheckHandler {
        protected boolean doCheck(Order o) {
            boolean ok = !o.blacklisted();
            System.out.println("  · 风控校验：" + ok);
            return ok;
        }
    }

    public static void main(String[] args) {
        // 每次请求单独建链：处理者持有可变 next，注册成 Spring 单例会有并发风险
        ParamCheckHandler h1 = new ParamCheckHandler();
        StockCheckHandler h2 = new StockCheckHandler();
        RiskCheckHandler h3 = new RiskCheckHandler();

        System.out.println("正确装配（链头单独保留）：");
        h1.setNext(h2).setNext(h3);
        OrderCheckHandler chain = h1;
        System.out.println("  结果 = " + chain.check(new Order("SO001", new BigDecimal("299"), 10, false)));

        System.out.println("\n失败短路（库存为 0，风控不会执行）：");
        chain.check(new Order("SO002", new BigDecimal("299"), 0, false));

        // 反面演示：正文注释里说的错误写法
        ParamCheckHandler b1 = new ParamCheckHandler();
        StockCheckHandler b2 = new StockCheckHandler();
        RiskCheckHandler b3 = new RiskCheckHandler();
        System.out.println("\n错误装配 chain = b1.setNext(b2).setNext(b3)：");
        OrderCheckHandler wrongChain = b1.setNext(b2).setNext(b3);
        System.out.println("  chain 实际类型是 " + wrongChain.getClass().getSimpleName() + "（只剩最后一个节点）");
        wrongChain.check(new Order("SO003", new BigDecimal("299"), 10, true));
        System.out.println("  ↑ 只跑了风控，参数/库存校验被整段跳过");

        System.out.println();
        System.out.println("结论：短路链 vs 全走链（过滤器）按业务语义选；");
        System.out.println("      节点多时考虑不可变的有序处理器列表，避免运行时改 next。");
    }
}
