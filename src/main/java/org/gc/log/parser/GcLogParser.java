package org.gc.log.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.heapfrag.model.HeapSnapshot;
import org.heapfrag.model.Region;

/**
 * Parser for GC log files that extracts heap region information
 * and creates HeapSnapshot objects from region-level GC log data.
 *
 * Requires JVM GC logging with: -Xlog:gc,heap,region=trace
 */
public class GcLogParser {

    /**
     * Parses a GC log file and returns a list of Region objects.
     *
     * @param logFilePath path to the GC log file
     * @return list of Region objects extracted from the log
     * @throws IOException if the file cannot be read
     */
    public static List<HeapSnapshot> parseLogFile(String logFilePath) {
        try {
            return parseLogFile(new File(logFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a GC log file and returns a list of Region objects.
     *
     * @param logFile the GC log file
     * @return list of Region objects extracted from the log
     * @throws IOException if the file cannot be read
     */
    public static List<HeapSnapshot> parseLogFile(File logFile)
        throws IOException {
        try (Scanner scanner = new Scanner(logFile)) {
            return parseLog(scanner);
        }
    }

    /**
     * Parses a GC log from a Scanner and returns a list of Region objects.
     *
     * @param scanner Scanner reading the GC log
     * @return list of Region objects extracted from the log
     */
    public static List<HeapSnapshot> parseLog(Scanner scanner) {
        List<HeapSnapshot> snapshots = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        boolean inHeapRegionSection = false;
        HeapSnapshot hs;
        boolean isFullGC = false;
        String gcType = "";
        String prevLine = ""; //utility purposes

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Check if we've reached the "Heap Regions:" section
            if (line.contains("Heap Regions:")) {
                inHeapRegionSection = true;

                if (prevLine.contains("gc,start")) {
                    //new GC cycle detected. Creating heap snapshot
                    if (prevLine.contains("Full")) isFullGC = true;
                    gcType = prevLine.substring(prevLine.indexOf(")") + 2);
                }
                continue;
            }

            // If we haven't reached the heap regions section yet, skip
            if (!inHeapRegionSection) {
                prevLine = line;

                continue;
            }
            Region region = parseLine(line);
            if (region != null) {
                regions.add(region);
            } else {
                // End of region section — save snapshot if we have data
                if (regions.size() > 0) {
                    hs = new HeapSnapshot(
                        regions.get(regions.size() - 1).getGcCycle(),
                        regions,
                        isFullGC,
                        gcType
                    );
                    snapshots.add(hs);
                }
                regions = new ArrayList<>();
                isFullGC = false;
                inHeapRegionSection = false;
                gcType = "";
            }
        }

        if (snapshots.size() == 0 && regions.size() != 0) {
            hs = new HeapSnapshot(
                regions.get(regions.size() - 1).getGcCycle(),
                regions,
                isFullGC,
                gcType
            );
            snapshots.add(hs);
        }

        return snapshots;
    }

    /**
     * Parses a single line from the GC log and creates a Region object.
     *
     * @param line the log line to parse
     * @return Region object if the line contains region data, null otherwise
     */
    private static Region parseLine(String line) {
        // Split by pipe character
        String[] parts = line.split("\\|");
        // We need at least 5 parts to have valid region data
        if (parts.length < 5) {
            return null;
        }

        try {
            // Extract region index (column 1)
            int index = Integer.parseInt(parts[1].trim());

            // Extract region type (column 4)
            String regionType = parts[4].trim();

            // Skip empty region types
            if (regionType.isEmpty()) {
                return null;
            }
            //gc cycle might be empty if we're parsing crash log
            int gcCycleNumber = 0;
            try {
                gcCycleNumber = Integer.valueOf(
                    parts[0].substring(
                        parts[0].indexOf("GC(") + 3,
                        parts[0].length() - 2
                    ).replace(")", "")
                ); // pretty brittle
            } catch (Exception e) {
                //no gc cycle here - parsing crash log
            }
            // Create and return the Region object
            return new Region(index, regionType, gcCycleNumber);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(parts[0]);
            return null;
        }
    }

    public static List<HeapSnapshot> process(String path) {
        return parseLogFile(path);
    }

    /**
     * CLI entry point: parses a GC log file and prints fragmentation metrics.
     * Usage: java GcLogParser <gc-log-file>
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java GcLogParser <gc-log-file>");
            System.exit(1);
        }

        String logFilePath = args[0];

        var snapshots = parseLogFile(new File(logFilePath));

        List<Integer> fragmentationMetrics = new ArrayList<>();
        List<Integer> fragmentationAfterMetrics = new ArrayList<>();
        System.out.println(
            "GC#, ext frag before GC, ext frag after GC, is full GC"
        );
        for (int i = 0; i < snapshots.size() - 1; i = i + 2) {
            var hs = snapshots.get(i);
            var hs2 = snapshots.get(i + 1);
            System.out.print(hs.getGcCycle() + ", ");
            System.out.print(hs.calculateExternalFragmentation());
            System.out.print(", " + hs2.calculateExternalFragmentation());
            fragmentationMetrics.add(hs.calculateExternalFragmentation());
            fragmentationAfterMetrics.add(hs2.calculateExternalFragmentation());
            System.out.println(", " + (hs.isFull() ? "1" : "0"));
            assert hs.getGcCycle() == hs2.getGcCycle();
        }

        System.out.println("AVG frag before: ");
        System.out.println(
            fragmentationMetrics.stream().mapToInt(Integer::intValue).sum() /
                fragmentationMetrics.size()
        );
        System.out.println("AVG frag after: ");
        System.out.println(
            fragmentationAfterMetrics
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum() /
                fragmentationMetrics.size()
        );
    }
}
