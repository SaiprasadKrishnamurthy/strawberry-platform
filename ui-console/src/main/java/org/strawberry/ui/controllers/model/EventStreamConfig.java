//package org.strawberry.ui.controllers.model;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.sai.strawberry.api.BatchQueryConfig;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static java.util.stream.Collectors.toList;
//
///**
// * Created by saipkri on 08/12/16.
// */
//@Data
//public class EventStreamConfig {
//
//    @AllArgsConstructor
//    @Data
//    public static final class NotificationRule {
//        private final String name;
//        private final String value;
//
//    }
//
//    private final ObjectMapper o = new ObjectMapper();
//
//    private final com.sai.strawberry.api.EventStreamConfig config;
//    private List<NotificationRule> esNotificationQueries;
//    private Map<String, String> sqlNotificationQueries;
//
//    public EventStreamConfig(final com.sai.strawberry.api.EventStreamConfig config) {
//        this.config = config;
//        o.enable(SerializationFeature.INDENT_OUTPUT);
//
//    }
//
//    public String getConfigId() {
//        return getConfig().getConfigId();
//    }
//
//    public boolean isPersistEvent() {
//        return getConfig().isPersistEvent();
//    }
//
//    public void setInternal(Map<String, Object> internal) {
//        getConfig().setInternal(internal);
//    }
//
//    public String getCustomProcessingHookScript() {
//        return getConfig().getCustomProcessingHookScript();
//    }
//
//    public String getDocumentIdField() {
//        return getConfig().getDocumentIdField();
//    }
//
//    public void setConfigId(String configId) {
//        getConfig().setConfigId(configId);
//    }
//
//    public Map<String, Object> getInternal() {
//        return getConfig().getInternal();
//    }
//
//    public boolean isEnabled() {
//        return getConfig().isEnabled();
//    }
//
//    public void setDocumentIdField(String documentIdField) {
//        getConfig().setDocumentIdField(documentIdField);
//    }
//
//    public boolean isEnableVisualization() {
//        return getConfig().isEnableVisualization();
//    }
//
//    public void setWatchQueriesSql(Map<String, String> watchQueriesSql) {
//        getConfig().setWatchQueriesSql(watchQueriesSql);
//    }
//
//    public String getCustomProcessingHookClassName() {
//        return getConfig().getCustomProcessingHookClassName();
//    }
//
//    public Map<String, String> getWatchQueriesSql() {
//        return getConfig().getWatchQueriesSql();
//    }
//
//    public BatchQueryConfig getBatchQueryConfig() {
//        return getConfig().getBatchQueryConfig();
//    }
//
//    public void setEnableVisualization(boolean enableVisualization) {
//        getConfig().setEnableVisualization(enableVisualization);
//    }
//
//    public void setSqlDDL(String sqlDDL) {
//        getConfig().setSqlDDL(sqlDDL);
//    }
//
//    public boolean isCacheEnabled() {
//        return getConfig().isCacheEnabled();
//    }
//
//    public void setEnabled(boolean enabled) {
//        getConfig().setEnabled(enabled);
//    }
//
//    public void setCustomProcessingHookScript(String customProcessingHookScript) {
//        getConfig().setCustomProcessingHookScript(customProcessingHookScript);
//    }
//
//    public String getSqlDDL() {
//        return getConfig().getSqlDDL();
//    }
//
//    public void setCustomProcessingHookClassName(String customProcessingHookClassName) {
//        getConfig().setCustomProcessingHookClassName(customProcessingHookClassName);
//    }
//
//    public Map<String, Map<String, Object>> getWatchQueries() {
//        return getConfig().getWatchQueries();
//    }
//
//    public List<NotificationRule> getEsNotificationQueries() {
//        Map<String, Map<String, Object>> wq = getWatchQueries();
//        if (wq != null) {
//            return esNotificationQueries = wq.entrySet().stream().map(entry -> {
//                try {
//                    return new NotificationRule(entry.getKey(), o.writeValueAsString(entry.getValue()));
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                    return new NotificationRule(entry.getKey(), "{}");
//                }
//            }).collect(toList());
//        } else {
//            return Collections.emptyList();
//        }
//    }
//
//    public List<NotificationRule> getSqlNotificationQueries() {
//        Map<String, String> wq = getWatchQueriesSql();
//        if (wq != null) {
//            return wq.entrySet().stream().map(entry -> new NotificationRule(entry.getKey(), entry.getValue())).collect(toList());
//        } else {
//            return Collections.emptyList();
//        }
//    }
//
//    public Map<String, Object> getIndexDefinition() {
//        return getConfig().getIndexDefinition();
//    }
//
//    public void setWatchQueries(Map<String, Map<String, Object>> watchQueries) {
//        getConfig().setWatchQueries(watchQueries);
//    }
//
//    public void setPersistEvent(boolean persistEvent) {
//        getConfig().setPersistEvent(persistEvent);
//    }
//
//    public void setDurableNotification(boolean durableNotification) {
//        getConfig().setDurableNotification(durableNotification);
//    }
//
//    public boolean isDurableNotification() {
//        return getConfig().isDurableNotification();
//    }
//
//    public void setIndexDefinition(Map<String, Object> indexDefinition) {
//        getConfig().setIndexDefinition(indexDefinition);
//    }
//
//    public void setCacheEnabled(boolean cacheEnabled) {
//        getConfig().setCacheEnabled(cacheEnabled);
//    }
//
//    public void setBatchQueryConfig(BatchQueryConfig batchQueryConfig) {
//        getConfig().setBatchQueryConfig(batchQueryConfig);
//    }
//
//    public String getIndexDefinitionJson() {
//        try {
//            return o.writeValueAsString(getIndexDefinition());
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return "{}";
//        }
//    }
//
//}
