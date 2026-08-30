package io.charlie.web.modular.task.similarity;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.Application;
import io.charlie.cores.result.Result;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.modular.data.library.param.BatchLibraryQueryParam;
import io.charlie.web.modular.data.reports.entity.TaskReports;
import io.charlie.web.modular.data.reports.mapper.TaskReportsMapper;
import io.charlie.web.modular.data.similarity.controller.TaskSimilarityController;
import io.charlie.web.modular.data.similarity.entity.TaskSimilarity;
import io.charlie.web.modular.data.similarity.mapper.TaskSimilarityMapper;
import io.charlie.web.modular.sys.user.entity.SysUser;
import io.charlie.web.modular.sys.user.mapper.SysUserMapper;
import io.charlie.web.utils.similarity.utils.CodeTokenUtil;
import io.charlie.web.utils.similarity.utils.TokenDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Similarity module pairwise tests (C_01 ~ C_04) on the full production pipeline:
 * <pre>
 * Controller.batch → SimilarityService → RabbitMQ → similarity-service (Go GST)
 *   → task_similarity rows → result MQ → TaskReports stats
 * </pre>
 * Each case compares exactly two library samples (BASE vs C_0x).
 * <p>
 * Prerequisites: run {@code sql/similarity_test_data.sql}; Nacos / MySQL / Redis / RabbitMQ /
 * oj / similarity-service must be up.
 */
