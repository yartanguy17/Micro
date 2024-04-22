package com.plethoria.butterknife.config.server.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The type File watch service.
 */
@Service
@RefreshScope
@Slf4j
public class FileWatchService {

  private final String propertiesFolderPath;

  /**
   * Instantiates a new File watch service.
   *
   * @param propertiesFolderPath the properties folder path
   */
  public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath) {
    this.propertiesFolderPath = propertiesFolderPath;
    log.debug("Initializing FileWatchService");
  }

  /**
   * Monitor.
   */
  @Async
  public void monitor() {
    try {
      WatchService watchService = FileSystems.getDefault().newWatchService();
      Path path = Paths.get(propertiesFolderPath);
      path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

      Path labelsPath = Paths.get(propertiesFolderPath + "/labels");
      labelsPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

      Path ldapPath = Paths.get(propertiesFolderPath + "/ldap");
      ldapPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

      Path lookupsPath = Paths.get(propertiesFolderPath + "/lookups");
      lookupsPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

      Path rulesPath = Paths.get(propertiesFolderPath + "/rules");
      rulesPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

      WatchKey key;
      while (true) {
        try {
          log.debug("Waiting for file change on path [{}]", path.toString());
          if ((key = watchService.take()) != null) {
            log.debug("Watch key event present...");
            for (WatchEvent<?> event : key.pollEvents()) {
              Path filePath = (Path) event.context();
              File modifiedFile = filePath.toFile();
              log.info("Configuration file [{}] in folder [{}] has been updated!", modifiedFile,
                  propertiesFolderPath);
              String parentDirectory = key.watchable().toString();

              if (parentDirectory.contains("ldap")) {
                log.info("Ldap configuration updated");
              } else if (parentDirectory.contains("labels")) {
                log.info("Labels configuration updated");
              } else if (parentDirectory.contains("rules")) {
                log.info("Rules configuration updated");
              } else if (parentDirectory.contains("lookups")) {
                log.info("Lookups configuration updated");
              } else {
                log.info("Configuration updated");
              }
            }
            key.reset();
            log.debug("Reset watch key...");
          }
        } catch (Exception e) {
          log.error("Monitoring folder [{}] failed. {}", propertiesFolderPath, e.getMessage());
          log.trace("Cause: ", e);
        }
      }
    } catch (IOException e) {
      log.error("Error trying to find folder path [{}]. {}", propertiesFolderPath,
          e.getMessage());
    }
  }
}