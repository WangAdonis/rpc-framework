package cn.adonis.rpc.spring;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;


public class ConsumerFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerFactoryBeanDefinitionParser.class);

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ConsumerFactoryBean.class;
    }

    protected void doParser(Element element, BeanDefinitionBuilder builder) {
        try {
            String timeout = element.getAttribute("timeout");
            String targetInterface = element.getAttribute("interface");
            String strategy = element.getAttribute("strategy");
            String remoteAppKey = element.getAttribute("remoteAppKey");
            String groupName = element.getAttribute("groupName");

            builder.addPropertyValue("timeout", Integer.parseInt(timeout));
            builder.addPropertyValue("targetInterface", Class.forName(targetInterface));
            builder.addPropertyValue("remoteAppKey", remoteAppKey);
            if (StringUtils.isNotBlank(strategy)) {
                builder.addPropertyValue("strategy", strategy);
            }
            if (StringUtils.isNotBlank(groupName)) {
                builder.addPropertyValue("groupName", groupName);
            }
        } catch (Exception e) {
            logger.error("ConsumerFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }
    }
}
