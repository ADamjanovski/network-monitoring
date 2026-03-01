#!/bin/sh
python -m venv ./venv
./venv/Scripts/pip install -r requirements.txt
docker-compose up -d
./venv/Scripts/python pmu_producer.py