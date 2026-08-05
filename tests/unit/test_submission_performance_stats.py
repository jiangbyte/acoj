"""Unit tests for submission performance pure stats helpers."""

from app.modules.biz.submission.performance.service import (
    MIN_SAMPLE_SIZE,
    build_histogram_buckets,
    compute_beats_pct,
)


def test_compute_beats_pct_insufficient_sample():
    metrics = [10, 20, 30, 40]
    assert len(metrics) < MIN_SAMPLE_SIZE
    assert compute_beats_pct(15, metrics) is None


def test_compute_beats_pct_strictly_better():
    metrics = [100, 200, 300, 400, 500]
    assert compute_beats_pct(250, metrics) == 60.0
    assert compute_beats_pct(500, metrics) == 0.0
    assert compute_beats_pct(50, metrics) == 100.0


def test_build_histogram_buckets_insufficient_sample():
    assert build_histogram_buckets([1, 2, 3, 4], current=2) is None


def test_build_histogram_buckets_equal_values():
    metrics = [42, 42, 42, 42, 42]
    buckets = build_histogram_buckets(metrics, current=42)
    assert buckets is not None
    assert len(buckets) == 1
    assert buckets[0].start == 42.0
    assert buckets[0].end == 42.0
    assert buckets[0].count == 5
    assert buckets[0].is_current is True


def test_build_histogram_buckets_marks_current_and_counts():
    metrics = [0, 10, 20, 30, 40]
    buckets = build_histogram_buckets(metrics, current=25, max_buckets=5)
    assert buckets is not None
    assert len(buckets) == 5
    assert sum(b.count for b in buckets) == 5
    current_buckets = [b for b in buckets if b.is_current]
    assert len(current_buckets) == 1
    assert current_buckets[0].count >= 1


def test_build_histogram_buckets_boundary_max_in_last_bucket():
    metrics = [0, 5, 10, 15, 20]
    buckets = build_histogram_buckets(metrics, current=20, max_buckets=4)
    assert buckets is not None
    assert buckets[-1].is_current is True
    assert buckets[-1].count >= 1
    assert buckets[-1].end == 20.0
