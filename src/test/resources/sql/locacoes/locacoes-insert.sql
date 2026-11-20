insert into USUARIOS (id, username, password, role)
    values (100, 'jose@gmail.com', '$2a$10$ywZo7Steaud99L9i.plj6upXkxGCE5CtAbngLIiX/pN/jZkZvWP.G','ROLE_ADMIN');
insert into USUARIOS (id, username, password, role)
    values (101, 'maria@gmail.com', '$2a$10$ywZo7Steaud99L9i.plj6upXkxGCE5CtAbngLIiX/pN/jZkZvWP.G','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (102, 'joao@gmail.com', '$2a$10$ywZo7Steaud99L9i.plj6upXkxGCE5CtAbngLIiX/pN/jZkZvWP.G','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (103, 'ana@gmail.com', '$2a$10$ywZo7Steaud99L9i.plj6upXkxGCE5CtAbngLIiX/pN/jZkZvWP.G','ROLE_CLIENTE');
insert into CLIENTES (id, nome, cpf, telefone, email, usuario_id)
    values (10, 'Maria da Silva', '11144477735', '49999990000', 'maria@gmail.com', 101);
insert into CLIENTES (id, nome, cpf, telefone, email, usuario_id)
    values (11, 'Ana Souza', '52998224725', '49988880000', 'ana@gmail.com', 103);
insert into BICICLETAS (id, modelo, codigo, status)
    values (10, 'Caloi Elite', 'BK-001', 'ALUGADA');
insert into BICICLETAS (id, modelo, codigo, status)
    values (11, 'Specialized Rockhopper', 'BK-002', 'MANUTENCAO');
insert into BICICLETAS (id, modelo, codigo, status)
    values (12, 'Oggi Big Wheel', 'BK-003', 'DISPONIVEL');
insert into BICICLETAS (id, modelo, codigo, status)
    values (13, 'Sense Impact', 'BK-004', 'DISPONIVEL');
insert into BICICLETAS (id, modelo, codigo, status)
    values (14, 'Caloi Explorer', 'BK-005', 'ALUGADA');
insert into LOCACOES (id, cliente_id, bicicleta_id, data_retirada, data_prevista_devolucao, final_cartao, status)
    values (20, 10, 10, DATEADD('MINUTE', -90, LOCALTIMESTAMP), DATEADD('MINUTE', 30, LOCALTIMESTAMP), '1111', 'ATIVA');
insert into LOCACOES (id, cliente_id, bicicleta_id, data_retirada, data_prevista_devolucao, final_cartao, status)
    values (21, 11, 14, DATEADD('MINUTE', -10, LOCALTIMESTAMP), DATEADD('MINUTE', 50, LOCALTIMESTAMP), '1111', 'ATIVA');
insert into LOCACOES (id, cliente_id, bicicleta_id, data_retirada, data_prevista_devolucao, data_devolucao, valor, final_cartao, status)
    values (22, 10, 11, '2026-11-01 08:00:00', '2026-11-01 09:00:00', '2026-11-01 09:00:00', 10.00, '1111', 'FINALIZADA');
