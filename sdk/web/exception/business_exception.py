class BusinessException(Exception):
    def __init__(self, message: str = "业务异常", code: int = 400):
        self.message = str(message)
        self.code = int(code)
        super().__init__(self.message)
