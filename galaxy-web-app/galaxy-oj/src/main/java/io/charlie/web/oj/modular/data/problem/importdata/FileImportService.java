package io.charlie.web.oj.modular.data.problem.importdata;// FileImportService.java

import cn.hutool.core.util.StrUtil;
import io.charlie.web.oj.modular.data.problem.mapper.DataProblemMapper;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import io.charlie.web.oj.modular.data.testcase.mapper.DataTestCaseMapper;
import io.charlie.web.oj.modular.data.problem.utils.FpsXmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileImportService {
    private final FpsXmlParser fpsXmlParser;
    private final DataProblemMapper dataProblemMapper;
    private final DataTestCaseMapper dataTestCaseMapper;

    /**
     * 导入FPS文件（支持单个XML或ZIP压缩包）
     */
    public ImportResult importFpsFile(MultipartFile file) {
        try {
            String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

            if (fileName.endsWith(".xml")) {
                // 单个XML文件导入
                return importSingleXml(file);
            } else if (fileName.endsWith(".zip")) {
                // ZIP压缩包导入
                return importZipFile(file);
            } else {
                return ImportResult.error("不支持的文件格式，请上传XML或ZIP文件");
            }
        } catch (Exception e) {
            log.error("导入文件失败", e);
            return ImportResult.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导入单个XML文件
     */
    private ImportResult importSingleXml(MultipartFile file) throws IOException, DocumentException {
        try (InputStream inputStream = file.getInputStream()) {
            List<ProblemImportData> problemDataList = fpsXmlParser.parseFpsXml(inputStream);
            return saveProblems(problemDataList);
        }
    }

    /**
     * 导入ZIP压缩包 - 修复版本
     */
    private ImportResult importZipFile(MultipartFile file) throws IOException {
        List<ProblemImportData> allProblemData = new ArrayList<>();
        List<String> zipErrors = new ArrayList<>();

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(file.getInputStream())) {
            ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
                    try {
                        // 读取ZIP条目内容到字节数组
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        // 解析XML内容
                        byte[] xmlData = baos.toByteArray();
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData)) {
                            List<ProblemImportData> problemDataList = fpsXmlParser.parseFpsXml(bais);
                            allProblemData.addAll(problemDataList);
                            log.info("成功解析ZIP中的XML文件: {}，包含 {} 个题目",
                                    entry.getName(), problemDataList.size());
                        }

                    } catch (DocumentException e) {
                        String errorMsg = String.format("解析ZIP中的XML文件失败: %s, 错误: %s",
                                entry.getName(), e.getMessage());
                        zipErrors.add(errorMsg);
                        log.warn(errorMsg);
                    } catch (Exception e) {
                        String errorMsg = String.format("处理ZIP文件条目失败: %s, 错误: %s",
                                entry.getName(), e.getMessage());
                        zipErrors.add(errorMsg);
                        log.warn(errorMsg);
                    }
                } else {
                    log.debug("跳过ZIP中的非XML文件或目录: {}", entry.getName());
                }
            }
        }

        if (allProblemData.isEmpty()) {
            if (zipErrors.isEmpty()) {
                return ImportResult.error("ZIP文件中未找到有效的XML文件");
            } else {
                return ImportResult.error("ZIP文件中所有XML文件解析失败: " + String.join("; ", zipErrors));
            }
        }

        ImportResult result = saveProblems(allProblemData);
        // 添加ZIP处理过程中的错误信息
        if (!zipErrors.isEmpty()) {
            result.getErrors().addAll(0, zipErrors);
            result.setMessage(result.getMessage() + " (部分文件解析失败)");
        }

        return result;
    }

    /**
     * 保存题目数据到数据库
     */
    private ImportResult saveProblems(List<ProblemImportData> problemDataList) {
        int successCount = 0;
        int totalCount = problemDataList.size();
        List<String> errorMessages = new ArrayList<>();

        for (ProblemImportData importData : problemDataList) {
            try {
                // 检查题目标题是否为空
                if (!StrUtil.isNotBlank(importData.getProblem().getTitle())) {
                    errorMessages.add("跳过标题为空的题目");
                    continue;
                }

                // 保存题目
                dataProblemMapper.insert(importData.getProblem());
                log.info("成功导入题目: {} ID: {}", importData.getProblem().getTitle(), importData.getProblem().getId());

                // 测试用例设置题目ID
                // 保存测试用例
                List<DataTestCase> testCases = importData.getTestCases();
                for (DataTestCase testCase : testCases) {
                    testCase.setProblemId(importData.getProblem().getId());

                    // TODO 写入测试用例文件并上传minio
                }
                dataTestCaseMapper.insert(testCases);

                successCount++;
                log.info("成功导入题目: {}，包含 {} 个测试用例", importData.getProblem().getTitle(), importData.getTestCases().size());

            } catch (Exception e) {
                String errorMsg = StrUtil.format("导入题目[{}]失败: {}", importData.getProblem().getTitle(), e.getMessage());
                errorMessages.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        return ImportResult.of(successCount, totalCount, errorMessages);
    }
}