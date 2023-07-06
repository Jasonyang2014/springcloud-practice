#!/bin/bash
set -e

ROCKETMQ_DIR="$HOME/devtools/rocketmq"
echo "rocketmq home $ROCKETMQ_DIR."

step() {
	echo -n " "
	local i=$1
	num=1
	until [ $num -gt $i ]; do
		sleep 1
		echo -ne "."
		num=$((num + 1))
	done
}

#启动服务
start_server() {
	echo "Start Server."
	nohup bin/mqnamesrv &
}

#启动broker
start_broker() {
	nohup bin/mqbroker -n localhost:9876 --enable-proxy &
}

#启动server后启动broker
start_all() {
	start_server
	echo "Starting..."
	step 10
	start_broker
}

stop_broker() {
	echo "Shutdown Broker."
	bin/mqshutdown broker
}

stop_server() {
	echo "Shutdown Namesrv."
	bin/mqshutdown namesrv
}

stop_all() {
	stop_broker
	sleep 3
	stop_server
}

cd "$ROCKETMQ_DIR"

while getopts ":s:t:" opt; do
	case $opt in
	s)
		p=$OPTARG
		if [ "$p" == "all" ]; then
			start_all
		elif [ "$p" == "server" ]; then
			start_server
		elif [ "$p" == "broker" ]; then
			start_broker
		else
			echo "Invalid arg $p"
			exit 1
		fi
		;;
	t)
		p=$OPTARG
		if [ "$p" == "all" ]; then
			stop_all
		elif [ "$p" == "server" ]; then
			stop_broker
			stop_server
		elif [ "$p" == "broker" ]; then
			stop_broker
		else
			echo "Invalid arg $p"
			exit 1
		fi
		;;
	\?)
		echo "Invalid arg $OPTARG."
		;;
	esac
done

echo " Done."
