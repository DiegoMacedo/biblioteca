-- Dados iniciais: executado pelo Hibernate logo depois de criar as tabelas.
INSERT INTO autor (nome, email) VALUES ('Machado de Assis', 'machado@literatura.com');
INSERT INTO autor (nome, email) VALUES ('Clarice Lispector', 'clarice@literatura.com');

INSERT INTO genero (nome, descricao) VALUES ('Romance', 'Narrativas ficcionais centradas em relações e conflitos humanos');
INSERT INTO genero (nome, descricao) VALUES ('Ficção científica', 'Histórias sobre tecnologia, espaço e futuros imaginados');

INSERT INTO livro (titulo, isbn, ano_publicacao) VALUES ('Dom Casmurro', '978-85-359-0277-3', 1899);
INSERT INTO livro (titulo, isbn, ano_publicacao) VALUES ('A Hora da Estrela', '978-85-325-3042-3', 1977);
