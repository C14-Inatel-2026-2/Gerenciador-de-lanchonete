-- =============================================================================
-- Usuários de teste para o login provisório (header X-Usuario-Id)
-- =============================================================================
-- Enquanto o login pelo Supabase Auth (#55) não estiver pronto, não dá para
-- criar perfis pela API. Este script cria um perfil de cada tipo para testar
-- as rotas protegidas.
--
-- COMO USAR
--   1. Suba a API ao menos uma vez (o Hibernate cria a tabela user_profile).
--   2. No painel do Supabase: SQL Editor > New query > cole este arquivo > Run.
--   3. O SELECT do final mostra o id de cada usuário. Use esse id no header:
--        X-Usuario-Id: <id>
--
-- O script pode ser rodado mais de uma vez: usuários que já existem são
-- ignorados (ON CONFLICT), e nada é duplicado.
--
-- ATENÇÃO: os supabase_user_id abaixo são fictícios. Quando a #55 entrar,
-- estes perfis deixam de servir para login e podem ser apagados com o
-- DELETE comentado no fim do arquivo.
-- =============================================================================

INSERT INTO user_profile (supabase_user_id, nome, email, role, ativo)
VALUES
    ('teste-cliente',     'Cliente Teste',     'cliente.teste@lanchonete.com',     'CLIENTE',     true),
    ('teste-funcionario', 'Funcionário Teste', 'funcionario.teste@lanchonete.com', 'FUNCIONARIO', true),
    ('teste-admin',       'Admin Teste',       'admin.teste@lanchonete.com',       'ADMIN',       true),
    -- Usuário desativado: serve para conferir que o acesso é negado (403)
    ('teste-desativado',  'Desativado Teste',  'desativado.teste@lanchonete.com',  'CLIENTE',     false)
ON CONFLICT DO NOTHING;

SELECT id, nome, email, role, ativo
FROM user_profile
WHERE supabase_user_id LIKE 'teste-%'
ORDER BY id;

-- Para remover os usuários de teste (depois da #55), descomente e rode:
-- DELETE FROM user_profile WHERE supabase_user_id LIKE 'teste-%';
