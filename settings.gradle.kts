pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/aesirlab/annotations-repo")
            credentials {
                username = "EvanMeyerss"
                password = "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"
            }
        }
    }
}

rootProject.name = "workoutSolidProject"
include(":app")
//include(":app:solid-auth")
//include(":app:solid-annotation")
//include(":app:solid-processor")
