package patterns;

/**
 * 一键跑完 17 个模式的可运行示例。
 * 单独跑某一个：java -cp out patterns.P51Strategy
 */
public class RunAll {

    private static final Class<?>[] DEMOS = {
            P11FactoryMethod.class,
            P12AbstractFactory.class,
            P13Singleton.class,
            P21Builder.class,
            P22Prototype.class,
            P31Adapter.class,
            P32Decorator.class,
            P33Proxy.class,
            P41Facade.class,
            P42Composite.class,
            P43Bridge.class,
            P51Strategy.class,
            P52Observer.class,
            P53TemplateMethod.class,
            P61ChainOfResponsibility.class,
            P62State.class,
            P63Command.class
    };

    public static void main(String[] args) throws Exception {
        int ok = 0, fail = 0;
        for (int i = 0; i < DEMOS.length; i++) {
            Class<?> c = DEMOS[i];
            String name = c.getSimpleName();
            System.out.println();
            System.out.println("================================================================");
            System.out.printf("  [%02d/%02d] %s%n", i + 1, DEMOS.length, name);
            System.out.println("================================================================");
            try {
                c.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                ok++;
            } catch (java.lang.reflect.InvocationTargetException e) {
                fail++;
                Throwable cause = e.getCause();
                System.out.println("  !! 运行失败：" + cause);
                cause.printStackTrace(System.out);
            }
        }
        System.out.println();
        System.out.println("================================================================");
        System.out.printf("  完成：%d 个成功 / %d 个失败（共 %d 个模式示例）%n", ok, fail, DEMOS.length);
        System.out.println("================================================================");
        if (fail > 0) System.exit(1);
    }
}
