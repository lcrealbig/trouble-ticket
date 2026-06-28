# Trouble Ticket API Test with cURL

This markdown file contains executable cURL commands to test the Trouble Ticket API with JWT authentication.

## Prerequisites
- cURL installed
- jq installed (for JSON processing)
- API server running on localhost:8080

## Test Execution

### 1. Get JWT Token (Basic Auth)
```bash
# Get JWT token using Basic Auth
echo "Getting JWT token..."
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/token" \
  -H "Authorization: Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==" \
  -H "Content-Type: application/json")

# Verify token was received
if [ -z "$TOKEN" ]; then
  echo "ERROR: Failed to get JWT token"
fi

echo "JWT Token received: $TOKEN"
```

### 2. Get All Trouble Tickets
```bash
# Get all trouble tickets
echo "Getting all trouble tickets..."
curl -X GET "http://localhost:8080/troubleTicket" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
echo ""
```

### 3. Get Specific Trouble Ticket
```bash
# Get specific trouble ticket
echo "Getting specific trouble ticket (TICKET-001)..."
curl -X GET "http://localhost:8080/api/v1/troubleTicket/TICKET-001" \
  -H "Authorization: Bearer $TOKEN"
echo ""
```

### 4. Create New Trouble Ticket
```bash
# Create new trouble ticket
echo "Creating new trouble ticket..."
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/troubleTicket" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ0ZW5hbnRJZCI6InVzZXJAZXhhbXBsZS5jb20iLCJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzgyNjc2NDgzLCJleHAiOjE3ODI3NjI4ODN9.acg7GWawYWx9jDjifuZeUKc-w6s1RM5LAdQKRKvRY5I" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "TICKET-001",
    "serviceId": 1,
    "description": "Created from cURL",
    "status": "new",
    "note": "Test note from cURL"
  }')

# Extract created ticket ID
CREATED_TICKET_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
if [ "$CREATED_TICKET_ID" = "null" ]; then
  echo "ERROR: Failed to create ticket or extract ID"
  echo "Response: $CREATE_RESPONSE"
  exit 1
fi

echo "Created ticket with ID: $CREATED_TICKET_ID"
echo ""
```

### 5. Create Note for Ticket
```bash
# Create note for the created ticket
echo "Creating note for ticket $CREATED_TICKET_ID..."
curl -X POST "http://localhost:8080/api/v1/troubleTicket/$CREATED_TICKET_ID/note" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test note from cURL"
  }'
echo ""
```

## Complete Test Execution

To run all tests in sequence, execute this command:
```bash
# Run complete test sequence
chmod +x api_test_curl.md
./api_test_curl.md
```

Or run each section individually by copying the commands.