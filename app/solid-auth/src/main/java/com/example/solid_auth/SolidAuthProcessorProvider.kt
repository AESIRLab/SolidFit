package com.example.solid_auth

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


class SolidAuthProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SolidAuthProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
}