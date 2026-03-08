package com.drewzillawood.customprogressbar.data.model

import com.intellij.openapi.components.BaseState
import kotlinx.serialization.Serializable
import java.awt.Color

private const val CYCLE_TIME_DEFAULT: Int = 800
private const val REPAINT_INTERVAL_DEFAULT: Int = 50

private val DEFAULT_PRIMARY_PINK = Color(0xFF, 0x3C, 0xAC)
private val DEFAULT_SECONDARY_BLUE = Color(0x46, 0xE0, 0xFF)

@Serializable
class PersistentConfigs : BaseState() {
  var myIndeterminatePrimaryColor: Int by property(DEFAULT_PRIMARY_PINK.rgb)
  var myIndeterminateSecondaryColor: Int by property(DEFAULT_SECONDARY_BLUE.rgb)
  var myDeterminatePrimaryColor: Int by property(DEFAULT_PRIMARY_PINK.rgb)
  var myDeterminateSecondaryColor: Int by property(DEFAULT_SECONDARY_BLUE.rgb)
  var isAdvancedOptionsEnabled: Boolean by property(false)
  var cycleTime: Int by property(CYCLE_TIME_DEFAULT)
  var repaintInterval: Int by property(REPAINT_INTERVAL_DEFAULT)
  var isCustomImageEnabled: Boolean by property(false)
  var imagePath: String? by string(null)
  var isCustomDimensionsEnabled: Boolean by property(false)
  var height: Int by property(5)
  var radius: Int by property(5)
}
