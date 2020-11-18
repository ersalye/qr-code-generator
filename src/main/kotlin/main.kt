import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.UUID as uuid
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val run = parseArgs(args)
    val document = PDDocument()
    for (pageNumber in 0..run.pages) {
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)
        for (posY in 0..8) {
            for (posX in 0..2) {
                generateCodeAtPosition(document, page, run.getCode(), posX, posY)
            }
        }
    }
    document.save(run.fileName)
    document.close()
}

data class Run(
    var prefix: String,
    var suffix: String?,
    var fileName: String,
    var filler: FillerType,
    var pages: Int
) {
    fun getCode(): String {
        return "$prefix$filler$suffix}"
    }
}

enum class FillerType {
    UUID, NONE;

    @Override
    override fun toString():String {
        return when(this) {
            UUID -> uuid.randomUUID().toString()
            else -> ""
        }
    }
}

private fun parseArgs(args: Array<String>): Run {
    var arguments = args
    if (arguments.size % 2 != 0) {
        println("""
            Welcome to barcode generator help section!
            
            This barcode generator will take the following inputs:
                
                -p [prefix] The text to be added before the generated filler, use this for a static barcode {defaults to empty}
                -s [suffix] The text to be added after the generated filler {defaults to none}
                -o [outputFile] Name of output file {defaults to "barcode.pdf"}
                -f [UUID, NONE] Type of filler to use {defaults to UUID}
                --pages [number] Number of pages to generate {defaults to one}
                
            Happy trails!
            
        """.trimIndent())
        exitProcess(1)
    }

    var prefix = ""
    var suffix: String? = null
    var fileName = "barcode.pdf"
    var filler: FillerType = FillerType.UUID
    var pages = 1

    try {
        while (arguments.isNotEmpty() && arguments.size % 2 == 0 ) {
            when (arguments[0]) {
                "-p" -> prefix = arguments[1]
                "-s" -> suffix = arguments[1]
                "-o" -> fileName = arguments[1]
                "-f" -> {
                    filler = when (arguments[1]) {
                        "UUID" -> FillerType.UUID
                        else -> FillerType.NONE
                    }
                }
                "--pages" -> pages = arguments[1].toInt()
            }
            arguments = arguments.copyOfRange(2, arguments.size)
        }
    } catch (e: IndexOutOfBoundsException) {
        println("Something went horribly wrong")
        println(e.message)
        exitProcess(1)
    }

    return Run(prefix, suffix, fileName, filler, pages)
}

private fun generateCodeAtPosition(document: PDDocument, page: PDPage, text: String, positionX: Int, positionY: Int ) {
    val yBase = 105
    val xBase = 198

    val size = 100f

    val y = (yBase - size) / 2f + (positionY * yBase)
    val x = (xBase - size) / 2f + (positionX * xBase)

    addQRCode(document, page, text, x, y, size)
}

private fun addQRCode(document: PDDocument, page: PDPage, text: String, x: Float, y: Float, size: Float) {
    try {
        val contentStream = PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)

        val hintMap = HashMap<EncodeHintType, Any>()
        hintMap.put(EncodeHintType.MARGIN, 0);
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 100, 100, hintMap);

        val config = MatrixToImageConfig(0xFF000001.toInt(), 0xFFFFFFFF.toInt());
        val bImage = MatrixToImageWriter.toBufferedImage(matrix, config);
        val image = JPEGFactory.createFromImage(document, bImage);
        contentStream.drawImage(image, x, y, size, size);
        contentStream.close();

    }
    catch (e: Exception) {
        e.printStackTrace();
    }

}