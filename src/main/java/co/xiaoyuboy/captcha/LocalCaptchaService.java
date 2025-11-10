package co.xiaoyuboy.captcha;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author smile_liuyu@qq.com @Smile
 * @description
 * @create 2025/11/10 15:19
 */
@Slf4j
public class LocalCaptchaService {

    private OrtEnvironment environment;
    private Map<Integer, String> idxToChar;
    private byte[] modelBytes;

    private static final int CORE_POOL_SIZE = 16;
    private static final int MAX_POOL_SIZE = 64;

    private final BlockingQueue<OrtSession> sessionQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);
    private final AtomicInteger activeCount = new AtomicInteger(0);

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong queueFullCount = new AtomicLong(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);

    private static LocalCaptchaService instance;
    private OrtSession.SessionOptions sessionOptions;

    @PostConstruct
    public void init() {
        try {
            instance = this;
            log.info("==========================================");
            log.info("初始化高性能验证码识别服务");
            log.info("CPU核心数: {}", Runtime.getRuntime().availableProcessors());
            log.info("核心Session池: {}", CORE_POOL_SIZE);
            log.info("最大Session池: {}", MAX_POOL_SIZE);
            log.info("==========================================");

            loadConfig();
            initONNX();
            createSessions(CORE_POOL_SIZE);
            warmup();
            startMonitorThread();

            log.info("✅ 验证码服务初始化完成，当前Session数: {}", currentPoolSize.get());
        } catch (Exception e) {
            log.error("验证码服务初始化失败", e);
            throw new RuntimeException("验证码服务初始化失败", e);
        }
    }

    public static LocalCaptchaService getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        return environment != null && currentPoolSize.get() > 0;
    }

    private void loadConfig() throws Exception {
        ClassPathResource configResource = new ClassPathResource("captcha/config.json");
        try (InputStream is = configResource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> config = mapper.readValue(is, Map.class);

            idxToChar = new HashMap<>();
            Map<String, Object> idxMap = (Map<String, Object>) config.get("idx_to_char");
            idxMap.forEach((k, v) -> idxToChar.put(Integer.parseInt(k), v.toString()));

            log.info("字符集大小: {}", idxToChar.size());
        }
    }

    private void initONNX() throws Exception {
        environment = OrtEnvironment.getEnvironment();

        ClassPathResource modelResource = new ClassPathResource("captcha/captcha_cpu_int8.onnx");
        try (InputStream is = modelResource.getInputStream()) {
            modelBytes = is.readAllBytes();
        }
        log.info("模型大小: {} KB", modelBytes.length / 1024);

        sessionOptions = new OrtSession.SessionOptions();
        sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        sessionOptions.setIntraOpNumThreads(1);
        sessionOptions.setInterOpNumThreads(1);
        sessionOptions.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL);
        sessionOptions.setMemoryPatternOptimization(true);
    }

    private void createSessions(int count) {
        for (int i = 0; i < count; i++) {
            try {
                OrtSession session = environment.createSession(modelBytes, sessionOptions);
                sessionQueue.offer(session);
                currentPoolSize.incrementAndGet();
            } catch (Exception e) {
                log.error("创建Session失败", e);
            }
        }
    }

    private void expandPool() {
        int current = currentPoolSize.get();
        if (current < MAX_POOL_SIZE) {
            int toCreate = Math.min(8, MAX_POOL_SIZE - current);
            log.info("动态扩容Session池: {} -> {}", current, current + toCreate);
            createSessions(toCreate);
        }
    }

    private void shrinkPool() {
        int current = currentPoolSize.get();
        if (current > CORE_POOL_SIZE && sessionQueue.size() > CORE_POOL_SIZE / 2) {
            int toRemove = Math.min(4, current - CORE_POOL_SIZE);
            for (int i = 0; i < toRemove; i++) {
                OrtSession session = sessionQueue.poll();
                if (session != null) {
                    try {
                        session.close();
                        currentPoolSize.decrementAndGet();
                    } catch (Exception e) {
                        log.error("关闭Session失败", e);
                    }
                }
            }
            log.info("动态缩容Session池: {} -> {}", current, currentPoolSize.get());
        }
    }

    private void warmup() throws Exception {
        log.info("开始预热...");
        float[][][][] dummy = new float[1][1][64][192];

        int warmupCount = Math.min(currentPoolSize.get() / 2, 5);
        for (int i = 0; i < warmupCount; i++) {
            OrtSession session = sessionQueue.poll(100, TimeUnit.MILLISECONDS);
            if (session != null) {
                try {
                    var inputTensor = OnnxTensor.createTensor(environment, dummy);
                    try (var results = session.run(Map.of("input", inputTensor))) {
                        // 预热
                    }
                    inputTensor.close();
                } finally {
                    sessionQueue.offer(session);
                }
            }
        }
        log.info("预热完成");
    }

    /**
     * 识别验证码并返回计算结果（JSON响应）
     * DreamHttpUtil会调用这个方法
     */
    public JSONObject recognizeWithJsonResponse(byte[] imageBytes) {
        JSONObject response = new JSONObject();
        long startTime = System.nanoTime();

        try {
            String result = recognize(imageBytes);  // 直接返回计算结果
            double totalMs = (System.nanoTime() - startTime) / 1_000_000.0;

            response.put("calculationResult", result);  // 这里是计算结果，不是表达式
            response.put("success", true);
            response.put("totalMs", String.format("%.2f", totalMs));

            long requests = totalRequests.incrementAndGet();
            if (requests % 100 == 0) {
                printStatistics();
            }

        } catch (Exception e) {
            log.error("识别失败", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 识别验证码并返回计算结果
     * @param imageBytes 图片字节
     * @return 计算结果（如输入"3+5"返回"8"）
     */
    public String recognize(byte[] imageBytes) throws Exception {
        String expression = recognizeExpression(imageBytes);
        String result = calculateExpression(expression);
        return result;
    }

    /**
     * 仅识别表达式，不计算
     * @param imageBytes 图片字节
     * @return 表达式字符串（如"3+5"）
     */
    public String recognizeExpression(byte[] imageBytes) throws Exception {
        // 添加输入验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("输入图片不能为空");
        }

        long waitStart = System.nanoTime();

        OrtSession session = sessionQueue.poll();

        if (session == null) {
            int active = activeCount.get();
            int poolSize = currentPoolSize.get();

            if (active >= poolSize * 0.8 && poolSize < MAX_POOL_SIZE) {
                expandPool();
            }

            session = sessionQueue.poll(1000, TimeUnit.MILLISECONDS);

            if (session == null) {
                queueFullCount.incrementAndGet();
                throw new RuntimeException("Session池耗尽，当前池大小: " + poolSize + ", 活跃: " + active);
            }
        }

        long waitTime = System.nanoTime() - waitStart;
        totalWaitTime.addAndGet(waitTime);

        activeCount.incrementAndGet();

        try {
            float[][][][] input = preprocessImage(imageBytes);

            var inputTensor = OnnxTensor.createTensor(environment, input);

            String result;
            try (OrtSession.Result results = session.run(Map.of("input", inputTensor))) {
                var output = (float[][][]) results.get(0).getValue();
                result = ctcDecode(output);
            } finally {
                inputTensor.close();
            }

            return result;

        } finally {
            activeCount.decrementAndGet();
            sessionQueue.offer(session);
        }
    }

    /**
     * 计算数学表达式（纯Java实现，最快速度）
     * @param expression 表达式（支持 +, -, ×, *, ÷, / ）
     * @return 计算结果字符串
     */
    public String calculateExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "0";
        }

        try {
            // 清理和标准化表达式
            String cleaned = expression
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("加", "+")
                    .replace("减", "-")
                    .replace("乘", "*")
                    .replace("除", "/")
                    .replaceAll("\\s+", "")  // 移除所有空格
                    .trim();

            // 简单四则运算解析
            int result = 0;

            // 处理加法
            if (cleaned.contains("+")) {
                String[] parts = cleaned.split("\\+");
                if (parts.length == 2) {
                    int a = parseNumber(parts[0]);
                    int b = parseNumber(parts[1]);
                    result = a + b;
                    return String.valueOf(result);
                }
            }

            // 处理减法
            if (cleaned.contains("-") && !cleaned.startsWith("-")) {
                String[] parts = cleaned.split("-");
                if (parts.length == 2) {
                    int a = parseNumber(parts[0]);
                    int b = parseNumber(parts[1]);
                    result = a - b;
                    return String.valueOf(result);
                }
            }

            // 处理乘法
            if (cleaned.contains("*")) {
                String[] parts = cleaned.split("\\*");
                if (parts.length == 2) {
                    int a = parseNumber(parts[0]);
                    int b = parseNumber(parts[1]);
                    result = a * b;
                    return String.valueOf(result);
                }
            }

            // 处理除法
            if (cleaned.contains("/")) {
                String[] parts = cleaned.split("/");
                if (parts.length == 2) {
                    int a = parseNumber(parts[0]);
                    int b = parseNumber(parts[1]);
                    if (b != 0) {
                        result = a / b;
                        return String.valueOf(result);
                    }
                }
            }

            // 如果不是表达式，尝试直接解析为数字
            return String.valueOf(parseNumber(cleaned));

        } catch (Exception e) {
            log.error("计算表达式失败: {} - {}", expression, e.getMessage());
            return "0";
        }
    }

    /**
     * 安全地解析数字
     */
    private int parseNumber(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void startMonitorThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "captcha-monitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                int active = activeCount.get();
                int poolSize = currentPoolSize.get();
                int queueSize = sessionQueue.size();

                if (queueSize > poolSize * 0.7 && poolSize > CORE_POOL_SIZE) {
                    shrinkPool();
                }

                // 只在有问题时才记录
                if (active > poolSize * 0.9) {
                    log.warn("Session池压力大: 活跃{}/{}", active, poolSize);
                }
            } catch (Exception e) {
                log.error("监控线程异常", e);
            }
        }, 30, 60, TimeUnit.SECONDS);  // 改为30秒检查一次
    }

    private static final long LOG_EVERY = 1_000;
    private void printStatistics() {
        long requests = totalRequests.get();          // 如果是LongAdder请改成 .sum()
        if (requests > 0 && requests % LOG_EVERY == 0) {
            double avgWaitMs = (totalWaitTime.get() / 1_000_000.0) / requests; // ns -> ms
            long queueFull = queueFullCount.get();    // 如果是LongAdder请改成 .sum()

            log.info("性能统计({}次): 平均等待{}ms, Session池{}/{}, 队列满:{}次",
                    requests,
                    String.format("%.2f", avgWaitMs), // 先格式化成字符串
                    currentPoolSize.get(),
                    MAX_POOL_SIZE,
                    queueFull);
        }
    }

    private float[][][][] preprocessImage(byte[] imageBytes) throws Exception {
        // 添加输入验证
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

        // 添加null检查
        if (img == null) {
            throw new IllegalArgumentException("无法读取图片数据，可能不是有效的图片格式");
        }

        float[][][][] input = new float[1][1][64][192];

        BufferedImage resized;
        if (img.getWidth() != 192 || img.getHeight() != 64) {
            resized = new BufferedImage(192, 64, BufferedImage.TYPE_BYTE_GRAY);
            resized.getGraphics().drawImage(img, 0, 0, 192, 64, null);
        } else {
            resized = img;
        }

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 192; x++) {
                int rgb = resized.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                input[0][0][y][x] = (gray / 255.0f - 0.5f) / 0.5f;
            }
        }

        return input;
    }

    private String ctcDecode(float[][][] output) {
        StringBuilder result = new StringBuilder();
        int lastIdx = -1;

        for (int t = 0; t < output[0].length; t++) {
            float[] probs = output[0][t];

            int maxIdx = 0;
            float maxVal = probs[0];

            for (int c = 1; c < probs.length; c++) {
                if (probs[c] > maxVal) {
                    maxVal = probs[c];
                    maxIdx = c;
                }
            }

            if (maxIdx != lastIdx && maxIdx != 0) {
                String ch = idxToChar.get(maxIdx);
                if (ch != null && !ch.equals("<blank>")) {
                    result.append(ch);
                }
            }
            lastIdx = maxIdx;
        }

        return result.toString();
    }

    /**
     * 批量测试文件夹中的验证码图片
     */
    public Map<String, TestResult> testFolderImages(String folderPath) {
        Map<String, TestResult> results = new LinkedHashMap<>();
        Path folder = Paths.get(folderPath);

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            log.error("文件夹不存在或不是目录: {}", folderPath);
            return results;
        }

        try {
            List<Path> imageFiles = new ArrayList<>();
            Files.list(folder)
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".png") ||
                                name.endsWith(".jpeg") || name.endsWith(".bmp");
                    })
                    .sorted()
                    .forEach(imageFiles::add);

            log.info("找到 {} 个图片文件", imageFiles.size());
            long batchStart = System.nanoTime();

            for (Path path : imageFiles) {
                try {
                    byte[] imageBytes = Files.readAllBytes(path);
                    String expression = recognizeExpression(imageBytes);
                    String result = calculateExpression(expression);

                    results.put(path.getFileName().toString(),
                            new TestResult(expression, result, true));

                    log.debug("识别: {} -> 表达式: {}, 结果: {}",
                            path.getFileName(), expression, result);

                } catch (Exception e) {
                    results.put(path.getFileName().toString(),
                            new TestResult("", "ERROR: " + e.getMessage(), false));
                    log.error("识别失败: {}", path.getFileName(), e);
                }
            }

            double totalMs = (System.nanoTime() - batchStart) / 1_000_000.0;

            int successCount = (int) results.values().stream()
                    .filter(r -> r.success)
                    .count();

            log.info("==========================================");
            log.info("批量测试完成");
            log.info("总文件数: {}, 成功: {}, 失败: {}",
                    results.size(), successCount, results.size() - successCount);
            log.info("总耗时: {:.2f} ms", totalMs);
            log.info("平均速度: {:.2f} ms/图", totalMs / results.size());
            log.info("==========================================");

        } catch (Exception e) {
            log.error("批量测试失败", e);
        }

        return results;
    }

    /**
     * 测试结果类
     */
    public static class TestResult {
        public final String expression;
        public final String result;
        public final boolean success;

        public TestResult(String expression, String result, boolean success) {
            this.expression = expression;
            this.result = result;
            this.success = success;
        }

        @Override
        public String toString() {
            return success ?
                    String.format("%s = %s", expression, result) :
                    result;
        }
    }

    @PreDestroy
    public void cleanup() throws Exception {
        printStatistics();

        for (OrtSession session : sessionQueue) {
            session.close();
        }
        if (environment != null) {
            environment.close();
        }
        log.info("验证码服务资源清理完成");
    }
}
