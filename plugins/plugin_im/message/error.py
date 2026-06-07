"""Application error type for the message module."""


class AppError(Exception):
    """Application-level error with code."""

    def __init__(self, message: str, code: int = 400):
        self.message = message
        self.code = code
        super().__init__(message)
