package com.plethoria.butterknife.config.client.api;

import com.plethoria.butterknife.config.client.RefreshApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The type Refresh controller.
 */
@Controller
public class RefreshController {

  @Autowired
  private RefreshApplicationContext refreshAppplicationContext;

  @Autowired
  private RefreshScope refreshScope;

  /**
   * Refresh string.
   *
   * @return the string
   */
  @RequestMapping(path = "/refreshall", method = RequestMethod.GET)
  public String refresh() {
    refreshScope.refreshAll();
    refreshAppplicationContext.refreshctx();
    return "Refreshed";
  }
}
