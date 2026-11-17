package patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * 4.2 组合 Composite —— 可运行版（对应正文 4.2 / 图 4.2）
 *
 * 叶子 MenuItem 与容器 MenuGroup 实现同一个 MenuNode 接口，
 * 调用方对「单个对象」和「整棵树」用同一句 render()，靠递归统一处理。
 */
public class P42Composite {

    interface MenuNode {
        void render(int depth);
    }

    static class MenuItem implements MenuNode {
        private final String name;
        MenuItem(String name) { this.name = name; }
        public void render(int depth) {
            System.out.println("  ".repeat(depth) + "- " + name);
        }
    }

    static class MenuGroup implements MenuNode {
        private final String name;
        private final List<MenuNode> children = new ArrayList<>();
        MenuGroup(String name) { this.name = name; }
        void add(MenuNode node) { children.add(node); }

        public void render(int depth) {
            System.out.println("  ".repeat(depth) + "+ " + name);
            for (MenuNode child : children) {
                child.render(depth + 1);   // 递归：组合模式的核心
            }
        }
    }

    public static void main(String[] args) {
        MenuGroup root = new MenuGroup("后台管理");
        MenuGroup order = new MenuGroup("订单管理");
        order.add(new MenuItem("订单列表"));
        order.add(new MenuItem("退款审核"));
        MenuGroup goods = new MenuGroup("商品管理");
        goods.add(new MenuItem("商品列表"));
        MenuGroup category = new MenuGroup("类目管理");
        category.add(new MenuItem("类目树"));
        goods.add(category);                 // 容器里再套容器
        root.add(order);
        root.add(goods);
        root.add(new MenuItem("系统设置"));

        System.out.println("调用方只调 root.render(0)，整棵树自己递归渲染：");
        root.render(0);

        System.out.println();
        System.out.println("结论：出现「整体—部分」树形结构且需要递归处理时用组合；");
        System.out.println("      树形权限校验、部门人数汇总都是同一套变体。");
    }
}
