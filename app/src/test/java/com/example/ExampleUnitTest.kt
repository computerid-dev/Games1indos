package com.example

import android.view.KeyEvent
import com.example.ui.INDONESIA_STAGES
import com.example.ui.components.mapAndroidKeyCodeToJs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun stagesContainThreeIndonesianRegions() {
        assertEquals(3, INDONESIA_STAGES.size)
        assertEquals("Jakarta - Monas", INDONESIA_STAGES[0].titleId)
        assertEquals("Yogyakarta - Prambanan", INDONESIA_STAGES[1].titleId)
        assertEquals("Bali - Pesisir Dewata", INDONESIA_STAGES[2].titleId)
    }

    @Test
    fun hardwareKeyMappingMapsSteeringJumpAndTurbo() {
        assertEquals("ArrowLeft", mapAndroidKeyCodeToJs(KeyEvent.KEYCODE_A))
        assertEquals("ArrowRight", mapAndroidKeyCodeToJs(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals("Space", mapAndroidKeyCodeToJs(KeyEvent.KEYCODE_SPACE))
        assertEquals("ShiftLeft", mapAndroidKeyCodeToJs(KeyEvent.KEYCODE_E))
        assertNull(mapAndroidKeyCodeToJs(KeyEvent.KEYCODE_VOLUME_UP))
    }
}
