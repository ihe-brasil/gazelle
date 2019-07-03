package net.ihe.gazelle.tm.configurations.action;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.ihe.gazelle.tm.configurations.ldap.model.LDAPDevice;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPNetworkAE;
import net.ihe.gazelle.tm.configurations.ldap.model.LDAPNetworkConnection;

public final class LdifGeneratorUtil {
	
	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";

	private LdifGeneratorUtil() {
		// private constructor
	}
	
	private static final String LDIF_AETITLE = "dn: dicomAETitle=$AETITLE,cn=Unique AE Titles Registry,cn=DICOM Configuration,dc=dcm4che,dc=org\n"
			+ "dicomaetitle: $AETITLE\n"
			+ "objectclass: dicomUniqueAETitle\n\n";
	
	public static String generateLDIFAETitle(List<LDAPDevice> ldevice) {
		Set<String> setAE = new TreeSet<>();
		if (ldevice != null) {
			for (LDAPDevice ldapDevice : ldevice) {
				for (LDAPNetworkAE nae : ldapDevice.getNetworkAE()) {
					setAE.add(nae.getDicomAETitle());
				}
			}
		}
		StringBuilder res = new StringBuilder();
		for (String aet : setAE) {
			String aetstr = LDIF_AETITLE.replace("$AETITLE", aet);
			res.append(aetstr);
		}
		return res.toString();
	}
	
	public static String generateLDIFDevices(List<LDAPDevice> ldevice) {
		StringBuilder res = new StringBuilder();
		if (ldevice != null) {
			for (LDAPDevice ldapDevice : ldevice) {
				StringBuilder toadd = new StringBuilder();
				toadd.append("dn: dicomDeviceName=" + ldapDevice.getDicomDeviceName() + ",cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n");
				toadd.append("dicomDeviceName: " + ldapDevice.getDicomDeviceName() + "\n");
				processAttribute(ldapDevice.getDicomDescription(), "dicomDescription", toadd);
				processAttribute(ldapDevice.getDicomManufacturer(), "dicomManufacturer", toadd);
				processAttribute(ldapDevice.getDicomManufacturerModelName(), "dicomManufacturerModelName", toadd);
				processAttributes(ldapDevice.getDicomSoftwareVersion(), "dicomSoftwareVersion", toadd);
				processAttribute(ldapDevice.getDicomStationName(), "dicomStationName", toadd);
				processAttribute(ldapDevice.getDicomDeviceSerialNumber(), "dicomDeviceSerialNumber", toadd);
				processAttributes(ldapDevice.getDicomPrimaryDeviceType(), "dicomPrimaryDeviceType", toadd);
				processAttributes(ldapDevice.getDicomInstitutionName(), "dicomInstitutionName", toadd);
				processAttributes(ldapDevice.getDicomInstitutionAddress(), "dicomInstitutionAddress", toadd);
				processAttributes(ldapDevice.getDicomInstitutionDepartmentName(), "dicomInstitutionDepartmentName", toadd);
				processAttribute(ldapDevice.getDicomIssuerOfPatientID(), "dicomIssuerOfPatientID", toadd);
				processAttributes(ldapDevice.getDicomRelatedDeviceReference(), "dicomRelatedDeviceReference", toadd);
				processAttributes(ldapDevice.getDicomAuthorizedNodeCertificateReference(), "dicomAuthorizedNodeCertificateReference", toadd);
				processAttributes(ldapDevice.getDicomThisNodeCertificateReference(), "dicomThisNodeCertificateReference", toadd);
				processBooleanAttribute(ldapDevice.getDicomInstalled(), "dicomInstalled", toadd);

				toadd.append("objectclass: dicomDevice\n");
				toadd.append("objectclass: dcmDevice\n\n");
				res.append(toadd);
			}
		}
		return res.toString();
	}
	
