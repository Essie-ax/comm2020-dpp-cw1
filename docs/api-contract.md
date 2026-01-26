# API Contract v0.1 (CW1)

Base URL: `/api`  
Content-Type: `application/json`

## Common Response Shape

Success:
```json
{ "success": true, "data": {} }
{ "success": false, "error": { "code": "STRING", "message": "STRING", "details": {} } }
