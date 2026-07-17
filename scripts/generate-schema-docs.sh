#!/usr/bin/env bash
# DB 스키마 문서(docs/schema) 재생성 스크립트
#
# 사용법: ./scripts/generate-schema-docs.sh
# 필요:   Docker 실행 중일 것 (tbls는 로컬 설치본이 핀 버전과 같을 때만 쓰고, 아니면 Docker 이미지로 실행)
#
# 동작: 임시 MySQL 8.0 컨테이너에 db/migration/prod 마이그레이션을 순서대로 적용한 뒤
#       tbls로 docs/schema 문서를 재생성한다. 스키마 변경 PR에는 이 산출물을 함께 커밋할 것.
set -euo pipefail
cd "$(dirname "$0")/.."

# tbls 버전 핀. CI(.github/workflows/schema-docs.yml)가 이 줄을 sed로 읽어 같은 버전을 설치하므로
# `TBLS_VERSION=<버전>` 형식을 유지할 것. 버전이 갈리면 산출물이 미묘하게 달라져 CI가 오탐한다.
TBLS_VERSION=v1.94.5
MIG_DIR=src/main/resources/db/migration/prod
CONTAINER=tbls-schema-mysql
PORT=33306

docker info >/dev/null 2>&1 || { echo "Docker를 먼저 실행해 주세요."; exit 1; }

docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
docker run -d --name "$CONTAINER" \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=hufscheer \
  -p "$PORT":3306 mysql:8.0 >/dev/null
trap 'docker rm -f "$CONTAINER" >/dev/null 2>&1' EXIT

echo "MySQL 기동 대기 중..."
until docker exec "$CONTAINER" mysqladmin ping -uroot -proot --silent >/dev/null 2>&1; do
  sleep 2
done

echo "마이그레이션 적용 중..."
for f in $(ls "$MIG_DIR" | grep '^V.*\.sql$' | sort -t_ -k1.2 -n); do
  echo "  - $f"
  docker exec -i "$CONTAINER" mysql -uroot -proot hufscheer < "$MIG_DIR/$f"
done

echo "tbls 문서 생성 중..."
LOCAL_TBLS=""
if command -v tbls >/dev/null 2>&1; then
  RAW_VERSION="$(tbls version 2>/dev/null || true)"
  if [ -n "$RAW_VERSION" ]; then
    LOCAL_TBLS="v${RAW_VERSION#v}" # tbls는 `1.94.5`로 출력하므로 v를 붙여 핀과 비교한다
  fi
fi

if [ "$LOCAL_TBLS" = "$TBLS_VERSION" ]; then
  echo "  로컬 tbls ${TBLS_VERSION} 사용"
  TBLS_DSN="mysql://root:root@127.0.0.1:${PORT}/hufscheer" tbls doc --force -c .tbls.yml
else
  if [ -n "$LOCAL_TBLS" ]; then
    echo "  로컬 tbls(${LOCAL_TBLS})가 핀 버전(${TBLS_VERSION})과 달라 Docker 이미지로 실행합니다."
  fi
  docker run --rm -v "$PWD":/work -w /work \
    --add-host=host.docker.internal:host-gateway \
    -e TBLS_DSN="mysql://root:root@host.docker.internal:${PORT}/hufscheer" \
    "ghcr.io/k1low/tbls:${TBLS_VERSION}" doc --force -c /work/.tbls.yml
fi

echo "완료: docs/schema/ 를 확인하세요."
