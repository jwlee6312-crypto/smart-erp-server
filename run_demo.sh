#!/bin/bash
# 🚀 Smart ERP - 통합 데모 가동 스크립트 (Timezone & TTS 보정판)
# 기능: 포트정리 -> 최신도커반영 -> 백엔드(Host) -> 프론트엔드(Host)

PROJECT_ROOT="/home/smart/smart-erp"

echo "=========================================="
echo "   Smart ERP System: FINAL RESTART       "
echo "=========================================="

# 1. 경로 이동
cd $PROJECT_ROOT

# 2. 기존 서비스 완전 종료
echo "[1/6] Stopping existing services..."
pkill -f 'erp-backend' 2>/dev/null
sudo pkill -f 'vite' 2>/dev/null
sudo fuser -k 80/tcp 2>/dev/null
sudo fuser -k 8080/tcp 2>/dev/null

echo "[2/6] Fixing directory permissions..."
sudo chmod -R 777 /var/lib/asterisk/sounds/custom
sudo chmod -R 777 /data/smart-erp/recordings

# 3. 도커 인프라 재기동 (최신 Timezone 설정 강제 반영)
echo "[3/6] Refreshing Infrastructure with Timezone Fix (Docker)..."
# 🚀 [해결] 백엔드와 프론트엔드 빌드를 무시하고, 필수 인프라만 띄웁니다.
sudo docker compose up -d --remove-orphans smartdb asterisk-db asterisk chatwoot-redis chatwoot-postgres chatwoot-web chatwoot-worker

# 4. 백엔드(Spring Boot) 클린 빌드 및 기동
echo "[4/6] Re-building Backend with KST fix..."
> $PROJECT_ROOT/backend_run.log
nohup ./gradlew clean :erp-backend:bootRun > $PROJECT_ROOT/backend_run.log 2>&1 &

# 5. 프론트엔드(Vite) 가동
echo "[5/6] Launching Frontend on Port 80..."
cd $PROJECT_ROOT/erp-frontend
> $PROJECT_ROOT/frontend_run.log
# 💡 [보정] 백엔드 부팅 시간을 확보하기 위해 3초 대기 후 시작
sleep 3
nohup sudo npm run dev -- --host 0.0.0.0 --port 80 > $PROJECT_ROOT/frontend_run.log 2>&1 &

# 6. Asterisk 최종 동기화
echo "[6/6] Finalizing engine synchronization..."
sleep 15
sudo docker exec asterisk asterisk -rx "manager reload"
sudo docker exec asterisk asterisk -rx "sip reload"
sudo docker exec asterisk asterisk -rx "dialplan reload"

echo "=========================================="
echo " ✅ SYSTEM RELAUNCHED SUCCESSFULLY!      "
echo " ⌚ Timezone: Asia/Seoul (KST) Synced    "
echo " 📱 External: smart-erp.duckdns.org:18888"
echo "=========================================="
