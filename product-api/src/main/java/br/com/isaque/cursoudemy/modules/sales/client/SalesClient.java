package br.com.isaque.cursoudemy.modules.sales.client;

import br.com.isaque.cursoudemy.modules.sales.dto.SalesProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(
        name = "salesClient",
        contextId = "salesClient",
        url = "${app-config.services.sales}"
)
public interface SalesClient {

    @GetMapping("products/{productsId}")
    Optional<SalesProductResponse> findSalesByProductId(@PathVariable Integer productsId);

}
