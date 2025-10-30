package com.example.instructions.controller;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.service.TradeService;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade Instructions", description = "API for processing trade instructions")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;

    @Autowired
    public TradeController(TradeService tradeService, ObjectMapper objectMapper) {
        this.tradeService = tradeService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        this.csvMapper.registerModule(new JavaTimeModule());
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload trade instructions file", 
               description = "Upload CSV or JSON file containing trade instructions")
    @ApiResponse(responseCode = "200", description = "File processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file format or content")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File is empty"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid filename"));
        }

        try {
            List<String> processedTradeIds = new ArrayList<>();
            
            if (filename.toLowerCase().endsWith(".csv")) {
                processedTradeIds = processCsvFile(file);
            } else if (filename.toLowerCase().endsWith(".json")) {
                processedTradeIds = processJsonFile(file);
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unsupported file format. Only CSV and JSON are supported."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "File processed successfully");
            response.put("processedCount", processedTradeIds.size());
            response.put("tradeIds", processedTradeIds);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    @PostMapping("/single")
    @Operation(summary = "Process single trade instruction", 
               description = "Process a single trade instruction via JSON payload")
    @ApiResponse(responseCode = "200", description = "Trade processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid trade data")
    public ResponseEntity<Map<String, Object>> processSingleTrade(@Valid @RequestBody CanonicalTrade trade) {
        try {
            String tradeId = tradeService.processTrade(trade);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Trade processed successfully");
            response.put("tradeId", tradeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing single trade: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to process trade: " + e.getMessage()));
        }
    }

    @GetMapping("/canonical/{tradeId}")
    @Operation(summary = "Get canonical trade by ID", 
               description = "Retrieve stored canonical trade data by trade ID")
    public ResponseEntity<CanonicalTrade> getCanonicalTrade(@PathVariable String tradeId) {
        CanonicalTrade trade = tradeService.getCanonicalTrade(tradeId);
        if (trade != null) {
            return ResponseEntity.ok(trade);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get processing statistics", 
               description = "Get current processing statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("storedTradeCount", tradeService.getStoredTradeCount());
        return ResponseEntity.ok(stats);
    }

    private List<String> processCsvFile(MultipartFile file) throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        MappingIterator<CanonicalTrade> iterator = csvMapper
            .readerFor(CanonicalTrade.class)
            .with(schema)
            .readValues(file.getInputStream());

        List<String> tradeIds = new ArrayList<>();
        while (iterator.hasNext()) {
            CanonicalTrade trade = iterator.next();
            String tradeId = tradeService.processTrade(trade);
            tradeIds.add(tradeId);
        }
        
        return tradeIds;
    }

    private List<String> processJsonFile(MultipartFile file) throws IOException {
        CanonicalTrade[] trades = objectMapper.readValue(file.getInputStream(), CanonicalTrade[].class);
        
        List<String> tradeIds = new ArrayList<>();
        for (CanonicalTrade trade : trades) {
            String tradeId = tradeService.processTrade(trade);
            tradeIds.add(tradeId);
        }
        
        return tradeIds;
    }
}