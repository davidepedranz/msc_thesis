.PHONY: help compile simulate
.ONESHELL:

SHELL=/bin/bash

SIMULATOR_BIN = simulator/build/libs/simulator.jar
SIMULALATOR_CONFIG = simulator/src/main/resources/config/bitcoin_all.txt

help:
	@echo ""
	@echo "Usage: make [command]"
	@echo ""
	@echo "The commands are:"
	@echo ""
	@echo "       compile     compile the Java simulator"
	@echo "       simulate    run the simulations"
	@echo "       help        prints this help message"
	@echo ""

compile:
	@echo "[task]: compile"
	(cd simulator; ./gradlew shadowJar)
	@echo ""

simulate: compile
	@echo "[task]: simulate"
	$(eval date := $(shell date '+%Y-%m-%d-%H:%M:%S'))
	$(eval commit := $(shell git rev-parse HEAD))
	$(eval status := $(shell [[ -n `git status --porcelain` ]] && echo "dirty" || echo ""))
	$(eval logs_directory := $(shell expr `find logs/run-* -d | tail -1 | cut -d'-' -f2` + 1 | xargs printf 'logs/run-%03d'))
	$(eval info := $(logs_directory)/info.txt)
	$(eval config := $(logs_directory)/config.cfg)
	@mkdir -p $(logs_directory)
	@echo "date=$(date)" >> $(info)
	@echo "commit=$(commit)" >> $(info)
	@echo "status=$(status)" >> $(info)
	cp $(SIMULALATOR_CONFIG) $(config)
	java -jar $(SIMULATOR_BIN) $(config) > $(logs_directory)/stdout.txt 2> >(tee -a $(logs_directory)/stderr.txt | grep 'Experiment' >&2) 
	@echo ""
