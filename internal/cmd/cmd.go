package cmd

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/gogf/gf/v2/os/gcmd"

	clientAuthCtrl "hei-goframe/internal/controller/client/auth/captcha/v1"
	clientUserAuthCtrl "hei-goframe/internal/controller/client/auth/username/v1"
	clientSessionCtrl "hei-goframe/internal/controller/client/session/v1"
	clientUserCtrl "hei-goframe/internal/controller/client/user/v1"
	sm2PubCtrl "hei-goframe/internal/controller/public/sm2/v1"
	analyzeCtrl "hei-goframe/internal/controller/sys/analyze/v1"
	busCaptchaCtrl "hei-goframe/internal/controller/sys/auth/captcha/v1"
	busUserAuthCtrl "hei-goframe/internal/controller/sys/auth/username/v1"
	bannerCtrl "hei-goframe/internal/controller/sys/banner/v1"
	configCtrl "hei-goframe/internal/controller/sys/config/v1"
	dictCtrl "hei-goframe/internal/controller/sys/dict/v1"
	fileCtrl "hei-goframe/internal/controller/sys/file/v1"
	groupCtrl "hei-goframe/internal/controller/sys/group/v1"
	homeCtrl "hei-goframe/internal/controller/sys/home/v1"
	logCtrl "hei-goframe/internal/controller/sys/log/v1"
	noticeCtrl "hei-goframe/internal/controller/sys/notice/v1"
	orgCtrl "hei-goframe/internal/controller/sys/org/v1"
	permCtrl "hei-goframe/internal/controller/sys/permission/v1"
	positionCtrl "hei-goframe/internal/controller/sys/position/v1"
	resourceCtrl "hei-goframe/internal/controller/sys/resource/v1"
	roleCtrl "hei-goframe/internal/controller/sys/role/v1"
	sessionCtrl "hei-goframe/internal/controller/sys/session/v1"
	userCtrl "hei-goframe/internal/controller/sys/user/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

var (
	Main = gcmd.Command{
		Name:  "main",
		Usage: "main",
		Brief: "start http server",
		Func: func(ctx context.Context, parser *gcmd.Parser) (err error) {
			initSM2(ctx)

			auth.RegisterPermissionInterface(auth.NewPermissionInterface())
			auth.RunPermissionScan(ctx)

			s := g.Server()

			s.Group("/", func(group *ghttp.RouterGroup) {
				group.Middleware(
					utility.MiddlewareTrace,
					utility.MiddlewareHandlerResponse,
					auth.MiddlewareCORS,
					auth.MiddlewareAuth,
				)
				group.Bind(
					busCaptchaCtrl.NewV1(),
					sm2PubCtrl.NewV1(),
					busUserAuthCtrl.NewV1(),
					clientAuthCtrl.NewV1(),
					clientUserAuthCtrl.NewV1(),
					userCtrl.NewV1(),
					roleCtrl.NewV1(),
					dictCtrl.NewV1(),
					configCtrl.NewV1(),
					resourceCtrl.NewV1(),
					permCtrl.NewV1(),
					orgCtrl.NewV1(),
					groupCtrl.NewV1(),
					positionCtrl.NewV1(),
					noticeCtrl.NewV1(),
					bannerCtrl.NewV1(),
					logCtrl.NewV1(),
					sessionCtrl.NewV1(),
					homeCtrl.NewV1(),
					analyzeCtrl.NewV1(),
					fileCtrl.NewV1(),
					clientSessionCtrl.NewV1(),
					clientUserCtrl.NewV1(),
				)
			})
			s.Run()
			return nil
		},
	}
)

func initSM2(ctx context.Context) {
	privateKey := g.Cfg().MustGet(ctx, "hei.sm2.privateKey").String()
	publicKey := g.Cfg().MustGet(ctx, "hei.sm2.publicKey").String()
	if privateKey == "" || publicKey == "" {
		g.Log().Warning(ctx, "SM2 keys not configured")
		return
	}
	if err := utility.InitSM2(privateKey, publicKey); err != nil {
		g.Log().Fatalf(ctx, "SM2 init failed: %v", err)
	}
}
