#!/bin/bash

set -e

echo "=================================="
echo "Running All Bug-Fix Casebook Tests"
echo "=================================="
echo ""

FAILED_CASES=()
PASSED_CASES=()

run_case() {
    local case_name=$1
    local case_dir=$2
    
    echo "▶ Testing: $case_name"
    echo "-----------------------------------"
    
    if [ -d "$case_dir/backend" ]; then
        cd "$case_dir/backend"
        if mvn test -q; then
            echo "✅ $case_name: PASSED"
            PASSED_CASES+=("$case_name")
        else
            echo "❌ $case_name: FAILED"
            FAILED_CASES+=("$case_name")
        fi
        cd - > /dev/null
    else
        echo "⚠️  $case_name: No backend found, skipping"
    fi
    
    echo ""
}

# Run each case
run_case "Case 01: Timezone Bug" "case-01-timezone-bug"
run_case "Case 02: N+1 Query Problem" "case-02-n-plus-one"
run_case "Case 03: Missing Pagination" "case-03-missing-pagination"
run_case "Case 04: Race Condition" "case-04-race-condition"
run_case "Case 05: Angular Memory Leak" "case-05-angular-memory-leak"
run_case "Case 06: Validation Handling" "case-06-validation-handling"

# Summary
echo "=================================="
echo "Summary"
echo "=================================="
echo "Passed: ${#PASSED_CASES[@]}"
echo "Failed: ${#FAILED_CASES[@]}"

if [ ${#FAILED_CASES[@]} -eq 0 ]; then
    echo ""
    echo "🎉 All tests passed!"
    exit 0
else
    echo ""
    echo "❌ Failed cases:"
    for case in "${FAILED_CASES[@]}"; do
        echo "  - $case"
    done
    exit 1
fi
