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