@SpringBootTest(classes = Application.class)
@DisplayName("Similarity module full-pipeline pairwise tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimilarityModuleTest {

    private static final int MIN_MATCH_LENGTH = 8;
    private static final String LANGUAGE = "cpp";
    private static final String MODULE_TYPE = "PROBLEM";
    private static final String COMPARE_MODE = "MULTI_BY_MULTI";

    @Autowired
    private TaskSimilarityController taskSimilarityController;

    @Autowired
    private DataLibraryMapper dataLibraryMapper;

    @Autowired
    private TaskReportsMapper taskReportsMapper;

    @Autowired
    private TaskSimilarityMapper taskSimilarityMapper;

    @Autowired
    private CodeTokenUtil codeTokenUtil;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${oj.test.username:junit_sim}")
    private String username;

    @Value("${oj.test.sim.problemId:junit_sim_sum}")
    private String problemId;

    @Value("${oj.test.sim.baseUserId:junit_sim_u_base}")
    private String baseUserId;

    @Value("${oj.test.sim.c01UserId:junit_sim_u_c01}")
    private String c01UserId;

    @Value("${oj.test.sim.c02UserId:junit_sim_u_c02}")
    private String c02UserId;

    @Value("${oj.test.sim.c03UserId:junit_sim_u_c03}")
    private String c03UserId;

    @Value("${oj.test.sim.c04UserId:junit_sim_u_c04}")
    private String c04UserId;

    @Value("${oj.test.sim.baseLibId:junit_sim_lib_base}")
    private String baseLibId;

    @Value("${oj.test.sim.c01LibId:junit_sim_lib_c01}")
    private String c01LibId;

    @Value("${oj.test.sim.c02LibId:junit_sim_lib_c02}")
    private String c02LibId;

    @Value("${oj.test.sim.c03LibId:junit_sim_lib_c03}")
    private String c03LibId;

    @Value("${oj.test.sim.c04LibId:junit_sim_lib_c04}")
    private String c04LibId;

    @BeforeAll
    void ensureLibraryTokens() {
        // similarity-service reads data_library.code_token; fill with production tokenizer
        for (String libId : List.of(baseLibId, c01LibId, c02LibId, c03LibId, c04LibId)) {
            DataLibrary library = dataLibraryMapper.selectById(libId);
            assertNotNull(library, "Library sample missing; run sql/similarity_test_data.sql, id=" + libId);
            assertNotNull(library.getCode(), "Library code is empty: " + libId);

            TokenDetail detail = codeTokenUtil.getCodeTokensDetail(LANGUAGE, library.getCode());
            assertNotNull(detail.getTokens());
            assertFalse(detail.getTokens().isEmpty(), "Tokenization produced empty tokens: " + libId);

            library.setCodeToken(detail.getTokens());
            library.setCodeTokenName(detail.getTokenNames());
            library.setCodeTokenTexts(detail.getTokenTexts());
            library.setCodeLength(library.getCode().length());
            assertTrue(dataLibraryMapper.updateById(library) > 0, "Failed to update tokens: " + libId);
        }
    }

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
    @DisplayName("C_01 Identical code: full pipeline similarity should be 1.00")
    void c01_identicalCode() throws Exception {
        BigDecimal similarity = runPairwiseAndGetSimilarity(baseUserId, c01UserId);

        assertEquals(0, new BigDecimal("1.00").compareTo(similarity.setScale(2, RoundingMode.HALF_UP)),
                "Identical sources should score 1.00, actual=" + similarity);
    }

    @Test
    @Order(2)
    @DisplayName("C_02 Completely different code: full pipeline similarity should be < 0.3")
    void c02_completelyDifferentCode() throws Exception {
        BigDecimal similarity = runPairwiseAndGetSimilarity(baseUserId, c02UserId);

        assertTrue(similarity.compareTo(new BigDecimal("0.30")) < 0,
                "Different sources should score < 0.3, actual=" + similarity);
    }

    @Test
    @Order(3)
    @DisplayName("C_03 Renamed identifiers: full pipeline similarity should stay 1.00")
    void c03_renamedIdentifiers() throws Exception {
        BigDecimal similarity = runPairwiseAndGetSimilarity(baseUserId, c03UserId);

        assertEquals(0, new BigDecimal("1.00").compareTo(similarity.setScale(2, RoundingMode.HALF_UP)),
                "After renaming identifiers, expect 1.00, actual=" + similarity);
    }

    @Test
    @Order(4)
    @DisplayName("C_04 Comments and whitespace: full pipeline similarity should stay 1.00")
    void c04_commentsAndWhitespace() throws Exception {
        BigDecimal similarity = runPairwiseAndGetSimilarity(baseUserId, c04UserId);

        assertEquals(0, new BigDecimal("1.00").compareTo(similarity.setScale(2, RoundingMode.HALF_UP)),
                "After comments/whitespace changes, expect 1.00, actual=" + similarity);
    }

    /**
     * Submit a MULTI_BY_MULTI batch with exactly two users (one pair), wait for Go worker,
     * then read the single task_similarity row.
     */
    private BigDecimal runPairwiseAndGetSimilarity(String userA, String userB) throws InterruptedException {
        BatchLibraryQueryParam param = new BatchLibraryQueryParam();
        param.setModuleType(MODULE_TYPE);
        param.setModuleId(problemId);
        param.setProblemId(problemId);
        param.setLanguage(LANGUAGE);
        param.setCompareMode(COMPARE_MODE);
        param.setUserIds(List.of(userA, userB));
        param.setMinMatchLength(MIN_MATCH_LENGTH);

        Result<?> batchResult = taskSimilarityController.batch(param);
        assertTrue(Boolean.TRUE.equals(batchResult.getSuccess()),
                () -> "Batch submit failed: " + batchResult.getMessage());
        assertInstanceOf(String.class, batchResult.getData());
        String reportId = (String) batchResult.getData();
        assertFalse(reportId.isBlank());

        TaskReports report = taskReportsMapper.selectById(reportId);
        assertNotNull(report, "TaskReports not found: " + reportId);
        String taskId = report.getTaskId();
        assertNotNull(taskId);

        TaskSimilarity pair = waitForPairResult(taskId);
        assertNotNull(pair.getSimilarity(), "similarity is null, taskId=" + taskId);
        return pair.getSimilarity();
    }

    private TaskSimilarity waitForPairResult(String taskId) throws InterruptedException {
        int maxRetries = 90;
        for (int i = 0; i < maxRetries; i++) {
            List<TaskSimilarity> rows = taskSimilarityMapper.selectList(
                    new LambdaQueryWrapper<TaskSimilarity>().eq(TaskSimilarity::getTaskId, taskId));
            if (rows != null && rows.size() == 1) {
                return rows.getFirst();
            }
            // Also accept completion via report stats (result MQ already applied)
            TaskReports report = taskReportsMapper.selectOne(
                    new LambdaQueryWrapper<TaskReports>().eq(TaskReports::getTaskId, taskId));
            if (report != null && report.getMaxSimilarity() != null && rows != null && !rows.isEmpty()) {
                return rows.getFirst();
            }
            Thread.sleep(2000L);
        }
        throw new AssertionError("Timed out waiting for similarity-service result, taskId=" + taskId);
    }
}
