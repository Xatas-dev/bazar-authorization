package org.bazar.authorization.arch

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.Architectures

@AnalyzeClasses(
    packages = ["org.bazar.authorization"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ArchitectureTest {

    companion object {
        const val DOMAIN = "DOMAIN"
        const val APPLICATION = "APPLICATION"
        const val ADAPTER_INBOUND = "ADAPTER_INBOUND"
        const val ADAPTER_OUTBOUND = "ADAPTER_OUTBOUND"
        const val INFRASTRUCTURE = "INFRASTRUCTURE"

        val ARCHITECTURE: Architectures.LayeredArchitecture = Architectures.layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer(DOMAIN).definedBy("..domain..")
            .layer(APPLICATION).definedBy("..application..")
            .layer(ADAPTER_INBOUND).definedBy("..adapter.inbound..")
            .layer(ADAPTER_OUTBOUND).definedBy("..adapter.outbound..")
            .layer(INFRASTRUCTURE).definedBy("..infrastructure..")
    }

    @ArchTest
    val domain_layer_must_not_depend_on_any_layer: ArchRule = ARCHITECTURE
        .whereLayer(DOMAIN)
        .mayNotAccessAnyLayer()
        .`as`("Domain layer must not depend on any other layer")

    @ArchTest
    val application_layer_may_only_access_domain: ArchRule = ARCHITECTURE
        .whereLayer(APPLICATION)
        .mayOnlyAccessLayers(DOMAIN)
        .`as`("Application layer may only access Domain layer")

    @ArchTest
    val application_layer_whitelist: ArchRule = classes()
        .that().resideInAPackage("..application..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "..application..",
            "..domain..",
            "org.jetbrains.exposed..",
            "org.jetbrains.annotations..",
            "org.slf4j..",
            "java..",
            "kotlin.."
        )
        .`as`(
            "Application layer may only depend on Domain, itself, JDK/Kotlin, slf4j, JetBrains annotations and " +
                "Exposed transaction DSL (pragmatic tradeoff)"
        )

    @ArchTest
    val adapter_inbound_may_only_access_domain_and_application: ArchRule = ARCHITECTURE
        .whereLayer(ADAPTER_INBOUND)
        .mayOnlyAccessLayers(DOMAIN, APPLICATION)
        .`as`("Inbound adapters may only access Domain and Application layers")

    @ArchTest
    val adapter_outbound_may_only_access_domain_application_and_infrastructure: ArchRule = ARCHITECTURE
        .whereLayer(ADAPTER_OUTBOUND)
        .mayOnlyAccessLayers(DOMAIN, APPLICATION, INFRASTRUCTURE)
        .`as`("Outbound adapters may only access Domain, Application and Infrastructure layers")

    @ArchTest
    val infrastructure_may_only_be_accessed_by_adapters: ArchRule = ARCHITECTURE
        .whereLayer(INFRASTRUCTURE)
        .mayOnlyBeAccessedByLayers(ADAPTER_INBOUND, ADAPTER_OUTBOUND)
        .`as`("Infrastructure layer may only be accessed by adapters")
}
