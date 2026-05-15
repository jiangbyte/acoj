package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/client/user/v1"
	"hei-goframe/internal/service/auth"
	userService "hei-goframe/internal/service/client/user"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.ClientUserPageReq) (res *api.ClientUserPageRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:page"); err != nil {
		return nil, err
	}
	result, err := userService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.ClientUserCreateReq) (res *api.ClientUserCreateRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:create"); err != nil {
		return nil, err
	}
	err = userService.Create(ctx, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone, req.Password, req.Status)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.ClientUserModifyReq) (res *api.ClientUserModifyRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:modify"); err != nil {
		return nil, err
	}
	err = userService.Modify(ctx, req.Id, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone, req.Status)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.ClientUserRemoveReq) (res *api.ClientUserRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:remove"); err != nil {
		return nil, err
	}
	err = userService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ClientUserDetailReq) (res *api.ClientUserDetailRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:detail"); err != nil {
		return nil, err
	}
	data, err := userService.Detail(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, nil
	}
	res = &api.ClientUserDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Current(ctx context.Context, req *api.ClientUserCurrentReq) (res *api.ClientUserCurrentRes, err error) {
	loginId := getLoginId(ctx)
	data, err := userService.GetCurrentUser(ctx, loginId)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.ClientUserCurrentRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) UpdateProfile(ctx context.Context, req *api.ClientUserUpdateProfileReq) (res *api.ClientUserUpdateProfileRes, err error) {
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "C端用户更新个人信息")()
	loginId := getLoginId(ctx)
	err = userService.UpdateProfile(ctx, loginId, req.Account, req.Nickname,
		req.Motto, req.Gender, req.Birthday, req.Email, req.Github)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdateProfileRes{}, nil
}

func (c *ControllerV1) UpdateAvatar(ctx context.Context, req *api.ClientUserUpdateAvatarReq) (res *api.ClientUserUpdateAvatarRes, err error) {
	defer log.SysLog(ctx, "C端用户更新头像")()
	loginId := getLoginId(ctx)
	err = userService.UpdateAvatar(ctx, loginId, req.Avatar)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdateAvatarRes{}, nil
}

func (c *ControllerV1) UpdatePassword(ctx context.Context, req *api.ClientUserUpdatePasswordReq) (res *api.ClientUserUpdatePasswordRes, err error) {
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "C端用户修改密码")()
	loginId := getLoginId(ctx)
	err = userService.UpdatePassword(ctx, loginId, req.CurrentPassword, req.NewPassword)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdatePasswordRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.ClientUserExportReq) (res *api.ClientUserExportRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:export"); err != nil {
		return nil, err
	}

	buffer, err := userService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("C端用户数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ClientUserExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.ClientUserTemplateReq) (res *api.ClientUserTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:template"); err != nil {
		return nil, err
	}
	buffer, err := userService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("C端用户导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ClientUserTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.ClientUserImportReq) (res *api.ClientUserImportRes, err error) {
	if err := auth.MustPerm(ctx, "client:user:import"); err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := userService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.ClientUserImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return ""
	}
	tokenStr := r.Header.Get(auth.ConsumerAuth.GetTokenName())
	id, _ := auth.ConsumerAuth.GetLoginId(ctx, tokenStr)
	return id
}
