

import com.github.tlrx.elasticsearch.test.annotations.*;
import com.github.tlrx.elasticsearch.test.support.junit.runners.ElasticsearchRunner;
import io.searchbox.annotations.JestId;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tlrx.elasticsearch.test.annotations.ElasticsearchMappingField.Store.*;
import static org.junit.Assert.*;

import java.io.IOException;

/**
 * Basic Test cases for working with ElasticSearch embedded
 * <p/>
 * User: jameshoare
 * Date: 31/08/2013
 */
@RunWith(ElasticsearchRunner.class)
@ElasticsearchNode
public class ElasticSearchTest {

    @ElasticsearchClient
    Client client;

    @Test
    @ElasticsearchIndex(indexName = "customers")
    public void indexInternalVersion() throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();

        // customer #1
        builder.startObject()
                .field("firstName", "James")
                .field("lastName", "Hoare")
                .endObject();

        // Index customer #1
        IndexResponse response = client.prepareIndex("customers", "customer", "1")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("First document version must be 1", 1, response.getVersion());

        // Modify customer #1
        builder = JsonXContent.contentBuilder().startObject()
                .field("firstName", "Jess")
                .field("lastName", "Hoare")
                .field("status", "EIP")
                .endObject();


        // Update customer #1
        response = client.prepareIndex("customers", "customer", "1")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("Updated version must be 2", 2, response.getVersion());

        // Try to update customer #1 with a wrong version number
        try {
            response = client.prepareIndex("customers", "customer", "1")
                    .setSource(builder)
                    .setVersion(1)
                    .execute().actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be 2", 2, e.getCurrentVersion());
        }

        // Update customer #1 with a right version number
        response = client.prepareIndex("customers", "customer", "1")
                .setSource(builder)
                .setVersion(2)
                .execute().actionGet();

        assertEquals("Updated version must be 3", 3, response.getVersion());

