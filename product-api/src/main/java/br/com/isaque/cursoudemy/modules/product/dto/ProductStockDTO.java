package br.com.isaque.cursoudemy.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockDTO {

    private String salesId;
    private List<ProductQuantityDTO> products;

}
