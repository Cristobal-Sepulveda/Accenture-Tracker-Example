package com.cristobal.accenturetrackerexample.data.models.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cristobal.accenturetrackerexample.domain.domainobjects.UserData
import java.util.UUID

@Entity
data class UsuarioDBO(
    val email: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)

fun List<UsuarioDBO>.asDomainModel() : List<UserData> {
    return map {
        UserData(
            email = it.email,
            isUserSupervisor = it.email == "1@1.1"
        )
    }
}
