from celery import Celery

from app.core.config.settings import settings

celery_app = Celery(
    "hei-fastapi",
    broker=settings.celery.broker_url,
    backend=settings.redis.url,
    include=["app.worker.tasks"],
)
celery_app.conf.task_default_queue = "default"
celery_app.conf.worker_enable_remote_control = settings.celery.worker_remote_control_enabled
celery_app.conf.worker_cancel_long_running_tasks_on_connection_loss = (
    settings.celery.worker_cancel_long_running_tasks_on_connection_loss
)
celery_app.conf.redbeat_redis_url = settings.redis.url
celery_app.conf.redbeat_lock_key = "redbeat:lock"
celery_app.conf.redbeat_lock_timeout = 30

from app.platform.tasks.redbeat_scheduler import sync_to_redbeat  # noqa: E402
sync_to_redbeat(celery_app)
