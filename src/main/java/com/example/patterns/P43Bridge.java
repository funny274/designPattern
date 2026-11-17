package patterns;

/**
 * 4.3 桥接 Bridge —— 可运行版（对应正文 4.3 / 图 4.3）
 *
 * 两个独立变化的维度：
 *   抽象维度 = 通知类型（订单 / 营销）
 *   实现维度 = 发送方式（短信 / 邮件）
 * 用组合连接，类数量是 2+2=4；若用继承硬组合则是 2×2=4 个类，维度增加后差距会迅速拉大。
 */
public class P43Bridge {

    interface MessageSender {
        boolean send(String to, String content);
    }
    static class SmsSender implements MessageSender {
        public boolean send(String to, String content) {
            System.out.println("  [短信 → " + to + "] " + content);
            return true;
        }
    }
    static class EmailSender implements MessageSender {
        public boolean send(String to, String content) {
            System.out.println("  [邮件 → " + to + "] " + content);
            return true;
        }
    }
    static class PushSender implements MessageSender {   // 新增实现维度，抽象侧一行不用改
        public boolean send(String to, String content) {
            System.out.println("  [推送 → " + to + "] " + content);
            return true;
        }
    }

    static abstract class Notification {
        protected final MessageSender sender;    // 桥：持有实现维度
        protected Notification(MessageSender sender) { this.sender = sender; }
        public abstract void notify(String to, String msg);
    }
    static class OrderNotification extends Notification {
        OrderNotification(MessageSender s) { super(s); }
        public void notify(String to, String msg) { sender.send(to, "[订单通知] " + msg); }
    }
    static class MarketingNotification extends Notification {
        MarketingNotification(MessageSender s) { super(s); }
        public void notify(String to, String msg) { sender.send(to, "[营销通知] " + msg); }
    }

    public static void main(String[] args) {
        System.out.println("两个维度自由组合：");
        new OrderNotification(new SmsSender()).notify("138****0001", "订单已支付");
        new MarketingNotification(new EmailSender()).notify("a@b.com", "双 11 大促");
        new OrderNotification(new PushSender()).notify("device-9527", "订单已发货");
        new MarketingNotification(new SmsSender()).notify("139****0002", "会员日提醒");

        System.out.println();
        System.out.println("新增发送方式 PushSender 时，抽象侧 2 个类完全没动 → 这就是解耦两个维度；");
        System.out.println("只有一个维度会变时，别上桥接，那是过度设计。");
    }
}
