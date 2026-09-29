package fiap.com.br.petguardian.pet.historico.dto;

import fiap.com.br.petguardian.pet.historico.Historico;

import java.time.LocalDateTime;

public record HistoricoResponse(
        Long id,
        String tipoHist,
        LocalDateTime dataHist,
        Long petId,
        String nomePet
) {
    public static HistoricoResponse fromEntity(Historico h) {
        return new HistoricoResponse(
                h.getId(),
                h.getTipoHist(),
                h.getDataHist(),
                h.getPet().getId(),
                h.getPet().getNome()
        );
    }
}
