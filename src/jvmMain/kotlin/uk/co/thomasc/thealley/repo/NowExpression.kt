package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class NowExpression<T>(override val columnType: IColumnType<T & Any>, private val transactionTime: Boolean) : ExpressionWithColumnType<T>() {
    constructor(column: Column<T>, transactionTime: Boolean = true) : this(column.columnType, transactionTime)

    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(if (transactionTime) "NOW()" else "clock_timestamp()")
    }
}
