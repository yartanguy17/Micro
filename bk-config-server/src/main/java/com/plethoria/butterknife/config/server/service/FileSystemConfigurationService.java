package com.plethoria.butterknife.config.server.service;


import com.plethoria.butterknife.config.server.exception.ConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * The type File system configuration service.
 */
@Service
@Qualifier(value = "fileSystemConfigurationService")
@RefreshScope
@Slf4j
public class FileSystemConfigurationService implements ConfigurationService {

  private static final String RUNTIME = "-runtime";
  private final String propertiesFolderPath;
  private final String brandingFilesFolder;
  private final FileConfigurationService fileConfigurationService;

  /**
   * Instantiates a new File system configuration service.
   *
   * @param propertiesFolderPath     the properties folder path
   * @param brandingFilesFolder      the branding files folder
   * @param fileConfigurationService the file configuration service
   */
  public FileSystemConfigurationService(
      @Value("${properties.folder.path}") String propertiesFolderPath,
      @Value("${branding.files.folder.path}") String brandingFilesFolder,
      FileConfigurationService fileConfigurationService) {
    this.propertiesFolderPath = propertiesFolderPath;
    this.brandingFilesFolder = brandingFilesFolder;
    this.fileConfigurationService = fileConfigurationService;
  }

  /**
   * Update properties.
   *
   * @param properties      the properties
   * @param applicationName the application name
   * @throws ConfigurationException the configuration exception
   */
  @Override
  public synchronized void updateProperties(Map<String, Object> properties, String applicationName)
      throws ConfigurationException {
    String configurationFilePath = getRuntimeConfigurationFilePath(applicationName);

    FileSystemResource yamlResource = loadYamlSystemResource(configurationFilePath);

    DumperOptions options = buildDumperOptions();

    Yaml yaml = new Yaml(options);
    try (InputStreamReader configStreamReader = new InputStreamReader(yamlResource.getInputStream(),
        StandardCharsets.UTF_8)) {
      Map<String, Object> configMap = yaml.load(configStreamReader);
      if (configMap == null) {
        configMap = new LinkedHashMap<>();
      }

      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        configMap.put(entry.getKey(), entry.getValue());
      }

      try (FileWriter fw = new FileWriter(yamlResource.getFile())) {
        yaml.dump(configMap, fw);
      }
    } catch (IOException e) {
      log.warn("Failed to read configuration from path [{}]", configurationFilePath);
      throw new ConfigurationException("Failed to read configuration.", e);
    }
  }

  /**
   * Remove properties.
   *
   * @param properties      the properties
   * @param applicationName the application name
   * @throws ConfigurationException the configuration exception
   */
  @Override
  public synchronized void removeProperties(List<String> properties, String applicationName)
      throws ConfigurationException {
    String configurationFilePath = getRuntimeConfigurationFilePath(applicationName);

    FileSystemResource yamlResource = loadYamlSystemResource(configurationFilePath);

    DumperOptions options = buildDumperOptions();

    Yaml yaml = new Yaml(options);

    try (InputStreamReader configStreamReader = new InputStreamReader(yamlResource.getInputStream(),
        StandardCharsets.UTF_8)) {
      Map<String, Object> configMap = yaml.load(configStreamReader);
      if (configMap == null) {
        configMap = new LinkedHashMap<>();
      }

      for (String property : properties) {
        configMap.remove(property);
      }

      try (FileWriter fw = new FileWriter(yamlResource.getFile())) {
        yaml.dump(configMap, fw);
      }
    } catch (IOException e) {
      log.warn("Failed to read configuration from path [{}]", configurationFilePath);
      throw new ConfigurationException("Failed to read configuration.", e);
    }
  }

  /**
   * Reset properties for file with name 'applicationName'
   *
   * @param applicationName - ex. 'cases-en', without the file extension (.yaml)
   * @throws ConfigurationException the configuration exception
   * @throws NoSuchFileException    the no such file exception
   */
  @Override
  public void resetFilePropertiesToDefault(String applicationName)
      throws ConfigurationException, NoSuchFileException {
    String resetFilePath;
    if (!applicationName.contains(FileSystemConfigurationService.RUNTIME)) {
      resetFilePath = String.format("%s/%s%s.yaml", propertiesFolderPath, applicationName, RUNTIME);
    } else {
      resetFilePath = String.format("%s/%s.yaml", propertiesFolderPath, applicationName);
    }

    String[] fileNameHelper = resetFilePath.split("/");
    String fileName = fileNameHelper[fileNameHelper.length - 1];

    log.info("Deleting file [{}]", fileName);

    File fileToBeDeleted = new File(resetFilePath);
    if (!fileToBeDeleted.exists()) {
      log.warn("File [{}] does not exists, nothing to delete.", fileName);
      throw new NoSuchFileException(
          String.format("File %s does not exists, nothing to delete.", fileName));
    } else if (!fileToBeDeleted.delete()) {
      throw new ConfigurationException(String.format("File %s could not be deleted", fileName));
    }
  }

  /**
   * Reset configuration branding files to default.
   *
   * @throws ConfigurationException the configuration exception
   */
  @Override
  public void resetConfigurationBrandingFilesToDefault() throws ConfigurationException {
    List<File> fileList = listAllRuntimeFilesInFolderAndSubfolders(brandingFilesFolder);

    for (File file : fileList) {
      if (file.getName().contains(FileSystemConfigurationService.RUNTIME)) {
        if (file.delete()) {
          log.info("Reset file [{}] to default version.", file.getName());
          String originalFileName = file.getName()
              .replace(FileSystemConfigurationService.RUNTIME, "");
          fileConfigurationService.sendNotification(originalFileName,
              FileConfigurationService.VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);

        } else {
          throw new ConfigurationException(
              String.format("File %s could not be fetched", file.getName()));
        }
      }
    }

  }

  /**
   * Reset properties to default.
   *
   * @throws ConfigurationException the configuration exception
   */
  @Override
  public void resetPropertiesToDefault() throws ConfigurationException {
    List<File> fileList = listAllRuntimeFilesInFolderAndSubfolders(propertiesFolderPath);
    for (File file : fileList) {
      if (file.getName().contains(FileSystemConfigurationService.RUNTIME)) {
        log.info("Deleting file [{}]", file.getName());
        if (!file.delete()) {
          throw new ConfigurationException(
              String.format("File %s could not be deleted", file.getName()));
        }
      }
    }
  }

  private List<File> listAllRuntimeFilesInFolderAndSubfolders(String directoryName) {
    File directory = new File(directoryName);

    List<File> resultList = new ArrayList<>();

    File[] fList = directory.listFiles();
    for (File file : fList) {
      if (file.isFile() && file.getName().contains(FileSystemConfigurationService.RUNTIME)) {
        resultList.add(file);
      } else if (file.isDirectory()) {
        resultList.addAll(listAllRuntimeFilesInFolderAndSubfolders(file.getAbsolutePath()));
      }
    }
    return resultList;
  }

  private FileSystemResource loadYamlSystemResource(String configurationFilePath)
      throws ConfigurationException {
    FileSystemResource yamlResource = new FileSystemResource(configurationFilePath);
    if (!yamlResource.getFile().exists()) {
      File file = new File(yamlResource.getPath());
      try {
        file.createNewFile();
      } catch (IOException e) {
        log.warn("Failed to create file to path [{}]", yamlResource.getPath());
        throw new ConfigurationException(e);
      }
    }
    return yamlResource;
  }

  private String getRuntimeConfigurationFilePath(String applicationName) {
    return String.format("%s/%s%s.yaml", propertiesFolderPath, applicationName, RUNTIME);
  }

  private DumperOptions buildDumperOptions() {
    DumperOptions options = new DumperOptions();
    options.setSplitLines(false);
    options.setMaxSimpleKeyLength(1024);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
    return options;
  }
}