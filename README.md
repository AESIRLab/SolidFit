# Solid Example App
**_NOTE: \*Many of the imports will need to be tweaked to match your package name\*_**

## Implement Solid Annotations Packages:
1. In GitHub, go _Settings -> Developer Settings -> Personal access tokens -> Tokens (classic)_
2. Click _Generate new token -> **Generate new token (classic)**_
3. Include relevant note (doesn't matter what you put)
4. Set desired expiration date
5. Check the following boxes:
   - _repo_
   - _write:packages_
   - _delete:packages_
6. **Should look like this:**
   - <img width="386" height="473" alt="token example" src="https://github.com/user-attachments/assets/528751ff-cef2-4bd6-a223-6a66382266b2" />
7. **Copy provided token:**
   - <img width="557" height="39" alt="token string" src="https://github.com/user-attachments/assets/8150b347-55ab-4ce0-a175-7ebfa730dd08" />
   - **_SAVE THIS TOKEN IN A TEXT DOCUMENT! IF YOU FORGET IT, YOU'LL HAVE TO REGENERATE IT OR CREATE A NEW ONE AND UPDATE YOUR PROGRAM FILES!_**
8. In Android Studio:
   - Go to _local.properties_ file and add the following:
     - ```
       gpr.user=<USERNAME>
       gpr.key=<GITHUB KEY>
       ```
    - Go to _settings.gradle.kts_ file, and at the top, add this:
       - ```
         import java.util.Properties
         import java.io.FileInputStream


         val localProperties = Properties().apply {
         val file = rootDir.resolve("local.properties")
         if (file.exists()) {
         load(FileInputStream(file))
             }
          }
          
          val gprUser: String? = localProperties.getProperty("gpr.user")
          val gprKey: String? = localProperties.getProperty("gpr.key")
         ```
    - Make the _dependencyResolutionManagement {...}_ block resemble the following:
       - ```
         dependencyResolutionManagement {
         repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
         repositories {
             google()
             mavenCentral()
             maven {
                 name = "GitHubPackages"
                 url = uri("https://maven.pkg.github.com/aesirlab/annotations-repo")
                 credentials {
                     username = gprUser
                     password = gprKey
               }
             }
           }
         }
         ```
9. In _build.gradle.kts (:app)_, above the _dependencies {...}_ block, add:
    - ```
      val version = "0.0.61-stable"
      ```
     - _**NOTE:**_ Current version is 0.0.61-stable, but it will continue to upgrade for the foreseeable future.
10. In the _dependencies_ block, add:
     - ```
       implementation("org.aesirlab:sksolidannotations:$version")
       ksp("org.aesirlab:skannotationscompiler:$version")
       implementation("org.aesirlab:authlib:$version")
       ```
    - If _ksp(...)_ is not recognized, make sure you have the following included in the _plugins {...}_ block of the same file:
       - ```
         id("com.google.devtools.ksp") version "2.0.21-1.0.25”
         ```
11. In the _defaultConfig{...}_ within the _android{...}_ block, set minSdk to 30:
    - ```
      minSdk = 30
      ```
12. Project _**should**_ be buildable.

## Creating/Modifying Necessary Files:
1. Create _Application.kt_ file:
   - Reference file in repo.
2. Create _Authorization.kt_ file to authorize Pod:
   - Reference file in repo.
3. Create _StartAuthScreen.kt, AuthCompleteScreen.kt,_ and _UnfetchableWebIdScreen.kt_:
   - Reference files in repo.
   - For _StartAuthScreen.kt,_ you'll need to modify the following block of code:
     - ```
       val appTitle = "<APP TITLE HERE>"
       var webId by rememberSaveable {
         mutableStateOf("<POD URL HERE>")
       ```
4. In the _build.gradle.kts (:app)_, add the following to the Android block:
   - ```
     manifestPlaceholders["appAuthRedirectScheme"] = "<PACKAGE NAME HERE>"
     ```
5. In your _AndroidManifest.xml_ include the following block underneath the existing intent filter:
   - ```
     <intent-filter>
     <action android:name="android.intent.action.VIEW" />
     <category android:name="android.intent.category.DEFAULT" />
     <category android:name="android.intent.category.BROWSABLE" />
  
  
     <data
         android:host="www.solid-oidc.com"
         android:pathPrefix="/callback"
         android:scheme="app" />
     </intent-filter>
     ```
