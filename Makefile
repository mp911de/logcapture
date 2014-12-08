PATH := ./work/redis-git/src:${PATH}

define REDIS_CONF
daemonize yes
port 6479
pidfile work/redis-6479.pid
logfile work/redis-6479.log
save ""
appendonly no
client-output-buffer-limit pubsub 256k 128k 5
endef

export REDIS_CONF

start: cleanup
	echo "$$REDIS_CONF" > work/redis-6479.conf && redis-server work/redis-6479.conf

cleanup: stop
	- mkdir -p work
	rm -f work/redis-cluster-node*.conf 2>/dev/null
	rm -f work/dump.rdb work/appendonly.aof work/*.conf work/*.log 2>/dev/null

stop:
	pkill redis-server || true
	sleep 2
	rm -f work/dump.rdb work/appendonly.aof work/*.conf work/*.log || true
	rm -f *.aof
	rm -f *.rdb

test:
	make start
	sleep 2
	mvn -B -DskipTests=false clean compile test
	make stop

travis-install:
	pkill redis-server || true
	[ ! -e work/redis-git ] && git clone https://github.com/antirez/redis.git --branch 3.0 --single-branch work/redis-git && cd work/redis-git|| true
	[ -e work/redis-git ] && cd work/redis-git && git reset --hard && git pull && git checkout 3.0 || true
	make -C work/redis-git clean
	make -C work/redis-git -j4

clean:
	rm -Rf work/
	rm -Rf target/

release:
	mvn release:clean
	mvn release:prepare -Psonatype-oss-release
	mvn release:perform -Psonatype-oss-release
	cd target/checkout
	cd target
	gpg -b -a *-bin.zip
	gpg -b -a *-bin.tar.gz
	cd ..
	mvn site:site
	mvn -o scm-publish:publish-scm -Dgithub.site.upload.skip=false

.PHONY: test

