/******************************************************************************
 * JWebUnit project (http://jwebunit.sourceforge.net)                         *
 * Distributed open-source, see full license under LICENCE.txt                *
 ******************************************************************************/
package net.sourceforge.jwebunit.selenium;


import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.jwebunit.api.ITestingEngine;
import net.sourceforge.jwebunit.exception.ElementNotFoundException;
import net.sourceforge.jwebunit.exception.ExpectedJavascriptAlertException;
import net.sourceforge.jwebunit.exception.ExpectedJavascriptConfirmException;
import net.sourceforge.jwebunit.exception.ExpectedJavascriptPromptException;
import net.sourceforge.jwebunit.exception.TestingEngineResponseException;
import net.sourceforge.jwebunit.html.Table;
import net.sourceforge.jwebunit.javascript.JavascriptAlert;
import net.sourceforge.jwebunit.javascript.JavascriptConfirm;
import net.sourceforge.jwebunit.javascript.JavascriptPrompt;
import net.sourceforge.jwebunit.util.TestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * Acts as the wrapper for Selenium access. A testing engine is initialized with a given URL, and maintains conversational state
 * as the dialog progresses through link navigation, form submission, etc.
 * 
 * @author Julien Henry
 * 
 */
public class SeleniumTestingEngineImpl implements ITestingEngine {

    /**
     * Logger for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(SeleniumTestingEngineImpl.class);

    private Selenium selenium;

    // Timeout in milliseconds.  It's a string 'cause Selenium wants a string.
    private static final String timeout = "3000";
    private static final int port = 4444;

    private TestContext testContext;

    // The xpath string that identifie the current form
    // ie : @name='myForm'
    private String formIdent;

    // The xpath prefix that identifie the current frame
    // ie : /html/frameset/frame[@name='myFrame']
    private String currentFrame;

    public SeleniumTestingEngineImpl() {
    }

    public void beginAt(URL aInitialURL, TestContext aTestContext)
            throws TestingEngineResponseException {
        this.setTestContext(aTestContext);
        selenium = new DefaultSelenium("localhost", port, "*chrome",
                aInitialURL.toString());
        selenium.start();
        gotoPage(aInitialURL);
    }

    public void checkCheckbox(String checkBoxName, String value) {
        selenium.check("xpath=//input[@type='checkbox' and @name='"
                + checkBoxName + "' and @value='" + value + "']");
    }

    public void checkCheckbox(String checkBoxName) {
        selenium.check("xpath=//input[@type='checkbox' and @name='"
                + checkBoxName + "']");
    }

    public void clickButton(String buttonId) {
        selenium.click("id=buttonId");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickButtonWithText(String buttonValueText) {
        selenium.click("xpath=" + formSelector() + "//button[contains(.,'" + buttonValueText + "')]");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickElementByXPath(String xpath) {
        selenium.click("xpath=" + xpath);
        selenium.waitForPageToLoad(timeout);
    }

    public void clickLink(String anID) {
        selenium.click("xpath=//a[@id='" + anID + "']");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickLinkWithExactText(String linkText, int index) {
        selenium.click("xpath=//a[.//*='" + linkText + "'][" + index + 1 + "]");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickLinkWithImage(String imageFileName, int index) {
        selenium.click("xpath=//a[contains(img/@src,'" + imageFileName + "')]");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickLinkWithText(String linkText, int index) {
        selenium.click("xpath=//a[contains(.,'" + linkText + "')][" + index + 1
                + "]");
        selenium.waitForPageToLoad(timeout);
    }

    public void clickRadioOption(String radioGroup, String radioOptionValue) {
        selenium.click("xpath=" + formSelector() + "//input[@name='" + radioGroup + "' and @value='"
                + radioOptionValue + "']");
    }

    public void closeBrowser() throws TestingEngineResponseException {
        selenium.stop();
    }

    public void closeWindow() {
        selenium.close();
    }

    public String getPageSource() {
        return selenium.getHtmlSource();
    }

    public String getPageText() {
        return selenium.getBodyText();
    }

    public String getPageTitle() {
        return selenium.getTitle();
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getCookies()
     */
    public List getCookies() {
        List l = new LinkedList();
        //FIXME How to parse this String in Cookie
        l.add(selenium.getCookie());
        return l;
    }


    public String[] getSelectedOptions(String selectName) {
        return selenium.getSelectedValues("xpath=" + formSelector()
                + "//select[@name='" + selectName + "']");
    }

