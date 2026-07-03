package com.delivery.view;

import com.delivery.model.SimulationRun;
import com.delivery.repository.SimulationRunRepository;
import java.util.List;

/**
 * ReportView - Giao diện Console xuất báo cáo kết quả mô phỏng.
 *
 * Nhiệm vụ (Thành viên 3 - Tuấn):
 * - Đọc dữ liệu lịch sử mô phỏng từ SimulationRunRepository
 * - In báo cáo tổng hợp và chi tiết từng lần chạy ra Console
 * - Tính tỷ lệ thành công và thất bại trung bình
 */
public class ReportView {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";

    private final SimulationRunRepository simulationRunRepository;

    public ReportView(String simulationFilePath) {
        this.simulationRunRepository = new SimulationRunRepository(simulationFilePath);
    }

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

        // Header bảng
        String separator = repeatChar('─', 78);
        System.out.println(YELLOW + separator + RESET);
        System.out.printf(BOLD + "%-6s | %-12s | %-10s | %-10s | %-12s | %-8s%n" + RESET,
                "ID", "TONG DON", "THANH CONG", "THAT BAI", "THOI GIAN(ms)", "TY LE %");
        System.out.println(YELLOW + separator + RESET);

        for (SimulationRun run : runs) {
            double successRate = run.getTotalOrders() == 0 ? 0
                    : (run.getTotalSuccess() * 100.0 / run.getTotalOrders());
            String rateColor = successRate >= 90 ? GREEN : (successRate >= 70 ? YELLOW : RED);

            System.out.printf("%-6d | %-12d | %-10d | %-10d | %-13d | %s%.1f%%%s%n",
                    run.getId(),
                    run.getTotalOrders(),
                    run.getTotalSuccess(),
                    run.getTotalFailed(),
                    run.getDurationMs(),
                    rateColor, successRate, RESET);
        }
        System.out.println(YELLOW + separator + RESET);
    }

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

        int totalRuns    = runs.size();
        long totalOrders   = runs.stream().mapToLong(SimulationRun::getTotalOrders).sum();
        long totalSuccess  = runs.stream().mapToLong(SimulationRun::getTotalSuccess).sum();
        long totalFailed   = runs.stream().mapToLong(SimulationRun::getTotalFailed).sum();
        long totalDuration = runs.stream().mapToLong(SimulationRun::getDurationMs).sum();
        double avgSuccessRate = totalOrders == 0 ? 0 : (totalSuccess * 100.0 / totalOrders);
        double avgDuration = (double) totalDuration / totalRuns;

        String rateColor = avgSuccessRate >= 90 ? GREEN : (avgSuccessRate >= 70 ? YELLOW : RED);

        String separator = repeatChar('═', 50);
        System.out.println(CYAN + separator + RESET);
        System.out.printf(BOLD + "  %-30s: %d lan%n" + RESET,   "So lan mo phong", totalRuns);
        System.out.printf(BOLD + "  %-30s: %d don%n" + RESET,   "Tong so don hang xu ly", totalOrders);
        System.out.printf(BOLD + "  %-30s: " + GREEN + "%d don%n" + RESET,
                "Tong don THANH CONG", totalSuccess);
        System.out.printf(BOLD + "  %-30s: " + RED + "%d don%n" + RESET,
                "Tong don THAT BAI", totalFailed);
        System.out.printf(BOLD + "  %-30s: %s%.2f%%%n" + RESET,
                "Ty le thanh cong trung binh", rateColor, avgSuccessRate);
        System.out.printf(BOLD + "  %-30s: %.1f ms%n" + RESET,
                "Thoi gian chay TB moi lan", avgDuration);
        System.out.println(CYAN + separator + RESET);

        // Đánh giá chất lượng hệ thống
        System.out.print("\n  " + BOLD + "Danh gia: " + RESET);
        if (avgSuccessRate >= 95) {
            System.out.println(GREEN + "XUAT SAC - He thong on dinh cao!" + RESET);
        } else if (avgSuccessRate >= 85) {
            System.out.println(YELLOW + "TOT - He thong hoat dong on dinh." + RESET);
        } else if (avgSuccessRate >= 70) {
            System.out.println(YELLOW + "TRUNG BINH - Can kiem tra lai co che Lock." + RESET);
        } else {
            System.out.println(RED + "KEM - He thong co van de nghiem trong!" + RESET);
        }
    }

    /**
     * In báo cáo đầy đủ: summary + chi tiết.
     */
    public void printFullReport() {
        printSummaryReport();
        System.out.println();
        printDetailedReport();
    }

    /**
     * Lưu kết quả một lần chạy mô phỏng mới vào file.
     */
    public void saveSimulationResult(int totalOrders, int totalSuccess, int totalFailed, long durationMs) {
        List<SimulationRun> all = simulationRunRepository.readAll();
        int newId = all.stream().mapToInt(SimulationRun::getId).max().orElse(0) + 1;

        SimulationRun newRun = new SimulationRun(newId, totalOrders, totalSuccess, totalFailed, durationMs);
        simulationRunRepository.save(newRun);

        System.out.println(GREEN + "\n==> [ReportView] Da luu ket qua mo phong #" + newId
                + " (" + totalSuccess + "/" + totalOrders + " don thanh cong)" + RESET);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private void printBanner(String title) {
        int width = 50;
        int padding = (width - title.length() - 2) / 2;
        String pad = repeatChar(' ', Math.max(0, padding));
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + repeatChar('═', width) + "╗");
        System.out.println("║" + pad + " " + title + " " + pad + " ║");
        System.out.println("╚" + repeatChar('═', width) + "╝" + RESET);
        System.out.println();
    }
}
