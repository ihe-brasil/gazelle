/*
 * Copyright 2009 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
 */
package net.ihe.gazelle.tm.configurations.action;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.slf4j.LoggerFactory;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPDevice;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPNetworkAE;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPNetworkConnection;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPTransferCapability;
import net.ihe.gazelle.tm.configurations.model.DICOM.AbstractDicomConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.AbstractDicomConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfigurationQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;

@Name("ldifGenerator")
@GenerateInterface("LdifGeneratorLocal")
@Stateless
public class LdifGenerator implements Serializable, LdifGeneratorLocal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String SESSION_ID = "sessionId";

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LdifGenerator.class);
	
	private static final String LDIF_FILENAME = "confs.ldif";
	
	@Override
    public void generateLDIFFile() {
        Integer sessionId;
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if ((params.get(SESSION_ID) == null) || (params.get(SESSION_ID).isEmpty())) {
            sessionId = TestingSession.getSelectedTestingSession().getId();
        } else {
            sessionId = Integer.parseInt(params.get(SESSION_ID));
        }
        TestingSession ts = TestingSession.GetSessionById(sessionId);
        if (ts == null) {
            LOG.error("The provided sessionId is invalid.");
        } else {
        	StringBuilder ldifContent = createLDIFFile(sessionId);
            ReportExporterManager.exportToFile("text/plain", ldifContent.toString().getBytes(StandardCharsets.UTF_8), LDIF_FILENAME, false);
        }
    }
	
	@Override
    public void generateLDIFFile(List<AbstractDicomConfiguration> listconf) {
        if (listconf == null) {
            LOG.error("The provided sessionId is invalid.");
        } else {
        	StringBuilder ldifContent = createLDIFFileForListConf(listconf);
            ReportExporterManager.exportToFile("text/plain", ldifContent.toString().getBytes(StandardCharsets.UTF_8), LDIF_FILENAME, false);
        }
    }



	protected StringBuilder createLDIFFile(Integer sessionId) {
		List<AbstractDicomConfiguration> listconf = extractListDicomSCPConf(sessionId);
		return createLDIFFileForListConf(listconf);
	}



	private StringBuilder createLDIFFileForListConf(List<AbstractDicomConfiguration> listconf) {
		StringBuilder ldifContent = new StringBuilder();
		List<LDAPDevice> ldc = generateDicomConfList(listconf);
		ldifContent.append(LdifGeneratorUtil.generateLDIFAETitle(ldc));
		ldifContent.append(LdifGeneratorUtil.generateLDIFDevices(ldc));
		ldifContent.append(LdifGeneratorUtil.generateLDIFNetworkConnection(ldc));
		ldifContent.append(LdifGeneratorUtil.generateLDIFNetworkAE(ldc));
		return ldifContent;
	}
	
	private List<LDAPDevice> generateDicomConfList(List<AbstractDicomConfiguration> listconf) {
		List<LDAPDevice> res = new ArrayList<>();
		for (AbstractDicomConfiguration gazelleDicomConf : listconf) {
			generateLDAPDevice(gazelleDicomConf, res);
		}
		return res;
	}



	private void generateLDAPDevice(AbstractDicomConfiguration gazelleDicomConf, List<LDAPDevice> res) {
		if (gazelleDicomConf != null) {
			LDAPDevice device = extractLDAPDevice(res, gazelleDicomConf);
			if (device == null) {
				device = generateDevice(gazelleDicomConf);
				res.add(device);
			}
			updateDeviceNetworkConnection(device, gazelleDicomConf);
			updateDeviceNetworkAE(device, gazelleDicomConf);
		}
	}



	private void updateDeviceNetworkAE(LDAPDevice device, AbstractDicomConfiguration gazelleDicomConf) {
		String aet = AbstractDicomConfigurationUtil.extractAETitle(gazelleDicomConf);
		if (device != null && aet != null) {
			LDAPNetworkAE nae = extractNetworkAE(device, aet);
			if (nae == null) {
				nae = generateNetworkAE(gazelleDicomConf);
				device.getNetworkAE().add(nae);
			}
			updateNetworkAEReferences(device, nae, gazelleDicomConf);
			updateNetworkAETransferCapability(nae, gazelleDicomConf);
		}
	}


	private void updateNetworkAETransferCapability(LDAPNetworkAE nae, AbstractDicomConfiguration gazelleDicomConf) {
		// nothing todo, the information under gazelle are not sufficient
	}

	/**
	 * update the attributes: dicomAssociationAcceptor and dicomNetworkConnectionReference
	 * @param device
	 * @param nae
	 * @param gazelleDicomConf
	 */
	private void updateNetworkAEReferences(LDAPDevice device, LDAPNetworkAE nae, AbstractDicomConfiguration gazelleDicomConf) {
		if (device == null || nae == null || gazelleDicomConf == null) {
			return;
		}
		if (AbstractDicomConfigurationUtil.extractPort(gazelleDicomConf) != null) {
			nae.setDicomAssociationAcceptor(true);
		}
		String dicomPort = AbstractDicomConfigurationUtil.extractPort(gazelleDicomConf);
		String dicomHostname = AbstractDicomConfigurationUtil.extractHostName(gazelleDicomConf);
		LDAPNetworkConnection nc = extractNetworkConnection(device, dicomPort, dicomHostname);
		if (nc != null && !nae.getDicomNetworkConnectionReference().contains(nc)) {
			nae.getDicomNetworkConnectionReference().add(nc);
		}
	}



	private LDAPNetworkAE generateNetworkAE(AbstractDicomConfiguration gazelleDicomConf) {
		LDAPNetworkAE nae = new LDAPNetworkAE();
		nae.setDicomAETitle(AbstractDicomConfigurationUtil.extractAETitle(gazelleDicomConf));
		nae.setDicomAssociationInitiator(true);
		nae.setDicomAssociationAcceptor(false);
		return nae;
	}



	private LDAPNetworkAE extractNetworkAE(LDAPDevice device, String aet) {
		if (device != null) {
			for (LDAPNetworkAE nae : device.getNetworkAE()) {
				if (nae.getDicomAETitle() != null && nae.getDicomAETitle().equals(aet)) {
					return nae;
				}
			}
		}
		return null;
	}



	private void updateDeviceNetworkConnection(LDAPDevice device, AbstractDicomConfiguration gazelleDicomConf) {
		if (device != null) {
			String dicomPort = AbstractDicomConfigurationUtil.extractPort(gazelleDicomConf);
			String dicomHostname = AbstractDicomConfigurationUtil.extractHostName(gazelleDicomConf);
			LDAPNetworkConnection nc = extractNetworkConnection(device, dicomPort, dicomHostname);
			if (nc == null) {
				nc = generateNetworkConnection(device, dicomPort, dicomHostname);
				device.getNetworkConnection().add(nc);
			}
		}
	}



	private LDAPNetworkConnection generateNetworkConnection(LDAPDevice device, String dicomPort,
			String dicomHostname) {
		LDAPNetworkConnection nc = new LDAPNetworkConnection();
		nc.setCn(generateCnForNetworkConnection(device));
		nc.setDicomHostname(dicomHostname);
		nc.setDicomPort(dicomPort);
		return nc;
	}



	private String generateCnForNetworkConnection(LDAPDevice device) {
		if (device != null && device.getDicomDeviceName() != null) {
			int size = device.getNetworkConnection().size();
			return device.getDicomDeviceName() + (size + 1);
		}
		return null;
	}



	private LDAPNetworkConnection extractNetworkConnection(LDAPDevice device, String dicomPort, String dicomHostname) {
		if (device != null) {
			for (LDAPNetworkConnection nc : device.getNetworkConnection()) {
				if (StringUtils.equals(dicomHostname, nc.getDicomHostname()) && 
						StringUtils.equals(dicomPort, nc.getDicomPort())) {
					return nc;
				}
			}
		}
		return null;
	}



	private LDAPDevice generateDevice(AbstractDicomConfiguration gazelleDicomConf) {
		LDAPDevice device;
		device = new LDAPDevice();
		device.setDicomDeviceName(AbstractDicomConfigurationUtil.extractDeviceName(gazelleDicomConf));
		device.setDicomInstalled(true);
		device.setDicomInstitutionAddress(AbstractDicomConfigurationUtil.extractInstitutionAddresses(gazelleDicomConf));
		device.setDicomInstitutionName(AbstractDicomConfigurationUtil.extractInstitutionNames(gazelleDicomConf));
		device.getDicomSoftwareVersion().add(AbstractDicomConfigurationUtil.extractSystemVersion(gazelleDicomConf));
		device.setDicomDescription("Imported From Gazelle");
		return device;
	}



	private LDAPDevice extractLDAPDevice(List<LDAPDevice> res, AbstractDicomConfiguration gazelleDicomConf) {
		String deviceName = AbstractDicomConfigurationUtil.extractDeviceName(gazelleDicomConf);
		for (LDAPDevice ldapDevice : res) {
			if (ldapDevice.getDicomDeviceName().equals(deviceName)) {
				return ldapDevice;
			}
		}
		return null;
	}

	protected List<AbstractDicomConfiguration> extractListDicomSCPConf(Integer sessionId) {
		List<AbstractDicomConfiguration> res = new ArrayList<>();
		res.addAll(extractListDicomSCPConfList1(sessionId));
		res.addAll(extractListDicomSCPConfList2(sessionId));
		return res;
	}

	protected List<DicomSCUConfiguration> extractListDicomSCPConfList1(Integer sessionId) {
		DicomSCUConfigurationQuery qr = new DicomSCUConfigurationQuery();
		qr.configuration().isApproved().eq(true);
		qr.configuration().isSecured().eq(false);
		qr.configuration().systemInSession().testingSession().id().eq(sessionId);
		qr.sopClass().keyword().eq("STORAGE COMMITMENT");
		qr.configuration().actor().keyword().in(Arrays.asList("MOD", "EC", "IMPORTER", "RM"));
		return qr.getListDistinct();
	}
	
	protected List<DicomSCPConfiguration> extractListDicomSCPConfList2(Integer sessionId) {
		DicomSCPConfigurationQuery qr = new DicomSCPConfigurationQuery();
		qr.configuration().isApproved().eq(true);
		qr.configuration().isSecured().eq(false);
		qr.configuration().systemInSession().testingSession().id().eq(sessionId);
		qr.sopClass().keyword().eq("STORAGE");
		qr.configuration().actor().keyword().in(Arrays.asList("IM", "RRD", "DOSE_INFO_CONSUMER", "ID", "IM_Model_I", "IM_Model_III"));
		return qr.getListDistinct();
	}






}
