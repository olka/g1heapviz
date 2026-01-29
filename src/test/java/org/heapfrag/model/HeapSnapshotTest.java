package org.heapfrag.model;

import org.gc.log.parser.GcLogParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeapSnapshotTest {

    private static final String DATA_DIR = "data/";

    @Test
    void externalFragmentationInRange() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "sample_gc.log"));

        for (HeapSnapshot snap : snapshots) {
            int ef = snap.calculateExternalFragmentation();
            assertTrue(ef >= 0 && ef <= 100,
                    "External fragmentation should be 0-100, got: " + ef);
        }
    }

    @Test
    void internalFragmentationInRange() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "sample_gc.log"));

        for (HeapSnapshot snap : snapshots) {
            int intFrag = snap.calculateInternalFragmentation();
            assertTrue(intFrag >= 0 && intFrag <= 100,
                    "Internal fragmentation should be 0-100, got: " + intFrag);
        }
    }

    @Test
    void freePercentageInRange() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "sample_gc.log"));

        for (HeapSnapshot snap : snapshots) {
            int free = snap.calculateFree();
            assertTrue(free >= 0 && free <= 100,
                    "Free percentage should be 0-100, got: " + free);
        }
    }

    @Test
    void gridSizeIsSquareRoot() {
        List<Region> regions = new java.util.ArrayList<>();
        for (int i = 0; i < 64; i++) {
            regions.add(new Region(i, "F", 0));
        }
        HeapSnapshot snap = new HeapSnapshot(0, regions, false, "");
        assertEquals(8, snap.getGridSize(), "Grid size of 64 regions should be 8");
    }

    @Test
    void gridSizeRoundsUp() {
        List<Region> regions = new java.util.ArrayList<>();
        for (int i = 0; i < 65; i++) {
            regions.add(new Region(i, "F", 0));
        }
        HeapSnapshot snap = new HeapSnapshot(0, regions, false, "");
        assertEquals(9, snap.getGridSize(), "Grid size of 65 regions should round up to 9");
    }

    @Test
    void regionsToJsonProducesValidOutput() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "lusearch.log"));
        HeapSnapshot snap = snapshots.get(0);

        String json = snap.regionsToJson();
        assertTrue(json.startsWith("["), "JSON should start with [");
        assertTrue(json.endsWith("]"), "JSON should end with ]");
    }

    @Test
    void metricsWorkOnCrashLog() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "jvm_crash.log"));
        HeapSnapshot snap = snapshots.get(0);

        // Should not throw
        int ef = snap.calculateExternalFragmentation();
        int intFrag = snap.calculateInternalFragmentation();
        int free = snap.calculateFree();

        assertTrue(ef >= 0 && ef <= 100);
        assertTrue(intFrag >= 0 && intFrag <= 100);
        assertTrue(free >= 0 && free <= 100);
    }

    @Test
    void allFreeRegionsHaveZeroExternalFragmentation() {
        List<Region> regions = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            regions.add(new Region(i, "F", 0));
        }
        HeapSnapshot snap = new HeapSnapshot(0, regions, false, "");
        assertEquals(0, snap.calculateExternalFragmentation(),
                "All-free contiguous heap should have 0% external fragmentation");
    }

    @Test
    void noFreeRegionsHaveMaxExternalFragmentation() {
        List<Region> regions = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            regions.add(new Region(i, "O", 0));
        }
        HeapSnapshot snap = new HeapSnapshot(0, regions, false, "");
        assertEquals(100, snap.calculateExternalFragmentation(),
                "No free regions should give 100% external fragmentation");
    }
}
