import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriver

public class KatiaReporter {

	static List<Map<String, String>> currentSteps = new ArrayList<>()

	public static StepRecord KatiaReporterScreenshot(String action, String data, String expected) {
		return new StepRecord(action, data, expected)
	}

	@Keyword
	def static addTestResult(String id, String name, String status, String apiData = null) {
		Map<String, Object> result = new HashMap<>()
		result.put("id", id.toString().trim())
		result.put("name", name.toString().trim())
		result.put("status", status)

		if (apiData != null) {
			Map<String, String> apiStep = new HashMap<>()
			apiStep.put("action", "Hit API Endpoint")
			apiStep.put("data", apiData)
			apiStep.put("expected", "")
			apiStep.put("status", status)
			apiStep.put("screenshot", "")
			currentSteps.add(apiStep)
		}

		result.put("steps", new ArrayList<>(currentSteps))
		currentSteps.clear()

		String projectDir = RunConfiguration.getProjectDir().replace("\\", "/")
		String folderPath = projectDir + "/katia_report"
		File reportDir = new File(folderPath)
		if (!reportDir.exists()) {
			reportDir.mkdirs()
		}

		
		
		
				
		// --- DETEKSI OTOMATIS PLATFORM / BROWSER YANG LEBIH AKURAT ---
		String executedDriver = ""
		try {
			// 1. Coba ambil dari DriverFactory Web (Chrome, Firefox, Edge, dll)
			executedDriver = com.kms.katalon.core.webui.driver.DriverFactory.getExecutedBrowser()?.toString() ?: ""
		} catch (Exception e) {}

		// 2. Jika kosong, coba cek apakah ini Mobile
		if (!executedDriver) {
			try {
				executedDriver = com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory.getDeviceName() ? "Android / iOS" : ""
			} catch (Exception e) {}
		}

		// 3. Jika tetap kosong, fallback ke properti umum atau biarkan kosong (untuk API)
		if (!executedDriver) {
			try {
				executedDriver = RunConfiguration.getExecutionProperties().get("RunConfiguration")?.get("driver")?.toString() ?: ""
			} catch (Exception e) {}
		}
		
		

		File jsonFile = new File(folderPath + "/result.json")
		def reportData = [
			"projectName": "E2E Hybrid Testing (Web, API, Mobile)",
			"framework": "Katalon Studio",
			"platform": executedDriver, // <-- Menggunakan hasil deteksi otomatis driver aktif
			"testDate": java.time.LocalDate.now().toString(),
			"summary": ["total": 0, "passed": 0, "failed": 0],
			"results": []
		]

		if (jsonFile.exists() && jsonFile.length() > 0) {
			try {
				def parsed = new JsonSlurper().parse(jsonFile)
				if (parsed.results != null) {
					reportData.results = parsed.results
				}
			} catch (Exception e) {}
		}

		// SMART OVERWRITE: Cegah duplikat jika TC yang sama di-run ulang (Retry)
		int existingIndex = reportData.results.findIndexOf { it.id == result.id }
		if (existingIndex != -1) {
			reportData.results[existingIndex] = result
		} else {
			reportData.results.add(result)
		}

		reportData.summary.total = reportData.results.size()
		reportData.summary.passed = reportData.results.count { it.status == 'PASSED' }
		reportData.summary.failed = reportData.results.count { it.status == 'FAILED' }

		jsonFile.write(JsonOutput.toJson(reportData))
	}

