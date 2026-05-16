package enums

type ResourceTypeEnum string

const (
	ResourceTypeDirectory    ResourceTypeEnum = "DIRECTORY"
	ResourceTypeMenu         ResourceTypeEnum = "MENU"
	ResourceTypeButton       ResourceTypeEnum = "BUTTON"
	ResourceTypeInternalLink ResourceTypeEnum = "INTERNAL_LINK"
	ResourceTypeExternalLink ResourceTypeEnum = "EXTERNAL_LINK"
)

func (e ResourceTypeEnum) Desc() string {
	descriptions := map[ResourceTypeEnum]string{
		ResourceTypeDirectory:    "目录",
		ResourceTypeMenu:         "菜单",
		ResourceTypeButton:       "按钮",
		ResourceTypeInternalLink: "内链",
		ResourceTypeExternalLink: "外链",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

type ResourceCategoryEnum string

const (
	ResourceCategoryBackendMenu    ResourceCategoryEnum = "BACKEND_MENU"
	ResourceCategoryFrontendMenu   ResourceCategoryEnum = "FRONTEND_MENU"
	ResourceCategoryBackendButton  ResourceCategoryEnum = "BACKEND_BUTTON"
	ResourceCategoryFrontendButton ResourceCategoryEnum = "FRONTEND_BUTTON"
)

func (e ResourceCategoryEnum) Desc() string {
	descriptions := map[ResourceCategoryEnum]string{
		ResourceCategoryBackendMenu:    "后台菜单",
		ResourceCategoryFrontendMenu:   "前台菜单",
		ResourceCategoryBackendButton:  "后台按钮",
		ResourceCategoryFrontendButton: "前台按钮",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}
