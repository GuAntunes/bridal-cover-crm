package br.com.gustavoantunes.bridalcovercrm

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(
    packages = ["br.com.gustavoantunes.bridalcovercrm"],
    importOptions = [
        ImportOption.DoNotIncludeTests::class,
        ImportOption.DoNotIncludeJars::class
    ]
)
class ArchitectureTest {

    companion object {
        // Nomenclature Rules
        @ArchTest
        @JvmField
        val controllersShouldEndWithController: ArchRule = classes()
            .that().resideInAPackage("..infrastructure.adapter.in..")
            .and().resideOutsideOfPackage("..dto..")
            .and().areTopLevelClasses()
            .and().areNotNestedClasses()
            .should().haveSimpleNameEndingWith("Controller")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val servicesShouldEndWithService: ArchRule = classes()
            .that().resideInAPackage("..application.usecase..")
            .should().haveSimpleNameEndingWith("Service")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val useCasesShouldEndWithUseCase: ArchRule = classes()
            .that().resideInAPackage("..domain.port.in..")
            .and().areTopLevelClasses()
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("UseCase")
            .because("Top-level use case interfaces should end with 'UseCase'")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val portOutShouldFollowNamingConvention: ArchRule = classes()
            .that().resideInAPackage("..domain.port.out..")
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Port")
            .orShould().haveSimpleNameEndingWith("Repository")
            .orShould().haveSimpleNameEndingWith("Gateway")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val adaptersShouldEndWithAdapter: ArchRule = classes()
            .that().resideInAPackage("..infrastructure.adapter.out..")
            .and().areNotInterfaces()
            .and().areTopLevelClasses()
            .and().areNotAnnotatedWith("org.springframework.stereotype.Repository")
            .and().haveSimpleNameNotEndingWith("Entity")
            .and().haveSimpleNameNotEndingWith("Mapper")
            .and().haveSimpleNameNotContaining("DataJdbc")
            .and().haveSimpleNameNotContaining("Jpa")
            .should().haveSimpleNameEndingWith("Adapter")
            .because("Output adapter implementations should end with 'Adapter'")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val entitiesShouldNotHaveSuffix: ArchRule = classes()
            .that().resideInAPackage("..domain.model..")
            .should().haveSimpleNameNotEndingWith("Entity")
            .allowEmptyShould(true)

        // Dependency Rules
        @ArchTest
        @JvmField
        val domainShouldNotDependOnOtherLayers: ArchRule = noClasses()
            .that().resideInAPackage("..domain.model..")
            .or().resideInAPackage("..domain.event..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..infrastructure.."
            ).because("Domain model and events should not depend on application or infrastructure layers")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val applicationLayerDependencies: ArchRule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("Application layer should not depend on infrastructure layer")
            .allowEmptyShould(true)

        // Spring Annotations Rules
        @ArchTest
        @JvmField
        val springControllersOnlyInInfrastructure: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .or().areAnnotatedWith("org.springframework.stereotype.Controller")
            .should().resideInAPackage("..infrastructure.adapter.in..")
            .because("Spring Controllers should only exist in infrastructure.adapter.in package or its subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val springServiceOnlyInApplicationUseCase: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().resideInAPackage("..application.usecase..")
            .because("Spring Services should only exist in application.usecase package or its subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val springRepositoryOnlyInInfrastructure: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Repository")
            .should().resideInAPackage("..infrastructure.adapter.out..")
            .because("Spring Repositories should only exist in infrastructure.adapter.out packages or their subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val springComponentOnlyInInfrastructure: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Component")
            .should().resideInAPackage("..infrastructure..")
            .because("Spring Components should only exist in infrastructure packages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val domainShouldNotUseSpringAnnotations: ArchRule = noClasses()
            .that().resideInAPackage("..domain.model..")
            .or().resideInAPackage("..domain.event..")
            .should().beAnnotatedWith("org.springframework..")
            .because("Domain model should not use Spring annotations")
            .allowEmptyShould(true)

        // Port Access Rules
        @ArchTest
        @JvmField
        val inputPortsAccessRule: ArchRule = classes()
            .that().resideInAPackage("..domain.port.in..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(
                "..application.usecase..",
                "..domain.port.in..",
                "..infrastructure.adapter.in.."
            )
            .because("Input ports can be accessed by use cases (in any subpackage) and input adapters")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val outputPortsAccessRule: ArchRule = classes()
            .that().resideInAPackage("..domain.port.out..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(
                "..application.usecase..",
                "..domain.port.out..",
                "..infrastructure.adapter.out.."
            )
            .because("Output ports can be accessed by use cases (in any subpackage) and output adapters")
            .allowEmptyShould(true)

        // Interface Implementation Rules
        @ArchTest
        @JvmField
        val useCasesShouldImplementInputPorts: ArchRule = classes()
            .that().resideInAPackage("..application.usecase..")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain.port.in..")
            .because("Use case implementations should implement interfaces from input ports")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val adaptersShouldImplementPorts: ArchRule = classes()
            .that().resideInAPackage("..infrastructure.adapter.out..")
            .and().areNotInterfaces()
            .and().haveSimpleNameEndingWith("Adapter")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain.port.out..")
            .because("Output adapter implementations should implement interfaces from output ports")
            .allowEmptyShould(true)
    }
} 