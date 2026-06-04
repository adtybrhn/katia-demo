import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import static KatiaReporter.KatiaReporterScreenshot

static TestObject createTestObjectByXPath(String xpath) {
	TestObject to = new TestObject()
	to.addProperty("xpath", ConditionType.EQUALS, xpath)
	return to
}

TestObject txtUsername = createTestObjectByXPath('//android.widget.EditText[@content-desc="test-Username"]')
TestObject txtPassword = createTestObjectByXPath('//android.widget.EditText[@content-desc="test-Password"]')
TestObject btnLogin = createTestObjectByXPath('//android.view.ViewGroup[@content-desc="test-LOGIN"]')


Mobile.startExistingApplication("com.swaglabsmobileapp")

try {
	Mobile.setText(txtUsername, "standard_user", 0)
	KatiaReporterScreenshot("Input Username", "standard_user", "field username terisi").PASSED
	
	Mobile.setText(txtPassword, "secret_sauce", 0)
	KatiaReporterScreenshot("Input Password", "secret_sauce", "field password terisi").PASSED
	
	Mobile.tap(btnLogin, 0)
	Mobile.delay(3) 
	KatiaReporterScreenshot("Klik Login", "", "berhasil login").PASSED
	
	KatiaReporter.addTestResult("Mobile_001","Verifikasi Login Mobile","PASSED")

} catch (Exception e) {
	KatiaReporter.addTestResult("Mobile_001","Verifikasi Login Mobile","FAILED")
	println(e.getMessage())

} finally {
	Mobile.closeApplication()
	
}