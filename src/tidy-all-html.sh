#!/bin/bash

function tidyDir() {
	find "$1" -type f -name "*.html" -print0 | xargs -0rIX bash -c "
		echo \"Processing 'X'\"
		tidy -c -i -m -q -u -w 120 -ashtml -utf8 'X'
		sed 's:Tidy for Linux/x86:Tidy for Linux:g' -i 'X'
	"
}

while [ "$1" ]; do
	tidyDir "$1"
	shift
done

echo "Done"
