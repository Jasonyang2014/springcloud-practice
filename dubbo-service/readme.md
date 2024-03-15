## dubbo 

自动装配注册类
`org.apache.dubbo.config.spring.context.annotation.DubboConfigConfigurationRegistrar`
开始**dubbo**的上下文进行初始化，同时注册多个`BeanPostProcessor`对dubbo类进行后置处理。

```java
private static void initContext(
            DubboSpringInitContext context,
            BeanDefinitionRegistry registry,
            ConfigurableListableBeanFactory beanFactory) {
        context.setRegistry(registry);
        context.setBeanFactory(beanFactory);

        // customize context, you can change the bind module model via DubboSpringInitCustomizer SPI
        customize(context);

        // init ModuleModel
        ModuleModel moduleModel = context.getModuleModel();
        if (moduleModel == null) {
            ApplicationModel applicationModel;
            if (findContextForApplication(ApplicationModel.defaultModel()) == null) {
                // first spring context use default application instance
                applicationModel = ApplicationModel.defaultModel();
                logger.info("Use default application: " + applicationModel.getDesc());
            } else {
                // create a new application instance for later spring context
                applicationModel = FrameworkModel.defaultModel().newApplication();
                logger.info("Create new application: " + applicationModel.getDesc());
            }

            // init ModuleModel
            moduleModel = applicationModel.getDefaultModule();
            context.setModuleModel(moduleModel);
            logger.info("Use default module model of target application: " + moduleModel.getDesc());
        } else {
            logger.info("Use module model from customizer: " + moduleModel.getDesc());
        }
        logger.info(
                "Bind " + moduleModel.getDesc() + " to spring container: " + ObjectUtils.identityToString(registry));

        // set module attributes
        Map<String, Object> moduleAttributes = context.getModuleAttributes();
        if (moduleAttributes.size() > 0) {
            moduleModel.getAttributes().putAll(moduleAttributes);
        }

        // bind dubbo initialization context to spring context
        registerContextBeans(beanFactory, context);

        // mark context as bound
        context.markAsBound();
        moduleModel.setLifeCycleManagedExternally(true);

        // register common beans
        DubboBeanUtils.registerCommonBeans(registry);
    }

    //org.apache.dubbo.config.spring.util.DubboBeanUtils#registerCommonBeans
    //注册多个bean到spring容器内
    static void registerCommonBeans(BeanDefinitionRegistry registry) {
    
        registerInfrastructureBean(registry, ServicePackagesHolder.BEAN_NAME, ServicePackagesHolder.class);
    
        registerInfrastructureBean(registry, ReferenceBeanManager.BEAN_NAME, ReferenceBeanManager.class);
    
        //处理@DubboReference, 具体对该方法进行处理的类
        //org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor.processReferenceAnnotatedBeanDefinition
        // Since 2.5.7 Register @Reference Annotation Bean Processor as an infrastructure Bean
        registerInfrastructureBean(
                registry, ReferenceAnnotationBeanPostProcessor.BEAN_NAME, ReferenceAnnotationBeanPostProcessor.class);
    
        // TODO Whether DubboConfigAliasPostProcessor can be removed ?
        // Since 2.7.4 [Feature] https://github.com/apache/dubbo/issues/5093
        registerInfrastructureBean(
                registry, DubboConfigAliasPostProcessor.BEAN_NAME, DubboConfigAliasPostProcessor.class);
    
        // register ApplicationListeners
        registerInfrastructureBean(
                registry, DubboDeployApplicationListener.class.getName(), DubboDeployApplicationListener.class);
        registerInfrastructureBean(
                registry, DubboConfigApplicationListener.class.getName(), DubboConfigApplicationListener.class);
    
        // Since 2.7.6 Register DubboConfigDefaultPropertyValueBeanPostProcessor as an infrastructure Bean
        registerInfrastructureBean(
                registry,
                DubboConfigDefaultPropertyValueBeanPostProcessor.BEAN_NAME,
                DubboConfigDefaultPropertyValueBeanPostProcessor.class);
    
        // Dubbo config initializer
        registerInfrastructureBean(registry, DubboConfigBeanInitializer.BEAN_NAME, DubboConfigBeanInitializer.class);
    
        // register infra bean if not exists later
        registerInfrastructureBean(
                registry, DubboInfraBeanRegisterPostProcessor.BEAN_NAME, DubboInfraBeanRegisterPostProcessor.class);
    }
```

dubbo 配置相关参数可以在类 `org.apache.dubbo.config.spring.context.annotation.DubboConfigConfiguration` 中查看