        // Try to update customer #1 with a wrong version number
        try {
            response = client.prepareIndex("customers", "customer", "1")
                    .setSource(builder)
                    .setVersion(5)
                    .execute().actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be 3", 3, e.getCurrentVersion());
        }
    }


    @Test
    @ElasticsearchIndex(indexName = "customers")
    public void indexExternalVersion() throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();

        // Book #2
        builder.startObject()
                .field("firstName", "James")
                .field("lastName", "Hoare")
                .endObject();

        long startVersion = System.currentTimeMillis();

        // Try to index customer #2 with a custom version but no external
        try {
            client.prepareIndex("customers", "customer", "2")
                    .setSource(builder)
                    .setVersion(startVersion)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals("Current version must be -1", -1, e.getCurrentVersion());
        }

        // Index customer #2 with a custom version with external
        IndexResponse response = client.prepareIndex("customers", "customer", "2")
                .setSource(builder)
                .setVersion(startVersion)
                .setVersionType(VersionType.EXTERNAL)
                .execute()
                .actionGet();

        assertEquals("Document version must be incremented", startVersion, response.getVersion());

        // Modify customer #2
        builder = JsonXContent.contentBuilder().startObject()
                .field("firstName", "James")
                .field("lastName", "Hoare")
                .field("status", "EIP")
                .endObject();

        // Update customer #2 without version control
        response = client.prepareIndex("customers", "customer", "2")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("Document updated must have an incremented version number", startVersion + 1, response.getVersion());

        // Try to index customer #2 with lower version number
        try {
            response = client.prepareIndex("customers", "customer", "2")
                    .setSource(builder)
                    .setVersion(startVersion - 10)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 1, e.getCurrentVersion());
        }

        // Try to index customer #2 with same version number
        try {
            response = client.prepareIndex("customers", "customer", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 1)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 1, e.getCurrentVersion());
        }

        // Try to index customer #2 with greater version number
        response = client.prepareIndex("customers", "customer", "2")
                .setSource(builder)
                .setVersion(startVersion + 10)
                .setVersionType(VersionType.EXTERNAL)
                .execute()
                .actionGet();
        assertEquals("Updated version must be " + startVersion + 10, startVersion + 10, response.getVersion());

        // Try to index customer #2 with lower version number and no version_type = external
        try {
            response = client.prepareIndex("customers", "customer", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 5)
                    .setVersionType(VersionType.EXTERNAL)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 10, e.getCurrentVersion());
        }

        // Update customer #2 with same version number but no version_type = external
        response = client.prepareIndex("customers", "customer", "2")
                .setSource(builder)
                .setVersion(startVersion + 10)
                .execute()
                .actionGet();

        assertEquals("Document updated must have an incremented version number", startVersion + 11, response.getVersion());

        // Try to index customer #2 with greater version number and no version_type = external
        try {
            response = client.prepareIndex("customers", "customer", "2")
                    .setSource(builder)
                    .setVersion(startVersion + 30)
                    .execute()
                    .actionGet();

            fail("Expected a VersionConflictEngineException");
        } catch (VersionConflictEngineException e) {
            assertNotNull(e);
            assertEquals(startVersion + 11, e.getCurrentVersion());
        }
    }

    @Test
    @ElasticsearchIndex(indexName = "customers", forceCreate = true,
            mappings = {@ElasticsearchMapping(typeName = "customer",
                    properties = {
                            @ElasticsearchMappingField(name = "firstName", store = Yes, type = ElasticsearchMappingField.Types.String),
                            @ElasticsearchMappingField(name = "lastName", store = ElasticsearchMappingField.Store.Yes, type = ElasticsearchMappingField.Types.String),
                            @ElasticsearchMappingField(name = "age", store = ElasticsearchMappingField.Store.Yes, type = ElasticsearchMappingField.Types.Integer),
                            @ElasticsearchMappingField(name = "title", store = ElasticsearchMappingField.Store.Yes, type = ElasticsearchMappingField.Types.String),
                            @ElasticsearchMappingField(name = "status", store = ElasticsearchMappingField.Store.Yes, type = ElasticsearchMappingField.Types.String)
                    })
            })
    //@ElasticsearchBulkRequest(dataFile = "customers.json")
    public void searchCustomers() throws Exception {




        XContentBuilder builder = JsonXContent.contentBuilder();

        // customer #1
        builder.startObject()
                .field("firstName", "James")
                .field("lastName", "Hoare")
                .field("age",37)
                .field("title","mr")
                .field("status","eip")
                .endObject();

        IndexResponse indexRes = client.prepareIndex("customers", "customer", "1")
                .setSource(builder)
                .execute()
                .actionGet();

        assertEquals("First document version must be 1", 1, indexRes.getVersion());


        SearchResponse response = client.prepareSearch("customers")
                .setQuery(QueryBuilders.matchAllQuery())
                .setFilter(FilterBuilders.termFilter("firstName", "James"))
                .execute()
                .actionGet();
        assertEquals(1L, response.getHits().totalHits());


        response = client.prepareSearch("customers").setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        assertEquals(1, response.getHits().totalHits());


        // Configuration
        /*ClientConfig clientConfig = new ClientConfig.Builder("http://localhost:9200").multiThreaded(true).build();

        // Construct a new Jest client according to configuration via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setClientConfig(clientConfig);
        JestClient client = factory.getObject();
*/



       /* Customer c = new Customer();
        c.setAge(37);
        c.setFirstName("James");
        c.setLastName("Hoare");
        c.setStatus("eip");
        c.setTitle("mr");



        Bulk bulk = new Bulk.Builder()
                .defaultIndex("customers")
                .defaultType("customer")
                .addAction(new Index.Builder(c).build())

                .build();


        // Index customer #1
        client.execute(bulk);


        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("customers")
                .addType("customer")
                .build();

        JestResult result = client.execute(search);
        result.getJsonString();

*/





    }
}


class Customer {

    // JestId is optional, use when you want to set a property as ElasticSearch index id
    @JestId
    private Long customerid;

    private String firstName;
    private String lastName;
    private Integer age;
    private String title;
    private String status;


    String getFirstName() {
        return firstName;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    String getLastName() {
        return lastName;
    }

    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    Integer getAge() {
        return age;
    }

    void setAge(Integer age) {
        this.age = age;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }
}