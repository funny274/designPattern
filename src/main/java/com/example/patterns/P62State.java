package patterns;

/**
 * 6.2 状态 State —— 可运行版（对应正文 6.2 / 图 6.2）
 *
 * 正文代码里标为「实现结构相同，此处省略」的 PaidState / ShippedState /
 * CancelledState / CompletedState 在这里全部实现，状态机可以完整跑通。
 *
 * 观察重点：调用方只发动作，不写 if (status == ...)；非法迁移由状态对象自己拒绝。
 */
public class P62State {

    interface OrderState {
        void pay(OrderContext ctx);
        void ship(OrderContext ctx);
        void cancel(OrderContext ctx);
        void complete(OrderContext ctx);
    }

    static class OrderContext {
        private OrderState state = new PendingPayState();
        void setState(OrderState s) {
            this.state = s;
            System.out.println("    → 状态迁移到 " + s.getClass().getSimpleName());
        }
        String stateName() { return state.getClass().getSimpleName(); }
        public void pay()      { state.pay(this); }
        public void ship()     { state.ship(this); }
        public void cancel()   { state.cancel(this); }
        public void complete() { state.complete(this); }
    }

    static class PendingPayState implements OrderState {
        public void pay(OrderContext ctx)      { ctx.setState(new PaidState()); }
        public void ship(OrderContext ctx)     { throw new IllegalStateException("未支付的订单不能发货"); }
        public void cancel(OrderContext ctx)   { ctx.setState(new CancelledState()); }
        public void complete(OrderContext ctx) { throw new IllegalStateException("未支付的订单不能完成"); }
    }
    static class PaidState implements OrderState {
        public void pay(OrderContext ctx)      { throw new IllegalStateException("已支付，不能重复支付"); }
        public void ship(OrderContext ctx)     { ctx.setState(new ShippedState()); }
        public void cancel(OrderContext ctx)   { ctx.setState(new CancelledState()); }
        public void complete(OrderContext ctx) { throw new IllegalStateException("未发货不能完成"); }
    }
    static class ShippedState implements OrderState {
        public void pay(OrderContext ctx)      { throw new IllegalStateException("已发货订单不能再支付"); }
        public void ship(OrderContext ctx)     { throw new IllegalStateException("不能重复发货"); }
        public void cancel(OrderContext ctx)   { throw new IllegalStateException("已发货订单不能直接取消，请走售后"); }
        public void complete(OrderContext ctx) { ctx.setState(new CompletedState()); }
    }
    static class CancelledState implements OrderState {
        public void pay(OrderContext ctx)      { throw new IllegalStateException("订单已取消"); }
        public void ship(OrderContext ctx)     { throw new IllegalStateException("订单已取消"); }
        public void cancel(OrderContext ctx)   { throw new IllegalStateException("订单已取消"); }
        public void complete(OrderContext ctx) { throw new IllegalStateException("订单已取消"); }
    }
    static class CompletedState implements OrderState {
        public void pay(OrderContext ctx)      { throw new IllegalStateException("订单已完成"); }
        public void ship(OrderContext ctx)     { throw new IllegalStateException("订单已完成"); }
        public void cancel(OrderContext ctx)   { throw new IllegalStateException("订单已完成"); }
        public void complete(OrderContext ctx) { throw new IllegalStateException("订单已完成"); }
    }

    public static void main(String[] args) {
        System.out.println("正常链路：");
        OrderContext ctx = new OrderContext();
        System.out.println("  初始状态 = " + ctx.stateName());
        ctx.pay();
        ctx.ship();
        ctx.complete();
        System.out.println("  最终状态 = " + ctx.stateName());

        System.out.println("\n非法迁移（重复支付）：");
        try { ctx.pay(); }
        catch (IllegalStateException e) { System.out.println("  被状态对象挡住：" + e.getMessage()); }

        System.out.println("\n取消分支：");
        OrderContext cancelled = new OrderContext();
        cancelled.cancel();
        System.out.println("  最终状态 = " + cancelled.stateName());

        System.out.println();
        System.out.println("结论：状态类把「某状态下哪些动作合法」内聚起来，替代散落的 switch；");
        System.out.println("      状态少、动作差异小时用「枚举 + 状态表」更轻，别为了模式而模式。");
        System.out.println("      真实系统还要处理持久化、乐观锁/CAS、幂等与并发迁移。");
    }
}
