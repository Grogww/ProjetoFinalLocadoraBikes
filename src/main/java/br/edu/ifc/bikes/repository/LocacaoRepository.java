package br.edu.ifc.bikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifc.bikes.entity.Locacao;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
}
