# Biblioteca - Gerenciador de Acervo (Quarkus + REST)

Projeto didático: uma API REST para gerenciar uma biblioteca, com CRUD de **Autores**, **Livros** e **Gêneros**.

Os dados ficam **em memória** (um `Map` dentro de cada service) para o foco ficar em HTTP/REST.
Ao reiniciar a aplicação, tudo volta aos dados iniciais.

## Como rodar

```bash
./mvnw quarkus:dev      # modo dev com live reload
./mvnw test             # roda os testes
```

- API: http://localhost:8080/autores
- Swagger UI (testar pelo navegador): http://localhost:8080/q/swagger-ui
- Dev UI: http://localhost:8080/q/dev-ui
- Requisições prontas: `requisicoes.http` (IntelliJ ou VS Code REST Client)

## Endpoints

O mesmo contrato vale para `/autores`, `/livros` e `/generos`:

| Método | Caminho          | O que faz                                  | Sucesso                      | Erros possíveis      |
|--------|------------------|--------------------------------------------|------------------------------|----------------------|
| GET    | `/autores`       | lista todos                                | `200 OK`                     | -                    |
| GET    | `/autores/{id}`  | busca um                                   | `200 OK`                     | `404`                |
| POST   | `/autores`       | cria                                       | `201 Created` + `Location`   | `400`, `409`, `415`  |
| PUT    | `/autores/{id}`  | **substitui** o recurso (todos os campos)  | `200 OK`                     | `400`, `404`, `409`  |
| PATCH  | `/autores/{id}`  | altera **só os campos enviados**           | `200 OK`                     | `400`, `404`, `409`  |
| DELETE | `/autores/{id}`  | remove                                     | `204 No Content`             | `404`                |

### Corpo das requisições

```jsonc
// autor
{"nome": "Machado de Assis", "email": "machado@literatura.com"}
// livro
{"titulo": "Dom Casmurro", "isbn": "978-85-359-0277-3", "anoPublicacao": 1899}
// gênero
{"nome": "Romance", "descricao": "Narrativas ficcionais centradas em relações e conflitos humanos"}
```

### Códigos de status usados

| Código | Quando acontece                                                             |
|--------|-----------------------------------------------------------------------------|
| 200    | leitura ou atualização com sucesso (com corpo)                             |
| 201    | recurso criado; o header `Location` aponta para ele                         |
| 204    | remoção com sucesso (sem corpo)                                             |
| 400    | dados inválidos: campo obrigatório vazio, email inválido, ano ≤ 0           |
| 404    | o `id` não existe                                                           |
| 409    | conflito: email (autor), ISBN (livro) ou nome (gênero) já cadastrado        |
| 415    | `Content-Type` diferente de `application/json`                              |

Erros de negócio voltam como `{"status": 404, "erro": "Autor com id 9 não encontrado(a)."}`.
Erros de validação (`@Valid`) usam o formato padrão do Quarkus, com a lista `violations`.

## Estrutura

```
src/main/java/br/com/ada/lms
├── modelo/      Autor, Livro, Genero             -> entidades (o que é devolvido em JSON)
├── rest/        *Resource                        -> rotas HTTP (camada fina)
│   ├── dto/     *Request, ErroDTO                -> o que entra e o que sai em caso de erro
│   └── ApiExceptionMapper                        -> exceção -> resposta HTTP 4xx
├── servico/     *Service, ValidadorPatch         -> regras de negócio + armazenamento em memória
└── excecao/     ApiException e subclasses        -> 400, 404, 409
```

Fluxo de uma requisição: **Resource** → **Service** → (lança exceção?) → **ExceptionMapper** → resposta.

## Pontos para discutir em aula

1. **PUT x PATCH**: no PUT o corpo precisa estar completo (`@Valid`); no PATCH só os campos enviados são
   validados e alterados (`ValidadorPatch`).
2. **Por que o POST retorna `Response`** e os outros métodos retornam o objeto direto?
3. **Idempotência**: chamar o mesmo PUT/DELETE duas vezes deixa o servidor no mesmo estado. E o POST?
4. **Por que o `id` não vai no corpo** do POST/PUT?
5. **400 x 409 x 404**: quem está errado em cada caso — o formato dos dados, o estado do servidor ou o endereço?

## Exercícios sugeridos

- Filtro por query param: `GET /livros?titulo=sapiens`.
- Associar um autor a um livro (`autorId` em `Livro`) e retornar `400`/`404` se o autor não existir.
- Associar um gênero a um livro (`generoId` em `Livro`).
- Impedir remover um autor que possui livros cadastrados (`409`).
