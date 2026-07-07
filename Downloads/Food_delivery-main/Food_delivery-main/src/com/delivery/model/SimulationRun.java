package com.delivery.model;

/**
 * SimulationRun - Model lưu kết quả một lần chạy mô phỏng đa luồng.
 *
 * Cập nhật Tuần 7-8 (Tuấn):
 * - Thêm trường lockMechanism để phân biệt 4 kịch bản: NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC
 * - Thêm errorDoubleAssign, errorOversell, errorDriverOverload để theo dõi 3 loại lỗi Race Condition
 * - Thêm throughput (don/giay) để so sanh hieu nang giua cac co che
 */
public class SimulationRun extends BaseEntity {
    private int totalOrders;
    private int totalSuccess;
    private int totalFailed;
    private long durationMs;

    // [TUẦN 7-8 - Tuấn] Cơ chế khóa dùng trong lần chạy này
    private String lockMechanism; // "NO_LOCK" | "SYNCHRONIZED" | "FILE_LOCK" | "OPTIMISTIC"

    // [TUẦN 7-8 - Tuấn] 3 loại lỗi Race Condition cần theo dõi
    private int errorDoubleAssign;   // Một đơn bị gán cho 2 tài xế khác nhau
    private int errorOversell;       // Tồn kho bị bán vượt số lượng (< 0)
    private int errorDriverOverload; // Một tài xế nhận 2 đơn cùng lúc

    // [TUẦN 7-8 - Tuấn] Throughput = số đơn xử lý thành công mỗi giây
    private double throughput;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SimulationRun() {
    }

    /** Constructor cũ (tương thích ngược với code Tuần 5-6) */
    public SimulationRun(int id, int totalOrders, int totalSuccess, int totalFailed, long durationMs) {
        this.id           = id;
        this.totalOrders  = totalOrders;
        this.totalSuccess = totalSuccess;
        this.totalFailed  = totalFailed;
        this.durationMs   = durationMs;
        this.lockMechanism      = "NO_LOCK";
        this.errorDoubleAssign  = 0;
        this.errorOversell      = 0;
        this.errorDriverOverload = 0;
        this.throughput = durationMs > 0 ? (totalSuccess * 1000.0 / durationMs) : 0;
    }

    /** Constructor đầy đủ cho Tuần 7-8 */
    public SimulationRun(int id, int totalOrders, int totalSuccess, int totalFailed, long durationMs,
                         String lockMechanism,
                         int errorDoubleAssign, int errorOversell, int errorDriverOverload) {
        this.id           = id;
        this.totalOrders  = totalOrders;
        this.totalSuccess = totalSuccess;
        this.totalFailed  = totalFailed;
        this.durationMs   = durationMs;
        this.lockMechanism       = lockMechanism;
        this.errorDoubleAssign   = errorDoubleAssign;
        this.errorOversell       = errorOversell;
        this.errorDriverOverload = errorDriverOverload;
        this.throughput = durationMs > 0 ? (totalSuccess * 1000.0 / durationMs) : 0;
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getTotalSuccess() { return totalSuccess; }
    public void setTotalSuccess(int totalSuccess) { this.totalSuccess = totalSuccess; }

    public int getTotalFailed() { return totalFailed; }
    public void setTotalFailed(int totalFailed) { this.totalFailed = totalFailed; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
        this.throughput = durationMs > 0 ? (totalSuccess * 1000.0 / durationMs) : 0;
    }

    public String getLockMechanism() { return lockMechanism; }
    public void setLockMechanism(String lockMechanism) { this.lockMechanism = lockMechanism; }

    public int getErrorDoubleAssign() { return errorDoubleAssign; }
    public void setErrorDoubleAssign(int errorDoubleAssign) { this.errorDoubleAssign = errorDoubleAssign; }

    public int getErrorOversell() { return errorOversell; }
    public void setErrorOversell(int errorOversell) { this.errorOversell = errorOversell; }

    public int getErrorDriverOverload() { return errorDriverOverload; }
    public void setErrorDriverOverload(int errorDriverOverload) { this.errorDriverOverload = errorDriverOverload; }

    public double getThroughput() { return throughput; }
    public void setThroughput(double throughput) { this.throughput = throughput; }

    /** Tổng số lỗi race condition của lần chạy này */
    public int getTotalErrors() {
        return errorDoubleAssign + errorOversell + errorDriverOverload;
    }

    /** Tỷ lệ thành công (%) */
    public double getSuccessRate() {
        return totalOrders == 0 ? 0 : (totalSuccess * 100.0 / totalOrders);
    }

    @Override
    public String toString() {
        return "SimulationRun{" +
                "id=" + id +
                ", mechanism=" + lockMechanism +
                ", totalOrders=" + totalOrders +
                ", totalSuccess=" + totalSuccess +
                ", totalFailed=" + totalFailed +
                ", durationMs=" + durationMs +
                ", throughput=" + String.format("%.1f", throughput) + " don/s" +
                ", errors(DA/OS/DL)=" + errorDoubleAssign + "/" + errorOversell + "/" + errorDriverOverload +
                '}';
    }
}
