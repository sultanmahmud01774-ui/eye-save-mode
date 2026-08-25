package com.mdsultanmahamud.eyesavemode.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPresetDao {
    @Query("SELECT * FROM custom_presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<FilterPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: FilterPreset)

    @Update
    suspend fun updatePreset(preset: FilterPreset)

    @Delete
    suspend fun deletePreset(preset: FilterPreset)

    @Query("DELETE FROM custom_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)

    @Query("SELECT * FROM custom_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: String): FilterPreset?
}
