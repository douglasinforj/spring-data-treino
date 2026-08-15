package com.douglas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import com.douglas.domain.Cliente;

public interface ClienteRepository  
        extends JpaRepository<Cliente, Long>,
                JpaSpecificationExecutor<Cliente>,
                RevisionRepository<Cliente, Long, Integer>{

    // TIPO 1 - Query Methods (Derivadas do Nome do Método)

    // Select * from clientes Where email = ?
    Optional<Cliente> findByEmail(String email);  

    // Select * from clientes Where nome Like "%?%"
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    // Select * from clientes Where cidade = ? ORDER BY nome ASC
    List<Cliente>findByCidadeOrderByNomeAsc(String cidade);

    // Select * From clientes where estado = ?
    List<Cliente>findByEstado(String estado);

    // Select * from cliente where estado = ? and cidade = ?
    List<Cliente> findByEstadoAndCidade(String estado, String cidade);

    // Select COUNT(*) from clientes where cidade = ?
    long countByCidade(String cidade);

    // Select * from clientes where criado_em BETWEEN ? AND ?
    List<Cliente> findByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);


    // Tipo 2 - @query com JPQL

    // Buscar clientes cujo nome começa com o prefixo informado
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT(:prefixo, '%'))")
    List<Cliente> buscarPorPrefixoNome(@Param("prefixo") String prefixo);

    // Buscar clientes que têm pedidos (JOIN implícito)
    @Query("SELECT DISTINCT c FROM  Cliente c JOIN c.pedidos p")
    List<Cliente> buscarClientesComPedidos();

    // Conta pedidos por cliente
    @Query("SELECT c, COUNT(p) FROM Cliente c LEFT JOIN c.pedido p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> contarPedidosPorCliente();

    /**
     * Caso de uso @EntityGraph - solução para o problema N+1
     * Carrega pedidos junto com o cliente em uma única query SQL
     * Sem @EntityGraph, acessar c.getPedidos() dispararia 1 query por cliente
     * */ 

    @EntityGraph(attributePaths = {"pedidos"})
    @Query("SELECT c FROM Cliente c WHERE c.estado = :estado")
    List<Cliente> buscarComPedidoporEstado(@Param("estado") String estado);

    /**Tipo 3 - @Query com Native Query (SQL puro)
     * Use nativeQuery = true para SQL especifico do banco
     * Útil para : funções do banco,CTes, Union, queries complexas
    */

    // SQL puro: top 5 clientes com mais pedidos
    @Query(value = """
            SELECT c.id, c.nome, c.email, COUNT(p.id) as total_pedidos
            FROM clientes c
            INNER JOIN pedidos p ON p.cliente_id = c.id
            GROUP BY c.id, c.nome, c.email
            ORDER BY total_pedidos DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> topClientesPorPedidos();

    // Native Query com paginação usando countQuery
    @Query(value = "SELECT * FROM clientes WHERE estado = :estado",
            countQuery = "SELECT COUNT(*) FROM clientes WHERE estado = :estado",
            nativeQuery = true)
    Page<Cliente> buscarPorEstadoNative(@Param("estado") String estado, Pageable pageable);

    /**
     * @Modifying indica que a query modifica dados(Insert, update, delete)
     * Deve Ser usado com @Transactional (no service).
    */

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Cliente c SET c.cidade = :novaCidade Where c.estado = :estado")
    int atualizarCidadePorEstado(@Param("estado") String estado, @Param("novaCidade") String novaCidade);
    
                    
}
