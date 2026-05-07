import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

/**
 * OJ系统渐进式性能测试
 * 测试流程：登录 → 随机代码提交 → 轮询判题结果
 */

// 测试配置
export const options = {
  stages: [
    // 50用户测试
    { duration: '30s', target: 50 },     // 预热到50用户
    { duration: '1m', target: 50 },      // 稳定在50用户
    { duration: '30s', target: 0 },      // 降压到0用户
    { duration: '30s', target: 0 },      // 休息期
    
    // 100用户测试
    { duration: '30s', target: 100 },    // 预热到100用户
    { duration: '1m', target: 100 },     // 稳定在100用户
    { duration: '30s', target: 0 },      // 降压到0用户
    { duration: '30s', target: 0 },      // 休息期
    
    // 150用户测试
    { duration: '30s', target: 150 },    // 预热到150用户
    { duration: '1m', target: 150 },     // 稳定在150用户
    { duration: '30s', target: 0 },      // 降压到0用户
    { duration: '30s', target: 0 },      // 休息期
    
    // 200用户测试
    { duration: '30s', target: 200 },    // 预热到200用户
    { duration: '1m', target: 200 },     // 稳定在200用户
    { duration: '30s', target: 0 },      // 降压到0用户
  ],
  thresholds: {
    http_req_duration: ['p(95) < 5000'], // 95%的请求响应时间小于5秒
    http_req_failed: ['rate < 0.1'],      // 错误率小于10%
  },
};

// 测试环境配置
const testConfig = {
  baseURL: 'http://47.99.236.69:93',
  captchaCode: 9926,
};

// 性能指标收集
let performanceMetrics = {
  loginTimes: [],
  submissionTimes: [],
  judgeTimes: [],
  successSubmissions: 0,
  failedSubmissions: 0,
  totalJudgmentTime: 0,
  userSessions: 0,
  failedSessions: 0,
  judgmentResults: {
    ACCEPTED: 0,
    SUCCESS: 0,
    CORRECT: 0,
    TIMEOUT: 0,
    OTHER: 0
  }
};

/**
 * 用户数据加载模块
 */
const users = new SharedArray('users', function() {
  try {
    const data = open('./test_users.csv');
    const lines = data.split('\n').slice(1); // 跳过标题行
    
    const parsedUsers = lines.map((line, index) => {
      if (!line.trim()) return null;
      
      const parts = line.split(',');
      if (parts.length < 4) {
        console.warn(`第 ${index + 2} 行格式错误: ${line}`);
        return null;
      }
      
      const user_id = parts[0].trim();
      const username = parts[1].trim();
      const email = parts[2].trim();
      const password = parts[3].trim();
      
      return { user_id, username, email, password };
    }).filter(user => user && user.user_id && user.user_id !== '');
    
    console.log(`✅ 成功加载 ${parsedUsers.length} 个测试用户`);
    return parsedUsers;
  } catch (error) {
    console.error('❌ 用户数据加载失败:', error.message);
    return [];
  }
});

/**
 * 测试代码数据
 */
