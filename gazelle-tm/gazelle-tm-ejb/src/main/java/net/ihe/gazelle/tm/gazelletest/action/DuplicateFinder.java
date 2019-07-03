/*
 * Copyright 2016 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.tm.gazelletest.bean.RoleInTestSystemInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>Class Description : </b>DuplicateFinder<br>
 * <br>
 *
 * @author Anne-Gaelle Berge / IHE-Europe development Project
 * @version 1.0 - 23/12/16
 * @class DuplicateFinder
 * @package net.ihe.gazelle.tm.gazelletest.action
 * @see anne-gaelle.berge@ihe-europe.net - http://gazelle.ihe.net
 */
public class DuplicateFinder {

    private DuplicateFinder() {

    }

    public static void findDuplicates(TestInstance selectedTestInstance, List<RoleInTestSystemInSession> rolesInTest, FacesMessages facesMessages) {
        selectedTestInstance.setContainsSameSystemsOrFromSameCompany(false);
        findDuplicatedCompany(selectedTestInstance, rolesInTest);
        findDuplicatesSystemInSession(selectedTestInstance, rolesInTest);
        if (!selectedTestInstance.getDuplicateSystems().isEmpty() || !selectedTestInstance.getDuplicateCompanies().isEmpty()) {
            selectedTestInstance.setContainsSameSystemsOrFromSameCompany(true);
            if (facesMessages != null) {
                for (SystemInSession systemInSession : selectedTestInstance.getDuplicateSystems()) {
                    FacesMessages.instance().add(StatusMessage.Severity.WARN,
                            "You have the same system for multiple roles: " + systemInSession.getSystem().getKeyword());
                }
                for (Institution institution : selectedTestInstance.getDuplicateCompanies()) {
                    facesMessages.add(StatusMessage.Severity.WARN, "Several systems from the same company are selected for this test: " +
                            institution.getName());
                }
            }
        }
    }

    private static void findDuplicatedCompany(TestInstance selectedTestInstance, List<RoleInTestSystemInSession> rolesInTest) {
        List<Institution> listCompaniesForTestInstance = new ArrayList<>();
        for (RoleInTestSystemInSession roleInTestSystemInSession : rolesInTest) {
            SystemInSession sis = roleInTestSystemInSession.getSystemInSession();
            if (!sis.getSystem().getIsTool()) {
                List<Institution> institutions = System.getInstitutionsForASystem(sis.getSystem());
                listCompaniesForTestInstance.addAll(institutions);
            }
        }
        selectedTestInstance.setDuplicateCompanies(new HashSet<Institution>());
        final Set<Institution> setForTest = new HashSet<>();
        for (Institution institution : listCompaniesForTestInstance) {
            if (!setForTest.add(institution)) {
                selectedTestInstance.getDuplicateCompanies().add(institution);
            }
        }
    }

    private static void findDuplicatesSystemInSession(TestInstance selectedTestInstance, List<RoleInTestSystemInSession> rolesInTest) {
        List<SystemInSession> listContainingDuplicates = new ArrayList<>();
        for (RoleInTestSystemInSession roleInTestSystemInSession : rolesInTest) {
            if (!roleInTestSystemInSession.getSystemInSession().getSystem().getIsTool()) {
                listContainingDuplicates.add(roleInTestSystemInSession.getSystemInSession());
            }
        }
        selectedTestInstance.setDuplicateSystems(new HashSet<SystemInSession>());
        final Set<SystemInSession> set1 = new HashSet();
        for (SystemInSession systemInSession : listContainingDuplicates) {
            if (!set1.add(systemInSession)) {
                selectedTestInstance.getDuplicateSystems().add(systemInSession);
            }
        }
    }

    public static void findDuplicates(TestInstance selectedTestInstance) {
        if (selectedTestInstance.isContainsSameSystemsOrFromSameCompany() && selectedTestInstance.getTestInstanceParticipants() != null) {
            selectedTestInstance.setDuplicateCompanies(new HashSet<Institution>());
            selectedTestInstance.setDuplicateSystems(new HashSet<SystemInSession>());
            Set<Institution> setForInstitution = new HashSet<Institution>();
            Set<SystemInSession> setForSystem = new HashSet<SystemInSession>();
            for (TestInstanceParticipants participant : selectedTestInstance.getTestInstanceParticipants()) {
                SystemInSession currentSIS = participant.getSystemInSessionUser().getSystemInSession();
                if (!setForSystem.add(currentSIS)) {
                    selectedTestInstance.getDuplicateSystems().add(currentSIS);
                }
                List<Institution> institutionsForSIS = System.getInstitutionsForASystem(currentSIS.getSystem());
                for (Institution currentInstitution : institutionsForSIS) {
                    if (!setForInstitution.add(currentInstitution)) {
                        selectedTestInstance.getDuplicateCompanies().add(currentInstitution);
                    }
                }
            }
        } else {
            return;
        }
    }
}
