package net.ihe.gazelle.tm.configurations.ldap.model;

import java.util.ArrayList;
import java.util.List;

public class LDAPNetworkAE {
	
	private String dicomAETitle;
	
	private String dicomDescription;
	
	private List<String> dicomApplicationCluster;
	
	private List<String> dicomPreferredCalledAETitle;
	
	private List<String> dicomPreferredCallingAETitle;
	
	private Boolean dicomAssociationAcceptor;
	
	private Boolean dicomAssociationInitiator;
	
	private List<LDAPNetworkConnection> dicomNetworkConnectionReference;
	
	private List<String> dicomSupportedCharacterSet;
	
	private Boolean dicomInstalled;
	
	private List<LDAPTransferCapability> transferCapability;

	public String getDicomAETitle() {
		return dicomAETitle;
	}

	public void setDicomAETitle(String dicomAETitle) {
		this.dicomAETitle = dicomAETitle;
	}

	public String getDicomDescription() {
		return dicomDescription;
	}

	public void setDicomDescription(String dicomDescription) {
		this.dicomDescription = dicomDescription;
	}

	public List<String> getDicomApplicationCluster() {
		if (dicomApplicationCluster == null) {
			dicomApplicationCluster = new ArrayList<>();
		}
		return dicomApplicationCluster;
	}

	public void setDicomApplicationCluster(List<String> dicomApplicationCluster) {
		this.dicomApplicationCluster = dicomApplicationCluster;
	}

	public List<String> getDicomPreferredCalledAETitle() {
		if (dicomPreferredCalledAETitle == null) {
			dicomPreferredCalledAETitle = new ArrayList<>();
		}
		return dicomPreferredCalledAETitle;
	}

	public void setDicomPreferredCalledAETitle(List<String> dicomPreferredCalledAETitle) {
		this.dicomPreferredCalledAETitle = dicomPreferredCalledAETitle;
	}

	public List<String> getDicomPreferredCallingAETitle() {
		if (dicomPreferredCallingAETitle == null) {
			dicomPreferredCallingAETitle = new ArrayList<>();
		}
		return dicomPreferredCallingAETitle;
	}

	public void setDicomPreferredCallingAETitle(List<String> dicomPreferredCallingAETitle) {
		this.dicomPreferredCallingAETitle = dicomPreferredCallingAETitle;
	}

	public Boolean getDicomAssociationAcceptor() {
		return dicomAssociationAcceptor;
	}

	public void setDicomAssociationAcceptor(Boolean dicomAssociationAcceptor) {
		this.dicomAssociationAcceptor = dicomAssociationAcceptor;
	}

	public Boolean getDicomAssociationInitiator() {
		return dicomAssociationInitiator;
	}

	public void setDicomAssociationInitiator(Boolean dicomAssociationInitiator) {
		this.dicomAssociationInitiator = dicomAssociationInitiator;
	}

	public List<LDAPNetworkConnection> getDicomNetworkConnectionReference() {
		if (dicomNetworkConnectionReference == null) {
			dicomNetworkConnectionReference = new ArrayList<>();
		}
		return dicomNetworkConnectionReference;
	}

	public void setDicomNetworkConnectionReference(List<LDAPNetworkConnection> dicomNetworkConnectionReference) {
		this.dicomNetworkConnectionReference = dicomNetworkConnectionReference;
	}

	public List<String> getDicomSupportedCharacterSet() {
		if (dicomSupportedCharacterSet == null) {
			dicomSupportedCharacterSet = new ArrayList<>();
		}
		return dicomSupportedCharacterSet;
	}

	public void setDicomSupportedCharacterSet(List<String> dicomSupportedCharacterSet) {
		this.dicomSupportedCharacterSet = dicomSupportedCharacterSet;
	}

	public Boolean getDicomInstalled() {
		return dicomInstalled;
	}

	public void setDicomInstalled(Boolean dicomInstalled) {
		this.dicomInstalled = dicomInstalled;
	}

