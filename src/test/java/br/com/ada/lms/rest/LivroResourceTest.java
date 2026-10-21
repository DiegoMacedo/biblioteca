package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class LivroResourceTest {

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
}
