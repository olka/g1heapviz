# g1heapviz [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.18428126.svg)](https://doi.org/10.5281/zenodo.18428126)

JVM Heap Fragmentation Analysis and Visualization Tool.

g1heapviz parses region-level G1GC logs from the JVM and provides fragmentation metrics (internal/external) along with an interactive heatmap visualization of heap regions over time.

This tool is the artifact for the paper accepted at **ICPE 2026 Industrial Track**.

## Demo



https://github.com/user-attachments/assets/f75310b0-cbf5-48a1-888a-290fa77a3d8c



## Docker instructions

```bash
docker build . -t g1heapviz
docker run -v $PWD/data/:/app/data --network=host -p 8080:8080  g1heapviz data/jvm_crash.log
```
Open http://127.0.0.1:8080 - — you should see a heatmap grid. File data/lusearch.log will be loaded for analysis.

## Java instructions 

### Prerequisites
- Java 21 (JDK)
- Maven 3.8+

### 1. Build

```bash
mvn package -DskipTests
```

### 2. Generate a sample GC log

```bash
java -Xmx1024m -Xms1024m -XX:G1HeapRegionSize=16m -XX:+UseG1GC "-Xlog:gc*=trace:file=sample_gc.log:time,tags:filecount=5,filesize=1000M" -cp target/classes org.test.SoftReferencesTest
```

### 3. Run g1heapviz with the GC log

```bash
java -jar target/quarkus-app/quarkus-run.jar data/jvm_crash.log
```

Expected output:
```
g1heapviz: Loading GC log: sample_gc.log
g1heapviz: Loaded XX heap snapshots
g1heapviz: Open http://localhost:8080/index.html to visualize
```

### 4. Verify

Open http://127.0.0.1:8080 - — you should see a heatmap grid. File data/lusearch.log will be loaded for analysis.

NOTE: When parsing crash log there's no data on previous GC cycles so only one cycle is available

### 5. CLI Analysis: Parse a GC log and print fragmentation metrics

Please note that CLI analysis is impossible for crash logs since there's only one heap snapshot available

```bash
java -cp target/classes org.gc.log.parser.GcLogParser data/lusearch.log
```

Output format: `GC#, ext_frag_before, ext_frag_after, is_full_gc`


### 6. REST API

| Endpoint | Method | Description |
|---|---|---|
| `/multipart` | POST | Upload a GC log file (multipart form) |
| `/graph/getn?n=<index>` | GET | Get heap region data for GC cycle at index |
| `/graph/size` | GET | Get total number of parsed snapshots |
| `/sse/events` | GET | Stream heap snapshots via Server-Sent Events |
| `/multipart/data` | GET | Get raw region data |


### 7. Python Analysis Scripts to replicate resutls from paper

Install dependencies:

```bash
python3.12 -m venv .
source bin/activate
pip install -r requirements.txt

python analysis/plot_fragmentation.py data/frag_format.txt
python analysis/visualize_regions.py ICPE_whitepaper/Figures/regions.json

```

Scripts in `analysis/`:

| Script | Input | Output |
|---|---|---|
| `plot_fragmentation.py` | `frag_format.txt` (CSV from CLI) | Fragmentation trend plot (PNG + PDF) |
| `visualize_regions.py` | Region JSON data | Heap region grid visualization |
| `visualize_benchmark.py` | `out.json` | Benchmark result plots |
| `visualize_lusearch.py` | `out.json` | DaCapo lusearch analysis |
| `visualize_lusearch_color.py` | `out.json` | Color-coded lusearch visualization |

To generate `frag_format.txt` from a GC log:

```bash
java -cp target/classes org.gc.log.parser.GcLogParser gc.log > frag_format.txt
```

## Examples how to generate a GC Log

Run any Java application with region-level GC tracing enabled:

```bash
java -XX:+UseG1GC "-Xlog:gc+heap+region=trace:file=gc.log" -jar your-application.jar 
```

After project was built:
```bash
java -XX:+UseG1GC -XX:G1HeapRegionSize=16m "-Xlog:gc+heap+region=trace:file=gc.log" -cp target/classes org.test.SoftReferencesTest     
```

### Example how to gather data for DaCapo (lusearch benchamrk)

```bash
java -XX:G1HeapRegionSize=1m "-Xlog:gc*,heap+region*=trace:file=lusearch_1m.log" -jar dacapo-23.11-MR2-chopin.jar lusearch -s large
java -XX:G1HeapRegionSize=8m "-Xlog:gc*,heap+region*=trace:file=lusearch_8m.log" -jar dacapo-23.11-MR2-chopin.jar lusearch -s large
java -XX:G1HeapRegionSize=16m "-Xlog:gc*,heap+region*=trace:file=lusearch_16m.log" -jar dacapo-23.11-MR2-chopin.jar lusearch -s large
java -XX:G1HeapRegionSize=32m "-Xlog:gc*,heap+region*=trace:file=lusearch_32m.log" -jar dacapo-23.11-MR2-chopin.jar lusearch -s large
```

============================================================================================================================================

## Example how hyperfine was used in paper
```bash
hyperfine --warmup 10 --runs 2 --export-json baseline_lusearch_param.json -L ppparams "-Xlog:gc+heap+region=trace:file=/tmp/gclog.txt","-Xlog:async -Xlog:gc+heap+region=trace:file=/tmp/gclog.txt" 'java {ppparams} -jar dacapo-23.11-MR2-chopin.jar lusearch -s large'
```

## Project Structure

```
g1heapviz/
├── src/
│   ├── main/
│   │   ├── java/org/
│   │   │   ├── gc/log/parser/
│   │   │   │   ├── GcLogParser.java       # GC log parser (main logic)
│   │   │   │   └── Main.java              # Humongous region analysis CLI
│   │   │   ├── heapfrag/model/
│   │   │   │   ├── GcCycle.java           # GC cycle model
│   │   │   │   ├── HeapSnapshot.java      # Snapshot with fragmentation metrics
│   │   │   │   └── Region.java            # Heap region model
│   │   │   ├── http/
│   │   │   │   ├── DataResource.java      # File upload endpoint (/multipart)
│   │   │   │   ├── GreetingResource.java  # Graph data + metrics API (/graph)
│   │   │   │   ├── HeapDataStore.java     # Shared CDI data store
│   │   │   │   ├── SSEResource.java       # Server-sent events (/sse)
│   │   │   │   └── StartupLoader.java     # CLI file arg loader
│   │   │   └── test/
│   │   │       └── SoftReferencesTest.java  # Sample GC workload
│   │   └── resources/
│   │       ├── META-INF/resources/
│   │       │   └── index.html             # Web UI
│   │       └── application.properties
│   └── test/java/org/
│       ├── gc/log/parser/
│       │   └── GcLogParserTest.java       # Parser tests
│       └── heapfrag/model/
│           └── HeapSnapshotTest.java      # Metrics + snapshot tests
├── analysis/                              # Python analysis scripts
│   ├── plot_fragmentation.py
│   ├── visualize_benchmark.py
│   ├── visualize_lusearch.py
│   ├── visualize_lusearch_color.py
│   └── visualize_regions.py
├── data/                                  # Sample GC logs
├── Dockerfile
├── pom.xml
├── requirements.txt                       # Python dependencies
└── LICENSE                                # Apache 2.0
```

## License

Apache License 2.0. See [LICENSE](LICENSE).
