package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.mapper.BicicletaMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.exception.CodigoUniqueViolationException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final BicicletaMapper bicicletaMapper;

    public BicicletaResponseDTO create(BicicletaRequestDTO bicicletaRequestDTO) {
        try {
            Bicicleta bicicleta = bicicletaMapper.toBicicleta(bicicletaRequestDTO);
            return bicicletaMapper.toResponse(bicicletaRepository.save(bicicleta));
        }catch (DataIntegrityViolationException ex){
            throw new CodigoUniqueViolationException(String.format("A bicicleta com código '%s' já está cadastrada", bicicletaRequestDTO.codigo()));
        }
    }

    @Transactional()
    public BicicletaResponseDTO getById(Long id) {
        return bicicletaMapper.toResponse(bicicletaRepository.findById(id).orElseThrow(
                ()->new EntityNotFoundException(String.format("Bicicleta com id=%d não encontrada", id))
        ));
    }

    @Transactional()
    public BicicletaResponseDTO getByCodigo(String codigo) {
        return bicicletaMapper.toResponse(bicicletaRepository.findByCodigo(codigo).orElseThrow(
                ()->new EntityNotFoundException(String.format("Bicicleta com código '%s' não encontrada", codigo))
        ));
    }

    public List<BicicletaResponseDTO> getAll(){
        return bicicletaMapper.toResponse(bicicletaRepository.findAll());
    }
}
