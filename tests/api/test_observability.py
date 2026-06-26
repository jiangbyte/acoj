async def test_metrics_endpoint_disabled_by_default(client):
    response = await client.get("/metrics")
    assert response.status_code == 404


async def test_request_id_header_present(client):
    response = await client.get("/")
    assert response.status_code == 200
    assert response.headers.get("X-Request-Id")


async def test_metrics_endpoint_enabled(metrics_client):
    response = await metrics_client.get("/metrics")
    assert response.status_code == 200
    assert "http_requests_total" in response.text


async def test_ready_endpoint_shape(client):
    response = await client.get("/api/v1/internal/health/ready")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in {"ready", "not_ready"}
    assert "database" in data["checks"]
    assert "redis" in data["checks"]
    assert "celery_broker" in data["checks"]
    assert "storage" in data["checks"]
    assert "enabled" in data["checks"]["database"]
    assert "ok" in data["checks"]["database"]
    assert "detail" in data["checks"]["database"]


async def test_ready_endpoint_marks_disabled_components(client):
    response = await client.get("/api/v1/internal/health/ready")
    data = response.json()
    if data["checks"]["redis"]["enabled"] is False:
        assert data["checks"]["redis"]["ok"] is False
        assert data["checks"]["redis"]["detail"] == "redis disabled by configuration"
