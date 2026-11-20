package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUtils;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/bicicletas/bicicletas-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/bicicletas/bicicletas-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BicicletaIT {
    @LocalServerPort
    int port;

    WebTestClient testClient;

    @BeforeEach
    void setUp(){
        this.testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test //TESTA PARA O CASO DE SUCESSO - BICICLETA CRIADA COMO DISPONÍVEL
    public void criarBicicleta_DadosValidos_RetornaBicicletaCriadaStatus201(){
        BicicletaResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaRequestDTO("Oggi Big Wheel", "BK-003"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.modelo()).isEqualTo("Oggi Big Wheel");
        org.assertj.core.api.Assertions.assertThat(responseBody.codigo()).isEqualTo("BK-003");
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("DISPONIVEL");
    }

    @Test //TESTA PARA O CASO DE ERRO - CÓDIGO JÁ CADASTRADO
    public void criarBicicleta_CodigoExistente_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaRequestDTO("Oggi Big Wheel", "BK-001"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //TESTA PARA O CASO DE DADOS INVÁLIDOS
    //FORAM REALIZADOS 02 TESTES - MODELO VAZIO E CÓDIGO CURTO
    public void criarBicicleta_DadosInvalidos_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaRequestDTO("", "BK-003"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaRequestDTO("Oggi Big Wheel", "BK"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO CADASTRA BICICLETA
    public void criarBicicleta_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new BicicletaRequestDTO("Oggi Big Wheel", "BK-003"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test //TESTA PARA O CASO DE SUCESSO - BICICLETA LOCALIZADA PELO CÓDIGO
    public void buscarBicicleta_CodigoExistente_RetornaBicicletaStatus200(){
        BicicletaResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/codigo/BK-002")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(11);
        org.assertj.core.api.Assertions.assertThat(responseBody.modelo()).isEqualTo("Specialized Rockhopper");
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("ALUGADA");
    }

    @Test //TESTA PARA O CASO DE ERRO - CÓDIGO INEXISTENTE
    public void buscarBicicleta_CodigoInexistente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/codigo/BK-999")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO BUSCA BICICLETA
    public void buscarBicicleta_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/codigo/BK-001")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test //TESTA PARA O CASO DE SUCESSO - BICICLETA LOCALIZADA PELO ID
    public void buscarBicicleta_IdExistente_RetornaBicicletaStatus200(){
        BicicletaResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/10")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.codigo()).isEqualTo("BK-001");
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("DISPONIVEL");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - ADMIN LISTA TODAS AS BICICLETAS
    public void listarBicicletas_UsuarioAdmin_RetornaBicicletasStatus200(){
        List<BicicletaResponseDTO> responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.size()).isEqualTo(2);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO LISTA AS BICICLETAS
    public void listarBicicletas_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }
}
