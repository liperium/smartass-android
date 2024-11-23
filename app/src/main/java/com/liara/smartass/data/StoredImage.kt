package com.liara.smartass.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.liara.smartass.GlobalVars
import java.io.File
import java.io.IOException
import java.util.Date

enum class ImageType {
  Proof,
  Help
}

data class StoredImage(
  val imageData: Bitmap,
  val imageID: Int,
  val uniqueItemString: String,
  val imageType: ImageType
) {

  companion object {
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
      // Create an image file name
      val timeStamp: String = Date().time.toString()
      val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
      ).apply {
        // Save a file: path for use with ACTION_VIEW intents
        GlobalVars.latestPicturePath = absolutePath
      }
    }
  }
}