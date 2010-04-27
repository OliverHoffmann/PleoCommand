#!/bin/bash

find "$1" -type f -name "*.html" -print0 | xargs -0rIX bash -c "
	echo \"Processing 'X'\"
	tidy -b -c -i -m -q -u -w 120 -ashtml -utf8 'X'
"

echo "Done"
