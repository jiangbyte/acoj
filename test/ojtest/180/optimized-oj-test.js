import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

/**
 *  k6 run optimized-oj-test-500.js
 * OJ系统性能测试配置 - 单实例SpringBoot部署环境
 * 测试目标：评估系统在并发用户增长情况下的性能表现和稳定性
 */
export const options = {
  // 分阶段压力测试：模拟真实用户负载变化
  stages: [
    // ==================== 100用户测试 - 常规负载 (快速版) ====================
    // { duration: '30s', target: 30 },     // 阶段1：快速预热到30用户
    // { duration: '1m', target: 70 },      // 阶段2：快速增加到70用户
    // { duration: '2m', target: 100 },     // 阶段3：稳定在100用户 (核心测试区)
    // { duration: '1m', target: 60 },      // 阶段4：降压到60用户
    // { duration: '30s', target: 30 },     // 阶段5：进一步降压
    // { duration: '30s', target: 0 },      // 阶段6：冷却期

    // ==================== 120用户测试 - 压力测试 (快速版) ====================
    // { duration: '30s', target: 30 },     // 预热
    // { duration: '1m', target: 120 },     // 直接爬升到120
    // { duration: '2m', target: 120 },     // 稳定在120 (观测点1)
    // { duration: '1m', target: 60 },      // 降压
    // { duration: '30s', target: 0 },      // 冷却归零

    // ==================== 140用户测试 - 压力测试 (快速版) ====================
    // { duration: '30s', target: 30 },     // 预热
    // { duration: '1m', target: 130 },     // 直接爬升到140
    // { duration: '2m', target: 130 },     // 稳定在140 (观测点2)
    // { duration: '1m', target: 60 },      // 降压
    // { duration: '30s', target: 0 },      // 冷却归零

    // ==================== 150用户测试 - 压力测试 (快速版) ====================
  // { duration: '10s', target: 30 },     // 极速预热：只需确保线程启动
  // { duration: '20s', target: 150 },    // 快速爬升：直接拉到目标值
  // { duration: '1m',  target: 150 },    // 核心观测：1分钟足够采集大量样本判断稳定性
  // { duration: '10s', target: 0 },      // 瞬间停止：无需缓慢降压，测完即停

    // ==================== 160用户测试 - 压力测试 (快速版) ====================
    // { duration: '30s', target: 30 },     // 预热
    // { duration: '1m', target: 160 },     // 直接爬升到160
    // { duration: '2m', target: 160 },     // 稳定在160 (观测点3：疑似拐点)
    // { duration: '1m', target: 60 },      // 降压
    // { duration: '30s', target: 0 },      // 冷却归零

    // ==================== 180用户测试 - 压力测试 (快速版) ====================
{ duration: '30s', target: 90 },     // 极速预热：快速拉起基础线程
  { duration: '80s', target: 180 },     // 直接爬升到120
  { duration: '2m', target: 180 },     // 稳定在120 (观测点1)
  { duration: '1m', target: 90 },      // 降压
{ duration: '30s',  target: 0 },      // 立即切断：极限测试后无需缓慢降压，防止故障扩散

    // ==================== 200用户测试 - 稳定性验证 (快速版) ====================
  // { duration: '12s', target: 50 },   // 原30s -> 12s: 快速爬到第一台阶
  // { duration: '24s', target: 100 },  // 原1m  -> 24s: 继续爬升到第二台阶
  // { duration: '48s', target: 200 },  // 原2m  -> 48s: 爬到峰值并维持近1分钟 (核心观测)
  // { duration: '24s', target: 100 },  // 原1m  -> 24s: 快速降压回中负载
  // { duration: '12s', target: 50 },   // 原30s -> 12s: 继续降压回低负载
  // { duration: '10s', target: 0 }     // 原30s -> 10s: 快速归零
  ],
};

// 测试环境配置
const testConfig = {
  // serverIP: '47.99.236.69',
  // serverIP: 'localhost',
  serverIP: '8.130.21.17',
  //serverIP: '192.168.31.65',
  serverPORT: '93',
  // baseURL: `http://47.99.236.69:93`,
  // baseURL: `http://localhost:89`,
  baseURL: `http://8.130.21.17:89`,
  //baseURL: `http://192.168.31.65:89`,
  testProblemId: '2030077872317329410', // 本地
  // testProblemId: '1985678530483163137', // 云端
  captchaCode: 9926, // 测试环境验证码
};

