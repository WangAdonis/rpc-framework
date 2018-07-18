package cn.adonis.rpc.spring;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    private static final Logger logger = LoggerFactory.getLogger(ProviderFactoryBeanDefinitionParser.class);

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ProviderFactoryBean.class;
    }

    protected void doParser(Element element, BeanDefinitionBuilder builder) {
        try {
            String serviceInterface = element.getAttribute("interface");
            String timeout = element.getAttribute("timeout");
            String serverPort = element.getAttribute("serverPort");
            String ref = element.getAttribute("ref");
            String weight = element.getAttribute("weight");
            String workerThreads = element.getAttribute("workerThreads");
            String appKey = element.getAttribute("appKey");
            String groupName = element.getAttribute("groupName");

            builder.addPropertyValue("serverPort", Integer.parseInt(serverPort));
            builder.addPropertyValue("timeout", Integer.parseInt(timeout));
            builder.addPropertyValue("serviceInterface", Class.forName(serviceInterface));
            builder.addPropertyReference("serviceImpl", ref);
            builder.addPropertyValue("appKey", appKey);
            if (StringUtils.isNotBlank(weight)) {
                builder.addPropertyValue("weight", Integer.parseInt(weight));
            }
            if (StringUtils.isNotBlank(workerThreads)) {
                builder.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
            }
            if (StringUtils.isNotBlank(groupName)) {
                builder.addPropertyValue("groupName", groupName);
            }
        } catch (Exception e) {
            logger.error("ProviderFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }
    }
}
