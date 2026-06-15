package com.delivery.repository;

import com.delivery.model.SimulationRun;
import java.util.Locale;

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
            run.setDurationMs(Integer.parseInt(parts[4].trim()));
            return run;
        }
        return null;
    }

    @Override
    protected String toCsvRow(SimulationRun entity) {
        return String.format(Locale.US, "%d,%d,%d,%d,%d",
                entity.getId(),
                entity.getTotalOrders(),
                entity.getTotalSuccess(),
                entity.getTotalFailed(),
                entity.getDurationMs()
        );
    }

    @Override
    protected String getHeader() {
        return "simulation_run_id,total_orders,total_success,total_failed,duration_ms";
    }
}
