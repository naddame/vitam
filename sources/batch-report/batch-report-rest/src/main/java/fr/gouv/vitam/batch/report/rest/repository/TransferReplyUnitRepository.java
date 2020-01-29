/*
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
 */
package fr.gouv.vitam.batch.report.rest.repository;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import fr.gouv.vitam.batch.report.model.TransferReplyUnitModel;
import fr.gouv.vitam.common.database.server.mongodb.MongoDbAccess;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * ReportRepository
 */
public class TransferReplyUnitRepository extends ReportCommonRepository {

    public static final String TRANSFER_REPLY_UNIT = "TransferReplyUnit";
    private final MongoCollection<Document> transferReplyReportCollection;

    @VisibleForTesting
    public TransferReplyUnitRepository(MongoDbAccess mongoDbAccess, String collectionName) {
        this.transferReplyReportCollection = mongoDbAccess.getMongoDatabase().getCollection(collectionName);
    }

    public TransferReplyUnitRepository(MongoDbAccess mongoDbAccess) {
        this(mongoDbAccess, TRANSFER_REPLY_UNIT);
    }

    public void bulkAppendReport(List<TransferReplyUnitModel> reports) {
        Set<TransferReplyUnitModel> reportsWithoutDuplicate = new HashSet<>(reports);
        List<Document> transferReplyUnitDocument =
            reportsWithoutDuplicate.stream()
                .map(ReportCommonRepository::pojoToDocument)
                .collect(Collectors.toList());
        super.bulkAppendReport(transferReplyUnitDocument, transferReplyReportCollection);
    }

    public MongoCursor<Document> findCollectionByProcessIdTenant(String processId, int tenantId) {

        return transferReplyReportCollection.aggregate(
            Arrays.asList(
                Aggregates.match(and(
                    eq(TransferReplyUnitModel.PROCESS_ID, processId),
                    eq(TransferReplyUnitModel.TENANT, tenantId)
                )),
                Aggregates.project(Projections.fields(
                    new Document("_id", 0),
                    new Document("id", "$_metadata.id"),
                    new Document("distribGroup", null),
                    new Document("params.id", "$_metadata.id"),
                    new Document("params.type", new Document("$literal", "Unit")),
                    new Document("params.status", "$_metadata.status")
                    )
                ))
        )
            // Aggregation query requires more than 100MB to proceed.
            .allowDiskUse(true)
            .iterator();
    }

    public void deleteReportByIdAndTenant(String processId, int tenantId) {
        super.deleteReportByIdAndTenant(processId, tenantId, transferReplyReportCollection);
    }
}