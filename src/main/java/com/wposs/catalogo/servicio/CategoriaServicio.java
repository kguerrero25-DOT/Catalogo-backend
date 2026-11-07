package com.wposs.catalogo.servicio;

import com.wposs.catalogo.dto.CategoriaDetalle;
import com.wposs.catalogo.dto.CategoriaNueva;
import com.wposs.catalogo.excepcion.RecursoDuplicadoException;
import com.wposs.catalogo.excepcion.RecursoNoEncontradoException;
import com.wposs.catalogo.excepcion.ReglaDeNegocioException;
import com.wposs.catalogo.mapper.CategoriaMapper;
import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.repositorio.CategoriaRepositorio;
import com.wposs.catalogo.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaServicio {
    private final CategoriaRepositorio repo;
    private final ProductoRepositorio productos;
    private final CategoriaMapper mapper;
    public CategoriaServicio(CategoriaRepositorio repo,ProductoRepositorio productos,CategoriaMapper mapper){this.repo=repo;this.productos=productos;this.mapper=mapper;}
    @Transactional(readOnly=true)
    public List<Categoria> listar(){return repo.findAll();}
    @Transactional(readOnly=true)
    public List<CategoriaDetalle> listarDetalles(){return repo.findAllWithProductos().stream().map(mapper::toDetalle).toList();}
    @Transactional
    public Categoria crear(String nombre){
        if(nombre==null||nombre.isBlank())throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        validarNombreDisponible(nombre,null);
        return repo.save(new Categoria(nombre.trim()));
    }
    @Transactional
    public CategoriaDetalle crear(CategoriaNueva datos){
        validarNombreDisponible(datos.nombre(),null);
        Categoria categoria=repo.save(new Categoria(datos.nombre().trim(),datos.descripcion()));
        return mapper.toDetalle(categoria);
    }
    @Transactional
    public boolean eliminar(Long id){
        if(!repo.existsById(id))return false;
        if(productos.existsByCategoria_Id(id))throw new ReglaDeNegocioException("No se puede eliminar una categoría que tiene productos");
        repo.deleteById(id);
        return true;
    }

    @Transactional
    public void eliminarRequerido(Long id){
        if(!repo.existsById(id))throw new RecursoNoEncontradoException("No se encontró la categoría solicitada");
        if(productos.existsByCategoria_Id(id))throw new ReglaDeNegocioException("No se puede eliminar una categoría que tiene productos");
        repo.deleteById(id);
    }

    private void validarNombreDisponible(String nombre,Long categoriaId){
        repo.findByNombreIgnoreCase(nombre.trim()).filter(categoria->!categoria.getId().equals(categoriaId))
                .ifPresent(categoria->{throw new RecursoDuplicadoException("Ya existe una categoría con ese nombre");});
    }
}
