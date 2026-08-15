package com.douglas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.douglas.domain.Pedido;
import com.douglas.domain.enums.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> { 

    // Query Methods básicas
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByDataPedidoBetween(LocalDate inicio, LocalDate fim);
    List<Pedido> finByClienteIdAndStatus(Long clienteId, StatusPedido status);
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    
    // Join fetch - resolvendo o problema n+1

    //buscar com itens
    @Query("""
            SELECT DISTINCT p FROM Pedido p
            JOIN FETCH p.itens i
            JOIN FETCH i.produto
            WHERE p.id = :id
            """)
    Optional<Pedido>buscarComItens(@Param("id") Long id);


    // buscar pedido completo por status
    @Query("""
            SELECT DISTINCT p FROM Pedido p
            JOIN FETCH p.cliente
            JOIN FETCH p.itens i
            JOIN FETCH i.produto
            WHERE p.status = :status
            """)
    List<Pedido> buscarCompletosPorStatus(@Param("status") StatusPedido status);


    //Relatórios

    // Total faturado por período
    @Query("""
            SELECT SUM(p.valorTotal) FROM Pedido p 
            WHERE p.dataPedido BETWEEN :inicio AND :fim
                AND p.status NOT IN ('CANCELADO')
            """)
    Optional<BigDecimal>totalFaturadoNoPeriodo(
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

    
    // Native query com função de banco

    // Faturamento por mês
    @Query(value = """
            SELECT
                YEAR(data_pedido)  AS ano,
                MONTH(data_pedido) AS mes,
                COUNT(*)           AS total_pedido,
                SUM(valor_total)   AS faturamento
            FROM pedidos
            WHERE status != 'CANCELADO'
            GROUP BY YEAR(data_pedido), MONTH(data_pedido)
            ORDER BY ano DESC, mes DESC
            """, nativeQuery = true)
    List<Object[]> faturamentoPorMes();

    // Pedidos acima de um valor mínimo
    @Query("SELECT p FROM Pedido WHERE p.valorTotal >= :valorMinimo ORDER BY p.valorTotal DESC")
    List<Pedido> buscarPorValorMinimo(@Param("valorMinimo") BigDecimal valorMinimo);

    // Subquery em JPQL

    /**
     * Pedidos cujo o valor é maior que a média geral - usando subquery.
     * Demostra que JPQL também suporta sbqueries como SQL.
     */
    
    @Query("""
            SELECT p FROM Pedido p
            WHERE p.valorTotal > (
                SELECT AVG(p2.valorTotal) FROM Pedido p2
                WHERE p2.status != 'CANCELADO'
            )
            ORDER BY p.valorTotal DESC
            """)
    List<Pedido> pedidosAcimaDeMediaGeral();

    // Ranking de clientes por faturamento
    @Query("""
            SELECT p.cliente.nome, SUM(p.valorTotal)), COUNT(p)
            FROM Pedido p
            WHERE p.status != 'CANCELADO'
            GROUP BY p.cliente
            ORDER BY SUM(p.valorTotal) DESC
            """)
    List<Object[]> rankingClientesPorFaturamento();

    // verificar Cliente tem pedido com status (Exists om subquery)
    @Query("SELECT COUNT(p) > 0 FROM Pedido p WHERE p.cliente.id = :clienteId AND p.status")
    boolean clienteTemPedidoComStatus(@Param("clienteId") Long clienteId, @Param("status") StatusPedido status);

}

