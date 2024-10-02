package com.example.solid_auth

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
import com.example.solid_annotation.SolidAuthAnnotation
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

const val PACKAGE_NAME = "com.solidannotations"
class SolidAuthProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {
    private val WEB_ID = "web_id"
    private val ACCESS_TOKEN = "access_token"
    private val REFRESH_TOKEN = "refresh_token"
    private val ID_TOKEN = "id_token"
    private val CLIENT_ID = "client_id"
    private val CLIENT_SECRET = "client_secret"
    private val TOKEN_URI = "token_uri"
    private val CODE_VERIFIER = "code_verifier"
    private val OIDC_PROVIDER = "oidc_provider"
    private val REDIRECT_URI = "redirect_uri"
    private val SIGNER = "signer"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(SolidAuthAnnotation::class.qualifiedName.toString())
            .filterIsInstance<KSFunctionDeclaration>()
            // Keeping this logging to make it possible to observe incremental build
            .also { logger.warn("Generating for ${it.joinToString { it.simpleName.getShortName() }}") }
            .forEach(::generateAuth)

        return emptyList()
    }

    private fun generateAuth(annotatedFunction: KSFunctionDeclaration) {
        buildTokenStoreFile(annotatedFunction)
        buildDPoPAuthFile(annotatedFunction)
        buildAuthUtilitiesFile(annotatedFunction)
    }

    private fun buildAuthUtilitiesFile(
        annotatedFunction: KSFunctionDeclaration
    ) {
//        val packageName = annotatedFunction.packageName.getQualifier()
        val packageName = PACKAGE_NAME

        val okHttpResponse = ClassName("okhttp3", "Response")
        val okHttpClient = ClassName("okhttp3", "OkHttpClient")
        val okHttpRequest = ClassName("okhttp3", "Request")
        val okHttpGetFun = FunSpec // okHttpRequest in cycletracker
            .builder("okHttpGetRequest")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("url", String::class)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val request = %T.Builder().url(url).build()", okHttpRequest)
            .addStatement("val response = client.newCall(request).execute()")
            .addStatement("return response")
            .returns(okHttpResponse)
            .build()

        val modelFactory = ClassName("com.hp.hpl.jena.rdf.model","ModelFactory")
        val queryFactory = ClassName("com.hp.hpl.jena.query","QueryFactory")
        val queryExecutionFactory = ClassName("com.hp.hpl.jena.query","QueryExecutionFactory")

        val setOidcProviderFun = FunSpec
            .builder("setOidcProvider")
            .addParameter("webIdResponse", String::class)
            .addStatement("val stringAsByteArray = webIdResponse.toByteArray()")
            .addStatement("val utf8String = String(stringAsByteArray, Charsets.UTF_8)")
            .addStatement("val inStream = utf8String.byteInputStream()")
            .addStatement("val m = %T.createDefaultModel().read(inStream, null, \"TURTLE\")", modelFactory)
            .addCode("""
                val queryString = "SELECT ?o\n" +
                    "WHERE\n" +
                    "{ ?s <http://www.w3.org/ns/solid/terms#oidcIssuer> ?o }"
                    
            """.trimIndent())
            .addStatement("val q = %T.create(queryString)", queryFactory)
            .addStatement("var result = \"\"")
            .addStatement("try {")
            .addStatement("val qexec = %T.create(q, m)", queryExecutionFactory)
            .addStatement("val results = qexec.execSelect()")
            .addStatement("while (results.hasNext()) {")
            .addStatement("val soln = results.nextSolution()")
            .addStatement("result = soln.getResource(\"o\").toString()")
            .addStatement("break")
            .addStatement("}")
            .addStatement("} catch (e: Exception) {")
//            .addStatement()
            .addStatement("}")
            .addStatement("return result")
            .returns(String::class)
            .build()

        val httpUrl = ClassName("okhttp3", "HttpUrl")
        val uriClass = ClassName("android.net", "Uri")
        val toHttpUrl = MemberName("okhttp3.HttpUrl.Companion", "toHttpUrl")
        val fetchAuthFun = FunSpec
            .builder("fetchAuth")
            .addParameter("authUrl", String::class)
            .addParameter("clientId", String::class)
            .addParameter("clientSecret", String::class.asTypeName().copy(true))
            .addParameter("codeVerifierChallenge", String::class)
            .addParameter("redirectUri", String::class)
            .addStatement("val authUrl2 = %T.parse(authUrl)", uriClass)
            .addCode("""
                val newAuthUriBuilder = Uri.Builder()
                    .scheme(authUrl2.scheme)
                    .authority(authUrl2.authority)
                    .appendEncodedPath("authorization")
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("redirect_uri", redirectUri)
                    .appendQueryParameter("scope", "offline_access openid webid")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("code_challenge_method", "S256")
                    .appendQueryParameter("code_challenge", codeVerifierChallenge)
                    .appendQueryParameter("prompt", "consent")
            
                if (clientSecret != null) {
                    newAuthUriBuilder.appendQueryParameter("client_secret", clientSecret)
                }
                val newAuthUri = newAuthUriBuilder.build()
                
            """.trimIndent())
            .addStatement("val newUrl = newAuthUri.toString().%M()", toHttpUrl)
            .addStatement("return newUrl")
            .returns(httpUrl)
            .build()

        val jsonObject = ClassName("org.json", "JSONObject")
        val jsonArray = ClassName("org.json", "JSONArray")
        val toRequestBody = MemberName("okhttp3.RequestBody.Companion", "toRequestBody")
        val toMediaTypeOrNull = MemberName("okhttp3.MediaType.Companion", "toMediaTypeOrNull")
        val fetchRegistrationFun = FunSpec
            .builder("fetchRegistration")
            .addParameter("clientName", String::class)
            .addParameter("registrationUrl", String::class)
            .addParameter("redirectUris", List::class.plusParameter(String::class))
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val jsonData = %T()", jsonObject)
            .addStatement("val redirectUrisJSON = %T(redirectUris)", jsonArray)
            .addCode("""
                
                jsonData.put("client_name", clientName)
                jsonData.put("redirect_uris", redirectUrisJSON)
                jsonData.put("application_type", "native")
                jsonData.put("token_endpoint_auth_method", "client_secret_post")
                val arr = JSONArray()
                arr.put("authorization_code")
                arr.put("refresh_token")
                jsonData.put("grant_types", arr)
            
                val jsonString = jsonData.toString()
                    
            """.trimIndent())
            .addStatement("val postBody = jsonString.%M(\"application/json\".%M())\n", toRequestBody, toMediaTypeOrNull)
            .addCode("""
                val request = Request.Builder()
                    .url(registrationUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(postBody)
                    .build()
                    
            """.trimIndent())
            .addStatement("val response = client.newCall(request).execute()")
            .addStatement("return response")
            .returns(okHttpResponse)
            .build()

        val fetchConfigFun = FunSpec
            .builder("fetchConfig")
            .addParameter("configUrl", String::class)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val request = %T.Builder().url(\"\$configUrl/.well-known/openid-configuration\").build()", okHttpRequest)
            .addStatement("val response = client.newCall(request).execute()")
            .addStatement("return response")
            .returns(okHttpResponse)
            .build()

        val formBody = ClassName("okhttp3","FormBody")
        val grantTypes = ClassName("net.openid.appauth", "GrantTypeValues")

        val tokenRequestFun = FunSpec
            .builder("tokenRequest")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("clientId", String::class)
            .addParameter("clientSecret", String::class.asTypeName().copy(true)) // needs nullable
            .addParameter("tokenUrl", String::class)
            .addParameter("code", String::class)
            .addParameter("codeVerifier", String::class)
            .addParameter("redirectUri", String::class)
            .addParameter("authString", String::class)
            .addStatement("val client = %T()", okHttpClient)
            .addStatement("val bodyBuilder = %T.Builder()", formBody)
            .addStatement(".add(\"grant_type\", %T.AUTHORIZATION_CODE)", grantTypes)
            .addStatement(".add(\"code_verifier\", codeVerifier)")
            .addStatement(".add(\"code\", code)")
            .addStatement(".add(\"redirect_uri\", redirectUri)")
            .addStatement(".add(\"client_id\", clientId)")
            .beginControlFlow("if (clientSecret != null)")
            .addStatement("bodyBuilder.add(\"client_secret\", clientSecret)")
            .endControlFlow()
            .addStatement("val body = bodyBuilder.build()")
            .addStatement("val tokenRequest = %T.Builder()", okHttpRequest)
            .addStatement(".url(tokenUrl)")
            .addStatement(".addHeader(\"Accept\", \"*/*\")")
            .addStatement(".addHeader(\"DPoP\", authString)")
            .addStatement(".addHeader(\"Content-Type\", \"application/x-www-form-urlencoded\")")
            .addStatement(".post(body)")
            .addStatement(".build()")
            .addStatement("val response = client.newCall(tokenRequest).execute()")
            .addStatement("val responseBody = response.body!!.string()")
            .addStatement("return responseBody")
            .returns(String::class)
            .build()

        val authUtilsClassName = ClassName(packageName, "AuthUtilities")

        val authUtilsFileSpec = FileSpec
            .builder(authUtilsClassName)
            .addFunction(okHttpGetFun)
            .addFunction(tokenRequestFun)
            .addFunction(setOidcProviderFun)
            .addFunction(fetchAuthFun)
            .addFunction(fetchRegistrationFun)
            .addFunction(fetchConfigFun)
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedFunction.containingFile!!)
        val storeFile = codeGenerator.createNewFile(dependencies, packageName,
            "AuthUtilities"
        )
        OutputStreamWriter(storeFile, StandardCharsets.UTF_8)
            .use(authUtilsFileSpec::writeTo)
    }

    private fun buildDPoPAuthFile(
        annotatedFunction: KSFunctionDeclaration
    ) {
//        val packageName = annotatedFunction.packageName.getQualifier()
        val packageName = PACKAGE_NAME

        val tokenUriParam = ParameterSpec
            .builder("tokenUri", String::class.asTypeName().copy(true))
            .defaultValue("\"\"")
            .build()
        val tokenUriProp = PropertySpec
            .builder(tokenUriParam.name, tokenUriParam.type)
            .mutable(true)
            .initializer("tokenUri")
            .addModifiers(KModifier.PRIVATE)
            .build()

        val signerTypeNotNull = ClassName("com.nimbusds.jose.crypto", "ECDSASigner")
        val signerType = signerTypeNotNull.copy(nullable = true)

        val signerParam = ParameterSpec
            .builder("signer", signerType)
            .defaultValue("null")
            .build()
        val signerProp = PropertySpec
            .builder(signerParam.name, signerParam.type)
            .mutable(true)
            .initializer("signer")
            .addModifiers(KModifier.PRIVATE)
            .build()

        val keyType = ClassName("com.nimbusds.jose.jwk", "JWK").copy(true)
        val keyParam = ParameterSpec
            .builder("key", keyType)
            .defaultValue("null")
            .build()
        val keyProp = PropertySpec
            .builder(keyParam.name, keyParam.type)
            .mutable(true)
            .initializer("key")
            .build()

        val joseObjectType = ClassName("com.nimbusds.jose", "JOSEObjectType")
        val jwsAlgorithm = ClassName("com.nimbusds.jose", "JWSAlgorithm")
        val jwsHeader = ClassName("com.nimbusds.jose", "JWSHeader")
        val curve = ClassName("com.nimbusds.jose.jwk", "Curve")
        val ecKey = ClassName("com.nimbusds.jose.jwk", "ECKey")
        val ecKeyGenerator = ClassName("com.nimbusds.jose.jwk.gen", "ECKeyGenerator")

        val jwtClaimsSet = ClassName("com.nimbusds.jwt", "JWTClaimsSet")
        val jwtSignedJWT = ClassName("com.nimbusds.jwt", "SignedJWT")

        val calendar = ClassName("java.util", "Calendar")

        val randomUUID = MemberName("java.util.UUID", "randomUUID")
        val generateAuthStringFun = FunSpec
            .builder("generateAuthString")
            .addParameter(
                ParameterSpec
                    .builder("method", String::class)
                    .build()
            )
            .addStatement("val keyId = %M().toString()", randomUUID)
            .beginControlFlow(" if (key == null)")
            .addStatement("val ecJWK = %T(%T.P_256).generate()", ecKeyGenerator, curve)
            .addStatement("this.key = ecJWK")
            .endControlFlow()
            .addStatement("val ecPublicJWK = key!!.toPublicJWK()")
            .beginControlFlow("if (signer == null)")
            .addStatement("val signer = %T(key as %T)", signerTypeNotNull, ecKey)
            .addStatement("this.signer = signer")
            .endControlFlow()
            .addStatement("val body = %T.Builder().claim(\"htu\", " +
                    "tokenUri).claim(\"htm\", method).issueTime(%T.getInstance().time).jwtID(%M().toString()).claim(\"nonce\", %M().toString()).build()",
                jwtClaimsSet, calendar, randomUUID, randomUUID)
            .addStatement("val header = %T.Builder(%T.ES256).keyID(keyId).type(%T(\"dpop+jwt\")).jwk(ecPublicJWK).build()",
                jwsHeader, jwsAlgorithm, joseObjectType)
            .addStatement("val signedJWT = %T(header, body)", jwtSignedJWT)
            .addStatement("signedJWT.sign(signer)")
            .addStatement("return signedJWT.serialize()")
            .returns(String::class)
            .build()

        val dpopClassName = ClassName(packageName, "DPoPAuth")

        val constructorFn = FunSpec
            .constructorBuilder()
            .addParameters(
                listOf(
                    keyParam,
                    signerParam,
                    tokenUriParam
                )
            )
            .build()

        val dpopClassBuilder = TypeSpec
            .classBuilder(dpopClassName)
            .primaryConstructor(constructorFn)
            .addProperties(
                listOf(
                    keyProp,
                    signerProp,
                    tokenUriProp
                )
            )
            .addFunction(generateAuthStringFun)

        val dpopClass = dpopClassBuilder
            .build()

        val dpopFileSpec = FileSpec
            .builder(dpopClassName)
            .addType(dpopClass)
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedFunction.containingFile!!)
        val storeFile = codeGenerator.createNewFile(dependencies, packageName,
            "DPoPAuth"
        )
        OutputStreamWriter(storeFile, StandardCharsets.UTF_8)
            .use(dpopFileSpec::writeTo)
    }

    private fun buildTokenStoreFile(
        annotatedFunction: KSFunctionDeclaration
    ) {
//        val packageName = annotatedFunction.packageName.getQualifier()
        val packageName = PACKAGE_NAME

        val keysList = listOf(
            WEB_ID,
            ACCESS_TOKEN,
            REFRESH_TOKEN,
            ID_TOKEN,
            CLIENT_ID,
            CLIENT_SECRET,
            TOKEN_URI,
            CODE_VERIFIER,
            OIDC_PROVIDER,
            REDIRECT_URI,
            SIGNER
        )

        val storeClassName = ClassName(packageName, "AuthTokenStore")
        val preferencesType = ClassName("androidx.datastore.preferences.core", "Preferences")
        val dataStoreType = ClassName("androidx.datastore.core", "DataStore")

        val stringPrefsMember = MemberName("androidx.datastore.preferences.core", "stringPreferencesKey")
        val mapMember = MemberName("kotlinx.coroutines.flow", "map")
        val editMember = MemberName("androidx.datastore.preferences.core", "edit")

        val contextStoreType = ClassName(packageName, "dataStore")
        val dataStoreProperty = PropertySpec
            .builder("dataStore", dataStoreType.plusParameter(preferencesType))
            .addModifiers(KModifier.PRIVATE)
            .initializer("context.%T", contextStoreType)
            .build()
        val companionBuilder = TypeSpec
            .companionObjectBuilder()

        val contextClass = ClassName("android.content", "Context")
        val contextParam = ParameterSpec
            .builder("context", contextClass)
            .build()
        val contextSpec = PropertySpec
            .builder(contextParam.name, contextParam.type)
            .initializer("context")
            .build()

        val contextFn = FunSpec
            .constructorBuilder()
            .addParameter(contextParam)
            .build()

        val storeClassBuilder = TypeSpec
            .classBuilder(storeClassName)
            .primaryConstructor(contextFn)
            .addProperty(contextSpec)
            .addProperty(dataStoreProperty)

        val flowType = ClassName("kotlinx.coroutines.flow", "Flow")

        val preferencesKeyClass = ClassName("androidx.datastore.preferences.core.Preferences", "Key")

        keysList.forEach {
            val charAfterUnderscoreIdx = it.indexOfFirst {char -> char == '_'}.plus(1)
            val capitalChar = it[charAfterUnderscoreIdx].uppercase()
            val newMemberName = it.replaceRange(charAfterUnderscoreIdx, charAfterUnderscoreIdx + 1, capitalChar)
            val memberCamelCase = newMemberName.replace("_", "")
            val memberProperCase = memberCamelCase.replaceFirstChar { char -> char.uppercase() }

            val thing = PropertySpec
                .builder(it.uppercase(), preferencesKeyClass.plusParameter(String::class.asTypeName()))
                .initializer("%M(\"$it\")", stringPrefsMember)
                .build()
            companionBuilder.addProperty(thing)

            val getFun = FunSpec
                .builder("get${memberProperCase}")
                .addStatement("return dataStore.data.%M { it[${it.uppercase()}] ?: \"\" }", mapMember)
                .returns(flowType.plusParameter(String::class.asTypeName()))
                .build()

            val setFun = FunSpec
                .builder("set${memberProperCase}")
                .addParameter(
                    ParameterSpec
                        .builder(memberCamelCase, String::class)
                        .build()
                )
                .addModifiers(KModifier.SUSPEND)
                .addStatement("dataStore.%M { it[${it.uppercase()}] = $memberCamelCase }", editMember)
                .build()

            storeClassBuilder
                .addFunctions(listOf(setFun, getFun))
        }

        val companion = companionBuilder.build()

        val storeClass = storeClassBuilder
            .addType(companion)
            .build()
        val storeFileSpec = FileSpec
            .builder(storeClassName)
            .addType(storeClass)
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedFunction.containingFile!!)
        val storeFile = codeGenerator.createNewFile(dependencies, packageName,
            "AuthTokenStore"
        )
        OutputStreamWriter(storeFile, StandardCharsets.UTF_8)
            .use(storeFileSpec::writeTo)
    }
}