package com.cristobal.accenturetrackerexample.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cristobal.accenturetrackerexample.data.models.dbo.UsuarioDBO

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(usuario: UsuarioDBO)

    @Query("select * from UsuarioDBO")
    fun getUsers(): List<UsuarioDBO>

    @Query("delete from UsuarioDBO")
    fun deleteUsers()
}