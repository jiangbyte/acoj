from snowflake import SnowflakeGenerator

from config.settings import settings

_generator = SnowflakeGenerator(instance=settings.snowflake.instance)


def generate_id() -> str:
    return str(next(_generator))
