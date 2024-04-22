package com.plethoria.butterknife.config.client.configurations;

import static org.springframework.cloud.config.client.ConfigClientProperties.AUTHORIZATION;
import static org.springframework.cloud.config.client.ConfigClientProperties.STATE_HEADER;
import static org.springframework.cloud.config.client.ConfigClientProperties.TOKEN_HEADER;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.bootstrap.support.OriginTrackedCompositePropertySource;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigClientStateHolder;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator.GenericRequestHeaderInterceptor;
import org.springframework.cloud.configuration.SSLContextFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * The type Custom service property source locator.
 */
@Order(0)
@Slf4j
public class CustomServicePropertySourceLocator implements PropertySourceLocator {

  private final ConfigClientProperties defaultProperties;
  private RestTemplate restTemplate;

  /**
   * Instantiates a new Custom service property source locator.
   *
   * @param defaultProperties the default properties
   */
  public CustomServicePropertySourceLocator(ConfigClientProperties defaultProperties) {
    this.defaultProperties = defaultProperties;
  }

  /**
   * Locate property source.
   *
   * @param environment the environment
   * @return the property source
   */
  @Override
  public PropertySource<?> locate(Environment environment) {
    ConfigClientProperties properties = this.defaultProperties;
    CompositePropertySource composite = new OriginTrackedCompositePropertySource("configService");
    RestTemplate restTemplate =
        this.restTemplate == null ? getSecureRestTemplate(properties) : this.restTemplate;
    Exception error = null;
    String errorBody = null;
    try {
      String[] labels = new String[]{""};
      if (StringUtils.hasText(properties.getLabel())) {
        labels = StringUtils.commaDelimitedListToStringArray(properties.getLabel());
      }
      String state = ConfigClientStateHolder.getState();
      // Try all the labels until one works
      for (String label : labels) {
        org.springframework.cloud.config.environment.Environment result = getRemoteEnvironment(
            restTemplate, properties, label.trim(), state);
        if (result != null) {
          log(result);

          // result.getPropertySources() can be null if using xml
          if (result.getPropertySources() != null) {
            for (org.springframework.cloud.config.environment.PropertySource source : result.getPropertySources()) {
              @SuppressWarnings("unchecked") Map<String, Object> map = translateOrigins(
                  source.getName(), (Map<String, Object>) source.getSource());
              composite.addPropertySource(
                  new OriginTrackedMapPropertySource(source.getName(), map));
            }
          }

          HashMap<String, Object> map = new HashMap<>();
          if (StringUtils.hasText(result.getState())) {
            putValue(map, "config.client.state", result.getState());
          }
          if (StringUtils.hasText(result.getVersion())) {
            putValue(map, "config.client.version", result.getVersion());
          }
          // the existence of this property source confirms a successful
          // response from config server
          composite.addFirstPropertySource(new MapPropertySource("configClient", map));
          return composite;
        }
      }
      errorBody = String.format("None of labels %s found", Arrays.toString(labels));
    } catch (HttpServerErrorException e) {
      error = e;
      if (MediaType.APPLICATION_JSON.includes(e.getResponseHeaders().getContentType())) {
        errorBody = e.getResponseBodyAsString();
      }
    } catch (Exception e) {
      error = e;
    }
    if (properties.isFailFast()) {
      throw new IllegalStateException(
          "Could not locate PropertySource and the fail fast property is set, failing" + (
              errorBody == null ? "" : ": " + errorBody), error);
    }
    log.warn(
        "Could not locate PropertySource: " + (error != null ? error.getMessage() : errorBody));
    return null;

  }

  private void log(org.springframework.cloud.config.environment.Environment result) {
    if (log.isInfoEnabled()) {
      log.info(
          String.format("Located environment: name=%s, profiles=%s, label=%s, version=%s, state=%s",
              result.getName(),
              result.getProfiles() == null ? "" : Arrays.asList(result.getProfiles()),
              result.getLabel(), result.getVersion(), result.getState()));
    }
    if (log.isDebugEnabled()) {
      List<org.springframework.cloud.config.environment.PropertySource> propertySourceList = result.getPropertySources();
      if (propertySourceList != null) {
        int propertyCount = 0;
        for (org.springframework.cloud.config.environment.PropertySource propertySource : propertySourceList) {
          propertyCount += propertySource.getSource().size();
        }
        log.debug(String.format("Environment %s has %d property sources with %d properties.",
            result.getName(), result.getPropertySources().size(), propertyCount));
      }

    }
  }

  private RestTemplate getSecureRestTemplate(ConfigClientProperties client) {
    if (client.getRequestReadTimeout() < 0) {
      throw new IllegalStateException("Invalid Value for Read Timeout set.");
    }
    if (client.getRequestConnectTimeout() < 0) {
      throw new IllegalStateException("Invalid Value for Connect Timeout set.");
    }

    ClientHttpRequestFactory requestFactory = createHttpRquestFactory(client);
    RestTemplate template = new RestTemplate(requestFactory);
    Map<String, String> headers = new HashMap<>(client.getHeaders());
    headers.remove(AUTHORIZATION); // To avoid redundant addition of header
    if (!headers.isEmpty()) {
      template.setInterceptors(Collections.singletonList(
          new GenericRequestHeaderInterceptor(headers)));
    }

    return template;
  }

