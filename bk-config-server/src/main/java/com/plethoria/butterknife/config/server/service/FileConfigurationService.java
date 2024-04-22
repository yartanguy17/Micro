package com.plethoria.butterknife.config.server.service;


import java.io.File;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * The type File configuration service.
 */
@Service
@Qualifier(value = "fileConfigurationService")
@RefreshScope
@Slf4j
public class FileConfigurationService {

  /**
   * The constant VIRTUAL_TOPIC_CONFIG_FILE_UPDATED.
   */
  public static final String VIRTUAL_TOPIC_CONFIG_FILE_UPDATED = "VirtualTopic.ConfigFileUpdated";
  private final String configServerRepo;


  /**
   * Instantiates a new File configuration service.
   *
   * @param configRepo the config repo
   */
  public FileConfigurationService(@Value("${properties.folder.path}") String configRepo) {
    this.configServerRepo = configRepo;
  }

  /**
   * Move file to configuration.
   *
   * @param file     the file
   * @param fileName the file name
   * @throws Exception the exception
   */
  public void moveFileToConfiguration(MultipartFile file, String fileName) throws Exception {
    try (InputStream logoStream = file.getInputStream()) {

      String originalFileName = getOriginalFileNameFromFilePath(fileName);

      String profileBasedFile = setProfileBasedResource(fileName);

      File logoFile = new File(configServerRepo + "/" + profileBasedFile);

      FileUtils.copyInputStreamToFile(logoStream, logoFile);

      log.info("File is with name {} created on the config server", fileName);

      sendNotification(originalFileName,
          VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);

    } catch (Exception e) {
      throw new Exception("Can't update logo file");
    }
  }

  private String getOriginalFileNameFromFilePath(String filePath) {
    String[] splitedFilePath = filePath.split("/");
    String originalFileName = splitedFilePath[splitedFilePath.length - 1];

    log.debug("Original file name from path {} is {}", filePath, originalFileName);

    return originalFileName;
  }

  private String setProfileBasedResource(String fileName) {
    return new StringBuilder(fileName).insert(fileName.indexOf("."), "-" + "runtime").toString();
  }

  /**
   * Send notification.
   *
   * @param message     the message
   * @param destination the destination
   */
  public void sendNotification(String message, String destination) {
    log.debug("File with name {} is updated and success message is sent for updating", message);

  }

}