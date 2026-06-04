package enums

type PageDataField string

const (
	PageDataFieldRecords PageDataField = "records"
	PageDataFieldTotal   PageDataField = "total"
	PageDataFieldPage    PageDataField = "page"
	PageDataFieldSize    PageDataField = "size"
	PageDataFieldPages   PageDataField = "pages"
)
