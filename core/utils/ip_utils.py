from typing import Optional
from fastapi import Request

UNKNOWN = "unknown"


def get_client_ip(request: Request) -> str:
    for header in ["X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"]:
        ip = request.headers.get(header)
        if ip and ip != UNKNOWN:
            return ip.split(",")[0].strip()
    
    if request.client:
        return request.client.host
    return UNKNOWN
