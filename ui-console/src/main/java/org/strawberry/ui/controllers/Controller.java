package org.strawberry.ui.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sai.strawberry.api.EventConfig;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.web.client.RestTemplate;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by saipkri on 13/12/16.
 */
@Data
public class Controller {

    private TreeNode root;
    private EventConfig config;
    private static final String configsEndpoint = "http://192.168.99.100:9999/configs";
    private final RestTemplate restTemplate = new RestTemplate();
    private String dashboard;
    private static final String dashboardEndpoint = "http://192.168.99.100:9999/ops-dashboard-link";
    private String groupId;
    private String artifactId;
    private String version = "1.0.0-SNAPSHOT";
    private String eventName = "my_event";
    private String description = "This app processes the events " + eventName;
    private String configUrl = "http://192.168.99.100:8888";
    private String db = "mongo";
    private String pk = "id";
    private String notificationQuery = "es";
    private List<String> availableChannels = new ArrayList<>();
    private List<String> selectedChannels = new ArrayList<>();

    public Controller() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        List _configs = restTemplate.getForObject(configsEndpoint, List.class);
        dashboard = restTemplate.getForObject(dashboardEndpoint, String.class);

        config = (EventConfig) _configs.stream().map(res -> objectMapper.convertValue(res, EventConfig.class)).findFirst().get();
        dashboard = restTemplate.getForObject(dashboardEndpoint, String.class);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = new DefaultTreeNode("+ " + config.getConfigId(), null);
        root.setExpanded(true);

        TreeNode node0 = node("+ Data Definition", root);
        TreeNode node1 = node("+ Data Transformation", root);
        TreeNode node2 = node("Notification Rules", root);

        // Definition.
        TreeNode node01 = node("+ Elasticsearch", node0);
        node(objectMapper.writeValueAsString(config.getDataDefinitions().getElasticsearchIndexDefinition()), node01);

        if (config.getDataDefinitions().getDatabase() != null) {
            TreeNode node02 = node("+ Database", node0);
            if (config.getDataDefinitions().getDatabase().getMongo() != null) {
                TreeNode node021 = node("+ Mongo", node02);
                node(objectMapper.writeValueAsString(objectMapper.writeValueAsString(config.getDataDefinitions().getDatabase().getMongo())), node021);
            }
        }

        if (config.getDataTransformation() != null) {
            node("Class name: " + config.getDataTransformation().getDataTransformerHookClass(), node1);
        }

        if (config.getNotification() != null) {
            if (config.getNotification().getElasticsearch() != null) {
                TreeNode node20 = node("+ Elasticsearch", node2);
                config.getNotification().getElasticsearch().getNotificationChannelsAndQueries().entrySet().forEach(entry -> {
                    TreeNode node200 = node("+ " + entry.getKey(), node20);
                    try {
                        availableChannels.add(entry.getKey());
                        node(objectMapper.writeValueAsString(entry.getValue()), node200);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (config.getNotification().getSql() != null) {
                TreeNode node20 = node("+ SQL", node2);
                config.getNotification().getSql().getNotificationChannelsAndQueries().entrySet().forEach(entry -> {
                    TreeNode node200 = node("+ " + entry.getKey(), node20);
                    try {
                        node(objectMapper.writeValueAsString(entry.getValue()), node200);
                        availableChannels.add(entry.getKey());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
            }

        }

    }

    private TreeNode node(final String name, final TreeNode parent) {
        TreeNode node = new DefaultTreeNode(name, parent);
        node.setRowKey(UUID.randomUUID().toString());
        node.setExpanded(true);
        return node;
    }

    public void newApp() throws Exception {
        System.out.println(" ------ New App --------- " + groupId);
        String pomContents = IOUtils.toString(Controller.class.getClassLoader().getResourceAsStream("app-templates/pom.xml"));
        pomContents = pomContents.replace("__gid", groupId).replace("__aid", artifactId).replace("__version", version).replace("__desc", description).replace("__ename", eventName);
        String eventReceiverContents = IOUtils.toString(Controller.class.getClassLoader().getResourceAsStream("app-templates/EventTransformer.java"));
        eventReceiverContents = eventReceiverContents.replace("__gid", groupId);
        String templateProps = IOUtils.toString(Controller.class.getClassLoader().getResourceAsStream("app-templates/template.properties"));
        templateProps = templateProps.replace("__eid", eventName);
        String configTemplateJson = IOUtils.toString(Controller.class.getClassLoader().getResourceAsStream("app-templates/config_template.json"));
        configTemplateJson = configTemplateJson.replace("__eid", eventName).replace("__gid", groupId).replace("__pk", pk);

        File temp = new File(UUID.randomUUID().toString());

        Path javaSrc = Paths.get(temp.getName(), eventName, "src", "main", "java");

        Files.createDirectories(javaSrc);

        String dockerFileContents = IOUtils.toString(Controller.class.getClassLoader().getResourceAsStream("app-templates/Dockerfile"));
        dockerFileContents = dockerFileContents.replace("__ename", eventName);


        if (StringUtils.isNotBlank(groupId)) {
            String pkg = javaSrc.toFile().getAbsolutePath() + File.separator + groupId.replace(".", File.separator);
            File pkgDir = new File(pkg);
            FileUtils.forceMkdir(pkgDir);
            FileUtils.writeStringToFile(new File(pkgDir.getAbsolutePath() + File.separator + "EventTransformer.java"), eventReceiverContents);
            FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + eventName + ".json"), configTemplateJson);
            FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + eventName + ".properties"), templateProps);
        }
        FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "pom.xml"), pomContents);

        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "main", "resources"));
        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "main", "docker"));
        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "test", "resources"));
        FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "docker" + File.separator + "Dockerfile"), dockerFileContents);


        Project p = new Project();
        p.init();
        Zip zip = new Zip();
        zip.setProject(p);
        File zipFile = new File(temp.getAbsolutePath() + File.separator + eventName + ".zip");
        zip.setDestFile(zipFile);
        zip.setBasedir(new File(temp.getAbsolutePath() + File.separator + eventName));
        zip.setIncludes("**/*");
        zip.perform();

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.setResponseContentType("application/zip");
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + eventName + ".zip\"");
        externalContext.setResponseHeader("Content-Length", String.valueOf(zipFile.length()));

        Files.copy(zipFile.toPath(), externalContext.getResponseOutputStream());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "App template ready!"));

        FacesContext.getCurrentInstance().responseComplete();
    }

    public void listen() {
        System.out.println("Selected Channels: "+selectedChannels);
    }

    public void notificationCheck() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Successful",  "Your message: " + selectedChannels) );
    }

    public TreeNode getRoot() {
        return root;
    }
}
