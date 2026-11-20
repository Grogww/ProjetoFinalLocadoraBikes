package br.edu.ifc.bikes.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifc.bikes.entity.Bicicleta;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    Optional<Bicicleta> findByCodigo(String codigo);

    //trava a bicicleta encontrada até o fim da transação, para duas locações ao mesmo tempo não pegarem a mesma
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Bicicleta> findFirstByStatusOrderByIdAsc(Bicicleta.Status status);
}
