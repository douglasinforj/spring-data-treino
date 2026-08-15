package com.douglas.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.douglas.domain.Produto;
import com.douglas.domain.enums.Categoria;

public interface ProdutoRepository  extends JpaRepository<Produto, Long>,
                                            JpaSpecificationExecutor<Produto>{

    //Query Method: buscar por categoria
    List<Produto> findByCategoria(Categoria categoria);

    //Query Method: buscar por categoria e disponibilidade
    List<Produto> findByCategoriaAndAtivoTrue(Categoria categoria);

    //Query Method: filtro de preço
    List<Produto> findByPrecoBetween(BigDecimal min, BigDecimal max);

    //QUery Method: busca por nome conteudo (Like)
    Page<Produto> findByNomeContainingIgnoreCase( String nome, Pageable pageable);

    //Query Method: estoque baixo
    List<Produto> findByEstoqueLessThanAtivoTrue(Integer limite);

    //Projeções com @Query (retornar apenas campos necessários)

    //Retornar somente o que a tela precisa, resumo dos produtos
    @Query("SELECT p.nome, p.preco, p.estoque, p.categoria FROM Produto p WHERE p.ativo = true ORDER BY p.preco")
    List<Object[]> resumoProdutoAtivos();

    //Valor médio por categoria
    @Query("SELECT p.categoria, AVG(p.preco), MIN(p.preco), MAX(p.preco) " +
           "FROM Produto p WHERE p.ativo = true GROUP BY p.categoria")
    List<Object[]> estatisticaPorCategoria();

    // Produtos com estoque zerado
    @Query("SELECT p FROM Produto p WHERE p.estoque = 0 AND p.ativo = true")
    List<Produto> produtosSemEstoque();

    // Busca completa com multiplos filtros opcionais (usando COALESCE no JPQL)
    @Query("""
            SELECT p FROM Produto p
            WHERE p.ativo = true
                AND (:categoria IS NULL OR CAST(p.categoria AS string) = :categoria)
                AND (:precoMin IS NULL OR p.preco >= :precoMin)
                AND (:precoMax IS NULL OR p.preco <= :precoMax)
                AND (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            ORDER BY p.preco ASC
            """)
    Page<Produto> buscarComFiltros(
        @Param("categoria") Categoria categoria,
        @Param("precoMin") BigDecimal precoMin,
        @Param("precoMax") BigDecimal precoMax,
        @Param("nome") String nome,
        Pageable pageable
    );

}
