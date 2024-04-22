package com.plethoria.butterknife.config.server.api;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Modules list api controller.
 */
@RestController
@RequestMapping("/config")
@RefreshScope
@Slf4j
public class ModulesListAPIController {

  private final List<String> langs;

  private final String labelsFolderPath;

  /**
   * Instantiates a new Modules list api controller.
   *
   * @param propertiesFolderPath the properties folder path
   * @param langs                the langs
   */
  public ModulesListAPIController(@Value("${properties.folder.path}") String propertiesFolderPath,
      @Value("${plethoria.languages}") String langs) {
    this.labelsFolderPath = propertiesFolderPath + "/labels";
    this.langs = Arrays.asList(langs.split(","));
  }

  /**
   * Gets modules.
   *
   * @return the modules
   */
  @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> getModules() {
    return getModulesNames();
  }

  /**
   * Return list of modules configuration
   *
   * @return modules names
   */
  public List<String> getModulesNames() {
    File modulesDir = new File(labelsFolderPath);

    File[] files = modulesDir.listFiles(file -> {
      return file.isFile() && !file.getName().toLowerCase().contains("-runtime");
    });

    List<String> modules = new ArrayList<>();

    for (File labelResource : files) {
      for (String lang : langs) {
        String fileName = labelResource.getName();
        if (fileName.contains(lang)) {
          int sepPos = fileName.indexOf(lang);
          String moduleName = fileName.substring(0, sepPos);
          if (!modules.stream().anyMatch(module -> module.equals(moduleName))) {
            modules.add(moduleName);
          }
        }
      }
    }

    log.info("Returns modules names. [{}]", modules.toArray());
    return modules;
  }
}