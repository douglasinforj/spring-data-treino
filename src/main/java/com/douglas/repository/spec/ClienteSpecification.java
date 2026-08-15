package com.douglas.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.douglas.domain.Cliente;

import org.springframework.util.StringUtils;

/* PROBLEMA:
 *  Como fazer filtros opcionais? Ex: buscar clientes por nome E/OU cidade
 *  E/OU estado — qualquer combinação.
 *  Com Query Methods ou @Query fixo, você precisaria de um método para
 *  cada combinação: buscarPorNome, buscarPorNomeECidade, etc. (impraticável!)
 *
 * SOLUÇÃO: Specifications + JpaSpecificationExecutor
 *  - Cada Specification encapsula UM filtro (uma cláusula WHERE).
 *  - Combinamos com .and() / .or() em tempo de execução.
 *  - O Spring Data gera o SQL dinamicamente baseado nos filtros ativos.
 *
 * DICA DE MERCADO:
 *  - Specifications são ideais para telas de pesquisa avançada.
 *  - Alternativa moderna: use a biblioteca Querydsl ou Blaze-Persistence
 *    para casos mais complexos. Em projetos simples/médios, Specification
 *    é suficiente e não adiciona dependências extras.
 */

public class ClienteSpecification {
    
    private ClienteSpecification() {}    //Classe utilitária - Não instanciável

    /**
     * Filtro por nome (parcial, case-insensitive)
     * Se nome for vazio/nulo, retorna null - Specification ignora este filtro
     */
    public static Specification<Cliente> nomeContendo(String nome) {
        return (root, query, cb) -> {
            if(!StringUtils.hasText(nome)) return null;
            // root.get("nome") - campo "nome" da entidade Cliente
            return cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
        };
    }

    // Filtro por estado exato
    public static Specification<Cliente> doEstado(String estado) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(estado)) return null;
            return cb.equal(root.get("estado"), estado.toUpperCase());
        };
    }

    // Filtro por cidade exata.
    public static Specification<Cliente> daCidade(String cidade) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(cidade)) return  null;
            return cb.equal(cb.lower(root.get("cidade")), cidade.toLowerCase());
        };
    }

    /**
     * Filtro: Apenas clientes com pelo menos um pedido.
     * Usa JOIN implicito com a coleção de pedidos
    */

    public static Specification<Cliente> comPedido() {
        return (root, query, cb) -> {
            //Faz um join com a coleção pedidos e verifica se existe pelo menos 1
            var join = root.join("pedidos");
            query.distinct(true);
            return join.isNotNull();
        };
    }

    //Filtro por e-mail contendo.
    public static Specification<Cliente> emailContendo(String email) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(email)) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

}