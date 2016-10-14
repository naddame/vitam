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
package fr.gouv.vitam.worker.core.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.processing.common.exception.HandlerNotFoundException;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.model.Action;
import fr.gouv.vitam.processing.common.model.ActionDefinition;
import fr.gouv.vitam.processing.common.model.EngineResponse;
import fr.gouv.vitam.processing.common.model.ProcessBehavior;
import fr.gouv.vitam.processing.common.model.ProcessResponse;
import fr.gouv.vitam.processing.common.model.Step;
import fr.gouv.vitam.processing.common.parameter.WorkerParametersFactory;
import fr.gouv.vitam.worker.core.api.Worker;
import fr.gouv.vitam.worker.core.handler.ActionHandler;
import fr.gouv.vitam.worker.core.handler.ExtractSedaActionHandler;

public class WorkerImplTest {

    private Worker workerImpl;

    @Before
    public void setUp() throws Exception {}

    @Test(expected = IllegalArgumentException.class)
    public void givenWorkerImplementWhenWorkParamsIsNullThenThrowsIllegalArgumentException()
        throws IllegalArgumentException, HandlerNotFoundException, ProcessingException {
        workerImpl = WorkerImplFactory.create();
        workerImpl.run(null, new Step());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenWorkerImplementWhenStepIsNullThenThrowsIllegalArgumentException()
        throws IllegalArgumentException, HandlerNotFoundException, ProcessingException {
        workerImpl = WorkerImplFactory.create();
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenWorkerImplementWhenEmptyActionsInStepThenThrowsIllegalArgumentException()
        throws IllegalArgumentException, HandlerNotFoundException, ProcessingException {
        workerImpl = WorkerImplFactory.create();
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            new Step());
    }

    @Test(expected = HandlerNotFoundException.class)
    public void givenWorkerImplementWhenActionIsNullThenThrowsHandlerNotFoundException()
        throws IllegalArgumentException, HandlerNotFoundException, ProcessingException {
        workerImpl = WorkerImplFactory.create();
        final Step step = new Step();
        final List<Action> actions = new ArrayList<Action>();
        final Action action = new Action();
        action.setActionDefinition(new ActionDefinition());
        actions.add(action);
        step.setActions(actions);
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            step);
    }

    @Test
    public void actionNoBlockTest() throws Exception {
        final Step step = new Step();
        step.setStepName("Traiter_archives");
        final List<Action> actions = new ArrayList<Action>();
        final Action action = new Action();
        final ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setActionKey(ExtractSedaActionHandler.getId());
        actionDefinition.setBehavior(ProcessBehavior.NOBLOCKING);
        action.setActionDefinition(actionDefinition);
        actions.add(action);
        step.setActions(actions);

        final ActionHandler actionHandler = mock(ExtractSedaActionHandler.class);
        final EngineResponse response = new ProcessResponse().setStatus(StatusCode.OK);
        when(actionHandler.execute(anyObject(), anyObject())).thenReturn(response);
        workerImpl = WorkerImplFactory.create().addActionHandler(ExtractSedaActionHandler.getId(), actionHandler);
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            step);
    }

    @Test
    public void actionBlockTest() throws Exception {
        final Step step = new Step();
        step.setStepName("Traiter_archives");
        final List<Action> actions = new ArrayList<Action>();
        final Action action = new Action();
        final ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setActionKey(ExtractSedaActionHandler.getId());
        actionDefinition.setBehavior(ProcessBehavior.BLOCKING);
        action.setActionDefinition(actionDefinition);
        actions.add(action);
        step.setActions(actions);

        final ActionHandler actionHandler = mock(ExtractSedaActionHandler.class);
        final EngineResponse response = new ProcessResponse().setStatus(StatusCode.FATAL);
        when(actionHandler.execute(anyObject(), anyObject())).thenReturn(response);
        workerImpl = WorkerImplFactory.create().addActionHandler(ExtractSedaActionHandler.getId(), actionHandler);
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            step);
    }

    @Test
    public void actionBlockTestWarning() throws Exception {
        final Step step = new Step();
        step.setStepName("Traiter_archives");
        final List<Action> actions = new ArrayList<Action>();
        final Action action = new Action();
        final ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setActionKey(ExtractSedaActionHandler.getId());
        actionDefinition.setBehavior(ProcessBehavior.NOBLOCKING);
        action.setActionDefinition(actionDefinition);
        actions.add(action);
        step.setActions(actions);

        final ActionHandler actionHandler = mock(ExtractSedaActionHandler.class);
        final EngineResponse response = new ProcessResponse().setStatus(StatusCode.FATAL);
        when(actionHandler.execute(anyObject(), anyObject())).thenReturn(response);
        workerImpl = WorkerImplFactory.create().addActionHandler(ExtractSedaActionHandler.getId(), actionHandler);
        workerImpl.run(
            WorkerParametersFactory.newWorkerParameters().setUrlWorkspace("fakeUrl").setUrlMetadata("fakeUrl")
                .setObjectName("objectName.json").setCurrentStep("currentStep").setContainerName("containerName"),
            step);
    }

}
