package org.http;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.heapfrag.model.HeapSnapshot;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/graph")
public class GreetingResource {

    @Inject
    HeapDataStore dataStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getn")
    public String getn(@RestQuery("n") String n) {
        List<HeapSnapshot> snapshots = dataStore.getSnapshots();
        if (snapshots.isEmpty() || n == null) {
            return "[]";
        }
        int index = Integer.parseInt(n);
        if (index < 0 || index >= snapshots.size()) {
            return "[]";
        }
        return snapshots.get(index).regionsToJson();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gridsize")
    public String gridsize() {
        List<HeapSnapshot> snapshots = dataStore.getSnapshots();
        if (snapshots.isEmpty()) {
            return "0";
        }
        return String.valueOf(snapshots.get(0).getGridSize());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/size")
    public String size() {
        return String.valueOf(dataStore.getSnapshots().size());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/metrics")
    public String metrics(@RestQuery("n") String n) {
        List<HeapSnapshot> snapshots = dataStore.getSnapshots();
        if (snapshots.isEmpty() || n == null) {
            return "{\"ext\":0,\"int\":0,\"free\":0}";
        }
        int index = Integer.parseInt(n);
        if (index < 0 || index >= snapshots.size()) {
            return "{\"ext\":0,\"int\":0,\"free\":0}";
        }
        HeapSnapshot snap = snapshots.get(index);
        return "{\"ext\":" + snap.calculateExternalFragmentation()
             + ",\"int\":" + snap.calculateInternalFragmentation()
             + ",\"free\":" + snap.calculateFree() + "}";
    }
}
