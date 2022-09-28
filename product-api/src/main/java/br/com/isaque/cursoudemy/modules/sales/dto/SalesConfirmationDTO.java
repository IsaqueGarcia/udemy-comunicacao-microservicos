package br.com.isaque.cursoudemy.modules.sales.dto;

import br.com.isaque.cursoudemy.modules.sales.enums.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesConfirmationDTO {

    private String salesId;
    private SalesStatus salesStatus;
}
