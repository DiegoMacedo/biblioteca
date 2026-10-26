-- Dados iniciais: executado pelo Hibernate logo depois de criar as tabelas.
INSERT INTO autor (nome, email) VALUES ('Machado de Assis', 'machado@literatura.com');
INSERT INTO autor (nome, email) VALUES ('Clarice Lispector', 'clarice@literatura.com');

INSERT INTO genero (nome, descricao) VALUES ('Romance', 'Narrativas ficcionais centradas em relações e conflitos humanos');
INSERT INTO genero (nome, descricao) VALUES ('Ficção científica', 'Histórias sobre tecnologia, espaço e futuros imaginados');

INSERT INTO livro (titulo, isbn, ano_publicacao, genero_id) VALUES ('Dom Casmurro', '978-85-359-0277-3', 1899, 1);
INSERT INTO livro (titulo, isbn, ano_publicacao, genero_id) VALUES ('A Hora da Estrela', '978-85-325-3042-3', 1977, 1);

-- Associação muitos-para-muitos entre livros e autores.
INSERT INTO livro_autor (livro_id, autor_id) VALUES (1, 1);
INSERT INTO livro_autor (livro_id, autor_id) VALUES (2, 2);
