package io.charlie.web.oj.modular.data.problem.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.problem.service.DataProblemService;
import io.charlie.web.oj.modular.task.judge.dto.TestCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 24/09/2025
 * @description TODO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemImportTool {
    private final DataProblemMapper problemMapper;

    public void importProblems(MultipartFile file) {
        try {
            // 创建临时目录
            Path tempDir = Files.createTempDirectory("problem_import_");
            File tempFile = new File(tempDir.toFile(), Objects.requireNonNull(file.getOriginalFilename()));

            // 保存上传的文件
            file.transferTo(tempFile);

            // 解压文件
            extractZipFile(tempFile, tempDir.toFile());

            // 处理解压后的目录
            processExtractedDirectory(tempDir.toFile());

            // 清理临时文件
            deleteDirectory(tempDir.toFile());

        } catch (IOException e) {
            log.error("导入问题失败", e);
            throw new RuntimeException("导入问题失败: " + e.getMessage());
        }
    }

    private void extractZipFile(File zipFile, File destDir) throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)))) {

            ZipArchiveEntry entry;
            while ((entry = zis.getNextZipEntry()) != null) {
                File file = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                        IOUtils.copy(zis, os);
                    }
                }
            }
        }
    }

    private void processExtractedDirectory(File baseDir) {
        File[] problemDirs = baseDir.listFiles(File::isDirectory);
        if (problemDirs == null) {
            throw new RuntimeException("压缩包中未找到问题目录");
        }

        List<DataProblem> problems = new ArrayList<>();

        for (File problemDir : problemDirs) {
            try {
                processProblemDirectory(problemDir, problems);
            } catch (Exception e) {
                log.error("处理问题目录失败: {}", problemDir.getName(), e);
            }
        }

        problemMapper.insert(problems);
    }

    private void processProblemDirectory(File problemDir, List<DataProblem> problems) throws IOException {
        DataProblem problemEntity = new DataProblem();

        // 读取problem.json
        File jsonFile = new File(problemDir, "problem.json");
        if (!jsonFile.exists()) {
            log.warn("目录 {} 中未找到problem.json文件", problemDir.getName());
            return;
        }

        String jsonContent = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
        ProblemImport problem = new ObjectMapper().readValue(jsonContent, ProblemImport.class);

        problemEntity.setId(null);
        problemEntity.setDisplayId(String.valueOf(problem.getDisplayId()));
        problemEntity.setTitle(problem.getTitle());
        problemEntity.setDescription(
                (problem.getDescription().getValue() != null ? "## 题目描述\n" + problem.getDescription().getValue() : "")
                        +
                        (problem.getInputDescription().getValue() != null ? "\n\n## 输入描述\n" + problem.getInputDescription().getValue() : "")
                        +
                        (problem.getOutputDescription().getValue() != null ? "\n\n## 输出描述\n" + problem.getOutputDescription().getValue() : "")
                        +
                        (problem.getHint().getValue() != null ? "\n\n## 提示\n" + problem.getHint().getValue() : "")
                        +
                        (problem.getRuleType() != null ? "\n\n## 规则\n" + problem.getRuleType() : "")
        );
        problemEntity.setMaxTime(BigDecimal.valueOf(problem.getTimeLimit()));
        problemEntity.setMaxMemory(BigDecimal.valueOf(problem.getMemoryLimit()));
        problemEntity.setSource(problem.getSource());
        problemEntity.setDifficulty(1);
        problemEntity.setCategoryId("0");
        problemEntity.setIsPublic(true);
        // 开放语言，默认CPP/C
        problemEntity.setAllowedLanguages(List.of("cpp", "c"));
        problemEntity.setThreshold(new BigDecimal("0.5"));
        problemEntity.setUseTemplate(false);

        List<TestCase> testCases = new ArrayList<>();

        // 处理测试用例
        File testcaseDir = new File(problemDir, "testcase");
        if (testcaseDir.exists() && testcaseDir.isDirectory()) {
            File[] inputFiles = testcaseDir.listFiles((dir, name) -> name.endsWith(".in"));
            if (inputFiles != null) {
                for (File inputFile : inputFiles) {
                    TestCase testCase = new TestCase();

                    String baseName = FilenameUtils.getBaseName(inputFile.getName());
                    File outputFile = new File(testcaseDir, baseName + ".out");

                    if (outputFile.exists()) {
                        String inputContent = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
                        String outputContent = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
                        testCase.setInput(inputContent);
                        testCase.setOutput(outputContent);
                        testCases.add(testCase);
                        log.info("保存测试用例 {} 对于问题 {} ", baseName, problem.getDisplayId());
                    }
                }
            }
        }
        problemEntity.setTestCase(testCases);

        problems.add(problemEntity);
        log.info("成功导入问题: {} (ID: {})", problem.getTitle(), problem.getDisplayId());
    }

    private void processTestCases(File testcaseDir, ProblemImport problem) throws IOException {
        File[] inputFiles = testcaseDir.listFiles((dir, name) -> name.endsWith(".in"));
        if (inputFiles != null) {
            for (File inputFile : inputFiles) {
                String baseName = FilenameUtils.getBaseName(inputFile.getName());
                File outputFile = new File(testcaseDir, baseName + ".out");

                if (outputFile.exists()) {
                    String inputContent = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
                    String outputContent = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);

                    // 保存测试用例（需要根据你的实际需求实现）
                    saveTestCase(problem, baseName, inputContent, outputContent);
                }
            }
        }
    }

    private void saveProblemToDatabase(ProblemImport problem) {
        // 这里需要根据你的实际数据库操作来实现
        // 例如：problemRepository.save(convertToEntity(problem));
        log.info("保存问题到数据库: {}", problem.getTitle());
    }

    private void saveTestCase(ProblemImport problem, String caseName,
                              String inputContent, String outputContent) {
        // 这里需要根据你的实际需求来实现测试用例的保存
        log.info("保存测试用例 {} 对于问题 {}", caseName, problem.getDisplayId());
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            FileUtils.deleteDirectory(directory);
        }
    }
}
