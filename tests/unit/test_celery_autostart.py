from __future__ import annotations

from app.core.config.settings import settings
from app.platform.tasks.autostart import CELERY_APP_PATH, CeleryProcessManager


class FakeProcess:
    pid = 12345

    def poll(self) -> int | None:
        return None

    def wait(self) -> int:
        return 0


def test_celery_autostart_builds_worker_command(monkeypatch) -> None:
    monkeypatch.setattr(settings.celery, "worker_log_level", "DEBUG")
    monkeypatch.setattr(settings.celery, "worker_pool", "solo")
    monkeypatch.setattr(settings.celery, "worker_concurrency", 2)
    monkeypatch.setattr(settings.celery, "worker_without_mingle", True)
    monkeypatch.setattr(settings.celery, "worker_without_gossip", True)

    command = CeleryProcessManager()._worker_command()

    assert command[2:6] == ["celery", "-A", CELERY_APP_PATH, "worker"]
    assert "--without-mingle" in command
    assert "--without-gossip" in command
    assert command[-6:] == ["--loglevel", "DEBUG", "--pool", "solo", "--concurrency", "2"]


def test_celery_autostart_can_keep_worker_cluster_features(monkeypatch) -> None:
    monkeypatch.setattr(settings.celery, "worker_without_mingle", False)
    monkeypatch.setattr(settings.celery, "worker_without_gossip", False)

    command = CeleryProcessManager()._worker_command()

    assert "--without-mingle" not in command
    assert "--without-gossip" not in command


def test_celery_autostart_starts_worker_and_beat(monkeypatch) -> None:
    started: list[tuple[list[str], dict[str, object]]] = []

    def fake_popen(command, **kwargs):
        started.append((command, kwargs))
        return FakeProcess()

    monkeypatch.setattr(settings.celery, "broker_url", "amqp://guest:guest@127.0.0.1:5672//")
    monkeypatch.setattr(settings.celery, "auto_start_enabled", True)
    monkeypatch.setattr(settings.celery, "auto_start_worker_enabled", True)
    monkeypatch.setattr(settings.celery, "auto_start_beat_enabled", True)
    monkeypatch.setattr("app.platform.tasks.autostart.subprocess.Popen", fake_popen)

    manager = CeleryProcessManager()
    manager.start()

    commands = [item[0] for item in started]
    assert [command[5] for command in commands] == ["worker", "beat"]
    assert all(command[2:5] == ["celery", "-A", CELERY_APP_PATH] for command in commands)


def test_celery_autostart_skips_when_broker_url_is_empty(monkeypatch) -> None:
    started: list[list[str]] = []

    def fake_popen(command, **kwargs):
        started.append(command)
        return FakeProcess()

    monkeypatch.setattr(settings.celery, "broker_url", "")
    monkeypatch.setattr("app.platform.tasks.autostart.subprocess.Popen", fake_popen)

    CeleryProcessManager().start()

    assert started == []
