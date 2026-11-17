package patterns;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.1 建造者 Builder —— 可运行版（对应正文 2.1 / 图 2.1）
 *
 * 正文用 Lombok @Builder；这里用纯 JDK 手写一遍，好处是能看清 Lombok 替你做了什么：
 *   - 链式 setter 返回 this；
 *   - build() 里做必填校验（Lombok 不会自动校验）；
 *   - 可变集合做防御性拷贝（字段 final 不等于内容不可变）。
 *
 * 正文 2.1 提到的落差：GoF 原版还有 Director 编排构建步骤，工程上的流式 Builder 通常省略它。
 */
public class P21Builder {

    enum PayChannel { WECHAT, ALIPAY }

    static final class OrderCreateRequest {
        private final String orderNo;
        private final Long userId;
        private final BigDecimal amount;
        private final PayChannel channel;
        private final String remark;
        private final List<String> tags;

        private OrderCreateRequest(Builder b) {
            this.orderNo = b.orderNo;
            this.userId = b.userId;
            this.amount = b.amount;
            this.channel = b.channel;
            this.remark = b.remark;
            this.tags = List.copyOf(b.tags);      // 防御性拷贝 + 不可变视图
        }

        public static Builder builder() { return new Builder(); }

        public String toString() {
            return "OrderCreateRequest{orderNo=" + orderNo + ", userId=" + userId
                    + ", amount=" + amount + ", channel=" + channel
                    + ", remark='" + remark + "', tags=" + tags + "}";
        }

        static final class Builder {
            private String orderNo;
            private Long userId;
            private BigDecimal amount;
            private PayChannel channel = PayChannel.WECHAT;   // 默认值
            private String remark = "";
            private final List<String> tags = new ArrayList<>();

            public Builder orderNo(String v)     { this.orderNo = v; return this; }
            public Builder userId(Long v)        { this.userId = v; return this; }
            public Builder amount(BigDecimal v)  { this.amount = v; return this; }
            public Builder channel(PayChannel v) { this.channel = v; return this; }
            public Builder remark(String v)      { this.remark = v; return this; }
            public Builder tag(String v)         { this.tags.add(v); return this; }

            public OrderCreateRequest build() {
                // 必填校验：这是 @Builder 不会替你做的事
                if (orderNo == null || orderNo.isBlank()) throw new IllegalStateException("orderNo 必填");
                if (userId == null)                        throw new IllegalStateException("userId 必填");
                if (amount == null || amount.signum() <= 0) throw new IllegalStateException("amount 必须大于 0");
                return new OrderCreateRequest(this);
            }
        }
    }

    public static void main(String[] args) {
        OrderCreateRequest req = OrderCreateRequest.builder()
                .orderNo("SO202611040001")
                .userId(10086L)
                .amount(new BigDecimal("299.00"))
                .channel(PayChannel.ALIPAY)
                .tag("大促")
                .tag("包邮")
                .build();
        System.out.println("正常构建：" + req);

        // 缺省字段走默认值
        OrderCreateRequest minimal = OrderCreateRequest.builder()
                .orderNo("SO202611040002").userId(10087L).amount(new BigDecimal("1"))
                .build();
        System.out.println("省略可选字段：" + minimal);

        // build() 校验生效
        try {
            OrderCreateRequest.builder().userId(1L).amount(BigDecimal.ONE).build();
        } catch (IllegalStateException e) {
            System.out.println("必填校验拦住了非法对象：" + e.getMessage());
        }

        System.out.println();
        System.out.println("结论：可选字段多、构造步骤多时才值；字段全 final 只保证引用不可变，");
        System.out.println("      指向可变集合时仍要防御性拷贝，且 @Builder 默认不包含父类字段。");
    }
}
