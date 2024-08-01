package uk.co.thomasc.thealley.repo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi

inline fun <reified T : Any> Table.json(
    name: String,
    kSerializer: KSerializer<T> = serializer(),
    json: Json
): Column<T> =
    this.json(
        name = name,
        stringify = { json.encodeToString(kSerializer, it) },
        parse = { json.decodeFromString(kSerializer, it) }
    )

fun <T : Any> Table.json(name: String, stringify: (T) -> String, parse: (String) -> T): Column<T> =
    registerColumn(name, JsonColumnType(stringify, parse))

class JsonColumnType<T : Any>(private val stringify: (T) -> String, private val parse: (String) -> T) : ColumnType<T>() {
    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        stmt[index] = value as String
    }

    override fun sqlType(): String = "json"
    override fun valueFromDB(value: Any) = parse(value as String)

    override fun notNullValueToDB(value: T) = stringify(value)

    override fun valueToString(value: T?): String = when (value) {
        is Iterable<*> -> notNullValueToDB(value)
        else -> super.valueToString(value)
    }
}