    public String getSelectOptionLabelForValue(String selectName,
            String optionValue) {
        return selenium.getText("xpath=" + formSelector()
                + "//select/option[@value='" + optionValue + "']");
    }

    public String getSelectOptionValueForLabel(String selectName,
            String optionLabel) {
        return selenium.getValue("xpath=" + formSelector()
                + "//select/option[contains(.,'" + optionLabel + "']");
    }

    public String[] getSelectOptionValues(String selectName) {
        String[] labels = selenium.getSelectOptions("xpath=" + formSelector()
                + "//select[@name='" + selectName + "']");
        String[] values = new String[labels.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = getSelectOptionValueForLabel(selectName, labels[i]);
        }
        return values;
    }

    public String getServerResponse() {
        throw new UnsupportedOperationException("getServerResponse");
    }

    public void goBack() {
        selenium.goBack();
        selenium.waitForPageToLoad(timeout);
    }

    public void gotoFrame(String frameName) {
        currentFrame = "/html/frameset/frame[@name='" + frameName + "']";
    }

    public void gotoPage(URL url) throws TestingEngineResponseException {
        selenium.open(url.toString());
        // selenium.waitForPageToLoad(timeout); implicit after open() 
    }

    public void gotoRootWindow() {
        selenium.selectWindow("null");
    }

    public void gotoWindow(String windowName) {
        selenium.selectWindow(windowName);
    }

    public void gotoWindowByTitle(String title) {
        // TODO Implement gotoWindowByTitle in SeleniumDialog
        throw new UnsupportedOperationException("gotoWindowByTitle");
    }

    public boolean hasButton(String buttonId) {
        // Not bothering with formSelector here because we're using an ID
        // to identify the element.  Is this the right thing to do?
        return selenium.isElementPresent("xpath=//button[@id='" + buttonId
                + "']");
    }

    public boolean hasButtonWithText(String text) {
        return selenium.isElementPresent("xpath=" + formSelector() + "//button[contains(.,'" + text
                + "')]");
    }

    public boolean hasElement(String anID) {
        return selenium.isElementPresent("xpath=//*[@id='" + anID + "']");
    }

    public boolean hasElementByXPath(String xpath) {
        return selenium.isElementPresent("xpath=" + xpath);
    }

    public boolean hasForm() {
        return selenium.isElementPresent("xpath=//form");
    }

    public boolean hasForm(String nameOrID) {
        return selenium.isElementPresent("xpath=//form[@name='" + nameOrID
                + "' or @id='" + nameOrID + "']");
    }

    public boolean hasFormParameterNamed(String paramName) {
        return selenium.isElementPresent("xpath=" + formSelector()
                + "//*[@name='" + paramName + "']");
    }

    public boolean hasFrame(String frameName) {
        return selenium.isElementPresent("xpath=//frame[@name='" + frameName + "']");
    }

    public boolean hasLink(String anId) {
        return selenium.isElementPresent("xpath=//a[@id='" + anId + "']");
    }

    public boolean hasLinkWithExactText(String linkText, int index) {
        return selenium.isElementPresent("xpath=//a[.//*='" + linkText + "']["
                + index + 1 + "]");
    }

    public boolean hasLinkWithImage(String imageFileName, int index) {
        return selenium.isElementPresent("xpath=//a[contains(img/@src,'"
                + imageFileName + "')]");
    }

    public boolean hasLinkWithText(String linkText, int index) {
        return selenium.isElementPresent("xpath=//a[contains(.,'" + linkText
                + "')][" + index + 1 + "]");
    }

    public boolean hasRadioOption(String radioGroup, String radioOptionValue) {
        return selenium.isElementPresent("xpath=" + formSelector() + "//input[@name='" + radioGroup
                + "' and @value='" + radioOptionValue + "']");
    }

    public boolean hasSelectOption(String selectName, String optionLabel) {
        try {
            getSelectOptionValueForLabel(selectName, optionLabel);
            return true;
        } catch (SeleniumException e) {
            return false;
        }
    }

    public boolean hasSelectOptionValue(String selectName, String optionValue) {
        try {
            getSelectOptionLabelForValue(selectName, optionValue);
            return true;
        } catch (SeleniumException e) {
            return false;
        }
    }
    
