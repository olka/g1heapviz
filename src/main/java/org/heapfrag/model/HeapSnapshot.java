package org.heapfrag.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of Heap snapshot
 * [0.152s][trace][gc,heap,region] GC(0) Heap Regions: E=young(eden), S=young(survivor), O=old, HS=humongous(starts), HC=humongous(continues), CS=collection set, F=free, TAMS=top-at-mark-start, PB=parsable bottom
 * [0.152s][trace][gc,heap,region] GC(0) |   0|0x0000000680000000, 0x0000000680000000, 0x0000000680400000|  0%| F|  |TAMS 0x0000000680000000| PB 0x0000000680000000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   1|0x0000000680400000, 0x0000000680400000, 0x0000000680800000|  0%| F|  |TAMS 0x0000000680400000| PB 0x0000000680400000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   2|0x0000000680800000, 0x0000000680800000, 0x0000000680c00000|  0%| F|  |TAMS 0x0000000680800000| PB 0x0000000680800000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   3|0x0000000680c00000, 0x0000000680c00000, 0x0000000681000000|  0%| F|  |TAMS 0x0000000680c00000| PB 0x0000000680c00000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   4|0x0000000681000000, 0x0000000681000000, 0x0000000681400000|  0%| F|  |TAMS 0x0000000681000000| PB 0x0000000681000000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   5|0x0000000681400000, 0x0000000681400000, 0x0000000681800000|  0%| F|  |TAMS 0x0000000681400000| PB 0x0000000681400000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   6|0x0000000681800000, 0x0000000681800000, 0x0000000681c00000|  0%| F|  |TAMS 0x0000000681800000| PB 0x0000000681800000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   7|0x0000000681c00000, 0x0000000681c00000, 0x0000000682000000|  0%| F|  |TAMS 0x0000000681c00000| PB 0x0000000681c00000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   8|0x0000000682000000, 0x0000000682000000, 0x0000000682400000|  0%| F|  |TAMS 0x0000000682000000| PB 0x0000000682000000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |   9|0x0000000682400000, 0x0000000682400000, 0x0000000682800000|  0%| F|  |TAMS 0x0000000682400000| PB 0x0000000682400000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |  10|0x0000000682800000, 0x0000000682800000, 0x0000000682c00000|  0%| F|  |TAMS 0x0000000682800000| PB 0x0000000682800000| Untracked
 * [0.152s][trace][gc,heap,region] GC(0) |  11|0x000000069e800000, 0x000000069ebd0940, 0x000000069ec00000| 95%| E|  |TAMS 0x000000069e800000| PB 0x000000069e800000| Complete
 * [0.152s][trace][gc,heap,region] GC(0) |  12|0x000000069ec00000, 0x000000069f000000, 0x000000069f000000|100%| E|CS|TAMS 0x000000069ec00000| PB 0x000000069ec00000| Complete
 * [0.152s][trace][gc,heap,region] GC(0) |  13|0x000000069f000000, 0x000000069f400000, 0x000000069f400000|100%| E|CS|TAMS 0x000000069f000000| PB 0x000000069f000000| Complete
 * [0.152s][trace][gc,heap,region] GC(0) |  14|0x000000069f400000, 0x000000069f800000, 0x000000069f800000|100%| E|CS|TAMS 0x000000069f400000| PB 0x000000069f400000| Complete
 */
public class HeapSnapshot {

    public List<Region> getRegions() {
        return regions;
    }

    public int getGridSize() {
        int count = regions == null ? 0 : regions.size();
        return (int) Math.ceil(Math.sqrt(count));
    }

    public String regionsToJson() {
        int gridSize = getGridSize();
        StringBuffer result = new StringBuffer();
        result.append("[");
        for (Region r : regions) {
            result.append(r.toJson(gridSize));
        }
        if (result.length() > 1) {
            result.setCharAt(result.length() - 1, ']');
        } else {
            result.append("]");
        }
        return result.toString();
    }

