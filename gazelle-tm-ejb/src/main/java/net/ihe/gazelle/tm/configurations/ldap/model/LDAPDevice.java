package net.ihe.gazelle.tm.configurations.ldap.model;

import java.util.ArrayList;
import java.util.List;

public class LDAPDevice {
	
	private String dicomDeviceName;
	
	private String dicomDescription;
	
	private String dicomManufacturer;
	
	private String dicomManufacturerModelName;
	
	private List<String> dicomSoftwareVersion;
	
	private String dicomStationName;
	
	private String dicomDeviceSerialNumber;
	
	private List<String> dicomPrimaryDeviceType;
	
	private List<String> dicomInstitutionName;
	
	private List<String> dicomInstitutionAddress;
	
	private List<String> dicomInstitutionDepartmentName;
	
	private String dicomIssuerOfPatientID;
	
	private List<String> dicomRelatedDeviceReference;
	
	private List<String> dicomAuthorizedNodeCertificateReference;
	
	private List<String> dicomThisNodeCertificateReference;
	
	private Boolean dicomInstalled;
	
	private List<LDAPNetworkConnection> networkConnection;
	
	private List<LDAPNetworkAE> networkAE;

	public String getDicomDeviceName() {
		return dicomDeviceName;
	}

	public void setDicomDeviceName(String dicomDeviceName) {
		this.dicomDeviceName = dicomDeviceName;
	}

	public String getDicomDescription() {
		return dicomDescription;
	}

	public void setDicomDescription(String dicomDescription) {
		this.dicomDescription = dicomDescription;
	}

	public String getDicomManufacturer() {
		return dicomManufacturer;
	}

	public void setDicomManufacturer(String dicomManufacturer) {
		this.dicomManufacturer = dicomManufacturer;
	}

	public String getDicomManufacturerModelName() {
		return dicomManufacturerModelName;
	}

	public void setDicomManufacturerModelName(String dicomManufacturerModelName) {
		this.dicomManufacturerModelName = dicomManufacturerModelName;
	}

	public List<String> getDicomSoftwareVersion() {
		if (dicomSoftwareVersion == null) {
			dicomSoftwareVersion = new ArrayList<>();
		}
		return dicomSoftwareVersion;
	}

	public void setDicomSoftwareVersion(List<String> dicomSoftwareVersion) {
		this.dicomSoftwareVersion = dicomSoftwareVersion;
	}

	public String getDicomStationName() {
		return dicomStationName;
	}

	public void setDicomStationName(String dicomStationName) {
		this.dicomStationName = dicomStationName;
	}

	public String getDicomDeviceSerialNumber() {
		return dicomDeviceSerialNumber;
	}

	public void setDicomDeviceSerialNumber(String dicomDeviceSerialNumber) {
		this.dicomDeviceSerialNumber = dicomDeviceSerialNumber;
	}

	public List<String> getDicomPrimaryDeviceType() {
		if (dicomPrimaryDeviceType == null) {
			dicomPrimaryDeviceType = new ArrayList<>();
		}
		return dicomPrimaryDeviceType;
	}

	public void setDicomPrimaryDeviceType(List<String> dicomPrimaryDeviceType) {
		this.dicomPrimaryDeviceType = dicomPrimaryDeviceType;
	}

	public List<String> getDicomInstitutionName() {
		if (dicomInstitutionName == null) {
			dicomInstitutionName = new ArrayList<>();
		}
		return dicomInstitutionName;
	}

	public void setDicomInstitutionName(List<String> dicomInstitutionName) {
		this.dicomInstitutionName = dicomInstitutionName;
	}

	public List<String> getDicomInstitutionAddress() {
		if (dicomInstitutionAddress == null) {
			dicomInstitutionAddress = new ArrayList<>();
		}
		return dicomInstitutionAddress;
	}

	public void setDicomInstitutionAddress(List<String> dicomInstitutionAddress) {
		this.dicomInstitutionAddress = dicomInstitutionAddress;
	}

	public List<String> getDicomInstitutionDepartmentName() {
		if (dicomInstitutionDepartmentName == null) {
			dicomInstitutionDepartmentName = new ArrayList<>();
		}
		return dicomInstitutionDepartmentName;
	}

