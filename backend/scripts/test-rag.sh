#!/bin/bash

BASE_URL="http://localhost:8080"

PASS=0
FAIL=0

pass() {
    echo "[PASS] $1"
    PASS=$((PASS + 1))
}

fail() {
    echo "[FAIL] $1"
    FAIL=$((FAIL + 1))
}

echo "================================"
echo "        QuadGPT RAG Tests"
echo "================================"
echo

# --------------------------------------------------
# 1. Create test users
# --------------------------------------------------

TIMESTAMP=$(date +%s)
PASSWORD="password123"

IT_EMAIL="rag-it-$TIMESTAMP@example.com"
RH_EMAIL="rag-rh-$TIMESTAMP@example.com"
DIRECTION_EMAIL="rag-direction-$TIMESTAMP@example.com"

register_user() {
    local email="$1"
    local department="$2"

    curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\",\"department\":\"$department\"}"
}

IT_STATUS=$(register_user "$IT_EMAIL" "IT")
RH_STATUS=$(register_user "$RH_EMAIL" "RH")
DIRECTION_STATUS=$(register_user "$DIRECTION_EMAIL" "DIRECTION")

if [ "$IT_STATUS" = "200" ] && \
   [ "$RH_STATUS" = "200" ] && \
   [ "$DIRECTION_STATUS" = "200" ]; then
    pass "Create test users"
else
    fail "Create test users"
fi

# --------------------------------------------------
# 2. Login users
# --------------------------------------------------

login_user() {
    local email="$1"

    curl -s \
        -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}"
}

IT_LOGIN=$(login_user "$IT_EMAIL")
RH_LOGIN=$(login_user "$RH_EMAIL")
DIRECTION_LOGIN=$(login_user "$DIRECTION_EMAIL")

IT_TOKEN=$(echo "$IT_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
RH_TOKEN=$(echo "$RH_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
DIRECTION_TOKEN=$(echo "$DIRECTION_LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -n "$IT_TOKEN" ] && \
   [ -n "$RH_TOKEN" ] && \
   [ -n "$DIRECTION_TOKEN" ]; then
    pass "Login test users"
else
    fail "Login test users"
fi

# --------------------------------------------------
# 3. Ingest document for IT + DIRECTION
# --------------------------------------------------

DOCUMENT_NAME="rag-multi-department-$TIMESTAMP"

INGEST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/rag/ingest" \
    -H "Authorization: Bearer $IT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\":\"$DOCUMENT_NAME\",
        \"departments\":[\"IT\",\"DIRECTION\"],
        \"text\":\"QuadGPT est une plateforme de chat souveraine pour les entreprises. Elle permet de rechercher des informations dans des documents internes.\"
    }")

if [ "$INGEST_STATUS" = "200" ]; then
    pass "Multi-department document ingestion"
else
    fail "Multi-department document ingestion (HTTP $INGEST_STATUS)"
fi

# --------------------------------------------------
# 4. IT can access the document
# --------------------------------------------------

IT_CHAT=$(curl -s \
    -X POST "$BASE_URL/api/chat" \
    -H "Authorization: Bearer $IT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "message":"Que permet de faire QuadGPT ?"
    }')

if [ -n "$IT_CHAT" ] && \
   [ "$IT_CHAT" != "Aucun document pertinent trouvé pour répondre à cette question." ]; then
    pass "IT can access the document"
else
    fail "IT can access the document"
fi

# --------------------------------------------------
# 5. DIRECTION can access the document
# --------------------------------------------------

DIRECTION_CHAT=$(curl -s \
    -X POST "$BASE_URL/api/chat" \
    -H "Authorization: Bearer $DIRECTION_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "message":"Que permet de faire QuadGPT ?"
    }')

if [ -n "$DIRECTION_CHAT" ] && \
   [ "$DIRECTION_CHAT" != "Aucun document pertinent trouvé pour répondre à cette question." ]; then
    pass "DIRECTION can access the document"
else
    fail "DIRECTION can access the document"
fi

# --------------------------------------------------
# 6. RH cannot access the document
# --------------------------------------------------

RH_CHAT=$(curl -s \
    -X POST "$BASE_URL/api/chat" \
    -H "Authorization: Bearer $RH_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "message":"Que permet de faire QuadGPT ?"
    }')

if [ "$RH_CHAT" = '{"response":"Aucun document pertinent trouvé pour répondre à cette question."}' ]; then
    pass "RH cannot access the document"
else
    fail "RH cannot access the document"
fi

# --------------------------------------------------
# 7. Client cannot override department
# --------------------------------------------------

IT_FAKE_DEPARTMENT=$(curl -s \
    -X POST "$BASE_URL/api/chat" \
    -H "Authorization: Bearer $IT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "message":"Que permet de faire QuadGPT ?",
        "department":"RH"
    }')

if [ -n "$IT_FAKE_DEPARTMENT" ] && \
   [ "$IT_FAKE_DEPARTMENT" != '{"response":"Aucun document pertinent trouvé pour répondre à cette question."}' ]; then
    pass "Client cannot override department"
else
    fail "Client cannot override department"
fi

# --------------------------------------------------
# Summary
# --------------------------------------------------

echo
echo "================================"
echo "Results"
echo "================================"
echo "Passed: $PASS"
echo "Failed: $FAIL"
echo

if [ "$FAIL" -eq 0 ]; then
    echo "All RAG tests passed!"
    exit 0
else
    echo "Some RAG tests failed."
    exit 1
fi