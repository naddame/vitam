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
package fr.gouv.vitam.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.SedaConstants;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.configuration.ClassificationLevel;
import fr.gouv.vitam.common.json.JsonHandler;

/**
 * classification level service
 */
public class ClassificationLevelUtil {

    private static final String PATH_CLASSIFICATION_LEVEL_1
        = SedaConstants.TAG_ARCHIVE_UNIT + "."
        + SedaConstants.TAG_MANAGEMENT + "."
        + SedaConstants.TAG_RULE_CLASSIFICATION + "."
        + SedaConstants.TAG_RULE_CLASSIFICATION_LEVEL;

    private static final String PATH_CLASSIFICATION_LEVEL_2
        = SedaConstants.TAG_ARCHIVE_UNIT
        + ".#management."
        + SedaConstants.TAG_RULE_CLASSIFICATION + "."
        + SedaConstants.TAG_RULE_CLASSIFICATION_LEVEL;

    private ClassificationLevelUtil() {
    }

    public static boolean checkClassificationLevel(JsonNode archiveUnit) {
        String classificationLevelValue = null;
        JsonNode classificationLevel = JsonHandler.findNode(archiveUnit, PATH_CLASSIFICATION_LEVEL_1);
        if (classificationLevel.isMissingNode()) {
            classificationLevel = JsonHandler.findNode(archiveUnit, PATH_CLASSIFICATION_LEVEL_2);
        }

        if (!classificationLevel.isMissingNode()) {
            classificationLevelValue = classificationLevel.asText();
        }

        return checkClassificationLevel(classificationLevelValue);
    }

    public static boolean checkClassificationLevel(String classificationLevelValue) {
        if (classificationLevelValue != null) {
            if (!VitamConfiguration.getClassificationLevel().getAllowList().contains(classificationLevelValue)) {
                return false;
            }
        } else {
            return VitamConfiguration.getClassificationLevel().authorizeNotDefined();
        }
        return true;
    }

}
