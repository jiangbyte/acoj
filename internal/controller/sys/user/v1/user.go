package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/user/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	userService "hei-goframe/internal/service/sys/user"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.UserPageReq) (res *api.UserPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:page"); err != nil {
		return nil, err
	}
	result, err := userService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.UserPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.UserCreateReq) (res *api.UserCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加用户")()
	err = userService.CreateUser(ctx, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone,
		req.OrgId, req.PositionId, req.GroupId, req.Status, req.RoleIds)
	if err != nil {
		return nil, err
	}
	return &api.UserCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.UserModifyReq) (res *api.UserModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑用户")()
	err = userService.ModifyUser(ctx, req.Id, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone,
		req.OrgId, req.PositionId, req.GroupId, req.Status, req.RoleIds)
	if err != nil {
		return nil, err
	}
	return &api.UserModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.UserRemoveReq) (res *api.UserRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除用户")()
	err = userService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.UserRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.UserDetailReq) (res *api.UserDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:detail"); err != nil {
		return nil, err
	}
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
	if err := auth.MustPerm(ctx, "sys:user:grant-role"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "分配用户角色")()
	err = userService.GrantRoles(ctx, req.UserId, req.RoleIds, req.Scope, req.CustomScopeGroupIds)
	if err != nil {
		return nil, err
	}
	return &api.GrantRoleRes{}, nil
}

func (c *ControllerV1) GrantPermission(ctx context.Context, req *api.GrantPermissionReq) (res *api.GrantPermissionRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:grant-permission"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "分配用户权限")()
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
	if err := auth.MustPerm(ctx, "sys:user:own-permission-detail"); err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	list, err := userService.GetPermissionDetails(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	items := make([]api.PermissionDetailItem, 0)
	for _, item := range list {
		items = append(items, api.PermissionDetailItem{
			PermissionCode:      item["permission_code"].(string),
			Scope:               item["scope"].(string),
			CustomScopeGroupIds: item["custom_scope_group_ids"].(string),
			CustomScopeOrgIds:   item["custom_scope_org_ids"].(string),
		})
	}
	r.Response.WriteJson(utility.SuccessWithCtx(ctx, items))
	return
}

func (c *ControllerV1) OwnRoles(ctx context.Context, req *api.OwnRolesReq) (res *api.OwnRolesRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:own-roles"); err != nil {
		return nil, err
	}
	roleIds, err := userService.GetOwnRoles(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.SuccessWithCtx(ctx, roleIds))
	return
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
	r.Response.WriteJson(utility.SuccessWithCtx(ctx, nodes))
	return
}

func (c *ControllerV1) Permissions(ctx context.Context, req *api.UserPermissionsReq) (res *api.UserPermissionsRes, err error) {
	loginId := getLoginId(ctx)
	codes, err := userService.GetPermissions(ctx, loginId)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.SuccessWithCtx(ctx, codes))
	return
}

func (c *ControllerV1) UpdateProfile(ctx context.Context, req *api.UpdateProfileReq) (res *api.UpdateProfileRes, err error) {
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "更新个人信息")()
	loginId := getLoginId(ctx)
	err = userService.UpdateProfile(ctx, loginId, req.Account, req.Nickname,
		req.Motto, req.Gender, req.Birthday, req.Email, req.Github, req.Phone)
	if err != nil {
		return nil, err
	}
	return &api.UpdateProfileRes{}, nil
}

func (c *ControllerV1) UpdateAvatar(ctx context.Context, req *api.UpdateAvatarReq) (res *api.UpdateAvatarRes, err error) {
	defer log.SysLog(ctx, "更新头像")()
	loginId := getLoginId(ctx)
	err = userService.UpdateAvatar(ctx, loginId, req.Avatar)
	if err != nil {
		return nil, err
	}
	return &api.UpdateAvatarRes{}, nil
}

func (c *ControllerV1) UpdatePassword(ctx context.Context, req *api.UpdatePasswordReq) (res *api.UpdatePasswordRes, err error) {
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "修改密码")()
	loginId := getLoginId(ctx)
	err = userService.UpdatePassword(ctx, loginId, req.CurrentPassword, req.NewPassword)
	if err != nil {
		return nil, err
	}
	return &api.UpdatePasswordRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.UserExportReq) (res *api.UserExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出用户数据")()
	buffer, err := userService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("用户数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.UserExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.UserTemplateReq) (res *api.UserTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:template"); err != nil {
		return nil, err
	}
	buffer, err := userService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("用户导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.UserTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.UserImportReq) (res *api.UserImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:user:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入用户数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := userService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.UserImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
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
