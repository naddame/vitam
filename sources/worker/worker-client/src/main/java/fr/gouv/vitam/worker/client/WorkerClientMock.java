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
package fr.gouv.vitam.worker.client;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitam.common.ServerIdentity;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.StatusMessage;
import fr.gouv.vitam.common.server.application.configuration.ClientConfigurationImpl;
import fr.gouv.vitam.processing.common.model.EngineResponse;
import fr.gouv.vitam.processing.common.model.ProcessResponse;
import fr.gouv.vitam.worker.client.exception.WorkerNotFoundClientException;
import fr.gouv.vitam.worker.client.exception.WorkerServerClientException;
import fr.gouv.vitam.worker.common.DescriptionStep;

/**
 * Mock client implementation for worker
 */
class WorkerClientMock extends WorkerClientRest implements WorkerClient {
    private static final ServerIdentity SERVER_IDENTITY = ServerIdentity.getInstance();

    /**
     * Constructor
     */
    WorkerClientMock() {
        super(new ClientConfigurationImpl("mock", 1), "/", false);
    }

    @Override
    public StatusMessage getStatus() throws VitamClientException {
        return new StatusMessage(SERVER_IDENTITY);
    }

    @Override
    public List<EngineResponse> submitStep(String requestId, DescriptionStep data)
        throws WorkerNotFoundClientException, WorkerServerClientException {
        final List<EngineResponse> mockResponse = new ArrayList<>();
        mockResponse.add(new ProcessResponse());
        return mockResponse;
    }

}
