from __future__ import annotations

from collections.abc import Callable
from typing import TypeVar

from anyio.to_thread import run_sync

T = TypeVar("T")


async def run_blocking(func: Callable[..., T], /, *args, **kwargs) -> T:
    return await run_sync(lambda: func(*args, **kwargs))
