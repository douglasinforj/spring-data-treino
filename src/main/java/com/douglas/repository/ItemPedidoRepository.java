package com.douglas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.douglas.domain.ItemPedido;

public interface ItemPedidoRepository  extends JpaRepository<ItemPedido, Long>{

    //Relatórios

    // Top produtos mais vendidos (por quantidade)
    @Query("""
            SELECT i.produto.nome,
                    SUM(i.quantidade)  AS totalVendido,
                    SUM(i.subTotal)    AS faturamento
            FROM ItemPedido i
            JOIN i.pedido p
            WHERE p.status != 'CANCELADO'
            GROUP BY i.produto
            ORDER BY SUM(i.quantidade) DESC
            """)
    List<Object[]> produtosMaisVendidos();

    // Itens de um pedido específico com dados do produto
    @Query("""
            SELECT i FROM ItemPedido i
            JOIN FETCH i.produto
            WHERE i.pedido.id = :pedidoId
            """)
    List<ItemPedido> buscarItensDoPedido(@Param("pedidoId") Long pedidoId);

}
