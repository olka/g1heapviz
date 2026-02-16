<<<<<<< HEAD
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
java -jar target/g1heapviz-1.0.0-runner.jar data/jvm_crash.log
```

Expected output:
```
g1heapviz: Loading GC log: sample_gc.log
g1heapviz: Loaded XX heap snapshots
g1heapviz: Open http://localhost:8080/ to visualize
```

### 4. Verify

Open http://127.0.0.1:8080 - — you should see a heatmap grid. File data/lusearch.log will be loaded for analysis.

NOTE: When parsing crash log there's no data on previous GC cycles so only one cycle is available

### 5. CLI Analysis: Parse a GC log and print fragmentation metrics

Please note that CLI analysis is impossible for crash logs since there's only one heap snapshot available

```bash
java -cp target/g1heapviz-1.0.0-runner.jar org.gc.log.parser.GcLogParser data/lusearch.log
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


### 7. Python Analysis Scripts

Install dependencies:

```bash
pip install -r requirements.txt
```

Scripts in `analysis/`:

| Script | Input | Output |
|---|---|---|
| `plot_fragmentation.py` | `frag_format.txt` (CSV from CLI) | Fragmentation trend plot (PNG + PDF) |
| `visualize_benchmark.py` | `data/regions_wide.json` | Impact of RegionSize on performance and external fragmentation on various DaCapo workloads |
| `visualize_lusearch_color.py` | `data/baseline_lusearch_param.json` | G1GC Logging parameters overhead |

To generate `frag_format.txt` from a GC log:

```bash
java -cp target/classes org.gc.log.parser.GcLogParser gc.log > frag_format.txt
```

To generate plots from paper:

```bash
python3.12 -m venv .
source bin/activate
pip install -r requirements.txt

python analysis/visualize_benchmark.py analysis/data/regions_wide.json
python analysis/visualize_lusearch_color.py analysis/data/baseline_lusearch_param.json
```

## Project Structure

```
g1heapviz/
├── src/main/java/org/
│   ├── gc/log/parser/        # GC log parsing
│   │   ├── GcLogParser.java  # Main parser
│   │   ├── Main.java         # Humongous region analysis CLI
│   │   └── VmInfoRetriever.java  # JMX live VM info
│   ├── heapfrag/model/       # Domain model
│   │   ├── HeapSnapshot.java # Snapshot + fragmentation metrics
│   │   └── Region.java       # Individual heap region
│   ├── http/                 # REST API
│   │   ├── DataResource.java # File upload endpoint
│   │   ├── GreetingResource.java  # Graph data endpoint
│   │   ├── SSEResource.java  # Server-sent events
│   │   ├── HeapDataStore.java # Shared data store
│   │   └── StaticResources.java   # Static file serving
│   └── test/                 # Test workloads
├── src/main/resources/static/ # Web UI (ECharts heatmap)
├── analysis/                 # Python analysis scripts
├── samples/                  # Sample data generation
├── ICPE_whitepaper/          # Paper materials
├── Dockerfile                # Container build
└── pom.xml                   # Maven build
```

## Development Mode

```bash
mvn quarkus:dev
```

Enables live reload. Dev UI at http://localhost:8080/q/dev/
