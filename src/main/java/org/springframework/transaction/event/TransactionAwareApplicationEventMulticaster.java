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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Special {@link ApplicationEventMulticaster} that treats {@link TransactionBoundApplicationEvent}s by delaying them to
 * be multicasted when the currently running transaction commits.
 * 
 * @author Oliver Gierke
 */
public class TransactionAwareApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.event.ApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent)
	 */
	public void multicastEvent(ApplicationEvent event) {

		if (!(event instanceof TransactionBoundApplicationEvent)) {
			doMulticastEvent(event);
			return;
		}

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("Detected transactional event but no transaction currently in progress!");
		}

		TransactionSynchronizationManager
				.registerSynchronization(new TransactionMessageMulticastingTransactionSynchronization(
						(TransactionBoundApplicationEvent) event));
	}

	/**
	 * Helper method to directly invoke the multicating of the given {@link ApplicationEvent} and work around the
	 * customization implemented in {@link #multicastEvent(ApplicationEvent)}.
	 * 
	 * @param event the event to multicast
	 */
	private void doMulticastEvent(ApplicationEvent event) {
		super.multicastEvent(event);
	}

	/**
	 * A {@link TransactionSynchronization} to publish the contained {@link TransactionBoundApplicationEvent} on
	 * transaction commit.
	 *
	 * @author Oliver Gierke
	 */
	private class TransactionMessageMulticastingTransactionSynchronization extends TransactionSynchronizationAdapter {

		private final TransactionBoundApplicationEvent event;

		/**
		 * Creates a new {@link TransactionMessageMulticastingTransactionSynchronization} for the given
		 * {@link TransactionBoundApplicationEvent}.
		 * 
		 * @param event must not be {@literal null}.
		 */
		public TransactionMessageMulticastingTransactionSynchronization(TransactionBoundApplicationEvent event) {

			Assert.notNull(event, "TransactionBoundApplicationEvent must not be null!");
			this.event = event;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#afterCommit()
		 */
		@Override
		public void afterCommit() {
			TransactionAwareApplicationEventMulticaster.this.doMulticastEvent(event);
		}
	}
}
