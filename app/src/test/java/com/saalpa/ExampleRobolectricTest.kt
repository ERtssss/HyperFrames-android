package com.saalpa

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saalpa.model.AspectRatioType
import com.saalpa.model.project.DefaultProjectFactory
import com.saalpa.model.project.ProjectHtmlCompiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HyperFrames", appName)
  }

  @Test
  fun `verify empty default project creation and compiler`() {
    val project = DefaultProjectFactory.createDefaultProject()
    assertNotNull(project)
    assertEquals(1, project.scenes.size)
    val scene1 = project.scenes.first()
    assertEquals("Scene 1", scene1.title)
    assertTrue("Scene 1 must start with empty composition", scene1.elements.isEmpty())

    val compiled = ProjectHtmlCompiler.compile(project)
    assertTrue(compiled.contains("<!DOCTYPE html>"))
    assertTrue(compiled.contains("window.AndroidHyperFrames"))
    assertTrue(compiled.contains("window.__hfSeek"))
    assertTrue(compiled.contains("gsap"))
  }
}

