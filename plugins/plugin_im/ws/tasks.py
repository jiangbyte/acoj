from __future__ import annotations

import asyncio
import logging
from collections.abc import Awaitable

logger = logging.getLogger(__name__)


def schedule(coro: Awaitable) -> None:
    from plugins.plugin_im import ws as im_ws

    hub = im_ws.GlobalCrossHub
    if hub is not None:
        hub.create_task(coro)
        return

    try:
        loop = asyncio.get_running_loop()
    except RuntimeError:
        logger.debug("IM task dropped because no event loop is running")
        return
    loop.create_task(coro)


__all__ = ["schedule"]
