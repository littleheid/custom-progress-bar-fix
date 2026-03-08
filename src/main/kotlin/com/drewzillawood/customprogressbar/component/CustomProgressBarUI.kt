package com.drewzillawood.customprogressbar.component

import com.drewzillawood.customprogressbar.data.PersistentConfigsComponent
import com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI
import com.intellij.openapi.components.service
import com.intellij.ui.icons.EMPTY_ICON
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ImageLoader
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.UIUtilities
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.LinearGradientPaint
import java.awt.MultipleGradientPaint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.JComponent
import javax.swing.JProgressBar
import javax.swing.UIManager
import kotlin.math.max
import kotlin.math.min

open class CustomProgressBarUI : DarculaProgressBarUI() {

  @Volatile
  private var indeterminateOffset = -20

  private var velocity: Int = 1

  private val DEFAULT_WIDTH = 4

  private var current = service<PersistentConfigsComponent>().state

  override fun updateIndeterminateAnimationIndex(startMillis: Long) {
    val numFrames = getSafeFrameCount()
    val timePassed = System.currentTimeMillis() - startMillis
    this.animationIndex = (timePassed / getSafeRepaintInterval().toLong() % numFrames.toLong()).toInt()
  }

  override fun installDefaults() {
    super.installDefaults()
    velocity = getSafeAnimationVelocity()
    UIManager.put("ProgressBar.repaintInterval", getSafeRepaintInterval())
    UIManager.put("ProgressBar.cycleTime", getSafeCycleTime())
  }

  override fun paintIndeterminate(g: Graphics?, component: JComponent?) {
    drawProgressBar(g, component) { c, g2d ->
      try {
        drawProgression(g2d, c)

        if (isCustomImageEnabled()) {
          drawCustomImage(g2d, c, true)
        }

        drawIndeterminateString(g2d)
      } finally {
        g2d.dispose()
      }
    }
  }

  private fun drawProgressBar(
    graphics: Graphics?,
    component: JComponent?,
    draw: (c: JComponent, g2d: Graphics2D) -> Unit
  ) {
    val g2d = graphics as? Graphics2D ?: return
    component ?: return

    if (isProgressBarInvalid()) return

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)

