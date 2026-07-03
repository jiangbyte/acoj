#FROM python:3.11-slim
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/python:3.11-slim
#FROM docker.xuanyuan.run/library/python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    APP__HOST=0.0.0.0 \
    APP__PORT=8000 \
    STORAGE__PROVIDER=local \
    STORAGE__LOCAL_ROOT=/app/storage \
    STORAGE__PUBLIC_PATH=/api/v1/files

ENV PIP_INDEX_URL=https://mirrors.aliyun.com/pypi/simple/ \
    PIP_EXTRA_INDEX_URL=https://pypi.org/simple \
    PIP_DEFAULT_TIMEOUT=120 \
    PIP_RETRIES=5

WORKDIR /app

COPY pyproject.toml README.md alembic.ini ./
COPY app ./app
COPY migrations ./migrations

RUN pip install --no-cache-dir ".[postgres]"
RUN mkdir -p /app/storage

VOLUME ["/app/storage"]

EXPOSE 8000

CMD ["python", "-m", "app.main"]
