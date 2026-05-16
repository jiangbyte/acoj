package enums

// PageDataField represents field names in paginated response data.
type PageDataField string

const (
	FieldRecords PageDataField = "records"
	FieldTotal   PageDataField = "total"
	FieldPage    PageDataField = "page"
	FieldSize    PageDataField = "size"
	FieldPages   PageDataField = "pages"
)
