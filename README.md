# Rently API

API REST para sistema de aluguel de veículos, desenvolvida com Spring Boot 3 e Java 21.

## Tecnologias

- **Java 21**
- **Spring Boot 3.5**
- **Spring Security + JWT** (autenticação stateless)
- **Spring Data JPA + H2** (banco em memória)
- **Caffeine Cache** (TTL de 60s)
- **Lombok**
- **Springdoc OpenAPI 3** (Swagger UI)
- **Docker**

---

## Pré-requisitos

| Opção | Requisitos |
|---|---|
| Local | Java 21, Maven 3.9+ |
| Docker | Docker e Docker Compose |

---

## Executando o projeto

### Com Docker (recomendado)

```bash
docker compose up --build
```

### Local

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080/api`.

---

## Documentação interativa

Após subir a aplicação, acesse:

- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/api-docs`
- **H2 Console:** `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:rentlydb`
  - Usuário: `sa` | Senha: _(vazio)_

---

## Autenticação

A API utiliza **JWT Bearer Token**. O fluxo é:

1. Criar um usuário via `POST /api/auth/register`
2. Fazer login via `POST /api/auth/login`
3. Usar o token retornado no header `Authorization: Bearer <token>`

### Roles disponíveis

| Role | Permissões |
|---|---|
| `ADMIN` | Acesso total — gerenciar veículos, clientes e aluguéis |
| `USER` | Visualizar veículos, criar/cancelar os próprios aluguéis e consultar/editar o próprio perfil |

### Claims do JWT

O token gerado contém os seguintes claims:

| Claim | Tipo | Presente em |
|---|---|---|
| `sub` | String (email) | ADMIN e USER |
| `role` | String (`ADMIN` ou `USER`) | ADMIN e USER |
| `name` | String | ADMIN e USER |
| `customerId` | Long | Apenas USER |

---

## Endpoints

### Auth — `/api/auth`

#### Registrar administrador
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@rently.com",
    "password": "123456",
    "name": "Admin Rently",
    "role": "ADMIN"
  }'
```

#### Registrar cliente (USER)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "123456",
    "name": "João Silva",
    "cpf": "123.456.789-00",
    "phone": "11999999999"
  }'
```

> O campo `role` é opcional no registro de clientes — quando omitido, assume `USER` por padrão.
> Para `USER`, os campos `name`, `cpf` e `phone` são obrigatórios.
> Para `ADMIN`, apenas `name` é obrigatório além de `email` e `password`.

**Resposta 201:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@rently.com",
    "password": "123456"
  }'
```

**Resposta 200:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

### Veículos — `/api/vehicles`

> Rotas de escrita exigem role `ADMIN`. Rotas de leitura exigem `ADMIN` ou `USER`.

#### Cadastrar veículo
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "brand": "Toyota",
    "model": "Corolla",
    "plate": "ABC-1234",
    "year": 2022,
    "dailyRate": 150.00
  }'
```

#### Listar veículos
```bash
curl http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer <token>"
```

#### Buscar por ID
```bash
curl http://localhost:8080/api/vehicles/1 \
  -H "Authorization: Bearer <token>"
```

#### Verificar disponibilidade
```bash
curl "http://localhost:8080/api/vehicles/available?startDate=2026-05-01&endDate=2026-05-05" \
  -H "Authorization: Bearer <token>"
```

#### Atualizar veículo
```bash
curl -X PUT http://localhost:8080/api/vehicles/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "brand": "Toyota",
    "model": "Corolla Cross",
    "plate": "ABC-1234",
    "year": 2023,
    "dailyRate": 180.00
  }'
```

#### Remover veículo
```bash
curl -X DELETE http://localhost:8080/api/vehicles/1 \
  -H "Authorization: Bearer <token>"
```

---

### Clientes — `/api/customers`

> Endpoints administrativos exigem role `ADMIN`. Os endpoints `/me` aceitam `ADMIN` ou `USER`.

#### Cadastrar cliente (ADMIN)
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpf": "123.456.789-00",
    "email": "joao@email.com",
    "phone": "11999999999"
  }'
```

#### Listar clientes (ADMIN)
```bash
curl http://localhost:8080/api/customers \
  -H "Authorization: Bearer <token>"
```

#### Buscar por ID (ADMIN)
```bash
curl http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer <token>"
```

#### Atualizar cliente por ID (ADMIN)
```bash
curl -X PUT http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva Santos",
    "cpf": "123.456.789-00",
    "email": "joao@email.com",
    "phone": "11988888888"
  }'
```

#### Remover cliente (ADMIN)
```bash
curl -X DELETE http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer <token>"
```

#### Consultar próprio perfil (USER/ADMIN)
```bash
curl http://localhost:8080/api/customers/me \
  -H "Authorization: Bearer <token>"
```

#### Atualizar próprio perfil (USER/ADMIN)
```bash
curl -X PUT http://localhost:8080/api/customers/me \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva Santos",
    "cpf": "123.456.789-00",
    "email": "ignorado",
    "phone": "11988888888"
  }'
