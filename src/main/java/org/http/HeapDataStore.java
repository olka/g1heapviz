package org.http;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import org.heapfrag.model.HeapSnapshot;

/**
 * Application-scoped store for parsed heap snapshots.
 */
@ApplicationScoped
public class HeapDataStore {

    private volatile List<HeapSnapshot> snapshots = Collections.emptyList();

    public List<HeapSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<HeapSnapshot> snapshots) {
        this.snapshots = snapshots;
    }
}
