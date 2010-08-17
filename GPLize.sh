#!/bin/bash

set -e

tmp=`mktemp`
find -type f -iname "*.java" -print0 | xargs -0rIX bash -c '
	grep -q "GNU General Public License" "X" || {
		cat GPL.txt "X" > "\$tmp"
		mv -f "\$tmp" "X"
	}
'

rm -f "$tmp"
