package com.wposs.catalogo.config;

import com.wposs.catalogo.modelo.*;
import com.wposs.catalogo.repositorio.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DatosIniciales {
    @Bean CommandLineRunner cargar(CategoriaRepositorio categorias,ProductoRepositorio productos){return args->{
        if(categorias.count()>0)return;
        Categoria libros=categorias.save(new Categoria("libros"));
        Categoria tecnologia=categorias.save(new Categoria("tecnologia"));
        Categoria papeleria=categorias.save(new Categoria("papeleria"));
        Categoria hogar=categorias.save(new Categoria("hogar"));
        String[][] datos={
            {"Java para principiantes","45000","12"},{"Clean Code","95000","5"},{"Spring Boot en practica","78000","7"},{"Patrones de diseño","99000","8"},{"Arquitectura limpia","88000","10"},
            {"Teclado mecanico","180000","9"},{"Mouse inalambrico","65000","20"},{"Monitor 24 pulgadas","650000","4"},{"Memoria USB","35000","15"},{"Auriculares","90000","6"},
            {"Cuaderno universitario","12000","35"},{"Boligrafo azul","2500","100"},{"Agenda","18000","12"},{"Lapiz","1000","80"},{"Resaltador","4500","45"},
            {"Lampara","55000","9"},{"Organizador","25000","13"},{"Reloj","85000","4"},{"Almohada","32000","17"},{"Taza","16000","24"}
        };
        for(int i=0;i<datos.length;i++){
            Categoria categoria=i<5?libros:i<10?tecnologia:i<15?papeleria:hogar;
            productos.save(new Producto(datos[i][0],new BigDecimal(datos[i][1]),categoria,Integer.valueOf(datos[i][2])));
        }
    };}
}
