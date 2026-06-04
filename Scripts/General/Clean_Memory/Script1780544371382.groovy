import KatiaReporter

println("======================================================")
println("[+] TS_SETUP: Membersihkan sisa laporan hari kemarin...")
println("======================================================")

// Menghapus file JSON dan gambar lama agar eksekusi E2E benar-benar bersih dari 0
KatiaReporter.cleanUpOldReport()