import pytest

from app.core.config.enums import StatusEnum
from app.core.exceptions.business import NotFoundError
from app.core.response.pagination import PageQuery
from app.modules.dict.schema import (
    DictAdminListQuery,
    DictCreateRequest,
    DictIdQuery,
    DictIdsRequest,
    DictTreeQuery,
    DictUpdateRequest,
)
from app.modules.dict.service import DictService


def _dict_create_request(**overrides) -> DictCreateRequest:
    data = {
        "code": "GENDER",
        "label": "Gender",
        "value": "gender",
        "color": "#1677ff",
        "category": "BIZ",
        "sort": 10,
        "status": StatusEnum.ENABLED,
    }
    data.update(overrides)
    return DictCreateRequest(**data)


async def test_dict_service_create_list_detail_update_delete(db_session):
    service = DictService(db_session)

    created = await service.create(_dict_create_request())
    page = await service.list_admin(
        DictAdminListQuery(
            pagination=PageQuery(current=1, size=20),
            category="BIZ",
            status=StatusEnum.ENABLED.value,
        )
    )
    detail = await service.get(DictIdQuery(id=created.id))
    await db_session.rollback()
    updated = await service.update(
        DictUpdateRequest(
            id=created.id,
            code="GENDER",
            label="Gender Updated",
            value="gender",
            category="BIZ",
            sort=1,
            status=StatusEnum.DISABLED,
        )
    )
    deleted = await service.delete(DictIdsRequest(ids=[created.id]))

    assert page.total == 1
    assert page.records[0].id == created.id
    assert detail.label == "Gender"
    assert updated.label == "Gender Updated"
    assert updated.status == StatusEnum.DISABLED
    assert deleted == [created.id]


async def test_dict_service_delete_requires_all_ids_exist(db_session):
    service = DictService(db_session)
    created = await service.create(_dict_create_request(code="VISIBLE"))

    with pytest.raises(NotFoundError):
        await service.delete(DictIdsRequest(ids=[created.id, "missing"]))


async def test_dict_service_tree_supports_category_filter_and_orphan_roots(db_session):
    service = DictService(db_session)
    profile_root = await service.create(
        _dict_create_request(code="PROFILE_GENDER", label="Gender", sort=1)
    )
    await service.create(
        _dict_create_request(
            code="PROFILE_GENDER_MALE",
            label="Male",
            value="M",
            parent_id=profile_root.id,
            sort=2,
        )
    )
    await service.create(
        _dict_create_request(
            code="ORDER_STATUS",
            label="Order Status",
            category="SYS",
            sort=3,
        )
    )
    await service.create(
        _dict_create_request(
            code="PROFILE_ORPHAN",
            label="Orphan",
            parent_id="missing-parent",
            sort=4,
        )
    )

    profile_tree = await service.list_tree(DictTreeQuery(category="BIZ"))
    all_tree = await service.list_tree(DictTreeQuery())

    assert [node.code for node in profile_tree] == ["PROFILE_GENDER", "PROFILE_ORPHAN"]
    assert profile_tree[0].children[0].code == "PROFILE_GENDER_MALE"
    assert [node.code for node in all_tree] == [
        "PROFILE_GENDER",
        "ORDER_STATUS",
        "PROFILE_ORPHAN",
    ]
