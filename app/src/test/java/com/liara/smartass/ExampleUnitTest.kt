package com.liara.smartass

import com.google.android.gms.maps.model.LatLng
import com.liara.smartass.data.AgendaItem
import com.liara.smartass.data.AgendaState
import com.liara.smartass.data.Importance
import com.liara.smartass.data.MapsTransportType
import com.liara.smartass.data.VerifMethod
import com.liara.smartass.static_objects.RemoteManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun getRoute() {
    val home = LatLng(48.4356, -71.1153)
    val uqac = LatLng(48.418948, -71.052471)
      assertNotNull(RemoteManager.getPolylinePoints(home, uqac, mean = MapsTransportType.Transit))
  }

  @Test
  fun singleItem() {
    var time = Instant.now().minusSeconds(5)

    val passedAgendaItem = AgendaItem(
      uid = 0,
      title = "test",
      description = "test",
      importance = Importance.Basic,
      beginInstant = time,
      endInstant = time,
      verifMethod = VerifMethod.Aucune,
      state = AgendaState.Upcoming
    )
    passedAgendaItem.updateState()
    assertEquals(AgendaState.PassedBasicNotDone, passedAgendaItem.state)
    assert(passedAgendaItem.getStartTime() < passedAgendaItem.getEndTime())

    time = Instant.now().plusSeconds(60)
    val futureAgendaItem = AgendaItem(
      uid = 0,
      title = "test",
      description = "test",
      importance = Importance.Important,
      beginInstant = time,
      endInstant = time,
      verifMethod = VerifMethod.Aucune,
      state = AgendaState.Upcoming
    )
    assertEquals(AgendaState.Upcoming, futureAgendaItem.state)


  }
}
