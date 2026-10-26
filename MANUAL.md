# Manual do Projeto Biblioteca

Manual técnico separado do `README.md`. Explica de forma direta o papel de cada arquivo, classe e método.

## 1. Visão geral

A aplicação é uma API REST em Quarkus para gerenciar autores, gêneros e livros. Os dados são gravados no PostgreSQL.

Cada livro tem um gênero principal (`@ManyToOne`) e uma lista de autores (`@ManyToMany`). As associações são alteradas por endpoints próprios (`/livros/{id}/genero/...` e `/livros/{id}/autores/...`), e não pelo corpo do `LivroRequest`.

Fluxo de uma requisição:

```
HTTP -> Resource -> Service -> Repository -> PostgreSQL
                    |            |
                    |            +-- Panache/Hibernate: INSERT, SELECT, UPDATE, DELETE
                    +-- regras de negócio + transações

Erro de negócio -> ApiException -> ApiExceptionMapper -> JSON {"status": ..., "erro": ...}
```

Responsabilidades:

| Camada | Pacote | Papel |
|---|---|---|
| Resource | `rest` | Recebe HTTP, valida entrada e devolve status/corpo. |
| DTO | `rest.dto` | Define o formato do JSON de entrada e de erro. |
| Service | `servico` | Aplica regras de negócio e controla transações. |
| Repository | `repositorio` | Executa as operações no banco via Panache. |
| Model | `modelo` | Entidades JPA que viram tabelas e JSON de resposta. |
| Exception | `excecao` | Representa erros com o status HTTP correspondente. |

## 2. Como executar

```bash
docker compose up -d     # sobe o PostgreSQL definido em docker-compose.yml
./mvnw quarkus:dev       # aplicação em modo dev: http://localhost:8080
./mvnw test              # testes; sobe um PostgreSQL descartável via Dev Services
./mvnw package           # gera o jar em target/quarkus-app
java -jar target/quarkus-app/quarkus-run.jar
```

Endereços úteis:

- API: `http://localhost:8080/autores`, `/generos`, `/livros`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI: `http://localhost:8080/q/dev-ui`

## 3. Configuração

### `pom.xml`

Define a versão do Quarkus (`3.40.1`), Java (`release 25`) e as dependências:

| Dependência | Para que serve |
|---|---|
| `quarkus-rest-jackson` | Cria endpoints REST e converte objetos em JSON. |
| `quarkus-arc` | Injeção de dependência (CDI). |
| `quarkus-hibernate-validator` | Valida `@NotBlank`, `@Email`, `@Positive` etc. e retorna 400. |
| `quarkus-smallrye-openapi` | Gera o OpenAPI e o Swagger UI. |
| `quarkus-hibernate-orm-panache` | JPA/Hibernate + repositórios prontos do Panache. |
| `quarkus-jdbc-postgresql` | Driver de conexão com o PostgreSQL. |
| `quarkus-junit` / `rest-assured` | Testes de integração HTTP. |

### `docker-compose.yml`

Sobe um PostgreSQL 17 com:

- banco: `biblioteca`
- usuário: `biblioteca`
- senha: `biblioteca`
- porta: `5432`
- volume `biblioteca-dados`: mantém os dados mesmo após `docker compose down`

### `application.properties`

| Propriedade | Efeito |
|---|---|
| `quarkus.datasource.db-kind=postgresql` | Diz que o banco é PostgreSQL. |
| `%dev,prod.quarkus.datasource.username/password` | Credenciais em dev e produção. |
| `%dev,prod.quarkus.datasource.jdbc.url` | URL `jdbc:postgresql://localhost:5432/biblioteca`. |
| `%dev.quarkus.datasource.devservices.enabled=false` | Em dev, usa o banco do Docker Compose em vez de subir outro. |
| `quarkus.hibernate-orm.schema-management.strategy=drop-and-create` | Apaga e recria as tabelas a cada inicialização. |
| `quarkus.hibernate-orm.sql-load-script=import.sql` | Executa o script de dados iniciais após criar as tabelas. |
| `quarkus.hibernate-orm.log.sql=true` | Mostra os SQLs gerados no console. |

Em testes não há URL configurada, então o Quarkus inicia um PostgreSQL temporário via Dev Services (Docker). Assim os testes não apagam os dados do banco de desenvolvimento.

### `src/main/resources/import.sql`

Roda logo depois da criação das tabelas. Insere:

- 2 autores: Machado de Assis e Clarice Lispector
- 2 gêneros: Romance e Ficção científica
- 2 livros: Dom Casmurro e A Hora da Estrela
- associações: cada livro recebe um gênero na coluna `genero_id` e um autor na tabela `livro_autor`

