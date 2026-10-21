package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Teste de integração: sobe a aplicação e faz requisições HTTP de verdade,
 * conferindo o status (2xx/4xx) e o JSON de resposta.
 */
@QuarkusTest
class AutorResourceTest {

    private Integer criarAutor(String nome, String email) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "%s", "email": "%s"}
                        """.formatted(nome, email))
                .when().post("/autores")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void getListaRetorna200() {
        given()
                .when().get("/autores")
                .then()
                .statusCode(200)
                .body("email", hasItem("machado@literatura.com"));
    }

    @Test
    void getPorIdInexistenteRetorna404() {
        given()
                .when().get("/autores/9999")
                .then()
                .statusCode(404)
                .body("erro", containsString("9999"));
    }

    @Test
    void postRetorna201ComLocation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Carlos Drummond", "email": "carlos@literatura.com"}
                        """)
                .when().post("/autores")
                .then()
                .statusCode(201)
                .header("Location", containsString("/autores/"))
                .body("id", notNullValue())
                .body("nome", equalTo("Carlos Drummond"));
    }

    @Test
    void postInvalidoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "", "email": "nao-e-email"}
                        """)
                .when().post("/autores")
                .then()
                .statusCode(400);
    }

    @Test
    void postSemCorpoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/autores")
                .then()
                .statusCode(400);
    }

    @Test
    void postComEmailRepetidoRetorna409() {
        criarAutor("Duplicado", "duplicado@literatura.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Outro", "email": "duplicado@literatura.com"}
                        """)
                .when().post("/autores")
                .then()
                .statusCode(409);
    }

    @Test
    void postComContentTypeErradoRetorna415() {
        given()
                .contentType(ContentType.TEXT)
                .body("nome=Ana")
                .when().post("/autores")
                .then()
                .statusCode(415);
    }

    @Test
    void putSubstituiTodosOsCampos() {
        Integer id = criarAutor("Para PUT", "put@literatura.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Nome Novo", "email": "put-novo@literatura.com"}
                        """)
                .when().put("/autores/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Nome Novo"))
                .body("email", equalTo("put-novo@literatura.com"));
    }

    @Test
    void putIncompletoRetorna400() {
        Integer id = criarAutor("Put Incompleto", "put-incompleto@literatura.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Só o nome"}
                        """)
                .when().put("/autores/" + id)
                .then()
                .statusCode(400);
    }

    @Test
    void patchAlteraSoOsCamposEnviados() {
        Integer id = criarAutor("Para PATCH", "patch@literatura.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Só o nome mudou"}
                        """)
                .when().patch("/autores/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Só o nome mudou"))
                .body("email", equalTo("patch@literatura.com"));
    }

    @Test
    void patchComEmailInvalidoRetorna400() {
        Integer id = criarAutor("Patch Inválido", "patch-invalido@literatura.com");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email": "sem-arroba"}
                        """)
                .when().patch("/autores/" + id)
                .then()
                .statusCode(400)
                .body("erro", equalTo("email inválido"));
    }

    @Test
    void patchInexistenteRetorna404() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Ninguém"}
                        """)
                .when().patch("/autores/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteRetorna204EDepois404() {
        Integer id = criarAutor("Para DELETE", "delete@literatura.com");

        given().when().delete("/autores/" + id).then().statusCode(204);
        given().when().get("/autores/" + id).then().statusCode(404);
        given().when().delete("/autores/" + id).then().statusCode(404);
    }
}