const codeData = [
  {
    problemId: '1998710577456422914',
    code: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello World!" << endl;
    return 0;
}`
  },
  {
    problemId: '1998710829378904065',
    code: `#include <iostream>
#include <iomanip>

using namespace std;

int main() {
    double d1, d2, d3, d4;

    d1 = 43211234;
    d2 = d1 * d1 * d1;
    d3 = 1.0 / 11;
    d4 = 1.9;

    cout << "d1=" << d1 << endl;
    cout << "d1*d1*d1=" << d2 << endl;
    cout << "1.0/11=" << d3 << endl;
    cout << "1.9=" << d4 << endl;

    cout.precision(25);

    cout << "d1=" << d1 << endl;
    cout << "d1*d1*d1=" << d2 << endl;
    cout << "1.0/11=" << d3 << endl;
    cout << "1.9=" << d4 << endl;

    return 0;
}`
  },
  {
    problemId: '1998710836748296193',
    code: `#include <iostream>
#include <vector>
using namespace std;

int main() {
    vector<int> arr;
    int x;
    char c;

    // 读取第一行所有整数
    while (true) {
        cin >> x;
        arr.push_back(x);
        c = cin.get(); // 读取下一个字符
        if (c == '\n' || c == '\r') break;
    }

    int target;
    cin >> target;

    // 查找
    for (int i = 0; i < arr.size(); i++) {
        if (arr[i] == target) {
            cout << i + 1 << endl;
            return 0;
        }
    }
    cout << 0 << endl;
    return 0;
}`
  }
];

console.log(`✅ 成功加载 ${codeData.length} 个测试代码文件`);

/**
 * 生成唯一提交ID
 */
function generateSubmitId() {
  const timestamp = Date.now();
  const randomStr = Math.random().toString(36).substring(2, 8);
  return `task-${timestamp}-${randomStr}`;
}

/**
 * 用户登录模块
 */
function loginUser(user) {
  const startTime = Date.now();
  const url = `${testConfig.baseURL}/api/v1/sys/auth/login`;
  
  const payload = JSON.stringify({
    username: user.username,
    email: user.email,
    password: user.password,
    captchaCode: testConfig.captchaCode,
    platform: 'CLIENT',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': 'K6-Performance-Test/1.0',
    },
    tags: { name: 'user_login', userId: user.user_id },
  };

  try {
    const response = http.post(url, payload, params);
    const endTime = Date.now();
    const loginTime = endTime - startTime;
    
    performanceMetrics.loginTimes.push(loginTime);
    
    const success = check(response, {
      'login successful': (r) => r.status === 200 && r.json('success') === true,
      'login returns token': (r) => r.json('data') !== undefined,
    });

    if (success) {
      console.log(`✅ 用户 ${user.username} 登录成功 (${loginTime}ms)`);
      return response.json('data');
    } else {
      console.log(`❌ 用户 ${user.username} 登录失败: ${response.status} - ${response.body}`);
      return null;
    }
  } catch (error) {
    console.log(`❌ 用户 ${user.username} 登录异常: ${error.message}`);
    return null;
  }
}

/**
 * 代码提交模块
 */
function submitCode(token, user) {
  const startTime = Date.now();
  const url = `${testConfig.baseURL}/api/v1/data/submit/execute`;
  
  // 随机选择一个问题和对应的代码
  if (codeData.length === 0) {
    console.error('❌ 没有可用的测试代码');
    return null;
  }
  
  const randomCodeItem = codeData[Math.floor(Math.random() * codeData.length)];
  const randomProblemId = randomCodeItem.problemId;
  const codeContent = randomCodeItem.code;
  const submitId = generateSubmitId();
  
  const payload = JSON.stringify({
    problemId: randomProblemId,
    setId: null,
    language: "cpp",
    code: codeContent,
    submitType: true,
    judgeTaskId: submitId,
  });

  const params = {
    headers: {
      'Authorization': token,
      'Content-Type': 'application/json',
      'User-Agent': 'K6-Performance-Test/1.0',
    },
    tags: { name: 'code_submission', userId: user.user_id },
  };

  try {
    const response = http.post(url, payload, params);
    const endTime = Date.now();
    const submissionTime = endTime - startTime;
    
    performanceMetrics.submissionTimes.push(submissionTime);
    
    const success = check(response, {
      'code submission successful': (r) => r.status === 200 && r.json('success') === true,
      'submission returns ID': (r) => r.json('data') !== undefined,
    });

    if (success) {
      console.log(`✅ 用户 ${user.username} 代码提交成功 (${submissionTime}ms)`);
      performanceMetrics.successSubmissions++;
      return response.json('data');
    } else {
      console.log(`❌ 用户 ${user.username} 代码提交失败: ${response.status}`);
      performanceMetrics.failedSubmissions++;
      return null;
    }
  } catch (error) {
    console.log(`❌ 用户 ${user.username} 代码提交异常: ${error.message}`);
    performanceMetrics.failedSubmissions++;
    return null;
  }
}

/**
 * 判题结果轮询模块
 */
function waitForJudgeResult(token, submitId, user, maxRetries = 90) {
  const url = `${testConfig.baseURL}/api/v1/data/submit/detail/client`;
  const judgeStartTime = Date.now();
  
  const params = {
    headers: {
      'Authorization': token,
      'User-Agent': 'K6-Performance-Test/1.0',
    },
    tags: { name: 'judge_result_query', userId: user.user_id },
  };

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const response = http.get(`${url}?id=${submitId}`, params);
      
      const checkResult = check(response, {
        'get judge result successful': (r) => r.status === 200 && r.json('success') === true,
      });

      if (checkResult) {
        const resultData = response.json('data');
        if (resultData && resultData.isFinish) {
          const judgeEndTime = Date.now();
          const totalJudgeTime = judgeEndTime - judgeStartTime;
          
          performanceMetrics.judgeTimes.push(totalJudgeTime);
          performanceMetrics.totalJudgmentTime += totalJudgeTime;
          
          // 统计判题结果
          const status = resultData.status || 'UNKNOWN';
          if (performanceMetrics.judgmentResults[status] !== undefined) {
            performanceMetrics.judgmentResults[status]++;
          } else {
            performanceMetrics.judgmentResults.OTHER++;
          }
          
          console.log(`✅ 用户 ${user.username} 判题完成: ${status} (总耗时: ${totalJudgeTime}ms)`);
          
          return {
            status: status,
            isFinish: true,
            memory: resultData.memory || 0,
            time: resultData.time || 0,
            score: resultData.score || 0,
            totalTime: totalJudgeTime,
            queryCount: attempt + 1,
          };
        } else {
          // 判题尚未完成，等待后重试
          if (attempt % 15 === 0) {
            console.log(`⏳ 用户 ${user.username} 判题进行中... (${attempt + 1}/${maxRetries})`);
          }
        }
      } else {
        console.log(`⚠️ 用户 ${user.username} 获取判题详情失败: ${response.status}`);
      }
    } catch (error) {
      console.log(`⚠️ 用户 ${user.username} 判题查询异常: ${error.message}`);
    }
    
    sleep(2); // 等待2秒后重试
  }

  console.log(`❌ 用户 ${user.username} 判题超时 (${maxRetries * 2}秒)`);
  performanceMetrics.judgmentResults.TIMEOUT++;
  return {
    status: 'TIMEOUT',
    isFinish: false,
    memory: 0,
    time: 0,
    score: 0,
    totalTime: Date.now() - judgeStartTime,
    queryCount: maxRetries,
  };
}

/**
 * 主测试流程
 */
export default function () {
  // 随机选择测试用户
  if (users.length === 0) {
    console.error('❌ 无可用测试用户');
    return;
  }
  
  const user = users[Math.floor(Math.random() * users.length)];
  if (!user) return;

  performanceMetrics.userSessions++;
  console.log(`🚀 开始处理用户会话: ${user.username}`);

  // 1. 用户登录
  const token = loginUser(user);
  if (!token) {
    console.log(`💥 用户 ${user.username} 会话终止: 登录失败`);
    performanceMetrics.failedSessions++;
    return;
  }

  // 2. 代码提交
  const submitId = submitCode(token, user);
  if (!submitId) {
    console.log(`💥 用户 ${user.username} 会话终止: 提交失败`);
    performanceMetrics.failedSessions++;
    return;
  }

  // 3. 等待并获取判题结果
  const judgeResult = waitForJudgeResult(token, submitId, user);

  // 4. 添加随机思考时间
  sleep(Math.random() * 3 + 1);
}

/**
 * 结果处理和报告生成
 */
export function handleSummary(data) {
  console.log('正在生成测试数据文件...');
  
  // 计算性能指标统计
  const calculateStats = (times) => {
    if (times.length === 0) return { avg: 0, min: 0, max: 0, p95: 0, count: 0 };
    const sorted = times.slice().sort((a, b) => a - b);
    return {
      avg: Math.round(times.reduce((a, b) => a + b, 0) / times.length),
      min: Math.round(Math.min(...times)),
      max: Math.round(Math.max(...times)),
      p95: Math.round(sorted[Math.floor(sorted.length * 0.95)]),
      count: times.length
    };
  };
  
  const loginStats = calculateStats(performanceMetrics.loginTimes);
  const submissionStats = calculateStats(performanceMetrics.submissionTimes);
  const judgeStats = calculateStats(performanceMetrics.judgeTimes);
  
  const totalSubmissions = performanceMetrics.successSubmissions + performanceMetrics.failedSubmissions;
  const successRate = totalSubmissions > 0 ? 
    ((performanceMetrics.successSubmissions / totalSubmissions) * 100).toFixed(2) : 0;
  
  const sessionSuccessRate = performanceMetrics.userSessions > 0 ?
    (((performanceMetrics.userSessions - performanceMetrics.failedSessions) / performanceMetrics.userSessions) * 100).toFixed(2) : 0;

  // 生成CSV格式的性能数据
  const csvData = `阶段,用户数,登录平均时间(ms),登录P95时间(ms),提交平均时间(ms),提交P95时间(ms),判题平均时间(ms),判题P95时间(ms),成功提交数,失败提交数,成功率(%),成功会话数,失败会话数,会话成功率(%)\n` +
    `50用户,50,${loginStats.avg},${loginStats.p95},${submissionStats.avg},${submissionStats.p95},${judgeStats.avg},${judgeStats.p95},${performanceMetrics.successSubmissions},${performanceMetrics.failedSubmissions},${successRate},${performanceMetrics.userSessions - performanceMetrics.failedSessions},${performanceMetrics.failedSessions},${sessionSuccessRate}`;

  // 完整数据JSON
  const fullTestData = {
    k6OriginalMetrics: data,
    customMetrics: {
      performanceMetrics: performanceMetrics,
      loginStats: loginStats,
      submissionStats: submissionStats,
      judgeStats: judgeStats,
      totalSubmissions: totalSubmissions,
      submissionSuccessRate: successRate,
      sessionSuccessRate: sessionSuccessRate
    },
    textSummary: textSummary(data, { indent: ' ', enableColors: false }),
    csvData: csvData
  };

  console.log('\n' + '='.repeat(80));
  console.log('📊 OJ系统性能测试报告生成完成');
  console.log('='.repeat(80));
  console.log('测试结果概览:');
  console.log(`总会话数: ${performanceMetrics.userSessions}`);
  console.log(`成功提交数: ${performanceMetrics.successSubmissions}`);
  console.log(`成功率: ${successRate}%`);
  console.log(`平均登录时间: ${loginStats.avg}ms`);
  console.log(`平均提交时间: ${submissionStats.avg}ms`);
  console.log(`平均判题时间: ${judgeStats.avg}ms`);
  console.log('='.repeat(80));
  
  return {
    'performance_test_results.json': JSON.stringify(fullTestData, null, 2),
    'performance_summary.csv': csvData,
    'performance_summary.txt': textSummary(data, { indent: ' ', enableColors: false }),
    'stdout': textSummary(data, { indent: ' ', enableColors: true })
  };
}

/**
 * 测试环境预检查
 */
export function setup() {
  console.log('🔧 初始化OJ系统性能测试环境...');
  console.log(`📍 目标服务器: ${testConfig.baseURL}`);
  console.log(`👥 可用测试用户: ${users.length} 个`);
  console.log(`💻 可用测试代码: ${codeData.length} 个`);
  console.log('✅ 测试环境初始化完成\n');
}