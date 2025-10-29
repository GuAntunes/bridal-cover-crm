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
            .that().resideInAPackage("..infrastructure.controller..")
            .should().haveSimpleNameEndingWith("Controller")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val servicesShouldEndWithService: ArchRule = classes()
            .that().resideInAPackage("..application.service..")
            .should().haveSimpleNameEndingWith("Service")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val useCasesShouldEndWithUseCase: ArchRule = classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areTopLevelClasses()
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("UseCase")
            .because("Top-level use case interfaces should end with 'UseCase'")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val portOutShouldFollowNamingConvention: ArchRule = classes()
            .that().resideInAPackage("..application.port.out..")
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Port")
            .orShould().haveSimpleNameEndingWith("Repository")
            .orShould().haveSimpleNameEndingWith("Gateway")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val repositoriesShouldEndWithRepository: ArchRule = classes()
            .that().resideInAPackage("..infrastructure.repository..")
            .or().resideInAPackage("..infrastructure.persistence.repository..")
            .should().haveSimpleNameEndingWith("Repository")
            .because("Repository implementations should end with 'Repository'")
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
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..infrastructure.."
            ).allowEmptyShould(true)

        @ArchTest
        @JvmField
        val applicationLayerDependencies: ArchRule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)

        // Spring Annotations Rules
        @ArchTest
        @JvmField
        val springControllersOnlyInInfrastructure: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .or().areAnnotatedWith("org.springframework.stereotype.Controller")
            .should().resideInAPackage("..infrastructure.controller..")
            .because("Spring Controllers should only exist in infrastructure.controller package or its subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val springServiceOnlyInApplicationService: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().resideInAPackage("..application.service..")
            .because("Spring Services should only exist in application.service package or its subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val springRepositoryOnlyInInfrastructure: ArchRule = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Repository")
            .should().resideInAPackage("..infrastructure.repository..")
            .orShould().resideInAPackage("..infrastructure.persistence.repository..")
            .because("Spring Repositories should only exist in infrastructure.repository or infrastructure.persistence.repository packages or their subpackages")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val domainShouldNotUseSpringAnnotations: ArchRule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("org.springframework..")
            .allowEmptyShould(true)

        // Port Access Rules
        @ArchTest
        @JvmField
        val inputPortsAccessRule: ArchRule = classes()
            .that().resideInAPackage("..application.port.in..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(
                "..application.service..",
                "..application.port.in..",
                "..infrastructure.controller.."
            )
            .because("Input ports can be accessed by services (in any subpackage) and controllers")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val outputPortsAccessRule: ArchRule = classes()
            .that().resideInAPackage("..application.port.out..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(
                "..application.service..",
                "..application.port.out..",
                "..infrastructure.repository..",
                "..infrastructure.persistence.repository.."
            )
            .because("Output ports can be accessed by services (in any subpackage) and repository implementations")
            .allowEmptyShould(true)

        // Interface Implementation Rules
        @ArchTest
        @JvmField
        val servicesShouldImplementUseCase: ArchRule = classes()
            .that().resideInAPackage("..application.service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.port.in..")
            .because("Services should implement use cases from input ports")
            .allowEmptyShould(true)

        @ArchTest
        @JvmField
        val customRepositoriesShouldImplementPorts: ArchRule = classes()
            .that().resideInAPackage("..infrastructure.repository..")
            .or().resideInAPackage("..infrastructure.persistence.repository..")
            .and().areNotInterfaces()
            .and().haveSimpleNameNotContaining("DataJdbc")
            .and().haveSimpleNameNotContaining("Jpa")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.port.out..")
            .because("Custom repository implementations (not Spring Data repositories) should implement interfaces from output ports")
            .allowEmptyShould(true)
    }
} 