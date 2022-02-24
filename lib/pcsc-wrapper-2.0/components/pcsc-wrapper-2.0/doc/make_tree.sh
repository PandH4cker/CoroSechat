#!/bin/sh

echo "Making Tree description..."

cd ../..
tree pcsc-wrapper-2.0 > /tmp/tree.txt
mv -i /tmp/tree.txt pcsc-wrapper-2.0/doc/tree.txt

echo "Done."
