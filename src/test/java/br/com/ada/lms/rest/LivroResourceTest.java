package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class LivroResourceTest {

    private Integer criar(String caminho, String json) {
        return given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post(caminho)
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void cicloCompletoDoCrud() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Sapiens", "isbn": "978-85-254-6100-1", "anoPublicacao": 2011}
                        """)
                .when().post("/livros")
                .then()
                .statusCode(201)
                .extract().path("id");

        given().when().get("/livros").then().statusCode(200).body("titulo", hasItem("Sapiens"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Sapiens: Uma breve história da humanidade", "isbn": "978-85-254-6100-1", "anoPublicacao": 2011}
                        """)
                .when().put("/livros/" + id)
                .then()
                .statusCode(200)
                .body("titulo", equalTo("Sapiens: Uma breve história da humanidade"));

        given().when().delete("/livros/" + id).then().statusCode(204);
        given().when().get("/livros/" + id).then().statusCode(404);
    }

    @Test
    void putSemIsbnRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Dom Casmurro", "anoPublicacao": 1899}
                        """)
                .when().put("/livros/1")
                .then()
                .statusCode(400);
    }

    @Test
    void postComAnoInvalidoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Inválido", "isbn": "000-00-000-0000-0", "anoPublicacao": -1}
                        """)
                .when().post("/livros")
                .then()
                .statusCode(400);
    }

    @Test
    void isbnRepetidoRetorna409() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Outro Dom Casmurro", "isbn": "978-85-359-0277-3", "anoPublicacao": 1899}
                        """)
                .when().post("/livros")
                .then()
                .statusCode(409);
    }

    @Test
    void tituloMuitoCurtoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Ab", "isbn": "111-11-111-1111-1", "anoPublicacao": 2020}
                        """)
                .when().post("/livros")
                .then()
                .statusCode(400);
    }

    @Test
    void tituloMuitoLongoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "%s", "isbn": "222-22-222-2222-2", "anoPublicacao": 2020}
                        """.formatted("x".repeat(201)))
                .when().post("/livros")
                .then()
                .statusCode(400);
    }

    @Test
    void anoAcimaDoLimiteRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"titulo": "Do Futuro", "isbn": "333-33-333-3333-3", "anoPublicacao": 2101}
                        """)
                .when().post("/livros")
                .then()
                .statusCode(400);
    }

    @Test
    void definirERemoverGenero() {
        Integer livro = criar("/livros", """
                {"titulo": "Ensaio sobre a Cegueira", "isbn": "444-44-444-4444-4", "anoPublicacao": 1995}
                """);
        Integer genero = criar("/generos", """
                {"nome": "Distopia", "descricao": "Sociedades futuras e críticas"}
                """);

        given().when().put("/livros/" + livro + "/genero/" + genero)
                .then()
                .statusCode(200)
                .body("genero.id", equalTo(genero))
                .body("genero.nome", equalTo("Distopia"));

        // gênero usado por um livro não pode ser removido
        given().when().delete("/generos/" + genero).then().statusCode(409);

        given().when().delete("/livros/" + livro + "/genero").then().statusCode(204);
        given().when().get("/livros/" + livro).then().statusCode(200).body("genero", nullValue());

        given().when().delete("/generos/" + genero).then().statusCode(204);
        given().when().delete("/livros/" + livro).then().statusCode(204);
    }

    @Test
    void generoInexistenteRetorna404() {
        given().when().put("/livros/1/genero/9999").then().statusCode(404);
    }

    @Test
    void adicionarERemoverAutor() {
        Integer livro = criar("/livros", """
                {"titulo": "O Cortiço", "isbn": "555-55-555-5555-5", "anoPublicacao": 1890}
                """);
        Integer autor = criar("/autores", """
                {"nome": "Aluísio Azevedo", "email": "aluisio@literatura.com"}
                """);

        given().when().post("/livros/" + livro + "/autores/" + autor)
                .then()
                .statusCode(200)
                .body("autores.id", hasItem(autor));

        // adicionar de novo é conflito
        given().when().post("/livros/" + livro + "/autores/" + autor).then().statusCode(409);

        // autor associado a um livro não pode ser removido
        given().when().delete("/autores/" + autor).then().statusCode(409);

        given().when().delete("/livros/" + livro + "/autores/" + autor).then().statusCode(204);
        given().when().get("/livros/" + livro).then().statusCode(200).body("autores", empty());

        given().when().delete("/autores/" + autor).then().statusCode(204);
        given().when().delete("/livros/" + livro).then().statusCode(204);
    }

    @Test
    void autorInexistenteRetorna404() {
        given().when().post("/livros/1/autores/9999").then().statusCode(404);
    }
}
