package net.ihe.gazelle.tm.configurations.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.ihe.gazelle.common.model.ApplicationPreference;
import net.ihe.gazelle.tm.configurations.model.DICOM.AbstractDicomConfiguration;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.users.model.Address;
import net.ihe.gazelle.users.model.Institution;

public final class AbstractDicomConfigurationUtil {
	
	private AbstractDicomConfigurationUtil() {
		// private constructor
	}

	public static String extractDeviceName(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getSystem() != null) {
			return gazelleDicomConf.getConfiguration().getSystemInSession().getSystem().getKeyword();
		}
		return null;
	}

	public static List<String> extractInstitutionAddresses(AbstractDicomConfiguration gazelleDicomConf) {
		List<String> res = new ArrayList<>();
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getSystem() != null) {
			List<Institution> listinst = System.getInstitutionsForASystem(gazelleDicomConf.getConfiguration().getSystemInSession().getSystem());
			if (listinst != null) {
				for (Institution institution : listinst) {
					for (Address addr : institution.getAddresses()) {
						String addrrv2 = StringUtils.trimToEmpty(addr.getAddress().trim().replaceAll("\\s+", " ")) + "^^" + 
								StringUtils.trimToEmpty(addr.getCity().trim()) + "^" + 
								StringUtils.trimToEmpty(addr.getState().trim()) + "^" + 
								StringUtils.trimToEmpty(addr.getZipCode().trim()) + "^" + 
								StringUtils.trimToEmpty(addr.getIso3166CountryCode().getIso3()) + "^" + 
								StringUtils.trimToEmpty(addr.getAddressType());
						res.add(addrrv2);
					}
				}
			}
		}
		return res;
	}
	
	public static List<String> extractInstitutionNames(AbstractDicomConfiguration gazelleDicomConf) {
		List<String> res = new ArrayList<>();
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getSystem() != null) {
			List<Institution> listinst = System.getInstitutionsForASystem(gazelleDicomConf.getConfiguration().getSystemInSession().getSystem());
			if (listinst != null) {
				for (Institution institution : listinst) {
					res.add(institution.getName());
				}
			}
		}
		return res;
	}

	public static String extractSystemVersion(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getSystem() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getSystem().getVersion() != null && 
				!gazelleDicomConf.getConfiguration().getSystemInSession().getSystem().getVersion().trim().isEmpty()) {
			return gazelleDicomConf.getConfiguration().getSystemInSession().getSystem().getVersion();
		}
		return null;
	}

	public static String extractGazelleOID() {
		return ApplicationPreference.get_preference_from_db("app_instance_oid").getPreferenceValue();
	}

	public static String extractTestingSessionName(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession() != null && 
				gazelleDicomConf.getConfiguration().getSystemInSession().getTestingSession() != null ) {
			return gazelleDicomConf.getConfiguration().getSystemInSession().getTestingSession().getDescription();
		}
		return null;
	}

	public static String extractPort(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null && gazelleDicomConf.getPort() != null) {
			return gazelleDicomConf.getPort().toString();
		}
		return null;
	}

	public static String extractHostName(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null && gazelleDicomConf.getConfiguration() != null && 
				gazelleDicomConf.getConfiguration().getHost() != null) {
			return gazelleDicomConf.getConfiguration().getHost().getHostname();
		}
		return null;
	}

	public static String extractAETitle(AbstractDicomConfiguration gazelleDicomConf) {
		if (gazelleDicomConf != null) {
			return gazelleDicomConf.getAeTitle();
		}
		return null;
	}

}
