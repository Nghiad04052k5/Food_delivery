package com.delivery.view;

import com.delivery.model.SimulationRun;
import com.delivery.repository.SimulationRunRepository;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * ReportView - Giao diện Console xuất báo cáo kết quả mô phỏng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - In báo cáo tổng hợp lịch sử mô phỏng (summary)
 * - In báo cáo chi tiết từng lần chạy (detailed)
 * - [TUẦN 8] In bảng so sánh 8 cột giữa 4 cơ chế khóa (NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC)
 * - Lưu kết quả lần chạy mới vào file CSV
 */
public class ReportView {

    // ANSI color codes
    private static final String RESET   = "\u001B[0m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";
    private static final String BOLD    = "\u001B[1m";
    private static final String MAGENTA = "\u001B[35m";

    private final SimulationRunRepository simulationRunRepository;

    public ReportView(String simulationFilePath) {
        this.simulationRunRepository = new SimulationRunRepository(simulationFilePath);
    }

    // =========================================================
    // [TUẦN 8 - Tuấn] BẢNG SO SÁNH 4 CƠ CHẾ KHÓA (8 cột)
    // =========================================================

    /**
     * In bảng so sánh hiệu năng và độ an toàn của 4 cơ chế khóa.
     * Mỗi hàng = 1 kịch bản, 8 cột đo lường khác nhau.
     * Được gọi sau khi SimulatorController đã chạy xong 4 kịch bản.
     */
    public void printLockComparisonTable(List<SimulationRun> runs) {
        System.out.println();
        String sep = repeatChar('═', 110);
        System.out.println(CYAN + BOLD + "╔" + sep + "╗" + RESET);
        System.out.println(CYAN + "║" + BOLD + centerText("SO SANH HIEU NANG 4 CO CHE KHOA - KET QUA MO PHONG DA LUONG", 110) + CYAN + "║" + RESET);
        System.out.println(CYAN + BOLD + "╠" + sep + "╣" + RESET);

        // Header 8 cột
        System.out.printf(CYAN + "║" + RESET + BOLD
                + " %-14s | %-6s | %-7s | %-7s | %-9s | %-7s | %-10s | %-8s "
                + CYAN + "║" + RESET + "%n",
                "CO CHE KHOA", "TONG", "THANH", "THAT", "THPUT(d/s)", "TY LE%",
                "ERR:DA/OS/DL", "TGIAN(ms)");

        System.out.println(CYAN + BOLD + "╠" + sep + "╣" + RESET);

        if (runs == null || runs.isEmpty()) {
            System.out.println(CYAN + "║" + RESET + RED + "  Chua co du lieu mo phong. Hay chay SimulatorController truoc!"
                    + repeatChar(' ', 52) + CYAN + "║" + RESET);
        } else {
            // Nhóm các run theo cơ chế, lấy run mới nhất của mỗi cơ chế
            Map<String, SimulationRun> latestByMechanism = new LinkedHashMap<>();
            String[] order = {"NO_LOCK", "SYNCHRONIZED", "FILE_LOCK", "OPTIMISTIC"};
            for (String mech : order) latestByMechanism.put(mech, null);

            for (SimulationRun run : runs) {
                String m = run.getLockMechanism();
                if (m == null) m = "NO_LOCK";
                latestByMechanism.put(m, run); // ghi đè → lấy cái mới nhất
            }

            for (String mech : order) {
                SimulationRun r = latestByMechanism.get(mech);
                if (r == null) {
                    System.out.printf(CYAN + "║" + RESET
                            + " %-14s | %-6s | %-7s | %-7s | %-9s | %-7s | %-10s | %-8s "
                            + CYAN + "║" + RESET + "%n",
                            mech, "-", "-", "-", "-", "-", "-/-/-", "-");
                } else {
                    String rateColor   = r.getSuccessRate() >= 99 ? GREEN  : (r.getSuccessRate() >= 90 ? YELLOW : RED);
                    String errorStr    = r.getErrorDoubleAssign() + "/" + r.getErrorOversell() + "/" + r.getErrorDriverOverload();
                    String errorColor  = r.getTotalErrors() == 0 ? GREEN : RED;

                    System.out.printf(CYAN + "║" + RESET
                            + " %-14s | %-6d | %-7d | %-7d | %-9s | %s%-6.1f%%%s | %s%-10s%s | %-8d "
                            + CYAN + "║" + RESET + "%n",
                            mech,
                            r.getTotalOrders(),
                            r.getTotalSuccess(),
                            r.getTotalFailed(),
                            String.format("%.1f", r.getThroughput()),
                            rateColor, r.getSuccessRate(), RESET,
                            errorColor, errorStr, RESET,
                            r.getDurationMs());
                }
            }
        }

        System.out.println(CYAN + BOLD + "╠" + sep + "╣" + RESET);

        // Chú thích cột
        System.out.println(CYAN + "║" + RESET + BOLD + " CHU THICH:" + RESET);
        System.out.println(CYAN + "║" + RESET + "  THPUT=Throughput(don/giay)  |  TY LE%=Ty le thanh cong  |  ERR:DA/OS/DL=DoubleAssign/Oversell/DriverOverload");
        System.out.println(CYAN + "║" + RESET + "  " + GREEN + "XANH" + RESET + "=An toan (0 loi)  |  " + YELLOW + "VANG" + RESET + "=Can kiem tra  |  " + RED + "DO" + RESET + "=Nguy hiem!");
        System.out.println(CYAN + BOLD + "╚" + sep + "╝" + RESET);
        System.out.println();
    }

