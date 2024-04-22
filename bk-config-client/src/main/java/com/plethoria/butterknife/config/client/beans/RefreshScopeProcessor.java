package com.plethoria.butterknife.config.client.beans;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * The type Refresh scope processor.
 */
@Component
@Slf4j
public class RefreshScopeProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  /**
   * Get a Spring bean by type.
   *
   * @param <T>       the type parameter
   * @param beanClass the bean class
   * @return bean bean
   */
  public static <T> T getBean(Class<T> beanClass) {
    return applicationContext.getBean(beanClass);
  }

  /**
   * Get a Spring bean by name.
   *
   * @param beanName the bean name
   * @return bean bean
   */
  public static Object getBean(String beanName) {
    return applicationContext.getBean(beanName);
  }

  /**
   * Post process bean factory.
   *
   * @param beanFactory the bean factory
   * @throws BeansException the beans exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    if (!Arrays.asList(beanFactory.getRegisteredScopeNames()).contains("refresh")) {
      beanFactory.registerScope("refresh", new RefreshScope());
    }
    String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (String beanName : Arrays.stream(beanNames).sorted().collect(Collectors.toList())) {
      log.trace("Post Processing Bean {}", beanName);
      BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
      beanDef.setLazyInit(true);
      beanDef.setScope("refresh");
    }
  }

  /**
   * Sets application context.
   *
   * @param context the context
   * @throws BeansException the beans exception
   */
  @Override
  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    applicationContext = context;
  }
}