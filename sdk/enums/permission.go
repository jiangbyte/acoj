package enums

// LoginTypeEnum 登录类型
type LoginTypeEnum string

const (
	LoginTypeBusiness LoginTypeEnum = "BUSINESS"
	LoginTypeConsumer LoginTypeEnum = "CONSUMER"
)

func (e LoginTypeEnum) Desc() string {
	descriptions := map[LoginTypeEnum]string{
		LoginTypeBusiness: "后台登录",
		LoginTypeConsumer: "客户端登录",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

// PermissionCategoryEnum 权限类别
type PermissionCategoryEnum string

const (
	PermissionCategoryBackend  PermissionCategoryEnum = "BACKEND"
	PermissionCategoryFrontend PermissionCategoryEnum = "FRONTEND"
)

func (e PermissionCategoryEnum) Desc() string {
	descriptions := map[PermissionCategoryEnum]string{
		PermissionCategoryBackend:  "后端权限",
		PermissionCategoryFrontend: "前端权限",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

// PermissionScopeEnum 权限范围
type PermissionScopeEnum string

const (
	PermissionScopeAll         PermissionScopeEnum = "ALL"
	PermissionScopeOrg         PermissionScopeEnum = "ORG"
	PermissionScopeOrgAndBelow PermissionScopeEnum = "ORG_AND_BELOW"
	PermissionScopeSelf        PermissionScopeEnum = "SELF"
)

func (e PermissionScopeEnum) Desc() string {
	descriptions := map[PermissionScopeEnum]string{
		PermissionScopeAll:         "全部",
		PermissionScopeOrg:         "本组织",
		PermissionScopeOrgAndBelow: "本组织及以下",
		PermissionScopeSelf:        "本人",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

// DataScopeEnum 数据权限范围
type DataScopeEnum string

const (
	DataScopeAll           DataScopeEnum = "ALL"
	DataScopeSelf          DataScopeEnum = "SELF"
	DataScopeOrg           DataScopeEnum = "ORG"
	DataScopeOrgAndBelow   DataScopeEnum = "ORG_AND_BELOW"
	DataScopeCustomOrg     DataScopeEnum = "CUSTOM_ORG"
	DataScopeGroup         DataScopeEnum = "GROUP"
	DataScopeGroupAndBelow DataScopeEnum = "GROUP_AND_BELOW"
	DataScopeCustomGroup   DataScopeEnum = "CUSTOM_GROUP"
)

// MostRestrictive 返回给定数据范围中最严格的一个
// 优先级: SELF(0) < CUSTOM_GROUP(1) < CUSTOM_ORG(2) < GROUP_AND_BELOW(3) < GROUP(4) < ORG_AND_BELOW(5) < ORG(6) < ALL(7)
func MostRestrictive(scopes ...string) string {
	priority := map[string]int{
		"SELF":            0,
		"CUSTOM_GROUP":    1,
		"CUSTOM_ORG":      2,
		"GROUP_AND_BELOW": 3,
		"GROUP":           4,
		"ORG_AND_BELOW":   5,
		"ORG":             6,
		"ALL":             7,
	}
	if len(scopes) == 0 {
		return ""
	}
	result := scopes[0]
	minPrio := priority[result]
	for _, s := range scopes[1:] {
		if p, ok := priority[s]; ok && p < minPrio {
			minPrio = p
			result = s
		}
	}
	return result
}

// CheckModeEnum 检查模式
type CheckModeEnum string

const (
	CheckModeAnd CheckModeEnum = "AND"
	CheckModeOr  CheckModeEnum = "OR"
)

func (e CheckModeEnum) Desc() string {
	descriptions := map[CheckModeEnum]string{
		CheckModeAnd: "且",
		CheckModeOr:  "或",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

// PermissionPathEnum 权限来源路径（值越小优先级越高）
type PermissionPathEnum string

const (
	PermissionPathDirect   PermissionPathEnum = "P0" // User → Direct Permission
	PermissionPathUserRole PermissionPathEnum = "P1" // User → Role → Permission
)

func (e PermissionPathEnum) Desc() string {
	descriptions := map[PermissionPathEnum]string{
		PermissionPathDirect:   "直接权限",
		PermissionPathUserRole: "角色权限",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}
