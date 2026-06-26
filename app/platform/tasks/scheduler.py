from app.platform.tasks.celery_app import celery_app

celery_app.conf.beat_schedule = {
    "flush-banner-interactions-every-5-minutes": {
        "task": "banner.flush_interactions",
        "schedule": 300.0,
    }
}
