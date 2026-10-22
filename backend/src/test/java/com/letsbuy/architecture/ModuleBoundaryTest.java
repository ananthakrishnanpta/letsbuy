package com.letsbuy.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.letsbuy", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule identityIsIsolated = noClasses()
            .that().resideInAPackage("com.letsbuy.identity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.letsbuy.catalog..",
                    "com.letsbuy.cart..",
                    "com.letsbuy.ordering.."
            );

    @ArchTest
    static final ArchRule catalogDoesNotDependOnOtherFeatures = noClasses()
            .that().resideInAPackage("com.letsbuy.catalog..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.letsbuy.identity..",
                    "com.letsbuy.cart..",
                    "com.letsbuy.ordering.."
            );

    @ArchTest
    static final ArchRule cartUsesCatalogOnlyThroughApi = noClasses()
            .that().resideInAPackage("com.letsbuy.cart..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.letsbuy.catalog.domain..",
                    "com.letsbuy.catalog.persistence..",
                    "com.letsbuy.catalog.service..",
                    "com.letsbuy.catalog.web..",
                    "com.letsbuy.ordering..",
                    "com.letsbuy.identity.."
            );

    @ArchTest
    static final ArchRule orderingUsesPeersOnlyThroughApi = noClasses()
            .that().resideInAPackage("com.letsbuy.ordering..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.letsbuy.catalog.domain..",
                    "com.letsbuy.catalog.persistence..",
                    "com.letsbuy.catalog.service..",
                    "com.letsbuy.catalog.web..",
                    "com.letsbuy.cart.domain..",
                    "com.letsbuy.cart.persistence..",
                    "com.letsbuy.cart.service..",
                    "com.letsbuy.cart.web..",
                    "com.letsbuy.identity.."
            );
}
