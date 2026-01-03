package org.bazar.authorization.utils.extensions

import org.bazar.authorization.grpc.AddUserToSpaceRequest
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.grpc.CreateSpaceRequest
import org.bazar.authorization.grpc.RemoveUserFromSpaceRequest
import org.bazar.authorization.persistence.entity.enums.Role
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions

fun AddUserToSpaceRequest.validate() {
    val errMessageList = mutableListOf<String>()
    if (!Role.contains(this.role))
        errMessageList.add("role can't be empty. ")
    if (this.spaceId == 0L)
        errMessageList.add("spaceId can't be null. ")
    if (this.userId.isEmpty())
        errMessageList.add("userId can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun RemoveUserFromSpaceRequest.validate(){
    val errMessageList = mutableListOf<String>()
    if (this.spaceId == 0L)
        errMessageList.add("spaceId can't be null. ")
    if (this.userId.isEmpty())
        errMessageList.add("userId can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun CreateSpaceRequest.validate() {
    val errMessageList = mutableListOf<String>()
    if (this.spaceId == 0L)
        errMessageList.add("spaceId can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}

fun AuthorizeRequest.validate() {
    val errMessageList = mutableListOf<String>()
    if (this.kind.isEmpty())
        errMessageList.add("kind can't empty. ")
    if (this.action.isEmpty())
        errMessageList.add("action can't be empty. ")
    if (this.resourceId == 0L)
        errMessageList.add("resource_id can't be null. ")

    if (errMessageList.isNotEmpty())
        throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT, errMessageList.toString())
}