    /**
     * Phiên bản đơn giản: Đọc từ file CSV và in bảng so sánh tự động.
     */
    public void printLockComparisonTableFromFile() {
        List<SimulationRun> runs = simulationRunRepository.readAll();
        printLockComparisonTable(runs);
    }

    // =========================================================
    // IN BÁO CÁO CHI TIẾT (từng lần chạy)
    // =========================================================

    /**
     * In báo cáo chi tiết từng lần chạy mô phỏng.
     */
    public void printDetailedReport() {
        List<SimulationRun> runs = simulationRunRepository.readAll();

        printBanner("BAO CAO CHI TIET LICH SU MO PHONG");

        if (runs.isEmpty()) {
            System.out.println(RED + "  Chua co du lieu lich su mo phong nao." + RESET);
            return;
        }

        String separator = repeatChar('─', 90);
        System.out.println(YELLOW + separator + RESET);
        System.out.printf(BOLD + "%-5s | %-14s | %-6s | %-7s | %-7s | %-9s | %-7s | %-12s%n" + RESET,
                "ID", "CO CHE KHOA", "TONG", "THANH", "THAT", "THPUT(d/s)", "TY LE%", "TGIAN(ms)");
        System.out.println(YELLOW + separator + RESET);

        for (SimulationRun run : runs) {
            String rateColor  = run.getSuccessRate() >= 90 ? GREEN : (run.getSuccessRate() >= 70 ? YELLOW : RED);
            String mech       = run.getLockMechanism() != null ? run.getLockMechanism() : "NO_LOCK";

            System.out.printf("%-5d | %-14s | %-6d | %-7d | %-7d | %-9.1f | %s%-6.1f%%%s | %-12d%n",
                    run.getId(),
                    mech,
                    run.getTotalOrders(),
                    run.getTotalSuccess(),
                    run.getTotalFailed(),
                    run.getThroughput(),
                    rateColor, run.getSuccessRate(), RESET,
                    run.getDurationMs());
        }
        System.out.println(YELLOW + separator + RESET);
    }

    // =========================================================
    // IN BÁO CÁO TỔNG HỢP (Summary)
    // =========================================================

