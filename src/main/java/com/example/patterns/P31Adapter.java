package patterns;

/**
 * 3.1 适配器 Adapter —— 可运行版（对应正文 3.1 / 图 3.1）
 *
 * 第三方的 SfClient 返回 SfRoute（字段/状态码都是对方的风格），
 * 我方系统只认 LogisticsInfo。适配器负责转换，业务代码完全不接触第三方结构。
 *
 * 正文代码里的 convert() 标了「此处省略实现」，这里给出完整实现。
 */
public class P31Adapter {

    // ===== 我方统一契约 =====
    interface LogisticsQuery {
        LogisticsInfo query(String trackingNo);
    }

    record LogisticsInfo(String trackingNo, String status, String description) {
        public String toString() { return trackingNo + " | " + status + " | " + description; }
    }

    // ===== 第三方 SDK（不可修改）=====
    static class SfClient {
        SfRoute getRouteByNumber(String no) {
            // 模拟顺丰返回：状态码是人家自己定义的
            return new SfRoute("SF" + no, "80", "2026-11-04 10:30", "广州转运中心");
        }
    }
    record SfRoute(String waybillNo, String opCode, String time, String location) { }

    // ===== 对象适配器：组合持有第三方 SDK =====
    static class SfLogisticsAdapter implements LogisticsQuery {
        private final SfClient sfClient;
        SfLogisticsAdapter(SfClient sfClient) { this.sfClient = sfClient; }

        @Override
        public LogisticsInfo query(String trackingNo) {
            SfRoute route = sfClient.getRouteByNumber(trackingNo);
            return convert(route);
        }

        // 正文省略的字段映射 / 状态码翻译
        private LogisticsInfo convert(SfRoute r) {
            String status = switch (r.opCode()) {
                case "80" -> "运输中";
                case "90" -> "已签收";
                case "50" -> "已揽收";
                default   -> "未知状态(" + r.opCode() + ")";
            };
            String desc = r.time() + " " + r.location();
            return new LogisticsInfo(r.waybillNo(), status, desc);
        }
    }

    // ===== 业务代码：只依赖我方接口 =====
    static class OrderDetailService {
        private final LogisticsQuery logisticsQuery;
        OrderDetailService(LogisticsQuery q) { this.logisticsQuery = q; }
        void show(String trackingNo) {
            System.out.println("  订单物流：" + logisticsQuery.query(trackingNo));
        }
    }

    public static void main(String[] args) {
        OrderDetailService service = new OrderDetailService(new SfLogisticsAdapter(new SfClient()));
        service.show("1234567890");

        System.out.println();
        System.out.println("结论：适配器是「事后补救」——对接方不可改时，把转换收敛到一个类里；");
        System.out.println("      以后接中通只需加一个 ZtoLogisticsAdapter，业务代码不动。");
    }
}
