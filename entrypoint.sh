#!/bin/sh
set -e

celery -A app.worker.main:celery_app worker \
    --without-mingle --without-gossip \
    --loglevel "${CELERY__WORKER_LOG_LEVEL:-INFO}" &

celery -A app.worker.main:celery_app beat \
    --loglevel "${CELERY__BEAT_LOG_LEVEL:-INFO}" \
    --scheduler redbeat.RedBeatScheduler &

exec gunicorn app.main:app -c gunicorn.conf.py
