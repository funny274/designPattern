package patterns;

/**
 * 4.1 门面 Facade —— 可运行版（对应正文 4.1 / 图 4.1）
 *
 * 调用方只调一个 pay()，内部编排风控 → 支付 → 记账 → 通知。
 * 正文的 PayOrder / PayResult / XxxService 都在这里补齐。
 *
 * 正文要点：门面只做编排，不承载业务细节；跨服务调用不能靠一个本地事务保证一致性。
 */
public class P41Facade {

    record PayOrder(String orderNo, double amount) { }
    record PayResult(boolean success, String message) { }

    static class RiskService    { void check(PayOrder o)      { System.out.println("  1. 风控校验通过 " + o.orderNo()); } }
    static class ChannelService { PayResult pay(PayOrder o)   { System.out.println("  2. 渠道扣款 ¥" + o.amount()); return new PayResult(true, "支付成功"); } }
    static class LedgerService  { void record(PayOrder o, PayResult r) { System.out.println("  3. 记账：" + r.message()); } }
    static class NotifyService  { void notify(PayOrder o, PayResult r) { System.out.println("  4. 通知用户：" + r.message()); } }

    static class PaymentFacade {
        private final RiskService riskService;
        private final ChannelService channelService;
        private final LedgerService ledgerService;
        private final NotifyService notifyService;

        PaymentFacade(RiskService r, ChannelService c, LedgerService l, NotifyService n) {
            this.riskService = r; this.channelService = c;
            this.ledgerService = l; this.notifyService = n;
        }

        // 对外唯一入口
        public PayResult pay(PayOrder order) {
            riskService.check(order);
            PayResult result = channelService.pay(order);
            ledgerService.record(order, result);
            notifyService.notify(order, result);
            return result;
        }
    }

    public static void main(String[] args) {
        PaymentFacade facade = new PaymentFacade(
                new RiskService(), new ChannelService(), new LedgerService(), new NotifyService());

        System.out.println("调用方只写一行 facade.pay(order)：");
        PayResult r = facade.pay(new PayOrder("SO202611040001", 299.00));
        System.out.println("  返回：" + r.message());

        System.out.println();
        System.out.println("结论：门面是做减法，把一组杂乱调用收拢成一个 API；");
        System.out.println("      别把业务规则堆进门面变成上帝类，也别指望它解决分布式事务一致性。");
    }
}
