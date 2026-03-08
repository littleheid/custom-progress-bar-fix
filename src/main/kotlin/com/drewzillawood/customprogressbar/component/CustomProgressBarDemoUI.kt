package com.drewzillawood.customprogressbar.component

import com.drewzillawood.customprogressbar.data.PersistentDemoConfigsComponent
import com.intellij.openapi.components.service
import com.intellij.ui.icons.EMPTY_ICON
import com.intellij.util.ImageLoader
import java.awt.Color
import java.awt.Image
import java.io.File

open class CustomProgressBarDemoUI : CustomProgressBarUI() {

  private var currentDemo = service<PersistentDemoConfigsComponent>().state

  override fun getCycleTime(): Int {
    return currentDemo.cycleTime
  }

  override fun getRepaintInterval(): Int {
    return currentDemo.repaintInterval
  }

  override fun isCustomImageEnabled(): Boolean {
    return currentDemo.isCustomImageEnabled
  }

  override fun loadImageAndScale(): Image = (
    currentDemo.imagePath?.takeIf { it.isNotEmpty() }
      ?.let { ImageLoader.loadFromUrl(File(it).toURI().toURL())
        ?.getScaledInstance(16, 16, Image.SCALE_SMOOTH) } ?: EMPTY_ICON.image)

  override fun getHeight(): Int {
    return currentDemo.height
  }

  override fun getRadius(): Int {
    return currentDemo.radius
  }

  override fun getIndeterminateSecondaryColor(): Color {
    return Color(currentDemo.myIndeterminateSecondaryColor)
  }

  override fun getIndeterminatePrimaryColor(): Color {
    return Color(currentDemo.myIndeterminatePrimaryColor)
  }

  override fun getDeterminatePrimaryColor(): Color {
    return Color(currentDemo.myDeterminatePrimaryColor)
  }

  override fun getDeterminateSecondaryColor(): Color {
    return Color(currentDemo.myDeterminateSecondaryColor)
  }
}