	public void setDicomInstitutionDepartmentName(List<String> dicomInstitutionDepartmentName) {
		this.dicomInstitutionDepartmentName = dicomInstitutionDepartmentName;
	}

	public String getDicomIssuerOfPatientID() {
		return dicomIssuerOfPatientID;
	}

	public void setDicomIssuerOfPatientID(String dicomIssuerOfPatientID) {
		this.dicomIssuerOfPatientID = dicomIssuerOfPatientID;
	}

	public List<String> getDicomRelatedDeviceReference() {
		if (dicomRelatedDeviceReference == null) {
			dicomRelatedDeviceReference = new ArrayList<>();
		}
		return dicomRelatedDeviceReference;
	}

	public void setDicomRelatedDeviceReference(List<String> dicomRelatedDeviceReference) {
		this.dicomRelatedDeviceReference = dicomRelatedDeviceReference;
	}

	public List<String> getDicomAuthorizedNodeCertificateReference() {
		if (dicomAuthorizedNodeCertificateReference == null) {
			dicomAuthorizedNodeCertificateReference = new ArrayList<>();
		}
		return dicomAuthorizedNodeCertificateReference;
	}

	public void setDicomAuthorizedNodeCertificateReference(List<String> dicomAuthorizedNodeCertificateReference) {
		this.dicomAuthorizedNodeCertificateReference = dicomAuthorizedNodeCertificateReference;
	}

	public List<String> getDicomThisNodeCertificateReference() {
		if (dicomThisNodeCertificateReference == null) {
			dicomThisNodeCertificateReference = new ArrayList<>();
		}
		return dicomThisNodeCertificateReference;
	}

	public void setDicomThisNodeCertificateReference(List<String> dicomThisNodeCertificateReference) {
		this.dicomThisNodeCertificateReference = dicomThisNodeCertificateReference;
	}

	public Boolean getDicomInstalled() {
		return dicomInstalled;
	}

	public void setDicomInstalled(Boolean dicomInstalled) {
		this.dicomInstalled = dicomInstalled;
	}

	public List<LDAPNetworkConnection> getNetworkConnection() {
		if (networkConnection == null) {
			networkConnection = new ArrayList<>();
		}
		return networkConnection;
	}

	public void setNetworkConnection(List<LDAPNetworkConnection> networkConnection) {
		this.networkConnection = networkConnection;
	}

	public List<LDAPNetworkAE> getNetworkAE() {
		if (networkAE == null) {
			networkAE = new ArrayList<>();
		}
		return networkAE;
	}

