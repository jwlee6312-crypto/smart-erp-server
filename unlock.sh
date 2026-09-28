#!/bin/bash
echo "🚨 빌드 파일 잠금 해제 및 프로세스 강제 종료 중..."
pkill -f 'GradleDaemon'
pkill -f 'java'
sudo fuser -k 8080/tcp
sudo rm -rf /home/smart/smart-erp/erp-backend/build
echo "✅ 준비 완료! 이제 기존 가동 스크립트를 실행하세요."
