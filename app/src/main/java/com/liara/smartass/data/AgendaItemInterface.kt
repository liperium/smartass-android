package com.liara.smartass.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.content.FileProvider
import com.liara.smartass.BuildConfig
import com.liara.smartass.GlobalVars
import com.liara.smartass.MainActivity
import com.liara.smartass.static_objects.RemoteManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.time.LocalTime

const val INTERFACE_TAG = "AgendaItemInterface"
interface AgendaItemInterface {
  val verifMethod: VerifMethod
  var done: Boolean
  val state: AgendaState
  val title: String
  val description: String

  fun updateState()

  fun getHourInDay(): Int
  fun getStartTime(): LocalTime
  fun getEndTime(): LocalTime
  fun getItemImportance(): Importance
  fun getType(): AgendaItemTypes

  fun deleteSelf(context: Context)
  fun getImage(proof: ImageType): StoredImage? {
    return RemoteManager.getImage(
      MainActivity.appContext, proof, getUniqueString(), GlobalVars.userLoginInfo.value!!
    )
  }

  suspend fun checkDoneWithVerifMethod(
    applicationContext: Context,
    newDesiredCheck: Boolean
  ) {
    // If we want to remove, we shouldn't check checks
    if(!newDesiredCheck){
      done = false
      return
    }
    done = when (verifMethod) {

      VerifMethod.Aucune -> newDesiredCheck

      VerifMethod.Image -> {
        Log.v(
          INTERFACE_TAG,
          "Starting image verification"
        )// Adding image - ask to take picture coroutine
        withContext(AgendaItem.myDispatcher) {
          // Take picture
          val tempFile = StoredImage.createImageFile(applicationContext)
          val uri = FileProvider.getUriForFile(
            applicationContext, BuildConfig.APPLICATION_ID + ".provider", tempFile
          )

          GlobalVars.cameraResultDeferred = CompletableDeferred<File?>()

          GlobalVars.getCameraImage.launch(uri)

          val imgFile = GlobalVars.cameraResultDeferred.await()

          // Resize image
          val options = BitmapFactory.Options()
          options.inSampleSize = 2
          var imgBM = BitmapFactory.decodeFile(imgFile!!.path, options)
          // Rotate image, don't know why it's 90d off.
          val matrix = Matrix().apply { postRotate(90.0f) }
          imgBM = Bitmap.createBitmap(imgBM, 0, 0, imgBM.width, imgBM.height, matrix, true)

          val byteArrayOutputStream = ByteArrayOutputStream()
          imgBM.compress(
            Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream
          ) //compress to 50% of original image quality

          val byteArray = byteArrayOutputStream.toByteArray()

          imgFile.writeBytes(byteArray)

          // Save remote
          RemoteManager.uploadImage(
            MainActivity.appContext,
            imgFile,
            getUniqueString(),
            ImageType.Proof,
            GlobalVars.userLoginInfo.value!!
          )
          Log.d(INTERFACE_TAG, "Image verification done")

          // If all went well return tru
          return@withContext true
        }

        // Removing image

        // Set done to false
        newDesiredCheck
      }
    }
  }

  // Returns true if updated in remote DB
  fun updateSelfRemote(context: Context, state: MutableState<AgendaState>): Boolean
  fun getUniqueString(): String
  fun getEndNotificationInstant(): Instant
  fun getStartNotificationInstant(): Instant
  fun isPassed(fromTime: Instant = Instant.now()): Boolean
  fun isFuture(fromTime: Instant = Instant.now()): Boolean {
    return !isPassed(fromTime)
  }
}

enum class VerifMethod(val value: Int) {
  Aucune(0),
  Image(1),
  ;

  companion object {
    fun fromInt(value: Int) = VerifMethod.values().first { it.value == value }
  }
}

enum class AgendaItemTypes {
  Single,
  Recurring
}