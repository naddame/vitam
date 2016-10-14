/*******************************************************************************
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.access.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import fr.gouv.vitam.access.client.AccessClientFactory.AccessClientType;
import fr.gouv.vitam.access.common.exception.AccessClientNotFoundException;
import fr.gouv.vitam.access.common.exception.AccessClientServerException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;

/**
 * Test for access operation client
 */
public class AccessClientMockTest {

    final String queryDsql =
        "{ $query : [ { $eq : { 'title' : 'test' } } ], " +
            " $filter : { $orderby : { '#id' } }," +
            " $projection : {$fields : {#id : 1, title:2, transacdate:1}}" +
            " }";
    final String ID = "identifier1";

    @Test
    public void givenMockConfExist_WhenCreateMockedClient_ThenReturnOK() {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);
    }

    @Test
    public void givenMockExists_whenSelectUnit_ThenReturnOK()
        throws AccessClientServerException, AccessClientNotFoundException, InvalidParseOperationException {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);

        assertThat(client.selectUnits(queryDsql)).isNotNull();
    }

    @Test
    public void givenMockExists_whenSelectUnitById_ThenReturnOK()
        throws AccessClientServerException, AccessClientNotFoundException, InvalidParseOperationException {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);

        assertThat(client.selectUnitbyId(queryDsql, ID)).isNotNull();
    }

    @Test
    public void givenMockExists_whenUpdateUnitById_ThenReturnOK()
        throws AccessClientServerException, AccessClientNotFoundException, InvalidParseOperationException {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);

        assertThat(client.updateUnitbyId(queryDsql, ID)).isNotNull();
    }

    @Test
    public void givenMockExistsWhenSelectObjectByIdThenReturnOK()
        throws AccessClientServerException, AccessClientNotFoundException, InvalidParseOperationException {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);

        assertThat(client.selectObjectbyId(queryDsql, ID)).isNotNull();
    }

    @Test
    public void givenMockExistsWhenGetObjectAsFileThenReturnOK() throws Exception {
        AccessClientFactory.setConfiguration(AccessClientType.MOCK, null, 0);

        final AccessClient client =
            AccessClientFactory.getInstance().getAccessOperationClient();
        assertNotNull(client);
        final InputStream stream = client.getObjectAsInputStream(queryDsql, ID, "usage", 1);
        final InputStream stream2 = IOUtils.toInputStream(AccessClientMock.MOCK_GET_FILE_CONTENT);
        assertNotNull(stream);
        assertTrue(IOUtils.contentEquals(stream, stream2));
    }

}
