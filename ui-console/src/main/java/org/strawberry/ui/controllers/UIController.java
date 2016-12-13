//package org.strawberry.ui.controllers;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.tools.ant.Project;
//import org.apache.tools.ant.taskdefs.Zip;
//import org.springframework.web.client.RestTemplate;
//import org.strawberry.ui.controllers.model.EventStreamConfig;
//
//import javax.faces.application.FacesMessage;
//import javax.faces.context.ExternalContext;
//import javax.faces.context.FacesContext;
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Created by saipkri on 08/12/16.
// */
//@Data
//public class UIController {
//
//    // TODO config.
//    private static final String configsEndpoint = "http://192.168.99.100:9999/config";
//    private static final String dashboardEndpoint = "http://192.168.99.100:9999/ops-dashboard-link";
//    private List<EventStreamConfig> configs = new ArrayList<>();
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper om = new ObjectMapper();
//    private String dashboard;
//    private String groupId;
//    private String artifactId;
//    private String version = "1.0.0-SNAPSHOT";
//    private String eventName = "my_event";
//    private String description = "This app processes the events " + eventName;
//    private String configUrl = "http://192.168.99.100:8888";
//
//    public UIController() {
//        configs.clear();
//        List _configs = restTemplate.getForObject(configsEndpoint, List.class);
//        _configs.forEach(res -> {
//            com.sai.strawberry.api.EventStreamConfig t = om.convertValue(res, com.sai.strawberry.api.EventStreamConfig.class);
//            configs.add(new EventStreamConfig(t));
//        });
//        dashboard = restTemplate.getForObject(dashboardEndpoint, String.class);
//    }
//
    //    public void newApp() throws Exception {
    //        System.out.println(" ------ New App --------- " + groupId);
    //        String pomContents = IOUtils.toString(UIController.class.getClassLoader().getResourceAsStream("app-templates/pom.xml"));
    //        pomContents = pomContents.replace("__gid", groupId).replace("__aid", artifactId).replace("__version", version).replace("__desc", description).replace("__ename", eventName);
    //        String eventReceiverContents = IOUtils.toString(UIController.class.getClassLoader().getResourceAsStream("app-templates/EventTransformer.java"));
    //        eventReceiverContents = eventReceiverContents.replace("_gid", groupId);
    //        String templateProps = IOUtils.toString(UIController.class.getClassLoader().getResourceAsStream("app-templates/template.properties"));
    //        templateProps = templateProps.replace("__eid", eventName);
    //        String configTemplateJson = IOUtils.toString(UIController.class.getClassLoader().getResourceAsStream("app-templates/config_template.json"));
    //        configTemplateJson = configTemplateJson.replace("__eid", eventName).replace("__gid", groupId);
    //
    //        File temp = new File(UUID.randomUUID().toString());
    //
    //        Path javaSrc = Paths.get(temp.getName(), eventName, "src", "main", "java");
    //
    //        Files.createDirectories(javaSrc);
    //
    //        String dockerFileContents = IOUtils.toString(UIController.class.getClassLoader().getResourceAsStream("app-templates/Dockerfile"));
    //        dockerFileContents = dockerFileContents.replace("__ename", eventName);
    //
    //
    //        if (StringUtils.isNotBlank(groupId)) {
    //            String pkg = javaSrc.toFile().getAbsolutePath() + File.separator + groupId.replace(".", File.separator);
    //            File pkgDir = new File(pkg);
    //            FileUtils.forceMkdir(pkgDir);
    //            FileUtils.writeStringToFile(new File(pkgDir.getAbsolutePath() + File.separator + "EventTransformer.java"), eventReceiverContents);
    //            FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + eventName + ".json"), configTemplateJson);
    //            FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + eventName + ".properties"), templateProps);
    //        }
    //        FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "pom.xml"), pomContents);
    //
    //        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "main", "resources"));
    //        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "main", "docker"));
    //        Files.createDirectories(Paths.get(temp.getName(), eventName, "src", "test", "resources"));
    //        FileUtils.writeStringToFile(new File(temp.getAbsolutePath() + File.separator + eventName + File.separator + "src" + File.separator + "main" + File.separator + "docker" + File.separator + "Dockerfile"), dockerFileContents);
    //
    //
    //        Project p = new Project();
    //        p.init();
    //        Zip zip = new Zip();
    //        zip.setProject(p);
    //        File zipFile = new File(temp.getAbsolutePath() + File.separator + eventName + ".zip");
    //        zip.setDestFile(zipFile);
    //        zip.setBasedir(new File(temp.getAbsolutePath() + File.separator + eventName));
    //        zip.setIncludes("**/*");
    //        zip.perform();
    //
    //        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    //        externalContext.setResponseContentType("application/zip");
    //        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + eventName + ".zip\"");
    //        externalContext.setResponseHeader("Content-Length", String.valueOf(zipFile.length()));
    //
    //        Files.copy(zipFile.toPath(), externalContext.getResponseOutputStream());
    //        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "App template ready!"));
    //
    //        FacesContext.getCurrentInstance().responseComplete();
    //    }
//}
