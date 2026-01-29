package org.test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashSet;

public class SoftReferencesTest {

    public static void main(String args[]) {
        int count = 0;
        int collected = 0;
        int lastCollected = 0;
        ReferenceQueue q = new ReferenceQueue();
        HashSet<Reference> refs = new HashSet<Reference>();
        try {
            while (true && collected < 100000) {
                for (int i = 0; i < 10; i++) {
                    byte junk[] = new byte[1000];
                }
                byte lump[] = new byte[1000_000];
                Reference ref = new SoftReference(lump, q);
                refs.add(ref);
                count++;

                Reference queued = null;
                while ((queued = q.poll()) != null) {
                    refs.remove(queued);
                    collected++;
                }
                if (count % 10000 == 0) System.out.println(
                    "Created: " +
                        count +
                        ", collected: " +
                        collected +
                        ", active: " +
                        (count - collected)
                );
            }
        } finally {
            System.out.println(
                "Created: " + count + ", Collected: " + collected
            );
        }
    }
}
