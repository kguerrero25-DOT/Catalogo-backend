package com.wposs.catalogo.servicio;

import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.repositorio.CategoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaServicio {
    private final CategoriaRepositorio repo;
    public CategoriaServicio(CategoriaRepositorio repo){this.repo=repo;}
    @Transactional(readOnly=true)
    public List<Categoria> listar(){return repo.findAll();}
    @Transactional
    public Categoria crear(String nombre){if(nombre==null||nombre.isBlank()||repo.findByNombreIgnoreCase(nombre.trim()).isPresent())throw new IllegalArgumentException("Categoria invalida");return repo.save(new Categoria(nombre.trim()));}
    @Transactional
    public boolean eliminar(Long id){if(!repo.existsById(id))return false;repo.deleteById(id);return true;}
}
