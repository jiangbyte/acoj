from sdk.auth.realm import infer_realm_id_from_path
from sdk.auth.enums import RealmID


def test_infer_realm_id_matches_gin_message_path_cases() -> None:
    cases = {
        "/api/v1/c/im/message/page": RealmID.CONSUMER,
        "/api/v12/c/im/message/page": RealmID.CONSUMER,
        "/api/v1/b/im/message/page": RealmID.BUSINESS,
        "/c/im/message/page": None,
    }

    for path, expected in cases.items():
        assert infer_realm_id_from_path(path) == expected
