package io.charlie.web.modular.task.similarity;

import io.charlie.Application;
import io.charlie.web.modular.data.library.entity.DataLibrary;
import io.charlie.web.modular.data.library.mapper.DataLibraryMapper;
import io.charlie.web.utils.similarity.utils.SimilarityCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Similarity module functional tests (C_01 ~ C_04).
 * <p>
 * Injects {@link SimilarityCalculator} and compares two source codes via Token + GST.
 * Before running, execute {@code sql/similarity_test_data.sql}.
 */
@SpringBootTest(classes = Application.class)
@DisplayName("Similarity module functional tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimilarityModuleTest {

    /** Same sensitivity as UI/batch default (token count should be >= this for short snippets) */
    private static final int MIN_MATCH_LENGTH = 8;

    private static final String LANGUAGE = "cpp";

    @Autowired
    private SimilarityCalculator similarityCalculator;

    @Autowired
    private DataLibraryMapper dataLibraryMapper;

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

    private String baseCode;
    private String c01Code;
    private String c02Code;
    private String c03Code;
    private String c04Code;

    @BeforeAll
    void loadCodesFromLibrary() {
        baseCode = requireCode(baseLibId);
        c01Code = requireCode(c01LibId);
        c02Code = requireCode(c02LibId);
        c03Code = requireCode(c03LibId);
        c04Code = requireCode(c04LibId);
    }

    @Test
    @Order(1)
    @DisplayName("C_01 Identical code: similarity should be 1.00")
    void c01_identicalCode() {
        double similarity = similarityCalculator.calculate(LANGUAGE, baseCode, c01Code, MIN_MATCH_LENGTH);

        assertEquals(1.00, similarity, 0.001,
                "Identical sources should score 1.00, actual=" + similarity);
    }

    @Test
    @Order(2)
    @DisplayName("C_02 Completely different code: similarity should be below threshold (< 0.3)")
    void c02_completelyDifferentCode() {
        double similarity = similarityCalculator.calculate(LANGUAGE, baseCode, c02Code, MIN_MATCH_LENGTH);

        assertTrue(similarity < 0.3,
                "Different sources should score < 0.3, actual=" + similarity);
    }

    @Test
    @Order(3)
    @DisplayName("C_03 Renamed identifiers: should resist identifier obfuscation, similarity stays 1.00")
    void c03_renamedIdentifiers() {
        double similarity = similarityCalculator.calculate(LANGUAGE, baseCode, c03Code, MIN_MATCH_LENGTH);

        assertEquals(1.00, similarity, 0.001,
                "After renaming variables/functions, token-type sequence should match; expect 1.00, actual=" + similarity);
    }

    @Test
    @Order(4)
    @DisplayName("C_04 Comments and whitespace: should resist format obfuscation, similarity stays 1.00")
    void c04_commentsAndWhitespace() {
        double similarity = similarityCalculator.calculate(LANGUAGE, baseCode, c04Code, MIN_MATCH_LENGTH);

        assertEquals(1.00, similarity, 0.001,
                "After adding comments/whitespace, similarity should stay 1.00, actual=" + similarity);
    }

    private String requireCode(String libraryId) {
        DataLibrary library = dataLibraryMapper.selectById(libraryId);
        assertNotNull(library, "Sample missing; run sql/similarity_test_data.sql first, id=" + libraryId);
        assertNotNull(library.getCode(), "Sample code is empty: " + libraryId);
        assertTrue(!library.getCode().isBlank(), "Sample code is empty: " + libraryId);
        return library.getCode();
    }
}