// 性能指标收集
let performanceMetrics = {
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
 * 使用SharedArray确保多VU间共享内存数据
 */
const users = new SharedArray('users', function() {
  try {
    const data = open('./测试用户数据_1000个.csv');
    const lines = data.split('\n').slice(1); // 跳过标题行

    const parsedUsers = lines.map((line, index) => {
      if (!line.trim()) return null;

      const parts = line.split(',');
      if (parts.length < 4) {
        console.warn(`第 ${index + 2} 行格式错误: ${line}`);
        return null;
      }

      // 清理字段数据
      const user_id = parts[0].trim().replace(/^"|"$/g, '');
      const username = parts[1].trim().replace(/^"|"$/g, '');
      const email = parts[2].trim().replace(/^"|"$/g, '');
      const password = parts[3].trim().replace(/^"|"$/g, '');
      const token = parts[4] ? parts[4].trim().replace(/^"|"$/g, '') : '';

      return { user_id, username, email, password, token };
    }).filter(user => user && user.user_id && user.user_id !== '');

    console.log(`✅ 成功加载 ${parsedUsers.length} 个测试用户`);

    // 验证数据质量
    if (parsedUsers.length < 100) {
      console.warn('⚠️ 测试用户数量较少，可能影响并发测试效果');
    }

    return parsedUsers;
  } catch (error) {
    console.error('❌ 用户数据加载失败:', error.message);
    return [];
  }
});

/**
 * 测试代码模板库
 * 包含多种C++ A+B问题实现，模拟真实提交多样性
 */
const codeVariants = [
  // 标准for循环实现
  `#include <iostream>

int main() {
    std::cout << "2 3 5 7" << std::endl;
    return 0;
}`];

/**
 * 代码提交模块
 */
function submitCode(token, user) {
  const startTime = Date.now();
  const url = `${testConfig.baseURL}/api/v1/data/submit/execute`;

  const randomCode = codeVariants[Math.floor(Math.random() * codeVariants.length)];
  const submitId = `task-${Date.now()}-${Math.random().toString(36).substring(2, 8)}`;

  const payload = JSON.stringify({
    problemId: testConfig.testProblemId,
    setId: null,
    language: "cpp",
    code: randomCode,
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
    // timeout: '30s',
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
 * 判题结果查询模块
 * 包含轮询机制和超时处理
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
    // timeout: '10s',
  };

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    const queryStartTime = Date.now();

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
          const queryTime = judgeEndTime - queryStartTime;

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
          if (attempt % 15 === 0) { // 每15次查询输出一次日志
            console.log(`⏳ 用户 ${user.username} 判题进行中... (${attempt + 1}/${maxRetries})`);
          }
        }
      } else {
        console.log(`⚠️ 用户 ${user.username} 获取判题详情失败: ${response.status}`);
      }
    } catch (error) {
      console.log(`⚠️ 用户 ${user.username} 判题查询异常: ${error.message}`);
    }

    sleep(2); // 等待2秒后重试，减少服务器压力
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
 * 模拟完整用户行为：登录 → 提交代码 → 等待判题结果
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
  const userSessionId = `user-${user.user_id}-${Math.random().toString(36).substring(2, 8)}`;

  console.log(`🚀 开始处理用户会话: ${userSessionId} (${user.username})`);

  // 1. 直接从用户数据中获取token
  const token = user.token;
  if (!token) {
    console.log(`💥 用户 ${user.username} 会话终止: Token不存在`);
    performanceMetrics.failedSessions++;
    return;
  }

  console.log(`✅ 用户 ${user.username} 使用已有Token`);

  // 2. 代码提交
  const submitId = submitCode(token, user);
  if (!submitId) {
    console.log(`💥 用户 ${user.username} 会话终止: 提交失败`);
    performanceMetrics.failedSessions++;
    return;
  }

  // 3. 等待并获取判题结果
  const judgeResult = waitForJudgeResult(token, submitId, user);

  // 4. 验证最终结果
  const finalCheck = check(judgeResult, {
    'judge completed successfully': (result) => result.isFinish === true,
  });

  if (finalCheck) {
    console.log(`🎉 用户 ${user.username} 完整流程成功完成`);
  } else {
    console.log(`⚠️ 用户 ${user.username} 流程完成但存在异常`);
  }

  // 5. 添加随机思考时间，模拟真实用户行为
  sleep(Math.random() * 5 + 2);
}

export function handleSummary(data) {
  console.log('正在生成测试数据文件...');

  // 【关键修复】在handleSummary内部定义calculateStats函数
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

  const timestamp = Date.now();
  const jsonFileName = `data-${timestamp}.json`; // 供Python读取的完整数据
  const textFileName = `summary-${timestamp}.txt`; // 保存textSummary内容

  // 1. 计算性能指标统计
  const submissionStats = calculateStats(performanceMetrics.submissionTimes);
  const judgeStats = calculateStats(performanceMetrics.judgeTimes);

  const totalSubmissions = performanceMetrics.successSubmissions + performanceMetrics.failedSubmissions;
  const successRate = totalSubmissions > 0 ?
      ((performanceMetrics.successSubmissions / totalSubmissions) * 100).toFixed(2) : 0;

  const sessionSuccessRate = performanceMetrics.userSessions > 0 ?
      (((performanceMetrics.userSessions - performanceMetrics.failedSessions) / performanceMetrics.userSessions) * 100).toFixed(2) : 0;

  // 2. 完整数据JSON（包含原始metrics和自定义指标）
  const fullTestData = {
    k6OriginalMetrics: data, // k6原生指标（如http_req_duration、http_reqs等）
    customMetrics: { // 自定义的提交、判题指标
      performanceMetrics: performanceMetrics, // 原始时间数组（用于Python计算分布）
      submissionStats: submissionStats,
      judgeStats: judgeStats,
      totalSubmissions: totalSubmissions,
      submissionSuccessRate: successRate,
      sessionSuccessRate: sessionSuccessRate
    },
    textSummary: textSummary(data, { indent: ' ', enableColors: false }) // 保存textSummary文本
  };

  console.log('\n' + '='.repeat(80));
  console.log('📥 测试数据生成完成');
  console.log(`JSON数据文件: ${jsonFileName}`);
  console.log(`文本摘要文件: ${textFileName}`);
  console.log('='.repeat(80));

  // 输出3个文件：完整JSON（供Python用）、textSummary文本、控制台打印
  return {
    [jsonFileName]: JSON.stringify(fullTestData, null, 2),
    [textFileName]: fullTestData.textSummary,
    'stdout': fullTestData.textSummary // 控制台仍显示textSummary
  };
}

/**
 * 测试环境预检查
 */
export function setup() {
  console.log('🔧 初始化OJ系统性能测试环境...');
  console.log(`📍 目标服务器: ${testConfig.baseURL}`);
  console.log(`👥 可用测试用户: ${users.length} 个`);
  console.log(`💻 测试代码变体: ${codeVariants.length} 种`);
  console.log('✅ 测试环境初始化完成\n');
}