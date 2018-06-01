#!/usr/bin/env bash
wget http://www.freepatentsonline.com/8591332.html -P ../data --accept-regex='/[0-9]{7}.html' -r -np --wait=1
