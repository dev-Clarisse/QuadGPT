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
# 1. Create test user
# --------------------------------------------------

TEST_EMAIL="ragtest-$(date +%s)@example.com"
TEST_PASSWORD="password123"

REGISTER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [ "$REGISTER_STATUS" = "200" ]; then
    pass "Create test user"
else
    fail "Create test user (HTTP $REGISTER_STATUS)"
fi

# --------------------------------------------------
# 2. Login
# --------------------------------------------------

LOGIN_RESPONSE=$(curl -s \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

if [ -n "$TOKEN" ]; then
    pass "Login"
else
    fail "Login"
fi

# --------------------------------------------------
# 3. Ingest document
# --------------------------------------------------

DOCUMENT_NAME="rag-test-$(date +%s)"
DEPARTMENT="informatique"

INGEST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/rag/ingest" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\":\"$DOCUMENT_NAME\",
        \"department\":\"$DEPARTMENT\",
        \"text\":\"QuadGPT est une plateforme de chat souveraine pour les entreprises. Elle permet de rechercher des informations dans des documents internes.\"
    }")

if [ "$INGEST_STATUS" = "200" ]; then
    pass "Document ingestion"
else
    fail "Document ingestion (HTTP $INGEST_STATUS)"
fi

# --------------------------------------------------
# 4. Ask question about document
# --------------------------------------------------

RAG_RESPONSE=$(curl -s \
    -X POST "$BASE_URL/api/rag/ask" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"question\":\"Que permet de faire QuadGPT ?\",
        \"department\":\"$DEPARTMENT\"
    }")

if [ -n "$RAG_RESPONSE" ] && \
   [ "$RAG_RESPONSE" != "Aucun document pertinent trouvé pour répondre à cette question." ]; then
    pass "RAG question answered"
else
    fail "RAG question answered"
fi

# --------------------------------------------------
# 5. Department filtering
# --------------------------------------------------

OTHER_DEPARTMENT_RESPONSE=$(curl -s \
    -X POST "$BASE_URL/api/rag/ask" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "question":"Que permet de faire QuadGPT ?",
        "department":"ressources-humaines"
    }')

EXPECTED_EMPTY="Aucun document pertinent trouvé pour répondre à cette question."

if [ "$OTHER_DEPARTMENT_RESPONSE" = "$EXPECTED_EMPTY" ]; then
    pass "Department filtering"
else
    fail "Department filtering"
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
