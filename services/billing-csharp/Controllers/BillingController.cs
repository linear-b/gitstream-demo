using Microsoft.AspNetCore.Mvc;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using System;
using System.Collections.Generic;

public class ChargeRequest
{
    [JsonPropertyName("username")]
    public string Username { get; set; }

    [JsonPropertyName("productId")]
    public string ProductId { get; set; }

    [JsonPropertyName("quantity")]
    public int Quantity { get; set; }

    // ISO-8601 timestamp of when the charge was requested
    [JsonPropertyName("date")]
    public DateTime Date { get; set; }
}

[ApiController]
[Route("billing")]
public class BillingController : ControllerBase
{
    private static readonly List<object> responseHistory = new List<object>();
    private readonly string EXPECTED_SECRET = Environment.GetEnvironmentVariable("BILLING_SECRET");

    [HttpPost("charge")]
    public async Task<IActionResult> Charge([FromBody] ChargeRequest request)
    {
        var receivedSecret = Request.Headers["X-Service-Secret"].ToString();
        if (string.IsNullOrEmpty(receivedSecret) || receivedSecret != EXPECTED_SECRET)
            return Unauthorized("Missing or invalid service secret");

        if (string.IsNullOrEmpty(request.Username) || string.IsNullOrEmpty(request.ProductId) || request.Quantity <= 0)
            return BadRequest("Invalid request payload");

        var responsePayload = new
        {
            status = "charged",
            user = request.Username,
            product = request.ProductId,
            quantity = request.Quantity,
            data = request.Date.ToString("o") // ISO-8601 format
        };

        responseHistory.Add(responsePayload);

        return Ok(JsonSerializer.Serialize(responsePayload));
    }
}
