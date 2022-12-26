/*
 * Copyright 2012-2013 the original author or authors.
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

package org.example.simple.service;

import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MongoTestResource {

	@Autowired
	MongoTemplate mongoTemplate;


	private static final Logger LOG = LoggerFactory
			.getLogger(MongoTestResource.class);


	@GetMapping(value = "/drop")
	public int dropCollectionProducts( ) {
		LOG.info("Dropping collection...");
		mongoTemplate.dropCollection(Products.class);
		LOG.info("Dropped!");
		return 1;
	}

	@GetMapping(value = "/insert")
	public int bulkInsertProducts(@RequestParam  int count, @RequestParam  String groupId ) {
		LOG.info("START bulkInsertProducts groupId:{}",groupId);
		Products [] productList = Products.RandomProducts(count,groupId);
		LOG.info("GENERATE RandomProducts END bulkInsertProducts groupId:{}",groupId );
		Instant start = Instant.now();
		mongoTemplate.setWriteConcern(WriteConcern.W1.withJournal(true));
		BulkOperations bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Products.class);
		for (int i=0; i<productList.length; ++i) {
			bulkInsertion.insert(productList[i]);
		}
		BulkWriteResult bulkWriteResult = bulkInsertion.execute();
		LOG.info("Bulk insert of "+bulkWriteResult.getInsertedCount()+" documents completed in "+ Duration.between(start, Instant.now()).toMillis() + " milliseconds");
		LOG.info("END bulkInsertProducts groupId:{}",groupId);
		return bulkWriteResult.getInsertedCount();
	}

	@GetMapping(value = "/select")
	public Page<Products> selectProducts(@RequestParam(required = false)  String id
			, @RequestParam(required = false)  String groupId
			, @RequestParam(required = false)  String name,
										 @RequestParam  Integer page,
										 @RequestParam  Integer size ) {
		Sort.Direction direction=Sort.Direction.DESC;


		Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10, direction,"createdDate" );

		final List<Criteria> criteria = new ArrayList<>();
		var query = new Query().with(pageable);
		if (id != null && !id.isBlank()) {
			criteria.add(Criteria.where("id").is( id ));
		}
		if (name != null && !name.isBlank()) {
			criteria.add(Criteria.where("name").regex(name, "i"));
		}
		if (groupId != null  ) {
			criteria.add(Criteria.where("groupId").is(groupId));
		}
		if (!criteria.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
		}
		LOG.info("START select Products query:{} pageable:{}",query,pageable);

		return PageableExecutionUtils.getPage(
				mongoTemplate.find(query, Products.class),
				pageable,
				() -> mongoTemplate.count(query.skip(0).limit(0), Products.class)
		);
	}

}
