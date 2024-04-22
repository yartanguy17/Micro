package com.plethoria.butterknife.config.server.api;

import com.plethoria.butterknife.config.server.service.FileConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * The type File configuration api controller.
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileConfigurationApiController {

  /**
   * The File configuration service.
   */
  @Autowired
  FileConfigurationService fileConfigurationService;

  /**
   * Move file to configuration response entity.
   *
   * @param file     the file
   * @param fileName the file name
   * @return the response entity
   * @throws Exception the exception
   */
  @PostMapping()
  public ResponseEntity moveFileToConfiguration(@RequestParam("file") MultipartFile file,
      @RequestParam("fileName") String fileName) throws Exception {

    log.info("branding file is received on config server! [{}]", fileName);

    fileConfigurationService.moveFileToConfiguration(file, fileName);

    log.info("file is moved to config server!");

    return ResponseEntity.ok().build();
  }

}