// ProblemImportData.java
package io.charlie.web.oj.modular.data.problem.importdata;

import io.charlie.web.oj.modular.data.problem.entity.DataProblem;
import io.charlie.web.oj.modular.data.testcase.entity.DataTestCase;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProblemImportData {
    private DataProblem problem;
    private List<DataTestCase> testCases;
    private List<SolutionData> solutions;
    private List<TemplateData> templates;

    public ProblemImportData() {
        this.testCases = new ArrayList<>();
        this.solutions = new ArrayList<>();
        this.templates = new ArrayList<>();
    }
}


