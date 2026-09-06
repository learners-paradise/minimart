# MiniMart Performance Tests (JMeter)

Three test plans, same request flow (browse → view product → checkout), different traffic shapes:

| Plan | Purpose | Default thread count | Default duration |
|---|---|---|---|
| `load-test.jmx` | Expected normal/peak traffic — verifies response-time SLAs are met | 50 | 5 min |
| `stress-test.jmx` | Push past expected peak to find the breaking point and confirm graceful failure | 300 (fast ramp) | 2 min |
| `soak-test.jmx` | Moderate load sustained a long time — catches leaks/degradation short tests miss | 20 | 30 min (bump to hours for a real soak run) |

## Prerequisites

- The app running and reachable (default `http://localhost:8080`)
- JMeter installed (`brew install jmeter`)

## Running

```bash
cd tests/performance
mkdir -p results

# Load test with defaults
jmeter -n -t load-test.jmx -l results/load-test-results.jtl

# Stress test, overriding thread count/ramp-up/duration
jmeter -n -t stress-test.jmx -Jthreads=500 -Jrampup=15 -Jduration=180 -l results/stress-test-results.jtl

# Soak test — run for real hours in a real run, e.g. 2 hours:
jmeter -n -t soak-test.jmx -Jduration=7200 -l results/soak-test-results.jtl
```

Every plan accepts `-Jhost=`, `-Jport=`, `-Jthreads=`, `-Jrampup=`, `-Jduration=` to override its defaults without editing the file.

## Viewing results

Open a `.jtl` results file in the JMeter GUI (`jmeter`, then load the corresponding `.jmx` and point its Aggregate Report listener at the file), or inspect the CSV directly — columns are `timeStamp,elapsed,label,responseCode,...,success,...`.

For a quick pass/fail signal without the GUI:

```bash
awk -F',' 'NR>1 {total++; if ($7=="false") fail++} END {printf "%d/%d failed (%.2f%%)\n", fail, total, (fail/total)*100}' results/load-test-results.jtl
```
