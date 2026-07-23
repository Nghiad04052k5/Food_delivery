package com.delivery.repository;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static final ConcurrentHashMap<String, Object> syncLocks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ReentrantLock> jvmFileLocks = new ConcurrentHashMap<>();

    // Cơ chế SYNCHRONIZED (cấp độ object / per-resource keys)
    public static Object getLock(String resourceKey) {
        return syncLocks.computeIfAbsent(resourceKey, k -> new Object());
    }

    // Cơ chế FILE_LOCK (khóa file CSV vật lý bằng java.nio.channels.FileLock)
    public static void executeWithFileLock(String filePath, Runnable action) {
        // Tránh OverlappingFileLockException khi chạy đa luồng trong cùng 1 JVM
        ReentrantLock jvmLock = jvmFileLocks.computeIfAbsent(filePath, k -> new ReentrantLock());
        jvmLock.lock();
        try {
            File file = new File(filePath + ".lock");
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 FileChannel channel = raf.getChannel();
                 FileLock lock = channel.lock()) { // OS-level lock
                
                action.run();
                
            } catch (Exception e) {
                System.err.println("Lỗi khi khóa file: " + e.getMessage());
            }
        } finally {
            jvmLock.unlock();
        }
    }
}
