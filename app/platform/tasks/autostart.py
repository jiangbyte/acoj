from __future__ import annotations

import asyncio
import logging
import os
import signal
import subprocess
import sys
from pathlib import Path

from app.core.config.settings import PROJECT_ROOT, settings

logger = logging.getLogger(__name__)

CELERY_APP_PATH = "app.worker.main:celery_app"


class CeleryProcessManager:
    def __init__(self) -> None:
        self._processes: list[tuple[str, subprocess.Popen[bytes]]] = []

    def start(self) -> None:
        if self._processes:
            return
        if not settings.celery.broker_url:
            logger.info("Skip Celery auto start because broker url is empty")
            return

        runtime_dir = PROJECT_ROOT / ".runtime"
        runtime_dir.mkdir(exist_ok=True)
        env = os.environ.copy()

        self._start_process("celery-worker", self._worker_command(), env)
        self._start_process("celery-beat", self._beat_command(runtime_dir), env)

    async def stop(self) -> None:
        if not self._processes:
            return

        timeout = settings.celery.shutdown_timeout_seconds
        for name, process in self._processes:
            if process.poll() is None:
                logger.info("Stopping %s process", name, extra={"pid": process.pid})
                self._terminate_process(process)

        for name, process in self._processes:
            try:
                await asyncio.wait_for(asyncio.to_thread(process.wait), timeout=timeout)
            except TimeoutError:
                logger.warning("Killing %s process after shutdown timeout", name)
                self._kill_process(process)
                await asyncio.to_thread(process.wait)

        self._processes.clear()

    def _start_process(self, name: str, command: list[str], env: dict[str, str]) -> None:
        kwargs: dict[str, object] = {
            "cwd": PROJECT_ROOT,
            "env": env,
        }
        if os.name == "nt":
            kwargs["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP
        else:
            kwargs["preexec_fn"] = os.setsid

        process = subprocess.Popen(command, **kwargs)
        self._processes.append((name, process))
        logger.info("Started %s process", name, extra={"pid": process.pid})

    def _worker_command(self) -> list[str]:
        command = [
            sys.executable,
            "-m",
            "celery",
            "-A",
            CELERY_APP_PATH,
            "worker",
            "--loglevel",
            settings.celery.worker_log_level,
        ]
        if settings.celery.worker_pool:
            command.extend(["--pool", settings.celery.worker_pool])
        if settings.celery.worker_concurrency > 0:
            command.extend(["--concurrency", str(settings.celery.worker_concurrency)])
        return command

    def _beat_command(self, runtime_dir: Path) -> list[str]:
        return [
            sys.executable,
            "-m",
            "celery",
            "-A",
            CELERY_APP_PATH,
            "beat",
            "--loglevel",
            settings.celery.beat_log_level,
            "--schedule",
            str(runtime_dir / "celerybeat-schedule"),
        ]

    def _terminate_process(self, process: subprocess.Popen[bytes]) -> None:
        if os.name == "nt":
            process.terminate()
            return
        try:
            os.killpg(os.getpgid(process.pid), signal.SIGTERM)
        except ProcessLookupError:
            return

    def _kill_process(self, process: subprocess.Popen[bytes]) -> None:
        if os.name == "nt":
            process.kill()
            return
        try:
            os.killpg(os.getpgid(process.pid), signal.SIGKILL)
        except ProcessLookupError:
            return


celery_process_manager = CeleryProcessManager()
