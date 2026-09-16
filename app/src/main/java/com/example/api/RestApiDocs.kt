package com.example.api

object RestApiDocs {

    fun getCurlSnippet(text: String): String {
        val escaped = text.replace("\"", "\\\"").take(120)
        return """curl -X POST "https://api.refextract.io/api/v1/extract" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "$escaped...",
    "options": {
      "categories": ["REFERENCE", "QUOTATION", "NAMED_ENTITY", "METRIC_STAT"],
      "minConfidence": 0.65
    }
  }'"""
    }

    val ASP_NET_CORE_CONTROLLER_SNIPPET = """
// ASP.NET Core 8.0 / .NET RESTful Controller
// File: Controllers/ExtractionController.cs
using Microsoft.AspNetCore.Mvc;
using System.Text.RegularExpressions;

namespace RefExtract.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    [Produces("application/json")]
    public class ExtractController : ControllerBase
    {
        private readonly IExtractionService _extractionService;
        private readonly ILogger<ExtractController> _logger;

        public ExtractController(IExtractionService extractionService, ILogger<ExtractController> logger)
        {
            _extractionService = extractionService;
            _logger = logger;
        }

        [HttpPost]
        [ProducesResponseType(typeof(ExtractionResponse), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<IActionResult> Extract([FromBody] ExtractionRequest request)
        {
            if (string.IsNullOrWhiteSpace(request?.Text))
            {
                return BadRequest(new { error = "The 'text' field cannot be null or empty." });
            }

            var stopwatch = System.Diagnostics.Stopwatch.StartNew();
            var extractedItems = await _extractionService.AnalyzeAsync(request.Text, request.Options);
            stopwatch.Stop();

            var response = new ExtractionResponse
            {
                Status = "success",
                Id = Guid.NewGuid().ToString(),
                Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds(),
                ProcessingTimeMs = stopwatch.ElapsedMilliseconds,
                TotalExtracted = extractedItems.Count,
                Results = extractedItems
            };

            return Ok(response);
        }
    }

    public record ExtractionRequest(string Text, ExtractionOptions? Options);
    public record ExtractionOptions(string[]? Categories, double MinConfidence = 0.6);
    public record ExtractedItem(
        string Id,
        string DetectedText,
        string Category,
        string SubCategory,
        int StartIndex,
        int EndIndex,
        double ConfidenceScore,
        Dictionary<string, string> Metadata
    );
    public record ExtractionResponse
    {
        public string Status { get; init; } = "success";
        public string Id { get; init; } = "";
        public long Timestamp { get; init; }
        public long ProcessingTimeMs { get; init; }
        public int TotalExtracted { get; init; }
        public List<ExtractedItem> Results { get; init; } = new();
    }
}
""".trimIndent()

    val FLUTTER_DART_CLIENT_SNIPPET = """
// Flutter / Dart Client Integration
// File: lib/services/extraction_client.dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class ExtractionClient {
  final String baseUrl;
  final String? apiKey;

  ExtractionClient({
    this.baseUrl = 'https://api.refextract.io/api/v1',
    this.apiKey,
  });

  Future<ExtractionResponse> analyzeText({
    required String text,
    List<String> categories = const [
      'REFERENCE',
      'QUOTATION',
      'NAMED_ENTITY',
      'METRIC_STAT',
      'TEMPORAL_DATE'
    ],
    double minConfidence = 0.60,
  }) async {
    final uri = Uri.parse('${'$'}baseUrl/extract');
    final response = await http.post(
      uri,
      headers: {
        'Content-Type': 'application/json',
        if (apiKey != null) 'Authorization': 'Bearer ${'$'}apiKey',
      },
      body: jsonEncode({
        'text': text,
        'options': {
          'categories': categories,
          'minConfidence': minConfidence,
        },
      }),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return ExtractionResponse.fromJson(data);
    } else {
      throw Exception('Extraction API failed: ${'$'}{response.statusCode} - ${'$'}{response.body}');
    }
  }
}

class ExtractionResponse {
  final String status;
  final String id;
  final int processingTimeMs;
  final int totalExtracted;
  final List<ExtractedItem> results;

  ExtractionResponse({
    required this.status,
    required this.id,
    required this.processingTimeMs,
    required this.totalExtracted,
    required this.results,
  });

  factory ExtractionResponse.fromJson(Map<String, dynamic> json) {
    return ExtractionResponse(
      status: json['status'] ?? '',
      id: json['id'] ?? '',
      processingTimeMs: json['processingTimeMs'] ?? 0,
      totalExtracted: json['totalExtracted'] ?? 0,
      results: (json['results'] as List<dynamic>? ?? [])
          .map((item) => ExtractedItem.fromJson(item))
          .toList(),
    );
  }
}

class ExtractedItem {
  final String detectedText;
  final String category;
  final String subCategory;
  final int startIndex;
  final int endIndex;
  final double confidenceScore;
  final Map<String, dynamic> metadata;

  ExtractedItem({
    required this.detectedText,
    required this.category,
    required this.subCategory,
    required this.startIndex,
    required this.endIndex,
    required this.confidenceScore,
    required this.metadata,
  });

  factory ExtractedItem.fromJson(Map<String, dynamic> json) {
    return ExtractedItem(
      detectedText: json['detectedText'] ?? '',
      category: json['category'] ?? '',
      subCategory: json['subCategory'] ?? '',
      startIndex: json['startIndex'] ?? 0,
      endIndex: json['endIndex'] ?? 0,
      confidenceScore: (json['confidenceScore'] as num?)?.toDouble() ?? 0.0,
      metadata: Map<String, dynamic>.from(json['metadata'] ?? {}),
    );
  }
}
""".trimIndent()
}
