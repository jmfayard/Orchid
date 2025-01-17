package com.eden.orchid.sourcedoc

import com.copperleaf.kodiak.common.DocElement
import com.copperleaf.kodiak.common.DocInvoker
import com.copperleaf.kodiak.common.ModuleDoc
import com.eden.common.util.IOStreamUtils
import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.generators.OrchidGenerator
import com.eden.orchid.api.options.OptionsExtractor
import com.eden.orchid.api.options.annotations.BooleanDefault
import com.eden.orchid.api.options.annotations.Description
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.resources.resource.OrchidResource
import com.eden.orchid.api.resources.resource.StringResource
import com.eden.orchid.api.theme.pages.OrchidReference
import com.eden.orchid.sourcedoc.model.SourceDocModel
import com.eden.orchid.sourcedoc.model.SourceDocModuleConfig
import com.eden.orchid.sourcedoc.model.SourceDocModuleModel
import com.eden.orchid.sourcedoc.page.SourceDocModuleHomePage
import com.eden.orchid.sourcedoc.page.SourceDocModulesPage
import com.eden.orchid.sourcedoc.page.SourceDocPage
import com.eden.orchid.utilities.OrchidUtils
import com.eden.orchid.utilities.camelCase
import com.eden.orchid.utilities.dashCase
import com.eden.orchid.utilities.from
import com.eden.orchid.utilities.to
import com.eden.orchid.utilities.with
import java.io.File
import java.nio.file.Path
import javax.inject.Named

abstract class SourcedocGenerator<U : ModuleDoc>(
    key: String,
    @Named("src") val resourcesDir: String,
    val invoker: DocInvoker<U>,
    private val extractor: OptionsExtractor
) : OrchidGenerator<SourceDocModel>(key, PRIORITY_EARLY) {

    private val cacheDir: Path by lazy { OrchidUtils.getCacheDir("sourcedoc-$key") }
    private val outputDir: Path by lazy { OrchidUtils.getTempDir("sourcedoc-$key", true) }

    @Option
    lateinit var modules: MutableList<SourceDocModuleConfig>

    @Option
    @Description("The source directories to document.")
    lateinit var sourceDirs: List<String>

    @Option
    @BooleanDefault(true)
    @Description("Whether to reuse outputs from the cache, or rerun each build")
    var fromCache: Boolean = true

    @Option
    @BooleanDefault(false)
    var showRunnerLogs: Boolean = false

    override fun startIndexing(context: OrchidContext): SourceDocModel {
        val loadedModules = if (modules.size > 1) {
            modules.map {
                setupModule(context, it)
            }
        } else {
            listOf(setupModule(context, null))
        }

        val indexPage = if (loadedModules.size > 1) setupIndexPage(context, loadedModules) else null

        return SourceDocModel(indexPage, loadedModules)
    }

    override fun startGeneration(context: OrchidContext, model: SourceDocModel) {
        model.allPages.forEach { context.renderTemplate(it) }
    }

// Setup modules and index pages
//----------------------------------------------------------------------------------------------------------------------

    private fun setupModule(context: OrchidContext, config: SourceDocModuleConfig?): SourceDocModuleModel {
        extractor.extractOptions(invoker, allData)

        val moduleName = if (config != null) config.name else ""
        val modulePath =
            if (config != null) config.name from { camelCase() } with { toLowerCase() } to { dashCase() } else ""

        val invokerModel: U? = loadFromCacheOrRun(config)
        val modelPageMap = invokerModel?.let {
            it.nodes.map { node ->
                val nodeName: String = node.prop.name
                val nodeElements: List<DocElement> = node.getter()

                val nodePages = nodeElements.map { element ->
                    SourceDocPage(
                        this@SourcedocGenerator,
                        context,
                        modulePath,
                        element,
                        nodeName,
                        element.name
                    )
                }

                node to nodePages
            }.toMap()
        } ?: emptyMap()

        return SourceDocModuleModel(
            setupModuleHomepage(context, config),
            moduleName,
            modulePath,
            invokerModel,
            modelPageMap
        )
    }

    private fun setupModuleHomepage(context: OrchidContext, config: SourceDocModuleConfig?): SourceDocModuleHomePage {
        var readmeFile: OrchidResource? = null

        for(baseDir in (config?.sourceDirs ?: sourceDirs)) {
            val baseFile = File(resourcesDir).toPath().resolve(baseDir).toFile().absolutePath
            val closestFile: OrchidResource? = context.findClosestFile(baseFile, "readme", false, 4)
            if(closestFile != null) {
                readmeFile = closestFile
                break
            }
        }

        if(readmeFile == null) {
            readmeFile = StringResource(
                "",
                OrchidReference(
                    context,
                    ""
                )
            )
        }

        val moduleName = if (config != null) config.name else ""
        val modulePath =
            if (config != null) config.name from { camelCase() } with { toLowerCase() } to { dashCase() } else ""

        readmeFile.reference.path = key
        readmeFile.reference.fileName = modulePath
        readmeFile.reference.outputExtension = "html"
        readmeFile.reference.title = moduleName

        return SourceDocModuleHomePage(readmeFile, this)
    }

    private fun setupIndexPage(context: OrchidContext, modules: List<SourceDocModuleModel>): SourceDocModulesPage {
        return SourceDocModulesPage(context, this, modules).also { modulePage ->
            modules.forEach { module ->
                module.homepage.parent = modulePage
            }
        }
    }

// helpers
//----------------------------------------------------------------------------------------------------------------------

    private fun loadFromCacheOrRun(config: SourceDocModuleConfig?): U? {
        if (config?.fromCache ?: fromCache) {
            val moduleDoc = invoker.loadCachedModuleDoc(outputDir)
            if (moduleDoc != null) {
                return moduleDoc
            }
        }

        return invoker.getModuleDoc(
            (config?.sourceDirs ?: sourceDirs).map { File(resourcesDir).toPath().resolve(it) },
            outputDir,
            emptyList()
        ) { inputStream ->
            if (config?.showRunnerLogs ?: showRunnerLogs) {
                IOStreamUtils.InputStreamPrinter(inputStream, null) as Runnable
            } else {
                IOStreamUtils.InputStreamIgnorer(inputStream) as Runnable
            }
        }
    }

}
