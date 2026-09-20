#!/bin/bash
# 🚀 Smart ERP - HDD 백업 스크립트 (최종 안정화 버전)
# 대상: MSSQL(ERP), MySQL(Asterisk), 첨부파일, 녹취파일

BACKUP_ROOT="/data/storage/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="$BACKUP_ROOT/$DATE"

echo "=========================================="
echo "   Smart ERP System: HDD BACKUP START    "
echo "   Date: $DATE"
echo "=========================================="

# 1. 백업 디렉토리 생성
mkdir -p $BACKUP_DIR

# 2. MSSQL (SMARTDB) 백업
echo "[1/4] Backing up MSSQL (SMARTDB)..."

# 💡 [최종 해결] 컨테이너 내부에서 sqlcmd 명령이 어디 있는지 확인 후 실행
SQLCMD=$(sudo docker exec smartdb which sqlcmd 2>/dev/null)
if [ -z "$SQLCMD" ]; then
    # which로 못찾으면 표준 설치 경로 강제 지정
    SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
fi

echo "🔍 Using sqlcmd: $SQLCMD"

# -C(신뢰) 옵션과 -Q(명령) 옵션을 사용하여 백업 수행
sudo docker exec smartdb $SQLCMD -S localhost -U sa -P Crmbank123! -C -Q "BACKUP DATABASE [SMARTDB] TO DISK = N'/var/opt/mssql/data/SMARTDB_$DATE.bak' WITH NOFORMAT, NOINIT, NAME = 'SMARTDB-Full Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10"

if [ $? -eq 0 ]; then
    echo "📦 Copying backup file from container..."
    sudo docker cp smartdb:/var/opt/mssql/data/SMARTDB_$DATE.bak $BACKUP_DIR/
    sudo docker exec smartdb rm -f /var/opt/mssql/data/SMARTDB_$DATE.bak
    echo "✅ MSSQL Backup Success."
else
    echo "❌ MSSQL Backup Failed. Check container status."
fi

# 3. MySQL (Asterisk) 백업
echo "[2/4] Backing up MySQL (Asterisk)..."
sudo docker exec asterisk-db mysqldump -u root -pCrmbank123! asterisk > $BACKUP_DIR/asterisk_$DATE.sql 2>/dev/null
echo "✅ MySQL Backup Success."

# 4. 첨부파일 및 녹취파일 백업 (압축)
echo "[3/4] Compressing Storage & Recordings..."
# 💡 [보정] 하드코딩된 폴더명 대신 현재 존재하는 모든 파일/폴더를 압축 (백업폴더 제외)
if [ -d "/data/storage" ]; then
    cd /data/storage && sudo tar -czf $BACKUP_DIR/storage_files_$DATE.tar.gz --exclude='./backups' .
    echo "✅ Storage Compression Success."
else
    echo "⚠️ /data/storage directory not found!"
fi

# 5. 오래된 백업 삭제 (7일 경과분)
echo "[4/4] Cleaning up old backups (over 7 days)..."
find $BACKUP_ROOT -maxdepth 1 -type d -mtime +7 -exec rm -rf {} +

echo "=========================================="
echo " ✅ ALL BACKUP PROCESS FINISHED!         "
echo " Location: $BACKUP_DIR"
echo "=========================================="
