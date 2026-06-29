from celery import Celery

from app.core.config.settings import settings

celery_app = Celery(
    "hei-fastapi",
    broker=settings.celery.broker_url,
    include=["app.worker.tasks"],
)
celery_app.conf.task_default_queue = "default"

from app.platform.tasks import scheduler  # noqa: E402,F401