    List<Region> regions;

    public int getGcCycle() {
        return gcCycle;
    }

    int gcCycle;

    public String getGcType() {
        return gcType;
    }

    String gcType;

    boolean isFull;

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public HeapSnapshot(
        int gcCycle,
        List<Region> regions,
        boolean isFull,
        String gcType
    ) {
        this.gcCycle = gcCycle;
        this.regions = regions;
        this.isFull = isFull;
        this.gcType = gcType;
    }

    public HeapSnapshot(List<Region> regions) {
        this.regions = regions;
    }

    public void addRegion(Region r) {
        if (regions == null) regions = new ArrayList<>();
        regions.add(r);
    }

    /**
     * Calculates external fragmentation based on free region distribution.
     * Returns a percentage (0-100) where:
     * - 0% = all free regions are contiguous (no fragmentation)
     * - Higher % = free regions are more scattered across the heap
     *
     * Algorithm matches Shenandoah GC implementation:
     */
    public Integer calculateExternalFragmentation() {
        if (regions == null || regions.isEmpty()) {
            return 0;
        }

        int lastIdx = -1;
        int maxContig = 0; // Maximum contiguous empty region count
        int emptyContig = 0; // Current contiguous empty region count
        int freeRegionCount = 0; // Total free region count

        // Iterate through regions to find largest contiguous free block
        for (Region region : regions) {
            boolean isFree = (region.regionType == 0); // F = free

            if (isFree) {
                freeRegionCount++;

                // Check if this region is contiguous with the last free region
                if (lastIdx + 1 == region.index) {
                    emptyContig++; // Extend contiguous sequence
                } else {
                    emptyContig = 1; // Start new sequence
                }
            } else {
                emptyContig = 0; // Break sequence
            }

            // Track maximum contiguous sequence
            maxContig = Math.max(maxContig, emptyContig);
            lastIdx = region.index;
        }

        // If no free regions, fragmentation is 100
        if (freeRegionCount == 0) {
            return 100;
        }

        // Calculate: EF = 1 - (max_contiguous / total_free)
        double ef = 1.0 - ((double) maxContig / freeRegionCount);
        return (int) Math.round(ef * 100);
    }

    /**
     * Calculates the percentage of free space in the heap.
     * High free percentage with high fragmentation (from calculateFragmentation)
     * indicates memory is available but scattered.
     */
    public Integer calculateFree() {
        if (regions == null || regions.isEmpty()) return 0;

        int freeRegionCount = 0;
        // Count free regions
        for (Region region : regions) {
            if (region.regionType == 0) freeRegionCount++; // F = free
        }

        // Calculate percentage of free regions
        int freePercentage = (freeRegionCount * 100) / regions.size();

        return freePercentage;
    }

    /**
     * Calculates internal fragmentation - how evenly used memory is distributed across regions.
     * Returns percentage (0-100) where:
     * - 0% = memory is densely packed (no internal fragmentation)
     * - Higher % = memory is spread thinly across many regions (more fragmentation)
     *
     * Algorithm matches Shenandoah GC implementation
     */
    public Integer calculateInternalFragmentation() {
        if (regions == null || regions.isEmpty()) {
            return 0;
        }

        double squared = 0; // sum(usagePercent[i]²)
        double linear = 0; // sum(usagePercent[i])

        for (Region region : regions) {
            double usagePercent = region.getUsagePercent(); // 0-100
            squared += usagePercent * usagePercent;
            linear += usagePercent;
        }

        if (linear > 0) {
            // When working with percentages (0-100), the formula simplifies to:
            // IF = 1 - sum(usagePercent²) / (100 × sum(usagePercent))
            double s = squared / (100.0 * linear);
            double fragmentation = 1.0 - s;
            return (int) Math.round(fragmentation * 100);
        } else {
            return 0;
        }
    }
}
