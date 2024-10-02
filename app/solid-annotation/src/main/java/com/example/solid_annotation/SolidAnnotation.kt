package com.example.solid_annotation

@Target(AnnotationTarget.CLASS)
annotation class SolidAnnotation(val name: String, val absoluteUri: String, val uriShortName: String)