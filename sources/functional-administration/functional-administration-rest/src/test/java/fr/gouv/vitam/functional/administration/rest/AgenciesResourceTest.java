package fr.gouv.vitam.functional.administration.rest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.Lists;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.SystemPropertyUtil;
import fr.gouv.vitam.common.client.VitamClientFactory;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.collections.VitamCollection;
import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchNode;
import fr.gouv.vitam.common.elasticsearch.ElasticsearchRule;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.mongo.MongoRule;
import fr.gouv.vitam.common.server.application.configuration.DbConfigurationImpl;
import fr.gouv.vitam.common.server.application.configuration.MongoDbNode;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.functional.administration.common.Agencies;
import fr.gouv.vitam.functional.administration.common.server.AdminManagementConfiguration;
import fr.gouv.vitam.functional.administration.common.server.ElasticsearchAccessFunctionalAdmin;
import fr.gouv.vitam.functional.administration.common.server.FunctionalAdminCollections;
import fr.gouv.vitam.functional.administration.common.server.MongoDbAccessAdminFactory;
import fr.gouv.vitam.functional.administration.common.server.MongoDbAccessReferential;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.jhades.JHades;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static fr.gouv.vitam.common.guid.GUIDFactory.newOperationLogbookGUID;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * As agencies Resource call AgencyService
 */
public class AgenciesResourceTest {

    private static final String PREFIX = GUIDFactory.newGUID().getId();

    @ClassRule
    public static MongoRule mongoRule =
        new MongoRule(VitamCollection.getMongoClientOptions(), "vitam-test");

    @ClassRule
    public static ElasticsearchRule elasticsearchRule = new ElasticsearchRule();

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(AgenciesResourceTest.class);
    private static final String ADMIN_MANAGEMENT_CONF = "functional-administration-test.conf";

    private static final String RESOURCE_URI = "/adminmanagement/v1";
    private static final String STATUS_URI = "/status";

    private static final int TENANT_ID = 0;

    static MongodExecutable mongodExecutable;
    static MongodProcess mongod;
    static MongoDbAccessReferential mongoDbAccess;
    private static String DATABASE_HOST = "localhost";

    private static JunitHelper junitHelper = JunitHelper.getInstance();
    private static int serverPort;
    private static File adminConfigFile;
    private static AdminManagementMain application;


    private static int workspacePort = junitHelper.findAvailablePort();

    @Rule
    public RunWithCustomExecutorRule runInThread =
        new RunWithCustomExecutorRule(VitamThreadPoolExecutor.getDefaultExecutor());

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(workspacePort);

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;
    private String AGENCIES_URI = "/agencies/import";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JHades().overlappingJarsReport();

        FunctionalAdminCollections.beforeTestClass(mongoRule.getMongoDatabase(), PREFIX,
            new ElasticsearchAccessFunctionalAdmin(ElasticsearchRule.VITAM_CLUSTER,
                Lists.newArrayList(new ElasticsearchNode("localhost", ElasticsearchRule.TCP_PORT))),
            Arrays.asList(FunctionalAdminCollections.RULES, FunctionalAdminCollections.AGENCIES));

        File tmpFolder = tempFolder.newFolder();
        System.setProperty("vitam.tmp.folder", tmpFolder.getAbsolutePath());
        SystemPropertyUtil.refresh();

        final List<ElasticsearchNode> nodesEs = new ArrayList<>();
        nodesEs.add(new ElasticsearchNode("localhost", ElasticsearchRule.TCP_PORT));
        LogbookOperationsClientFactory.changeMode(null);


        final File adminConfig = PropertiesUtils.findFile(ADMIN_MANAGEMENT_CONF);
        final AdminManagementConfiguration realAdminConfig =
            PropertiesUtils.readYaml(adminConfig, AdminManagementConfiguration.class);
        realAdminConfig.getMongoDbNodes().get(0).setDbPort(mongoRule.getDataBasePort());
        realAdminConfig.setElasticsearchNodes(nodesEs);
        realAdminConfig.setClusterName(ElasticsearchRule.VITAM_CLUSTER);

        realAdminConfig.setWorkspaceUrl("http://localhost:" + workspacePort);

        adminConfigFile = File.createTempFile("test", ADMIN_MANAGEMENT_CONF, adminConfig.getParentFile());
        PropertiesUtils.writeYaml(adminConfigFile, realAdminConfig);

        final List<MongoDbNode> nodes = new ArrayList<>();
        nodes.add(new MongoDbNode(DATABASE_HOST, mongoRule.getDataBasePort()));
        mongoDbAccess =
            MongoDbAccessAdminFactory.create(new DbConfigurationImpl(nodes, mongoRule.getMongoDatabase().getName()));

        serverPort = junitHelper.findAvailablePort();

