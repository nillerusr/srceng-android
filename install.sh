#!/bin/sh
echo "pm install -r bin/srceng-debug.apk"
ssh 127.0.0.1 "bash -c \"source /etc/bash/bashrc;cd /root/projects/srceng-android/bin;chmod 777 srceng-debug.apk;pm install -r srceng-debug.apk\""

