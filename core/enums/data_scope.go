package enums

// DataScope represents data permission scope levels.
type DataScope string

const (
	DataScopeAll           DataScope = "ALL"
	DataScopeSelf          DataScope = "SELF"
	DataScopeOrg           DataScope = "ORG"
	DataScopeOrgAndBelow   DataScope = "ORG_AND_BELOW"
	DataScopeCustomOrg     DataScope = "CUSTOM_ORG"
	DataScopeGroup         DataScope = "GROUP"
	DataScopeGroupAndBelow DataScope = "GROUP_AND_BELOW"
	DataScopeCustomGroup   DataScope = "CUSTOM_GROUP"
)

// MostRestrictive returns the most restrictive scope from the given list.
func MostRestrictive(scopes ...string) string {
	priority := map[string]int{
		"SELF": 0, "CUSTOM_GROUP": 1, "CUSTOM_ORG": 2,
		"GROUP_AND_BELOW": 3, "GROUP": 4,
		"ORG_AND_BELOW": 5, "ORG": 6, "ALL": 7,
	}
	best := ""
	bestP := 99
	for _, s := range scopes {
		if p, ok := priority[s]; ok && p < bestP {
			bestP = p
			best = s
		}
	}
	return best
}
