#!/bin/bash

set -e

rev=`git log -n 1 --format="%hH"`
[ "$rev" ] && sed "s:String VERSION_GITREV.*$:String VERSION_GITREV = \"$rev\";:" -i "$1"

tstamp=`git log -n 1 --format="%ct"`
[ "$tstamp" ] && sed "s:long VERSION_DATE.*$:long VERSION_DATE = $tstamp;:" -i "$1"

