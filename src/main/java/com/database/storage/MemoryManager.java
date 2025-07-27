package com.database.storage;

/**
 * Manages memory allocation and tracking for the database engine.
 */
public class MemoryManager {
    private long usedMemory;
    private final long maxMemory;
    private static final long DEFAULT_MAX_MEMORY = 512 * 1024 * 1024; // 512 MB

    public MemoryManager() {
        this(DEFAULT_MAX_MEMORY);
    }

    public MemoryManager(long maxMemory) {
        if (maxMemory <= 0) {
            throw new IllegalArgumentException("Max memory must be positive");
        }
        this.maxMemory = maxMemory;
        this.usedMemory = 0;
    }

    /**
     * Allocates the specified amount of memory.
     * @param size the number of bytes to allocate
     * @throws OutOfMemoryError if allocation would exceed max memory
     */
    public synchronized void allocate(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Allocation size cannot be negative");
        }
        
        if (usedMemory + size > maxMemory) {
            throw new OutOfMemoryError("Cannot allocate " + size + " bytes. " +
                "Would exceed max memory limit of " + maxMemory + " bytes");
        }
        
        usedMemory += size;
    }

    /**
     * Deallocates the specified amount of memory.
     * @param size the number of bytes to deallocate
     */
    public synchronized void deallocate(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Deallocation size cannot be negative");
        }
        
        usedMemory = Math.max(0, usedMemory - size);
    }

    /**
     * Returns the current memory usage as a percentage.
     */
    public synchronized double getMemoryUsage() {
        return (double) usedMemory / maxMemory;
    }

    /**
     * Returns the amount of used memory in bytes.
     */
    public synchronized long getUsedMemory() {
        return usedMemory;
    }

    /**
     * Returns the maximum memory limit in bytes.
     */
    public long getMaxMemory() {
        return maxMemory;
    }

    /**
     * Returns the amount of available memory in bytes.
     */
    public synchronized long getAvailableMemory() {
        return maxMemory - usedMemory;
    }

    /**
     * Checks if the specified allocation would exceed memory limits.
     */
    public synchronized boolean canAllocate(int size) {
        return usedMemory + size <= maxMemory;
    }

    /**
     * Forces garbage collection and updates memory statistics.
     */
    public void forceGC() {
        System.gc();
        // In a real implementation, we might recalculate actual memory usage here
    }

    @Override
    public String toString() {
        return String.format("MemoryManager{used=%d, max=%d, usage=%.2f%%}", 
            getUsedMemory(), getMaxMemory(), getMemoryUsage() * 100);
    }
}
