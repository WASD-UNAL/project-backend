package app.gymly.exception

class InvalidCredentialsException(
    message: String = "Invalid credentials",
) : RuntimeException(message)

class EmailAlreadyExistsException(
    email: String,
) : RuntimeException("El correo '$email' ya está registrado.")

class DocumentAlreadyExistsException(
    document: String,
) : RuntimeException("El documento '$document' ya está registrado.")

class RoleNotConfiguredException(
    roleName: String,
) : IllegalStateException("Role '$roleName' is not seeded in the database")
