package enums

// ResourceType represents the type of a resource/menu node.
type ResourceType string

const (
	ResourceTypeDirectory    ResourceType = "DIRECTORY"
	ResourceTypeMenu         ResourceType = "MENU"
	ResourceTypeButton       ResourceType = "BUTTON"
	ResourceTypeInternalLink ResourceType = "INTERNAL_LINK"
	ResourceTypeExternalLink ResourceType = "EXTERNAL_LINK"
)

func (e ResourceType) Desc() string {
	switch e {
	case ResourceTypeDirectory:
		return "目录"
	case ResourceTypeMenu:
		return "菜单"
	case ResourceTypeButton:
		return "按钮"
	case ResourceTypeInternalLink:
		return "内链"
	case ResourceTypeExternalLink:
		return "外链"
	default:
		return ""
	}
}

// ResourceCategory represents the category of a resource.
type ResourceCategory string

const (
	ResourceCategoryBackendMenu    ResourceCategory = "BACKEND_MENU"
	ResourceCategoryFrontendMenu   ResourceCategory = "FRONTEND_MENU"
	ResourceCategoryBackendButton  ResourceCategory = "BACKEND_BUTTON"
	ResourceCategoryFrontendButton ResourceCategory = "FRONTEND_BUTTON"
)

func (e ResourceCategory) Desc() string {
	switch e {
	case ResourceCategoryBackendMenu:
		return "后台菜单"
	case ResourceCategoryFrontendMenu:
		return "前台菜单"
	case ResourceCategoryBackendButton:
		return "后台按钮"
	case ResourceCategoryFrontendButton:
		return "前台按钮"
	default:
		return ""
	}
}
