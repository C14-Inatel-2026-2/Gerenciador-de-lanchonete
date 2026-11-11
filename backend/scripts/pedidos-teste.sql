-- Pedidos de teste para a fila da cozinha (US04), enquanto o POST /api/pedidos (#45) não existe.
-- Itens marcados com [TESTE] para poder apagar depois (DELETE no final).
WITH novos AS (
  INSERT INTO pedido (cliente_id, total, criado_em, status) VALUES
    (1, 37.80, (now() AT TIME ZONE 'America/Sao_Paulo') - interval '30 minutes', 'CRIADO'),
    (1, 12.00, (now() AT TIME ZONE 'America/Sao_Paulo') - interval '20 minutes', 'EM_PREPARO'),
    (1, 25.50, (now() AT TIME ZONE 'America/Sao_Paulo') - interval '10 minutes', 'PRONTO')
  RETURNING id, status
)
INSERT INTO item_pedido (pedido_id, produto_id, nome, preco_unitario, quantidade)
SELECT id, 1, '[TESTE] X-Burguer', 18.90, 2 FROM novos WHERE status = 'CRIADO'
UNION ALL
SELECT id, 2, '[TESTE] Batata frita', 12.00, 1 FROM novos WHERE status = 'EM_PREPARO'
UNION ALL
SELECT id, 3, '[TESTE] Suco natural', 8.50, 3 FROM novos WHERE status = 'PRONTO';

SELECT p.id, p.status, p.criado_em, i.nome, i.quantidade
FROM pedido p JOIN item_pedido i ON i.pedido_id = p.id
WHERE i.nome LIKE '[TESTE]%' ORDER BY p.criado_em;
