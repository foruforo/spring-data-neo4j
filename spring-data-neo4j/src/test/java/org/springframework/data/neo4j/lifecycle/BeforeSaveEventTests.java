/**
 * Copyright 2011 the original author or authors.
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
package org.springframework.data.neo4j.lifecycle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.neo4j.helpers.collection.IteratorUtil.single;

@NodeEntity
class Foo {
    @GraphId
    Long id;

    String generatedId = "no event";
}

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class BeforeSaveEventTests {
    @Configuration
    @EnableNeo4jRepositories
    static class TestConfig extends Neo4jConfiguration {
        TestConfig() throws ClassNotFoundException {
            setBasePackage(Foo.class.getPackage().getName());
        }
        @Bean
        GraphDatabaseService graphDatabaseService() {
            return new TestGraphDatabaseFactory().newImpermanentDatabase();
        }

        @Bean
        ApplicationListener<BeforeSaveEvent> beforeSaveEventApplicationListener() {
            return new ApplicationListener<BeforeSaveEvent>() {
                @Override
                public void onApplicationEvent(BeforeSaveEvent event) {
                    Foo foo = (Foo) event.getEntity();
                    foo.generatedId = "before event";
                }
            };
        }
    }

    @Autowired
    private Neo4jTemplate template;

    @Test
    public void shouldFireBeforeEntityIsSaved() throws Exception {
        assertThat(template.save(new Foo()).generatedId, is("before event"));
        assertThat(single(template.findAll(Foo.class)).generatedId, is("before event"));
    }
}
