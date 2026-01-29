#!/usr/bin/env python3
"""
Visualize hyperfine benchmark results from out.json
"""

import json
import sys

import matplotlib.pyplot as plt
import numpy as np

# Read the JSON file
with open(sys.argv[1], "r") as f:
    data = json.load(f)

# Extract data for visualization
commands = []
means = []
stddevs = []
num_threads = []

for result in data["results"]:
    # Extract command name (e.g., "echo 1" -> "1")
    cmd = result["command"].split()[-1]
    commands.append(cmd)

    # Extract mean and stddev (convert to milliseconds for better readability)
    means.append(result["mean"] * 1000)  # Convert to ms
    stddevs.append(result["stddev"] * 1000)  # Convert to ms

    # Extract num_threads parameter
    num_threads.append(result["parameters"]["num_threads"])

# Create the bar chart
fig, ax = plt.subplots(figsize=(12, 6))

x = np.arange(len(commands))
bars = ax.bar(
    x,
    means,
    yerr=stddevs,
    capsize=5,
    alpha=0.7,
    color="steelblue",
    edgecolor="black",
    linewidth=1.2,
)

# Customize the plot
ax.set_xlabel("Number of Threads", fontsize=12, fontweight="bold")
ax.set_ylabel("Mean Execution Time (ms)", fontsize=12, fontweight="bold")
ax.set_title(
    "Hyperfine Benchmark Results: Execution Time vs Thread Count",
    fontsize=14,
    fontweight="bold",
    pad=20,
)
ax.set_xticks(x)
ax.set_xticklabels(num_threads)

# Add grid for better readability
ax.grid(True, axis="y", alpha=0.3, linestyle="--")
ax.set_axisbelow(True)

# Add value labels on top of bars
for i, (bar, mean, stddev) in enumerate(zip(bars, means, stddevs)):
    height = bar.get_height()
    ax.text(
        bar.get_x() + bar.get_width() / 2.0,
        height + stddev,
        f"{mean:.3f}",
        ha="center",
        va="bottom",
        fontsize=8,
        rotation=0,
    )

# Add statistics text box
total_runs = sum(len(result["times"]) for result in data["results"])
stats_text = f"Total benchmark runs: {total_runs}"
ax.text(
    0.02,
    0.98,
    stats_text,
    transform=ax.transAxes,
    fontsize=10,
    verticalalignment="top",
    bbox=dict(boxstyle="round", facecolor="wheat", alpha=0.5),
)

plt.tight_layout()

# Save the figure
plt.savefig("benchmark_results.png", dpi=300, bbox_inches="tight")
print("Chart saved as 'benchmark_results.png'")

# Display the plot
plt.show()
