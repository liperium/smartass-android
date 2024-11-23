package com.liara.smartass.static_objects

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.liara.smartass.GlobalVars
import com.liara.smartass.MainActivity
import com.liara.smartass.data.AgendaItem
import com.liara.smartass.data.AgendaItemDao
import com.liara.smartass.data.AgendaState
import com.liara.smartass.data.ImageType
import com.liara.smartass.data.LoginInfo
import com.liara.smartass.data.MapsTransportType
import com.liara.smartass.data.RecurringItem
import com.liara.smartass.data.RecurringItemAtDateDao
import com.liara.smartass.data.RecurringItemDao
import com.liara.smartass.data.RecurringItemView
import com.liara.smartass.data.RouteResult
import com.liara.smartass.data.StoredImage
import com.liara.smartass.static_objects.NetworkHelper.checkNetwork
import com.liara.smartass.static_objects.StoredNotificationManager.removeAllNotifications
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit


object RemoteManager {
    // Could maybe use a library for all of this volley/retrofit
    private const val REST_API_URL = "https://hdp-rest.mattysgervais.com"
    private const val GET_ALL_URL = "$REST_API_URL/rpc/get_all_agenda_items"
    private const val ADD_AGENDA_ITEM_URL = "$REST_API_URL/rpc/add_agenda_item"
    private const val ADD_RECURRING_ITEM_URL = "$REST_API_URL/rpc/add_recurring_item"
    private const val UPDATE_RECURRING_ITEM_URL = "$REST_API_URL/rpc/update_recurring_item"
    private const val DELETE_RECURRING_ITEM_URL = "$REST_API_URL/rpc/delete_recurring_item"
    private const val DELETE_URL = "$REST_API_URL/rpc/delete_agenda_item"
    private const val TS_FAST_UPDATE_URL = "$REST_API_URL/rpc/fast_update"
    private const val TS_FAST_UPDATE_RECURRING_URL = "$REST_API_URL/rpc/fast_update_recurring"
    private const val TS_FAST_UPDATE_DONE_RECURRING_URL =
        "$REST_API_URL/rpc/fast_update_done_recurring"
    private const val UPDATE_URL = "$REST_API_URL/rpc/update_agenda_item"
    private const val SET_DONE_RECURRING_URL = "$REST_API_URL/rpc/done_recurring_item"

    private const val GET_REC_ALL_URL = "$REST_API_URL/rpc/get_all_recurring_items"

    private const val IMG_API_URL = "https://hdp-img.mattysgervais.com"
    private const val GET_IMAGE_URL = "$IMG_API_URL/get_image"
    private const val UPLOAD_IMAGE_URL = "$IMG_API_URL/upload_image"


    private val httpClient = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build()

    private const val TAG = "RemoteManager2"

