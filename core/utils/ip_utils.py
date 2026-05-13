import logging
import os
from typing import Optional
from fastapi import Request

logger = logging.getLogger(__name__)

UNKNOWN = "unknown"
IP2REGION_DB_PATH = os.path.join(os.path.dirname(__file__), "ip2region.xdb")

_searcher = None


def _init_searcher():
    """Initialize ip2region searcher (load xdb into memory)."""
    global _searcher
    try:
        from ip2region.searcher import new_with_buffer
        from ip2region.util import load_content_from_file, version_from_name

        db_path = IP2REGION_DB_PATH
        if not os.path.exists(db_path):
            logger.warning("ip2region.xdb not found at %s", db_path)
            return None

        c_buffer = load_content_from_file(db_path)
        searcher = new_with_buffer(version_from_name("IPv4"), c_buffer)
        return searcher
    except Exception as e:
        logger.error("Failed to init ip2region searcher: %s", e)
        return None


def get_searcher():
    """Lazy-init singleton access to the ip2region searcher."""
    global _searcher
    if _searcher is None:
        _searcher = _init_searcher()
    return _searcher


def get_client_ip(request: Request) -> str:
    for header in ["X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"]:
        ip = request.headers.get(header)
        if ip and ip != UNKNOWN:
            return ip.split(",")[0].strip()

    if request.client:
        return request.client.host
    return UNKNOWN


def get_city_info(ip: str) -> str:
    """Get city info for an IP address using ip2region offline database.

    Returns a string like "中国|江苏|苏州" or "未知" on failure.
    """
    if not ip or ip == UNKNOWN or ip == "127.0.0.1" or ip == "::1":
        return ""

    searcher = get_searcher()
    if not searcher:
        return ""

    try:
        region = searcher.search(ip)
        if region:
            return region.replace("0|", "").replace("|0", "")
        return ""
    except Exception as e:
        logger.debug("ip2region search failed for %s: %s", ip, e)
        return ""
