package org.bazar.authorization.utils.extensions

import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.grpc.CreateUserRequest
import org.bazar.authorization.grpc.DeleteSpaceRequest
import org.bazar.authorization.grpc.DeleteUserRequest
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions

fun CreateUserRequest.validate() {
    val errMessageList = mutableListOf<String>()
    if (this.spaceId == 0L)
        errMessageList.add("spaceId can't be null. ")
    if (this.userId == null || this.userId.isBlank())
        errMessageList.add("userId can't be blank")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun AuthorizeRequest.validate() {
    val errMessageList = mutableListOf<String>()
    if (this.resource.isEmpty())
        errMessageList.add("kind can't empty. ")
    if (this.action.isEmpty())
        errMessageList.add("action can't be empty. ")
    if (this.spaceId == 0L)
        errMessageList.add("resource_id can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun DeleteSpaceRequest.validate() {
    val errMessageList = mutableListOf<String>()

    if (this.spaceId == 0L)
        errMessageList.add("space id can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun DeleteUserRequest.validate() {
    val errMessageList = mutableListOf<String>()

    if (this.userId == null || this.userId.isBlank())
        errMessageList.add("userId can't be null")

    if (this.spaceId == 0L)
        errMessageList.add("spaceId can't be null")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}