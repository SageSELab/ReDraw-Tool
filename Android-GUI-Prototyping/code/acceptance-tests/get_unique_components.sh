#!/bin/bash

NAMES=/tmp/names.txt

echo "" > "$NAMES"
for file in $(find -iname "ui-dump*"); do
	cat "$file" | grep -Po 'class="([^"]*)"' >> "$NAMES"
done

sort $NAMES | uniq | sed 's/class=\"\([^)]*\)\"/\1/'