## 4. Pacote `modelo`

As três classes são entidades JPA: cada uma representa uma tabela. O Jackson gera o JSON usando os getters. O construtor vazio `protected` existe porque o Hibernate precisa criar o objeto antes de preencher os campos.

O `Livro` é o centro dos relacionamentos:

- `Livro.genero`: `@ManyToOne`, pois vários livros podem ter o mesmo gênero.
- `Livro.autores`: `@ManyToMany`, pois um livro pode ter vários autores e um autor pode ter vários livros.

### `Autor`

Tabela `autor`.

| Campo | Regra |
|---|---|
| `id` | Chave primária gerada pelo banco (`IDENTITY`). |
| `nome` | Obrigatório, até 100 caracteres. |
| `email` | Obrigatório e único, até 150 caracteres. |

Métodos:

- `Autor()`: construtor vazio exigido pelo JPA.
- `Autor(String nome, String email)`: cria um autor novo sem `id`.
- `getId()`, `getNome()`, `getEmail()`: leitura usada pelo Jackson e pelo código.
- `setNome()`, `setEmail()`: alteram os campos. Como a entidade vem gerenciada do banco, o Hibernate detecta a mudança e gera o `UPDATE`.

### `Genero`

Tabela `genero`.

| Campo | Regra |
|---|---|
| `id` | Chave primária gerada pelo banco. |
| `nome` | Obrigatório e único, até 100 caracteres. |
| `descricao` | Opcional, até 500 caracteres. |

Métodos:

- `Genero()`: construtor vazio para o JPA.
- `Genero(String nome, String descricao)`: cria um gênero novo.
- Getters e setters: expõem e alteram os dados.

### `Livro`

Tabela `livro`.

| Campo | Regra |
|---|---|
| `id` | Chave primária gerada pelo banco. |
| `titulo` | Obrigatório, até 200 caracteres. |
| `isbn` | Obrigatório, único, até 20 caracteres. |
| `anoPublicacao` | Obrigatório; coluna `ano_publicacao`. |
| `genero` | `@ManyToOne`; coluna `genero_id` na tabela `livro`. Pode ser `null`. |
| `autores` | `@ManyToMany`; tabela de junção `livro_autor` (`livro_id`, `autor_id`). Inicia como lista vazia. |

Métodos:

- `Livro()`: construtor vazio para o JPA.
- `Livro(String titulo, String isbn, Integer anoPublicacao)`: cria um livro novo.
- Getters e setters: expõem e alteram os dados.
- `getGenero()` e `setGenero(Genero)`: leem e definem o gênero principal.
- `getAutores()`: devolve a lista de autores.
- `adicionarAutor(Autor)`: adiciona somente se o autor ainda não estiver na lista; o Hibernate grava a linha em `livro_autor` no commit.
- `removerAutor(Autor)`: remove o autor da lista; o Hibernate apaga a linha da tabela de junção.

O `@ManyToMany` usa `FetchType.EAGER` para que o Jackson consiga serializar os autores depois que a transação do service terminou. Sem isso, poderia ocorrer `LazyInitializationException`.

## 5. Pacote `repositorio`

Os repositórios implementam `PanacheRepository<T>`. Isso já fornece métodos prontos como `listAll`, `findById`, `findByIdOptional`, `persist`, `deleteById` e `count`.

### `AutorRepository`

- `emailEmUso(String email, Long idAtual)`: verifica se outro autor já usa o email, ignorando maiúsculas/minúsculas.
  - Se `idAtual` é `null` (cadastro), procura qualquer autor com o email.
  - Se `idAtual` tem valor (atualização), ignora o próprio autor na comparação.
  - Retorna `true` quando existe conflito.

### `GeneroRepository`

- `nomeEmUso(String nome, Long idAtual)`: mesma ideia, comparando o nome do gênero sem diferenciar maiúsculas de minúsculas.

### `LivroRepository`

- `isbnEmUso(String isbn, Long idAtual)`: mesma ideia, comparando o ISBN sem diferenciar maiúsculas de minúsculas.
- `existeComGenero(Long generoId)`: verifica se algum livro usa o gênero. Serve para impedir a remoção de um gênero ainda referenciado (409).
- `existeComAutor(Long autorId)`: percorre a tabela de junção `livro_autor` (`join l.autores a`) e verifica se algum livro tem o autor. Serve para impedir a remoção de um autor ainda associado (409).

Esses métodos usam HQL do Panache, por exemplo:

```
count("lower(email) = lower(?1) and id <> ?2", email, idAtual)
```

## 6. Pacote `servico`

