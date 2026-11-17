package patterns;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 6.3 命令 Command —— 可运行版（对应正文 6.3 / 图 6.3）
 *
 * 正文只演示了「排队执行」；这里再补上定义里提到的 undo（撤销），
 * 因为「动作被封装成对象」最大的价值正是能存、能传、能延迟、能回滚。
 *
 * 正文要点：ArrayDeque 不是线程安全队列，生产跨线程请用 BlockingQueue / 线程池。
 */
public class P63Command {

    static class StockService {
        private int stock = 10;
        void deduct(long skuId, int count) {
            stock -= count;
            System.out.println("    扣减 sku=" + skuId + " × " + count + "，剩余库存 = " + stock);
        }
        void refund(long skuId, int count) {
            stock += count;
            System.out.println("    回滚 sku=" + skuId + " × " + count + "，剩余库存 = " + stock);
        }
        int stock() { return stock; }
    }

    interface Command {
        void execute();
        default void undo() { throw new UnsupportedOperationException("该命令不支持撤销"); }
    }

    static class DeductStockCommand implements Command {
        private final StockService stockService;
        private final long skuId;
        private final int count;
        DeductStockCommand(StockService s, long skuId, int count) {
            this.stockService = s; this.skuId = skuId; this.count = count;
        }
        public void execute() { stockService.deduct(skuId, count); }
        public void undo()    { stockService.refund(skuId, count); }
    }

    // 调用者：解耦「发起动作」与「执行动作」，可排队、可延迟、可撤销
    static class CommandQueue {
        private final Deque<Command> queue = new ArrayDeque<>();
        private final List<Command> history = new ArrayList<>();
        void submit(Command c) { queue.offer(c); }
        void run() {
            while (!queue.isEmpty()) {
                Command c = queue.poll();
                c.execute();
                history.add(c);
            }
        }
        void undoLast() {
            if (history.isEmpty()) return;
            history.remove(history.size() - 1).undo();
        }
    }

    public static void main(String[] args) {
        StockService stock = new StockService();
        CommandQueue queue = new CommandQueue();

        System.out.println("把 3 个扣库存动作先提交、后统一执行：");
        queue.submit(new DeductStockCommand(stock, 1001L, 2));
        queue.submit(new DeductStockCommand(stock, 1002L, 3));
        queue.submit(new DeductStockCommand(stock, 1003L, 1));
        queue.run();
        System.out.println("  执行后库存 = " + stock.stock());

        System.out.println("\n撤销最后一个命令：");
        queue.undoLast();
        System.out.println("  撤销后库存 = " + stock.stock());

        System.out.println("\n结论：Runnable / Job / MQ 消息本质上都是命令对象；");
        System.out.println("      生产环境请用线程安全队列，并明确拒绝策略、幂等和失败重试。");
    }
}
