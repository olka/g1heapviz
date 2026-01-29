package org.http;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.POST;

import org.gc.log.parser.GcLogParser;
import org.heapfrag.model.HeapSnapshot;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("multipart")
public class DataResource {

    @Inject
    HeapDataStore dataStore;

    @POST
    public String upload(@RestForm String description,
                         @RestForm("file") FileUpload file) throws IOException {
        System.out.println("Uploading: " + file.fileName() + " (" + file.size() + " bytes)");

        List<HeapSnapshot> snapshots = GcLogParser.parseLogFile(file.uploadedFile().toFile());
        dataStore.setSnapshots(snapshots);

        System.out.println("Parsed " + snapshots.size() + " heap snapshots");
        return "Parsed " + snapshots.size() + " heap snapshots from " + file.fileName();
    }
}
