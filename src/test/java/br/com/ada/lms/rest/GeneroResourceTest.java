package br.com.ada.lms.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class GeneroResourceTest {

    @Test
    void cicloCompletoDoCrud() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Aventura", "descricao": "Histórias de exploração e descobertas"}
                        """)
                .when().post("/generos")
                .then()
                .statusCode(201)
                .extract().path("id");

        given().when().get("/generos").then().statusCode(200).body("nome", hasItem("Aventura"));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"descricao": "Jornadas, exploração e grandes descobertas"}
                        """)
                .when().patch("/generos/" + id)
                .then()
                .statusCode(200)
                .body("descricao", equalTo("Jornadas, exploração e grandes descobertas"))
                .body("nome", equalTo("Aventura"));

        given().when().delete("/generos/" + id).then().statusCode(204);
        given().when().get("/generos/" + id).then().statusCode(404);
    }

    @Test
    void descricaoLongaRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"nome\": \"Inválido\", \"descricao\": \"" + "a".repeat(501) + "\"}")
                .when().post("/generos")
                .then()
                .statusCode(400);
    }

    @Test
    void nomeRepetidoRetorna409() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Romance", "descricao": "Outro romance"}
                        """)
                .when().post("/generos")
                .then()
                .statusCode(409);
    }

    @Test
    void nomeMuitoCurtoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "Ab", "descricao": "Curto demais"}
                        """)
                .when().post("/generos")
                .then()
                .statusCode(400);
    }

    @Test
    void nomeMuitoLongoRetorna400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome": "%s", "descricao": "Longo demais"}
                        """.formatted("x".repeat(101)))
                .when().post("/generos")
                .then()
                .statusCode(400);
    }
}
