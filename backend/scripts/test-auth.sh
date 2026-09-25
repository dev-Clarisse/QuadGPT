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
echo "       QuadGPT Auth Tests"
echo "================================"
echo

# --------------------------------------------------
# 1. Register new user
# --------------------------------------------------

TEST_EMAIL="autotest@example.com"
TEST_PASSWORD="password123"

REGISTER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [ "$REGISTER_STATUS" = "200" ]; then
    pass "Register"
else
    fail "Register (HTTP $REGISTER_STATUS)"
fi

# --------------------------------------------------
# 2. Duplicate register
# --------------------------------------------------

DUPLICATE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if [ "$DUPLICATE_STATUS" = "409" ]; then
    pass "Duplicate register rejected"
else
    fail "Duplicate register (HTTP $DUPLICATE_STATUS)"
fi

# --------------------------------------------------
# 3. Login
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
# 4. Wrong password
# --------------------------------------------------

WRONG_PASSWORD_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"wrongpassword\"}")

if [ "$WRONG_PASSWORD_STATUS" = "401" ]; then
    pass "Wrong password rejected"
else
    fail "Wrong password (HTTP $WRONG_PASSWORD_STATUS)"
fi

# --------------------------------------------------
# 5. Unknown user
# --------------------------------------------------

UNKNOWN_USER_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"unknown@example.com","password":"password123"}')

if [ "$UNKNOWN_USER_STATUS" = "401" ]; then
    pass "Unknown user rejected"
else
    fail "Unknown user (HTTP $UNKNOWN_USER_STATUS)"
fi

# --------------------------------------------------
# 6. Protected route without token
# --------------------------------------------------

NO_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/test")

if [ "$NO_TOKEN_STATUS" = "403" ]; then
    pass "Protected route rejects unauthenticated request"
else
    fail "Protected route without token (HTTP $NO_TOKEN_STATUS)"
fi

# --------------------------------------------------
# 7. Invalid token
# --------------------------------------------------

INVALID_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/test" \
    -H "Authorization: Bearer invalid-token")

if [ "$INVALID_TOKEN_STATUS" = "403" ]; then
    pass "Invalid token rejected"
else
    fail "Invalid token (HTTP $INVALID_TOKEN_STATUS)"
fi

# --------------------------------------------------
# 8. Valid token
# --------------------------------------------------

VALID_TOKEN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/test" \
    -H "Authorization: Bearer $TOKEN")

if [ "$VALID_TOKEN_STATUS" = "200" ]; then
    pass "Valid JWT accepted"
else
    fail "Valid JWT (HTTP $VALID_TOKEN_STATUS)"
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
    echo "All authentication tests passed!"
    exit 0
else
    echo "Some authentication tests failed."
    exit 1
fi
