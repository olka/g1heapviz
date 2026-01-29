package org.http;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.heapfrag.model.HeapSnapshot;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.time.Duration;
import java.util.List;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
@Path("/sse")
public class SSEResource {

    @Inject
    HeapDataStore dataStore;

    String postprocess(List<HeapSnapshot> snaps) {
        StringBuffer result = new StringBuffer();
        result.append("[");
        for (HeapSnapshot snapshot : snaps)
            result.append(snapshot.regionsToJson().substring(1, snapshot.regionsToJson().length() - 1)).append(",");
        if (result.length() > 1) {
            result.setCharAt(result.length() - 1, ']');
        } else {
            result.append("]");
        }
        return result.toString();
    }

    @GET
    @Path("/events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<String> stream() {
        return Multi.createFrom().ticks().every(Duration.ofMillis(320))
                .onOverflow().buffer(100)
                .map(tick -> {
                    try {
                        List<HeapSnapshot> snapshots = dataStore.getSnapshots();
                        return postprocess(snapshots.stream()
                                .filter(rr -> rr.getGcCycle() == tick.intValue()).toList());
                    } catch (Exception e) {
                        System.err.println("Error streaming: " + e.getMessage());
                        return "[]";
                    }
                });
    }
}
