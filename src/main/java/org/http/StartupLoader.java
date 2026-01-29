package org.http;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.gc.log.parser.GcLogParser;
import org.heapfrag.model.HeapSnapshot;

import java.io.File;
import java.util.List;

/**
 * Loads a GC log file on startup if a file path is provided as a CLI argument.
 * Usage: java -jar quarkus-run.jar /path/to/gc.log
 */
@ApplicationScoped
public class StartupLoader {

    @Inject
    HeapDataStore dataStore;

    @Inject
    @io.quarkus.runtime.annotations.CommandLineArguments
    String[] args;

    void onStart(@Observes StartupEvent event) {
        if (args == null || args.length == 0) {
            System.out.println("g1heapviz: No GC log file specified. Use the web UI to upload a file.");
            System.out.println("g1heapviz: Usage: java -jar quarkus-run.jar <gc-log-file>");
            return;
        }

        String filePath = args[0];
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("g1heapviz: File not found: " + filePath);
            return;
        }

        System.out.println("g1heapviz: Loading GC log: " + filePath);
        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(filePath);
        dataStore.setSnapshots(snapshots);
        System.out.println("g1heapviz: Loaded " + snapshots.size() + " heap snapshots");
        System.out.println("g1heapviz: Open http://localhost:8080/index.html to visualize");
    }
}
