package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUtils;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/locacoes/locacoes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/locacoes/locacoes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LocacaoIT {
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

    @Test //TESTA PARA O CASO DE SUCESSO - CLIENTE ALUGA A PRIMEIRA BICICLETA DISPONÍVEL
    public void alugarBicicleta_DadosValidos_RetornaLocacaoCriadaStatus201(){
        LocacaoResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 2))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.cliente().id()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(responseBody.bicicleta().codigo()).isEqualTo("BK-003");
        org.assertj.core.api.Assertions.assertThat(responseBody.bicicleta().status()).isEqualTo("ALUGADA");
        org.assertj.core.api.Assertions.assertThat(responseBody.dataRetirada()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.dataPrevistaDevolucao()).isEqualTo(responseBody.dataRetirada().plusHours(2));
        org.assertj.core.api.Assertions.assertThat(responseBody.dataDevolucao()).isNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.valor()).isNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.finalCartao()).isEqualTo("1111");
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("ATIVA");

        BicicletaResponseDTO bicicleta = testClient
                .get()
                .uri("/api/v1/bicicletas/12")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(bicicleta).isNotNull();
        org.assertj.core.api.Assertions.assertThat(bicicleta.status()).isEqualTo("ALUGADA");
    }

    @Test //TESTA PARA O CASO DE ERRO - NENHUMA BICICLETA DISPONÍVEL
    //FORAM REALIZADOS 03 TESTES - A MARIA ALUGA AS DUAS DISPONÍVEIS E A ANA NÃO ENCONTRA BICICLETA
    public void alugarBicicleta_SemBicicletaDisponivel_RetornaErrorMessageStatus409(){
        LocacaoResponseDTO locacao = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 1))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(locacao).isNotNull();
        org.assertj.core.api.Assertions.assertThat(locacao.bicicleta().codigo()).isEqualTo("BK-003");

        locacao = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 1))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(locacao).isNotNull();
        org.assertj.core.api.Assertions.assertThat(locacao.bicicleta().codigo()).isEqualTo("BK-004");

        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "ana@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 1))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //TESTA PARA O CASO DE DADOS INVÁLIDOS
    //FORAM REALIZADOS 02 TESTES - TODOS OS CAMPOS FORA DO FORMATO E TODOS OS CAMPOS VAZIOS
    public void alugarBicicleta_DadosInvalidos_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111112", "13/99", "12", 25))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        org.assertj.core.api.Assertions.assertThat(responseBody.getErrors()).containsKeys("numeroCartao", "validadeCartao", "cvv", "horas");

        responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("", "", "", null))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        org.assertj.core.api.Assertions.assertThat(responseBody.getErrors()).containsKeys("numeroCartao", "validadeCartao", "cvv", "horas");
    }

    @Test //TESTA PARA O CASO DE ERRO - CARTÃO COM VALIDADE VENCIDA
    public void alugarBicicleta_CartaoVencido_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "01/20", "123", 1))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test //TESTA PARA O CASO DE ERRO - USUÁRIO SEM CADASTRO DE CLIENTE
    public void alugarBicicleta_UsuarioSemCliente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 1))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - ADMIN NÃO ALUGA BICICLETA
    public void alugarBicicleta_UsuarioAdmin_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new LocacaoRequestDTO("4111111111111111", "12/99", "123", 1))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }
}
