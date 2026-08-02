"""Elo-MMR rating math ported from DMOJ judge/ratings.py (core only)."""

from __future__ import annotations

from math import pi, sqrt, tanh
from operator import attrgetter
from typing import Callable, Iterable, TypeVar

BETA2 = 328.33**2
RATING_INIT = 1200  # used for floor/ceiling eligibility when unrated
MEAN_INIT = 1500.0
VAR_INIT = 350**2 * (BETA2 / 212**2)
SD_INIT = sqrt(VAR_INIT)
VALID_RANGE = (MEAN_INIT - 20 * SD_INIT, MEAN_INIT + 20 * SD_INIT)
VAR_PER_CONTEST = 1219.047619 * (BETA2 / 212**2)
VAR_LIM = (sqrt(VAR_PER_CONTEST**2 + 4 * BETA2 * VAR_PER_CONTEST) - VAR_PER_CONTEST) / 2
SD_LIM = sqrt(VAR_LIM)
TANH_C = sqrt(3) / pi

T = TypeVar("T")


def tie_ranker(iterable: Iterable[T], key: Callable[[T], object] = attrgetter("points")):
    rank = 0
    delta = 1
    last = None
    buf: list[T] = []
    for item in iterable:
        new = key(item)
        if new != last:
            for _ in buf:
                yield rank + (delta - 1) / 2.0
            rank += delta
            delta = 0
            buf = []
        delta += 1
        buf.append(item)
        last = new
    for _ in buf:
        yield rank + (delta - 1) / 2.0


def eval_tanhs(tanh_terms: list[tuple[float, float, float]], x: float) -> float:
    return sum((wt / sd) * tanh((x - mu) / (2 * sd)) for mu, sd, wt in tanh_terms)


def solve(
    tanh_terms: list[tuple[float, float, float]],
    y_tg: float,
    lin_factor: float = 0,
    bounds: tuple[float, float] = VALID_RANGE,
) -> float:
    L, R = bounds
    Ly, Ry = None, None
    while R - L > 2:
        x = (L + R) / 2
        y = lin_factor * x + eval_tanhs(tanh_terms, x)
        if y > y_tg:
            R, Ry = x, y
        elif y < y_tg:
            L, Ly = x, y
        else:
            return x
    if Ly is None:
        Ly = lin_factor * L + eval_tanhs(tanh_terms, L)
    if y_tg <= Ly:
        return L
    if Ry is None:
        Ry = lin_factor * R + eval_tanhs(tanh_terms, R)
    if y_tg >= Ry:
        return R
    ratio = (y_tg - Ly) / (Ry - Ly)
    return L * (1 - ratio) + R * ratio


def get_var(times_ranked: int, cache: list[float] | None = None) -> float:
    if cache is None:
        cache = [VAR_INIT]
    while times_ranked >= len(cache):
        next_var = 1.0 / (1.0 / (cache[-1] + VAR_PER_CONTEST) + 1.0 / BETA2)
        cache.append(next_var)
    return cache[times_ranked]


def approximate_mean_from_rating(rating: int, times_ranked: int) -> float:
    """Invert display formula when mean was not persisted."""
    return float(rating) + (sqrt(get_var(times_ranked + 1)) - SD_LIM)


def recalculate_ratings(
    ranking: list[float],
    old_mean: list[float],
    times_ranked: list[int],
    historical_p: list[list[float]],
    perf_ceiling: float | None,
) -> tuple[list[int], list[float], list[float]]:
    n = len(ranking)
    new_p = [0.0] * n
    new_mean = [0.0] * n

    updated_bounds = list(VALID_RANGE)
    if perf_ceiling is not None:
        updated_bounds[1] = min(updated_bounds[1], float(perf_ceiling))

    delta = [TANH_C * sqrt(get_var(t) + VAR_PER_CONTEST + BETA2) for t in times_ranked]
    p_tanh_terms = [(m, d, 1) for m, d in zip(old_mean, delta)]

    def solve_idx(i: int, bounds: tuple[float, float]) -> None:
        r = ranking[i]
        y_tg = 0.0
        for d, s in zip(delta, ranking):
            if s > r:
                y_tg += 1.0 / d
            elif s < r:
                y_tg -= 1.0 / d
        new_p[i] = solve(p_tanh_terms, y_tg, bounds=bounds)

    def divconq(i: int, j: int) -> None:
        if j - i > 1:
            k = (i + j) // 2
            solve_idx(k, bounds=(new_p[j], new_p[i]))
            divconq(i, k)
            divconq(k, j)

    if n < 2:
        new_p = list(old_mean)
        new_mean = list(old_mean)
    else:
        solve_idx(0, (updated_bounds[0], updated_bounds[1]))
        solve_idx(n - 1, (updated_bounds[0], updated_bounds[1]))
        divconq(0, n - 1)

        for i, r in enumerate(ranking):
            tanh_terms: list[tuple[float, float, float]] = []
            w_prev = 1.0
            w_sum = 0.0
            for j, h in enumerate([new_p[i]] + historical_p[i]):
                gamma2 = VAR_PER_CONTEST if j > 0 else 0
                h_var = get_var(times_ranked[i] + 1 - j)
                k = h_var / (h_var + gamma2)
                w = w_prev * k**2
                tanh_terms.append((h, sqrt(BETA2) * TANH_C, w))
                w_prev = w
                w_sum += w / BETA2
            w0 = 1.0 / get_var(times_ranked[i] + 1) - w_sum
            p0 = eval_tanhs(tanh_terms[1:], old_mean[i]) / w0 + old_mean[i]
            new_mean[i] = solve(tanh_terms, w0 * p0, lin_factor=w0, bounds=(updated_bounds[0], updated_bounds[1]))

    new_rating = [max(1, round(m - (sqrt(get_var(t + 1)) - SD_LIM))) for m, t in zip(new_mean, times_ranked)]
    return new_rating, new_mean, new_p
