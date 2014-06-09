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

/**
 * Special {@link ApplicationEvent} that will be multicasted on transaction commit.
 * 
 * @author Oliver Gierke
 */
public abstract class TransactionBoundApplicationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 562193908344675498L;

	/**
	 * Creates a new {@link TransactionBoundApplicationEvent}.
	 * 
	 * @param source must not be {@literal null}.
	 */
	public TransactionBoundApplicationEvent(Object source) {
		super(source);
	}
}
