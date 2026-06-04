import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.RequestObject
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// 1. Definisikan dan Panggil API dari Object Repository
RequestObject request = findTestObject("Object Repository/reqres/Update User")
ResponseObject response = WS.sendRequest(request)

// 2. Ekstrak Data Penting
int statusCode = response.getStatusCode()
String requestUrl = request.getRestUrl()
String method = request.getRestRequestMethod()

// 3. TANGKAP DAN PERCANTIK (PRETTY PRINT) RESPONSE BODY
String rawResponseBody = response.getResponseBodyContent()
String formattedResponseBody = rawResponseBody

try {
	// Coba format teks menyamping tadi menjadi JSON yang rapi ke bawah
	def jsonObject = new JsonSlurper().parseText(rawResponseBody)
	formattedResponseBody = JsonOutput.prettyPrint(JsonOutput.toJson(jsonObject))
} catch (Exception e) {
	// Jika respons API kebetulan bukan JSON (misalnya HTML), biarkan apa adanya
}

// 4. Ambil Request Body secara aman
String requestBody = "Tidak ada payload"
try {
	if (request.getBodyContent() != null) {
		requestBody = request.getHttpBody()
	}
} catch (Exception e) {}

// 5. Auto-Truncate (Batas karakter dinaikkan jadi 1500 karena format rapi memakan lebih banyak tempat)
int batasKarakter = 1500
String finalRes = formattedResponseBody.length() > batasKarakter ? formattedResponseBody.substring(0, batasKarakter) + "\n\n...[TRUNCATED]" : formattedResponseBody
String finalReq = requestBody.length() > batasKarakter ? requestBody.substring(0, batasKarakter) + "\n\n...[TRUNCATED]" : requestBody

int expectedStatusCode = 200

// 6. Rangkai Teks (Ubah menjadi bentuk Array agar terdeteksi sebagai Tabel)
def tableData = [
	["REQUEST\n(URL & BODY)", "Method: ${method}\nURL: ${requestUrl}\n\nPayload:\n${finalReq}"],
	["STATUS CODE", "Expected: ${expectedStatusCode}\nActual: ${statusCode}"],
	["RESPONSE BODY", "${finalRes}"]
]

// Ubah Array menjadi JSON String sebelum dikirim ke Katia-Report
String gabunganData = JsonOutput.toJson(tableData)

// 7. Validasi dan Catat
try {
	WS.verifyResponseStatusCode(response, 200)
	KatiaReporter.addTestResult("API-004", "Update User", "PASSED", gabunganData)
} catch (Exception e) {
	KatiaReporter.addTestResult("API-004", "Update User", "FAILED", gabunganData)
}
