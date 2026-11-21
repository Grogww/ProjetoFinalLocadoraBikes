# Bikes - Locadora de Bicicletas

API REST para o aluguel temporário de bicicletas. O cliente se cadastra, aluga uma bicicleta informando o cartão de crédito e devolve quando terminar; o sistema calcula o valor pelo tempo de uso. O administrador cadastra as bicicletas e gera o relatório de locações em PDF.

Projeto final da disciplina WS com Spring Boot (IFC - Campus Videira).

## Tecnologias
- Java 25 e Spring Boot 4.1.1 (Maven)
- Spring Security com JWT
- Spring Data JPA (Hibernate) com PostgreSQL; H2 nos testes
- Bean Validation, MapStruct e Lombok
- SpringDoc (Swagger)
- JasperReports 7.0.8

## Como executar
1. Criar o banco `Bikes` no PostgreSQL (`localhost:5432`, usuário `postgres`).
2. Criar o arquivo `.env` na raiz do projeto com a senha do banco (ele não vai para o git):
   ```
   DB_PASSWORD=sua_senha
   ```
3. Rodar a classe `BikesApplication`. As tabelas são criadas pelo Hibernate.
4. Documentação: http://localhost:8080/docs-bikes.html

Testes de integração:
```
./mvnw test -Dtest='UsuarioIT,ClienteIT,BicicletaIT,LocacaoIT'
```

## Estrutura

```
src/main/java/br/edu/ifc/bikes
├── config           timezone, auditoria, segurança e Swagger
├── entity           Usuario, Cliente, Bicicleta e Locacao
├── repository       acesso ao banco (Spring Data JPA)
├── dto              dados de entrada e saída da API (records com validação)
│   └── mapper       conversão entre entidade e DTO (MapStruct)
├── service          regras de negócio
├── web
│   ├── controller   endpoints REST
│   └── exception    tratamento dos erros em um formato único (ErrorMessage)
├── exception        exceções das regras de negócio
└── jwt              geração e validação do token

src/main/resources/reports   relatório Jasper (.jrxml e .jasper)
src/test                     testes de integração e scripts SQL de cada recurso
```

As regras ficam nos services; os controllers só recebem a requisição, chamam o service e devolvem o status.

## Autenticação
- `POST /api/v1/auth` com `username` e `password` devolve o token.
- As demais rotas pedem o header `Authorization: Bearer <token>`.
- Perfis `ADMIN` e `CLIENTE`. Todo usuário novo é criado como `CLIENTE`.

## Endpoints

| Método | Rota | Perfil | O que faz |
|---|---|---|---|
| POST | `/api/v1/usuarios` | público | Cria um usuário |
| POST | `/api/v1/auth` | público | Login, devolve o token |
| GET | `/api/v1/usuarios/{id}` | ADMIN ou o próprio usuário | Busca um usuário |
| PATCH | `/api/v1/usuarios/{id}` | o próprio usuário | Altera a senha |
| GET | `/api/v1/usuarios` | ADMIN | Lista os usuários |
| POST | `/api/v1/clientes` | CLIENTE | Cadastra o cliente do usuário logado |
| GET | `/api/v1/clientes/{id}` | ADMIN | Busca um cliente |
| GET | `/api/v1/clientes` | ADMIN | Lista os clientes |
| GET | `/api/v1/clientes/detalhes` | CLIENTE | Dados do próprio cliente |
| POST | `/api/v1/bicicletas` | ADMIN | Cadastra uma bicicleta |
| GET | `/api/v1/bicicletas/{id}` | ADMIN | Busca pelo id |
| GET | `/api/v1/bicicletas/codigo/{codigo}` | ADMIN | Busca pelo código |
| GET | `/api/v1/bicicletas` | ADMIN | Lista as bicicletas |
| POST | `/api/v1/locacoes` | CLIENTE | Aluga uma bicicleta |
| PATCH | `/api/v1/locacoes/{id}/devolucao` | CLIENTE | Devolve a bicicleta |
| GET | `/api/v1/locacoes/relatorio` | ADMIN | Relatório de locações em PDF |

