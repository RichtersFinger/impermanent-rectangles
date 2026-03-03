package com.example.impermanentrectangles.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(tableName = "item_lists")
data class ItemListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val iterationStartTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = ItemListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val title: String,
    val description: String,
    val currentValue: Int,
    val targetValue: Int,
    val position: Int = 0
)

@Entity(
    tableName = "history_entries",
    foreignKeys = [
        ForeignKey(
            entity = ItemListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "history_values",
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["historyEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistoryValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val historyEntryId: Long,
    val itemId: String,
    val value: Float
)

data class HistoryEntryWithValues(
    @Embedded val entry: HistoryEntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "historyEntryId"
    )
    val values: List<HistoryValueEntity>
)
