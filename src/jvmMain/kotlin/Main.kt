import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
fun App() {
    var showBarCode by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var eanCode by remember { mutableStateOf("") }
    var showTooShortError by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            TextField(
                value = inputText,
                onValueChange = { newText ->
                    val actualText = newText.filter { it.isDigit() }
                    if (newText.length <= 12) {
                        inputText = actualText
                        showTooShortError = false
                    }
                },
                maxLines = 1,
                isError = showTooShortError,
            )
            if (showTooShortError) {
                Text(
                    text = "Trzeba wprowadzić 12 cyfr",
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(height = 16.dp))

            Button(onClick = {
                if (inputText.length < 12) {
                    showTooShortError = true
                    showBarCode = false
                } else {
                    showTooShortError = false
                    val checkSum = calculateCheckDigit(inputText)
                    eanCode = inputText + checkSum
                    showBarCode = true
                }
            }) {
                Text("Generuj nowy kod kreskowy")
            }

            if (showBarCode) {
                Text(text = "Kod EAN-13: $eanCode")
                Barcode(eanCode)
            }
        }
    }
}


@Composable
fun Barcode(ean13: String) {
    val barcodeWidth = 300f
    val barcodeHeight = 150f
    val barWidth = barcodeWidth / (95)

    val leftOddPatterns = mapOf(
        '0' to "0001101",
        '1' to "0011001",
        '2' to "0010011",
        '3' to "0111101",
        '4' to "0100011",
        '5' to "0110001",
        '6' to "0101111",
        '7' to "0111011",
        '8' to "0110111",
        '9' to "0001011"
    )

    val leftEvenPatterns = mapOf(
        '0' to "0100111",
        '1' to "0110011",
        '2' to "0011011",
        '3' to "0100001",
        '4' to "0011101",
        '5' to "0111001",
        '6' to "0000101",
        '7' to "0010001",
        '8' to "0001001",
        '9' to "0010111"
    )

    val rightPatterns = mapOf(
        '0' to "1110010",
        '1' to "1100110",
        '2' to "1101100",
        '3' to "1000010",
        '4' to "1011100",
        '5' to "1001110",
        '6' to "1010000",
        '7' to "1000100",
        '8' to "1001000",
        '9' to "1110100"
    )

    val leftPatternParity = arrayOf(
        "OOOOOO", // 0
        "OOEOEE", // 1
        "OOEEOE", // 2
        "OOEEEO", // 3
        "OEOOEE", // 4
        "OEEOOE", // 5
        "OEEEOO", // 6
        "OEOEOE", // 7
        "OEOEEO", // 8
        "OEEOEO"  // 9
    )

    Canvas(modifier = Modifier.size(barcodeWidth.dp, barcodeHeight.dp)) {
        var xPosition = 0f

        val startCode = "101"
        for (bit in startCode) {
            val color = if (bit == '1') Color.Black else Color.White
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(xPosition, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, barcodeHeight)
            )
            xPosition += barWidth
        }

        val leftParity = leftPatternParity[ean13[0].toString().toInt()]

        for (i in 1..6) {
            val digit = ean13[i]
            val pattern = if (leftParity[i - 1] == 'O') {
                leftOddPatterns[digit]!!
            } else {
                leftEvenPatterns[digit]!!
            }
            for (bit in pattern) {
                val color = if (bit == '1') Color.Black else Color.White
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(xPosition, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth, barcodeHeight)
                )
                xPosition += barWidth
            }
        }

        val middleCode = "01010"
        for (bit in middleCode) {
            val color = if (bit == '1') Color.Black else Color.White
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(xPosition, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, barcodeHeight)
            )
            xPosition += barWidth
        }

        for (i in 7..12) {
            val digit = ean13[i]
            val pattern = rightPatterns[digit]!!
            for (bit in pattern) {
                val color = if (bit == '1') Color.Black else Color.White
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(xPosition, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth, barcodeHeight)
                )
                xPosition += barWidth
            }
        }

        val endCode = "101"
        for (bit in endCode) {
            val color = if (bit == '1') Color.Black else Color.White
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(xPosition, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, barcodeHeight)
            )
            xPosition += barWidth
        }
    }
}


fun calculateCheckDigit(ean: String): String {
    val sum = ean.mapIndexed { index, c ->
        val digit = c.toString().toInt()
        if (index % 2 == 0) digit else digit * 3
    }.sum()

    val remainder = sum % 10
    return if (remainder == 0) "0" else (10 - remainder).toString()
}
