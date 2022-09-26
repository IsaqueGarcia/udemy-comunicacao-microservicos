package br.com.isaque.cursoudemy.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductRequest {

    private String name;
    private Integer supplierId;
    private Integer categoryId;
    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

}