	public static void cleanUpOldReport() {
		String folderPath = RunConfiguration.getProjectDir().replace("\\", "/") + "/katia_report"
		File jsonFile = new File(folderPath + "/result.json")
		if(jsonFile.exists()) jsonFile.delete()
		File screenshotDir = new File(folderPath + "/screenshots")
		if(screenshotDir.exists()) screenshotDir.listFiles().each { if(it.isFile()) it.delete() }
		currentSteps.clear()
	}

	
	public static void generatePDFReport() {
		println("[+] Memulai pembuatan PDF Master Report...")
		String projectDir = com.kms.katalon.core.configuration.RunConfiguration.getProjectDir().replace("\\", "/")
		String baseReportDir = projectDir + "/katia_report"
		File jsonFile = new File(baseReportDir + "/result.json")

		if (!jsonFile.exists()) {
			println("[!] Gagal: result.json tidak ditemukan!")
			return
		}

		// --- 1. AMBIL DATA JSON ---
		String appName = "", scenario = "", tcId = "TC", tcName = "Execution"
		try {
			def parsed = new groovy.json.JsonSlurper().parse(jsonFile)

			appName = parsed.appName ?: parsed.app_name ?: ""
			scenario = parsed.scenario ?: ""

			if (parsed.projectConfig) {
				if (!appName) appName = parsed.projectConfig.appName ?: parsed.projectConfig.app_name ?: ""
				if (!scenario) scenario = parsed.projectConfig.scenario ?: ""
			}

			if (parsed.results && parsed.results.size() > 0) {
				tcId = parsed.results[0].id ?: parsed.results[0].tc_id ?: "TC"
				tcName = parsed.results[0].name ?: parsed.results[0].tc_name ?: "Execution"

				if (!appName) appName = parsed.results[0].appName ?: parsed.results[0].app_name ?: ""
				if (!scenario) scenario = parsed.results[0].scenario ?: ""
			}
		} catch (Exception e) {}

		// --- 2. DETEKSI TSC CERDAS (MEMANJAT STRUKTUR FOLDER) ---
		String executionId = com.kms.katalon.core.configuration.RunConfiguration.getExecutionSourceId()
		String tsName = com.kms.katalon.core.configuration.RunConfiguration.getExecutionSourceName()
		boolean isTestSuite = executionId != null && executionId.contains("Test Suites")
		String tscName = ""

		try {
			File currentLogDir = new File(com.kms.katalon.core.configuration.RunConfiguration.getLogFolderPath())
			List<String> pathHierarchy = new ArrayList<>()

			while (currentLogDir != null) {
				pathHierarchy.add(currentLogDir.getName())
				currentLogDir = currentLogDir.getParentFile()
			}

			int reportsIndex = -1
			for (int i = 0; i < pathHierarchy.size(); i++) {
				if (pathHierarchy.get(i).equalsIgnoreCase("Reports")) {
					reportsIndex = i
					break
				}
			}

			if (reportsIndex >= 5) {
				tscName = pathHierarchy.get(3)
			} else if (reportsIndex == -1 && pathHierarchy.size() >= 5) {
				if (pathHierarchy.get(2).matches(".*\\d{4}.*") && pathHierarchy.get(4).matches(".*\\d{4}.*")) {
					tscName = pathHierarchy.get(3)
				}
			}
		} catch (Exception e) {}

		// --- 3. FUNGSI HELPER FOLDER ---
		def getOrCreateFolder = { File parent, String folderName ->
			if (!folderName || folderName.trim() == "") return parent
			folderName = folderName.trim().replaceAll("[\\\\/:*?\"<>|]", "_")

			File[] existing = parent.listFiles({ it.isDirectory() } as java.io.FileFilter)
			File match = existing?.find { it.name.equalsIgnoreCase(folderName) }

			if (match) return match
			else {
				File newFolder = new File(parent, folderName)
				newFolder.mkdirs()
				return newFolder
			}
		}

		// --- 4. TENTUKAN TARGET FOLDER FINAL ---
		File targetDir = new File(baseReportDir)
		targetDir = getOrCreateFolder(targetDir, "folder_report")

		if (isTestSuite) {
			if (tscName && tscName.trim() != "") {
				targetDir = getOrCreateFolder(targetDir, "Test Suite Collection")
				targetDir = getOrCreateFolder(targetDir, tscName)
			} else {
				targetDir = getOrCreateFolder(targetDir, "Test Suite")
				targetDir = getOrCreateFolder(targetDir, tsName)
			}
		} else {
			if (appName && appName.trim() != "") targetDir = getOrCreateFolder(targetDir, appName)
			if (scenario && scenario.trim() != "") targetDir = getOrCreateFolder(targetDir, scenario)
		}

		// --- 5. FORMAT NAMA FILE ---
		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyy-HH-mm-ss")
		String timestamp = java.time.LocalDateTime.now().format(formatter)
		String dynamicFileName = ""

		if (isTestSuite) {
			String activeName = (tscName && tscName.trim() != "") ? tscName : tsName
			dynamicFileName = "${activeName}-${timestamp}.pdf"
		} else {
			dynamicFileName = "${tcId}-${tcName}-${timestamp}.pdf"
		}

		dynamicFileName = dynamicFileName.replaceAll("[^a-zA-Z0-9_.-]", "_")
		File outputFile = new File(targetDir, dynamicFileName)
		String outputFilePath = outputFile.getAbsolutePath().replace("\\", "/")

		// --- 6. EKSEKUSI (Cek .exe atau Node.js) ---
		File exeFile = new File(baseReportDir + "/katia-report.exe")
		def pb

		if (exeFile.exists()) {
			println("[i] Menggunakan katia-report.exe untuk generate laporan...")
			pb = new ProcessBuilder(exeFile.getAbsolutePath(), "-i", "result.json", "-o", outputFilePath)
		} else {
			println("[i] File .exe tidak ditemukan. Fallback menggunakan node katia.js...")
			pb = new ProcessBuilder("node", "katia.js", "-i", "result.json", "-o", outputFilePath)
		}

		pb.directory(new File(baseReportDir))
		pb.redirectErrorStream(true)

		def process = pb.start()
		def output = process.inputStream.text
		process.waitFor()

		if (process.exitValue() == 0) {
			println("[V] Laporan Master PDF sukses dibuat: " + outputFilePath)
			try {
				File tempScreenshots = new File(baseReportDir + "/screenshots")
				if (tempScreenshots.exists()) {
					tempScreenshots.deleteDir()
				}
				
				this.cleanUpOldReport()
			} catch (Exception e) {}
		} else {
			println("[X] Gagal membuat laporan! Error:")
			println(output)
		}
	}

