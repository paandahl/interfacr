package no.sb1.hackathon

import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf

fun generateTypescriptInterfaces(classes: List<KClass<*>>): String {
    val ts = StringBuilder()
    classes.forEach { clazz ->
        if (clazz.isSubclassOf(Enum::class)) {
            ts.append(clazz.tsEnum())
        } else {
            ts.append(clazz.tsInterface(classes))
        }
    }
    return ts.toString()
}

fun KClass<*>.tsInterface(classes: List<KClass<*>>): String {
    val ts = StringBuilder()
    ts.appendln("export interface ${this.simpleName} {")
    val memberProps = this.declaredMemberProperties
    memberProps.forEach {
        ts.appendln("    ${it.name}${nullabilityMarker(it.returnType)}: ${it.returnType.tsType(classes)}")
    }
    ts.appendln("}\n")
    return ts.toString()
}

fun KClass<*>.tsEnum(): String {
    val ts = StringBuilder()
    ts.appendln("export const enum ${this.simpleName} {")
    val enumConstants = this.java.enumConstants.map { it as Enum<*> }
    enumConstants.forEach {
        ts.appendln("    ${it.name} = '${it.name}'")
    }
    ts.appendln("}\n")
    return ts.toString()
}

fun KType.tsType(interfaceClasses: List<KClass<*>>): String =
        when (val clazz = this.classifier as KClass<*>) {
            Int::class -> "number"
            Long::class -> "number"
            Double::class -> "number"
            String::class -> "string"
            List::class -> {
                val listElementKtType = this.arguments.first().type!!
                val listElementTsType = listElementKtType.tsType(interfaceClasses)
                "Array<$listElementTsType${undefinedMarker(listElementKtType)}>"
            }
            else -> if (interfaceClasses.contains(clazz)) {
                clazz.simpleName!!
            } else {
                throw Exception("No TS interface for class: ${clazz.simpleName}")
            }
        }

fun nullabilityMarker(type: KType): String = if (type.isMarkedNullable) "?" else ""

fun undefinedMarker(type: KType): String = if (type.isMarkedNullable) "|undefined" else ""