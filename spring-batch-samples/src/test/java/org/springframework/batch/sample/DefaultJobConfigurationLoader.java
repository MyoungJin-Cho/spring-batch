/**
 * 
 */
package org.springframework.batch.sample;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.configuration.JobConfiguration;
import org.springframework.batch.core.configuration.ListableJobConfigurationRegistry;
import org.springframework.batch.core.configuration.NoSuchJobConfigurationException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

public class DefaultJobConfigurationLoader implements JobConfigurationLoader,
		ApplicationContextAware {

	private ListableJobConfigurationRegistry registry;
	private ApplicationContext applicationContext;
	private Map configurations = new HashMap();

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setRegistry(ListableJobConfigurationRegistry registry) {
		this.registry = registry;
	}

	public Map getConfigurations() {
		Map result = new HashMap(configurations);
		for (Iterator iterator = registry.getJobConfigurations().iterator(); iterator
				.hasNext();) {
			JobConfiguration configuration = (JobConfiguration) iterator.next();
			String name = configuration.getName();
			if (!configurations.containsKey(name)) {
				result.put(name, "<unknown path>: " + configuration);
			}
		}
		return result;
	}

	public void loadResource(String path) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { path }, applicationContext);
		String[] names = context.getBeanNamesForType(JobConfiguration.class);
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			configurations.put(name, path);
		}
	}
	
	public Object getJobConfiguration(String name) {
		try {
			return registry.getJobConfiguration(name);
		} catch (NoSuchJobConfigurationException e) {
			return null;
		}
	}

	public Object getProperty(String path) {
		int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(path);
		BeanWrapperImpl wrapper = createBeanWrapper(path, index);
		String key = path.substring(index+1);
		return wrapper.getPropertyValue(key);
	}

	public void setProperty(String path, String value) {
		int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(path);
		BeanWrapperImpl wrapper = createBeanWrapper(path, index);
		String key = path.substring(index+1);
		wrapper.setPropertyValue(key, value);
	}

	private BeanWrapperImpl createBeanWrapper(String path, int index) {
		Assert.state(index>0, "Path must be nested, e.g. bean.value");
		String name = path.substring(0,index);
		Object bean = getJobConfiguration(name);
		Assert.notNull(bean, "No JobConfiguration exists with name="+name);
		BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
		return wrapper;
	}

}
