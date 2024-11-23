package com.liara.smartass.static_objects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Message(val message: String, val id: Int, val type: MessageType) {
  enum class MessageType {
    Error,
    ApplicationFeedback
  }
  companion object {
    var n = 0
  }
}

object VisualFeedbackManager {

  private lateinit var currentShownMessage: MutableState<Message?>
  private var messageList = mutableListOf<Message>()
  fun showMessage(message: String, type: Message.MessageType = Message.MessageType.Error) {
    val thisCallMessage = Message(message, Message.n++, type)
    messageList.add(thisCallMessage)
    startOrNextMessage()

  }

  private fun startOrNextMessage() {
    if (currentShownMessage.value == null) {
      currentShownMessage.value = messageList[0]
      CoroutineScope(Dispatchers.Main).launch {
        delay(2000) // Wait for 1 second
        if (currentShownMessage.value!!.id == messageList[0].id) {
          currentShownMessage.value = null
          messageList.removeAt(0)
          if (messageList.isNotEmpty()) {
            startOrNextMessage()
          }
        }
      }
    }

  }
  @Composable
  fun Popup() {
    currentShownMessage = remember { mutableStateOf(null) }

    Box(
      modifier = Modifier.fillMaxSize(), // Fill the parent
      contentAlignment = Alignment.BottomCenter // Align children to bottom-end (bottom-right)
    ) {
      if (currentShownMessage.value != null) {
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
          val prefix =
            if (currentShownMessage.value!!.type == Message.MessageType.Error) "Erreur : " else ""
          val shownText = "$prefix${currentShownMessage.value!!.message}"
          Text(
            shownText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
