package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/user/v1"
	"hei-goframe/internal/service/auth"
	userService "hei-goframe/internal/service/sys/user"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.UserPageReq) (res *api.UserPageRes, err error) {
	result, err := userService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.UserPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.UserCreateReq) (res *api.UserCreateRes, err error) {
	err = userService.CreateUser(ctx, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone,
		req.OrgId, req.PositionId, req.GroupId, req.Status, req.RoleIds)
	if err != nil {
		return nil, err
	}
	return &api.UserCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.UserModifyReq) (res *api.UserModifyRes, err error) {
	err = userService.ModifyUser(ctx, req.Id, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone,
		req.OrgId, req.PositionId, req.GroupId, req.Status, req.RoleIds)
	if err != nil {
		return nil, err
	}
	return &api.UserModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.UserRemoveReq) (res *api.UserRemoveRes, err error) {
	err = userService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.UserRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.UserDetailReq) (res *api.UserDetailRes, err error) {
	data, err := userService.Detail(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, nil
	}
	res = &api.UserDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) GrantRole(ctx context.Context, req *api.GrantRoleReq) (res *api.GrantRoleRes, err error) {
	err = userService.GrantRoles(ctx, req.UserId, req.RoleIds, req.Scope, req.CustomScopeGroupIds)
	if err != nil {
		return nil, err
	}
	return &api.GrantRoleRes{}, nil
}

func (c *ControllerV1) GrantPermission(ctx context.Context, req *api.GrantPermissionReq) (res *api.GrantPermissionRes, err error) {
	perms := make([]map[string]interface{}, len(req.Permissions))
	for i, p := range req.Permissions {
		perms[i] = map[string]interface{}{
			"permission_code":        p.PermissionCode,
			"scope":                  p.Scope,
			"custom_scope_group_ids": p.CustomScopeGroupIds,
			"custom_scope_org_ids":   p.CustomScopeOrgIds,
		}
	}
	err = userService.GrantPermissions(ctx, req.UserId, perms)
	if err != nil {
		return nil, err
	}
	return &api.GrantPermissionRes{}, nil
}

func (c *ControllerV1) OwnPermissionDetail(ctx context.Context, req *api.OwnPermissionDetailReq) (res *api.OwnPermissionDetailRes, err error) {
	list, err := userService.GetPermissionDetails(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	res = &api.OwnPermissionDetailRes{List: make([]api.PermissionDetailItem, 0)}
	for _, item := range list {
		res.List = append(res.List, api.PermissionDetailItem{
			PermissionCode:      item["permission_code"].(string),
			Scope:               item["scope"].(string),
			CustomScopeGroupIds: item["custom_scope_group_ids"].(string),
			CustomScopeOrgIds:   item["custom_scope_org_ids"].(string),
		})
	}
	return res, nil
}

func (c *ControllerV1) OwnRoles(ctx context.Context, req *api.OwnRolesReq) (res *api.OwnRolesRes, err error) {
	roleIds, err := userService.GetOwnRoles(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	return &api.OwnRolesRes{RoleIds: roleIds}, nil
}

func (c *ControllerV1) Current(ctx context.Context, req *api.CurrentUserReq) (res *api.CurrentUserRes, err error) {
	loginId := getLoginId(ctx)
	data, err := userService.GetCurrentUser(ctx, loginId)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.CurrentUserRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Menus(ctx context.Context, req *api.MenusReq) (res *api.MenusRes, err error) {
	loginId := getLoginId(ctx)
	menuList, err := userService.GetMenus(ctx, loginId)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	nodes := make([]*api.MenuNode, 0)
	for _, m := range menuList {
		nodes = append(nodes, menuMapToNode(m))
	}
	r.Response.WriteJson(utility.Success(nodes))
	return
}

func (c *ControllerV1) Permissions(ctx context.Context, req *api.UserPermissionsReq) (res *api.UserPermissionsRes, err error) {
	loginId := getLoginId(ctx)
	codes, err := userService.GetPermissions(ctx, loginId)
	if err != nil {
		return nil, err
	}
	return &api.UserPermissionsRes{Codes: codes}, nil
}

func (c *ControllerV1) UpdateProfile(ctx context.Context, req *api.UpdateProfileReq) (res *api.UpdateProfileRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdateProfile(ctx, loginId, req.Account, req.Nickname,
		req.Motto, req.Gender, req.Birthday, req.Email, req.Github, req.Phone)
	if err != nil {
		return nil, err
	}
	return &api.UpdateProfileRes{}, nil
}

func (c *ControllerV1) UpdateAvatar(ctx context.Context, req *api.UpdateAvatarReq) (res *api.UpdateAvatarRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdateAvatar(ctx, loginId, req.Avatar)
	if err != nil {
		return nil, err
	}
	return &api.UpdateAvatarRes{}, nil
}

func (c *ControllerV1) UpdatePassword(ctx context.Context, req *api.UpdatePasswordReq) (res *api.UpdatePasswordRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdatePassword(ctx, loginId, req.CurrentPassword, req.NewPassword)
	if err != nil {
		return nil, err
	}
	return &api.UpdatePasswordRes{}, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	// Fallback: extract from request
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return ""
	}
	tokenStr := r.Header.Get(auth.BusinessAuth.GetTokenName())
	id, _ := auth.BusinessAuth.GetLoginId(ctx, tokenStr)
	return id
}

func menuMapToNode(m g.Map) *api.MenuNode {
	node := &api.MenuNode{
		Id:            m["id"].(string),
		Code:          m["code"].(string),
		Name:          m["name"].(string),
		Type:          m["type"].(string),
		ParentId:      m["parent_id"].(string),
		RoutePath:     m["route_path"].(string),
		ComponentPath: m["component_path"].(string),
		RedirectPath:  m["redirect_path"].(string),
		Icon:          m["icon"].(string),
		IsVisible:     m["is_visible"].(bool),
		IsCache:       m["is_cache"].(bool),
		IsAffix:       m["is_affix"].(bool),
		IsBreadcrumb:  m["is_breadcrumb"].(bool),
		SortCode:      m["sort_code"].(int),
	}
	if children, ok := m["children"].([]g.Map); ok {
		for _, child := range children {
			node.Children = append(node.Children, menuMapToNode(child))
		}
	}
	return node
}
