from __future__ import annotations

from collections import defaultdict
from typing import Callable, TypeVar


T = TypeVar("T")


def build_tree(
    nodes: list[T],
    *,
    get_id: Callable[[T], str],
    get_parent_id: Callable[[T], str],
    get_children: Callable[[T], list[T]],
    get_sort_code: Callable[[T], int | None],
) -> list[T]:
    node_map = {get_id(node): node for node in nodes}
    roots: list[T] = []

    for node in nodes:
        parent_id = get_parent_id(node)
        parent = node_map.get(parent_id) if parent_id else None
        if parent is None:
            roots.append(node)
            continue
        get_children(parent).append(node)

    _sort_tree(roots, get_children, get_sort_code)
    return roots


def collect_descendant_ids(
    rows: list[T],
    ids: list[str],
    *,
    get_id: Callable[[T], str],
    get_parent_id: Callable[[T], str],
) -> list[str]:
    children_map: dict[str, list[str]] = defaultdict(list)
    for row in rows:
        children_map[get_parent_id(row)].append(get_id(row))

    all_ids = set(ids)
    stack = list(ids)
    while stack:
        parent_id = stack.pop()
        for child_id in children_map.get(parent_id, []):
            if child_id in all_ids:
                continue
            all_ids.add(child_id)
            stack.append(child_id)
    return list(all_ids)


def _sort_tree(
    nodes: list[T],
    get_children: Callable[[T], list[T]],
    get_sort_code: Callable[[T], int | None],
) -> None:
    nodes.sort(key=lambda item: get_sort_code(item) or 0)
    for node in nodes:
        children = get_children(node)
        if children:
            _sort_tree(children, get_children, get_sort_code)
