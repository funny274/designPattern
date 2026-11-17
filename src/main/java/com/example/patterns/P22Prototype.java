package patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.2 原型 Prototype —— 可运行版（对应正文 2.2）
 *
 * 亲手制造一次「浅拷贝污染」：只调用 super.clone()，副本和原件共享同一个 List。
 * 然后展示深拷贝如何修好它。
 *
 * 正文强调：Spring @Scope("prototype") 是按 BeanDefinition 新建实例，不是复制；
 * 对象池是复用实例，也不是原型模式。
 */
public class P22Prototype {

    static class OrderTemplate implements Cloneable {
        private String channel;                        // 不可变引用类型
        private List<String> tags = new ArrayList<>(); // 可变引用类型

        OrderTemplate(String channel) { this.channel = channel; }

        void addTag(String t) { tags.add(t); }
        List<String> tags()   { return tags; }

        // 浅拷贝：故意只写到 super.clone()
        OrderTemplate shallowCopy() {
            try { return (OrderTemplate) super.clone(); }
            catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }

        // 深拷贝：把可变引用字段也复制一份
        OrderTemplate deepCopy() {
            OrderTemplate copy = shallowCopy();
            copy.tags = new ArrayList<>(this.tags);
            return copy;
        }
    }

    public static void main(String[] args) {
        OrderTemplate original = new OrderTemplate("WECHAT");
        original.addTag("大促");

        // ---- 浅拷贝的坑 ----
        OrderTemplate shallow = original.shallowCopy();
        shallow.addTag("仅给副本");
        System.out.println("浅拷贝后，原件 tags = " + original.tags());
        System.out.println("           副本 tags = " + shallow.tags());
        System.out.println("两个 List 是否同一个对象：" + (original.tags() == shallow.tags())
                + "  → 改副本污染了原件");

        // ---- 深拷贝修好 ----
        OrderTemplate deep = original.deepCopy();
        deep.addTag("深拷贝副本");
        System.out.println();
        System.out.println("深拷贝后，原件 tags = " + original.tags());
        System.out.println("           副本 tags = " + deep.tags());
        System.out.println("两个 List 是否同一个对象：" + (original.tags() == deep.tags()));

        System.out.println();
        System.out.println("结论：String 等不可变对象浅拷贝可安全共享；可变集合必须复制。");
        System.out.println("      能用「不可变对象 + 构造器」解决的，就别用 clone()。");
    }
}
