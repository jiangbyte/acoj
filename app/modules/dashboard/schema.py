from app.core.schema.base import ApiSchema


class DashboardMetric(ApiSchema):
    key: str
    value: int | float
    trend_value: float | None = None


class DashboardTrendPoint(ApiSchema):
    date: str
    type: str
    value: int | float


class DashboardStatusItem(ApiSchema):
    name: str
    value: int


class DashboardOverviewResponse(ApiSchema):
    metrics: list[DashboardMetric]
    account_trend: list[DashboardTrendPoint]
    file_type_share: list[DashboardStatusItem]
