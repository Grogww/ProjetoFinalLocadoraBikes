package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioLoginDto;
import br.edu.ifc.bikes.jwt.JwtToken;
import br.edu.ifc.bikes.jwt.JwtUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public class JwtAuthentication {

    //faz o login no /auth e devolve o token pronto para o header Authorization
    public static String getHeaderAuthorization(WebTestClient client, String username, String password){
        String token = client
                .post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioLoginDto(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult().getResponseBody().getToken();

        return JwtUtils.JWT_BEARER + token;
    }
}
