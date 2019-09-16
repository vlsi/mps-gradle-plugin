package de.itemis.mps.gradle;

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

open class RunAntScript : DefaultTask() {
    lateinit var script: Any
    var targets: List<String> = emptyList()
    var scriptClasspath: FileCollection? = null
    var scriptArgs: List<String> = emptyList()
    var includeDefaultArgs = true
    var includeDefaultClasspath = true
    var executable: Any? = null

    fun targets(vararg targets: String) {
        this.targets = targets.toList()
    }

    fun executable(executable: Any?) {
        this.executable = executable
    }

    @TaskAction
    fun build() {
        var allArgs = scriptArgs
        if (includeDefaultArgs) {
            val defaultArgs = project.findProperty("itemis.mps.gradle.ant.defaultScriptArgs") as Collection<*>?
            if (defaultArgs != null) {
                allArgs = allArgs + defaultArgs.map { it as String }
            }
        }

        project.javaexec {
            if (this@RunAntScript.executable != null) {
                executable(this@RunAntScript.executable)
            } else {
                val defaultJava = project.findProperty("itemis.mps.gradle.ant.defaultJavaExecutable")
                if (defaultJava != null) {
                    executable(defaultJava)
                }
            }

            main = "org.apache.tools.ant.launch.Launcher"
            workingDir = project.rootDir

            if (includeDefaultClasspath) {
                val defaultClasspath = project.findProperty(
                        "itemis.mps.gradle.ant.defaultScriptClasspath") as FileCollection?
                if (defaultClasspath != null) {
                    classpath(defaultClasspath)
                }
            }

            if (scriptClasspath != null) {
                classpath(scriptClasspath)
            }

            args(allArgs)
            args("-buildfile", project.file(script))
            args(targets)
        }
    }
}

open class BuildLanguages : RunAntScript() {
    init {
        targets = listOf("clean", "assemble")
    }
}

open class TestLanguages : RunAntScript() {
    init {
        targets = listOf("clean", "check")
    }
}