	public List<LDAPTransferCapability> getTransferCapability() {
		if (transferCapability == null) {
			transferCapability = new ArrayList<>();
		}
		return transferCapability;
	}

	public void setTransferCapability(List<LDAPTransferCapability> transferCapability) {
		this.transferCapability = transferCapability;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dicomAETitle == null) ? 0 : dicomAETitle.hashCode());
		result = prime * result + ((dicomApplicationCluster == null) ? 0 : dicomApplicationCluster.hashCode());
		result = prime * result + ((dicomAssociationAcceptor == null) ? 0 : dicomAssociationAcceptor.hashCode());
		result = prime * result + ((dicomAssociationInitiator == null) ? 0 : dicomAssociationInitiator.hashCode());
		result = prime * result + ((dicomDescription == null) ? 0 : dicomDescription.hashCode());
		result = prime * result + ((dicomInstalled == null) ? 0 : dicomInstalled.hashCode());
		result = prime * result
				+ ((dicomNetworkConnectionReference == null) ? 0 : dicomNetworkConnectionReference.hashCode());
		result = prime * result + ((dicomPreferredCalledAETitle == null) ? 0 : dicomPreferredCalledAETitle.hashCode());
		result = prime * result
				+ ((dicomPreferredCallingAETitle == null) ? 0 : dicomPreferredCallingAETitle.hashCode());
		result = prime * result + ((dicomSupportedCharacterSet == null) ? 0 : dicomSupportedCharacterSet.hashCode());
		result = prime * result + ((transferCapability == null) ? 0 : transferCapability.hashCode());
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
		LDAPNetworkAE other = (LDAPNetworkAE) obj;
		if (dicomAETitle == null) {
			if (other.dicomAETitle != null)
				return false;
		} else if (!dicomAETitle.equals(other.dicomAETitle))
			return false;
		if (dicomApplicationCluster == null) {
			if (other.dicomApplicationCluster != null)
				return false;
		} else if (!dicomApplicationCluster.equals(other.dicomApplicationCluster))
			return false;
		if (dicomAssociationAcceptor == null) {
			if (other.dicomAssociationAcceptor != null)
				return false;
		} else if (!dicomAssociationAcceptor.equals(other.dicomAssociationAcceptor))
			return false;
		if (dicomAssociationInitiator == null) {
			if (other.dicomAssociationInitiator != null)
				return false;
		} else if (!dicomAssociationInitiator.equals(other.dicomAssociationInitiator))
			return false;
		if (dicomDescription == null) {
			if (other.dicomDescription != null)
				return false;
		} else if (!dicomDescription.equals(other.dicomDescription))
			return false;
		if (dicomInstalled == null) {
			if (other.dicomInstalled != null)
				return false;
		} else if (!dicomInstalled.equals(other.dicomInstalled))
			return false;
		if (dicomNetworkConnectionReference == null) {
			if (other.dicomNetworkConnectionReference != null)
				return false;
		} else if (!dicomNetworkConnectionReference.equals(other.dicomNetworkConnectionReference))
			return false;
		if (dicomPreferredCalledAETitle == null) {
			if (other.dicomPreferredCalledAETitle != null)
				return false;
		} else if (!dicomPreferredCalledAETitle.equals(other.dicomPreferredCalledAETitle))
			return false;
		if (dicomPreferredCallingAETitle == null) {
			if (other.dicomPreferredCallingAETitle != null)
				return false;
		} else if (!dicomPreferredCallingAETitle.equals(other.dicomPreferredCallingAETitle))
			return false;
		if (dicomSupportedCharacterSet == null) {
			if (other.dicomSupportedCharacterSet != null)
				return false;
		} else if (!dicomSupportedCharacterSet.equals(other.dicomSupportedCharacterSet))
			return false;
		if (transferCapability == null) {
			if (other.transferCapability != null)
				return false;
		} else if (!transferCapability.equals(other.transferCapability))
			return false;
		return true;
	}
	
	
	
}
