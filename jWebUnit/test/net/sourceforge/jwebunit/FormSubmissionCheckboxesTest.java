package net.sourceforge.jwebunit;

import net.sourceforge.jwebunit.util.JettySetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test form submission where checkbox name+value is not known.
 */
public class FormSubmissionCheckboxesTest extends JWebUnitAPITestCase {

	public static Test suite() {
		Test suite = new TestSuite(FormSubmissionCheckboxesTest.class);
		return new JettySetup(suite);
	}	
	
	public void setUp() throws Exception {
		super.setUp();
		getTestContext().setBaseUrl(HOST_PATH + "/FormSubmissionTest");
	}

	public void testCheckboxIdentificationLabelBeforeBox() {
		beginAt("/CheckboxFormWithLabels.html");
		// we don't know the name or value, just the label
		checkCheckboxWithLabel("Check 20");
		//submit();
		//assertTextPresent("Params are: chk=20");
		assertFormElementEquals("chk", "20");
	}

	public void testCheckboxIdentificationLabelAfterBox1() {
		doTestCheckboxBefore(1);
	}

	/**
	 * Extracting test logic because it must be improved to use jetty
	 * @param value
	 */
	private void doTestCheckboxBefore(int value) {
		beginAt("/CheckboxFormWithLabels.html");
		checkCheckboxBeforeLabel("Check " + value);
		//submit();
		//assertTextPresent("Params are: chk=" + value);
		assertFormElementEquals("chk", "" + value);
	}	

	public void testCheckboxIdentificationLabelAfterBox2() {
		doTestCheckboxBefore(5);
	}

	public void testCheckboxIdentificationLabelAfterBox3() {
		doTestCheckboxBefore(3);
	}

	private void doTestRadioBefore(int value) {
		beginAt("/CheckboxFormWithLabels.html");
		// we don't know the name or value, just the label
		checkCheckboxBeforeLabel("Radio " + value);
		//submit("button");
		//assertTextPresent("Params are: chk=" + value);
		assertFormElementEquals("rad", "" + value);
	}	
	
	public void testCheckboxIdentificationLabelAfterBox10() {
		doTestRadioBefore(10);
	}
	
	public void testCheckboxIdentificationLabelAfterBox11() {
		doTestRadioBefore(11);
	}
}