"""Worker tasks."""

from app.modules.banner.tasks import flush_banner_interactions

__all__ = ["flush_banner_interactions"]
