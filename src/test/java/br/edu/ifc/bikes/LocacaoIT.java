package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.DevolucaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUtils;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
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

    @Test //TESTA PARA O CASO DE SUCESSO - DEVOLUÇÃO APÓS 90 MINUTOS COBRA 2 HORAS E LIBERA A BICICLETA
    public void devolverBicicleta_DadosValidos_RetornaLocacaoFinalizadaStatus200(){
        LocacaoResponseDTO responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("FINALIZADA");
        org.assertj.core.api.Assertions.assertThat(responseBody.dataDevolucao()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.valor()).isEqualByComparingTo("20.00");
        org.assertj.core.api.Assertions.assertThat(responseBody.bicicleta().status()).isEqualTo("DISPONIVEL");

        BicicletaResponseDTO bicicleta = testClient
                .get()
                .uri("/api/v1/bicicletas/10")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(bicicleta).isNotNull();
        org.assertj.core.api.Assertions.assertThat(bicicleta.status()).isEqualTo("DISPONIVEL");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - DEVOLUÇÃO ANTES DE 1 HORA COBRA O VALOR MÍNIMO
    public void devolverBicicleta_MenosDeUmaHora_RetornaValorMinimoStatus200(){
        LocacaoResponseDTO responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/21/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "ana@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.valor()).isEqualByComparingTo("10.00");
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("FINALIZADA");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - CLIENTE RELATA PROBLEMA E A BICICLETA VAI PARA MANUTENÇÃO
    public void devolverBicicleta_ComProblema_RetornaBicicletaManutencaoStatus200(){
        LocacaoResponseDTO responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(true))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo("FINALIZADA");
        org.assertj.core.api.Assertions.assertThat(responseBody.bicicleta().status()).isEqualTo("MANUTENCAO");
    }

    @Test //TESTA PARA O CASO DE ERRO - LOCAÇÃO JÁ FINALIZADA
    public void devolverBicicleta_LocacaoFinalizada_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/22/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //TESTA PARA O CASO DE ERRO - LOCAÇÃO NÃO ENCONTRADA PARA O CLIENTE
    //FORAM REALIZADOS 02 TESTES - LOCAÇÃO DE OUTRO CLIENTE E LOCAÇÃO INEXISTENTE
    public void devolverBicicleta_LocacaoDeOutroCliente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "ana@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);

        responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/99/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "ana@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test //TESTA PARA O CASO DE DADOS INVÁLIDOS - CAMPO POSSUIPROBLEMA NÃO INFORMADO
    public void devolverBicicleta_DadosInvalidos_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(null))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
        org.assertj.core.api.Assertions.assertThat(responseBody.getErrors()).containsKey("possuiProblema");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - ADMIN GERA O RELATÓRIO COM TODAS AS LOCAÇÕES
    public void gerarRelatorio_UsuarioAdmin_RetornaPdfComTodasStatus200() throws Exception {
        byte[] responseBody = testClient
                .get()
                .uri("/api/v1/locacoes/relatorio")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotEmpty();
        String texto = new PdfTextExtractor(new PdfReader(responseBody)).getTextFromPage(1);
        org.assertj.core.api.Assertions.assertThat(texto).contains("Todas as locações", "BK-001", "BK-005", "BK-002", "EM ANDAMENTO", "FINALIZADA");
    }

    @Test //TESTA PARA O CASO DE SUCESSO - FILTRO APENAS ATIVAS NÃO MOSTRA AS FINALIZADAS
    public void gerarRelatorio_ApenasAtivas_RetornaPdfSemFinalizadasStatus200() throws Exception {
        byte[] responseBody = testClient
                .get()
                .uri("/api/v1/locacoes/relatorio?apenasAtivas=true")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotEmpty();
        String texto = new PdfTextExtractor(new PdfReader(responseBody)).getTextFromPage(1);
        org.assertj.core.api.Assertions.assertThat(texto).contains("Locações em andamento", "BK-001", "BK-005");
        org.assertj.core.api.Assertions.assertThat(texto).doesNotContain("BK-002", "FINALIZADA");
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - CLIENTE NÃO GERA O RELATÓRIO
    public void gerarRelatorio_UsuarioCliente_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/locacoes/relatorio")
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test //TESTA PARA O CASO DE ACESSO NEGADO - ADMIN NÃO DEVOLVE BICICLETA
    public void devolverBicicleta_UsuarioAdmin_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtUtils.JWT_AUTHORIZATION, JwtAuthentication.getHeaderAuthorization(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }
}
