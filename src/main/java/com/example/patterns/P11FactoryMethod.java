package patterns;

/**
 * 1.1 工厂方法 Factory Method —— 可运行版（对应正文 1.1 / 图 1.1）
 *
 * 观察重点：
 *   1) 业务代码只依赖 PaymentServiceFactory + PaymentService 两个抽象；
 *   2) 「实例化哪个具体产品」由工厂子类决定，这就是创建多态；
 *   3) 换渠道只换工厂实例，pay() 一行都不用改。
 *
 * 注意：正文强调「按 Map 从 Spring 已有 Bean 里挑一个实现」属于策略注册表，
 *      不是 GoF 工厂方法。本文件只演示工厂方法，策略注册表见 P51Strategy。
 *
 * 为让 17 个示例互不冲突，协作类统一写成主类的静态内部类；
 * 结构与正文代码一致，只是组织形式不同。
 */
public class P11FactoryMethod {

    // ===== 产品抽象 =====
    interface PaymentService {
        PayResult pay(PayOrder order);
    }

    // ===== 具体产品 =====
    static class WechatPayClient {
        PayResult unifiedOrder(PayOrder o) {
            return new PayResult("WECHAT", o.amount(), "微信支付成功");
        }
    }

    static class AlipayClient {
        PayResult tradeCreate(PayOrder o) {
            return new PayResult("ALIPAY", o.amount(), "支付宝支付成功");
        }
    }

    static class WechatPayService implements PaymentService {
        private final WechatPayClient client;
        WechatPayService(WechatPayClient client) { this.client = client; }
        public PayResult pay(PayOrder order) { return client.unifiedOrder(order); }
    }

    static class AlipayService implements PaymentService {
        private final AlipayClient client;
        AlipayService(AlipayClient client) { this.client = client; }
        public PayResult pay(PayOrder order) { return client.tradeCreate(order); }
    }

    // ===== 创建者：createService() 才是工厂方法，pay() 只是复用的业务流程 =====
    static abstract class PaymentServiceFactory {
        protected abstract PaymentService createService();

        public final PayResult pay(PayOrder order) {
            PaymentService service = createService();
            return service.pay(order);
        }
    }

    static class WechatPaymentServiceFactory extends PaymentServiceFactory {
        private final WechatPayClient client;
        WechatPaymentServiceFactory(WechatPayClient client) { this.client = client; }
        @Override protected PaymentService createService() { return new WechatPayService(client); }
    }

    static class AlipayPaymentServiceFactory extends PaymentServiceFactory {
        private final AlipayClient client;
        AlipayPaymentServiceFactory(AlipayClient client) { this.client = client; }
        @Override protected PaymentService createService() { return new AlipayService(client); }
    }

    // ===== 协作类（正文里标为「省略定义」的部分）=====
    record PayOrder(String orderNo, double amount) { }
    record PayResult(String channel, double amount, String message) {
        public String toString() { return channel + " | ¥" + amount + " | " + message; }
    }

    public static void main(String[] args) {
        PayOrder order = new PayOrder("SO202611040001", 299.00);

        // 装配处才出现 new；业务层只认识抽象工厂
        PaymentServiceFactory wechat = new WechatPaymentServiceFactory(new WechatPayClient());
        PaymentServiceFactory alipay = new AlipayPaymentServiceFactory(new AlipayClient());

        System.out.println("[微信工厂] " + wechat.pay(order));
        System.out.println("[支付宝工厂] " + alipay.pay(order));

        System.out.println();
        System.out.println("结论：新增一个渠道 = 加 1 个产品类 + 1 个工厂子类，pay() 与调用方零改动。");
        System.out.println("若只是想「按枚举从已有 Bean 里选一个」，请用策略注册表，不要硬套工厂方法。");
    }
}
