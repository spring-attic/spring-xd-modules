/*
 * Copyright 2015 the original author or authors.
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


package org.springframework.xd.sink.cassandra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;
import org.springframework.xd.module.cassandra.CassandraConfiguration;

/**
 * @author Artem Bilan
 */
@Configuration
@EnableIntegration
@Import(CassandraConfiguration.class)
public class CassandraSinkConfiguration {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	@Value("#{environment.consistencyLevel}")
	private ConsistencyLevel consistencyLevel;

	@Value("#{environment.retryPolicy}")
	private RetryPolicy retryPolicy;

	@Value("#{environment.ttl}")
	private Integer ttl;

	@Value("${queryType}")
	private CassandraMessageHandler.Type queryType;

	@Value("${ingestQuery:}")
	private String ingestQuery;

	@Value("${statementExpression:}")
	private String statementExpression;

	@Autowired
	public CassandraOperations template;

	@Bean
	public MessageChannel input() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "input")
	public MessageHandler cassandraSinkMessageHandler() {
		CassandraMessageHandler<?> cassandraMessageHandler = new CassandraMessageHandler<>(this.template, this.queryType);
		cassandraMessageHandler.setProducesReply(false);
		if (this.consistencyLevel != null || this.retryPolicy  != null || this.ttl != null) {
			cassandraMessageHandler.setWriteOptions(new WriteOptions(this.consistencyLevel, this.retryPolicy, this.ttl));
		}
		if (StringUtils.hasText(this.ingestQuery)) {
			cassandraMessageHandler.setIngestQuery(this.ingestQuery);
		}
		else if (StringUtils.hasText(this.statementExpression)) {
			Expression expression = PARSER.parseExpression(this.statementExpression);
			cassandraMessageHandler.setStatementExpression(expression);
		}
		return cassandraMessageHandler;
	}

}
