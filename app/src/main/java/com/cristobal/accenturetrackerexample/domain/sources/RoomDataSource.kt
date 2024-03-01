package com.cristobal.accenturetrackerexample.domain.sources

import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData

interface RoomDataSource {
    suspend fun saveUser(email: String)

    suspend fun deleteUsers()

    suspend fun isUserRoleAdministrador(): Boolean

    suspend fun getUsersFromRoomDatabase(): List<UserData>
}