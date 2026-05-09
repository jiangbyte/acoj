import io
from typing import List
from urllib.parse import quote
from fastapi import UploadFile
from fastapi.responses import StreamingResponse
from openpyxl import Workbook

from config.settings import settings
from core.exception import BusinessException

_import_max_bytes = settings.app.import_max_file_size_mb * 1024 * 1024


def validate_import_file(file: UploadFile):
    """Validate uploaded import file size. Raises BusinessException if too large."""
    if file.size and file.size > _import_max_bytes:
        raise BusinessException(f"导入文件大小不能超过{settings.app.import_max_file_size_mb}MB")


def export_excel(data: List[dict], filename: str, sheet_name: str = "Sheet1") -> StreamingResponse:
    wb = Workbook()
    ws = wb.active
    ws.title = sheet_name

    if data:
        ws.append(list(data[0].keys()))
        for row in data:
            ws.append(list(row.values()))

    buffer = io.BytesIO()
    wb.save(buffer)
    buffer.seek(0)

    encoded_filename = quote(f"{filename}.xlsx")
    return StreamingResponse(
        buffer,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": f"attachment; filename*=UTF-8''{encoded_filename}"}
    )
