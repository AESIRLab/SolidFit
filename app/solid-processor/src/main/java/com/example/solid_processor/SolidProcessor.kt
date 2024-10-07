package com.example.solid_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.example.solid_annotation.SolidAnnotation
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Locale


const val PACKAGE_NAME = "com.solidannotations"
@OptIn(KspExperimental::class)
class SolidProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(SolidAnnotation::class.qualifiedName.toString())
            .filterIsInstance<KSClassDeclaration>()
            // Keeping this logging to make it possible to observe incremental build
            .also { logger.warn("Generating for ${it.joinToString { it.simpleName.getShortName() }}") }
            .forEach(::generateSolidModel)

        return emptyList()
    }

    private fun generateSolidModel(
        annotatedClass: KSClassDeclaration
    ) {
        buildDaoFile(annotatedClass)
        buildUtilitiesFile(annotatedClass)
        buildDaoImplFile(annotatedClass)
        buildRepositoryFile(annotatedClass)
        buildDatabaseFile(annotatedClass)
        buildContextScript(annotatedClass)
        buildSolidUtilitiesFile(annotatedClass)
    }

    private fun buildContextScript(
        annotatedClass: KSClassDeclaration
    ) {
//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME

        val contextClass = ClassName("android.content", "Context")
        val dataStoreClass = ClassName("androidx.datastore.core", "DataStore")
        val preferencesClass = ClassName("androidx.datastore.preferences.core", "Preferences")
        val preferencesDataStoreMember = MemberName("androidx.datastore.preferences", "preferencesDataStore")

        val scriptFileSpec = FileSpec
            .scriptBuilder("Script", packageName)
            .addStatement("val %T.dataStore: %T<%T> by %M(\"userData\")",
                contextClass, dataStoreClass, preferencesClass, preferencesDataStoreMember)
            .build()
        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val scriptFile = codeGenerator.createNewFile(dependencies, scriptFileSpec.packageName, scriptFileSpec.name)
        OutputStreamWriter(scriptFile, StandardCharsets.UTF_8)
            .use(scriptFileSpec::writeTo)
    }

    private fun buildSolidUtilitiesFile(
        annotatedClass: KSClassDeclaration
    ) {
//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME
        val className = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name
        val baseClassName = ClassName(annotatedClass.packageName.asString(), annotatedClass.simpleName.getShortName())

        val contextType = ClassName("android.content", "Context")
        val contextParam = ParameterSpec
            .builder("context", contextType)
            .build()

        // TODO: figure out the best way to get the base package name
        val tokenStoreType = ClassName(packageName, "AuthTokenStore")
//        val tokenStoreType = ClassName("com.zybooks.testannotationsprocessor.ui.store", "AuthTokenStore")

        val tokenStoreProp = PropertySpec
            .builder("tokenStore", tokenStoreType)
            .initializer("%T(context)", tokenStoreType)
            .addModifiers(KModifier.PRIVATE)
            .build()

        val requestBody = ClassName("okhttp3", "RequestBody")
        val request = ClassName("okhttp3", "Request")
        val solidUtilities = ClassName(packageName, "Utilities")
        val absoluteUriRef = MemberName(packageName.plus(".Utilities.Companion"), "ABSOLUTE_URI")
        val queryExecutionFactory = ClassName("com.hp.hpl.jena.query","QueryExecutionFactory")
        val queryFactory = ClassName("com.hp.hpl.jena.query", "QueryFactory")
        val model = ClassName("com.hp.hpl.jena.rdf.model", "Model")
        val modelFactory = ClassName("com.hp.hpl.jena.rdf.model", "ModelFactory")
        val joseObjectType = ClassName("com.nimbusds.jose","JOSEObjectType")
        val jwsAlgo = ClassName("com.nimbusds.jose","JWSAlgorithm")
        val jwsHeader = ClassName("com.nimbusds.jose","JWSHeader")
        val ecdsaSigner = ClassName("com.nimbusds.jose.crypto","ECDSASigner")
        val ecKey = ClassName("com.nimbusds.jose.jwk","ECKey")
        val jwk = ClassName("com.nimbusds.jose.jwk","JWK")
        val jwtClaimsSet = ClassName("com.nimbusds.jwt","JWTClaimsSet")
        val signedJwt = ClassName("com.nimbusds.jwt","SignedJWT")
        val first = MemberName("kotlinx.coroutines.flow","first")
        val runBlocking = MemberName("kotlinx.coroutines","runBlocking")
        val formBody = ClassName("okhttp3","FormBody")
        val okHttpClient = ClassName("okhttp3","OkHttpClient")
        val toRequestBody = MemberName("okhttp3.RequestBody.Companion","toRequestBody")
        val jsonObject = ClassName("org.json","JSONObject")
        val byteArrayOutputStream = ClassName("java.io","ByteArrayOutputStream")
        val calendar = ClassName("java.util","Calendar")
        val uuid = ClassName("java.util","UUID")

        val regenerateRefreshTokenFun = FunSpec
            .builder("regenerateRefreshToken")
            .addModifiers(KModifier.SUSPEND)
            .addStatement("val tokenUri = %M { tokenStore.getTokenUri().%M() }", runBlocking, first)
            .addStatement("val refreshToken = %M { tokenStore.getRefreshToken().%M() }", runBlocking, first)
            .addStatement("val clientId = %M { tokenStore.getClientId().%M() }", runBlocking, first)
            .addStatement("val accessToken = %M { tokenStore.getAccessToken().%M() }", runBlocking, first)
            .addStatement("val clientSecret = %M { tokenStore.getClientSecret().%M() }", runBlocking, first)
            .addCode("""
                val formBody = %T.Builder()
                .addEncoded("grant_type", "refresh_token")
                .addEncoded("refresh_token", refreshToken)
                .addEncoded("client_id", clientId)
                .addEncoded("client_secret", clientSecret)
                .addEncoded("scope", "openid+offline_access+webid")
                .build()
                
            """.trimIndent(), formBody)
            .addStatement("val request = %T.Builder().url(tokenUri).addHeader(\"DPoP\", generateCustomToken(\"POST\", tokenUri))" +
                    ".addHeader(\"Authorization\", \"DPoP \$accessToken\")" +
                    ".addHeader(\"Content-Type\", \"application/x-www-form-urlencoded\").method(\"POST\", formBody)" +
                    ".build()", request)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val response = client.newCall(request).execute()")
            .addStatement("val body = response.body!!.string()")
            .beginControlFlow("if (response.code in 400..499)")
            .addStatement("throw Error(\"could not refresh token sad\")")
            .nextControlFlow("else")
            .addStatement("val jsonResponse = %T(body)", jsonObject)
            .addStatement("val newAccessToken = jsonResponse.getString(\"access_token\")")
            .addStatement("val newIdToken = jsonResponse.getString(\"id_token\")")
            .addStatement("val newRefreshToken = jsonResponse.getString(\"refresh_token\")")
            .addStatement("tokenStore.setIdToken(newIdToken)")
            .addStatement("tokenStore.setRefreshToken(newRefreshToken)")
            .addStatement("tokenStore.setAccessToken(newAccessToken)")
            .endControlFlow()
            .build()


        val generatePutRequestFun = FunSpec
            .builder("generatePutRequest")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("resourceUri", String::class)
            .addParameter("rBody", requestBody)
            .addStatement("val accessToken = %M { tokenStore.getAccessToken().%M() }", runBlocking, first)
            .addStatement("return %T.Builder().url(resourceUri).addHeader(\"DPoP\", generateCustomToken(\"PUT\", resourceUri))" +
                    ".addHeader(\"Authorization\", \"DPoP \$accessToken\").addHeader(\"content-type\", \"text/turtle\")" +
                    ".addHeader(\"Link\", \"<http://www.w3.org/ns/ldp#Resource>;rel=\\\"type\\\"\").method(\"PUT\", rBody)" +
                    ".build()", request)
            .returns(request)
            .build()

        val generateGetRequestFun = FunSpec
            .builder("generateGetRequest")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("resourceUri", String::class)
            .addParameter("accessToken", String::class)
            .addStatement("return %T.Builder().url(resourceUri).addHeader(\"DPoP\", generateCustomToken(\"GET\", resourceUri))" +
                    ".addHeader(\"Authorization\", \"DPoP \$accessToken\")" +
                    ".addHeader(\"Content-Type\", \"text/turtle\")" +
                    ".addHeader(\"Link\", \"<http://www.w3.org/ns/ldp#Resource>;rel=\\\"type\\\"\").method(\"GET\", null)" +
                    ".build()", request)
            .returns(request)
            .build()

        val checkStorageFun = FunSpec
            .builder("checkStorage")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("storageUri", String::class)
            .addParameter("accessToken", String::class)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val request = generateGetRequest(\"\$storageUri\$%M\", accessToken)", absoluteUriRef)
            .addStatement("val response = client.newCall(request).execute()")
            .beginControlFlow("if (response.code in 400..499)")
            .addStatement("return \"\"")
            .endControlFlow()
            .addStatement("val body = response.body!!.string()")
            .addStatement("return body")
            .returns(String::class)
            .build()

        val generateCustomTokenFun = FunSpec
            .builder("generateCustomToken")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("method", String::class)
            .addParameter("uri", String::class)
            .addStatement("val signingJwk = %M { tokenStore.getSigner().%M() }", runBlocking, first)
            .beginControlFlow("if (signingJwk == \"\")")
            .addStatement("throw Error(\"no signing jwk found\")")
            .endControlFlow()
            .addStatement("val parsedKey = %T.parse(%T.parse(signingJwk).toJSONObject())", ecKey, jwk)
            .addStatement("val ecPublicJWK = parsedKey.toPublicJWK()")
            .addStatement("val signer = %T(parsedKey)", ecdsaSigner)
            .addStatement("val body = %T.Builder().claim(\"htu\", uri).claim(\"htm\", method)" +
                    ".issueTime(%T.getInstance().time).jwtID(%T.randomUUID().toString()).build()",
                jwtClaimsSet, calendar, uuid)
            .addStatement("val header = %T.Builder(%T.ES256).type(%T(\"dpop+jwt\")).jwk(ecPublicJWK)" +
                    ".build()", jwsHeader, jwsAlgo, joseObjectType)
            .addStatement("val signedJWT = %T(header, body)", signedJwt)
            .addStatement("signedJWT.sign(signer)")
            .addStatement("return signedJWT.serialize()")
            .returns(String::class)
            .build()

        val updateSolidDatasetFunBuilder = FunSpec
            .builder("updateSolidDataset")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("items", List::class.asTypeName().plusParameter(baseClassName))
            .addStatement("val accessToken = %M { tokenStore.getAccessToken().%M() }", runBlocking, first)
            .addStatement("val storageUri = %M { val webId = tokenStore.getWebId().%M(); getStorage(webId) }", runBlocking, first)
            .beginControlFlow("if (storageUri != \"\" && accessToken != \"\")")
            .addStatement("val client = %T.Builder().build()", okHttpClient)
            .addStatement("val resourceUri = \"\${storageUri}\${%M}\"", absoluteUriRef)
            .addStatement("val model = %T.createDefaultModel()", modelFactory)
            .addStatement("model.setNsPrefix(\"acp\", %T.NS_ACP)", solidUtilities)
            .addStatement("model.setNsPrefix(\"acl\", %T.NS_ACL)", solidUtilities)
            .addStatement("model.setNsPrefix(\"ldp\", %T.NS_LDP)", solidUtilities)
            .addStatement("model.setNsPrefix(\"skos\", %T.NS_SKOS)", solidUtilities)
            .addStatement("model.setNsPrefix(\"ci\", %T.NS_$className)", solidUtilities)

        val filteredProps = annotatedClass
            .getAllProperties()
            .filter { it.simpleName.getShortName() != "id" }

        val varsList = mutableListOf<Pair<KSPropertyDeclaration, String>>()
        filteredProps.forEach {
            varsList.add(Pair(it, "ci${it.simpleName.getShortName().replaceFirstChar { char -> char.uppercase() }}"))
            updateSolidDatasetFunBuilder.addStatement("val ci${it.simpleName.getShortName().replaceFirstChar { char -> char.uppercase() }} =" +
                    " model.createProperty(Utilities.NS_$className + \"${it.simpleName.getShortName()}\")")
        }

        updateSolidDatasetFunBuilder.addStatement("items.forEach { ci -> ")
        updateSolidDatasetFunBuilder.addStatement("val id = ci.id")
        updateSolidDatasetFunBuilder.addStatement("val mThingUri = model.createResource(\"\$resourceUri#\$id\")")
        varsList.forEach { variable ->

            when (variable.first.type.resolve().toString()) {

                "String" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }

                "Date" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }

                "Float" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }

                "Int" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }

                "Boolean" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }

                "Long" -> {
                    updateSolidDatasetFunBuilder.addStatement("mThingUri.addLiteral(${variable.second}, ci.${variable.first.simpleName.getShortName()})")
                }
            }
        }

        updateSolidDatasetFunBuilder.addStatement("}")

        val updateSolidDatasetFun = updateSolidDatasetFunBuilder
            .addStatement("val bOutputStream = %T()", byteArrayOutputStream)
            .addStatement("model.write(bOutputStream, \"TURTLE\", null)")
            .addStatement("val rBody = bOutputStream.toByteArray().%M(null, 0, bOutputStream.size())", toRequestBody)
            .addStatement("val putRequest = generatePutRequest(resourceUri, rBody)")
            .addStatement("val putResponse = client.newCall(putRequest).execute()")
