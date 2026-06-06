package langconf

import (
	"log"
	"sync"

	"hei-gin/sdk/config"
)

// LangDef 语言定义（运行时结构）
type LangDef struct {
	Name          string   // c / cpp / python3 / go / java / rust ...
	Interpreted   bool     // 是否为解释型语言
	Compiler      string   // 编译器名称
	CompileArgs   []string // 编译参数
	CompileOut    []string // 编译产出文件列表
	RunArgs       []string // 运行参数
	SourceFile    string   // 源代码文件名
}

var (
	mu         sync.RWMutex
	languages  []LangDef
	langMap    map[string]*LangDef // name -> LangDef
	inited     bool
)

// Init 从全局配置加载语言定义
func Init() {
	mu.Lock()
	defer mu.Unlock()

	langMap = make(map[string]*LangDef)
	languages = nil
	inited = true

	// 从 config.yaml 读取语言配置
	rawJudge, ok := config.C.Raw["judge"]
	if !ok {
		log.Println("[langconf] no judge config found, using defaults")
		loadDefaults()
		return
	}
	judgeMap, ok := rawJudge.(map[string]any)
	if !ok {
		log.Println("[langconf] judge config is not a map, using defaults")
		loadDefaults()
		return
	}
	rawLangs, ok := judgeMap["languages"]
	if !ok {
		log.Println("[langconf] no languages in judge config, using defaults")
		loadDefaults()
		return
	}
	langList, ok := rawLangs.([]any)
	if !ok {
		log.Println("[langconf] languages is not a list, using defaults")
		loadDefaults()
		return
	}

	for i, raw := range langList {
		lm, ok := raw.(map[string]any)
		if !ok {
			continue
		}
		ld := LangDef{
			Name:        getString(lm, "name"),
			Interpreted: getBool(lm, "interpreted"),
			Compiler:    getString(lm, "compiler"),
			CompileArgs: getStringSlice(lm, "compile_args"),
			CompileOut:  getStringSlice(lm, "compile_out"),
			RunArgs:     getStringSlice(lm, "run_args"),
			SourceFile:  getString(lm, "source_file"),
		}
		if ld.Name == "" {
			log.Printf("[langconf] skip language entry %d: empty name", i)
			continue
		}
		languages = append(languages, ld)
		langMap[ld.Name] = &languages[len(languages)-1]
	}

	if len(languages) == 0 {
		log.Println("[langconf] no valid languages in config, using defaults")
		loadDefaults()
		return
	}

	log.Printf("[langconf] loaded %d languages from config", len(languages))
	for _, l := range languages {
		kind := "compiled"
		if l.Interpreted {
			kind = "interpreted"
		}
		log.Printf("[langconf]   %s (%s): compiler=%s, run=%v", l.Name, kind, l.Compiler, l.RunArgs)
	}
}

func loadDefaults() {
	defaults := []LangDef{
		{Name: "c", Compiler: "gcc", CompileArgs: []string{"main.c", "-o", "main"}, CompileOut: []string{"main"}, RunArgs: []string{"./main"}, SourceFile: "main.c"},
		{Name: "cpp", Compiler: "g++", CompileArgs: []string{"main.cpp", "-o", "main"}, CompileOut: []string{"main"}, RunArgs: []string{"./main"}, SourceFile: "main.cpp"},
		{Name: "go", Compiler: "go", CompileArgs: []string{"build", "-o", "main", "main.go"}, CompileOut: []string{"main"}, RunArgs: []string{"./main"}, SourceFile: "main.go"},
		{Name: "rust", Compiler: "rustc", CompileArgs: []string{"main.rs", "-o", "main"}, CompileOut: []string{"main"}, RunArgs: []string{"./main"}, SourceFile: "main.rs"},
		{Name: "java", Compiler: "javac", CompileArgs: []string{"Main.java"}, CompileOut: []string{"Main.class"}, RunArgs: []string{"java", "Main"}, SourceFile: "Main.java"},
		{Name: "python3", Interpreted: true, RunArgs: []string{"python3", "main.py"}, SourceFile: "main.py"},
		{Name: "python", Interpreted: true, RunArgs: []string{"python3", "main.py"}, SourceFile: "main.py"},
		{Name: "bash", Interpreted: true, RunArgs: []string{"sh", "main.sh"}, SourceFile: "main.sh"},
		{Name: "sh", Interpreted: true, RunArgs: []string{"sh", "main.sh"}, SourceFile: "main.sh"},
		{Name: "javascript", Interpreted: true, RunArgs: []string{"node", "main.js"}, SourceFile: "main.js"},
		{Name: "js", Interpreted: true, RunArgs: []string{"node", "main.js"}, SourceFile: "main.js"},
		{Name: "node", Interpreted: true, RunArgs: []string{"node", "main.js"}, SourceFile: "main.js"},
	}
	for i := range defaults {
		languages = append(languages, defaults[i])
		langMap[defaults[i].Name] = &languages[len(languages)-1]
	}
}

// Get 获取指定语言的定义，返回 nil 表示不支持
func Get(name string) *LangDef {
	mu.RLock()
	defer mu.RUnlock()
	if !inited {
		mu.RUnlock()
		Init()
		mu.RLock()
	}
	return langMap[name]
}

// IsInterpreted 判断语言是否为解释型
func IsInterpreted(name string) bool {
	ld := Get(name)
	if ld == nil {
		return false
	}
	return ld.Interpreted
}

// All 返回所有已注册语言
func All() []LangDef {
	mu.RLock()
	defer mu.RUnlock()
	if !inited {
		mu.RUnlock()
		Init()
		mu.RLock()
	}
	result := make([]LangDef, len(languages))
	copy(result, languages)
	return result
}

// ---------- helper ----------

func getString(m map[string]any, key string) string {
	if v, ok := m[key]; ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return ""
}

func getBool(m map[string]any, key string) bool {
	if v, ok := m[key]; ok {
		if b, ok := v.(bool); ok {
			return b
		}
	}
	return false
}

func getStringSlice(m map[string]any, key string) []string {
	if v, ok := m[key]; ok {
		if arr, ok := v.([]any); ok {
			result := make([]string, len(arr))
			for i, a := range arr {
				result[i], _ = a.(string)
			}
			return result
		}
	}
	return nil
}
