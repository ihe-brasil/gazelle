package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by gthomazon on 10/07/17.
 */
public class SystemInSessionValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionValidator.class);

    /**
     * Validate the system keyword filled in the JSF. We check that the system name does not already exist in DB Many cases : - we create a system
     * : - we have to check that the system does not exist
     * in the database - we update a system : - we have to check that the system does not exist in the database, excepting the actual system Note :
     * if you update this method, don't forget to update
     * validateSystemName method, very similar
     *
     * @param begin of the keyword (ie. PACS_AGFA)
     * @param end of the keyword (ie. COPY_2 to get PACS_AGFA_COPY_2)
     * @param inSystem     the system (to know if the system found in the DB is the one edited)
     * @param isModeCopy   indicating if we copy a system from a previous testing session (true) or not (false)
     * @return boolean : if the validation is done without error (true)
     * @throws SystemActionException : exception
     */
    public static boolean validateSystemKeywordStatic(String begin, String end, System inSystem, Boolean isModeCopy) throws SystemActionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean validateSystemKeywordStatic");
        }

        Integer results = 0;
        Integer iFound = 0;
        String currentSystemKeyword = "";

        if ((end == null) || (end.length() == 0)) {
            currentSystemKeyword = begin;
        } else {
            currentSystemKeyword = begin + "_" + end;
        }

        List<System> listOfExistingSystemsForCurrentInstitution = null;
        if ((inSystem.getId() != null) && (Role.isLoggedUserAdmin() || Role.isLoggedUserProjectManager())) {
            List<Institution> listOfInstitutions = System.getInstitutionsForASystem(inSystem);
            if ((listOfInstitutions != null) && (listOfInstitutions.size() > 0)) {
                Institution selectedInstitutionForKeyword = listOfInstitutions.get(0);
                listOfExistingSystemsForCurrentInstitution = System
                        .getSystemsForAnInstitution(selectedInstitutionForKeyword);
            }
        } else {
            listOfExistingSystemsForCurrentInstitution = System.getSystemsForAnInstitution(Institution
                    .getLoggedInInstitution());
        }

        if (listOfExistingSystemsForCurrentInstitution == null) {
            return true;
        }

        for (int i = 0; i < listOfExistingSystemsForCurrentInstitution.size(); i++) {
            if (listOfExistingSystemsForCurrentInstitution.get(i).getKeyword().equalsIgnoreCase(currentSystemKeyword)) {
                results++;
                iFound = i;
            }
        }

        if (results == 0) {
            return true;
        } else if (results == 1) {
            if ((inSystem.getId() != null)
                    && (inSystem.getId().intValue() == listOfExistingSystemsForCurrentInstitution.get(iFound).getId()
                    .intValue())) {
                // We found one system in the database with the same keyword, but it's
                // the current system to update > NO error
                return true;
            } else {
                throw new SystemActionException("gazelle.validator.system.keyword.alreadyExists");
            }
        } else {
            throw new SystemActionException("gazelle.validator.system.keyword.alreadyExists");
        }
    }

    public static boolean validateSystemNameAndSystemVersionStatic(String currentSystemName, String version, Integer id, EntityManager
            entityManager) throws SystemActionException {
        Query query = null;
        Integer results = 0;
        List<System> listSystems = null;

        query = entityManager
                .createQuery("SELECT distinct syst FROM System syst where syst.name = :systemname and syst.version= :systversion");
        query.setParameter("systemname", currentSystemName);
        query.setParameter("systversion", version);

        listSystems = query.getResultList();
        results = listSystems.size();

        if (results == 0) {
            return true;
        } else if (results == 1) {
            if ((id != null) && (id.intValue() == listSystems.get(0).getId().intValue())) {
                // We found one user in the database with the same name, but it's
                // the current system to update > NO error
                return true;
            } else {
                // We found one user in the database with the same name, but it's
                // NOT the current system to update > ERROR
                throw new SystemActionException("gazelle.validator.system.nameAndVersion.alreadyExists");
            }
        } else {
            throw new SystemActionException("gazelle.validator.system.nameAndVersion.alreadyExists");
        }
    }

}