//            .addStatement("putResponse.body?.let { }")
            .addStatement("return putResponse.code")
            .nextControlFlow("else")
            .addStatement("return 600")
            .endControlFlow()
            .returns(Int::class)
            .build()

        val getStorageFun = FunSpec
            .builder("getStorage")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("webId", String::class)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val webIdRequest = %T.Builder().url(webId).build()", request)
            .addStatement("val webIdResponse = client.newCall(webIdRequest).execute()")
            .addStatement("val responseString = webIdResponse.body!!.string()")
            .addStatement("val byteArray = responseString.toByteArray()")
            .addStatement("val inStream = String(byteArray).byteInputStream()")
            .addStatement("val m = %T.createDefaultModel().read(inStream, null, \"TURTLE\")", modelFactory)
            .addStatement("val queryString = \"SELECT ?o\\n\" +\n" +
                    "            \"WHERE\\n\" +\n" +
                    "            \"{ ?s <http://www.w3.org/ns/pim/space#storage> ?o }\"")
            .addStatement("val q = %T.create(queryString)", queryFactory)
            .addStatement("var storage = \"\"")
            .addStatement("try {")
            .addStatement("val qexec = %T.create(q, m)", queryExecutionFactory)
            .addStatement("val results = qexec.execSelect()")
            .addStatement("while (results.hasNext()) {")
            .addStatement("val soln = results.nextSolution()")
            .addStatement("storage = soln.getResource(\"o\").toString()")
            .addStatement("break")
            .addStatement("}")
            .addStatement("} catch (e: Exception) {")
            .addStatement("}")
            .addStatement("return storage")
            .returns(String::class)
            .build()

        val constructor = FunSpec.constructorBuilder()
            .addParameter(contextParam)
            .build()

        val classBuilder = TypeSpec
            .classBuilder("SolidUtilities")
            .primaryConstructor(constructor)
            .addProperty(tokenStoreProp)
            .addFunctions(
                listOf(
                    updateSolidDatasetFun,
                    generateGetRequestFun,
                    generateCustomTokenFun,
                    generatePutRequestFun,
                    regenerateRefreshTokenFun,
                    checkStorageFun
                )
            )

        val solidUtilsFileSpec = FileSpec
            .builder(packageName, "SolidUtilities")
            .addType(
                classBuilder.build()
            )
            .addFunction(getStorageFun)
            .build()
        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val utilsFile = codeGenerator.createNewFile(dependencies, solidUtilsFileSpec.packageName, solidUtilsFileSpec.name)
        OutputStreamWriter(utilsFile, StandardCharsets.UTF_8)
            .use(solidUtilsFileSpec::writeTo)
    }

    private fun buildUtilitiesFile(
        annotatedClass: KSClassDeclaration
    ) {
        val className = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name
//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME
        val uriShortName = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .uriShortName
        val absoluteUri = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .absoluteUri

        val modelFactoryClass = ClassName("com.hp.hpl.jena.rdf.model", "ModelFactory")
        val resourceFactoryClass = ClassName("com.hp.hpl.jena.rdf.model", "ResourceFactory")

        val resourceConverterFnSpec = FunSpec
            .builder("resourceTo$className")
            .addParameter(
                ParameterSpec
                    .builder("resource", ClassName("com.hp.hpl.jena.rdf.model", "Resource"))
                    .build()
            )
            .addStatement("val anonModel = %T.createDefaultModel()", modelFactoryClass)
            .addStatement("val id = resource.uri.split(\"#\")[1]")

        val filteredProps = annotatedClass
            .getAllProperties()
            .filter { it.simpleName.getShortName() != "id" }

        filteredProps.forEach {
            resourceConverterFnSpec
                .addStatement("val ${it.simpleName.getShortName()}Prop = anonModel.createProperty(NS_$className + \"${it.simpleName.getShortName()}\")")
//            resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Literal = anonModel.createTypedLiteral(${it.simpleName.getShortName()}Object)")
            when (it.type.resolve().toString()) {

                "String" -> {
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop).`object`")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.toString() ")
                }

                "Date" -> {
//                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Literal")

                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop)")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.toString() as Date")
                }

                "Float" -> {
//                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Literal.float")

                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop)")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.toString() as Float")
                }

                "Int" -> {
//                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Literal.int")

                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop)")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.toString() as Integer")
                }

                "Boolean" -> {

                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop)")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.toString() as Boolean")
