#!/usr/bin/env python3
"""
Visualize DaCapo benchmark results from baseline_lusearch_param.json
"""

import json
import sys

import matplotlib.pyplot as plt
import numpy as np

# Read the JSON file
with open(sys.argv[1], "r") as f:
    data = json.load(f)

# Organize data by benchmark and parameter configuration
benchmarks = {}

for result in data["results"]:
    bench_name = result["parameters"]["bench"]
    ppparams = result["parameters"]["ppparams"]
    mean = result["mean"]
    stddev = result["stddev"]

    # Categorize ppparams
    if ppparams == "":
        param_label = "Baseline"
    elif "-Xlog:async" in ppparams:
        param_label = "-Xlog:async -Xlog:gc+heap+region=trace:file=/tmp/gc.log"
    else:
        param_label = "-Xlog:gc+heap+region=trace:file=/tmp/gc.log"

    if bench_name not in benchmarks:
        benchmarks[bench_name] = {}

    benchmarks[bench_name][param_label] = {"mean": mean, "stddev": stddev}

# Sort benchmarks by name
sorted_benchmarks = sorted(benchmarks.keys())

# Prepare data for grouped bar chart
param_labels = [
    "Baseline",
    "-Xlog:async -Xlog:gc+heap+region=trace:file=/tmp/gc.log",
    "-Xlog:gc+heap+region=trace:file=/tmp/gc.log",
]
x = np.arange(len(sorted_benchmarks))
width = 0.25  # Width of each bar

fig, ax = plt.subplots(figsize=(16, 8))

# Define colors for each configuration
colors = ["steelblue", "lightgreen", "coral"]

# Create bars for each parameter configuration
for i, param_label in enumerate(param_labels):
    means = []
    stddevs = []

    for bench in sorted_benchmarks:
        if param_label in benchmarks[bench]:
            means.append(benchmarks[bench][param_label]["mean"])
            stddevs.append(benchmarks[bench][param_label]["stddev"])
        else:
            means.append(0)
            stddevs.append(0)

    offset = (i - 1) * width
    bars = ax.bar(
        x + offset,
        means,
        width,
        yerr=stddevs,
        capsize=3,
        label=param_label,
        alpha=0.8,
        color=colors[i],
        edgecolor="black",
        linewidth=0.8,
    )

# Customize the plot
ax.set_xlabel("Benchmark", fontsize=12, fontweight="bold")
ax.set_ylabel("Mean Execution Time (seconds)", fontsize=12, fontweight="bold")
ax.set_title(
    "DaCapo Benchmark Results: JVM Logging Parameter Comparison",
    fontsize=14,
    fontweight="bold",
    pad=20,
)
ax.set_xticks(x)
ax.set_xticklabels(sorted_benchmarks, rotation=45, ha="right")
ax.legend(loc="upper left", fontsize=10)

# Add grid for better readability
ax.grid(True, axis="y", alpha=0.3, linestyle="--")
ax.set_axisbelow(True)

# Add statistics text box
total_benchmarks = len(sorted_benchmarks)
total_configs = len(param_labels)
stats_text = f'Benchmarks: {total_benchmarks} | Configurations: {total_configs}'
ax.text(0.98, 0.98, stats_text, transform=ax.transAxes,
       fontsize=10, verticalalignment='top', horizontalalignment='right',
       bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

plt.tight_layout()

# Save the figure
plt.savefig("dacapo_benchmark_results_logs.png", dpi=300, bbox_inches="tight")
print("Chart saved as 'dacapo_benchmark_results.png'")

# Also create a focused chart for just lusearch benchmark
fig2, ax2 = plt.subplots(figsize=(10, 6))

if "lusearch" in benchmarks:
    lusearch_data = benchmarks["lusearch"]
    labels = []
    means = []
    stddevs = []

    for param_label in param_labels:
        if param_label in lusearch_data:
            labels.append(param_label)
            means.append(lusearch_data[param_label]["mean"])
            stddevs.append(lusearch_data[param_label]["stddev"])

    x_lusearch = np.arange(len(labels))
    bars = ax2.bar(
        x_lusearch,
        means,
        yerr=stddevs,
        capsize=5,
        alpha=0.7,
        color=["steelblue", "lightgreen", "coral"],
        edgecolor="black",
        linewidth=1.2,
    )

    # Add value labels on bars
    for bar, mean in zip(bars, means):
        height = bar.get_height()
        ax2.text(
            bar.get_x() + bar.get_width() / 2.0,
            height,
            f"{mean:.3f}s",
            ha="center",
            va="bottom",
            fontsize=11,
            fontweight="bold",
        )

    ax2.set_xlabel("Configuration", fontsize=12, fontweight="bold")
    ax2.set_ylabel("Mean Execution Time (seconds)", fontsize=12, fontweight="bold")
    ax2.set_title(
        "Lusearch Benchmark: JVM G1HeapRegionSize Parameter Impact",
        fontsize=14,
        fontweight="bold",
        pad=20,
    )
    ax2.set_xticks(x_lusearch)
    ax2.set_xticklabels(labels)
    ax2.grid(True, axis="y", alpha=0.3, linestyle="--")
    ax2.set_axisbelow(True)

    plt.tight_layout()
    plt.savefig("lusearch_benchmark_comparison.png", dpi=300, bbox_inches="tight")
    print("Focused lusearch chart saved as 'lusearch_benchmark_comparison.png'")

# Display the plots
plt.show()
