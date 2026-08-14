# Spring Data JPA — Mini Curso Prático

> Projeto didático para dominar **Spring Data JPA** com foco em técnicas valorizadas no mercado.

---

## Tecnologias

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 21 | Linguagem base |
| Spring Boot | 3.3 | Framework |
| Spring Data JPA | 3.3 | Persistência |
| Hibernate Envers | 6.x | Auditoria / Histórico |
| H2 Database | — | Banco em memória (estudo) |
| Lombok | — | Redução de boilerplate |
| JUnit 5 + AssertJ | — | Testes |

---

## Estrutura de Entidades

```
Cliente ──< Pedido ──< ItemPedido >── Produto
```
- Um **Cliente** tem muitos **Pedidos**
- Um **Pedido** tem muitos **ItemPedido**
- Cada **ItemPedido** referencia um **Produto**

---

## As 13 Lições do Curso

| # | Lição | Arquivo |
|---|---|---|
| 01 | Mapeamento de Entidade, Auditoria, @OneToMany | `Cliente.java` |
| 02 | @Enumerated(STRING), BigDecimal, @DecimalMin | `Produto.java` |
| 03 | @ManyToOne, @JoinColumn, @PrePersist/@PreUpdate | `Pedido.java` |
| 04 | Entidade de associação, Snapshot de preço | `ItemPedido.java` |
| 05 | 4 Tipos de consulta (Derived, JPQL, Native, Spec) | `ClienteRepository.java` |
| 06 | Paginação, Filtros com Pageable, Projeções | `ProdutoRepository.java` |
| 07 | JOIN FETCH, Problema N+1, Subqueries | `PedidoRepository.java` |
| 08 | Relatórios e Rankings via ItemPedido | `ItemPedidoRepository.java` |
| 09 | Specifications — Filtros Dinâmicos | `ClienteSpecification.java` |
| 10 | @Transactional, readOnly, Histórico Envers | `ClienteService.java` |
| 11 | Transações, Cascatas, Rollback, Validação | `PedidoService.java` |
| 12 | @EnableJpaAuditing, CommandLineRunner, Seed | `DataInitializer.java` |
| 13 | Demonstração completa de todas as consultas | `ConsultasRunner.java` |

---

## Como Rodar

```bash
# Clone o projeto
git clone <url>

# Entre na pasta
cd springdata-minicurso

# Rode
./mvnw spring-boot:run
```

### Após iniciar:

- **H2 Console** → http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:springdatadb`
  - User: `sa` | Password: *(vazio)*

- **Logs do console** → Observe o SQL gerado por cada consulta

---

## Conceitos de Mercado Cobertos

### Tipos de Consulta Spring Data

```java
// 1. Query Method (gerada pelo nome)
List<Cliente> findByCidadeOrderByNomeAsc(String cidade);

// 2. @Query JPQL
@Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT(:p, '%'))")
List<Cliente> buscarPorPrefixoNome(@Param("p") String prefixo);

// 3. @Query Native SQL
@Query(value = "SELECT * FROM clientes WHERE estado = :e", nativeQuery = true)
List<Cliente> porEstadoNative(@Param("e") String estado);

// 4. Specification (filtro dinâmico)
Specification<Cliente> spec = Specification
    .where(ClienteSpecification.nomeContendo(nome))
    .and(ClienteSpecification.doEstado(estado));
clienteRepository.findAll(spec);
```

### Problema N+1 e JOIN FETCH

```java
// ERRADO — gera N+1 queries
List<Pedido> pedidos = pedidoRepository.findAll(); // 1 query
pedidos.forEach(p -> p.getItens().size());         // N queries!

// CORRETO — 1 única query com JOIN FETCH
@Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.itens i JOIN FETCH i.produto WHERE p.id = :id")
Optional<Pedido> buscarComItens(@Param("id") Long id);
```

### @Transactional Corretamente

```java
@Service
@Transactional(readOnly = true) // padrão para leituras
public class ClienteService {

    @Transactional // sobrescreve para escrita
    public Cliente salvar(Cliente c) { ... }

    public List<Cliente> listar() { ... } // herda readOnly
}
```

### Auditoria com Envers

```java
@Audited
@Entity
public class Cliente { ... }

// Interface do repository
public interface ClienteRepository
        extends RevisionRepository<Cliente, Long, Integer> { ... }

// Uso
List<Revision<Integer, Cliente>> historico = repository.findRevisions(id).toList();
```

### Paginação

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("nome").ascending());
Page<Cliente> pagina = repository.findAll(pageable);

pagina.getContent();       // itens da página
pagina.getTotalElements(); // total de registros
pagina.getTotalPages();    // total de páginas
```

---

## Diagrama de Tabelas (H2)

```sql
CLIENTES          PEDIDOS              ITENS_PEDIDO       PRODUTOS
─────────         ─────────────        ──────────────     ──────────
id (PK)           id (PK)              id (PK)            id (PK)
nome              cliente_id (FK)      pedido_id (FK)     nome
email             status               produto_id (FK)    preco
telefone          valor_total          quantidade         estoque
cidade            data_pedido          preco_unitario     categoria
estado            data_entrega_prev    subtotal           ativo
criado_em         data_entrega_real    
atualizado_em     observacao           
                  criado_em            

CLIENTES_AUDIT (Envers)
────────────────────────
id, rev, rev_type, nome, email, ...
```

---

##  Rodando os Testes

```bash
./mvnw test
```

Os testes usam `@DataJpaTest` — mais rápido que `@SpringBootTest`,
carrega apenas a camada JPA com H2 em memória.

---

## Por que este projeto é relevante no mercado?

| Tópico | Por que importa |
|---|---|
| Spring Data JPA | Framework padrão em 95%+ das empresas Java |
| Problema N+1 | Causa mais comum de lentidão em APIs JPA |
| Specifications | Filtros dinâmicos sem código duplicado |
| @Transactional readOnly | Performance em leituras intensas |
| Paginação | Obrigatório em qualquer API profissional |
| Auditoria Envers | Requisito de compliance e histórico |
| @Enumerated(STRING) | Evita bugs graves em produção |
| BigDecimal | Padrão para valores monetários |