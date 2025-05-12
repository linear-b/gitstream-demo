FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src
COPY . .
RUN dotnet restore Billing.csproj && dotnet restore Billing.Tests.csproj
RUN dotnet test Billing.Tests.csproj --no-restore --logger "trx;LogFileName=test_results.trx"
RUN dotnet publish Billing.csproj -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "Billing.dll"]