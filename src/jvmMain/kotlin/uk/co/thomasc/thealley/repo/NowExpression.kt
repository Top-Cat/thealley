package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class NowExpression<T>(override val columnType: IColumnType) : ExpressionWithColumnType<T>() {
    constructor(column: Column<T>) : this(column.columnType)

    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("NOW()")
    }
}
