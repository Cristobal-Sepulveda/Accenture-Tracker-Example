package com.cristobal.accenturetrackerexample.domain.domainobjects

data class RequestAlertParams(
    val permissionRequestList: List<String>,
    val permissionsShowRationale: String,
    val alertTitle: Int,
    val alertMessage: Int
)