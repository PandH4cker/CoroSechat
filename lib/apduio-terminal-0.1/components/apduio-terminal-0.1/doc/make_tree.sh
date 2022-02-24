#!/bin/sh

echo "Making Tree description..."

cd ../..
tree apduio-terminal-0.1 > /tmp/tree.txt
mv -i /tmp/tree.txt apduio-terminal-0.1/doc/tree.txt

echo "Done."
