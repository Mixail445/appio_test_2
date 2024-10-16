package com.example.appio_test_2.data.repository.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.appio_test_2.domain.DomainPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM PlaceEntity")
    fun getAll(): Flow<List<PlaceEntity>>

    @Query("DELETE FROM PlaceEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity)

    @Update
    suspend fun update(place: PlaceEntity)

}

@Entity(tableName = "PlaceEntity")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val name: String = "",
) {
    fun mapToDomain() = DomainPoint(
        id = id,
        latitude = latitude,
        longitude = longitude,
        name = name
    )
}