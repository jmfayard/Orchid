package com.eden.orchid.sourcedoc

import com.copperleaf.kodiak.kotlin.KotlindocInvokerImpl
import com.copperleaf.kodiak.kotlin.models.KotlinModuleDoc
import com.eden.orchid.api.options.OptionsExtractor
import com.eden.orchid.strikt.pageWasRendered
import com.eden.orchid.testhelpers.OrchidIntegrationTest
import com.eden.orchid.testhelpers.TestResults
import strikt.api.Assertion
import javax.inject.Inject
import javax.inject.Named

class NewKotlindocGenerator
@Inject
constructor(
    @Named("src") resourcesDir: String,
    invoker: KotlindocInvokerImpl,
    extractor: OptionsExtractor
) : SourcedocGenerator<KotlinModuleDoc>("kotlindoc", resourcesDir, invoker, extractor) {
    companion object {
        val type = "kotlin"
        val nodeKinds = listOf("packages", "classes")
        val otherSourceKinds = listOf("java")
    }
}

fun OrchidIntegrationTest.kotlindocSetup(showRunnerLogs: Boolean = false) {
    sourceDocTestSetup(
        NewKotlindocGenerator.type,
        NewKotlindocGenerator.nodeKinds,
        NewKotlindocGenerator.otherSourceKinds,
        showRunnerLogs
    )
}

fun OrchidIntegrationTest.kotlindocSetup(modules: List<String>, showRunnerLogs: Boolean = false) {
    sourceDocTestSetup(
        NewKotlindocGenerator.type,
        NewKotlindocGenerator.nodeKinds,
        NewKotlindocGenerator.otherSourceKinds,
        modules,
        showRunnerLogs
    )
}

fun Assertion.Builder<TestResults>.assertKotlin(baseDir: String = "/kotlindoc"): Assertion.Builder<TestResults> {
    return this
        .pageWasRendered("$baseDir/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/CustomString/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/JavaAnnotation/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/JavaClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/JavaEnumClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/JavaExceptionClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/JavaInterface/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinAnnotation/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinClassWithCompanionObject/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinEnumClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinExceptionClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinInlineClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinInterface/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinObjectClass/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinSealedClass1/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinSealedClass2/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinSealedClass3/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/KotlinSealedClasses/index.html") { }
        .pageWasRendered("$baseDir/com/eden/orchid/mock/index.html") { }
}

fun Assertion.Builder<TestResults>.assertKotlin(baseDirs: List<String>): Assertion.Builder<TestResults> {
    return baseDirs.fold(this) { acc, dir ->
        acc.assertKotlin("/kotlindoc/$dir")
    }
}
