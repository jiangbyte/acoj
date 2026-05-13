import uuid
from contextvars import ContextVar

TRACE_ID_HEADER = "traceId"
_trace_id_var: ContextVar[str] = ContextVar("trace_id", default="")


def generate_trace_id() -> str:
    """Generate a new trace ID (UUID without dashes)."""
    return uuid.uuid4().hex


def get_trace_id() -> str:
    """Get the current trace ID from context, or empty string if none set."""
    return _trace_id_var.get()


def set_trace_id(trace_id: str):
    """Set the current trace ID in context."""
    _trace_id_var.set(trace_id)


def clear_trace_id():
    """Clear the current trace ID from context."""
    _trace_id_var.set("")
