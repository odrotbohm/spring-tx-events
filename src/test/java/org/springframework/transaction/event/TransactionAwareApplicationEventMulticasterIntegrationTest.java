/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.transaction.event;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Integration tests for {@link TransactionAwareApplicationEventMulticaster}.
 * 
 * @author Oliver Gierke
 */
@SuppressWarnings("serial")
public class TransactionAwareApplicationEventMulticasterIntegrationTest {

	ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

	@Before
	public void setUp() {
		context = new AnnotationConfigApplicationContext(Config.class);
	}

	@After
	public void tearDown() {
		context.close();
	}

	@Test
	public void delaysTransactionalApplicationEventsUntilTransactionCommits() {

		ApplicationComponent component = (ApplicationComponent) context.getBean("applicationComponent");
		component.sampleMethod();

		// Expect the transactional event having been published eventually.
		assertThat(component.getReceivedEvent(), is(instanceOf(SampleTransactionalApplicationEvent.class)));
	}

	// Config

	@Configuration
	@EnableTransactionManagement(proxyTargetClass = true)
	static class Config {

		@Bean
		ApplicationEventMulticaster applicationEventMulticaster() {
			return new TransactionAwareApplicationEventMulticaster();
		}

		@Bean
		ApplicationComponent applicationComponent() {
			return new ApplicationComponent();
		}

		@Bean
		PlatformTransactionManager transactionManager() {
			return new StubPlatformTransactionManager();
		}
	}

	// Application component

	private static class ApplicationComponent implements ApplicationListener<ApplicationEvent> {

		@Autowired ApplicationEventPublisher publisher;

		SampleApplicationEvent receivedEvent;

		@Transactional
		public void sampleMethod() {

			// Throw transactional event -> expected to be delayed
			publisher.publishEvent(new SampleTransactionalApplicationEvent(this));
			assertThat(receivedEvent, is(nullValue()));

			// Throw non-transactional event -> exepect immediate publication
			SampleNonTransactionalApplicationEvent event = new SampleNonTransactionalApplicationEvent(this);
			publisher.publishEvent(event);
			assertThat(receivedEvent, is((SampleApplicationEvent) event));
		}

		public SampleApplicationEvent getReceivedEvent() {
			return receivedEvent;
		}

		public void onApplicationEvent(ApplicationEvent event) {

			if (event instanceof SampleApplicationEvent) {
				this.receivedEvent = (SampleApplicationEvent) event;
			}
		}
	}

	// Events

	private interface SampleApplicationEvent {}

	private static class SampleNonTransactionalApplicationEvent extends ApplicationEvent implements
			SampleApplicationEvent {

		public SampleNonTransactionalApplicationEvent(Object source) {
			super(source);
		}
	}

	private static class SampleTransactionalApplicationEvent extends TransactionBoundApplicationEvent implements
			SampleApplicationEvent {

		public SampleTransactionalApplicationEvent(Object source) {
			super(source);
		}
	}

	// Transaction manager

	private static class StubPlatformTransactionManager extends AbstractPlatformTransactionManager {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
		 */
		@Override
		protected Object doGetTransaction() throws TransactionException {
			return new Object();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object, org.springframework.transaction.TransactionDefinition)
		 */
		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {

		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
		 */
		@Override
		protected void doCommit(DefaultTransactionStatus status) throws TransactionException {

		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
		 */
		@Override
		protected void doRollback(DefaultTransactionStatus status) throws TransactionException {

		}
	}
}
