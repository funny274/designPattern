package patterns;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 5.1 策略 Strategy —— 可运行版（对应正文 5.1 / 图 5.1）
 *
 * 正文用 Spring 注入 List<DiscountStrategy>；这里用纯 JDK 手动传入同样的 List，
 * 注册表逻辑完全一致：按 type 建索引 + 重复校验 + 找不到抛业务异常。
 *
 * 顺带演示正文修正过的两个细节：满减金额不能为负、不支持的类型不能 NPE。
 * 另外展示「策略注册表 ≠ 工厂方法」：这里是从已有实现中挑选，没有创建多态。
 */
public class P51Strategy {

    enum DiscountType { FULL_REDUCTION, MEMBER, NONE }

    interface DiscountStrategy {
        BigDecimal discount(BigDecimal amount);
        DiscountType type();
    }

    static class FullReductionStrategy implements DiscountStrategy {
        public BigDecimal discount(BigDecimal amount) {
            if (amount.compareTo(new BigDecimal("100")) < 0) return amount;
            return amount.subtract(new BigDecimal("50")).max(BigDecimal.ZERO); // 满 100 减 50，且不为负
        }
        public DiscountType type() { return DiscountType.FULL_REDUCTION; }
    }
    static class MemberStrategy implements DiscountStrategy {
        public BigDecimal discount(BigDecimal amount) { return amount.multiply(new BigDecimal("0.9")); }
        public DiscountType type() { return DiscountType.MEMBER; }
    }
    static class NoneStrategy implements DiscountStrategy {
        public BigDecimal discount(BigDecimal amount) { return amount; }
        public DiscountType type() { return DiscountType.NONE; }
    }

    // ===== 策略注册表：负责「选哪个」，策略负责「怎么算」=====
    static class PriceCalculator {
        private final Map<DiscountType, DiscountStrategy> strategies;
        PriceCalculator(List<DiscountStrategy> list) {
            Map<DiscountType, DiscountStrategy> m = new HashMap<>();
            for (DiscountStrategy s : list) {
                DiscountStrategy previous = m.putIfAbsent(s.type(), s);
                if (previous != null) throw new IllegalStateException("重复的策略类型: " + s.type());
            }
            this.strategies = Map.copyOf(m);
        }
        BigDecimal calc(DiscountType type, BigDecimal amount) {
            DiscountStrategy s = strategies.get(type);
            if (s == null) throw new IllegalArgumentException("不支持的优惠类型: " + type);
            return s.discount(amount);
        }
    }

    public static void main(String[] args) {
        PriceCalculator calc = new PriceCalculator(List.of(
                new FullReductionStrategy(), new MemberStrategy(), new NoneStrategy()));

        System.out.println("原价 ¥299：");
        System.out.println("  满减   = ¥" + calc.calc(DiscountType.FULL_REDUCTION, new BigDecimal("299")));
        System.out.println("  会员价 = ¥" + calc.calc(DiscountType.MEMBER, new BigDecimal("299")));
        System.out.println("  无优惠 = ¥" + calc.calc(DiscountType.NONE, new BigDecimal("299")));
        System.out.println("  小额满减（¥30，不达门槛）= ¥" + calc.calc(DiscountType.FULL_REDUCTION, new BigDecimal("30")));
        System.out.println("  刚过门槛（¥120，减 50）= ¥" + calc.calc(DiscountType.FULL_REDUCTION, new BigDecimal("120")));

        // 若把规则配成「满 30 减 50」，.max(ZERO) 会把结果兜到 0，而不是负数或异常
        DiscountStrategy aggressive = new DiscountStrategy() {
            public BigDecimal discount(BigDecimal a) { return a.subtract(new BigDecimal("50")).max(BigDecimal.ZERO); }
            public DiscountType type() { return DiscountType.FULL_REDUCTION; }
        };
        System.out.println("  规则配错时（满 30 减 50，原价 ¥30）金额保护 = ¥"
                + aggressive.discount(new BigDecimal("30")) + "，不会算出负数");

        // 重复类型校验（对应 Spring 里两个 @Component 报同一个 type）
        try {
            new PriceCalculator(List.of(new MemberStrategy(), new MemberStrategy()));
        } catch (IllegalStateException e) {
            System.out.println("\n注册校验拦截：" + e.getMessage());
        }
        // 未知类型不会 NPE
        try {
            new PriceCalculator(List.of(new NoneStrategy())).calc(DiscountType.MEMBER, BigDecimal.TEN);
        } catch (IllegalArgumentException e) {
            System.out.println("查找失败给出明确异常：" + e.getMessage());
        }

        System.out.println();
        System.out.println("结论：判据是「算法是否独立变化、是否持续扩展」，不是单纯数 if-else 个数；");
        System.out.println("      2 个稳定短分支保留 if-else 往往更清晰。这里没有工厂子类，所以不是工厂方法。");
    }
}