    /**
     * In báo cáo tổng hợp (summary) toàn bộ lịch sử mô phỏng.
     */
    public void printSummaryReport() {
        List<SimulationRun> runs = simulationRunRepository.readAll();

        printBanner("BAO CAO TONG HOP HE THONG");

        if (runs.isEmpty()) {
            System.out.println(RED + "  Chua co du lieu lich su mo phong nao." + RESET);
            return;
        }

        int totalRuns     = runs.size();
        long totalOrders  = runs.stream().mapToLong(SimulationRun::getTotalOrders).sum();
        long totalSuccess = runs.stream().mapToLong(SimulationRun::getTotalSuccess).sum();
        long totalFailed  = runs.stream().mapToLong(SimulationRun::getTotalFailed).sum();
        long totalDuration = runs.stream().mapToLong(SimulationRun::getDurationMs).sum();
        int totalErrors   = runs.stream().mapToInt(SimulationRun::getTotalErrors).sum();
        double avgSuccessRate = totalOrders == 0 ? 0 : (totalSuccess * 100.0 / totalOrders);
        double avgDuration   = (double) totalDuration / totalRuns;
        double avgThroughput = runs.stream().mapToDouble(SimulationRun::getThroughput).average().orElse(0);

        String rateColor  = avgSuccessRate >= 90 ? GREEN : (avgSuccessRate >= 70 ? YELLOW : RED);
        String errorColor = totalErrors == 0 ? GREEN : RED;

        String separator = repeatChar('═', 50);
        System.out.println(CYAN + separator + RESET);
        System.out.printf(BOLD + "  %-32s: %d lan%n" + RESET,    "So lan mo phong", totalRuns);
        System.out.printf(BOLD + "  %-32s: %d don%n" + RESET,    "Tong so don hang xu ly", totalOrders);
        System.out.printf(BOLD + "  %-32s: " + GREEN + "%d don%n" + RESET, "Tong don THANH CONG", totalSuccess);
        System.out.printf(BOLD + "  %-32s: " + RED + "%d don%n" + RESET,   "Tong don THAT BAI", totalFailed);
        System.out.printf(BOLD + "  %-32s: %s%d loi%n" + RESET,  "Tong loi Race Condition", errorColor, totalErrors);
        System.out.printf(BOLD + "  %-32s: %s%.2f%%%n" + RESET,  "Ty le thanh cong trung binh", rateColor, avgSuccessRate);
        System.out.printf(BOLD + "  %-32s: %.1f don/s%n" + RESET, "Throughput TB", avgThroughput);
        System.out.printf(BOLD + "  %-32s: %.1f ms%n" + RESET,   "Thoi gian chay TB moi lan", avgDuration);
        System.out.println(CYAN + separator + RESET);

        System.out.print("\n  " + BOLD + "Danh gia: " + RESET);
        if (avgSuccessRate >= 95 && totalErrors == 0) {
            System.out.println(GREEN + "XUAT SAC - He thong an toan va on dinh cao!" + RESET);
        } else if (avgSuccessRate >= 85) {
            System.out.println(YELLOW + "TOT - He thong hoat dong on dinh." + RESET);
        } else if (avgSuccessRate >= 70) {
            System.out.println(YELLOW + "TRUNG BINH - Can kiem tra lai co che Lock." + RESET);
        } else {
            System.out.println(RED + "KEM - He thong co van de nghiem trong! Nen dung co che OPTIMISTIC hoac SYNCHRONIZED." + RESET);
        }
    }

    /**
     * In báo cáo đầy đủ: summary + chi tiết + bảng so sánh.
     */
    public void printFullReport() {
        printSummaryReport();
        System.out.println();
        printDetailedReport();
        System.out.println();
        printLockComparisonTableFromFile();
    }

    // =========================================================
    // LƯU KẾT QUẢ MỚI
    // =========================================================

    /**
     * Lưu kết quả một lần chạy mô phỏng mới vào file.
     * Phiên bản cũ (tương thích ngược - dùng cho Tuần 5-6).
     */
    public void saveSimulationResult(int totalOrders, int totalSuccess, int totalFailed, long durationMs) {
        saveSimulationResult(totalOrders, totalSuccess, totalFailed, durationMs, "NO_LOCK", 0, 0, 0);
    }

    /**
     * Lưu kết quả một lần chạy mô phỏng mới vào file.
     * Phiên bản đầy đủ Tuần 7-8 với thông tin cơ chế khóa và lỗi.
     */
    public void saveSimulationResult(int totalOrders, int totalSuccess, int totalFailed, long durationMs,
                                     String lockMechanism,
                                     int errorDoubleAssign, int errorOversell, int errorDriverOverload) {
        List<SimulationRun> all = simulationRunRepository.readAll();
        int newId = all.stream().mapToInt(SimulationRun::getId).max().orElse(0) + 1;

        SimulationRun newRun = new SimulationRun(newId, totalOrders, totalSuccess, totalFailed, durationMs,
                lockMechanism, errorDoubleAssign, errorOversell, errorDriverOverload);
        simulationRunRepository.save(newRun);

        System.out.println(GREEN + "\n==> [ReportView] Da luu ket qua mo phong #" + newId
                + " [" + lockMechanism + "] (" + totalSuccess + "/" + totalOrders + " don thanh cong"
                + ", Loi: " + newRun.getTotalErrors() + ")" + RESET);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return repeatChar(' ', padding) + text + repeatChar(' ', width - text.length() - padding);
    }

    private void printBanner(String title) {
        int width   = 50;
        int padding = (width - title.length() - 2) / 2;
        String pad  = repeatChar(' ', Math.max(0, padding));
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + repeatChar('═', width) + "╗");
        System.out.println("║" + pad + " " + title + " " + pad + " ║");
        System.out.println("╚" + repeatChar('═', width) + "╝" + RESET);
        System.out.println();
    }
}
