
// TemplateData.java
package io.charlie.web.oj.modular.data.problem.importdata;

import lombok.Data;

@Data
public class TemplateData {
    private String type; // prepend, template, append
    private String language;
    private String code;
}