        RestAssured.port = serverPort;
        RestAssured.basePath = RESOURCE_URI;

        try {
            application = new AdminManagementMain(adminConfigFile.getAbsolutePath());
            application.start();
            JunitHelper.unsetJettyPortSystemProperty();
        } catch (final VitamApplicationServerException e) {
            LOGGER.error(e);
            throw new IllegalStateException(
                "Cannot start the AdminManagement Application Server", e);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.debug("Ending tests");
        try {
            application.stop();
        } catch (final VitamApplicationServerException e) {
            LOGGER.error(e);
        }

        junitHelper.releasePort(serverPort);
        FunctionalAdminCollections.afterTestClass(true);
        VitamClientFactory.resetConnections();
    }

    @Before
    public void setUp() throws Exception {
        VitamThreadUtils.getVitamSession().setRequestId(newOperationLogbookGUID(TENANT_ID));
        instanceRule.stubFor(WireMock.post(urlMatching("/workspace/v1/containers/(.*)"))
            .willReturn(
                aResponse().withStatus(201).withHeader(GlobalDataRest.X_TENANT_ID, Integer.toString(TENANT_ID))));
        instanceRule.stubFor(WireMock.delete(urlMatching("/workspace/v1/containers/(.*)"))
            .willReturn(
                aResponse().withStatus(204).withHeader(GlobalDataRest.X_TENANT_ID, Integer.toString(TENANT_ID))));
    }

    @After
    public void tearDown() throws Exception {
        FunctionalAdminCollections
            .afterTest(Arrays.asList(FunctionalAdminCollections.RULES, FunctionalAdminCollections.AGENCIES));
    }

    @Test
    @RunWithCustomExecutor
    public final void testGetStatus() {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        get(STATUS_URI).then().statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @RunWithCustomExecutor
    public void givenAWellFormedAgenciesThenReturnCeated() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        File fileAgencies = PropertiesUtils.getResourceFile("agencies.csv");
        MetaDataClientFactory.changeMode(null);

        given().contentType(ContentType.BINARY).body(new FileInputStream(fileAgencies))
            .header(GlobalDataRest.X_TENANT_ID, TENANT_ID)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())

            .when().post(AGENCIES_URI)
            .then().statusCode(Status.CREATED.getStatusCode());
    }

    @Test
    @RunWithCustomExecutor
    public void testDuplicatedAgencies() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        File fileAgencies = PropertiesUtils.getResourceFile("agencies_duplicate.csv");

        MetaDataClientFactory.changeMode(null);

        given().contentType(ContentType.BINARY).body(new FileInputStream(fileAgencies))
            .header(GlobalDataRest.X_TENANT_ID, TENANT_ID)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())

            .when().post(AGENCIES_URI)
            .then().statusCode(Status.BAD_REQUEST.getStatusCode());
    }



    @Test
    @RunWithCustomExecutor
    public void givenAgenciesTest() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(TENANT_ID);
        File fileAgencies = PropertiesUtils.getResourceFile("agencies.csv");
        // first succefull create
        given().contentType(ContentType.BINARY).body(new FileInputStream(fileAgencies))
            .header(GlobalDataRest.X_TENANT_ID, TENANT_ID)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())

            .when().post(AGENCIES_URI)
            .then().statusCode(Status.CREATED.getStatusCode());


        String name = "AG-000003";
        Select select = new Select();
        select.setQuery(QueryHelper.eq(Agencies.IDENTIFIER, name));

        JsonPath body = given().contentType(ContentType.JSON)
            .header(GlobalDataRest.X_TENANT_ID, 0)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())
            .body(select.getFinalSelect())
            .when()
            .get(AgenciesResource.AGENCIES)
            .then().statusCode(Status.OK.getStatusCode()).extract().body().jsonPath();
        List<String> names = body.get("$results.Identifier");

        assertThat(names).hasSize(1);

        name = "agency";
        select = new Select();
        select.setQuery(QueryHelper.eq("Name", name));
        body = given().contentType(ContentType.JSON)
            .header(GlobalDataRest.X_TENANT_ID, 0)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())
            .body(select.getFinalSelect())
            .when()
            .get(AgenciesResource.AGENCIES)
            .then().statusCode(Status.OK.getStatusCode()).extract().body().jsonPath();
        names = body.get("$results.Identifier");
        assertThat(names.size()).isGreaterThan(1);

        fileAgencies = PropertiesUtils.getResourceFile("agencies_remove.csv");
        given().contentType(ContentType.BINARY).body(new FileInputStream(fileAgencies))
            .header(GlobalDataRest.X_TENANT_ID, TENANT_ID)
            .header(GlobalDataRest.X_REQUEST_ID, VitamThreadUtils.getVitamSession().getRequestId())

            .when().post(AGENCIES_URI)
            .then().statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
