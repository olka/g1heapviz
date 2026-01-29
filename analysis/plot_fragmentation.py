import sys

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

if len(sys.argv) < 2:
    print("Usage: python plot_fragmentation.py <frag_format.txt>")
    sys.exit(1)

# Read the data
data = pd.read_csv(
    sys.argv[1],
    skiprows=1,
    names=["gc_count", "frag_before", "frag_after", "is_full_gc"],
)
data = data.apply(pd.to_numeric, errors="coerce").dropna()

# Create figure with high DPI for publication quality
fig, ax = plt.subplots(figsize=(10, 6), dpi=300)

# Plot fragmentation before and after GC
ax.plot(
    data["gc_count"],
    data["frag_before"],
    marker="o",
    markersize=4,
    linewidth=1.5,
    label="Fragmentation before GC",
    color="#E74C3C",
    alpha=0.8,
)

ax.plot(
    data["gc_count"],
    data["frag_after"],
    marker="s",
    markersize=4,
    linewidth=1.5,
    label="Fragmentation after GC",
    color="#3498DB",
    alpha=0.8,
)

# Highlight full GC events
full_gc_events = data[data["is_full_gc"] == 1]
if not full_gc_events.empty:
    ax.axvline(
        x=full_gc_events["gc_count"].values[0],
        color="#2ECC71",
        linestyle="--",
        linewidth=2,
        label="Full GC",
        alpha=0.7,
    )
    # Add vertical spans for full GC events
    for gc_count in full_gc_events["gc_count"]:
        ax.axvspan(gc_count - 0.5, gc_count + 0.5, alpha=0.15, color="#2ECC71")

# Formatting for scientific paper
ax.set_xlabel("GC Event Count", fontsize=12, fontweight="bold")
ax.set_ylabel("External Fragmentation", fontsize=12, fontweight="bold")
ax.set_title(
    "Memory Fragmentation Before and After Garbage Collection",
    fontsize=14,
    fontweight="bold",
    pad=20,
)

# Grid for readability
ax.grid(True, alpha=0.3, linestyle="--", linewidth=0.5)

# Legend
ax.legend(loc="upper left", frameon=True, shadow=True, fontsize=10, framealpha=0.9)

# Set axis limits with some padding
ax.set_xlim(data["gc_count"].min() - 2, data["gc_count"].max() + 2)
ax.set_ylim(-2, data[["frag_before", "frag_after"]].max().max() + 5)

# Tick parameters
ax.tick_params(axis="both", which="major", labelsize=10)

# Tight layout to prevent label cutoff
plt.tight_layout()

# Save the figure in multiple formats for publication
plt.savefig("fragmentation_analysis.png", dpi=300, bbox_inches="tight")
plt.savefig("fragmentation_analysis.pdf", bbox_inches="tight")

print("Figures saved:")
print("  - fragmentation_analysis.png (high-res raster)")
print("  - fragmentation_analysis.pdf (vector format)")

# Display statistics
print(f"\nData Statistics:")
print(f"Total GC events: {len(data)}")
print(f"Full GC events: {data['is_full_gc'].sum()}")
print(f"Average fragmentation before GC: {data['frag_before'].mean():.2f}")
print(f"Average fragmentation after GC: {data['frag_after'].mean():.2f}")
print(f"Max fragmentation before GC: {data['frag_before'].max()}")
print(f"Max fragmentation after GC: {data['frag_after'].max()}")

# Show the plot
plt.show()
