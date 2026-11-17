package patterns;

/**
 * 1.2 抽象工厂 Abstract Factory —— 可运行版（对应正文 1.2 / 图 1.2）
 *
 * 观察重点：
 *   1) 一个工厂产出「一族」相关产品（上传器 + 下载器）；
 *   2) 换工厂 = 整族切换，调用方完全不感知厂商；
 *   3) 族内产品互相配套（示例中共享同一套认证信息）。
 *
 * 与工厂方法的区别：工厂方法产「一个」产品，抽象工厂产「一族」产品。
 */
public class P12AbstractFactory {

    interface Uploader   { void upload(String fileName); }
    interface Downloader { void download(String fileName); }

    interface OssClientFactory {
        Uploader createUploader();
        Downloader createDownloader();
    }

    // ===== 阿里云一族：共享同一套认证 =====
    static class AliyunOssFactory implements OssClientFactory {
        private final String credential = "aliyun-ak";
        public Uploader createUploader()     { return new AliyunUploader(credential); }
        public Downloader createDownloader() { return new AliyunDownloader(credential); }
    }
    static class AliyunUploader implements Uploader {
        private final String credential;
        AliyunUploader(String c) { this.credential = c; }
        public void upload(String f) { System.out.println("  阿里云 OSS 上传 " + f + "（认证=" + credential + "）"); }
    }
    static class AliyunDownloader implements Downloader {
        private final String credential;
        AliyunDownloader(String c) { this.credential = c; }
        public void download(String f) { System.out.println("  阿里云 OSS 下载 " + f + "（认证=" + credential + "）"); }
    }

    // ===== 腾讯云一族 =====
    static class TencentCosFactory implements OssClientFactory {
        private final String credential = "tencent-secret";
        public Uploader createUploader()     { return new CosUploader(credential); }
        public Downloader createDownloader() { return new CosDownloader(credential); }
    }
    static class CosUploader implements Uploader {
        private final String credential;
        CosUploader(String c) { this.credential = c; }
        public void upload(String f) { System.out.println("  腾讯云 COS 上传 " + f + "（认证=" + credential + "）"); }
    }
    static class CosDownloader implements Downloader {
        private final String credential;
        CosDownloader(String c) { this.credential = c; }
        public void download(String f) { System.out.println("  腾讯云 COS 下载 " + f + "（认证=" + credential + "）"); }
    }

    // ===== 调用方：只依赖抽象工厂 =====
    static class ObjectStorageService {
        private final OssClientFactory factory;
        ObjectStorageService(OssClientFactory factory) { this.factory = factory; }
        void store(String fileName) {
            factory.createUploader().upload(fileName);
        }
        void fetch(String fileName) {
            factory.createDownloader().download(fileName);
        }
    }

    public static void main(String[] args) {
        System.out.println("切换到阿里云产品族：");
        new ObjectStorageService(new AliyunOssFactory()).store("order.pdf");
        new ObjectStorageService(new AliyunOssFactory()).fetch("order.pdf");

        System.out.println("切换到腾讯云产品族（调用方代码一字未改）：");
        ObjectStorageService cos = new ObjectStorageService(new TencentCosFactory());
        cos.store("order.pdf");
        cos.fetch("order.pdf");

        System.out.println();
        System.out.println("代价：新增「产品种类」（比如再加 CDN 刷新器）要改工厂接口和全部实现；");
        System.out.println("      新增「产品族」（比如 AWS S3）只需加一套实现类。产品种类稳定时才值得用。");
    }
}