    draw(component, g2d)
  }

  private fun isProgressBarInvalid(): Boolean {
    return progressBar.width - (progressBar.insets.left + progressBar.insets.right) <= 0
      || progressBar.height - (progressBar.insets.top + progressBar.insets.bottom) <= 0
  }

  private fun drawProgression(
    g2d: Graphics2D,
    c: JComponent,
  ) {
    val r = Rectangle(progressBar.size)
    if (c.isOpaque) {
      g2d.color = c.parent.background
      g2d.fill(r)
    }

    val ph = progressBar.preferredSize.height

    JBInsets.removeFrom(r, progressBar.insets)
    val (x, y, w, h, ar) = r.let {
      listOf(
        it.x.toFloat(),
        (it.y + (it.height - ph) / 2).toFloat() - SCALED_PROGRESSION_HEIGHT * 2,
        it.width.toFloat(),
        SCALED_PROGRESSION_HEIGHT,
        SCALED_PROGRESSION_RADIUS
      )
    }

    val y1 = (r.y + ph / 2).toFloat()
    g2d.paint = createIndeterminateGradient(r.x.toFloat(), y1, r.width.toFloat())
    g2d.fill(getShapedRect(x, y, w, h, ar))

    val dynamicHeight = JBUIScale.scale(getHeight().toFloat())
    val dynamicRadius = JBUIScale.scale(getRadius().toFloat())
    g2d.fill(
      RoundRectangle2D.Float(
        0f * SCALED_MARGIN,
        (SCALED_PROGRESSION_HEIGHT * 2 * SCALED_MARGIN - 2) - ((dynamicHeight - SCALED_PROGRESSION_HEIGHT) / 2),
        JBUIScale.scale(r.width * 1f),
        dynamicHeight,
        dynamicRadius,
        dynamicRadius
      )
    )
  }

  open fun getHeight(): Int {
    return current.height
  }

  open fun getRadius(): Int {
    return current.radius
  }

  private fun drawCustomImage(g2d: Graphics2D, c: JComponent, isIndeterminate: Boolean) {
    val img = loadImageAndScale()
    if (EMPTY_ICON.image == img) return
    val loadingImage: BufferedImage = toBufferedImage(img)

    val offset = if (isIndeterminate) {
      incrementIndeterminateVelocity(loadingImage)
    } else {
      getAmountFull(progressBar.insets, progressBar.size.width, progressBar.size.height).toFloat()
    }

    val verticalMargin = (c.height - loadingImage.height) / 2f
    val horizontalMargin = JBUIScale.scale(-20f)
    val maxedOffset = min(
      max(horizontalMargin, offset - loadingImage.width / 2f),
      c.width - loadingImage.width - horizontalMargin
    )

    g2d.drawImage(
      loadingImage,
      affineTransform(
        tx = maxedOffset,
        ty = verticalMargin,
        sx = 1f,
        sy = 1f
      ),
      null
    )
  }

  private fun incrementIndeterminateVelocity(loadingImage: BufferedImage): Float {
    indeterminateOffset += velocity
    if (indeterminateOffset >= progressBar.size.width + loadingImage.width) {
      indeterminateOffset = -loadingImage.width
      velocity = getSafeAnimationVelocity()
    }
    return indeterminateOffset.toFloat()
  }

  protected open fun getCycleTime(): Int {
    return current.cycleTime
  }

  protected open fun getRepaintInterval(): Int {
    return current.repaintInterval
  }

  private fun getSafeRepaintInterval(): Int {
    return getRepaintInterval().coerceAtLeast(1)
  }

  private fun getSafeCycleTime(): Int {
    return getCycleTime().coerceAtLeast(getSafeRepaintInterval())
  }

  private fun getSafeFrameCount(): Int {
    return max(1, getSafeCycleTime() / getSafeRepaintInterval())
  }

  private fun getSafeAnimationVelocity(): Int {
    return max(1, getSafeFrameCount() / 4)
  }

  private fun getIndeterminatePhase(): Float {
    val cycleTime = getSafeCycleTime().toLong()
    val timeInCycle = System.currentTimeMillis() % cycleTime
    return timeInCycle.toFloat() / cycleTime.toFloat()
  }

  private fun drawIndeterminateString(g2d: Graphics2D) {
    val r = Rectangle(progressBar.size)
    if (progressBar.isStringPainted) {
      paintString(g2d, progressBar.insets.left, progressBar.insets.top, r.width, r.height, boxRect.x, boxRect.width)
    }
  }

  private fun paintString(g: Graphics2D, x: Int, y: Int, w: Int, h: Int, fillStart: Int, amountFull: Int) {
    val progressString = progressBar.string
    g.font = progressBar.font
    val renderLocation = getStringPlacement(g, progressString, x, y, w, h)
    val oldClip = g.clipBounds

    g.color = selectionBackground
    UIUtilities.drawString(progressBar, g, progressString, renderLocation.x, renderLocation.y)

    g.color = selectionForeground
    g.clipRect(fillStart, y, amountFull, h)
    UIUtilities.drawString(progressBar, g, progressString, renderLocation.x, renderLocation.y)
    g.clip = oldClip
  }

  open fun getIndeterminatePrimaryColor(): Color {
    return Color(current.myIndeterminatePrimaryColor)
  }

  open fun getIndeterminateSecondaryColor(): Color {
    return Color(current.myIndeterminateSecondaryColor)
  }

  private fun createIndeterminateGradient(x: Float, y: Float, width: Float): LinearGradientPaint {
    val primary = getIndeterminatePrimaryColor()
    val secondary = getIndeterminateSecondaryColor()
    val gradientWidth = max(JBUIScale.scale(120f), width * 0.45f)
    val patternPeriod = gradientWidth * 2f
    val offset = patternPeriod * getIndeterminatePhase()
    val startX = x - gradientWidth + offset
    val blend1 = blendColors(primary, secondary, 0.18f)
    val blend2 = blendColors(primary, secondary, 0.36f)
    val blend3 = blendColors(primary, secondary, 0.64f)
    val blend4 = blendColors(primary, secondary, 0.82f)

    return LinearGradientPaint(
      startX,
      y,
      startX + gradientWidth,
      y,
      floatArrayOf(0f, 0.18f, 0.38f, 0.62f, 0.82f, 1f),
      arrayOf(
        primary,
        blend1,
        blend2,
        blend3,
        blend4,
        secondary,
      ),
      MultipleGradientPaint.CycleMethod.REFLECT
    )
  }

  private fun blendColors(start: Color, end: Color, t: Float): Color {
    val easedT = smoothStep(t)
    return Color(
      lerp(start.red.toFloat(), end.red.toFloat(), easedT).toInt().coerceIn(0, 255),
      lerp(start.green.toFloat(), end.green.toFloat(), easedT).toInt().coerceIn(0, 255),
      lerp(start.blue.toFloat(), end.blue.toFloat(), easedT).toInt().coerceIn(0, 255),
    )
  }

  private fun smoothStep(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
  }

  private fun lerp(start: Float, end: Float, t: Float): Float {
    return start + (end - start) * t
  }

  override fun getPreferredSize(c: JComponent?): Dimension {
    val size = super.getPreferredSize(c)
    if (c !is JProgressBar) {
      return size
    }
    if (!c.isStringPainted) {
      size.height = getStripeWidth()
    }
    return Dimension(size.width, (16 + MARGIN * 4).toInt())
  }

  private fun getStripeWidth(): Int {
    val ho = progressBar.getClientProperty("ProgressBar.stripeWidth")
    return if (ho != null) {
      try {
        JBUIScale.scale(ho.toString().toInt())
      } catch (_: NumberFormatException) {
        JBUIScale.scale(DEFAULT_WIDTH)
      }
    } else {
      JBUIScale.scale(DEFAULT_WIDTH)
    }
  }

  private fun getShapedRect(x: Float, y: Float, w: Float, h: Float, ar: Float): Shape {
    val flatEnds = progressBar.getClientProperty("ProgressBar.flatEnds") == true
    return if (flatEnds) Rectangle2D.Float(x, y, w, h) else RoundRectangle2D.Float(x, y, w, h, ar, ar)
  }

  override fun paintDeterminate(g: Graphics?, component: JComponent?) {
    drawProgressBar(g, component) { c, g2d ->
      try {
        val r = Rectangle(progressBar.size)
        if (c.isOpaque && c.parent != null) {
          g2d.color = c.parent.background
          g2d.fill(r)
        }

        val insets = progressBar.insets
        JBInsets.removeFrom(r, insets)
        val amountFull = getAmountFull(insets, r.width, r.height)

        val fullShape: Shape
        val coloredShape: Shape
        val dynamicHeight = JBUIScale.scale(getHeight().toFloat())
        val yOffset = r.y + (r.height - progressBar.preferredSize.height) / 2 - 2 - ((dynamicHeight - SCALED_PROGRESSION_HEIGHT) / 2)
        fullShape = getShapedRect(r.x.toFloat(), yOffset + SCALED_PROGRESSION_HEIGHT * 2, r.width.toFloat(), dynamicHeight, getRadius().toFloat())
        coloredShape = getShapedRect(r.x.toFloat(), yOffset + SCALED_PROGRESSION_HEIGHT * 2, amountFull.toFloat(), dynamicHeight, getRadius().toFloat())
        g2d.color = getDeterminateSecondaryColor()
        g2d.fill(fullShape)
        g2d.color = getDeterminatePrimaryColor()
        g2d.fill(coloredShape)

        if (isCustomImageEnabled()) {
          drawCustomImage(g2d, c, false)
        }

        if (progressBar.isStringPainted) {
          paintString(g, insets.left, insets.top, r.width, r.height, amountFull, insets)
        }
      } finally {
        g2d.dispose()
      }
    }
  }

  open fun isCustomImageEnabled(): Boolean {
    return current.isCustomImageEnabled
  }

  open fun loadImageAndScale(): Image = (
    current.imagePath?.takeIf { it.isNotEmpty() }
      ?.let { ImageLoader.loadFromUrl(File(it).toURI().toURL())
        ?.getScaledInstance(16, 16, Image.SCALE_SMOOTH) } ?: EMPTY_ICON.image)

  open fun getDeterminatePrimaryColor(): Color {
    return Color(current.myDeterminatePrimaryColor)
  }

  open fun getDeterminateSecondaryColor(): Color {
    return Color(current.myDeterminateSecondaryColor)
  }

  private fun toBufferedImage(img: Image): BufferedImage {
    if (img is BufferedImage) return img

    val bufferedImage = ImageUtil.createImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val bufferedImageGraphics = bufferedImage.createGraphics()
    bufferedImageGraphics.drawImage(img, 0, 0, null)
    bufferedImageGraphics.dispose()

    return bufferedImage
  }

  companion object {
    private const val MARGIN = 1f
    private const val HEIGHT = 5f
    private val SCALED_MARGIN = JBUIScale.scale(MARGIN)
    private val SCALED_PROGRESSION_HEIGHT = JBUIScale.scale(HEIGHT)
    private val SCALED_PROGRESSION_RADIUS = JBUIScale.scale(HEIGHT)
  }

  private fun affineTransform(tx: Float, ty: Float, sx: Float, sy: Float): AffineTransform =
    AffineTransform().apply {
      scale(
        sx.toDouble(),
        sy.toDouble()
      )
      translate(
        tx.toDouble(),
        ty.toDouble()
      )
    }
}
