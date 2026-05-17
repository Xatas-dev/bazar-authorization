package org.bazar.authorization.service

import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.model.commands.AuthorizeCommand
import org.bazar.authorization.service.attribute_extractor.AttributeExtractionContext
import org.bazar.authorization.service.attribute_extractor.AttributeExtractor
import org.bazar.authorization.utils.extensions.builder.buildAuthorizationRequest
import java.util.*

class AuthorizationService(
    private val spaceUserService: SpaceUserService,
    private val cerbosAccessService: CerbosAccessService,
    private val attributeExtractors: List<AttributeExtractor>
) {

    /**
     * Main authorization method.
     * To check authorization :
     * 1. Check that specified action exist in DB (throw 404 if not)
     * 2. Find roleId from space_user by specified spaceId and userId (throw 401 if no such)
     * 3. Find role-action mapping from roles_actions with specified role_id and action_id (throw 401 if no such)
     * 4. Check authorization decision via cerbos
     */
    suspend fun authorize(command: AuthorizeCommand): Boolean {
        val spaceUserInDb = spaceUserService.getSpaceUser(command.spaceId, command.loggedInUserId)

        val initialAttributeExtractionContext = buildAttributeExtractionContext(command, spaceUserInDb)

        val finalContext = attributeExtractors
            .filter { it.isApplicable(initialAttributeExtractionContext) }
            .fold(initialAttributeExtractionContext) { context, extractor ->
                extractor.extract(context)
            }

        val authzRequest =
            buildAuthorizationRequest(
                spaceUserInDb,
                command.resource,
                command.action,
                finalContext.principalAttributes,
                finalContext.resourceAttributes
            )

        return cerbosAccessService.checkAccess(authzRequest)

    }

    suspend fun checkIfUserInSpace(userId: UUID, spaceId: Long) {
        spaceUserService.getSpaceUser(spaceId, userId)
    }

    private fun buildAttributeExtractionContext(
        authorizeCommand: AuthorizeCommand,
        authenticatedUser: SpaceUserEntity
    ) =
        AttributeExtractionContext(
            authorizeCommand.principalAttributes.toMutableMap(),
            authorizeCommand.resourceAttributes.toMutableMap(),
            authenticatedUser,
            authorizeCommand.resource,
            authorizeCommand.action,
            authorizeCommand.resourceId
        )


}