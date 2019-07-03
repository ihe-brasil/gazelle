package net.ihe.gazelle.tm.configurations.ldap.model;

import java.util.ArrayList;
import java.util.List;

public class LDAPNetworkConnection {
	
	private String cn;
	
	private String dicomPort;
	
	private String dicomHostname;
	
	private List<String> dicomTLSCipherSuite;
	
	private Boolean dicomInstalled;

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getDicomPort() {
		return dicomPort;
	}

	public void setDicomPort(String dicomPort) {
		this.dicomPort = dicomPort;
	}

	public String getDicomHostname() {
		return dicomHostname;
	}

	public void setDicomHostname(String dicomHostname) {
		this.dicomHostname = dicomHostname;
	}

	public List<String> getDicomTLSCipherSuite() {
		if (dicomTLSCipherSuite == null) {
			dicomTLSCipherSuite = new ArrayList<>();
		}
		return dicomTLSCipherSuite;
	}

	public void setDicomTLSCipherSuite(List<String> dicomTLSCipherSuite) {
		this.dicomTLSCipherSuite = dicomTLSCipherSuite;
	}

	public Boolean getDicomInstalled() {
		return dicomInstalled;
	}

	public void setDicomInstalled(Boolean dicomInstalled) {
		this.dicomInstalled = dicomInstalled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cn == null) ? 0 : cn.hashCode());
		result = prime * result + ((dicomHostname == null) ? 0 : dicomHostname.hashCode());
		result = prime * result + ((dicomInstalled == null) ? 0 : dicomInstalled.hashCode());
		result = prime * result + ((dicomPort == null) ? 0 : dicomPort.hashCode());
		result = prime * result + ((dicomTLSCipherSuite == null) ? 0 : dicomTLSCipherSuite.hashCode());
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
		LDAPNetworkConnection other = (LDAPNetworkConnection) obj;
		if (cn == null) {
			if (other.cn != null)
				return false;
		} else if (!cn.equals(other.cn))
			return false;
		if (dicomHostname == null) {
			if (other.dicomHostname != null)
				return false;
		} else if (!dicomHostname.equals(other.dicomHostname))
			return false;
		if (dicomInstalled == null) {
			if (other.dicomInstalled != null)
				return false;
		} else if (!dicomInstalled.equals(other.dicomInstalled))
			return false;
		if (dicomPort == null) {
			if (other.dicomPort != null)
				return false;
		} else if (!dicomPort.equals(other.dicomPort))
			return false;
		if (dicomTLSCipherSuite == null) {
			if (other.dicomTLSCipherSuite != null)
				return false;
		} else if (!dicomTLSCipherSuite.equals(other.dicomTLSCipherSuite))
			return false;
		return true;
	}
	
	

}
