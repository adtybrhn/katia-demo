import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import Login_Menu as Login

try {
	Login.openBrowser()
	Login.login()
	KatiaReporter.addTestResult("TC-001", "Verifikasi Login Valid", "PASSED")

} catch (Exception e) {
	KatiaReporter.addTestResult("TC-001", "Verifikasi Login Valid", "FAILED")
	KeywordUtil.markFailed("Test Gagal: " + e.getMessage())	
} finally {
	WebUI.closeBrowser()
}