```

> O campo `email` no body é ignorado no `PUT /me` — o email é o identificador de login e não pode ser alterado.

---

### Aluguéis — `/api/rentals`

> Todos os endpoints exigem `ADMIN` ou `USER`. A filtragem por cliente é feita automaticamente no backend com base no token.

#### Registrar aluguel

**Como ADMIN** (deve informar `customerId`):
```bash
curl -X POST http://localhost:8080/api/rentals \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "vehicleId": 1,
    "startDate": "2026-05-01",
    "endDate": "2026-05-04"
  }'
```

**Como USER** (`customerId` é ignorado — derivado do token):
```bash
curl -X POST http://localhost:8080/api/rentals \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "startDate": "2026-05-01",
    "endDate": "2026-05-04"
  }'
```

**Resposta 201:**
```json
{
  "id": 1,
  "vehicle": {
    "id": 1,
    "brand": "Toyota",
    "model": "Corolla",
    "plate": "ABC-1234",
    "year": 2022,
    "dailyRate": 150.00
  },
  "customer": {
    "id": 1,
    "name": "João Silva",
    "cpf": "123.456.789-00",
    "email": "joao@email.com",
    "phone": "11999999999"
  },
  "startDate": "2026-05-01",
  "endDate": "2026-05-04",
  "totalValue": 450.00,
  "status": "ACTIVE"
}
```

#### Listar aluguéis
```bash
curl http://localhost:8080/api/rentals \
  -H "Authorization: Bearer <token>"
```

> `ADMIN` recebe todos os aluguéis. `USER` recebe apenas os próprios.

#### Buscar por ID
```bash
curl http://localhost:8080/api/rentals/1 \
  -H "Authorization: Bearer <token>"
```

#### Cancelar aluguel
```bash
curl -X PATCH http://localhost:8080/api/rentals/1/cancel \
  -H "Authorization: Bearer <token>"
```

> `USER` só pode cancelar aluguéis do próprio customer. Tentar cancelar de outro cliente retorna `422`.

---

## Regras de negócio

### Cálculo do valor total
O valor é calculado automaticamente ao registrar o aluguel:

```
totalValue = dailyRate × número de dias
```

Exemplo: diária de R$ 150,00 por 3 dias = **R$ 450,00**

### Disponibilidade
- Um veículo não pode ser alugado em período que já possua um aluguel `ACTIVE`
- A validação verifica sobreposição: `startDate < endDateOutro AND endDate > startDateOutro`
- Aluguéis com status `CANCELLED` não bloqueiam o período

### Validações gerais
- `endDate` deve ser posterior a `startDate`
- `startDate` não pode ser no passado
- Email de usuário deve ser único
- `USER` só pode cancelar os próprios aluguéis

---

## Permissões por endpoint

| Endpoint | ADMIN | USER |
|---|---|---|
| `POST /vehicles` | ✅ | ❌ |
| `GET /vehicles` | ✅ | ✅ |
| `GET /vehicles/available` | ✅ | ✅ |
| `GET /vehicles/{id}` | ✅ | ✅ |
| `PUT /vehicles/{id}` | ✅ | ❌ |
| `DELETE /vehicles/{id}` | ✅ | ❌ |
| `POST /customers` | ✅ | ❌ |
| `GET /customers` | ✅ | ❌ |
| `GET /customers/{id}` | ✅ | ❌ |
| `PUT /customers/{id}` | ✅ | ❌ |
| `DELETE /customers/{id}` | ✅ | ❌ |
| `GET /customers/me` | ✅ | ✅ |
| `PUT /customers/me` | ✅ | ✅ |
| `POST /rentals` | ✅ | ✅ |
| `GET /rentals` | ✅ (todos) | ✅ (próprios) |
| `GET /rentals/{id}` | ✅ | ✅ |
| `PATCH /rentals/{id}/cancel` | ✅ (qualquer) | ✅ (próprios) |

---

## Respostas de erro

Todos os erros seguem o padrão:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Data de término deve ser posterior à data de início",
  "path": "/api/rentals",
  "timestamp": "2026-04-13T10:30:00"
}
```

| Status | Situação |
|---|---|
| `400` | Erro de validação (campo obrigatório, formato inválido) |
| `401` | Token ausente, expirado ou credenciais inválidas |
| `403` | Sem permissão para o recurso |
| `404` | Recurso não encontrado |
| `409` | Email já cadastrado |
| `422` | Violação de regra de negócio |
| `500` | Erro interno do servidor |

---

## Estrutura do projeto

```
src/main/java/com/rently/api/
├── config/         # SecurityConfig, CacheConfig
├── controller/     # AuthController, VehicleController, CustomerController, RentalController
├── domain/         # Entidades JPA: Vehicle, Customer, Rental, User, Role, RentalStatus
├── dto/
│   ├── auth/       # AuthRequestDTO, RegisterRequestDTO, AuthResponseDTO
│   ├── vehicle/    # VehicleRequestDTO, VehicleResponseDTO
│   ├── customer/   # CustomerRequestDTO, CustomerResponseDTO
│   └── rental/     # RentalRequestDTO, RentalResponseDTO
├── exception/      # GlobalExceptionHandler, ErrorResponse, exceções customizadas
├── repository/     # Interfaces JPA
├── security/       # JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
└── service/        # AuthService, VehicleService, CustomerService, RentalService
```

---

## Executando os testes

```bash
./mvnw test
```