    private fun genericJsonPostRemote(
        context: Context, requestBody: String, url: String
    ): String? {
        checkNetwork(context)

        //Boilerplate
        val token = APITokenManager.getValidToken(context)

        // Request
        val mediaType = "application/json".toMediaType()

        val request = Request.Builder().url(url).post(requestBody.toRequestBody(mediaType))
            .addHeader("authorization", "Bearer $token").build()

        return try {
            Log.d(TAG, "Sending POST request with body: $requestBody")
            val response = httpClient.newCall(request).execute()
            Log.d(TAG, "Received POST request response: ${response.code}")

            if (response.code == 200) {
                response.body!!.string()
            } else {
                printErrors(response)
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //Returns response only if OK
    private fun genericPostMultipartRemote(
        context: Context, requestBody: MultipartBody, url: String
    ): Response? {
        checkNetwork(context)
        val request = Request.Builder().url(url).post(requestBody).build()
        try {
            val response = httpClient.newCall(request).execute()
            if (response.code == 200) {
                return response
            } else {
                printErrors(response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return null
    }

    // Gets all remote agenda items, and returns them as a list. It also adds notifications for them.
    fun getAllAgendaItems(
        context: Context, loginInfo: LoginInfo, agendaItemDao: AgendaItemDao
    ): List<AgendaItem>? {
        checkNetwork(context)

        //Boilerplate
        val token = APITokenManager.getValidToken(context)

        val request = Request.Builder().url("${GET_ALL_URL}?_auth_id=${loginInfo.onlyId()}")
            .addHeader("authorization", "Bearer $token").build()
        try {
            val requestTimestamp = Instant.now()
            val response = httpClient.newCall(request).execute()
            if (response.code == 200) {
                val stringJsonArray = response.body!!.string()
                val agendaItems = mutableListOf<AgendaItem>()

                val jsonArray = Json.parseToJsonElement(stringJsonArray).jsonArray
                for (json in jsonArray) {
                    agendaItems.add(AgendaItem.fromJson(json.toString()).first)
                }
                removeAllNotifications()
                agendaItemDao.deleteTable()
                agendaItemDao.insertAllAndReplace(*agendaItems.toTypedArray())
                StoredNotificationManager.updateNotificationForItems(agendaItems, context)

                // Update the last update timestamp
                GlobalVars.updateTimestamp(MainActivity.loginInfoDao, requestTimestamp)

                return agendaItems
            } else {
                printErrors(response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return null
    }


    //Adds an agenda item to the remote database, and returns the item if successful. Adds a notification for the item.
    fun addAgendaItem(
        context: Context, loginInfo: LoginInfo, newItem: AgendaItem, agendaItemDao: AgendaItemDao
    ): AgendaItem? {
        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_title": "${newItem.title}","_description": "${newItem.description}",
        "_importance":  ${newItem.importance.value},"_startts": "${
                AgendaItem.convertInstant(newItem.beginInstant)
            }","_endts": "${AgendaItem.convertInstant(newItem.endInstant)}","_verif_method": "${newItem.verifMethod.value}"}"""

        return try {
            val responseString =
                genericJsonPostRemote(context, requestBody, ADD_AGENDA_ITEM_URL) ?: return null

            val json = Json.parseToJsonElement(responseString).jsonArray[0]
            val addedItem = AgendaItem.fromJson(json.toString()).first
            agendaItemDao.insertAllAndReplace(addedItem)

            StoredNotificationManager.updateNotificationForItems(listOf(addedItem), context)

            addedItem
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //Updates an agenda item in the remote database, and returns true if successful. Updates the notification time.
    fun updateAgendaItem(
        context: Context,
        loginInfo: LoginInfo,
        updatedItem: AgendaItem,
        agendaItemDao: AgendaItemDao,
        agendaItemState: MutableState<AgendaState>
    ): Boolean {
        val initialState = agendaItemState.value
        agendaItemState.value = updatedItem.getNewState()

        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_id": "${updatedItem.uid}","_title": "${updatedItem.title}","_description": "${updatedItem.description}","_done": "${updatedItem.done}","_importance":  ${updatedItem.importance.value},"_startts": "${
                AgendaItem.convertInstant(updatedItem.beginInstant)
            }","_endts": "${AgendaItem.convertInstant(updatedItem.endInstant)}"}"""

        return try {
            val response = genericJsonPostRemote(context, requestBody, UPDATE_URL)
            return if (response != null) {
                agendaItemDao.update(updatedItem)
                //TODO Call getUpdates to update the last update timestamp, and the removed item
                // Change notification time TODO test
                StoredNotificationManager.updateNotificationForItems(
                    listOf(updatedItem), context
                )
                true
            } else {
                agendaItemState.value = initialState
                false
            }
        } catch (e: Exception) {
            agendaItemState.value = initialState
            e.printStackTrace()
            false
        }
    }

    //Deletes an agenda item in the remote database, and returns true if successful. Removes the notification.
    fun deleteAgendaItem(
        context: Context,
        loginInfo: LoginInfo,
        itemToDelete: AgendaItem,
        agendaItemDao: AgendaItemDao
    ): Boolean {

        val requestBody = """{"_auth_id": "${loginInfo.onlyId()}","_item_id":${itemToDelete.uid}}"""

        return try {
            genericJsonPostRemote(context, requestBody, DELETE_URL) ?: return false
            agendaItemDao.delete(itemToDelete)
            StoredNotificationManager.updateNotificationForItems(listOf(itemToDelete), context)

            //TODO Call getUpdates to update the last update timestamp, and the removed item
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllFastUpdate(
        context: Context,
        loginInfo: LoginInfo,
        agendaItemDao: AgendaItemDao,
        recurringItemDao: RecurringItemDao,
        recurringItemAtDateDao: RecurringItemAtDateDao
    ): Boolean {
        val requestBody = """
        {
        "_auth_id": "${loginInfo.onlyId()}",
        "_update_from_ts": "${loginInfo.lastUpdateTimestamp}"
        }
        """

        return try {
            val startRequestTimestamp = Instant.now()
            val agendaItemsResponse =
                genericJsonPostRemote(context, requestBody, TS_FAST_UPDATE_URL) ?: return false
            val recurringReturnedString =
                genericJsonPostRemote(context, requestBody, TS_FAST_UPDATE_RECURRING_URL)
                    ?: return false
            val doneRecurringReturnedString =
                genericJsonPostRemote(context, requestBody, TS_FAST_UPDATE_DONE_RECURRING_URL)
                    ?: return false

            val agendaItems = mutableListOf<AgendaItem>()
            val recurringItems = mutableListOf<RecurringItem>()
            for (json in Json.parseToJsonElement(agendaItemsResponse).jsonArray) {
                val pair = AgendaItem.fromJson(json.toString())
                // If the item is marked as deleted, delete it from the database
                if (pair.second) {
                    agendaItemDao.delete(pair.first)
                } else {
                    agendaItems.add(pair.first)
                }
            }
            for (json in Json.parseToJsonElement(recurringReturnedString).jsonArray) {
                val pair = RecurringItem.fromJson(json.toString())
                // If the item is marked as deleted, delete it from the database
                if (pair.second) {
                    recurringItemDao.delete(pair.first)
                } else {
                    recurringItems.add(pair.first)
                }
            }

            agendaItemDao.insertAllAndReplace(*agendaItems.toTypedArray())
            StoredNotificationManager.updateNotificationForItems(agendaItems, context)

            recurringItemDao.insertAllAndReplace(*recurringItems.toTypedArray())
            RecurringItem.buildRecurringItemAtDate(recurringItems)
            // TODO call service to update recurring items StoredNotificationManager.updateNotificationForItems(allRecurringItemsAtDaysView, context)
            for (json in Json.parseToJsonElement(doneRecurringReturnedString).jsonArray) {
                println(json.toString())
                recurringItemAtDateDao.setDone(
                    json.jsonObject["done"]!!.jsonPrimitive.boolean,
                    json.jsonObject["occurence_number"]!!.jsonPrimitive.int,
                    json.jsonObject["recurring_item_id"]!!.jsonPrimitive.int
                )
            }
            // Update the last update timestamp
            GlobalVars.updateTimestamp(MainActivity.loginInfoDao, startRequestTimestamp)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getImage(
        context: Context,
        type: ImageType,
        uniqueItemString: String,
        loginInfo: LoginInfo
    ): StoredImage? {
        val body =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user_id", loginInfo.onlyId())
                .addFormDataPart("task_id", uniqueItemString)
                .addFormDataPart("task_type", type.ordinal.toString()).build()
        // todo refresh token if necessary
        val response = genericPostMultipartRemote(context, body, GET_IMAGE_URL)
        if (response == null) {
            VisualFeedbackManager.showMessage("Failed to get image")
            return null
        }

        // Convert to bitmap
        val stream = response.body!!.byteStream()
        val decodedByte = BitmapFactory.decodeStream(stream)

        return StoredImage(decodedByte, 0, uniqueItemString, type)
    }

    fun uploadImage(
        context: Context,
        image: File,
        uniqueItemString: String,
        type: ImageType,
        loginInfo: LoginInfo
    ) {
        val requestBody = image.asRequestBody("image/*".toMediaTypeOrNull())
        val body =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user_id", loginInfo.onlyId())
                .addFormDataPart("image", image.path, requestBody)
                .addFormDataPart("task_id", uniqueItemString)
                .addFormDataPart("task_type", type.ordinal.toString()).build()
        // todo refresh token if necessary
        val responseBody = genericPostMultipartRemote(context, body, UPLOAD_IMAGE_URL)
        if (responseBody == null) {
            VisualFeedbackManager.showMessage("Failed to post image")
        }

    }

    fun getPolylinePoints(from: LatLng, to: LatLng, mean: MapsTransportType): RouteResult? {
        // get users current time using google format 2023-10-15T15:01:23.045123456Z
        val currentTime = Instant.now().plusSeconds(30).toString()
        var time = """
      "departureTime": "$currentTime",
    """.trimIndent()
        // Can't put the time if driving by car, see api.
        if (mean == MapsTransportType.Car) time = ""
        val json = """
        {
          "origin": {
            "location": {
              "latLng": {
                "latitude": ${from.latitude},
                "longitude": ${from.longitude}
              }
            }
          },
          "destination": {
            "location": {
              "latLng": {
                "latitude": ${to.latitude},
                "longitude": ${to.longitude}
              }
            }
          },
          "travelMode": "${mean.mapsApiMapping}",
          $time
          "computeAlternativeRoutes": false,
          "languageCode": "en-US",
          "units": "METRIC"
        }
    """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request =
            Request.Builder().url("https://routes.googleapis.com/directions/v2:computeRoutes")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", /*Your API key*/).addHeader(
                    "X-Goog-FieldMask",
                    "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.travelAdvisory,routes.legs"
                ).post(requestBody).build()
        val response = httpClient.newCall(request).execute()
        if (response.code != 200) {
            printErrors(response)
            return null
        }
        val responseBody = response.body!!.string()
        Log.d(TAG, "Received POST request response: $responseBody")
        if (responseBody == "{}\n") {
            return null
        }
        // parse to json
        val jsonElement = Json.parseToJsonElement(responseBody)
        // routes -> polyline -> encodedPolyline
        var nestedEncoded =
            jsonElement.jsonObject["routes"]!!.jsonArray[0].jsonObject["polyline"]!!.jsonObject.getValue(
                "encodedPolyline"
            )
                .toString()
        // remove quotes
        nestedEncoded =
            nestedEncoded.replace("\"", "").replace("\\\\", "\\") // Removes quotes and java escapes
        // decode polyline
        val decoded = PolyUtil.decode(nestedEncoded)

        val distance =
            jsonElement.jsonObject["routes"]!!.jsonArray[0].jsonObject["distanceMeters"]!!.toString()
                .toDouble()

        val duration =
            jsonElement.jsonObject["routes"]!!.jsonArray[0].jsonObject["duration"]!!.toString()
                .replace("s", "").replace("\"", "").toInt()

        return RouteResult(decoded, distance, duration)
    }

    private fun printErrors(response: Response) {
        val errorString = "Server error: ${response.code}"
        Log.e(TAG, "body: " + errorString + "\n" + response.body?.string())
        VisualFeedbackManager.showMessage(errorString)
    }

    // Gets all remote agenda items, and returns them as a list. It also adds notifications for them.
    @Deprecated("Use fastupdateall")
    fun getAllRecurringItems(
        context: Context,
        loginInfo: LoginInfo,
        recurringItemDao: RecurringItemDao,
        recurringItemAtDateDao: RecurringItemAtDateDao
    ): List<RecurringItem>? {
        checkNetwork(context)

        //Boilerplate
        val token = APITokenManager.getValidToken(context)

        val request = Request.Builder().url("${GET_REC_ALL_URL}?_auth_id=${loginInfo.onlyId()}")
            .addHeader("authorization", "Bearer $token").build()
        try {
            val requestTimestamp = Instant.now() // TODO removee??
            val response = httpClient.newCall(request).execute()
            if (response.code == 200) {
                val stringJsonArray = response.body!!.string()
                val recurringItems = mutableListOf<RecurringItem>()

                val jsonArray = Json.parseToJsonElement(stringJsonArray).jsonArray
                for (json in jsonArray) {
                    recurringItems.add(RecurringItem.fromJson(json.toString()).first)
                }
                removeAllNotifications()
                recurringItemDao.deleteTable()
                recurringItemDao.insertAllAndReplace(*recurringItems.toTypedArray())

                // TODO add at ~5 day interval??
                // StoredNotificationManager.addNotificationForItems(recurringItems.asIterable(), context)

                // Update the last update timestamp
                GlobalVars.updateTimestamp(MainActivity.loginInfoDao, requestTimestamp)

                recurringItemDao.deleteTable()
                RecurringItem.buildRecurringItemAtDate(recurringItems)

                return recurringItems
            } else {
                printErrors(response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return null
    }

    fun deleteRecurringItem(
        context: Context,
        loginInfo: LoginInfo,
        recurringItemView: RecurringItemView,
        recurringItemDao: RecurringItemDao
    ): Boolean {

        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_item_id":${recurringItemView.recurringItemData.uid}}"""

        return try {
            genericJsonPostRemote(context, requestBody, DELETE_RECURRING_ITEM_URL)
                ?: return false
            recurringItemDao.delete(recurringItemView.recurringItemData)
            StoredNotificationManager.updateNotificationForItems(
                listOf(recurringItemView),
                context
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun addRecurringItem(
        appContext: Context,
        loginInfo: LoginInfo,
        newItem: RecurringItem,
        recurringItemDao: RecurringItemDao
    ): RecurringItem? {
        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_title": "${newItem.title}","_description": "${newItem.description}",
        "_importance":  ${newItem.importance.value},"_rrule": "${
                newItem.rrule.toString().trimEnd().trimStart('R', 'R', 'U', 'L', 'E', ':')
            }","_first_occurrence": "${AgendaItem.convertInstant(newItem.first_occurence)}","_duration": "0 years 0 mons 0 days 0 hours ${newItem.duration} mins 0.0 secs","_verif_method": ${newItem.verif_method.value}}"""

        return try {
            val responseString =
                genericJsonPostRemote(appContext, requestBody, ADD_RECURRING_ITEM_URL)
                    ?: return null

            val json = Json.parseToJsonElement(responseString).jsonArray[0]
            val addedItem = RecurringItem.fromJson(json.toString()).first
            recurringItemDao.insertAllAndReplace(addedItem)
            RecurringItem.buildRecurringItemAtDate(listOf(addedItem))
            TimeSensitiveNotificationBuilder.updateNotificationsNow(appContext)

            addedItem
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun doneRecurringItem(
        appContext: Context,
        loginInfo: LoginInfo,
        toUpdateItem: RecurringItemView,
        recurringItemAtDateDao: RecurringItemAtDateDao
    ): Boolean {
        val recurringId = toUpdateItem.recurringItemData.uid
        val occurrenceId = toUpdateItem.viewId
        val newDoneState = toUpdateItem.done

        //_auth_id varchar(24), _recurring_id integer, _occurence_number integer, _done boolean
        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_recurring_id": ${recurringId},"_occurence_number": ${occurrenceId},"_done": ${newDoneState}}"""

        return try {
            val responseString =
                genericJsonPostRemote(appContext, requestBody, SET_DONE_RECURRING_URL)
                    ?: return false
            Log.d(TAG, "recurring done response: $responseString")
            recurringItemAtDateDao.setDone(newDoneState, occurrenceId, recurringId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // For simplicity reasons, we don't update the rrules here, as it would conflict with our done states
    fun updateRecurringItem(
        appContext: Context,
        loginInfo: LoginInfo,
        updatedItem: RecurringItem,
        recurringItemDao: RecurringItemDao
    ): RecurringItem? {
        val requestBody =
            """{"_auth_id": "${loginInfo.onlyId()}","_item_id":${updatedItem.uid},"_title": "${updatedItem.title}","_description": "${updatedItem.description}",
        "_importance":  ${updatedItem.importance.value},"_rrule": "${
                updatedItem.rrule.toString().trimEnd().trimStart('R', 'R', 'U', 'L', 'E', ':')
            }","_first_occurrence": "${AgendaItem.convertInstant(updatedItem.first_occurence)}","_duration": "0 years 0 mons 0 days 0 hours ${updatedItem.duration} mins 0.0 secs","_verif_method": ${updatedItem.verif_method.value},"_deleted":${false}}"""

        return try {
            val responseString =
                genericJsonPostRemote(appContext, requestBody, UPDATE_RECURRING_ITEM_URL)
                    ?: return null

            val json = Json.parseToJsonElement(responseString).jsonArray[0]
            val addedItem = RecurringItem.fromJson(json.toString()).first
            recurringItemDao.insertAllAndReplace(addedItem)
            RecurringItem.buildRecurringItemAtDate(listOf(addedItem))
            TimeSensitiveNotificationBuilder.updateNotificationsNow(appContext)

            addedItem
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
