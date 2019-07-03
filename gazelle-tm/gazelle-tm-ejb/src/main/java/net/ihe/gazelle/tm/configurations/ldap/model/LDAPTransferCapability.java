package net.ihe.gazelle.tm.configurations.ldap.model;

import java.util.ArrayList;
import java.util.List;

public class LDAPTransferCapability {
	
	private String cn;
	
	private String dicomSOPClass;
	
	private String dicomTransferRole;
	
	private List<String> dicomTransferSyntax;

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getDicomSOPClass() {
		return dicomSOPClass;
	}

	public void setDicomSOPClass(String dicomSOPClass) {
		this.dicomSOPClass = dicomSOPClass;
	}

	public String getDicomTransferRole() {
		return dicomTransferRole;
	}

	public void setDicomTransferRole(String dicomTransferRole) {
		this.dicomTransferRole = dicomTransferRole;
	}

	public List<String> getDicomTransferSyntax() {
		if (dicomTransferSyntax == null) {
			dicomTransferSyntax = new ArrayList<>();
		}
		return dicomTransferSyntax;
	}

	public void setDicomTransferSyntax(List<String> dicomTransferSyntax) {
		this.dicomTransferSyntax = dicomTransferSyntax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cn == null) ? 0 : cn.hashCode());
		result = prime * result + ((dicomSOPClass == null) ? 0 : dicomSOPClass.hashCode());
		result = prime * result + ((dicomTransferRole == null) ? 0 : dicomTransferRole.hashCode());
		result = prime * result + ((dicomTransferSyntax == null) ? 0 : dicomTransferSyntax.hashCode());
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
		LDAPTransferCapability other = (LDAPTransferCapability) obj;
		if (cn == null) {
			if (other.cn != null)
				return false;
		} else if (!cn.equals(other.cn))
			return false;
		if (dicomSOPClass == null) {
			if (other.dicomSOPClass != null)
				return false;
		} else if (!dicomSOPClass.equals(other.dicomSOPClass))
			return false;
		if (dicomTransferRole == null) {
			if (other.dicomTransferRole != null)
				return false;
		} else if (!dicomTransferRole.equals(other.dicomTransferRole))
			return false;
		if (dicomTransferSyntax == null) {
			if (other.dicomTransferSyntax != null)
				return false;
		} else if (!dicomTransferSyntax.equals(other.dicomTransferSyntax))
			return false;
		return true;
	}
	
}
