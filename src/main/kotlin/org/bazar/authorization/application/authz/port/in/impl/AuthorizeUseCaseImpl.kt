package org.bazar.authorization.application.authz.port.`in`.impl

import org.bazar.authorization.application.authz.port.`in`.AuthorizeUseCase
import org.bazar.authorization.application.shared.AttributeExtractionContext
import org.bazar.authorization.application.shared.AttributeExtractor
import org.bazar.authorization.application.shared.port.out.AccessPolicyChecker
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.authz.AuthorizationCheck
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthorizeUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val accessPolicyChecker: AccessPolicyChecker,
    private val attributeExtractors: List<AttributeExtractor>
) : AuthorizeUseCase {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(check: AuthorizationCheck): Boolean {
        val finalCheck = suspendTransaction {
            val spaceUser = spaceUserRepositoryPort.findBySpaceIdAndUserId(check.spaceId, check.loggedInUserId)
                ?: throw DomainException(
                    DomainErrors.NO_SUCH_USER_IN_SPACE,
                    "spaceId: ${check.spaceId}, userId: ${check.loggedInUserId}"
                )

            val initialContext = buildAttributeExtractionContext(check, spaceUser)

            val finalContext = attributeExtractors
                .filter { it.isApplicable(initialContext) }
                .fold(initialContext) { context, extractor ->
                    extractor.extract(context)
                }

            check.copy(
                principalAttributes = finalContext.principalAttributes,
                resourceAttributes = finalContext.resourceAttributes
            )
        }

        logger.debug("Checking access for request: {}", finalCheck.toString())
        return accessPolicyChecker.checkAccess(finalCheck)
    }

    private fun buildAttributeExtractionContext(
        check: AuthorizationCheck,
        authenticatedUser: SpaceUser
    ) = AttributeExtractionContext(
        principalAttributes = check.principalAttributes.toMutableMap(),
        resourceAttributes = check.resourceAttributes.toMutableMap(),
        authenticatedUser = authenticatedUser,
        resource = check.resource,
        action = check.action,
        resourceId = check.resourceId
    )
}