  private ClientHttpRequestFactory createHttpRquestFactory(ConfigClientProperties client) {
    if (client.getTls().isEnabled()) {
      try {
        SSLContextFactory factory = new SSLContextFactory(client.getTls());
        SSLContext sslContext = factory.createSSLContext();
        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        HttpComponentsClientHttpRequestFactory result = new HttpComponentsClientHttpRequestFactory(
            httpClient);

        result.setReadTimeout(client.getRequestReadTimeout());
        result.setConnectTimeout(client.getRequestConnectTimeout());
        return result;

      } catch (GeneralSecurityException | IOException ex) {
        log.error("An error occured! {}", ex.toString());
        throw new IllegalStateException("Failed to create config client with TLS.", ex);
      }
    }

    SimpleClientHttpRequestFactory result = new SimpleClientHttpRequestFactory();
    result.setReadTimeout(client.getRequestReadTimeout());
    result.setConnectTimeout(client.getRequestConnectTimeout());
    return result;
  }

  private void putValue(HashMap<String, Object> map, String key, String value) {
    if (StringUtils.hasText(value)) {
      map.put(key, value);
    }
  }

  private org.springframework.cloud.config.environment.Environment getRemoteEnvironment(
      RestTemplate restTemplate, ConfigClientProperties properties, String label, String state) {
    String path = "/{name}/{profile}";
    String name = properties.getName();
    String profile = properties.getProfile();
    String token = properties.getToken();
    int noOfUrls = properties.getUri().length;
    if (noOfUrls > 1) {
      log.info("Multiple Config Server Urls found listed.");
    }

    Object[] args = new String[]{name, profile};
    if (StringUtils.hasText(label)) {
      // workaround for Spring MVC matching / in paths
      label = org.springframework.cloud.config.environment.Environment.denormalize(label);
      args = new String[]{name, profile, label};
      path = path + "/{label}";
    }
    ResponseEntity<org.springframework.cloud.config.environment.Environment> response = null;
    List<MediaType> acceptHeader = Collections.singletonList(
        MediaType.parseMediaType(properties.getMediaType()));

    for (int i = 0; i < noOfUrls; i++) {
      ConfigClientProperties.Credentials credentials = properties.getCredentials(i);
      String uri = credentials.getUri();
      String username = credentials.getUsername();
      String password = credentials.getPassword();

      log.info("Fetching config from server at : " + uri);

      try {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptHeader);
        addAuthorizationToken(properties, headers, username, password);
        if (StringUtils.hasText(token)) {
          headers.add(TOKEN_HEADER, token);
        }
        if (StringUtils.hasText(state) && properties.isSendState()) {
          headers.add(STATE_HEADER, state);
        }

        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        response = restTemplate.exchange(uri + path, HttpMethod.GET, entity,
            org.springframework.cloud.config.environment.Environment.class, args);
      } catch (HttpClientErrorException e) {
        if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
          throw e;
        }
      } catch (ResourceAccessException e) {
        log.info("Connect Timeout Exception on Url - " + uri
            + ". Will be trying the next url if available");
        if (i == noOfUrls - 1) {
          throw e;
        } else {
          continue;
        }
      }

      if (response == null || response.getStatusCode() != HttpStatus.OK) {
        return null;
      }

      org.springframework.cloud.config.environment.Environment result = response.getBody();
      return result;
    }

    return null;
  }

  private Map<String, Object> translateOrigins(String name, Map<String, Object> source) {
    Map<String, Object> withOrigins = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      boolean hasOrigin = false;

      if (entry.getValue() instanceof Map) {
        @SuppressWarnings("unchecked") Map<String, Object> value = (Map<String, Object>) entry.getValue();
        if (value.size() == 2 && value.containsKey("origin") && value.containsKey("value")) {
          Origin origin = new ConfigServiceOrigin(name, value.get("origin"));
          OriginTrackedValue trackedValue = OriginTrackedValue.of(value.get("value"), origin);
          withOrigins.put(entry.getKey(), trackedValue);
          hasOrigin = true;
        }
      }

      if (!hasOrigin) {
        withOrigins.put(entry.getKey(), entry.getValue());
      }
    }
    return withOrigins;
  }

  private void addAuthorizationToken(ConfigClientProperties configClientProperties,
      HttpHeaders httpHeaders, String username, String password) {
    String authorization = configClientProperties.getHeaders().get(AUTHORIZATION);

    if (password != null && authorization != null) {
      throw new IllegalStateException("You must set either 'password' or 'authorization'");
    }

    if (password != null) {
      byte[] token = Base64Utils.encode((username + ":" + password).getBytes());
      httpHeaders.add("Authorization", "Basic " + new String(token));
    } else if (authorization != null) {
      httpHeaders.add("Authorization", authorization);
    }

  }

  /**
   * The type Config service origin.
   */
  static class ConfigServiceOrigin implements Origin {

    private final String remotePropertySource;

    private final Object origin;

    /**
     * Instantiates a new Config service origin.
     *
     * @param remotePropertySource the remote property source
     * @param origin               the origin
     */
    ConfigServiceOrigin(String remotePropertySource, Object origin) {
      this.remotePropertySource = remotePropertySource;
      Assert.notNull(origin, "origin may not be null");
      this.origin = origin;

    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
      return "Config Server " + this.remotePropertySource + ":" + this.origin.toString();
    }

  }


}