    public boolean hasSubmitButton() {
        return selenium
        .isElementPresent("xpath=" + formSelector() + "(//input[@type='submit' or @type='image']|//button[@type='submit'])");
    }

    public boolean hasSubmitButton(String nameOrID, String value) {
        return selenium
                .isElementPresent("xpath=" + formSelector() + "(//input[(@type='submit' or @type='image') and (@id='"
                        + nameOrID + "' or @name='" + nameOrID
                        + "') and @value='" + value + "']|//button[@type='submit' and (@id='"
                        + nameOrID + "' or @name='" + nameOrID
                        + "') and @value='" + value + "'])");
    }

    public boolean hasSubmitButton(String nameOrID) {
        return selenium
        .isElementPresent("xpath=" + formSelector() + "(//input[(@type='submit' or @type='image') and (@id='"
                + nameOrID + "' or @name='" + nameOrID
                + "')]|//button[@type='submit' and (@id='"
                + nameOrID + "' or @name='" + nameOrID
                + "')])");
    }

    public boolean hasResetButton() {
        return selenium
        .isElementPresent("xpath=" + formSelector() + "//input[@type='reset']");
    }

    public boolean hasResetButton(String nameOrID) {
        return selenium
                .isElementPresent("xpath=" + formSelector() + "//input[@type='reset' and (@id='"
                        + nameOrID + "' or @name='" + nameOrID + "')]");
    }

    public boolean hasTable(String tableSummaryNameOrId) {
        return selenium.isElementPresent("xpath=//table[@id='"+tableSummaryNameOrId+"' or @name='"+tableSummaryNameOrId+"' or @summary='"+tableSummaryNameOrId+"']");
    }

    public boolean hasWindow(String windowName) {
        // TODO Implement hasWindow in SeleniumDialog
        throw new UnsupportedOperationException("hasWindow");
    }

    public boolean hasWindowByTitle(String title) {
        // TODO Implement hasWindowByTitle in SeleniumDialog
        throw new UnsupportedOperationException("hasWindowByTitle");
    }