	public static void generatePDFReportScreenshot() {
		println("[+] Memulai pembuatan PDF Report beserta Backup Screenshot...")
		String projectDir = com.kms.katalon.core.configuration.RunConfiguration.getProjectDir().replace("\\", "/")
		String baseReportDir = projectDir + "/katia_report"
		File jsonFile = new File(baseReportDir + "/result.json")

		if (!jsonFile.exists()) {
			println("[!] Gagal: result.json tidak ditemukan!")
			return
		}

		// --- 1. AMBIL DATA JSON (Termasuk AppName & Scenario) ---
		String appName = "", scenario = "", tcId = "TC", tcName = "Execution"
		try {
			def parsed = new groovy.json.JsonSlurper().parse(jsonFile)

			appName = parsed.appName ?: parsed.app_name ?: ""
			scenario = parsed.scenario ?: ""

			if (parsed.projectConfig) {
				if (!appName) appName = parsed.projectConfig.appName ?: parsed.projectConfig.app_name ?: ""
				if (!scenario) scenario = parsed.projectConfig.scenario ?: ""
			}

			if (parsed.results && parsed.results.size() > 0) {
				tcId = parsed.results[0].id ?: parsed.results[0].tc_id ?: "TC"
				tcName = parsed.results[0].name ?: parsed.results[0].tc_name ?: "Execution"

				if (!appName) appName = parsed.results[0].appName ?: parsed.results[0].app_name ?: ""
				if (!scenario) scenario = parsed.results[0].scenario ?: ""
			}
		} catch (Exception e) {}

		// --- 2. DETEKSI NAMA TEST SUITE/COLLECTION ---
		String executionId = com.kms.katalon.core.configuration.RunConfiguration.getExecutionSourceId()
		String tsName = com.kms.katalon.core.configuration.RunConfiguration.getExecutionSourceName()
		boolean isTestSuite = executionId != null && executionId.contains("Test Suites")
		String tscName = ""

		try {
			File currentLogDir = new File(com.kms.katalon.core.configuration.RunConfiguration.getLogFolderPath())
			List<String> pathHierarchy = new ArrayList<>()

			while (currentLogDir != null) {
				pathHierarchy.add(currentLogDir.getName())
				currentLogDir = currentLogDir.getParentFile()
			}

			int reportsIndex = -1
			for (int i = 0; i < pathHierarchy.size(); i++) {
				if (pathHierarchy.get(i).equalsIgnoreCase("Reports")) {
					reportsIndex = i
					break
				}
			}

			if (reportsIndex >= 5) {
				tscName = pathHierarchy.get(3)
			} else if (reportsIndex == -1 && pathHierarchy.size() >= 5) {
				if (pathHierarchy.get(2).matches(".*\\d{4}.*") && pathHierarchy.get(4).matches(".*\\d{4}.*")) {
					tscName = pathHierarchy.get(3)
				}
			}
		} catch (Exception e) {}

		// --- 3. FORMAT TIMESTAMP UNTUK NAMA FILE ---
		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyy-HH-mm-ss")
		String timestamp = java.time.LocalDateTime.now().format(formatter)

		String dynamicFileName = ""

		if (isTestSuite) {
			String activeName = (tscName && tscName.trim() != "") ? tscName : tsName
			dynamicFileName = "${activeName}-${timestamp}.pdf"
		} else {
			dynamicFileName = "${tcId}-${tcName}-${timestamp}.pdf"
		}

		dynamicFileName = dynamicFileName.replaceAll("[^a-zA-Z0-9_.-]", "_")

		// --- 4. BUAT STRUKTUR FOLDER REPORT & SCREENSHOT (IDENTIK) ---
		File targetReportDir = new File(baseReportDir + "/folder_report")
		File targetScreenshotBaseDir = new File(baseReportDir + "/folder_screenshot")

		if (appName && appName.trim() != "") {
			String cleanApp = appName.trim().replaceAll("[\\\\/:*?\"<>|]", "_")
			targetReportDir = new File(targetReportDir, cleanApp)
			targetScreenshotBaseDir = new File(targetScreenshotBaseDir, cleanApp)
		}

		if (scenario && scenario.trim() != "") {
			String cleanScenario = scenario.trim().replaceAll("[\\\\/:*?\"<>|]", "_")
			targetReportDir = new File(targetReportDir, cleanScenario)
			targetScreenshotBaseDir = new File(targetScreenshotBaseDir, cleanScenario)
		}

		targetReportDir.mkdirs()

		File outputFile = new File(targetReportDir, dynamicFileName)
		String outputFilePath = outputFile.getAbsolutePath().replace("\\", "/")

		// --- 5. EKSEKUSI PEMBUATAN PDF (.exe) ---
		File exeFile = new File(baseReportDir + "/katia-report.exe")
		def pb

		if (exeFile.exists()) {
			println("[i] Menggunakan katia-report.exe untuk generate laporan...")
			pb = new ProcessBuilder(exeFile.getAbsolutePath(), "-i", "result.json", "-o", outputFilePath)
		} else {
			println("[i] File .exe tidak ditemukan. Fallback menggunakan node katia.js...")
			pb = new ProcessBuilder("node", "katia.js", "-i", "result.json", "-o", outputFilePath)
		}

		pb.directory(new File(baseReportDir))
		pb.redirectErrorStream(true)

		def process = pb.start()
		def output = process.inputStream.text
		process.waitFor()

		// --- 6. BACKUP SCREENSHOT DAN CLEANUP ---
		if (process.exitValue() == 0) {
			println("[V] Laporan PDF sukses dibuat di: " + outputFilePath)

			try {
				File sourceScreenshots = new File(baseReportDir + "/screenshots")
				if (sourceScreenshots.exists() && sourceScreenshots.listFiles().length > 0) {
					File targetEvidenceDir = new File(targetScreenshotBaseDir, timestamp)
					targetEvidenceDir.mkdirs()

					sourceScreenshots.listFiles().each { file ->
						if (file.isFile()) {
							java.nio.file.Files.copy(
									file.toPath(),
									new File(targetEvidenceDir, file.getName()).toPath(),
									java.nio.file.StandardCopyOption.REPLACE_EXISTING
									)
						}
					}
					println("[V] Screenshot sukses di-backup ke: " + targetEvidenceDir.getAbsolutePath())
				}

				if (sourceScreenshots.exists()) {
					sourceScreenshots.deleteDir()
				}

				this.cleanUpOldReport()
			} catch (Exception e) {
				println("[X] Gagal melakukan backup screenshot atau cleanup: " + e.getMessage())
			}
		} else {
			println("[X] Gagal membuat laporan! Error:")
			println(output)
		}
	}
}

