from app.platform.tasks.celery_app import celery_app

celery_app.conf.beat_schedule = {
    "flush-banner-interactions-every-5-minutes": {
        "task": "banner.flush_interactions",
        "schedule": 300.0,
    },
    "purge-cancelled-accounts-daily": {
        "task": "account.purge_cancelled_accounts",
        "schedule": 86400.0,
    },
}
