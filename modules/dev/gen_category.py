class GenCategoryEnum:
    TABLE = "TABLE"
    TREE = "TREE"
    LEFT_TREE_TABLE = "LEFT_TREE_TABLE"
    MASTER_DETAIL = "MASTER_DETAIL"

    @classmethod
    def is_dual_table(cls, value: str) -> bool:
        return value in (cls.LEFT_TREE_TABLE, cls.MASTER_DETAIL)

    @classmethod
    def is_tree_type(cls, value: str) -> bool:
        return value in (cls.TREE, cls.LEFT_TREE_TABLE)
