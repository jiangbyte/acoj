package message

import (
	"log"

	"hei-gin/sdk/db"
	imModel "hei-gin/plugins/plugin-im/model"
)

func init() {
	db.RegisterModel(&imModel.Message{})
}

// FixConversationSchema repairs im_conversation table column names.
// The old schema used user_id1 (without underscore) — rename to user_id_1.
func FixConversationSchema() {
	if !db.DB.Migrator().HasTable("im_conversation") {
		return
	}
	renames := map[string]string{
		"user_id1":   "user_id_1",
		"user_type1": "user_type_1",
		"user_id2":   "user_id_2",
		"user_type2": "user_type_2",
	}
	for oldCol, newCol := range renames {
		if db.DB.Migrator().HasColumn("im_conversation", oldCol) {
			if !db.DB.Migrator().HasColumn("im_conversation", newCol) {
				exec := "ALTER TABLE im_conversation CHANGE COLUMN " + oldCol + " " + newCol + " VARCHAR(32) NOT NULL"
				if err := db.DB.Exec(exec).Error; err != nil {
					log.Printf("[IM] Failed to rename %s -> %s: %v", oldCol, newCol, err)
				} else {
					log.Printf("[IM] Renamed column %s -> %s in im_conversation", oldCol, newCol)
				}
			}
		}
	}
}
