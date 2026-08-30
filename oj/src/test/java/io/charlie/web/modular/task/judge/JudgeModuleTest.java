package io.charlie.web.modular.task.judge;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.Application;
import io.charlie.cores.result.Result;
import io.charlie.web.modular.data.submit.controller.DataSubmitController;
import io.charlie.web.modular.data.submit.entity.DataSubmit;
import io.charlie.web.modular.data.submit.param.DataSubmitExeParam;
import io.charlie.web.modular.data.submit.param.DataSubmitIdParam;
import io.charlie.web.modular.sys.user.entity.SysUser;
import io.charlie.web.modular.sys.user.mapper.SysUserMapper;
import io.charlie.web.modular.task.judge.enums.JudgeStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Judge module functional tests (J_01 ~ J_04).
 * <p>
 * Calls Controller → Service directly; real MQ / judge-service writes back results.
 * Before running:
 * <ol>
 *   <li>Execute {@code sql/judge_test_problem.sql} (user junit_judge / junit123456, problem and cases)</li>
 *   <li>Ensure Nacos, MySQL, Redis, RabbitMQ, oj, and judge-service are available</li>
 * </ol>
 */
@SpringBootTest(classes = Application.class)
@DisplayName("Judge module functional tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JudgeModuleTest {

    @Autowired
    private DataSubmitController dataSubmitController;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${oj.test.username:junit_judge}")
    private String username;

    /** A+B problem from sql/judge_test_problem.sql */
    @Value("${oj.test.problemId:junit_judge_a_plus_b}")
    private String problemId;

    @Value("${oj.test.language:cpp}")
    private String language;

    /** Correct A + B implementation */
    private static final String ACCEPTED_CODE = """
            #include <iostream>
            int main() {
                long long a, b;
                std::cin >> a >> b;
                std::cout << a + b << std::endl;
                return 0;
            }
            """;

    private static final String COMPILE_ERROR_CODE = """
            int main() {
                int a b;
                return 0;
            }
            """;

    private static final String TIME_LIMIT_CODE = """
            int main() {
                while (true) {}
                return 0;
            }
            """;

    private static final String RUNTIME_ERROR_CODE = """
            #include <vector>
            int main() {
                std::vector<int> v;
                return v.at(100);
            }
            """;

    @BeforeEach
    void loginAsTestUser() {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.toLowerCase()));
        assertNotNull(user, "Test user not found: " + username);
        StpUtil.login(user.getId(), "CLIENT");
    }

    @AfterEach
    void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Test
    @Order(1)
    @DisplayName("J_01 Accepted: correct code should be ACCEPTED")
    void j01_accepted() throws Exception {
        DataSubmit result = submitAndWait(ACCEPTED_CODE);

        assertEquals(JudgeStatus.ACCEPTED.getValue(), result.getStatus(),
                "Correct solution should be ACCEPTED");
        assertTrue(Boolean.TRUE.equals(result.getIsFinish()), "Judging should be finished");
    }

    @Test
    @Order(2)
    @DisplayName("J_02 Compilation error: syntax error should return COMPILATION_ERROR with message")
    void j02_compilationError() throws Exception {
        DataSubmit result = submitAndWait(COMPILE_ERROR_CODE);

        assertEquals(JudgeStatus.COMPILATION_ERROR.getValue(), result.getStatus(),
                "Syntax error should be COMPILATION_ERROR");
        assertTrue(Boolean.TRUE.equals(result.getIsFinish()));
        assertNotNull(result.getMessage());
        assertFalse(result.getMessage().isBlank(), "Compilation failure should return error details");
    }

    @Test
    @Order(3)
    @DisplayName("J_03 Resource limit: infinite loop should return TLE or MLE")
    void j03_resourceLimitExceeded() throws Exception {
        DataSubmit result = submitAndWait(TIME_LIMIT_CODE);

        Set<String> expected = Set.of(
                JudgeStatus.TIME_LIMIT_EXCEEDED.getValue(),
                JudgeStatus.MEMORY_LIMIT_EXCEEDED.getValue()
        );
        assertTrue(expected.contains(result.getStatus()),
                "Sandbox should kill the process and return TLE/MLE, actual: " + result.getStatus());
        assertTrue(Boolean.TRUE.equals(result.getIsFinish()));
    }

    @Test
    @Order(4)
    @DisplayName("J_04 Runtime error: out-of-bounds access should return RUNTIME_ERROR")
    void j04_runtimeError() throws Exception {
        DataSubmit result = submitAndWait(RUNTIME_ERROR_CODE);

        assertEquals(JudgeStatus.RUNTIME_ERROR.getValue(), result.getStatus(),
                "Out-of-bounds access should be RUNTIME_ERROR");
        assertTrue(Boolean.TRUE.equals(result.getIsFinish()));
    }

    /**
     * Controller.execute → Service.handleProblemSubmit → MQ judge,
     * then poll Controller.detailClient until finished.
     */
    private DataSubmit submitAndWait(String code) throws InterruptedException {
        DataSubmitExeParam param = new DataSubmitExeParam();
        param.setJudgeTaskId("task-junit-" + UUID.randomUUID());
        param.setProblemId(problemId);
        param.setLanguage(language);
        param.setCode(code);
        param.setSubmitType(true);

        Result<?> submitResult = dataSubmitController.execute(param);
        assertTrue(Boolean.TRUE.equals(submitResult.getSuccess()),
                () -> "Submit failed: " + submitResult.getMessage());
        assertInstanceOf(String.class, submitResult.getData());
        String submitId = (String) submitResult.getData();
        assertFalse(submitId.isBlank());

        return waitForFinish(submitId);
    }

    private DataSubmit waitForFinish(String submitId) throws InterruptedException {
        DataSubmitIdParam idParam = new DataSubmitIdParam();
        idParam.setId(submitId);

        int maxRetries = 90;
        for (int i = 0; i < maxRetries; i++) {
            Result<?> detailResult = dataSubmitController.detailClient(idParam);
            assertTrue(Boolean.TRUE.equals(detailResult.getSuccess()),
                    () -> "Failed to query submit detail: " + detailResult.getMessage());
            assertInstanceOf(DataSubmit.class, detailResult.getData());
            DataSubmit data = (DataSubmit) detailResult.getData();
            if (Boolean.TRUE.equals(data.getIsFinish())) {
                return data;
            }
            Thread.sleep(2000L);
        }
        throw new AssertionError("Judging timed out, submitId=" + submitId);
    }
}
