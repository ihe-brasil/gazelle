//package net.ihe.gazelle.tm.configurations.action;
//
//import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import net.ihe.gazelle.tm.configurations.model.Configuration;
//import net.ihe.gazelle.tm.configurations.model.Host;
//import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
//
//public class LdifGeneratorTest extends LdifGenerator {
//
//	@Test
//	public void testCreateLDIFFile() {
//		//fail("Not yet implemented");
//	}
//
//	@Test
//	public void testExtractListDicomSCPConf() {
//		//fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCleanUpDuplication() {
//		//fail("Not yet implemented");
//	}
//
//	@Test
//	public void testContainsConfiguration() {
//		//fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConfsAreEquals() {
//		DicomSCPConfiguration d1 = new DicomSCPConfiguration();
//		d1.setAeTitle("ZZ");
//		d1.setConfiguration(new Configuration());
//		d1.getConfiguration().setHost(new Host());
//		d1.getConfiguration().getHost().setHostname("aa");
//		DicomSCPConfiguration d2 = new DicomSCPConfiguration();
//		d2.setAeTitle("ZZ");
//		d2.setConfiguration(new Configuration());
//		d2.getConfiguration().setHost(new Host());
//		d2.getConfiguration().getHost().setHostname("bb");
//		assertFalse(confsAreEquals(d1, d2));
//		d2.getConfiguration().getHost().setHostname("aa");
//		assertTrue(confsAreEquals(d1, d2));
//	}
//
//	@Test
//	public void testHostnameAreEquals() {
//		DicomSCPConfiguration d1 = new DicomSCPConfiguration();
//		d1.setConfiguration(new Configuration());
//		d1.getConfiguration().setHost(new Host());
//		d1.getConfiguration().getHost().setHostname("aa");
//		DicomSCPConfiguration d2 = new DicomSCPConfiguration();
//		d2.setConfiguration(new Configuration());
//		d2.getConfiguration().setHost(new Host());
//		d2.getConfiguration().getHost().setHostname("bb");
//		assertFalse(hostnameAreEquals(d1, d2));
//		d2.getConfiguration().getHost().setHostname("aa");
//		assertTrue(hostnameAreEquals(d1, d2));
//		d2.getConfiguration().setHost(null);
//		assertFalse(hostnameAreEquals(d1, d2));
//		d2.setConfiguration(null);
//		assertFalse(hostnameAreEquals(d1, d2));
//	}
//
//	@Test
//	public void testHostnameNotNull() {
//		DicomSCPConfiguration d2 = new DicomSCPConfiguration();
//		d2.setConfiguration(new Configuration());
//		d2.getConfiguration().setHost(new Host());
//		d2.getConfiguration().getHost().setHostname("bb");
//		assertFalse(hostnameNotNull(d2));
//		d2.getConfiguration().getHost().setHostname("aa");
//		assertTrue(hostnameNotNull(d2));
//		d2.getConfiguration().setHost(null);
//		assertFalse(hostnameNotNull(d2));
//		d2.setConfiguration(null);
//		assertFalse(hostnameNotNull(d2));
//	}
//
//	@Test
//	public void testPortsAreEquals() {
//		Integer i = 0; 
//		Integer j = 1;
//		assertTrue(portsAreEquals(i, j));
//		assertFalse(portsAreEquals(null, j));
//		assertTrue(portsAreEquals(i, null));
//	}
//
//}
