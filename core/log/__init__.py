from .decorator import sys_log

# Uppercase alias for use as @SysLog("名称")
SysLog = sys_log

__all__ = ["sys_log", "SysLog"]
