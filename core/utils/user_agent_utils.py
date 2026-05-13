from typing import Optional
from user_agents import parse


def get_browser(user_agent: Optional[str]) -> str:
    """Extract browser name from User-Agent string."""
    if not user_agent:
        return "-"
    try:
        ua = parse(user_agent)
        browser = ua.browser.family or "-"
        if len(browser) > 250:
            browser = browser[:250]
        return "-" if browser == "Unknown" else browser
    except Exception:
        return "-"


def get_os(user_agent: Optional[str]) -> str:
    """Extract OS name from User-Agent string."""
    if not user_agent:
        return "-"
    try:
        ua = parse(user_agent)
        os_name = ua.os.family or "-"
        if len(os_name) > 250:
            os_name = os_name[:250]
        return "-" if os_name == "Unknown" else os_name
    except Exception:
        return "-"
