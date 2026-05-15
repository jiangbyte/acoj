package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type PublicKeyReq struct {
	g.Meta `path:"/api/v1/public/b/sm2/public-key" method:"get" summary:"获取SM2公钥" tags:"B端认证"`
}

type PublicKeyRes struct {
	PublicKey string `json:"public_key"`
}

type ConsumerPublicKeyReq struct {
	g.Meta `path:"/api/v1/public/c/sm2/public-key" method:"get" summary:"获取C端SM2公钥" tags:"C端认证"`
}

type ConsumerPublicKeyRes struct {
	PublicKey string `json:"public_key"`
}
