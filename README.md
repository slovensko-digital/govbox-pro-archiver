# GovBox Pro - Archiver

The application is based on the Autogram project.

Runs on port `8720` by default.

## Docker

```
docker build -t archiver .
docker run -p8720:8720 archiver
```


## Endpoints

1. `POST /validate` - takes an AdES signed file and returns validation report regarding signatures and timestamps.
2. `POST /extend` - takes an AdES signed file and return the same file extended to BASELINE_LTA level. If the file doesn't already contain signature timestamps Archiver will add them before adding archive timestamps.
3. `GET /health` - return some status information about the server.
4. `GET /docs` - serves Swagger with Open API definition.

