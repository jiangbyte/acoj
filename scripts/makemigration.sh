#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "Usage: scripts/makemigration.sh \"migration message\"" >&2
  exit 1
fi

alembic revision --autogenerate -m "$*"

echo
echo "Structure migration generated. Review migrations/versions/*.py before running scripts/migrate.sh."
