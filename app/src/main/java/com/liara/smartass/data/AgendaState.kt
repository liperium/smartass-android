package com.liara.smartass.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.liara.smartass.ui.theme.Black
import com.liara.smartass.ui.theme.DoneGreen
import com.liara.smartass.ui.theme.GrayNotImportantNotDone
import com.liara.smartass.ui.theme.GrayUpcoming
import com.liara.smartass.ui.theme.RedImportantNotDone
import com.liara.smartass.ui.theme.White

enum class AgendaState {
  Upcoming,
  PassedBasicNotDone,
  PassedImportantNotDone,
    PassedModerateNotDone,
  Done,
  ;
  val backgroundColor: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
          Upcoming -> GrayUpcoming
          PassedImportantNotDone -> RedImportantNotDone
          PassedBasicNotDone -> GrayNotImportantNotDone
            PassedModerateNotDone -> GrayNotImportantNotDone
          Done -> DoneGreen
        }
  val textColor: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
          Upcoming -> Black
          PassedImportantNotDone -> Black
          PassedBasicNotDone -> White
            PassedModerateNotDone -> White
          Done -> Black
        }
}
