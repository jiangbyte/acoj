package io.charlie.web.oj.modular.data.problem.utils;// FpsXmlParser.java

import cn.hutool.core.util.IdUtil;
import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import io.charlie.web.oj.modular.data.problem.importdata.ProblemImportData;
import io.charlie.web.oj.modular.data.problem.importdata.SolutionData;
import io.charlie.web.oj.modular.data.problem.importdata.TemplateData;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FpsXmlParser {

    /**
     * 解析FPS XML文件
     */
    public List<ProblemImportData> parseFpsXml(InputStream inputStream) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();

        List<ProblemImportData> problemDataList = new ArrayList<>();

        // 解析每个item（题目）
        List<Element> items = root.elements("item");
        if (items.isEmpty()) {
            throw new DocumentException("XML文件中未找到题目数据");
        }

        for (Element item : items) {
            try {
                ProblemImportData problemData = parseProblemItem(item);
                if (problemData.getProblem() != null) {
                    problemDataList.add(problemData);
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他题目
                log.error("解析题目失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return problemDataList;
    }

    /**
     * 解析单个题目item
     */
    private ProblemImportData parseProblemItem(Element item) {
        ProblemImportData importData = new ProblemImportData();

        DataProblem problem = new DataProblem();

        // 解析题目基本信息
        problem.setTitle(getElementText(item, "title"));
        problem.setUrl(getElementText(item, "url"));
        problem.setSource(getElementText(item, "source"));

        // 解析时间限制并转换单位到ms
        Element timeLimit = item.element("time_limit");
        if (timeLimit != null) {
            String timeText = timeLimit.getTextTrim();
            String unit = timeLimit.attributeValue("unit");
            problem.setMaxTime(convertTimeToMs(timeText, unit));
        } else {
            problem.setMaxTime(new BigDecimal("1000")); // 默认1秒
        }

        // 解析内存限制并转换单位到KB
        Element memoryLimit = item.element("memory_limit");
        if (memoryLimit != null) {
            String memoryText = memoryLimit.getTextTrim();
            String unit = memoryLimit.attributeValue("unit");
            problem.setMaxMemory(convertMemoryToKb(memoryText, unit));
        } else {
            problem.setMaxMemory(new BigDecimal("65536")); // 默认64MB
        }

        // 构建完整的描述（包含输入输出描述和提示）
        StringBuilder descriptionBuilder = new StringBuilder();
        appendDescriptionSection(descriptionBuilder, "题目描述", getElementText(item, "description"));
        appendDescriptionSection(descriptionBuilder, "输入描述", getElementText(item, "input"));
        appendDescriptionSection(descriptionBuilder, "输出描述", getElementText(item, "output"));
        appendDescriptionSection(descriptionBuilder, "提示", getElementText(item, "hint"));

        problem.setDescription(descriptionBuilder.toString());

        // 设置默认值
        problem.setDifficulty(1); // 默认难度
        problem.setThreshold(BigDecimal.ZERO);
        problem.setIsPublic(Boolean.FALSE);
        problem.setIsVisible(Boolean.FALSE);
        problem.setUseAi(Boolean.FALSE);
        problem.setSolved(0L);

        importData.setProblem(problem);

        // ============================ 解析样例 ============================
        List<Element> sampleInputs = item.elements("sample_input");
        List<Element> sampleOutputs = item.elements("sample_output");
        int sampleCount = Math.min(sampleInputs.size(), sampleOutputs.size());
        for (int i = 0; i < sampleCount; i++) {
            String input = i < sampleInputs.size() ? sampleInputs.get(i).getText() : "";
            String output = i < sampleOutputs.size() ? sampleOutputs.get(i).getText() : "";
            // 只要其中一个有文本就可以创建
            if (StringUtils.hasText(input) || StringUtils.hasText(output)) {
                DataTestCase testCase = new DataTestCase();
                testCase.setCaseSign(IdUtil.objectId());
                testCase.setInputData(sampleInputs.get(i).getText());
                testCase.setExpectedOutput(sampleOutputs.get(i).getText());
                testCase.setIsSample(Boolean.TRUE);
                testCase.setScore(0);
                importData.getTestCases().add(testCase);
            }
        }

        // ============================ 解析测试用例 ============================
        List<Element> testInputs = item.elements("test_input");
        List<Element> testOutputs = item.elements("test_output");
        int testCount = Math.min(testInputs.size(), testOutputs.size());
        for (int i = 0; i < testCount; i++) {
            String input = i < testInputs.size() ? testInputs.get(i).getText() : "";
            String output = i < testOutputs.size() ? testOutputs.get(i).getText() : "";

            // 只要其中一个有文本就可以创建
            if (StringUtils.hasText(input) || StringUtils.hasText(output)) {
                DataTestCase testCase = new DataTestCase();
                testCase.setCaseSign(IdUtil.objectId());
                testCase.setInputData(input);
                testCase.setExpectedOutput(output);
                testCase.setIsSample(Boolean.FALSE);
                testCase.setScore(10);
                importData.getTestCases().add(testCase);
            }
        }

        // ============================ 解析解决方案 ============================
        for (Element solution : item.elements("solution")) {
            SolutionData solutionData = new SolutionData();
            solutionData.setLanguage(solution.attributeValue("language"));
            solutionData.setCode(solution.getText());
            importData.getSolutions().add(solutionData);
        }

        // ============================ 解析模板代码 ============================
        // 解析prepend
        for (Element prepend : item.elements("prepend")) {
            TemplateData template = new TemplateData();
            template.setType("prepend");
            template.setLanguage(prepend.attributeValue("language"));
            template.setCode(prepend.getText());
            importData.getTemplates().add(template);
        }

        // 解析template
        for (Element template : item.elements("template")) {
            TemplateData templateData = new TemplateData();
            templateData.setType("template");
            templateData.setLanguage(template.attributeValue("language"));
            templateData.setCode(template.getText());
            importData.getTemplates().add(templateData);
        }

        // 解析append
        for (Element append : item.elements("append")) {
            TemplateData template = new TemplateData();
            template.setType("append");
            template.setLanguage(append.attributeValue("language"));
            template.setCode(append.getText());
            importData.getTemplates().add(template);
        }

        return importData;
    }

    /**
     * 时间单位转换到ms
     */
    private BigDecimal convertTimeToMs(String timeText, String unit) {
        try {
            BigDecimal time = new BigDecimal(timeText);
            if ("s".equalsIgnoreCase(unit)) {
                return time.multiply(new BigDecimal("1000")); // 秒转毫秒
            } else if ("ms".equalsIgnoreCase(unit)) {
                return time; // 已经是毫秒
            } else {
                // 默认认为是秒
                return time.multiply(new BigDecimal("1000"));
            }
        } catch (NumberFormatException e) {
            return new BigDecimal("1000"); // 默认1秒
        }
    }

    /**
     * 内存单位转换到KB
     */
    private BigDecimal convertMemoryToKb(String memoryText, String unit) {
        try {
            BigDecimal memory = new BigDecimal(memoryText);
            if ("mb".equalsIgnoreCase(unit) || "m".equalsIgnoreCase(unit)) {
                return memory.multiply(new BigDecimal("1024")); // MB转KB
            } else if ("gb".equalsIgnoreCase(unit) || "g".equalsIgnoreCase(unit)) {
                return memory.multiply(new BigDecimal("1048576")); // GB转KB
            } else if ("kb".equalsIgnoreCase(unit) || "k".equalsIgnoreCase(unit)) {
                return memory; // 已经是KB
            } else if ("b".equalsIgnoreCase(unit)) {
                return memory.divide(new BigDecimal("1024")); // B转KB
            } else {
                // 默认认为是MB
                return memory.multiply(new BigDecimal("1024"));
            }
        } catch (NumberFormatException e) {
            return new BigDecimal("65536"); // 默认64MB
        }
    }

    /**
     * 获取元素文本
     */
    private String getElementText(Element parent, String childName) {
        Element child = parent.element(childName);
        return child != null ? child.getText() : "";
    }

    /**
     * 添加描述部分
     */
    private void appendDescriptionSection(StringBuilder builder, String sectionName, String content) {
        if (StringUtils.hasText(content)) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append("【").append(sectionName).append("】\n");
            builder.append(content);
        }
    }
}