//                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Literal.boolean")
                }

                "Long" -> {

                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()}Object = resource.getProperty(${it.simpleName.getShortName()}Prop)")
                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Object.long")
//                    resourceConverterFnSpec.addStatement("val ${it.simpleName.getShortName()} = ${it.simpleName.getShortName()}Literal.long")
                }
            }
        }
        val propsString = filteredProps.joinToString {
            it.simpleName.getShortName()
        }

        val classAnnotation = ClassName(annotatedClass
            .qualifiedName
            ?.getQualifier()
            .orEmpty(), className)
        resourceConverterFnSpec.addStatement(
            "return %T(id, $propsString)",
            classAnnotation
        )

        val resourceConverterFn = resourceConverterFnSpec
            .returns(classAnnotation)
            .build()

        val absoluteUriSpec = PropertySpec
            .builder("ABSOLUTE_URI", String::class, KModifier.CONST)
            .initializer("\"$absoluteUri\"")
            .build()
        val nsAcpSpec = PropertySpec
            .builder("NS_ACP", String::class, KModifier.CONST)
            .initializer("\"http://www.w3.org/ns/solid/acp#\"")
            .build()
        val nsAclSpec = PropertySpec
            .builder("NS_ACL", String::class, KModifier.CONST)
            .initializer("\"http://www.w3.org/ns/auth/acl#\"")
            .build()
        val nsLdpSpec = PropertySpec
            .builder("NS_LDP", String::class, KModifier.CONST)
            .initializer("\"http://www.w3.org/ns/ldp#\"")
            .build()
        val nsSkosSpec = PropertySpec
            .builder("NS_SKOS", String::class, KModifier.CONST)
            .initializer("\"http://www.w3.org/2004/02/skos/core#\"")
            .build()
        val nsSolidSpec = PropertySpec
            .builder("NS_SOLID", String::class, KModifier.CONST)
            .initializer("\"http://www.w3.org/ns/solid/terms#\"")
            .build()
        val nsClassTagSpec = PropertySpec
            .builder("NS_${className}", String::class, KModifier.CONST)
            .initializer("\"$uriShortName\"")
            .build()

        val companionObject = TypeSpec
            .companionObjectBuilder()
            .addProperties(
                listOf(
                    absoluteUriSpec,
                    nsAcpSpec,
                    nsAclSpec,
                    nsLdpSpec,
                    nsSkosSpec,
                    nsSolidSpec,
                    nsClassTagSpec
                )
            )
            .addFunction(
                resourceConverterFn
            )
            .build()

        val utilsFileSpec = FileSpec
            .builder(packageName, "Utilities")
            .addType(
                TypeSpec
                    .classBuilder("Utilities")
                    .addType(companionObject)
                    .build()
            )
            .build()
        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val utilsFile = codeGenerator.createNewFile(dependencies, utilsFileSpec.packageName, utilsFileSpec.name)
        OutputStreamWriter(utilsFile, StandardCharsets.UTF_8)
            .use(utilsFileSpec::writeTo)
    }

    private fun buildDatabaseFile(
        annotatedClass: KSClassDeclaration
    ) {
        val className = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name
//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME
        if (className.isBlank()) {
            throw Error("class name cannot be empty")
        }

        val modelClass = ClassName("com.hp.hpl.jena.rdf.model", "Model")
        val contextClass = ClassName("android.content", "Context")
        val utilsObject = ClassName(packageName, "Utilities")

        val javaIoFileType = ClassName("java.io", "File")
        val modelFactoryType = ClassName("com.hp.hpl.jena.rdf.model", "ModelFactory")

        val modelParam = ParameterSpec
            .builder("model", modelClass)
            .build()
        val modelSpec = PropertySpec
            .builder(modelParam.name, modelParam.type)
            .initializer(modelParam.name)
            .build()
        val baseUriParam = ParameterSpec
            .builder("baseUri", String::class)
            .build()
        val baseUriSpec = PropertySpec
            .builder(baseUriParam.name, baseUriParam.type)
            .initializer(baseUriParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()
        val filePathParam = ParameterSpec
            .builder("filePath", String::class)
            .build()
        val filePathSpec = PropertySpec
            .builder(filePathParam.name, filePathParam.type)
            .initializer(filePathParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()
        val contextParam = ParameterSpec
            .builder("context", contextClass)
            .build()
        val contextSpec = PropertySpec
            .builder(contextParam.name, contextParam.type)
            .initializer(contextParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()
        val daoFunction = FunSpec
            .builder("${className}Dao")
            .returns(ClassName(packageName, "${className}Dao"))
            .addStatement("return ${className}DaoImpl(model, baseUri, filePath, context)")
            .build()
        val instanceSpec = PropertySpec
            .builder("INSTANCE", ClassName(packageName, "${className}Database").copy(true))
            .addAnnotation(
                AnnotationSpec
                    .builder(ClassName("android.annotation", "SuppressLint"))
                    .build()
            )
            .addAnnotation(Volatile::class)
            .addModifiers(listOf(KModifier.PRIVATE))
            .initializer("null")
            .mutable()
            .build()
        val getDbFun = FunSpec
            .builder("getDatabase")
            .addParameter(
                ParameterSpec
                    .builder("context", contextClass)
                    .build()
            )
            .addParameter(
                ParameterSpec
                    .builder("baseUri", String::class)
                    .build()
            )
            .addParameter(
                ParameterSpec
                    .builder("filePath", String::class)
                    .build()
            )
            .returns(ClassName(packageName, "${className}Database"))
//            .addStatement("return INSTANCE ?: synchronized(this) {")
            .beginControlFlow("if (INSTANCE != null)")
            .addStatement("return INSTANCE!!")
            .nextControlFlow("else")
            .addStatement("synchronized(this) {")
            .addStatement("val file = %T(context.filesDir, filePath)", javaIoFileType)
            .addStatement("val model: Model?")
            .beginControlFlow("if (file.exists())")
            .addStatement("val inStream = file.inputStream()")
            .addStatement("model = %T.createDefaultModel().read(inStream, null)", modelFactoryType)
            .nextControlFlow("else")
            .addStatement("model = %T.createDefaultModel()", modelFactoryType)
            .addStatement("model.setNsPrefix(\"acp\", %T.NS_ACP)", utilsObject)
            .addStatement("model.setNsPrefix(\"acl\", %T.NS_ACL)", utilsObject)
            .addStatement("model.setNsPrefix(\"ldp\", %T.NS_LDP)", utilsObject)
            .addStatement("model.setNsPrefix(\"skos\", %T.NS_SKOS)", utilsObject)
            .addStatement("model.setNsPrefix(\"ti\", %T.NS_$className)", utilsObject)
            .endControlFlow()
            .addStatement("val instance = ${className}Database(baseUri, filePath, context, model)\n")
            .addStatement("INSTANCE = instance")
            .addStatement("return instance")
            .addStatement("}")
            .addStatement("}")
            .build()
        val companionObject = TypeSpec
            .companionObjectBuilder()
            .addProperty(instanceSpec)
            .addFunction(getDbFun)
            .build()

        val dbFileSpec = FileSpec.builder(packageName, className.plus("Database"))
            .addType(
                TypeSpec.classBuilder(
                    className.plus("Database")
                )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                listOf(baseUriParam, filePathParam, contextParam, modelParam)
                            )
                            .build()
                    )
                    .addProperties(
                        listOf(baseUriSpec, filePathSpec, contextSpec, modelSpec)
                    )
                    .addFunctions(listOf(daoFunction))
                    .addType(companionObject)
                    .build()
            )
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val dbFile = codeGenerator.createNewFile(dependencies, dbFileSpec.packageName, dbFileSpec.name)
        OutputStreamWriter(dbFile, StandardCharsets.UTF_8)
            .use(dbFileSpec::writeTo)
    }

    private fun buildRepositoryFile(
        annotatedClass: KSClassDeclaration
    ) {
        val className = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name

//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME
        if (className.isBlank()) {
            throw Error("class name cannot be empty")
        }

        val uuidClass = ClassName("java.util", "UUID")
        val baseClass = ClassName(annotatedClass.packageName.asString(), annotatedClass.simpleName.getShortName())
        val listClass = List::class.asTypeName()
        val flowClass = ClassName("kotlinx.coroutines.flow", "Flow")
        val customType = flowClass.plusParameter(listClass.plusParameter(baseClass))

        val daoParam = ParameterSpec
            .builder(
                className.replaceFirstChar { it.lowercase(Locale.getDefault()) }.plus("Dao"),
                ClassName(packageName, className.plus("Dao")),
            )
            .build()

        val insertManyFn = FunSpec.builder("insertMany")
            .addParameter(
                ParameterSpec.builder("itemList", listClass.plusParameter(baseClass))
                    .build()
            )
            .addAnnotation(ClassName("androidx.annotation", "WorkerThread"))
            .addModifiers(listOf(KModifier.SUSPEND))
            .beginControlFlow("itemList.forEach")
            .addStatement("${daoParam.name}.insert(it)")
            .endControlFlow()
            .build()

        val insertFn = FunSpec.builder("insert")
            .addAnnotation(ClassName("androidx.annotation", "WorkerThread"))
            .addParameter(
                ParameterSpec.builder("item", baseClass)
                    .build()
            )
            .addModifiers(listOf(KModifier.SUSPEND))
            .beginControlFlow("if (item.id == \"\")")
            // should not have to do this but here we fuckin are
            .addStatement("item.id = %T.randomUUID().toString()", uuidClass)
            .endControlFlow()
            .addStatement("${daoParam.name}.insert(item)")
            .build()

        val deleteByUriFn = FunSpec.builder("deleteByUri")
            .addAnnotation(ClassName("androidx.annotation", "WorkerThread"))
            .addModifiers(listOf(KModifier.SUSPEND))
            .addParameter(
                ParameterSpec.builder("uri", String::class)
                    .build()
            )
            .addStatement("${daoParam.name}.delete(uri)")
            .build()

        val getLiveDataFn = FunSpec.builder("get${className}LiveData")
            .addParameter(
                ParameterSpec.builder("uri", String::class)
                    .build()
            )
            .addStatement("return ${daoParam.name}.get${className}ByIdAsFlow(uri)")
            .returns(flowClass.plusParameter(baseClass))
            .build()

        val daoParamProp = PropertySpec.builder(daoParam.name, daoParam.type)
            .initializer(daoParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()

        val allItemsProp = PropertySpec.builder("all${className}s", customType)
            .initializer("${daoParam.name}.getAll${className}s()")
            .build()

        val constructorFn = FunSpec
            .constructorBuilder()
            .addParameter(
                daoParam
            )
            .build()

        val repoFileSpec = FileSpec.builder(packageName, className.plus("Repository"))
            .addType(
                TypeSpec.classBuilder(
                    className.plus("Repository")
                )
                    .primaryConstructor(constructorFn)
                    .addProperties(
                        listOf(
                            daoParamProp,
                            allItemsProp
                        )
                    )
                    .addFunctions(
                        listOf(
                            insertFn,
                            insertManyFn,
                            deleteByUriFn,
                            getLiveDataFn
                        )
                    )
                    .build()
            )
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val repoFile = codeGenerator.createNewFile(dependencies, repoFileSpec.packageName, repoFileSpec.name)
        OutputStreamWriter(repoFile, StandardCharsets.UTF_8)
            .use(repoFileSpec::writeTo)
    }

    private fun buildDaoImplFile(
        annotatedClass: KSClassDeclaration
    ) {
        val className = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name
//        val packageName = annotatedClass.packageName.getQualifier()
        val packageName = PACKAGE_NAME
        val modelClass = ClassName("com.hp.hpl.jena.rdf.model", "Model")
        val contextClass = ClassName("android.content", "Context")
        val resourceFactory = ClassName("com.hp.hpl.jena.rdf.model", "ResourceFactory")
        val modelFactory = ClassName("com.hp.hpl.jena.rdf.model", "ModelFactory")
        val mutableStateFlowMember = MemberName("kotlinx.coroutines.flow", "MutableStateFlow")
        val flowOfMember = MemberName("kotlinx.coroutines.flow", "flowOf")

        val resourceToItem = MemberName(packageName.plus(".Utilities.Companion"), "resourceTo$className")

        val utilsObject = ClassName(packageName, "Utilities")

        if (className.isBlank()) {
            throw Error("class name cannot be empty")
        }
        val classProperties = annotatedClass.getAllProperties()
        val filteredProperties = classProperties.filter { it.simpleName.getShortName() != "id" }
        val firstProp = filteredProperties.first()

        val baseClassName = ClassName(annotatedClass.packageName.asString(), annotatedClass.simpleName.getShortName())
        val listClass = List::class.asTypeName()
        val flowClass = ClassName("kotlinx.coroutines.flow", "Flow")
        val mutableFlowClass = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
        val customType = flowClass.plusParameter(listClass.plusParameter(baseClassName))

        val insertFnBuilder = FunSpec.builder("insert")
            .addParameter(
                "item", baseClassName
            )
            .addStatement("val id = java.util.UUID.randomUUID().toString()")
            .addStatement("val mThingUri = model.createResource(\"\$baseUri#\$id\")")

        filteredProperties.forEach {
            insertFnBuilder.addStatement("val m${it.simpleName.getShortName()} = model.createProperty(%T.NS_$className + \"${it.simpleName.getShortName()}\")", utilsObject)
            when (it.type.resolve().toString()) {

                "String" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createLiteral(item.${it.simpleName.getShortName()}).string")
                }
                "Date" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createLiteral(item.${it.simpleName.getShortName()})")
                }
                "Float" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createTypedLiteral(item.${it.simpleName.getShortName()})")
                }
                "Int" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createTypedLiteral(item.${it.simpleName.getShortName()})")
                }
                "Boolean" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createTypedLiteral(item.${it.simpleName.getShortName()})")
                }
                "Long" -> {
                    insertFnBuilder.addStatement("val ${it.simpleName.getShortName()}Literal = model.createTypedLiteral(item.${it.simpleName.getShortName()})")
                }
            }

            insertFnBuilder.addStatement("mThingUri.addProperty(m${it.simpleName.getShortName()}, ${it.simpleName.getShortName()}Literal)")
        }

        val insertFn = insertFnBuilder
            .addStatement("val file = java.io.File(context.filesDir, saveFilePath)")
            .addStatement("val os = file.outputStream()")
            .addStatement("model.write(os, null, null)")
            .addStatement("modelLiveData.value = all${className}s()")
            .addModifiers(listOf(KModifier.SUSPEND, KModifier.OVERRIDE))
            .build()

        val deleteFn = FunSpec.builder("delete")
            .addParameter(
                "uri", String::class
            )
            .addStatement("val resource = %T.createDefaultModel().createResource(\"\$baseUri#\$uri\")", modelFactory)
            .addStatement("model.removeAll(resource, null ,null)")
            .addStatement("val file = java.io.File(context.filesDir, saveFilePath)")
            .addStatement("val os = file.outputStream()")
            .addStatement("model.write(os, null, null)")
            .addStatement("modelLiveData.value = all${className}s()")
            .addModifiers(listOf(KModifier.SUSPEND, KModifier.OVERRIDE))
            .build()

        val modelParam = ParameterSpec
            .builder("model", modelClass)
            .build()
        val modelSpec = PropertySpec
            .builder(modelParam.name, modelParam.type)
            .initializer(modelParam.name)
            .build()
        val baseUriParam = ParameterSpec
            .builder("baseUri", String::class)
            .build()
        val baseUriSpec = PropertySpec
            .builder(baseUriParam.name, baseUriParam.type)
            .initializer(baseUriParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()
        val filePathParam = ParameterSpec
            .builder("saveFilePath", String::class)
            .build()
        val filePathSpec = PropertySpec
            .builder(filePathParam.name, filePathParam.type)
            .initializer(filePathParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()
        val contextParam = ParameterSpec
            .builder("context", contextClass)
            .build()
        val contextSpec = PropertySpec
            .builder(contextParam.name, contextParam.type)
            .initializer(contextParam.name)
            .addModifiers(KModifier.PRIVATE)
            .build()

        val constructorFn = FunSpec.constructorBuilder()
            .addParameters(
                listOf(
                    modelParam,
                    baseUriParam,
                    filePathParam,
                    contextParam
                )
            )
            .build()

        val modelLiveDataProp = PropertySpec.builder("modelLiveData", mutableFlowClass.plusParameter(listClass.plusParameter(baseClassName)))
            // should be MutableLiveData(allItems())
            .initializer("%M(all${className}s())", mutableStateFlowMember)
            .build()

        val getByIdFn = FunSpec.builder("get${annotatedClass.simpleName.getShortName()}ByIdAsFlow")
            .addParameter(
                "id", String::class
            )
            .addStatement("val toSearch = %T.createResource(\"\$baseUri#\$id\")", resourceFactory)
            .beginControlFlow("if (model.containsResource(toSearch))")
            .addStatement("return %M(%M(model.getResource(toSearch.uri)))", flowOfMember, resourceToItem)
            .endControlFlow()
            .addStatement("return %M()", flowOfMember)
            .returns(flowClass.plusParameter(baseClassName))
            .addModifiers(listOf(KModifier.OVERRIDE))
            .build()

        val listAllItemsFn = FunSpec.builder("all${className}s")
            .addStatement("val itemList = mutableListOf<$className>()")
            .addStatement("val res = model.listResourcesWithProperty(model.createProperty(%T.NS_$className + \"$firstProp\"))", utilsObject)
            .beginControlFlow("while (res.hasNext())")
            .addStatement("val nextResource = res.nextResource()")
            .addStatement("itemList.add(%M(nextResource))", resourceToItem)
            .endControlFlow()
            .addStatement("return itemList")
            .addModifiers(listOf(KModifier.PRIVATE))
            .returns(listClass.plusParameter(baseClassName))
            .build()

        val getAllItemsFn = FunSpec.builder("getAll${className}s")
            .addStatement("return modelLiveData")
            .returns(customType)
            .addModifiers(listOf(KModifier.OVERRIDE))
            .build()

        val daoImplFileSpec = FileSpec.builder(packageName, className.plus("DaoImpl"))
            .addType(
                TypeSpec
                    .classBuilder(className.plus("DaoImpl"))
                    .addSuperinterface(ClassName(packageName, annotatedClass.simpleName.getShortName().plus("Dao")))
                    .primaryConstructor(
                        constructorFn
                    )
                    .addProperties(
                        listOf(
                            modelSpec,
                            baseUriSpec,
                            filePathSpec,
                            contextSpec
                        )
                    )
                    .addProperty(
                        modelLiveDataProp
                    )
                    .addFunctions(
                        listOf(
                            getAllItemsFn,
                            listAllItemsFn,
                            insertFn,
                            deleteFn,
                            getByIdFn
                        )
                    )
                    .build()
            )
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val daoImplFile = codeGenerator.createNewFile(dependencies, daoImplFileSpec.packageName, daoImplFileSpec.name)
        OutputStreamWriter(daoImplFile, StandardCharsets.UTF_8)
            .use(daoImplFileSpec::writeTo)
    }

    private fun buildDaoFile(annotatedClass: KSClassDeclaration) {
        val modelName = annotatedClass
            .getAnnotationsByType(SolidAnnotation::class)
            .single()
            .name
//        val modelPackage = annotatedClass.packageName.getQualifier()
        val modelPackage = PACKAGE_NAME
        if (modelName.isBlank()) {
            throw Error("Interface name cannot be empty")
        }

        val baseClass = ClassName(annotatedClass.packageName.asString(), annotatedClass.simpleName.getShortName())
        val listClass = List::class.asTypeName()
        val flowClass = ClassName("kotlinx.coroutines.flow", "Flow")
        val customType = flowClass.plusParameter(listClass.plusParameter(baseClass))

        val getAllFn = FunSpec.builder("getAll${modelName}s")
            .addModifiers(listOf(KModifier.ABSTRACT))
            .returns(customType)
            .build()

        val insertFn = FunSpec.builder("insert")
            .addParameter(
                "item", baseClass
            )
            .addModifiers(listOf(KModifier.SUSPEND, KModifier.ABSTRACT))
            .build()

        val deleteFn = FunSpec.builder("delete")
            .addParameter(
                "uri", String::class
            )
            .addModifiers(listOf(KModifier.SUSPEND, KModifier.ABSTRACT))
            .build()

        val getByIdFn = FunSpec.builder("get${annotatedClass.simpleName.getShortName()}ByIdAsFlow")
            .addParameter(
                "id", String::class
            )
            .returns(flowClass.plusParameter(baseClass))
            .addModifiers(listOf(KModifier.ABSTRACT))
            .build()

        val daoFileSpec = FileSpec.builder(modelPackage, modelName.plus("Dao"))
            .addType(
                TypeSpec.interfaceBuilder(
                    modelName.plus("Dao")
                )
                    .addFunctions(
                        listOf(
                            getByIdFn,
                            deleteFn,
                            getAllFn,
                            insertFn
                        )
                    )
                    .build()
            )
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        val daoFile = codeGenerator.createNewFile(dependencies, daoFileSpec.packageName, daoFileSpec.name)
        OutputStreamWriter(daoFile, StandardCharsets.UTF_8)
            .use(daoFileSpec::writeTo)
    }

}