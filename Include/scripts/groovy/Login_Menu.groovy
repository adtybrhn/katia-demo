import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import static KatiaReporter.KatiaReporterScreenshot

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows

import internal.GlobalVariable

public class Login_Menu {
	static TestObject textbox_username 	= new TestObject().addProperty('xpath', ConditionType.EQUALS, "//input[@id='user-name']")
	static TestObject textbox_password 	= new TestObject().addProperty('xpath', ConditionType.EQUALS, "//input[@id='password']")
	static TestObject btn_login 		= new TestObject().addProperty('xpath', ConditionType.EQUALS, "//input[@id='login-button']")
	static TestObject dashboard_logo 	= new TestObject().addProperty('xpath', ConditionType.EQUALS, "//div[@class='app_logo']")

	public static void openBrowser() {
		WebUI.openBrowser(GlobalVariable.url)
		WebUI.maximizeWindow()
	}

	public static void login() {
		String username = 'standard_user'
		String password = 'secret_sauce'

		WebUI.setText(textbox_username, username)
		KatiaReporterScreenshot("Input Username", "standard_user", "Text username terisi").PASSED

		WebUI.setText(textbox_password, password)
		KatiaReporterScreenshot("Input Password", "secret_sauce", "Text password terisi").PASSED

		WebUI.click(btn_login)

		if (WebUI.verifyElementPresent(dashboard_logo, 5, FailureHandling.OPTIONAL)) {
			KatiaReporterScreenshot("Berhasil Login", "", "Masuk ke halaman dashboard").PASSED
		} else {
			KatiaReporterScreenshot("Gagal Login", "", "Gagal Masuk ke halaman dashboard").FAILED
		}
	}

	public static void loginFailed() {
		String username = 'standard_user'
		String password = 'nopassword'

		WebUI.setText(textbox_username, username)
		KatiaReporterScreenshot("Input Username", "standard_user", "Text username terisi").PASSED

		WebUI.setText(textbox_password, password)
		KatiaReporterScreenshot("Input Password", "nopassword", "Text password terisi").PASSED

		WebUI.click(btn_login)

		if (WebUI.verifyElementPresent(dashboard_logo, 5, FailureHandling.OPTIONAL)) {
			KatiaReporterScreenshot("Berhasil Login", "", "Seharusnya tidak bisa masuk dashboard").FAILED
		} else {
			KatiaReporterScreenshot("Gagal Login", "", "Gagal Masuk ke halaman dashboard (Sesuai Ekspektasi)").PASSED
		}
	}
}