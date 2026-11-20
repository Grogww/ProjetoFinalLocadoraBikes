package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
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
@Sql(scripts = "/sql/clientes/clientes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clientes/clientes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ClienteIT {
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

    @Test //TESTA PARA O CASO DE SUCESSO - CLIENTE CRIADO PARA O USUÁRIO LOGADO
    public void criarCliente_DadosValidos_RetornaClienteCriadoStatus201(){
        ClienteResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("João Pereira", "39053344705", "49977770000", "joao@gmail.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.nome()).isEqualTo("João Pereira");
        org.assertj.core.api.Assertions.assertThat(responseBody.cpf()).isEqualTo("39053344705");
        org.assertj.core.api.Assertions.assertThat(responseBody.telefone()).isEqualTo("49977770000");
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo("joao@gmail.com");
    }

    @Test //TESTA PARA O CASO DE ERRO - CPF JÁ CADASTRADO PARA OUTRO CLIENTE
    public void criarCliente_CpfExistente_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("João Pereira", "11144477735", null, null))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //TESTA PARA O CASO DE ERRO - USUÁRIO QUE JÁ POSSUI CLIENTE
    public void criarCliente_UsuarioComCliente_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("Maria da Silva", "39053344705", null, null))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //TESTA PARA O CASO DE DADOS INVÁLIDOS
    //FORAM REALIZADOS 02 TESTES - NOME VAZIO E CPF COM DÍGITOS INVÁLIDOS
    public void criarCliente_DadosInvalidos_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("", "39053344705", null, null))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("João Pereira", "12345678900", null, null))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - ADMIN NÃO CADASTRA CLIENTE
    public void criarCliente_UsuarioAdmin_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new ClienteRequestDTO("José Admin", "39053344705", null, null))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test //TESTA PARA O CASO DE SUCESSO - ADMIN BUSCA CLIENTE EXISTENTE
    public void buscarCliente_IdExistente_RetornaClienteStatus200(){
        ClienteResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/clientes/10")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(responseBody.nome()).isEqualTo("Maria da Silva");
        org.assertj.core.api.Assertions.assertThat(responseBody.cpf()).isEqualTo("11144477735");
    }

    @Test //TESTA PARA O CASO DE ERRO - CLIENTE INEXISTENTE (ID 0)
    public void buscarCliente_IdInexistente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/0")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO BUSCA POR ID
    public void buscarCliente_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/10")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test //TESTA PARA O CASO DE SUCESSO - CLIENTE CONSULTA OS PRÓPRIOS DADOS
    public void buscarDetalhes_ClienteAutenticado_RetornaClienteStatus200(){
        ClienteResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(responseBody.cpf()).isEqualTo("11144477735");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - ADMIN LISTA TODOS OS CLIENTES
    public void listarClientes_UsuarioAdmin_RetornaClientesStatus200(){
        List<ClienteResponseDTO> responseBody = testClient
                .get()
                .uri("/api/v1/clientes")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.size()).isEqualTo(2);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO LISTA OS CLIENTES
    public void listarClientes_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }
}
