package org.heapfrag.model;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Region {

    int index = 0;
    int regionType;
    int usagePercent = 0;

    String name;

    int gcCycle;

    public Region(int index, String regionType, int gcCycle) {
        this(index, regionType, gcCycle, 0);
    }

    public Region(int index, String regionType, int gcCycle, int usagePercent) {
        this.index = index;
        this.regionType = parseType(regionType.trim());
        this.name = regionType.trim();
        this.gcCycle = gcCycle;
        this.usagePercent = usagePercent;
    }

    //Heap Regions:   CS=, F=free, TAMS=top-at-mark-start, PB=parsable bottom
    private int parseType(String str) {
        switch (str) {
            case "HS":
                return 120; //humongous
            case "HC":
                return 150; //humongous
            case "F":
                return 0; //free
            case "CS":
                return 10; //collection set
            case "E":
                return 30; //Eden
            case "S":
                return 60; //survivor
            case "O":
                return 90; // old
            default:
                throw new IllegalArgumentException(
                    index + "Invalid input: " + str
                );
        }
    }

    public String toJson(int gridSize) {
        if (regionType == 0) return "";
        int x = index / gridSize;
        int y = index % gridSize;
        return "[" + x + ", " + y + ", " + regionType + "],";
    }

    @Override
    public String toString() {
        return "Region " + index + ": type=" + regionType + ", name=" + name + " cycle=" + gcCycle;
    }

    /**
     * Parses usage percentage from strings like "95%" or "  0%"
     * Returns integer 0-100
     */
    private static int parseUsagePercent(String percentStr) {
        try {
            String cleaned = percentStr.replace("%", "").trim();
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String process(String s) {
        return process(new Scanner(s));
    }

    public static String process(Scanner s) {
        StringBuffer result = new StringBuffer();
        result.append("[");
        boolean skip = true;
        while (s.hasNextLine()) {
            String nextLine = s.nextLine();
            if (nextLine.contains("Heap Regions:")) {
                skip = false;
                continue;
            }
            if (skip) continue;

            var line = nextLine.split("\\|");

            try {
                int regionIndex = Integer.parseInt(line[1].trim());
                String regionType = line[4];
                int usagePercent = parseUsagePercent(line[3].trim());
                result.append(
                    new Region(regionIndex, regionType, 0, usagePercent)
                );
            } catch (Exception e) {
                if (e instanceof ArrayIndexOutOfBoundsException) {
                    System.err.println("Ended: " + e.getMessage());
                    break;
                } else throw e;
            }
        }
        result.setCharAt(result.length() - 1, ']');
        System.out.println("================");
        return result.toString();
    }

    public int getGcCycle() {
        return gcCycle;
    }

    public int getUsagePercent() {
        return usagePercent;
    }

    public static void main(String[] args) throws IOException {
        System.err.println(args[0]);
        Scanner s = new Scanner(new File(args[0]));
        System.out.println(Region.process(s));
    }
}
