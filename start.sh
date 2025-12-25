#!/bin/sh
python -m venv ./venv
pip install -r requirements.txt
docker-compose up -d
python pmu_producer.py
