package com.delivery.repository;

import com.delivery.model.BaseEntity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class CsvRepository<T extends BaseEntity> {
    protected String filePath;

    public CsvRepository(String filePath) {
        this.filePath = filePath;
    }

    // Các hàm Abstract (Dynamic) cho việc parse từ String -> Object và Object -> String
    protected abstract T parseLine(String line);
    protected abstract String toCsvRow(T entity);
    protected abstract String getHeader();

    public List<T> readAll() {
        List<T> entities = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return entities;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Bỏ qua header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                T entity = parseLine(line);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file CSV: " + e.getMessage());
        }
        return entities;
    }

    public T findById(int id) {
        for (T entity : readAll()) {
            if (entity.getId() == id) {
                return entity;
            }
        }
        return null;
    }

    public void saveAll(List<T> entities) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(getHeader());
            bw.newLine();
            for (T entity : entities) {
                bw.write(toCsvRow(entity));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file CSV: " + e.getMessage());
        }
    }

    public void save(T entity) {
        List<T> entities = readAll();
        entities.add(entity);
        saveAll(entities);
    }

    public void update(T entityToUpdate) {
        List<T> entities = readAll();
        boolean updated = false;
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId() == entityToUpdate.getId()) {
                entities.set(i, entityToUpdate);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveAll(entities);
        }
    }

    public void delete(int id) {
        List<T> entities = readAll();
        boolean removed = entities.removeIf(e -> e.getId() == id);
        if (removed) {
            saveAll(entities);
        }
    }
}

