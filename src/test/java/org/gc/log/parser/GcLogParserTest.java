package org.gc.log.parser;

import org.heapfrag.model.HeapSnapshot;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GcLogParserTest {

    private static final String DATA_DIR = "data/";

    @Test
    void parseSampleGcLog() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "sample_gc.log"));

        assertFalse(snapshots.isEmpty(), "Should parse at least one snapshot");
        for (HeapSnapshot snap : snapshots) {
            assertNotNull(snap.getRegions());
            assertFalse(snap.getRegions().isEmpty(), "Each snapshot should have regions");
        }
    }

    @Test
    void parseLusearchLog() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "lusearch.log"));

        assertFalse(snapshots.isEmpty(), "Should parse at least one snapshot");
        for (HeapSnapshot snap : snapshots) {
            assertNotNull(snap.getRegions());
            assertFalse(snap.getRegions().isEmpty());
        }
    }

    @Test
    void parseJvmCrashLog() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "jvm_crash.log"));

        assertFalse(snapshots.isEmpty(), "Crash log should produce at least one snapshot");
        // Crash logs typically have only one snapshot (no GC cycle info)
        HeapSnapshot snap = snapshots.get(0);
        assertNotNull(snap.getRegions());
        assertFalse(snap.getRegions().isEmpty());
    }

    @Test
    void snapshotsHavePositiveRegionCount() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "sample_gc.log"));

        for (HeapSnapshot snap : snapshots) {
            assertTrue(snap.getRegions().size() > 0,
                    "Each snapshot should have at least one region");
        }
    }

    @Test
    void parsedSnapshotsHaveGcCycleInfo() throws IOException {
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(new File(DATA_DIR + "lusearch.log"));

        // Non-crash logs should have GC cycle numbers
        assertTrue(snapshots.size() > 1, "Lusearch log should have multiple GC cycles");
    }
}
