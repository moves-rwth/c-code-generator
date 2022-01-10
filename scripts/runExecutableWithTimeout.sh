#!/bin/bash

timeout --kill-after=35s 30s $1
retcode="$?"
echo "Return code was: $retcode"

if [ "$retcode" = "124" ]; then
	echo "Timeout";
	exit 0;
else
	echo "other cause";
	exit $retcode;
fi
