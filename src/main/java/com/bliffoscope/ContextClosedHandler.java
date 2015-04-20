package com.bliffoscope;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;


@Component
class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> , ApplicationContextAware,BeanPostProcessor{
	
    private ApplicationContext context;

	private Logger logger = LoggerFactory.getLogger(ContextClosedHandler.class);
	
    public void onApplicationEvent(ContextClosedEvent event) {
    	
       	Map<String, ThreadPoolTaskExecutor> executers = context.getBeansOfType(ThreadPoolTaskExecutor.class);
       	
       	for (ThreadPoolTaskExecutor executor: executers.values()) {
       		int retryCount = 0;
            while(executor.getActiveCount()>0 && ++retryCount<10){
            	try {
            		logger.info("Executer "+executor.getThreadNamePrefix()+" is still working with active " + executor.getActiveCount()+" work. Retry count is "+retryCount);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            if(!(retryCount<10))
            	logger.info("Executer "+executor.getThreadNamePrefix()+" is still working.Since Retry count exceeded max value "+retryCount+", will be killed immediately");
    		executor.shutdown();
    		logger.debug("Executer "+executor.getThreadNamePrefix()+" with active " + executor.getActiveCount()+" work has killed");
		}
    }


	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;		
	}


	public Object postProcessAfterInitialization(Object object, String arg1)
			throws BeansException {
		return object;
	}


	public Object postProcessBeforeInitialization(Object object, String arg1)
			throws BeansException {
		if(object instanceof ThreadPoolTaskScheduler)
			((ThreadPoolTaskScheduler)object).setWaitForTasksToCompleteOnShutdown(true);
		if(object instanceof ThreadPoolTaskExecutor)
			((ThreadPoolTaskExecutor)object).setWaitForTasksToCompleteOnShutdown(true);
		return object;
	}

}