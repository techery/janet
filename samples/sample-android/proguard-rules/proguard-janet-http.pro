## Keep Generated HttpActionHelperFactory
-keep class * implements io.techery.janet.HttpActionService$ActionHelperFactory
#
## Annotation processor (compiler) classes should be ignored
-dontwarn javax.servlet.**
-dontwarn com.google.auto.common.**
-dontwarn com.google.auto.service.processor.**
-dontwarn com.squareup.javapoet.**
-dontwarn org.apache.commons.collections.BeanMap
-dontwarn org.apache.tools.**
-dontwarn org.apache.velocity.**
-dontwarn io.techery.janet.compiler.**
-dontwarn io.techery.janet.validation.**
-dontwarn io.techery.janet.JanetHttpProcessor
-dontwarn io.techery.janet.HttpActionClass
-dontwarn io.techery.janet.HttpHelpersGenerator
-dontwarn io.techery.janet.HelpersFactoryGenerator
