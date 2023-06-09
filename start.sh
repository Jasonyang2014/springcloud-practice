#!/bin/bash
set -e
BASE_DIR="$HOME/devtools"
NACOS_DIR="$BASE_DIR/nacos"
SEATA_DIR="$BASE_DIR/seata"
SENTINEL_DIR="$BASE_DIR/sentinel"

PID=
P_NAME=
RUNNING_FLAG=0

# help usage
usage() {
  echo "Usage: $0 [-m <mode>] [-s <service>] [-t <service>]"
  echo "Options:"
  echo "  -m <mode>       (start/stop) all services(nacos,seata,sentinel)"
  echo "  -s <service>    Specify the service nacos|seata|sentinel to start"
  echo "  -t <service>    Specify the service nacos|seata|sentinel to stop"
}

#usage
# 获取PID
get_pid() {
  PID=$(ps -ef | grep $P_NAME | grep -v grep | awk '{print $2}')
}
# 判断是否在运行
is_running() {
  get_pid
  if [ -n "$PID" ]; then
    RUNNING_FLAG=1
    echo "$P_NAME is running. pid is $PID"
  else
    echo "$P_NAME starts run"
  fi
}

start_nacos() {
  P_NAME="nacos.nacos"
  is_running
  if [ $RUNNING_FLAG != 1 ]; then
    $NACOS_DIR/bin/startup.sh -m standalone
  fi
}

start_seata() {
  P_NAME="seata-server"
  is_running
  if [ $RUNNING_FLAG != 1 ]; then
    $SEATA_DIR/bin/seata-server.sh
  fi
}
start_sentinel() {
  P_NAME="sentinel-dashboard"
  is_running
  if [ $RUNNING_FLAG != 1 ]; then
    nohup java -jar $SENTINEL_DIR/sentinel-dashboard-1.8.6.jar 2>&1 >> $BASE_DIR/sentinel.log &
  fi
}

stop_nacos() {
  $NACOS_DIR/bin/shutdown.sh
}

stop_seata() {
  P_NAME="seata-server"
  get_pid
  if [ -n "$PID" ]; then
    echo "stop $P_NAME."
    kill $PID
    echo "send signal to $P_NAME($PID) ok."
  else
    echo "$P_NAME is already stopped."
  fi
}

stop_sentinel() {
  P_NAME="sentinel-dashboard"
  get_pid
  if [ -n "$PID" ]; then
    echo "stop $P_NAME."
    kill $PID
    echo "send signal to $P_NAME($PID) ok."
  else
    echo "$P_NAME is already stopped."
  fi
}

while getopts ":m:s:t:" opt; do
  case $opt in
  "m")
    case $OPTARG in
    "start")
      shift $((OPTIND - 1))
      start_nacos
      sleep 1
      start_seata
      sleep 1
      start_sentinel
      ;;
    "stop")
      shift $((OPTIND - 1))
      stop_nacos
      stop_seata
      stop_sentinel
      ;;
    *)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
    esac
    ;;
  "s")
    case $OPTARG in
    "nacos")
      shift $((OPTIND - 1))
      start_nacos
      ;;
    "seata")
      shift $((OPTIND - 1))
      start_seata
      ;;
    "sentinel")
      shift $((OPTIND - 1))
      start_sentinel
      ;;
    *)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
    esac
    ;;
  "t")
    case $OPTARG in
    "nacos")
      stop_nacos
      ;;
    "seata")
      stop_seata
      ;;
    "sentinel")
      stop_sentinel
      ;;
    *)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
    esac
    ;;
  *)
    echo "Invalid option: -$OPTARG"
    usage
    exit 1
    ;;
  esac
done
