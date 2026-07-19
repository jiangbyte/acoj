# Codegen DDL Test Tables

These PostgreSQL DDL files are isolated with the `cg_test_` prefix and can be imported one by one.

| File | Suggested gen type | Main table | Sub table | Notes |
| --- | --- | --- | --- | --- |
| `01_crud_activity.sql` | `TABLE` | `cg_test_activity` | - | Covers string, text, int, numeric, bool, timestamptz, jsonb, status dict. |
| `02_tree_catalog.sql` | `TREE` | `cg_test_catalog` | - | Use `parent_id` as tree parent field and `name` as tree label field. |
| `03_master_detail_order.sql` | `MASTER_DETAIL` | `cg_test_order` | `cg_test_order_item` | Use `order_id` as sub foreign key. |
| `04_left_tree_table_knowledge.sql` | `LEFT_TREE_TABLE` | `cg_test_knowledge_category` | `cg_test_knowledge_doc` | Use `parent_id` / `name` for the tree and `category_id` as sub foreign key. |

All files include column comments so the code generator can reflect Chinese labels.
