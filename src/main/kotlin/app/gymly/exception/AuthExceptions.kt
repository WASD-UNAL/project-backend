package app.gymly.exception

class InvalidCredentialsException(message: String = "Invalid credentials") : RuntimeException(message)

class EmailAlreadyExistsException(email: String) : RuntimeException("Email '$email' is already registered")

class DocumentAlreadyExistsException(document: String) : RuntimeException("Document '$document' is already registered")

class RoleNotConfiguredException(roleName: String) : IllegalStateException("Role '$roleName' is not seeded in the database")