	private static void processAttributes(List<String> values, String attrName, StringBuilder toadd) {
		if (!values.isEmpty()) {
			for (String value : values) {
				toadd.append(attrName + ": " + value + "\n");
			}
		}
	}

	private static void processAttribute(String value, String attrName, StringBuilder toadd) {
		if (value != null) {
			toadd.append(attrName);
			toadd.append(": ");
			toadd.append(value);
			toadd.append("\n");
		}
	}
	
	private static void processBooleanAttribute(Boolean value, String attrName, StringBuilder toadd) {
		if (value != null) {
			toadd.append(attrName);
			toadd.append(": ");
			toadd.append(value?TRUE:FALSE);
			toadd.append("\n");
		}
	}
	
	public static String generateLDIFNetworkConnection(List<LDAPDevice> ldevice) {
		StringBuilder res = new StringBuilder();
		if (ldevice != null) {
			for (LDAPDevice ldapDevice : ldevice) {
				for (LDAPNetworkConnection nc : ldapDevice.getNetworkConnection()) {
					StringBuilder toadd = new StringBuilder("dn: cn=" + nc.getCn() + ",dicomDeviceName=" + ldapDevice.getDicomDeviceName() + 
							",cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n");
					toadd.append("cn: " + nc.getCn() + "\n");
					processAttribute(nc.getDicomHostname(), "dicomHostname", toadd);
					processAttribute(nc.getDicomPort(), "dicomPort", toadd);
					processAttributes(nc.getDicomTLSCipherSuite(), "dicomTLSCipherSuite", toadd);
					processBooleanAttribute(nc.getDicomInstalled(), "dicomInstalled", toadd);
					toadd.append("objectclass: dicomNetworkConnection\n");
					toadd.append("objectclass: dcmNetworkConnection\n\n");
					res.append(toadd);
				}
			}
		}
		return res.toString();
	}
	
	public static String generateLDIFNetworkAE(List<LDAPDevice> ldevice) {
		StringBuilder res = new StringBuilder();
		if (ldevice != null) {
			for (LDAPDevice ldapDevice : ldevice) {
				for (LDAPNetworkAE nae : ldapDevice.getNetworkAE()) {
					StringBuilder toadd = new StringBuilder("dn: dicomAETitle=" + nae.getDicomAETitle() + ",dicomDeviceName=" + ldapDevice.getDicomDeviceName() + 
							",cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n");
					toadd.append("dicomAETitle: " + nae.getDicomAETitle() + "\n");
					processAttribute(nae.getDicomDescription(), "dicomDescription", toadd);
					processAttributes(nae.getDicomApplicationCluster(), "dicomApplicationCluster", toadd);
					processAttributes(nae.getDicomPreferredCalledAETitle(), "dicomPreferredCalledAETitle", toadd);
					processAttributes(nae.getDicomPreferredCallingAETitle(), "dicomPreferredCallingAETitle", toadd);
					processBooleanAttribute(nae.getDicomAssociationAcceptor(), "dicomAssociationAcceptor", toadd);
					processBooleanAttribute(nae.getDicomAssociationInitiator(), "dicomAssociationInitiator", toadd);
					
					if (nae.getDicomNetworkConnectionReference() != null) {
						for (LDAPNetworkConnection nc : nae.getDicomNetworkConnectionReference()) {
							toadd.append("dicomNetworkConnectionReference: cn=" + nc.getCn() + ",dicomDeviceName=" + 
								 	ldapDevice.getDicomDeviceName() + ",cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n");
						}
					}
					
					processAttributes(nae.getDicomSupportedCharacterSet(), "dicomSupportedCharacterSet", toadd);
					processBooleanAttribute(nae.getDicomInstalled(), "dicomInstalled", toadd);
					toadd.append("objectclass: dicomNetworkAE\n");
					toadd.append("objectclass: dcmNetworkAE\n\n");
					res.append(toadd);
				}
			}
		}
		return res.toString();
	}

}
