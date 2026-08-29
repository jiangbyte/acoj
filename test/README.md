# test — Load Testing & Test Data

This directory provides **test-user generation**, **batch register / login**, and **k6** concurrency tests with result aggregation. It is for capacity evaluation of a deployment—not a product feature module.

## Layout

```text
test/
├── User Generate/          # Generate user CSV, batch register, batch login + write tokens
│   ├── user_gen.py
│   ├── register.py
│   ├── login.py
│   └── 测试用户数据_1000个.csv
└── ojtest/                 # k6 suites (by concurrency) and analysis scripts
    ├── 100/ … 250/         # Per-concurrency scripts and historical results
    ├── analyze_k6_batch.py
    └── performance_summary_cn_*.csv
```

## Prerequisites

| Purpose | Dependencies |
| --- | --- |
| User scripts | Python 3; `pandas`, `requests`; register also needs `matplotlib`, `seaborn`, `numpy`; generator needs `faker` |
| Load tests | [k6](https://k6.io/) |
| Analysis | Python 3; `pandas`, `matplotlib`, `seaborn`, `numpy` (Chinese font such as SimHei optional) |

The target `oj` service must be reachable.

## 1. Prepare Test Users

Work under `User Generate`:

```bash
cd "test/User Generate"
```

### 1.1 Generate user CSV

```bash
python user_gen.py
```

Default output: `测试用户数据_1000个.csv` with columns `ID`, `用户名`, `邮箱`, `密码`.

### 1.2 Batch register

```bash
python register.py
```

- Default URL: `http://localhost:89/api/v1/sys/auth/register`
- You can enter concurrency when prompted; results go to a timestamped `register_test_results_*` directory

If the backend is not local port 89, edit `url` in the script first.

### 1.3 Batch login and write tokens

```bash
python login.py
```

- Reads `测试用户数据_1000个.csv` in the same directory
- On success, writes tokens back into the CSV for k6 and later steps
- **Change the login URL in the script to your target environment first** (the repo may still point at a remote sample host)

Captcha, `platform`, and related fields must match the backend’s test policy (scripts use fixed test captcha values).

## 2. k6 Concurrency Tests

### 2.1 Pick a concurrency tier

Under `ojtest`, directories are named by target concurrency, e.g. `100`, `120`, `150`, `180`, `200`, `220`, `250`. Each usually contains:

| File | Description |
| --- | --- |
| `optimized-oj-test.js` | k6 scenario script |
| `run-k6-tests.ps1` | Windows helper to run multiple rounds |
| `data-*.json` / `summary-*.txt` | Historical run outputs (ignore or clear before retesting) |

### 2.2 Point the script at your environment

Edit `optimized-oj-test.js` in the chosen directory and confirm at least:

- `baseURL` / `serverIP` / `serverPORT` → target `oj`
- `testProblemId` → a real problem ID in that environment
- `captchaCode` → matches the test captcha policy
- Token data source → matches your CSV / SharedArray load path

### 2.3 Run

Single run:

```bash
cd test/ojtest/100
k6 run optimized-oj-test.js
```

Multiple rounds (PowerShell; default 10 runs, 10s interval):

```powershell
cd test\ojtest\100
.\run-k6-tests.ps1
```

Other tiers work the same: enter the numeric directory and run.

## 3. Aggregate Results

From `ojtest`, run the analyzer to scan result JSON under concurrency folders and produce summary tables / charts:

```bash
cd test/ojtest
python analyze_k6_batch.py
```

Typical outputs (as produced by the script):

- `performance_summary_cn_vertical.csv` / `performance_summary_cn_transpose.csv`
- Latency and performance charts (e.g. `performance_analysis_pro.png`)

## Notes

1. **Only load-test environments you are authorized to use**—never production or third-party systems without permission.
2. Before submit-heavy scenarios, ensure the judge path (`oj` + RabbitMQ + `judge-service`) is ready; otherwise results are meaningless.
3. IPs, ports, problem IDs, and captchas in scripts are samples or leftovers—**update them for your environment every time**.
4. Batch registration writes real DB rows; use a dedicated or disposable test database.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../oj/README.md](../oj/README.md)
