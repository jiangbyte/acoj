```bash
curl -X POST 'http://localhost:8848/nacos/v1/ns/service' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'serviceName=app-gateway&groupName=DEFAULT_GROUP&ephemeral=false'
```

```bash
curl -X POST 'http://localhost:8848/nacos/v1/ns/service' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'serviceName=app-core&groupName=DEFAULT_GROUP&ephemeral=false'
```