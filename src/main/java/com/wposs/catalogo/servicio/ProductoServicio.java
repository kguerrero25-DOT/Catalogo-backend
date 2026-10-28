package com.wposs.catalogo.servicio;

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
    private final String iva;
    public ProductoServicio(ProductoRepositorio repositorio, CategoriaRepositorio categorias,
                            @Value("${catalogo.iva:0.19}") String iva) {
        this.repositorio=repositorio;this.categorias=categorias;this.iva=iva;
    }
    @Transactional(readOnly=true)
    public List<Producto> buscarTodos(){return repositorio.buscarConCategoria();}


    @Transactional(readOnly=true)
    public List<Producto> buscarPorCategoria(String nombre){return repositorio.findByCategoria_NombreIgnoreCase(nombre);}
    @Transactional(readOnly=true)
    public Optional<Producto> buscarPorId(Long id){
        return repositorio.findConCategoriaById(id);
    }
    @Transactional(readOnly=true)
    public List<Producto> sinStock(int limite){return repositorio.findByExistenciasLessThan(limite);}
    @Transactional
    public Producto crear(ProductoDatos datos){validar(datos);return repositorio.save(new Producto(datos.titulo().trim(),datos.precio(),resolverCategoria(datos),datos.existencias()));}
    @Transactional
    public Optional<Producto> actualizar(Long id, ProductoDatos datos){
        validar(datos);
        return repositorio.findById(id).map(p->{p.setTitulo(datos.titulo().trim());p.setPrecio(datos.precio());p.setExistencias(datos.existencias());p.setCategoria(resolverCategoria(datos));return p;});
    }
    @Transactional
    public boolean eliminar(Long id){if(!repositorio.existsById(id))return false;repositorio.deleteById(id);return true;}
    @Transactional(readOnly=true)
    public Estadisticas estadisticas(){
        List<Producto> lista=repositorio.buscarConCategoria();
        BigDecimal total=lista.stream().map(p->p.getPrecio().multiply(BigDecimal.valueOf(p.getExistencias()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        Map<String,Long> grupos=lista.stream().collect(Collectors.groupingBy(Producto::getCategoriaNombre,TreeMap::new,Collectors.counting()));
        return new Estadisticas(lista.size(),total,grupos,iva);
    }
    @Transactional
    public void transferirExistencias(Long origen,Long destino,int cantidad){
        if(cantidad<=0||origen.equals(destino))throw new IllegalArgumentException("Transferencia invalida");
        Producto desde=repositorio.findById(origen).orElseThrow(()->new IllegalArgumentException("Origen inexistente"));
        Producto hacia=repositorio.findById(destino).orElseThrow(()->new IllegalArgumentException("Destino inexistente"));
        desde.setExistencias(desde.getExistencias()-cantidad);
        if(desde.getExistencias()<0)throw new IllegalArgumentException("Existencias insuficientes");
        hacia.setExistencias(hacia.getExistencias()+cantidad);
    }
    @Transactional
    public void cambiarPrecioSinSave(Long id,BigDecimal precio){
        if(precio==null||precio.signum()<=0)throw new IllegalArgumentException("Precio invalido");
        Producto producto=repositorio.findById(id).orElseThrow(()->new IllegalArgumentException("No existe"));
        producto.setPrecio(precio); // Hibernate detecta el cambio al cerrar la transaccion.
    }
    private Categoria resolverCategoria(ProductoDatos datos){
        if(datos.categoriaId()!=null)return categorias.findById(datos.categoriaId()).orElseThrow(()->new IllegalArgumentException("Categoria inexistente"));
        return categorias.findByNombreIgnoreCase(datos.categoria().trim()).orElseThrow(()->new IllegalArgumentException("Categoria inexistente"));
    }
    private void validar(ProductoDatos d){
        if(d==null||d.titulo()==null||d.titulo().isBlank()||d.precio()==null||d.precio().signum()<=0||d.existencias()==null||d.existencias()<0||
            (d.categoriaId()==null&&(d.categoria()==null||d.categoria().isBlank())))throw new IllegalArgumentException("Producto invalido");
    }
}