	public void setNetworkAE(List<LDAPNetworkAE> networkAE) {
		this.networkAE = networkAE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dicomAuthorizedNodeCertificateReference == null) ? 0
				: dicomAuthorizedNodeCertificateReference.hashCode());
		result = prime * result + ((dicomDescription == null) ? 0 : dicomDescription.hashCode());
		result = prime * result + ((dicomDeviceName == null) ? 0 : dicomDeviceName.hashCode());
		result = prime * result + ((dicomDeviceSerialNumber == null) ? 0 : dicomDeviceSerialNumber.hashCode());
		result = prime * result + ((dicomInstalled == null) ? 0 : dicomInstalled.hashCode());
		result = prime * result + ((dicomInstitutionAddress == null) ? 0 : dicomInstitutionAddress.hashCode());
		result = prime * result
				+ ((dicomInstitutionDepartmentName == null) ? 0 : dicomInstitutionDepartmentName.hashCode());
		result = prime * result + ((dicomInstitutionName == null) ? 0 : dicomInstitutionName.hashCode());
		result = prime * result + ((dicomIssuerOfPatientID == null) ? 0 : dicomIssuerOfPatientID.hashCode());
		result = prime * result + ((dicomManufacturer == null) ? 0 : dicomManufacturer.hashCode());
		result = prime * result + ((dicomManufacturerModelName == null) ? 0 : dicomManufacturerModelName.hashCode());
		result = prime * result + ((dicomPrimaryDeviceType == null) ? 0 : dicomPrimaryDeviceType.hashCode());
		result = prime * result + ((dicomRelatedDeviceReference == null) ? 0 : dicomRelatedDeviceReference.hashCode());
		result = prime * result + ((dicomSoftwareVersion == null) ? 0 : dicomSoftwareVersion.hashCode());
		result = prime * result + ((dicomStationName == null) ? 0 : dicomStationName.hashCode());
		result = prime * result
				+ ((dicomThisNodeCertificateReference == null) ? 0 : dicomThisNodeCertificateReference.hashCode());
		result = prime * result + ((networkAE == null) ? 0 : networkAE.hashCode());
		result = prime * result + ((networkConnection == null) ? 0 : networkConnection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LDAPDevice other = (LDAPDevice) obj;
		if (dicomAuthorizedNodeCertificateReference == null) {
			if (other.dicomAuthorizedNodeCertificateReference != null)
				return false;
		} else if (!dicomAuthorizedNodeCertificateReference.equals(other.dicomAuthorizedNodeCertificateReference))
			return false;
		if (dicomDescription == null) {
			if (other.dicomDescription != null)
				return false;
		} else if (!dicomDescription.equals(other.dicomDescription))
			return false;
		if (dicomDeviceName == null) {
			if (other.dicomDeviceName != null)
				return false;
		} else if (!dicomDeviceName.equals(other.dicomDeviceName))
			return false;
		if (dicomDeviceSerialNumber == null) {
			if (other.dicomDeviceSerialNumber != null)
				return false;
		} else if (!dicomDeviceSerialNumber.equals(other.dicomDeviceSerialNumber))
			return false;
		if (dicomInstalled == null) {
			if (other.dicomInstalled != null)
				return false;
		} else if (!dicomInstalled.equals(other.dicomInstalled))
			return false;
		if (dicomInstitutionAddress == null) {
			if (other.dicomInstitutionAddress != null)
				return false;
		} else if (!dicomInstitutionAddress.equals(other.dicomInstitutionAddress))
			return false;
		if (dicomInstitutionDepartmentName == null) {
			if (other.dicomInstitutionDepartmentName != null)
				return false;
		} else if (!dicomInstitutionDepartmentName.equals(other.dicomInstitutionDepartmentName))
			return false;
		if (dicomInstitutionName == null) {
			if (other.dicomInstitutionName != null)
				return false;
		} else if (!dicomInstitutionName.equals(other.dicomInstitutionName))
			return false;
		if (dicomIssuerOfPatientID == null) {
			if (other.dicomIssuerOfPatientID != null)
				return false;
		} else if (!dicomIssuerOfPatientID.equals(other.dicomIssuerOfPatientID))
			return false;
		if (dicomManufacturer == null) {
			if (other.dicomManufacturer != null)
				return false;
		} else if (!dicomManufacturer.equals(other.dicomManufacturer))
			return false;
		if (dicomManufacturerModelName == null) {
			if (other.dicomManufacturerModelName != null)
				return false;
		} else if (!dicomManufacturerModelName.equals(other.dicomManufacturerModelName))
			return false;
		if (dicomPrimaryDeviceType == null) {
			if (other.dicomPrimaryDeviceType != null)
				return false;
		} else if (!dicomPrimaryDeviceType.equals(other.dicomPrimaryDeviceType))
			return false;
		if (dicomRelatedDeviceReference == null) {
			if (other.dicomRelatedDeviceReference != null)
				return false;
		} else if (!dicomRelatedDeviceReference.equals(other.dicomRelatedDeviceReference))
			return false;
		if (dicomSoftwareVersion == null) {
			if (other.dicomSoftwareVersion != null)
				return false;
		} else if (!dicomSoftwareVersion.equals(other.dicomSoftwareVersion))
			return false;
		if (dicomStationName == null) {
			if (other.dicomStationName != null)
				return false;
		} else if (!dicomStationName.equals(other.dicomStationName))
			return false;
		if (dicomThisNodeCertificateReference == null) {
			if (other.dicomThisNodeCertificateReference != null)
				return false;
		} else if (!dicomThisNodeCertificateReference.equals(other.dicomThisNodeCertificateReference))
			return false;
		if (networkAE == null) {
			if (other.networkAE != null)
				return false;
		} else if (!networkAE.equals(other.networkAE))
			return false;
		if (networkConnection == null) {
			if (other.networkConnection != null)
				return false;
		} else if (!networkConnection.equals(other.networkConnection))
			return false;
		return true;
	}
	
	

}
