package com.wposs.catalogo.servicio;

import com.wposs.catalogo.dto.EstadisticasCatalogo;
import com.wposs.catalogo.dto.ProductoActualizar;
import com.wposs.catalogo.dto.ProductoDetalle;
import com.wposs.catalogo.dto.ProductoNuevo;
import com.wposs.catalogo.dto.ProductoResumen;
import com.wposs.catalogo.excepcion.RecursoDuplicadoException;
import com.wposs.catalogo.excepcion.RecursoNoEncontradoException;
import com.wposs.catalogo.mapper.ProductoMapper;
import com.wposs.catalogo.modelo.*;
import com.wposs.catalogo.repositorio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoServicio {
    private final ProductoRepositorio repositorio;
    private final CategoriaRepositorio categorias;
    private final ProductoMapper mapper;
    private final String iva;
    public ProductoServicio(ProductoRepositorio repositorio, CategoriaRepositorio categorias,
                            ProductoMapper mapper,
                            @Value("${catalogo.iva:0.19}") String iva) {
        this.repositorio=repositorio;this.categorias=categorias;this.mapper=mapper;this.iva=iva;
    }
    @Transactional(readOnly=true)
    public List<Producto> buscarTodos(){return repositorio.buscarConCategoria();}

    @Transactional(readOnly=true)
    public List<ProductoResumen> buscarTodosResumen(){return repositorio.buscarConCategoria().stream().map(mapper::toResumen).toList();}


    @Transactional(readOnly=true)
    public List<Producto> buscarPorCategoria(String nombre){return repositorio.findByCategoria_NombreIgnoreCase(nombre);}
    @Transactional(readOnly=true)
    public List<ProductoResumen> buscarPorCategoriaResumen(String nombre){return repositorio.findByCategoria_NombreIgnoreCase(nombre).stream().map(mapper::toResumen).toList();}
    @Transactional(readOnly=true)
    public Optional<Producto> buscarPorId(Long id){
        return repositorio.findConCategoriaById(id);
    }
    @Transactional(readOnly=true)
    public ProductoDetalle buscarDetallePorId(Long id){
        Producto producto=repositorio.findConCategoriaById(id).orElseThrow(()->new RecursoNoEncontradoException("No se encontró el producto solicitado"));
        return mapper.toDetalle(producto);
    }
    @Transactional(readOnly=true)
    public List<Producto> sinStock(int limite){return repositorio.findByExistenciasLessThan(limite);}
    @Transactional(readOnly=true)
    public List<ProductoDetalle> sinStockDetalle(int limite){return repositorio.findByExistenciasLessThan(limite).stream().map(mapper::toDetalle).toList();}
    @Transactional
    public Producto crear(ProductoDatos datos){validar(datos);validarTituloDisponible(datos.titulo(),null);return repositorio.save(new Producto(datos.titulo().trim(),datos.precio(),resolverCategoria(datos),datos.existencias()));}
    @Transactional
    public ProductoDetalle crear(ProductoNuevo datos){
        validarTituloDisponible(datos.titulo(),null);
        Categoria categoria=resolverCategoria(datos.categoriaId());
        Producto producto=repositorio.save(new Producto(datos.titulo().trim(),datos.precio(),categoria,datos.existencias()));
        return mapper.toDetalle(producto);
    }
    @Transactional
    public Optional<Producto> actualizar(Long id, ProductoDatos datos){
        validar(datos);
        return repositorio.findById(id).map(p->{validarTituloDisponible(datos.titulo(),id);p.setTitulo(datos.titulo().trim());p.setPrecio(datos.precio());p.setExistencias(datos.existencias());p.setCategoria(resolverCategoria(datos));return p;});
    }
    @Transactional
    public ProductoDetalle actualizar(Long id, ProductoActualizar datos){
        Producto producto=repositorio.findConCategoriaById(id).orElseThrow(()->new RecursoNoEncontradoException("No se encontró el producto solicitado"));
        validarTituloDisponible(datos.titulo(),id);
        producto.setTitulo(datos.titulo().trim());
        producto.setPrecio(datos.precio());
        producto.setExistencias(datos.existencias());
        producto.setCategoria(resolverCategoria(datos.categoriaId()));
        return mapper.toDetalle(producto);
    }
    @Transactional
    public boolean eliminar(Long id){if(!repositorio.existsById(id))return false;repositorio.deleteById(id);return true;}
    @Transactional
    public void eliminarRequerido(Long id){
        if(!repositorio.existsById(id))throw new RecursoNoEncontradoException("No se encontró el producto solicitado");
        repositorio.deleteById(id);
    }
    @Transactional(readOnly=true)
    public Estadisticas estadisticas(){
        List<Producto> lista=repositorio.buscarConCategoria();
        BigDecimal total=lista.stream().map(p->p.getPrecio().multiply(BigDecimal.valueOf(p.getExistencias()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        Map<String,Long> grupos=lista.stream().collect(Collectors.groupingBy(Producto::getCategoriaNombre,TreeMap::new,Collectors.counting()));
        return new Estadisticas(lista.size(),total,grupos,iva);
    }
    @Transactional(readOnly=true)
    public EstadisticasCatalogo estadisticasCatalogo(){
        Estadisticas estadisticas=estadisticas();
        return new EstadisticasCatalogo(estadisticas.totalProductos(),estadisticas.valorTotalInventario(),
                estadisticas.cantidadPorCategoria(),estadisticas.iva());
    }
    @Transactional
    public void transferirExistencias(Long origen,Long destino,int cantidad){
        if(cantidad<=0||origen.equals(destino))throw new IllegalArgumentException("Transferencia invalida");
        Producto desde=repositorio.findById(origen).orElseThrow(()->new RecursoNoEncontradoException("No se encontró el producto de origen"));
        Producto hacia=repositorio.findById(destino).orElseThrow(()->new RecursoNoEncontradoException("No se encontró el producto de destino"));
        desde.setExistencias(desde.getExistencias()-cantidad);
        if(desde.getExistencias()<0)throw new IllegalArgumentException("Existencias insuficientes");
        hacia.setExistencias(hacia.getExistencias()+cantidad);
    }
    @Transactional
    public void cambiarPrecioSinSave(Long id,BigDecimal precio){
        if(precio==null||precio.signum()<=0)throw new IllegalArgumentException("Precio invalido");
        Producto producto=repositorio.findById(id).orElseThrow(()->new RecursoNoEncontradoException("No se encontró el producto solicitado"));
        producto.setPrecio(precio); // Hibernate detecta el cambio al cerrar la transaccion.
    }
    private Categoria resolverCategoria(ProductoDatos datos){
        if(datos.categoriaId()!=null)return resolverCategoria(datos.categoriaId());
        return categorias.findByNombreIgnoreCase(datos.categoria().trim()).orElseThrow(()->new RecursoNoEncontradoException("No se encontró la categoría indicada",400));
    }
    private Categoria resolverCategoria(Long categoriaId){
        return categorias.findById(categoriaId).orElseThrow(()->new RecursoNoEncontradoException("No se encontró la categoría indicada",400));
    }
    private void validarTituloDisponible(String titulo,Long productoId){
        repositorio.findByTituloIgnoreCase(titulo.trim()).filter(producto->!producto.getId().equals(productoId))
                .ifPresent(producto->{throw new RecursoDuplicadoException("Ya existe un producto con ese título");});
    }
    private void validar(ProductoDatos d){
        if(d==null||d.titulo()==null||d.titulo().isBlank()||d.precio()==null||d.precio().signum()<=0||d.existencias()==null||d.existencias()<0||
            (d.categoriaId()==null&&(d.categoria()==null||d.categoria().isBlank())))throw new IllegalArgumentException("Producto invalido");
    }
}
