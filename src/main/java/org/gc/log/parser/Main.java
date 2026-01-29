package org.gc.log.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class GcLogLine {

    int gcNumer;
    int humongousBefore;
    int humongousAfter;

    public GcLogLine(String line) {
        int index = line.indexOf("GC(");
        String[] data = line.substring(index).split(" ");
        gcNumer = Integer.parseInt(data[0].substring(3, data[0].length() - 1));
        String[] humongous = data[3].split("->");
        humongousBefore = Integer.parseInt(humongous[0]);
        humongousAfter = Integer.parseInt(humongous[1]);
    }

    @Override
    public String toString() {
        return gcNumer + ", " + humongousBefore + ", " + humongousAfter;
    }
}

public class Main {

    public static void main(String[] args) throws IOException {
        //        System.err.println(args[0]);
        if (!Files.isDirectory(Paths.get(args[0]))) handleFile(
            Paths.get(args[0])
        );
        else {
            List<Path> files = Files.list(Paths.get(args[0])).toList();
            files.stream().forEach(Main::handleFile);
        }
    }

    private static void handleFile(Path file) {
        if (!file.getFileName().toString().endsWith(".log")) return;
        List<GcLogLine> logs = null;
        try {
            logs = Files.readAllLines(file)
                .stream()
                .filter(l -> l.contains("Humongous regions:"))
                .map(GcLogLine::new)
                .filter(gcLogLine -> gcLogLine.humongousAfter > 90000)
                .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //        System.out.println("GC#, Humongous Before, Humongous After");
        for (GcLogLine line : logs) {
            System.out.println(line);
        }
    }
}
