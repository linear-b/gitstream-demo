using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using Xunit;
using System.Text.Json;
using System.Threading.Tasks;

public class BillingControllerTests
{
    [Fact]
    public async Task ReturnsOkWithValidPayload()
    {
        System.Environment.SetEnvironmentVariable("BILLING_SECRET", "topsecretvalue");

        var controller = new BillingController();
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext()
        };
        controller.Request.Headers["X-Service-Secret"] = "topsecretvalue";

        var result = await controller.Charge(new ChargeRequest
        {
            Username = "alice",
            ProductId = "widget-123",
            Quantity = 1
        });

        var okResult = Assert.IsType<OkObjectResult>(result);
        var json = okResult.Value as string;
        Assert.NotNull(json);

        var doc = JsonSerializer.Deserialize<JsonElement>(json);
        Assert.Equal("charged", doc.GetProperty("status").GetString());
        Assert.Equal("alice", doc.GetProperty("user").GetString());
        Assert.Equal("widget-123", doc.GetProperty("product").GetString());
        Assert.Equal(1, doc.GetProperty("quantity").GetInt32());
    }

    [Fact]
    public async Task ReturnsUnauthorizedWithoutSecret()
    {
        var controller = new BillingController();
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext()
        };
        controller.Request.Headers["X-Service-Secret"] = "wrongsecret";

        var result = await controller.Charge(new ChargeRequest
        {
            Username = "bob",
            ProductId = "widget-123",
            Quantity = 1
        });

        Assert.IsType<UnauthorizedObjectResult>(result);
    }

    [Fact]
    public async Task ReturnsBadRequestWithMissingFields()
    {
        System.Environment.SetEnvironmentVariable("BILLING_SECRET", "topsecretvalue");

        var controller = new BillingController();
        controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext()
        };
        controller.Request.Headers["X-Service-Secret"] = "topsecretvalue";

        var result = await controller.Charge(new ChargeRequest
        {
            Username = "",
            ProductId = "",
            Quantity = 0
        });

        Assert.IsType<BadRequestObjectResult>(result);
    }
}