    public boolean isCheckboxSelected(String checkBoxName) {
        return selenium.isChecked("xpath=" + formSelector() + "//input[@type='checkbox' and @name='"
                + checkBoxName + "']");
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#isCheckboxSelected(java.lang.String, java.lang.String)
     */
    public boolean isCheckboxSelected(String checkBoxName, String checkBoxValue) {
        return selenium.isChecked("xpath=" + formSelector() + "//input[@type='checkbox' and @name='"
                + checkBoxName + "' and @value='" + checkBoxValue + "']");
    }


    public boolean isMatchInElement(String elementID, String regexp) {
        //TODO Implement isMatchInElement in SeleniumDialog
        throw new UnsupportedOperationException("isMatchInElement");
    }

    public boolean isTextInElement(String elementID, String text) {
        // TODO Implement isTextInElement in SeleniumDialog
        throw new UnsupportedOperationException("isTextInElement");
    }

    public void refresh() {
        selenium.refresh();
        selenium.waitForPageToLoad(timeout);
    }

    public void reset() {
        selenium.click("xpath=" + formSelector() + "//input[@type='reset']");
        selenium.waitForPageToLoad(timeout);
    }

    public void selectOptions(String selectName, String[] optionsValue) {
        for (int i=0; i<optionsValue.length; i++) {
            selenium.addSelection("xpath=" + formSelector() + "//select[@name='"+selectName+"']","value="+optionsValue[i]);
        }
    }

    public void setScriptingEnabled(boolean value) {
        if (value==false)
            throw new UnsupportedOperationException("setScriptingEnabled");
    }

    public void setTextField(String inputName, String text) {
        selenium.type("xpath=" + formSelector() + "(//input[@name='"+inputName+"' and (@type='text' or @type='password' or @type='file')]|//textarea[@name='"+inputName+"'])", text);
    }

    public void setWorkingForm(String nameOrId, int index) {
        if (nameOrId != null)
            formIdent="(@name='"+nameOrId+"' or @id='"+nameOrId+"') and position()="+index;
        else
            formIdent=null;
    }

    public void submit() {
        selenium.click("xpath=" + formSelector() + "(//input[@type='submit' or @type='image']|//button[@type='submit'])");
        selenium.waitForPageToLoad(timeout);
    }

    public void submit(String buttonName, String buttonValue) {
        selenium.click("xpath=" + formSelector() + "(//input[(@type='submit' or @type='image') and (@id='"
                        + buttonName + "' or @name='" + buttonName
                        + "') and @value='" + buttonValue + "']|//button[@type='submit' and (@id='"
                        + buttonName + "' or @name='" + buttonName
                        + "') and @value='" + buttonValue + "'])");
        selenium.waitForPageToLoad(timeout);
    }

    public void submit(String buttonName) {
        selenium.click("xpath=" + formSelector() + "(//input[(@type='submit' or @type='image') and (@id='"
                        + buttonName + "' or @name='" + buttonName
                        + "')]|//button[@type='submit' and (@id='"
                        + buttonName + "' or @name='" + buttonName
                        + "')])");
        selenium.waitForPageToLoad(timeout);
    }

    public void uncheckCheckbox(String checkBoxName, String value) {
        selenium.uncheck("xpath=" + formSelector() + "//input[@type='checkbox' and @name='"
                + checkBoxName + "' and @value='" + value + "']");
    }

    public void uncheckCheckbox(String checkBoxName) {
        selenium.uncheck("xpath=" + formSelector() + "//input[@type='checkbox' and @name='"
                + checkBoxName + "']");
    }

    public void unselectOptions(String selectName, String[] options) {
        for (int i=0; i<options.length; i++) {
            selenium.removeSelection("xpath=" + formSelector() + "//select[@name='"+selectName+"']","value="+options[i]);
        }
    }

    public TestContext getTestContext() {
        return testContext;
    }

    public void setTestContext(TestContext testContext) {
        this.testContext = testContext;
    }

    public Table getTable(String tableSummaryNameOrId) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String formSelector() {
        if (formIdent == null)
            return "";
        return "//form[" + formIdent + "]";
    }

    public int getWindowCount() {
        //TODO implement getWindowCount in SeleniumDialog
        throw new UnsupportedOperationException("getWindowCount");
    }

    public void gotoWindow(int windowID) {
        selenium.selectWindow(""+windowID);
    }

    public String getTextFieldValue(String paramName) {
        //TODO implement getTextFieldValue in SeleniumDialog
        throw new UnsupportedOperationException("getTextFieldValue");
    }

    public String getHiddenFieldValue(String paramName) {
        //TODO implement getHiddenFieldValue in SeleniumDialog
        throw new UnsupportedOperationException("getHiddenFieldValue");
    }
    
    public String getJavascriptAlert() throws ElementNotFoundException {
        if (selenium.isAlertPresent()) {
            return selenium.getAlert(); 
        }
        else {
            throw new net.sourceforge.jwebunit.exception.ElementNotFoundException("There is no pending alert.");
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getElementAttributByXPath(java.lang.String, java.lang.String)
     */
    public String getElementAttributByXPath(String xpath, String attribut) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getElementAttributByXPath");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getElementTextByXPath(java.lang.String)
     */
    public String getElementTextByXPath(String xpath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getElementTextByXPath");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getInputStream()
     */
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getInputStream");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getInputStream(java.net.URL)
     */
    public InputStream getInputStream(URL url) throws TestingEngineResponseException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getInputStream");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getPageURL()
     */
    public URL getPageURL() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("getPageURL");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#getSelectedRadio(java.lang.String)
     */
    public String getSelectedRadio(String radioGroup) {
        throw new UnsupportedOperationException("getSelectedRadio");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#setExpectedJavaScriptAlert(net.sourceforge.jwebunit.javascript.JavascriptAlert[])
     */
    public void setExpectedJavaScriptAlert(JavascriptAlert[] alerts) throws ExpectedJavascriptAlertException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("setExpectedJavaScriptAlert");
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#setExpectedJavaScriptConfirm(net.sourceforge.jwebunit.javascript.JavascriptConfirm[])
     */
    public void setExpectedJavaScriptConfirm(JavascriptConfirm[] confirms) throws ExpectedJavascriptConfirmException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("setExpectedJavaScriptConfirm");
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#setExpectedJavaScriptPrompt(net.sourceforge.jwebunit.javascript.JavascriptPrompt[])
     */
    public void setExpectedJavaScriptPrompt(JavascriptPrompt[] prompts) throws ExpectedJavascriptPromptException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("setExpectedJavaScriptPrompt");        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jwebunit.api.ITestingEngine#setWorkingForm(int)
     */
    public void setWorkingForm(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("setWorkingForm");
    }

}