package net.ihe.gazelle.tm.configurations.action;

final class LDIFTemplate {
	
	private LDIFTemplate() {
		// private constructor
	}
	
	public static final String LDIF_AETITLE = "dn: dicomAETitle=$AETITLE,cn=Unique AE Titles Registry,cn=DICOM Configuration,dc=dcm4che,dc=org\n"
			+ "dicomaetitle: $AETITLE\n"
			+ "objectclass: dicomUniqueAETitle\n\n";
	
	public static final String LDIF_DEVICE_NAME = "dn: dicomDeviceName=$SYSKEYWORD,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n"
			+ "dicomdevicename: $SYSKEYWORD\n"
			+ "dicominstalled: TRUE\n"
			+ "objectclass: dicomDevice\n"
			+ "objectclass: dcmDevice\n\n";
	
	public static final String LDIF_CONF = "dn: cn=$CNNAME,dicomDeviceName=$SYSKEYWORD,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n"
			+ "cn: $CNNAME\n"
			+ "dicomhostname: $HOSTNAME\n"
			+ "dicomport: $PORT\n"
			+ "objectclass: dicomNetworkConnection\n"
			+ "objectclass: dcmNetworkConnection\n\n";
	
	public static final String LDIF_CONF_AET = "dn: dicomAETitle=$AETITLE,dicomDeviceName=$SYSKEYWORD,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n"
			+ "dicomaetitle: $AETITLE\n"
			+ "dicomassociationacceptor: TRUE\n"
			+ "dicomassociationinitiator: TRUE\n"
			+ "$dicomnetworkconnectionreference"
			+ "objectclass: dicomNetworkAE\n"
			+ "objectclass: dcmNetworkAE\n\n";
	
	public static final String LDIF_CONF_AET_CN = "dicomnetworkconnectionreference: cn=$CNNAME,dicomDeviceName=$SYSKEYWORD,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org\n";

}
