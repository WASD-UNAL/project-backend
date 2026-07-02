package app.gymly.service.client

import app.gymly.dto.client.ClientResponse
import app.gymly.dto.client.ClientUpdateRequest
import app.gymly.model.User
import app.gymly.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClientManagementService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun searchClients(query: String?): List<ClientResponse> {
        val users =
            if (query.isNullOrBlank()) {
                userRepository.findAll()
            } else {
                userRepository.searchByQuery(query.trim())
            }
        return users.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getClient(id: Int): ClientResponse? = userRepository.findById(id).map { it.toResponse() }.orElse(null)

    @Transactional
    fun updateClient(
        id: Int,
        request: ClientUpdateRequest,
    ): ClientResponse? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        request.name?.let { user.name = it }
        request.lastname?.let { user.lastname = it }
        request.email?.let { user.email = it }
        request.document?.let { user.document = it.trim() }
        request.phone?.let { user.phone = it }
        request.weight?.let { user.weight = it }
        request.height?.let { user.height = it }
        request.active?.let { user.active = it }
        val saved = userRepository.save(user)
        return saved.toResponse()
    }

    private fun User.toResponse() =
        ClientResponse(
            id = id ?: error("User id is null"),
            name = name,
            lastname = lastname,
            email = email,
            document = document,
            phone = phone,
            weight = weight,
            height = height,
            active = active,
            createdAt = createdAt,
        )
}
