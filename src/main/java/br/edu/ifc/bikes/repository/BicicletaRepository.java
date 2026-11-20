package br.edu.ifc.bikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifc.bikes.entity.Bicicleta;

import java.util.Optional;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    Optional<Bicicleta> findByCodigo(String codigo);
}
