package com.cristobal.accenturetrackerexample.domain.usecases

import com.cristobal.accenturetrackerexample.data.models.LiveResult
import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData
import com.cristobal.accenturetrackerexample.domain.sources.RoomDataSource
import com.cristobal.accenturetrackerexample.utils.ResultUseCase

class GetUserDataUseCase(
    private val roomDataSource: RoomDataSource
) : ResultUseCase<String, UserData>() {
    override suspend fun executeOnBackground(params: String): UserData {
        return roomDataSource.getUsersFromRoomDatabase().first()
    }

    override suspend fun executeOnException(throwable: Throwable): Throwable? {
        return super.executeOnException(throwable)
    }

    override suspend fun executeOnResponse(liveData: LiveResult<UserData>, response: UserData) {
        super.executeOnResponse(liveData, response)
    }
}