Os services concentram as regras de negócio. Os métodos que gravam, alteram ou removem usam `@Transactional`: o Hibernate confirma tudo no final ou desfaz tudo se uma exceção for lançada. Não existe chamada explícita de `update`; o Hibernate percebe os setters na entidade gerenciada e gera o `UPDATE` no commit (*dirty checking*).

### `AutorService`

| Método | O que faz |
|---|---|
| `AutorService(AutorRepository, ValidadorPatch, LivroRepository)` | Recebe as dependências pelo construtor. |
| `listar()` | Busca todos os autores ordenados por `id`. |
| `buscar(Long id)` | Busca por id; se não achar, lança `RecursoNaoEncontradoException` (404). |
| `criar(AutorRequest)` | Verifica email duplicado, cria o `Autor` e faz `persist` (INSERT). |
| `atualizar(Long id, AutorRequest)` | PUT: busca o autor, verifica email, substitui nome e email. |
| `atualizarParcialmente(Long id, AutorRequest)` | PATCH: busca, valida só os campos enviados e altera apenas os que não são `null`. |
| `remover(Long id)` | Antes de remover, verifica se algum livro usa o autor; se sim, lança `ConflitoException` (409). Caso contrário, usa `deleteById`; se nada foi removido, lança 404. |
| `verificarEmailDisponivel(...)` | Regra privada que lança `ConflitoException` (409) quando o email já existe. |

Detalhe importante do PATCH: `buscar(id)` acontece antes da validação. Por isso, um id inexistente gera 404 mesmo que o corpo também esteja inválido.

### `GeneroService`

Mesmo desenho do `AutorService`, com estas diferenças:

| Método | O que faz |
|---|---|
| `listar()` | Lista gêneros por `id`. |
| `buscar(Long id)` | Busca o gênero ou lança 404. |
| `criar(GeneroRequest)` | Verifica nome duplicado, cria e persiste. |
| `atualizar(Long id, GeneroRequest)` | PUT: substitui nome e descrição. |
| `atualizarParcialmente(Long id, GeneroRequest)` | PATCH: altera somente nome e/ou descrição enviados. |
| `remover(Long id)` | Antes de remover, verifica se algum livro usa o gênero; se sim, lança 409. Caso contrário, remove ou lança 404. |
| `verificarNomeDisponivel(...)` | Lança 409 quando o nome do gênero já existe. |

### `LivroService`

Mesmo desenho, usando ISBN como chave de unicidade.

| Método | O que faz |
|---|---|
| `listar()` | Lista livros por `id`. |
| `buscar(Long id)` | Busca o livro ou lança 404. |
| `criar(LivroRequest)` | Verifica ISBN duplicado, cria e persiste. |
| `atualizar(Long id, LivroRequest)` | PUT: substitui título, ISBN e ano. |
| `atualizarParcialmente(Long id, LivroRequest)` | PATCH: altera apenas os campos enviados. |
| `remover(Long id)` | Remove ou lança 404. |
| `definirGenero(Long id, Long generoId)` | Busca o livro e o gênero (404 se algum não existir), define ou troca o gênero e deixa o Hibernate gerar o `UPDATE` de `genero_id`. |
| `removerGenero(Long id)` | Busca o livro e define `genero = null`. |
| `adicionarAutor(Long id, Long autorId)` | Busca livro e autor, verifica se o autor já está associado (409) e adiciona na lista. O Hibernate grava em `livro_autor`. |
| `removerAutor(Long id, Long autorId)` | Busca livro e autor e remove o autor da lista. Se não estava associado, não faz nada (idempotente). |
| `verificarIsbnDisponivel(...)` | Lança 409 quando o ISBN já existe. |

O `LivroService` depende de `AutorService` e `GeneroService` para aproveitar o `buscar(id)` deles e devolver 404 quando o autor ou gênero informado não existe.

### `ValidadorPatch`

- `ValidadorPatch(Validator validator)`: recebe o validador do Bean Validation.
- `validarCamposInformados(Object request)`: valida o DTO do PATCH.
  - Se o corpo for `null`, lança 400.
  - Roda as validações (`@Email`, `@Positive`, `@Size` etc.).
  - Ignora as violações de campos que não foram enviados (`valor == null`), pois no PATCH o campo ausente é permitido.
  - Junta as mensagens restantes e lança 400 se houver alguma.

## 7. Pacote `rest.dto`

DTOs não são entidades. Eles definem somente o JSON que entra e o JSON de erro que sai.

### `AutorRequest`

```json
{"nome": "Machado de Assis", "email": "machado@literatura.com"}
```

