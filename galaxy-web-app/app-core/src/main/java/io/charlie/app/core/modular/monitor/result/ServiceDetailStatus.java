package io.charlie.app.core.modular.monitor.result;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServiceDetailStatus {
    private String serviceName;
    private String groupName;
    private int clusterCount;
    private int instanceCount;
    private int healthyInstanceCount;
    private boolean protectionThresholdEnabled;
    private List<ClusterDetail> clusters;

    @Data
    public static class ClusterDetail {
        private String clusterName;
        private List<InstanceDetail> instances;
    }

    @Data
    public static class InstanceDetail {
        private String ip;
        private int port;
        private boolean ephemeral;
        private double weight;
        private boolean healthy;
        private Map<String, String> metadata;
    }
}

