CREATE VIEW CUSTO (FATO_ID, DIA, DESCRICAO, VALOR) 
AS (SELECT F.ID, F.DIA, F.DESCRICAO, SUM(L.VALOR) 
    FROM FATO F, LANCAMENTO L 
    WHERE L.FATO_ID = F.ID
    GROUP BY F.ID, F.DIA, F.DESCRICAO
);

CREATE VIEW SALDO(CONTA_ID, NOME, SALDO)
AS (SELECT C.ID, C.NOME, SUM(L.VALOR) 
    FROM CONTA C, LANCAMENTO L 
    WHERE C.ID = L.CONTA_ID 
    GROUP BY C.ID, C.NOME
);