- `nome`: `@NotBlank` e `@Size(min = 3, max = 100)`.
- `email`: `@NotBlank`, `@Email` e `@Size(max = 150)`.
- Não possui `id`, porque o id é gerado pelo banco.

### `GeneroRequest`

```json
{"nome": "Romance", "descricao": "Narrativas ficcionais..."}
```

- `nome`: `@NotBlank` e `@Size(min = 3, max = 100)`.
- `descricao`: opcional, no máximo 500 caracteres (`@Size`).

### `LivroRequest`

```json
{"titulo": "Dom Casmurro", "isbn": "978-85-359-0277-3", "anoPublicacao": 1899}
```

- `titulo`: `@NotBlank` e `@Size(min = 3, max = 200)`.
- `isbn`: `@NotBlank`, máximo 20 caracteres.
- `anoPublicacao`: `@NotNull`, `@Positive` (maior que zero) e `@Max(2100)`.

As validações desses DTOs só rodam nos métodos anotados com `@Valid` (POST e PUT). No PATCH, quem valida é o `ValidadorPatch`.

### `ErroDTO`

Formato padrão de erro:

```json
{"status": 404, "erro": "Autor com id 9 não encontrado(a)."}
```

- `status`: código HTTP.
- `erro`: mensagem legível.

## 8. Pacote `excecao`

| Classe | Status | Quando é usada |
|---|---|---|
| `ApiException` | genérico | Classe base abstrata. Guarda o `Response.Status` e a mensagem. |
| `ConflitoException` | 409 | Dado repetido (email, nome de gênero ou ISBN) ou exclusão bloqueada por relacionamento (autor/gênero usado por livro). |
| `RecursoNaoEncontradoException` | 404 | O id informado não existe. |
| `RequisicaoInvalidaException` | 400 | Corpo inválido no PATCH ou corpo ausente. |

`ApiException.getStatus()` devolve o status HTTP. As subclasses só escolhem o status e passam a mensagem.

## 9. Pacote `rest`

### `ApiExceptionMapper`

Implementa `ExceptionMapper<ApiException>`.

- `toResponse(ApiException e)`: converte qualquer exceção de negócio em resposta JSON com o status correto e um `ErroDTO`. Assim os resources não precisam de `try/catch`.

### `AutorResource`

Rota base `/autores`. A classe usa `@Produces` e `@Consumes` com `application/json`.

| Método | Rota | O que faz | Respostas |
|---|---|---|---|
| `listar()` | `GET /autores` | Chama `service.listar()`. | 200 |
| `buscar(id)` | `GET /autores/{id}` | Chama `service.buscar(id)`. | 200, 404 |
| `criar(dados, uriInfo)` | `POST /autores` | Valida com `@Valid`, cria e monta o header `Location`. | 201, 400, 409, 415 |
| `atualizar(id, dados)` | `PUT /autores/{id}` | Exige todos os campos e substitui o recurso. | 200, 400, 404, 409 |
| `atualizarParcialmente(id, dados)` | `PATCH /autores/{id}` | Altera só os campos enviados. | 200, 400, 404, 409 |
| `remover(id)` | `DELETE /autores/{id}` | Remove e responde sem corpo. | 204, 404 |

O `POST` retorna `Response` porque precisa controlar o status 201 e o header `Location`. Os demais retornam o objeto ou um `Response` simples.

### `GeneroResource`

Mesmo contrato do `AutorResource`, aplicado a `/generos`:

- `listar()`: lista gêneros.
- `buscar(id)`: busca um gênero.
- `criar(dados, uriInfo)`: cria e devolve 201 + `Location`.
- `atualizar(id, dados)`: PUT com todos os campos.
- `atualizarParcialmente(id, dados)`: PATCH parcial.
- `remover(id)`: DELETE com 204.

### `LivroResource`

Mesmo contrato, aplicado a `/livros`:

- `listar()`: lista livros.
- `buscar(id)`: busca um livro.
- `criar(dados, uriInfo)`: cria e devolve 201 + `Location`.
- `atualizar(id, dados)`: PUT com todos os campos.
- `atualizarParcialmente(id, dados)`: PATCH parcial.
- `remover(id)`: DELETE com 204.
- `definirGenero(id, generoId)`: `PUT /livros/{id}/genero/{generoId}`; define ou troca o gênero.
- `removerGenero(id)`: `DELETE /livros/{id}/genero`; remove o gênero e responde 204.
- `adicionarAutor(id, autorId)`: `POST /livros/{id}/autores/{autorId}`; adiciona o autor (409 se já estiver associado).
- `removerAutor(id, autorId)`: `DELETE /livros/{id}/autores/{autorId}`; remove o autor e responde 204.