Os erros voltam no formato `ErrorMessage`: 403 (perfil sem permissão), 404 (não encontrado), 409 (conflito com uma regra) e 422 (dados inválidos, com o erro de cada campo). Requisição sem token recebe 403 sem corpo.

## Modelo de dados
`usuarios` 1:1 `clientes` 1:N `locacoes` N:1 `bicicletas`. Todas as tabelas têm auditoria (`data_criacao`, `data_modificacao`, `criado_por`, `modificado_por`), preenchida com o usuário do token.

Em relação ao diagrama da descrição, a locação ganhou `data_prevista_devolucao` e `final_cartao`, que os requisitos pedem e o diagrama não tinha.

## Regras de negócio

### Usuário
- O email é o username e é único. Senha de 6 caracteres, gravada com BCrypt.
- Cada usuário só consulta e altera os próprios dados; o ADMIN lista todos.

### Cliente
- É cadastrado pelo próprio usuário logado, e o vínculo vem do token, para ninguém criar cliente em nome de outro.
- Cpf validado pelos dígitos verificadores e único. Um usuário só pode ter um cliente.

### Bicicleta
- Só o ADMIN gerencia, porque as bicicletas são da locadora.
- Código único, diferente do id, usado para localizar a bicicleta.
- Status `DISPONIVEL`, `ALUGADA` ou `MANUTENCAO`. Toda bicicleta começa disponível.

### Locação
- O cliente não escolhe a bicicleta: o sistema pega a primeira disponível. O cliente não tem acesso à lista de bicicletas, que é só do ADMIN. Sem bicicleta livre, a resposta é 409.
- O cartão é validado (16 dígitos com dígito verificador, validade `MM/AA` não vencida e cvv de 3 dígitos), mas só os 4 últimos dígitos são gravados, para não guardar dado sensível.
- O cliente informa por quantas horas vai usar (1 a 24), o que define a devolução prevista.
- A bicicleta passa para `ALUGADA`. A busca trava a bicicleta no banco até o fim da transação, para duas locações ao mesmo tempo não pegarem a mesma.
- Um cliente pode ter mais de uma locação ativa.

```
POST /api/v1/locacoes
{ "numeroCartao": "4111111111111111", "validadeCartao": "12/30", "cvv": "123", "horas": 2 }
```

### Devolução
- O cliente devolve a própria locação pelo id.
- O valor é de R\$ 10,00 por hora iniciada, com mínimo de 1 hora: 45 minutos custam R\$ 10,00 e 1h30 custa R\$ 20,00. Não há multa por atraso, só é cobrado o tempo usado.
- A locação fica `FINALIZADA` e a bicicleta volta para `DISPONIVEL`. Se o cliente informar um problema, ela vai para `MANUTENCAO`.
- A resposta mostra o valor a ser debitado e o final do cartão.

```
PATCH /api/v1/locacoes/{id}/devolucao
{ "possuiProblema": false }
```

### Relatório
- Feito no Jaspersoft Studio (`bikesReport.jrxml`) e gerado em PDF pelo `JasperService`. Só o ADMIN gera.
- Lista as locações da mais recente para a mais antiga, com cliente, código da bicicleta, retirada, devolução, duração (`5h`, `1h30`) e status. A locação ativa aparece como "EM ANDAMENTO".
- `GET /api/v1/locacoes/relatorio` traz todas; com `?apenasAtivas=true`, só as em andamento.

## Testes
Testes de integração de ponta a ponta com `WebTestClient`, um por recurso (`UsuarioIT`, `ClienteIT`, `BicicletaIT` e `LocacaoIT`). Cada teste carrega e apaga os próprios dados com scripts SQL (`src/test/resources/sql/`) e cobre sucesso, dados inválidos, conflito, não encontrado e acesso negado.
