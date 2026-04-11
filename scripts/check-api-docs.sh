#!/bin/bash
# api.adoc에 문서화되지 않은 엔드포인트를 감지하는 스크립트
# 테스트 실행 후 build/generated-snippets/ 디렉토리와 api.adoc의 operation:: 참조를 비교합니다.

set -euo pipefail

SNIPPETS_DIR="build/generated-snippets"
ADOC_FILE="src/docs/asciidoc/api.adoc"

if [ ! -d "$SNIPPETS_DIR" ]; then
    echo "❌ 스니펫 디렉토리가 없습니다: $SNIPPETS_DIR"
    echo "   테스트를 먼저 실행해주세요."
    exit 1
fi

if [ ! -f "$ADOC_FILE" ]; then
    echo "❌ API 문서 파일이 없습니다: $ADOC_FILE"
    exit 1
fi

missing=()
total=0

for class_dir in "$SNIPPETS_DIR"/*/; do
    [ -d "$class_dir" ] || continue
    class_name=$(basename "$class_dir")

    for method_dir in "$class_dir"*/; do
        [ -d "$method_dir" ] || continue
        method_name=$(basename "$method_dir")
        operation="${class_name}/${method_name}"
        total=$((total + 1))

        if ! grep -qF "operation::${operation}[" "$ADOC_FILE"; then
            missing+=("$operation")
        fi
    done
done

if [ ${#missing[@]} -gt 0 ]; then
    echo "❌ api.adoc에 누락된 엔드포인트가 있습니다. (총 ${#missing[@]}개)"
    echo ""
    for op in "${missing[@]}"; do
        echo "  operation::${op}[snippets='...']"
    done
    echo ""
    echo "위 항목을 $ADOC_FILE 에 추가해주세요."
    exit 1
fi

echo "✅ 모든 엔드포인트가 api.adoc에 문서화되어 있습니다. (총 ${total}개)"