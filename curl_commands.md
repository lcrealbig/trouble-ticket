# Trouble Ticket API - Simple cURL Commands

Below are the cURL commands you can copy and paste directly into your terminal or browser-based terminal.

## 1. Get JWT Token (Basic Auth)
```bash
curl -X POST "http://localhost:8080/auth/token" \
  -H "Authorization: Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==" \
  -H "Content-Type: application/json"
```

## 2. Get All Trouble Tickets
```bash
curl -X GET "http://localhost:8080/api/v1/troubleTicket" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

## 3. Get Specific Trouble Ticket
```bash
curl -X GET "http://localhost:8080/api/v1/troubleTicket/TICKET-001" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 4. Create New Trouble Ticket
```bash
curl -X POST "http://localhost:8080/troubleTicket" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "TICKET-001",
    "serviceId": 1,
    "description": "Created from cURL",
    "status": "new",
    "note": "Test note from cURL"
  }'
```

## 5. Create Note for Ticket
```bash
curl -X POST "http://localhost:8080/api/v1/troubleTicket/TICKET-001/note" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "UPDATED NOTE"
  }'
```

## 6. Close Trouble Ticket (Change Status to Closed)
```bash
curl -X PATCH "http://localhost:8080/api/v1/troubleTicket/TICKET-001" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRJZCI6InVzZXJAZXhhbXBsZS5jb20iLCJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzgyNjYzNzcyLCJleHAiOjE3ODI3NTAxNzJ9.OOXqbTiGcDHvjs4MgHdSEdb44k_rTEObTFfzyZGe9iI" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "closed"
  }'
```

## 7. Get All Trouble Tickets (Summary View)
```bash
curl -X GET "http://localhost:8080/api/v1/troubleTicket" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRJZCI6InVzZXJAZXhhbXBsZS5jb20iLCJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzgyNjYzNzcyLCJleHAiOjE3ODI3NTAxNzJ9.OOXqbTiGcDHvjs4MgHdSEdb44k_rTEObTFfzyZGe9iI"
```

## Usage Instructions

1. **First**, run the JWT token command (command #1) and copy the token from the response
2. **Replace** all JWT tokens in commands #2-#7 with your actual token
3. **For commands that require ticket IDs** (#3, #5, #6), replace `TICKET-001` with an actual ticket ID
4. **For ticket creation** (#4), ensure `externalId` is unique and `status` is "new"
5. **For closing tickets** (#6), only "closed" status is allowed
6. **Copy and paste** each command individually into your terminal

