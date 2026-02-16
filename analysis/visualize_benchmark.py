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

for result in data['results']:
    bench_name = result['parameters']['benchmark']
    ppparams = result['parameters']['ppparams']
    mean = result['mean']
    fragm_1 = result['fragm_1']
    fragm_2 = result['fragm_2']
    stddev = result['stddev']

    # Categorize ppparams
    if ppparams == "1M":
        param_label = "1M RegionSize"
    elif "8M" in ppparams:
        param_label = "8M RegionSize"
    elif "16M" in ppparams:
        param_label = "16M RegionSize"
    else:
        param_label = "32M RegionSize"

    if bench_name not in benchmarks:
        benchmarks[bench_name] = {}

    benchmarks[bench_name][param_label] = {
        'mean': mean,
        'fragm_1': fragm_1,
        'fragm_2': fragm_2,
        'stddev': stddev
    }

# Sort benchmarks by name
sorted_benchmarks = benchmarks.keys()
# Sort benchmarks by mean


# Prepare data for grouped bar chart
param_labels = ["1M RegionSize", "8M RegionSize", "16M RegionSize", "32M RegionSize"]
x = np.arange(len(sorted_benchmarks))
width = 0.20  # Width of each bar

fig, ax = plt.subplots(figsize=(16, 8))

# Define colors for each configuration
colors = ['coral', 'steelblue', 'lightgreen', 'wheat']
fragm_1 = [[i for i in range(0)]for i in range(4)]
print(fragm_1)
fragm_2 = [[i for i in range(0)]for i in range(4)]
print(fragm_2)

# Create bars for each parameter configuration
for i, param_label in enumerate(param_labels):
    means = []
    stddevs = []

    for bench in sorted_benchmarks:
        if param_label in benchmarks[bench]:
            means.append(benchmarks[bench][param_label]['mean'])
            fragm_1[i].append(benchmarks[bench][param_label]['fragm_1'])
            fragm_2[i].append(benchmarks[bench][param_label]['fragm_2'])
            stddevs.append(benchmarks[bench][param_label]['stddev'])
        else:
            means.append(0)
            fragm_1.append(0)
            fragm_2.append(0)
            stddevs.append(0)

    offset = (i - 1) * width
    bars = ax.bar(x + offset, means, width, yerr=stddevs, capsize=3,
                  label=param_label, alpha=0.8, color=colors[i],
                  edgecolor='black', linewidth=0.8)
    markerline, stemlines, baseline = ax.stem(x + offset, fragm_2[i], 'b', markerfmt='bx', linefmt=':')
    plt.setp(stemlines, 'linewidth', 0.9)

    #plot2 = ax.plot(x[4:7] + offset, fragm_2[4:7])




for i in range(11):
    offset = (i*5 - 1) * width
    plot1 = ax.plot([offset, offset+width, offset+2*width, offset+3*width], [fragm_2[0][i],fragm_2[1][i],fragm_2[2][i],fragm_2[3][i]], color='grey', linewidth=0.6, label='Ext. fragmentation')





# Customize the plot
ax.set_xlabel('Benchmark', fontsize=12, fontweight='bold')
ax.set_ylabel('Mean Execution Time (seconds)', fontsize=12, fontweight='bold')
ax.set_title('DaCapo Benchmark Results: JVM G1HeapRegionSize Parameter Comparison',
             fontsize=14, fontweight='bold', pad=20)
ax.set_xticks(x)
ax.set_xticklabels(sorted_benchmarks, ha="left")
handles, labels = plt.gca().get_legend_handles_labels()
labels, ids = np.unique(labels, return_index=True)
handles = [handles[i] for i in ids]
ax.legend(handles, labels, loc='best', fontsize=14)

#ax.legend(loc='upper right', fontsize=11)

# Add grid for better readability
ax.grid(True, axis='y', alpha=0.3, linestyle='--')
ax.set_axisbelow(True)

# Add statistics text box
total_benchmarks = len(sorted_benchmarks)
total_configs = len(param_labels)
# stats_text = f'Benchmarks: {total_benchmarks} | Configurations: {total_configs}'
# ax.text(0.98, 0.98, stats_text, transform=ax.transAxes,
#        fontsize=10, verticalalignment='top', horizontalalignment='right',
#        bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

plt.tight_layout()

# Save the figure
plt.savefig('dacapo_benchmark_results.png', dpi=300, bbox_inches='tight')
print("Chart saved as 'dacapo_benchmark_results.png'")

# Also create a focused chart for just lusearch benchmark
fig2, ax2 = plt.subplots(figsize=(10, 6))

if 'eclipse' in benchmarks:
    lusearch_data = benchmarks['eclipse']
    labels = []
    means = []
    stddevs = []

    for param_label in param_labels:
        if param_label in lusearch_data:
            labels.append(param_label)
            means.append(lusearch_data[param_label]['mean'])
            stddevs.append(lusearch_data[param_label]['stddev'])

    x_lusearch = np.arange(len(labels))
    bars = ax2.bar(x_lusearch, means, yerr=stddevs, capsize=5, alpha=0.7,
                   color=['steelblue', 'lightgreen', 'coral'],
                   edgecolor='black', linewidth=1.2)

    # Add value labels on bars
    for bar, mean in zip(bars, means):
        height = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width() / 2., height,
                f'{mean:.3f}s',
                ha='center', va='bottom', fontsize=11, fontweight='bold')

    ax2.set_xlabel('Configuration', fontsize=12, fontweight='bold')
    ax2.set_ylabel('Mean Execution Time (seconds)', fontsize=12, fontweight='bold')
    ax2.set_title('Lusearch Benchmark: JVM G1HeapRegionSize Parameter Impact',
                 fontsize=14, fontweight='bold', pad=20)
    ax2.set_xticks(x_lusearch)
    ax2.set_xticklabels(labels)
    ax2.grid(True, axis='y', alpha=0.3, linestyle='--')
    ax2.set_axisbelow(True)

    plt.tight_layout()
    plt.savefig('benchmark_comparison.png', dpi=300, bbox_inches='tight')
    print("Focused eclipse chart saved as 'leclipse_benchmark_comparison.png'")

# Display the plots
plt.show()
