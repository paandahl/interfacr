package no.sb1.hackathon

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.io.FileWriter
import java.net.URLClassLoader

@Mojo(name = "generate")
class GenerateTypescriptMojo : AbstractMojo() {

    @Parameter(required = true)
    private lateinit var classes: List<String>

    @Parameter(required = true)
    private lateinit var relativeOutputPath: String

    @Parameter(defaultValue = "\${project}")
    private lateinit var project: MavenProject

    override fun execute() {
        val runtimeUrls = project.runtimeClasspathElements.map { File(it).toURI().toURL() }.toTypedArray()
        val classLoader = URLClassLoader(runtimeUrls, Thread.currentThread().contextClassLoader)
        val kotlinClasses = classes.map { classLoader.loadClass(it).kotlin }

        val typeScript = generateTypescriptInterfaces(kotlinClasses)

        val file = File("${project.basedir}$relativeOutputPath")
        file.parentFile.mkdirs()
        FileWriter(file).use { writer ->
            writer.write(typeScript)
            writer.close()
        }
    }
}
