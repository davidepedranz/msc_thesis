#
# Copyright (c) 2018 Davide Pedranz. All rights reserved.
#
# This code is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

.PHONY: help setup compile simulate
.ONESHELL:

SHELL=/usr/local/bin/bash

SIMULATOR_BIN = simulator/build/libs/simulator.jar
SIMULATOR_CONFIG_BASE = simulator/src/main/resources/config
CONFIG = bitcoin_balance_attack.cfg

CLUSTER = cluster

help:
	@echo ""
	@echo "Usage: make [command] [CONFIG=bitcoin_balance_attack.txt]"
	@echo ""
	@echo "The commands are:"
	@echo ""
	@echo "       setup       install the Python dependencies for the analysis"
	@echo "       compile     compile the Java simulator"
	@echo "       simulate    run the simulations"
	@echo "       help        prints this help message"
	@echo ""

setup:
	@echo "Install analysis/requirements.txt"
	@( \
    	source analysis/venv/bin/activate; \
    	pip install -r analysis/requirements.txt; \
    )

compile:
	@echo "[task]: compile"
	@(cd simulator; ./gradlew shadowJar)
	@echo ""

simulate-remote: compile
	$(eval date := $(shell date '+%Y-%m-%d-%H:%M:%S'))
	$(eval commit := $(shell git rev-parse HEAD))
	$(eval status := $(shell [[ -n `git status --porcelain` ]] && echo "dirty" || echo ""))
	$(eval logs_directory := $(shell expr `find logs/run-* -d | tail -1 | cut -d'-' -f2` + 1 | xargs printf 'logs/run-%03d'))
	$(eval info := $(logs_directory)/info.txt)
	$(eval config_src := $(SIMULATOR_CONFIG_BASE)/$(CONFIG))
	$(eval config_dest := $(logs_directory)/config.cfg)
	$(eval simulator := $(logs_directory)/simulator.jar)
	@mkdir -p $(logs_directory)
	@echo "date=$(date)" >> $(info)
	@echo "commit=$(commit)" >> $(info)
	@echo "status=$(status)" >> $(info)
	@cp $(config_src) $(config_dest)
	@cp $(SIMULATOR_BIN) $(simulator)
	@echo "Content of the ssh login file: $(CLUSTER)"
	@cat $(CLUSTER)
	@echo ""
	@echo "Ready to start the simulation in parallel on multiple machines!!!"
	@echo ""
	java -cp $(simulator) peersim.parallelsim.ParallelSimulator $(config_dest) 2> $(logs_directory)/parallel.txt | \
		sed 's|.*|java -cp $(simulator) \0|g' > $(logs_directory)/jobs_raw.txt
	cat $(logs_directory)/jobs_raw.txt | \
		tr \' \" | awk 'BEGIN{FS=OFS="\""} {for(i=2;i<NF;i+=2)gsub(" ","\\ ",$$i)} 1' | \
		sed 's/\"//g' > $(logs_directory)/jobs.txt
	# cat $(logs_directory)/jobs.txt | parallel --will-cite --joblog $(logs_directory)/joblog.txt --sshloginfile nodefile --workdir thesis --basefile $(simulator) --basefile $(config_dest) -k --progress
	cat $(logs_directory)/jobs.txt | { { parallel --halt soon,fail=40% --will-cite --joblog $(logs_directory)/joblog.txt --sshloginfile $(CLUSTER) --workdir thesis --basefile $(simulator) --basefile $(config_dest) -k | sed 's/.$$//g' > $(logs_directory)/stdout.txt; } 2>&1 1>&3 | tee -a $(logs_directory)/stderr.txt | grep --line-buffered 'Simulator:' ; } 3>&1 1>&2
	@echo ""
	@echo "The results are available at: $(logs_directory)"
	@echo "  > open $(logs_directory)"
	@echo ""

simulate: compile
	$(eval date := $(shell date '+%Y-%m-%d-%H:%M:%S'))
	$(eval commit := $(shell git rev-parse HEAD))
	$(eval status := $(shell [[ -n `git status --porcelain` ]] && echo "dirty" || echo ""))
	$(eval logs_directory := $(shell expr `find logs/run-* -d | tail -1 | cut -d'-' -f2` + 1 | xargs printf 'logs/run-%03d'))
	$(eval info := $(logs_directory)/info.txt)
	$(eval config_src := $(SIMULATOR_CONFIG_BASE)/$(CONFIG))
	$(eval config_dest := $(logs_directory)/config.cfg)
	$(eval simulator := $(logs_directory)/simulator.jar)
	@echo "[task]: simulate -> config_file = $(config_src)"
	@mkdir -p $(logs_directory)
	@echo "date=$(date)" >> $(info)
	@echo "commit=$(commit)" >> $(info)
	@echo "status=$(status)" >> $(info)	
	@cp $(config_src) $(config_dest)
	@cp $(SIMULATOR_BIN) $(simulator)
	@java -cp $(simulator) peersim.parallelsim.ParallelSimulator $(config_dest) 2> $(logs_directory)/parallel.txt | \
		sed 's|.*|echo "[EXPERIMENT]: \0" 1>\&2 \&\& java -cp $(simulator) \0|g' | \
		{ { parallel -j 7 -k --joblog $(logs_directory)/joblog.txt --will-cite | sed 's/.$$//g' > $(logs_directory)/stdout.txt; } 2>&1 1>&3 | tee -a $(logs_directory)/stderr.txt | grep --line-buffered 'EXPERIMENT' | sed "s/.*' //"; } 3>&1 1>&2
	@echo "[task]: analyze the simulation results"
	@( \
    	source analysis/venv/bin/activate; \
    	python analysis/all.py $(logs_directory)/; \
    )
	@echo ""
	@echo "The results are available at: $(logs_directory)"
	@echo "  > open $(logs_directory)"
	@echo ""

simulate-sequentially: compile
	$(eval date := $(shell date '+%Y-%m-%d-%H:%M:%S'))
	$(eval commit := $(shell git rev-parse HEAD))
	$(eval status := $(shell [[ -n `git status --porcelain` ]] && echo "dirty" || echo ""))
	$(eval logs_directory := $(shell expr `find logs/run-* -d | tail -1 | cut -d'-' -f2` + 1 | xargs printf 'logs/run-%03d'))
	$(eval info := $(logs_directory)/info.txt)
	$(eval config_src := $(SIMULATOR_CONFIG_BASE)/$(CONFIG))
	$(eval config_dest := $(logs_directory)/config.cfg)
	$(eval simulator := $(logs_directory)/simulator.jar)
	@echo "[task]: simulate -> config_file = $(config_src)"
	@mkdir -p $(logs_directory)
	@echo "date=$(date)" >> $(info)
	@echo "commit=$(commit)" >> $(info)
	@echo "status=$(status)" >> $(info)
	@cp $(config_src) $(config_dest)
	@cp $(SIMULATOR_BIN) $(simulator)
	@time java -jar $(simulator) $(config_dest) > $(logs_directory)/stdout.txt 2> >(tee -a $(logs_directory)/stderr.txt | grep 'Experiment' >&2) 
	@echo ""
	@echo "[task]: analyze the simulation results"
	@( \
    	source analysis/venv/bin/activate; \
    	python analysis/all.py $(logs_directory)/; \
    )
	@echo ""
	@echo "The results are available at: $(logs_directory)"
	@echo "  > open $(logs_directory)"
	@echo ""
