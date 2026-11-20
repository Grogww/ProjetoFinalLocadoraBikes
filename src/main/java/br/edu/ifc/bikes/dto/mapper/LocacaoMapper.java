package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.entity.Locacao;
import org.mapstruct.Mapper;
import java.util.List;

//usa os mappers de cliente e bicicleta para montar os dados de cada um na resposta
@Mapper(componentModel = "spring", uses = {ClienteMapper.class, BicicletaMapper.class})
public interface LocacaoMapper {
    LocacaoResponseDTO toResponse(Locacao locacao);
    List<LocacaoResponseDTO> toResponse(List<Locacao> locacoes);
}
