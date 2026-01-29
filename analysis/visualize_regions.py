#!/usr/bin/env python3
"""
Visualize DaCapo benchmark results from baseline_lusearch_param.json
"""

import json
import sys

import matplotlib.pyplot as plt
import numpy as np

# Configure matplotlib for publication quality
plt.rcParams["font.family"] = "serif"
plt.rcParams["font.serif"] = ["Times New Roman", "Times", "DejaVu Serif"]
plt.rcParams["font.size"] = 9
plt.rcParams["axes.labelsize"] = 9
plt.rcParams["axes.titlesize"] = 10
plt.rcParams["xtick.labelsize"] = 8
plt.rcParams["ytick.labelsize"] = 8
plt.rcParams["legend.fontsize"] = 8
plt.rcParams["figure.titlesize"] = 10
plt.rcParams["pdf.fonttype"] = 42  # TrueType fonts for PDF
plt.rcParams["ps.fonttype"] = 42

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
    if ppparams == "1M":
        param_label = "1M"
    elif "16M" in ppparams:
        param_label = "Baseline"
    else:
        param_label = "32M"

    if bench_name not in benchmarks:
        benchmarks[bench_name] = {}

    benchmarks[bench_name][param_label] = {"mean": mean, "stddev": stddev}

# Sort benchmarks by name
sorted_benchmarks = sorted(benchmarks.keys())

# Prepare data for grouped bar chart
# Simplified labels for publication
param_labels_full = ["1M", "Baseline", "32M"]
param_labels_short = ["1M", "Baseline", "32M"]
x = np.arange(len(sorted_benchmarks))
width = 0.25  # Width of each bar

# Two-column figure size (7 inches width for full width)
fig, ax = plt.subplots(figsize=(7, 4))

# Grayscale colors and hatching patterns for print compatibility
colors = ["0.9", "0.6", "0.3"]  # Light gray to dark gray
hatches = ["", "///", "xxx"]

# Create bars for each parameter configuration
for i, (param_label_full, param_label_short) in enumerate(
    zip(param_labels_full, param_labels_short)
):
    means = []
    stddevs = []

    for bench in sorted_benchmarks:
        if param_label_full in benchmarks[bench]:
            means.append(benchmarks[bench][param_label_full]["mean"])
            stddevs.append(benchmarks[bench][param_label_full]["stddev"])
        else:
            means.append(0)
            stddevs.append(0)

    offset = (i - 1) * width
    bars = ax.bar(
        x + offset,
        means,
        width,
        yerr=stddevs,
        capsize=2,
        label=param_label_short,
        color=colors[i],
        hatch=hatches[i],
        edgecolor="black",
        linewidth=0.5,
        error_kw={"linewidth": 0.5},
    )

# Customize the plot
ax.set_xlabel("Benchmark")
ax.set_ylabel("Execution Time (s)")
ax.set_xticks(x)
ax.set_xticklabels(sorted_benchmarks, ha="center")
ax.legend(loc="upper right", frameon=True, fancybox=False, edgecolor="black")

# Add subtle grid for readability
ax.grid(True, axis="y", alpha=0.3, linestyle=":", linewidth=0.5, color="gray")
ax.set_axisbelow(True)

# Remove top and right spines for cleaner look
ax.spines["top"].set_visible(False)
ax.spines["right"].set_visible(False)

plt.tight_layout()

# Save as PDF (vector format) for publication
plt.savefig("dacapo_benchmark_results.pdf", dpi=300, bbox_inches="tight")
print("Chart saved as 'dacapo_benchmark_results.pdf'")

# Also create a focused chart for just lusearch benchmark
# Single column figure size (3.5 inches width)
fig2, ax2 = plt.subplots(figsize=(3.5, 2.5))

if "lusearch" in benchmarks:
    lusearch_data = benchmarks["lusearch"]
    labels = []
    means = []
    stddevs = []

    for param_label_full, param_label_short in zip(
        param_labels_full, param_labels_short
    ):
        if param_label_full in lusearch_data:
            labels.append(param_label_short)
            means.append(lusearch_data[param_label_full]["mean"])
            stddevs.append(lusearch_data[param_label_full]["stddev"])

    x_lusearch = np.arange(len(labels))
    bars = ax2.bar(
        x_lusearch,
        means,
        yerr=stddevs,
        capsize=2,
        color=colors,
        hatch=hatches,
        edgecolor="black",
        linewidth=0.5,
        error_kw={"linewidth": 0.5},
    )

    # Add value labels on bars (smaller font for publication)
    for bar, mean in zip(bars, means):
        height = bar.get_height()
        ax2.text(
            bar.get_x() + bar.get_width() / 2.0,
            height,
            f"{mean:.2f}",
            ha="center",
            va="bottom",
            fontsize=7,
        )

    ax2.set_xlabel("Configuration")
    ax2.set_ylabel("Execution Time (s)")
    ax2.set_xticks(x_lusearch)
    ax2.set_xticklabels(labels, ha="center")

    # Add subtle grid
    ax2.grid(True, axis="y", alpha=0.3, linestyle=":", linewidth=0.5, color="gray")
    ax2.set_axisbelow(True)

    # Remove top and right spines
    ax2.spines["top"].set_visible(False)
    ax2.spines["right"].set_visible(False)

    plt.tight_layout()
    plt.savefig("lusearch_benchmark_comparison.pdf", dpi=300, bbox_inches="tight")
    print("Focused lusearch chart saved as 'lusearch_benchmark_comparison.pdf'")

# Display the plots
plt.show()
