package org.example.catalogservice.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.catalogservice.entity.CatalogEntity;
import org.example.catalogservice.service.CatalogService;
import org.example.catalogservice.vo.ResponseCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog-service")
public class CatalogController {

    private final Environment environment;
    private final CatalogService catalogService;

    @GetMapping("/health_check")
    public String status() {
        log.info("CatalogController.status");
        return String.format("Working in [CATALOG-SERVICE] PORT ON %s", environment.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<?> getCatalogs() {
        log.info("CatalogController.getUsers");
        Iterable<CatalogEntity> catalogs = catalogService.getAllCatalogs();
        List<ResponseCatalog> responseCatalogs = new ArrayList<>();
        catalogs.forEach(v -> {
            responseCatalogs.add(new ModelMapper().map(v, ResponseCatalog.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(responseCatalogs);
    }
}
