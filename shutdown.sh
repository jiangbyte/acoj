#!/bin/sh
set -e

echo "Stopping all services started by entrypoint.sh..."

if pkill -f "celery.*app\.worker\.main:celery_app.*worker" 2>/dev/null; then
  echo "  celery worker   stopped"
else
  echo "  celery worker   not found"
fi

if pkill -f "celery.*app\.worker\.main:celery_app.*beat" 2>/dev/null; then
  echo "  celery beat     stopped"
else
  echo "  celery beat     not found"
fi

if pkill -f "gunicorn.*app\.main:app" 2>/dev/null; then
  echo "  gunicorn        stopped"
else
  echo "  gunicorn        not found"
fi

echo "Done."
