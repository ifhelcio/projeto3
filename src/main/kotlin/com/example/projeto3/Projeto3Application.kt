package com.example.projeto3

import jakarta.persistence.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*




@Entity
data class Produto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var nome: String = "",
    var preco: Double = 0.0
) {

}


@Entity
data class Pedido(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var numero: String,
    var total: Double,
    @ManyToMany
    val produtos: MutableList<Produto> = mutableListOf()
) {
    constructor() : this(0, "", 0.0)
}


@Repository
interface ProdutoRepository : JpaRepository<Produto, Long> {
}


@Repository
interface PedidoRepository : JpaRepository<Pedido, Long> {
}


@RestController
@RequestMapping("/produtos")
class ProdutoController(
    private val produtoRepository: ProdutoRepository,
    private val pedidoRepository: PedidoRepository
) {



    @PostMapping
    fun criarProduto(@RequestBody produto: Produto): Produto {
        return produtoRepository.save(produto)
    }

    @GetMapping("/{id}")
    fun obterProduto(@PathVariable id: Long): Produto {
        return produtoRepository.findById(id).orElseThrow { NoSuchElementException("Produto não encontrado") }
    }

    @PutMapping("/{id}")
    fun atualizarProduto(@PathVariable id: Long, @RequestBody produto: Produto): Produto {
        val produtoExistente = produtoRepository.findById(id).orElseThrow { NoSuchElementException("Produto não encontrado") }
        produtoExistente.nome = produto.nome
        produtoExistente.preco = produto.preco
        return produtoRepository.save(produtoExistente)
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun deletarProduto(@PathVariable id: Long) {
        produtoRepository.deleteById(id)
    }

    @GetMapping
    fun listarTodosProdutos(): List<Produto> {
        return produtoRepository.findAll()
    }


    @PostMapping("/{id}/associar-pedido/{pedidoId}")
    fun associarProdutoPedido(@PathVariable id: Long, @PathVariable pedidoId: Long): Produto {
        val produto = produtoRepository.findById(id).orElseThrow { NoSuchElementException("Produto não encontrado") }
        val pedido = pedidoRepository.findById(pedidoId).orElseThrow { NoSuchElementException("Pedido não encontrado") }
        pedido.produtos.add(produto)
        pedidoRepository.save(pedido)
        return produto
    }
}


@RestController
@RequestMapping("/pedidos")
class PedidoController(
    private val pedidoRepository: PedidoRepository,
    private val produtoRepository: ProdutoRepository
) {

    // CRUD do Pedido

    @PostMapping
    fun criarPedido(@RequestBody pedido: Pedido): Pedido {
        return pedidoRepository.save(pedido)
    }

    @GetMapping("/{id}")
    fun obterPedido(@PathVariable id: Long): Pedido {
        return pedidoRepository.findById(id).orElseThrow { NoSuchElementException("Pedido não encontrado") }
    }

    @PutMapping("/{id}")
    fun atualizarPedido(@PathVariable id: Long, @RequestBody pedido: Pedido): Pedido {
        val pedidoExistente = pedidoRepository.findById(id).orElseThrow { NoSuchElementException("Pedido não encontrado") }
        pedidoExistente.numero = pedido.numero
        pedidoExistente.total = pedido.total
        return pedidoRepository.save(pedidoExistente)
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    fun deletarPedido(@PathVariable id: Long) {
        pedidoRepository.deleteById(id)
    }

    @GetMapping
    fun listarPedidos(): List<Pedido> {
        return pedidoRepository.findAll()
    }


    @PostMapping("/{id}/associar-produto/{produtoId}")
    fun associarPedidoProduto(@PathVariable id: Long, @PathVariable produtoId: Long): Pedido {
        val pedido = pedidoRepository.findById(id).orElseThrow { NoSuchElementException("Pedido não encontrado") }
        val produto = produtoRepository.findById(produtoId).orElseThrow { NoSuchElementException("Produto não encontrado") }
        pedido.produtos.add(produto)
        pedidoRepository.save(pedido)
        return pedido
    }

    @DeleteMapping("/{pedidoId}/excluir-produto/{produtoId}")
    fun excluirProdutoDoPedido(@PathVariable pedidoId: Long, @PathVariable produtoId: Long): Pedido {
        val pedido = pedidoRepository.findById(pedidoId).orElseThrow { NoSuchElementException("Pedido não encontrado") }
        val produto = produtoRepository.findById(produtoId).orElseThrow { NoSuchElementException("Produto não encontrado") }

        pedido.produtos.remove(produto)
        pedidoRepository.save(pedido)

        return pedido
    }
}


@RestController
@RequestMapping("/consulta")
class ConsultaController(private val produtoRepository: ProdutoRepository) {

    @GetMapping
    fun consultarProdutos(
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) preco: Double?,
        @RequestParam(defaultValue = "nome") orderBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): List<Produto> {
        val sortDirection = if (direction.toLowerCase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val sortProperties = arrayOf(orderBy)
        val sort = Sort.by(sortDirection, *sortProperties)

        val produtoExample = Example.of(
            Produto(
                nome = "%${nome.toString()}%",
                preco = preco ?: 0.0
            )
        )

        val produtos = produtoRepository.findAll(produtoExample, sort)
        println("Produtos encontrados: $produtos")

        return produtos
    }
}


//@Configuration
//@EnableWebSecurity
//class SecurityConfig(): WebSecurityConfigurerAdapter {
//
//    fun configure(http: HttpSecurity) {
//        http
//            .authorizeRequests()
//            .requestMatchers(HttpMethod.POST, "/produtos/**").authenticated()
//            .requestMatchers(HttpMethod.PUT, "/produtos/**").authenticated()
//            .requestMatchers(HttpMethod.DELETE, "/produtos/**").hasRole("ADMIN")
//            .requestMatchers(HttpMethod.POST, "/pedidos/**").authenticated()
//            .requestMatchers(HttpMethod.PUT, "/pedidos/**").authenticated()
//            .requestMatchers(HttpMethod.DELETE, "/pedidos/**").hasRole("ADMIN")
//            .anyRequest().permitAll()
//            .and()
//
//    }
//
//    @Bean
//    fun userDetailsService(): UserDetailsService {
//        val user = User.builder()
//            .username("user")
//            .password("{noop}password")
//            .roles("USER")
//            .build()
//
//        val admin = User.builder()
//            .username("admin")
//            .password("{noop}password")
//            .roles("USER", "ADMIN")
//            .build()
//
//        return InMemoryUserDetailsManager(user, admin)
//
//    }
//}



@SpringBootApplication
@ComponentScan(basePackages = ["com.example"])
class Projeto3Application

fun main(args: Array<String>) {
    runApplication<Projeto3Application>(*args)
}
