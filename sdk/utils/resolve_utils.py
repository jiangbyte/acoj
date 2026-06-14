from typing import Optional, List, Dict
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession


def resolve_path_from_map(entity_id: Optional[str], node_map: Dict) -> List[str]:
    """Resolve a name path from a pre-built node_map (id -> {name, parent_id})."""
    if not entity_id:
        return []
    names = []
    current = entity_id
    while current and current in node_map:
        names.append(node_map[current]["name"])
        current = node_map[current].get("parent_id")
        if current == "0":
            break
    return list(reversed(names))


async def resolve_name_path(entity_id: Optional[str], db: AsyncSession, model_class) -> List[str]:
    """Resolve a hierarchical entity ID to a list of names from root to current."""
    if not entity_id:
        return []
    rows = (await db.execute(select(model_class.id, model_class.name, model_class.parent_id))).all()
    node_map = {r.id: {"name": r.name, "parent_id": r.parent_id} for r in rows}
    return resolve_path_from_map(entity_id, node_map)
