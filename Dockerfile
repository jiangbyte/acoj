FROM python:3.12-slim AS builder

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    VIRTUAL_ENV=/opt/venv

WORKDIR /app

# Build toolchain for asyncmy (Cython/C) in case no prebuilt wheel matches.
RUN apt-get update \
    && apt-get install -y --no-install-recommends gcc build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN python -m venv "$VIRTUAL_ENV"
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

COPY requirements-prod.txt .
RUN python -m pip install --upgrade pip \
    && python -m pip install -r requirements-prod.txt


FROM python:3.12-slim AS runtime

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    VIRTUAL_ENV=/opt/venv \
    PATH="/opt/venv/bin:$PATH" \
    APP__HOST=0.0.0.0 \
    APP__PORT=18886

WORKDIR /app

RUN addgroup --system hei \
    && adduser --system --ingroup hei --home /app hei \
    && mkdir -p /app/uploads \
    && chown -R hei:hei /app

COPY --from=builder /opt/venv /opt/venv
COPY --chown=hei:hei main.py gunicorn.conf.py pyproject.toml .env.example ./
COPY --chown=hei:hei sdk ./sdk
COPY --chown=hei:hei plugins ./plugins
COPY --chown=hei:hei cli ./cli
COPY --chown=hei:hei scripts ./scripts

USER hei

EXPOSE 18886

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD python -c "import os, urllib.request; port=os.environ.get('APP__PORT','18886'); urllib.request.urlopen(f'http://127.0.0.1:{port}/health/live', timeout=3).read()" || exit 1

CMD ["gunicorn", "main:app", "-c", "gunicorn.conf.py"]
