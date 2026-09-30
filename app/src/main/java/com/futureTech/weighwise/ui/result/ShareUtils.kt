package com.futureTech.weighwise.ui.result

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.domain.OptionResult
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareResult(context: Context, decision: DecisionEntity, results: List<OptionResult>) {
        val bitmap = createSummaryBitmap(decision, results)
        
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "decision_result.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "I just made a decision: ${decision.title}. The winner is ${results.firstOrNull()?.option?.name}!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Decision"))
    }

    private fun createSummaryBitmap(decision: DecisionEntity, results: List<OptionResult>): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Background
        paint.color = android.graphics.Color.parseColor("#FAF7F2") // LightBackground
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Title
        paint.color = android.graphics.Color.parseColor("#0B1B3A") // LightInk
        paint.textSize = 64f
        paint.isFakeBoldText = true
        canvas.drawText(decision.title, 64f, 100f, paint)

        // Winner
        val winner = results.firstOrNull()
        if (winner != null) {
            paint.color = android.graphics.Color.parseColor("#12B5A6") // PrimaryTeal
            paint.textSize = 48f
            paint.isFakeBoldText = false
            canvas.drawText("Winner: ${winner.option.name}", 64f, 200f, paint)
            
            // Score bar
            paint.color = android.graphics.Color.parseColor("#FFB020") // Amber
            canvas.drawRoundRect(RectF(64f, 240f, 64f + (winner.score * 5f), 280f), 20f, 20f, paint)
        }

        // Runner up
        val runnerUp = results.getOrNull(1)
        if (runnerUp != null) {
            paint.color = android.graphics.Color.parseColor("#6B7590") // SecondaryText
            paint.textSize = 32f
            canvas.drawText("Runner up: ${runnerUp.option.name}", 64f, 360f, paint)
        }
        
        // Branding
        paint.color = android.graphics.Color.parseColor("#6B7590")
        paint.textSize = 24f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Decided with WeighWise", width - 40f, height - 40f, paint)

        return bitmap
    }
}