class StepRecord {
	String action, data, expected

	StepRecord(String action, String data, String expected) {
		this.action = action; this.data = data; this.expected = expected
	}

	StepRecord getPASSED() { recordStep("PASSED"); return this }
	StepRecord getFAILED() { recordStep("FAILED"); return this }

	private void recordStep(String status) {
		String projectDir = RunConfiguration.getProjectDir().replace("\\", "/")
		String baseFileName = action.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis().toString()
		String screenshotFolder = projectDir + "/katia_report/screenshots"
		File screenshotDir = new File(screenshotFolder)
		if (!screenshotDir.exists()) screenshotDir.mkdirs()

		String pngPath = screenshotFolder + "/" + baseFileName + ".png"
		String finalPath = ""

		try {
			WebDriver nativeDriver = null
			try { nativeDriver = com.kms.katalon.core.webui.driver.DriverFactory.getWebDriver() } catch (Exception e) {}
			if (nativeDriver == null) {
				try { nativeDriver = com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory.getDriver() } catch (Exception e) {}
			}

			if (nativeDriver != null && nativeDriver instanceof TakesScreenshot) {
				File srcFile = ((TakesScreenshot) nativeDriver).getScreenshotAs(OutputType.FILE)
				Files.copy(srcFile.toPath(), new File(pngPath).toPath(), StandardCopyOption.REPLACE_EXISTING)
			}

			File pngFile = new File(pngPath)
			if (pngFile.exists() && pngFile.length() > 0) {
				String jpgPath = screenshotFolder + "/" + baseFileName + ".jpg"
				BufferedImage image = ImageIO.read(pngFile)

				if (image != null) {
					BufferedImage compressedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB)
					compressedImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
					ImageIO.write(compressedImage, "jpg", new File(jpgPath))
					pngFile.delete()
					finalPath = jpgPath
				} else {
					finalPath = pngPath
				}
			}
		} catch (Exception e) {}

		Map<String, String> step = new HashMap<>()
		step.put("action", action); step.put("data", data)
		step.put("expected", expected); step.put("status", status)
		step.put("screenshot", finalPath)
		KatiaReporter.currentSteps.add(step)
	}
}