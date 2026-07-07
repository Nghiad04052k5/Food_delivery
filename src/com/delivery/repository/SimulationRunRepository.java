package com.delivery.repository;

import com.delivery.model.SimulationRun;
import java.util.Locale;

/**
 * SimulationRunRepository - Đọc/ghi kết quả các lần chạy mô phỏng đa luồng.
 *
 * Cập nhật Tuần 7-8 (Tuấn):
 * - Cấu trúc CSV mở rộng thêm: lock_mechanism, err_double_assign, err_oversell, err_driver_overload
 * - Tương thích ngược: Nếu file CSV cũ (5 cột) vẫn đọc được bình thường
 */
public class SimulationRunRepository extends CsvRepository<SimulationRun> {

    public SimulationRunRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected SimulationRun parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            SimulationRun run = new SimulationRun();
            run.setId(Integer.parseInt(parts[0].trim()));
            run.setTotalOrders(Integer.parseInt(parts[1].trim()));
            run.setTotalSuccess(Integer.parseInt(parts[2].trim()));
            run.setTotalFailed(Integer.parseInt(parts[3].trim()));
            run.setDurationMs(Long.parseLong(parts[4].trim()));

            // Tương thích ngược: chỉ đọc thêm trường mới nếu file CSV có đủ cột
            if (parts.length >= 9) {
                run.setLockMechanism(parts[5].trim());
                run.setErrorDoubleAssign(Integer.parseInt(parts[6].trim()));
                run.setErrorOversell(Integer.parseInt(parts[7].trim()));
                run.setErrorDriverOverload(Integer.parseInt(parts[8].trim()));
            } else {
                run.setLockMechanism("NO_LOCK");
                run.setErrorDoubleAssign(0);
                run.setErrorOversell(0);
                run.setErrorDriverOverload(0);
            }
            return run;
        }
        return null;
    }

    @Override
    protected String toCsvRow(SimulationRun entity) {
        String mechanism = entity.getLockMechanism() != null ? entity.getLockMechanism() : "NO_LOCK";
        return String.format(Locale.US, "%d,%d,%d,%d,%d,%s,%d,%d,%d",
                entity.getId(),
                entity.getTotalOrders(),
                entity.getTotalSuccess(),
                entity.getTotalFailed(),
                entity.getDurationMs(),
                mechanism,
                entity.getErrorDoubleAssign(),
                entity.getErrorOversell(),
                entity.getErrorDriverOverload()
        );
    }

    @Override
    protected String getHeader() {
        return "simulation_run_id,total_orders,total_success,total_failed,duration_ms," +
               "lock_mechanism,err_double_assign,err_oversell,err_driver_overload";
    }
}
