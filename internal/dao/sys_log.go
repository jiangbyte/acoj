package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysLog = sysLogDao{}

type sysLogDao struct {
	Table   string
	Columns sysLogColumns
}

type sysLogColumns struct {
	Id         string
	Category   string
	Name       string
	ExeStatus  string
	ExeMessage string
	OpIp       string
	OpAddress  string
	OpBrowser  string
	OpOs       string
	ClassName  string
	MethodName string
	ReqMethod  string
	ReqUrl     string
	ParamJson  string
	ResultJson string
	OpTime     string
	TraceId    string
	OpUser     string
	SignData   string
	CreatedAt  string
	CreatedBy  string
	UpdatedAt  string
	UpdatedBy  string
}

func init() {
	SysLog.Table = "sys_log"
	SysLog.Columns = sysLogColumns{
		Id:         "id",
		Category:   "category",
		Name:       "name",
		ExeStatus:  "exe_status",
		ExeMessage: "exe_message",
		OpIp:       "op_ip",
		OpAddress:  "op_address",
		OpBrowser:  "op_browser",
		OpOs:       "op_os",
		ClassName:  "class_name",
		MethodName: "method_name",
		ReqMethod:  "req_method",
		ReqUrl:     "req_url",
		ParamJson:  "param_json",
		ResultJson: "result_json",
		OpTime:     "op_time",
		TraceId:    "trace_id",
		OpUser:     "op_user",
		SignData:   "sign_data",
		CreatedAt:  "created_at",
		CreatedBy:  "created_by",
		UpdatedAt:  "updated_at",
		UpdatedBy:  "updated_by",
	}
}

func (d sysLogDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}
