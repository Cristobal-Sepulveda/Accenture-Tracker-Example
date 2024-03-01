package com.cristobal.accenturetrackerexample.data.repositories

import com.cristobal.accenturetrackerexample.data.models.dbo.UsuarioDBO
import com.cristobal.accenturetrackerexample.data.models.dbo.asDomainModel
import com.cristobal.accenturetrackerexample.data.room.LocalDatabase
import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData
import com.cristobal.accenturetrackerexample.domain.sources.RoomDataSource
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class RoomRepository(
    private val roomDatabase: LocalDatabase,
    private val ioDispatcher: CoroutineContext
): RoomDataSource {
    override suspend fun saveUser(email: String) = withContext(ioDispatcher) {
        roomDatabase.userDao.saveUser(UsuarioDBO(email))
    }

    override suspend fun deleteUsers() = withContext(ioDispatcher) {
        roomDatabase.userDao.deleteUsers()
    }

    override suspend fun getUsersFromRoomDatabase() : List<UserData> = withContext(ioDispatcher) {
        return@withContext roomDatabase.userDao.getUsers().asDomainModel()
    }
    override suspend fun isUserRoleAdministrador(): Boolean = withContext(ioDispatcher) {
        val currentUserEmail = roomDatabase.userDao.getUsers().first().email
        return@withContext currentUserEmail == "1@1.1"
    }
}