Os endpoints de relacionamento não têm corpo. Por isso usam `@Consumes(MediaType.WILDCARD)` no PUT/POST, aceitando requisições sem `Content-Type`. Caso contrário, o Quarkus poderia responder 415.

## 10. Relacionamentos em resumo

| Relacionamento | Anotação | Coluna/tabela | Endpoints | Regras |
|---|---|---|---|---|
| Livro -> Gênero | `@ManyToOne` | `livro.genero_id` | `PUT /livros/{id}/genero/{generoId}` e `DELETE /livros/{id}/genero` | 404 se livro/gênero não existir; gênero usado por livro não pode ser removido (409). |
| Livro <-> Autor | `@ManyToMany` | tabela `livro_autor` (`livro_id`, `autor_id`) | `POST /livros/{id}/autores/{autorId}` e `DELETE /livros/{id}/autores/{autorId}` | 404 se livro/autor não existir; adicionar duas vezes retorna 409; autor associado não pode ser removido (409). |

Como funciona a persistência:

- `setGenero(...)`: o Hibernate altera a coluna `genero_id` no `UPDATE` do livro.
- `adicionarAutor(...)`: o Hibernate insere uma linha em `livro_autor`.
- `removerAutor(...)`: o Hibernate apaga a linha de `livro_autor`.
- `getAutores()` precisa vir com `EAGER` para o JSON da resposta incluir os autores fora da transação.
- As verificações `existeComGenero` e `existeComAutor` evitam que o banco recuse um `DELETE` por chave estrangeira (o que viraria um 500).

## 11. Exemplos de fluxo

### POST com sucesso

1. `AutorResource.criar` recebe o JSON e o `@Valid` valida o `AutorRequest`.
2. `AutorService.criar` verifica se o email já existe.
3. Se estiver livre, cria o `Autor` e chama `repository.persist`.
4. O Hibernate executa o `INSERT` e devolve o id.
5. O resource monta `Location: /autores/{id}` e responde 201 com o autor em JSON.

### POST com email repetido

1. O resource recebe os dados válidos.
2. O service detecta o email em uso.
3. Lança `ConflitoException`.
4. `ApiExceptionMapper` transforma em 409 com `ErroDTO`.

### PATCH parcial

1. O resource recebe somente os campos a alterar.
2. O service busca a entidade no banco (404 se não existir).
3. `ValidadorPatch` valida apenas os campos enviados (400 se inválido).
4. Os setters alteram a entidade gerenciada.
5. No commit da transação, o Hibernate gera o `UPDATE`.

### DELETE

1. O service chama `repository.deleteById(id)`.
2. Se retornar `true`, o resource responde 204 sem corpo.
3. Se retornar `false`, o id não existia e o service lança 404.
4. No caso de autor ou gênero, antes do `deleteById` o service consulta o `LivroRepository`. Se houver livro usando o registro, lança 409 para evitar erro de chave estrangeira.

### Associar gênero a um livro

1. `PUT /livros/{livroId}/genero/{generoId}` chega ao `LivroResource.definirGenero`.
2. `LivroService.definirGenero` busca o livro e o gênero; qualquer id inexistente gera 404.
3. `livro.setGenero(genero)` altera a entidade gerenciada.
4. No commit, o Hibernate gera `UPDATE livro SET genero_id = ...`.

### Associar autor a um livro

1. `POST /livros/{livroId}/autores/{autorId}` chega ao `LivroResource.adicionarAutor`.
2. O service busca livro e autor; ids inexistentes geram 404.
3. Se o autor já está em `livro.getAutores()`, lança 409.
4. Caso contrário, `livro.adicionarAutor(autor)` atualiza a lista.
5. No commit, o Hibernate insere uma linha em `livro_autor`.

## 12. Testes

Os testes ficam em `src/test/java/br/com/ada/lms/rest`. Usam `@QuarkusTest` e REST Assured para fazer requisições HTTP reais.

- `AutorResourceTest`: cobre lista, busca por id, POST, validações de tamanho, conflito, content type, PUT, PATCH e DELETE.
- `GeneroResourceTest`: cobre o ciclo CRUD, descrição longa, nome repetido e limites de tamanho do nome.
- `LivroResourceTest`: cobre o ciclo CRUD, validações de tamanho/ano, ISBN repetido, definição/remoção de gênero, adição/remoção de autores, 404 em relacionamentos inexistentes e 409 ao remover autor/gênero ainda vinculado a livro.

Durante os testes, o Quarkus sobe um PostgreSQL descartável via Dev Services (Docker) e recria as tabelas com o `import.sql`.
