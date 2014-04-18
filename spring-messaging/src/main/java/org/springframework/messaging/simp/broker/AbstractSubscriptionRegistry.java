/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.simp.broker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MultiValueMap;

/**
 * Abstract base class for implementations of {@link SubscriptionRegistry} that
 * looks up information in messages but delegates to abstract methods for the
 * actual storage and retrieval.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public abstract class AbstractSubscriptionRegistry implements SubscriptionRegistry {

	protected final Log logger = LogFactory.getLog(getClass());


	@Override
	public final void registerSubscription(Message<?> message) {

		MessageHeaders headers = message.getHeaders();
		SimpMessageType type = SimpMessageHeaderAccessor.getMessageType(headers);

		if (!SimpMessageType.SUBSCRIBE.equals(type)) {
			logger.error("Expected SUBSCRIBE message: " + message);
			return;
		}
		String sessionId = SimpMessageHeaderAccessor.getSessionId(headers);
		if (sessionId == null) {
			logger.error("Ignoring subscription. No sessionId in message: " + message);
			return;
		}
		String subscriptionId = SimpMessageHeaderAccessor.getSubscriptionId(headers);
		if (subscriptionId == null) {
			logger.error("Ignoring subscription. No subscriptionId in message: " + message);
			return;
		}
		String destination = SimpMessageHeaderAccessor.getDestination(headers);
		if (destination == null) {
			logger.error("Ignoring destination. No destination in message: " + message);
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Adding subscription id=" + subscriptionId + ", destination=" + destination);
		}
		addSubscriptionInternal(sessionId, subscriptionId, destination, message);
	}

	protected abstract void addSubscriptionInternal(String sessionId, String subscriptionId,
			String destination, Message<?> message);

	@Override
	public final void unregisterSubscription(Message<?> message) {

		MessageHeaders headers = message.getHeaders();
		SimpMessageType type = SimpMessageHeaderAccessor.getMessageType(headers);

		if (!SimpMessageType.UNSUBSCRIBE.equals(type)) {
			logger.error("Expected UNSUBSCRIBE message: " + message);
			return;
		}
		String sessionId = SimpMessageHeaderAccessor.getSessionId(headers);
		if (sessionId == null) {
			logger.error("Ignoring subscription. No sessionId in message: " + message);
			return;
		}
		String subscriptionId = SimpMessageHeaderAccessor.getSubscriptionId(headers);
		if (subscriptionId == null) {
			logger.error("Ignoring subscription. No subscriptionId in message: " + message);
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Unubscribe request: " + message);
		}
		removeSubscriptionInternal(sessionId, subscriptionId, message);
	}

	protected abstract void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message);

	@Override
	public abstract void unregisterAllSubscriptions(String sessionId);

	@Override
	public final MultiValueMap<String, String> findSubscriptions(Message<?> message) {

		MessageHeaders headers = message.getHeaders();
		SimpMessageType type = SimpMessageHeaderAccessor.getMessageType(headers);

		if (!SimpMessageType.MESSAGE.equals(type)) {
			logger.trace("Ignoring message type " + type);
			return null;
		}
		String destination = SimpMessageHeaderAccessor.getDestination(headers);
		if (destination == null) {
			logger.trace("Ignoring message, no destination");
			return null;
		}
		MultiValueMap<String, String> result = findSubscriptionsInternal(destination, message);
		if (logger.isTraceEnabled()) {
			logger.trace("Found " + result.size() + " subscriptions for destination=" + destination);
		}
		return result;
	}

	protected abstract MultiValueMap<String, String> findSubscriptionsInternal(
			String destination, Message<?> message);

}